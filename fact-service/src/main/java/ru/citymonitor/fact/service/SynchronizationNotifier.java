package ru.citymonitor.fact.service;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service; import org.springframework.web.client.RestTemplate;
import ru.citymonitor.fact.dto.FactDtos.*; import java.time.OffsetDateTime;
@Service public class SynchronizationNotifier {
 private final RestTemplate http; private final String graphUrl;
 public SynchronizationNotifier(RestTemplate http,@Value("${integration.graph-service-url}") String graphUrl){this.http=http;this.graphUrl=graphUrl;}
 public void notify(long through){http.postForEntity(graphUrl+"/api/v1/synchronization/notifications",new Notification("fact-service",through,OffsetDateTime.now()),Void.class);}
}
