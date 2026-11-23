package ru.citymonitor.graph.service;

import org.springframework.beans.factory.annotation.Value;import org.springframework.jdbc.core.*;import org.springframework.jdbc.support.GeneratedKeyHolder;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
import ru.citymonitor.graph.dto.GraphDtos.*;
import java.sql.*;import java.time.*;import java.util.*;import java.util.stream.*;

@Service public class GraphProjectionWriter {
 private final JdbcTemplate db;private final double maxMeters;private final long maxMinutes;
 public GraphProjectionWriter(JdbcTemplate db,@Value("${graph.nearby-meters}") double maxMeters,@Value("${graph.nearby-minutes}") long maxMinutes){this.db=db;this.maxMeters=maxMeters;this.maxMinutes=maxMinutes;}
 public long lastSequence(){return db.queryForObject("select last_sequence from sync_state where id=1",Long.class);}
 @Transactional
 public long applyPage(List<FactChangeDto> input,long through,String source,boolean finalPage){
  // Блокируем курсор, пока эта транзакция обновляет проекцию и снимки графа.
  long cursor=db.queryForObject("select last_sequence from sync_state where id=1 for update",Long.class);List<FactChangeDto> changes=input.stream().filter(c->c.sequenceId()>cursor).sorted(Comparator.comparingLong(FactChangeDto::sequenceId)).toList();
  if(changes.isEmpty()){if(finalPage)db.update("update sync_state set last_sequence=greatest(last_sequence,?) where id=1",through);return finalPage?Math.max(cursor,through):cursor;}
  // Сравниваем позиции и рёбра со старым состоянием, чтобы записать историю изменений.
  Map<UUID,Integer> oldPositions=new HashMap<>();Map<UUID,GraphNode> oldActive=loadNodes(true).stream().collect(Collectors.toMap(GraphNode::factId,n->n));
  oldActive.forEach((id,n)->oldPositions.put(id,n.position()));Set<GraphEdge> oldEdges=new HashSet<>(loadEdges());
  long version=db.queryForObject("insert into graph_version(recorded_at,through_sequence,source) values(?,?,?) returning version_id",Long.class,OffsetDateTime.now(ZoneOffset.UTC),through,source);
  Map<UUID,Long> touched=new HashMap<>();
  for(FactChangeDto c:changes){
   boolean already=db.query("select 1 from applied_change where sequence_id=?",(ResultSetExtractor<Boolean>)rs->rs.next(),c.sequenceId());if(already)continue;
   if("ADDED".equals(c.operation())) db.update("insert into graph_fact(fact_id,object_id,occurred_at,event_type,severity,description,sensor_id,latitude,longitude,created_at,registered_at,deleted_at,active,current_position) values(?,?,?,?,?,?,?,?,?,?,?,?,true,null) on conflict(fact_id) do update set active=true,deleted_at=null",c.factId(),c.objectId(),c.occurredAt(),c.eventType(),c.severity(),c.description(),c.sensorId(),c.latitude(),c.longitude(),c.createdAt(),c.operatedAt(),null);
   else if("DELETED".equals(c.operation()))db.update("update graph_fact set active=false,deleted_at=?,current_position=null where fact_id=?",c.deletedAt()==null?c.operatedAt():c.deletedAt(),c.factId());
   else throw new IllegalArgumentException("Неизвестная операция журнала: "+c.operation());
   db.update("insert into applied_change(sequence_id,fact_id,operation,applied_at) values(?,?,?,?)",c.sequenceId(),c.factId(),c.operation(),OffsetDateTime.now(ZoneOffset.UTC));touched.put(c.factId(),c.sequenceId());
  }
  List<GraphNode> all=loadNodes(false);List<GraphNode> active=all.stream().filter(GraphNode::active).sorted(Comparator.comparing(GraphNode::objectId).thenComparing(GraphNode::occurredAt).thenComparing(n->n.factId().toString())).toList();
  Map<UUID,Integer> positions=new HashMap<>();Map<String,Integer> counters=new HashMap<>();for(GraphNode n:active){int p=counters.merge(n.objectId(),1,Integer::sum);positions.put(n.factId(),p);}
  for(GraphNode n:all){Integer old=oldPositions.get(n.factId());Integer next=positions.get(n.factId());Long seq=touched.getOrDefault(n.factId(),through);
   if(next!=null)db.update("update graph_fact set current_position=? where fact_id=?",next,n.factId());else db.update("update graph_fact set current_position=null where fact_id=?",n.factId());
   if(!Objects.equals(old,next)){
    if(old!=null)db.update("update fact_position_history set valid_to_version=? where fact_id=? and valid_to_version is null",version,n.factId());
    if(next!=null){String reason=old==null?"REGISTERED":"POSITION_CHANGED";db.update("insert into fact_position_history(fact_id,object_id,position,valid_from_version,reason) values(?,?,?,?,?)",n.factId(),n.objectId(),next,version,reason);}
    if(old==null&&next!=null)change(version,seq,"NODE_ADDED",n.factId(),n.objectId(),next,null,null);else if(old!=null&&next==null)change(version,seq,"NODE_DELETED",n.factId(),n.objectId(),old,null,null);else if(old!=null)change(version,seq,"POSITION_CHANGED",n.factId(),n.objectId(),next,null,null);
   }
  }
  List<GraphNode> positioned=loadNodes(true).stream().map(n->new GraphNode(n.factId(),n.objectId(),n.occurredAt(),n.eventType(),n.severity(),n.description(),n.sensorId(),n.latitude(),n.longitude(),true,positions.get(n.factId()))).toList();
  Set<GraphEdge> newEdges=new HashSet<>(buildEdges(positioned));for(GraphEdge e:oldEdges)if(!newEdges.contains(e))change(version,through,"EDGE_REMOVED",e.sourceFactId(),null,null,e.edgeType(),e.targetFactId());for(GraphEdge e:newEdges)if(!oldEdges.contains(e))change(version,through,"EDGE_ADDED",e.sourceFactId(),null,null,e.edgeType(),e.targetFactId());
  db.update("delete from graph_node_snapshot where version_id=?",version);for(GraphNode n:all){boolean isActive=positions.containsKey(n.factId());Integer p=positions.get(n.factId());db.update("insert into graph_node_snapshot(version_id,fact_id,object_id,occurred_at,event_type,severity,description,sensor_id,latitude,longitude,active,position) values(?,?,?,?,?,?,?,?,?,?,?,?)",version,n.factId(),n.objectId(),n.occurredAt(),n.eventType(),n.severity(),n.description(),n.sensorId(),n.latitude(),n.longitude(),isActive,p);}
  for(GraphEdge e:newEdges)db.update("insert into graph_edge_snapshot(version_id,source_fact_id,target_fact_id,edge_type) values(?,?,?,?)",version,e.sourceFactId(),e.targetFactId(),e.edgeType());
  long next=finalPage?Math.max(through,changes.get(changes.size()-1).sequenceId()):changes.get(changes.size()-1).sequenceId();db.update("update sync_state set last_sequence=? where id=1",next);return next;
 }
 private void change(long version,long seq,String type,UUID fact,String object,Integer pos,String edge,UUID related){db.update("insert into graph_change(version_id,source_sequence,recorded_at,change_type,fact_id,object_id,position,edge_type,related_fact_id) values(?,?,?,?,?,?,?,?,?)",version,seq,OffsetDateTime.now(ZoneOffset.UTC),type,fact,object,pos,edge,related);}
 public List<GraphNode> loadNodes(boolean activeOnly){String sql="select fact_id,object_id,occurred_at,event_type,severity,description,sensor_id,latitude,longitude,active,current_position from graph_fact"+(activeOnly?" where active=true":"");return db.query(sql,(rs,i)->new GraphNode(rs.getObject(1,UUID.class),rs.getString(2),rs.getObject(3,OffsetDateTime.class),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7),rs.getDouble(8),rs.getDouble(9),rs.getBoolean(10),(Integer)rs.getObject(11)));}
 public List<GraphEdge> loadEdges(){return buildEdges(loadNodes(true));}
 public List<GraphEdge> buildEdges(List<GraphNode> nodes){List<GraphEdge> edges=new ArrayList<>();Map<String,List<GraphNode>> byObject=nodes.stream().collect(Collectors.groupingBy(GraphNode::objectId));for(List<GraphNode> list:byObject.values()){list.sort(Comparator.comparing(GraphNode::occurredAt).thenComparing(n->n.factId().toString()));for(int i=1;i<list.size();i++)edges.add(new GraphEdge(list.get(i-1).factId(),list.get(i).factId(),"NEXT_FOR_OBJECT"));}
  List<GraphNode> ordered=nodes.stream().sorted(Comparator.comparing(GraphNode::occurredAt).thenComparing(n->n.factId().toString())).toList();for(int i=0;i<ordered.size();i++)for(int j=i+1;j<ordered.size();j++){GraphNode a=ordered.get(i),b=ordered.get(j);if(a.objectId().equals(b.objectId()))continue;if(Duration.between(a.occurredAt(),b.occurredAt()).abs().compareTo(Duration.ofMinutes(maxMinutes))<=0&&distance(a.latitude(),a.longitude(),b.latitude(),b.longitude())<=maxMeters){edges.add(new GraphEdge(a.factId(),b.factId(),"NEARBY_IN_TIME"));}}
  return edges;
 }
 private double distance(double lat1,double lon1,double lat2,double lon2){double r=6371000,dLat=Math.toRadians(lat2-lat1),dLon=Math.toRadians(lon2-lon1);double a=Math.sin(dLat/2)*Math.sin(dLat/2)+Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(dLon/2)*Math.sin(dLon/2);return r*2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));}
}
