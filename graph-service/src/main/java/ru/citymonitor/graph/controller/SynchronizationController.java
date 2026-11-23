package ru.citymonitor.graph.controller;
import io.swagger.v3.oas.annotations.Operation;import io.swagger.v3.oas.annotations.tags.Tag;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import org.springframework.web.bind.annotation.*;import ru.citymonitor.graph.dto.GraphDtos.Notification;import ru.citymonitor.graph.service.GraphSyncService;import java.util.Map;
@RestController @RequestMapping("/api/v1/synchronization/notifications") @Tag(name="Internal synchronization",description="Синхронный внутренний HTTP-контракт с Fact Service") public class SynchronizationController {
 public record NotificationRequest(@NotBlank String source,@PositiveOrZero long throughSequence,@NotNull java.time.OffsetDateTime notifiedAt){}
 private final GraphSyncService sync;public SynchronizationController(GraphSyncService sync){this.sync=sync;}
 @PostMapping @Operation(summary="Внутренний синхронный вызов из Fact Service") public Map<String,Object> notify(@Valid @RequestBody NotificationRequest request){sync.synchronize(new Notification(request.source(),request.throughSequence(),request.notifiedAt()));return Map.of("status","APPLIED","lastSequence",sync.lastSequence());}
}
