package ru.citymonitor.graph.dto;
import java.time.OffsetDateTime;import java.time.LocalDate;import java.util.*;
public final class GraphDtos {
 private GraphDtos(){}
 public record FactChangeDto(long sequenceId,UUID factId,String operation,OffsetDateTime operatedAt,String objectId,OffsetDateTime occurredAt,String eventType,String severity,String description,String sensorId,double latitude,double longitude,OffsetDateTime createdAt,OffsetDateTime deletedAt){}
 public record Notification(String source,long throughSequence,OffsetDateTime notifiedAt){}
 public record GraphNode(UUID factId,String objectId,OffsetDateTime occurredAt,String eventType,String severity,String description,String sensorId,double latitude,double longitude,boolean active,Integer position){}
 public record GraphEdge(UUID sourceFactId,UUID targetFactId,String edgeType){}
 public record GraphView(Long versionId,OffsetDateTime recordedAt,long lastSequence,List<GraphNode> nodes,List<GraphEdge> edges){}
 public record ChainView(String objectId,LocalDate asOf,Long versionId,OffsetDateTime versionRecordedAt,List<GraphNode> facts,List<GraphEdge> edges){}
 public record LifecycleEvent(String type,OffsetDateTime recordedAt,Long versionId,Long sourceSequence,Integer position,String reason){}
 public record Lifecycle(UUID factId,String objectId,boolean active,Integer currentPosition,List<LifecycleEvent> events){}
 public record VersionSummary(long versionId,OffsetDateTime recordedAt,long throughSequence,String source){}
 public record ChangeItem(long id,long versionId,long sourceSequence,OffsetDateTime recordedAt,String changeType,UUID factId,String objectId,Integer position,String edgeType,UUID relatedFactId){}
}
