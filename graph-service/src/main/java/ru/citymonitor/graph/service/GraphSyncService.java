package ru.citymonitor.graph.service;

import org.springframework.beans.factory.annotation.Value;import org.springframework.core.ParameterizedTypeReference;import org.springframework.http.*;import org.springframework.stereotype.Service;import org.springframework.web.client.RestTemplate;
import ru.citymonitor.graph.dto.GraphDtos.*;import java.util.*;

@Service public class GraphSyncService {
 private final RestTemplate http;private final String factUrl;private final GraphProjectionWriter writer;
 public GraphSyncService(RestTemplate http,@Value("${integration.fact-service-url}") String factUrl,GraphProjectionWriter writer){this.http=http;this.factUrl=factUrl;this.writer=writer;}
 public long lastSequence(){return writer.lastSequence();}
 public void synchronize(Notification notification){long cursor=writer.lastSequence();if(notification.throughSequence()<=cursor)return;int limit=500;
  while(cursor<notification.throughSequence()){
   // Забираем только новые записи до границы, которую сообщил Fact Service.
   ResponseEntity<List<FactChangeDto>> response=http.exchange(factUrl+"/internal/v1/changes?afterSequence={after}&throughSequence={through}&limit={limit}",HttpMethod.GET,HttpEntity.EMPTY,new ParameterizedTypeReference<>(){},cursor,notification.throughSequence(),limit);
   List<FactChangeDto> page=response.getBody()==null?List.of():response.getBody();boolean finalPage=page.size()<limit;long newCursor=writer.applyPage(page,notification.throughSequence(),notification.source(),finalPage);if(newCursor<=cursor)break;cursor=newCursor;
   if(finalPage)break;
  }
 }
}
