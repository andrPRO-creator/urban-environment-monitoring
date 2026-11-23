package ru.citymonitor.fact.dto;

import ru.citymonitor.fact.entity.Fact;
import ru.citymonitor.fact.entity.FactChange;
import ru.citymonitor.fact.entity.MonitoredObject;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class FactDtos {
    private FactDtos() {}
    public record ObjectDto(String objectId,String name,String objectType,double latitude,double longitude,String description) {
        public static ObjectDto from(MonitoredObject o){return new ObjectDto(o.getObjectId(),o.getName(),o.getObjectType(),o.getLatitude(),o.getLongitude(),o.getDescription());}
    }
    public record FactDto(UUID factId,String objectId,OffsetDateTime occurredAt,String eventType,String severity,String description,String sensorId,double latitude,double longitude,OffsetDateTime createdAt,OffsetDateTime deletedAt) {
        public static FactDto from(Fact f){return new FactDto(f.getFactId(),f.getObjectId(),f.getOccurredAt(),f.getEventType(),f.getSeverity(),f.getDescription(),f.getSensorId(),f.getLatitude(),f.getLongitude(),f.getCreatedAt(),f.getDeletedAt());}
    }
    public record ChangeDto(long sequenceId,UUID factId,String operation,OffsetDateTime operatedAt,String objectId,OffsetDateTime occurredAt,String eventType,String severity,String description,String sensorId,double latitude,double longitude,OffsetDateTime createdAt,OffsetDateTime deletedAt) {
        public static ChangeDto from(FactChange c){return new ChangeDto(c.getSequenceId(),c.getFactId(),c.getOperation(),c.getOperatedAt(),c.getObjectId(),c.getOccurredAt(),c.getEventType(),c.getSeverity(),c.getDescription(),c.getSensorId(),c.getLatitude(),c.getLongitude(),c.getCreatedAt(),c.getDeletedAt());}
    }
    public record BatchResult(int added,int deleted,long throughSequence) {}
    public record Notification(String source,long throughSequence,OffsetDateTime notifiedAt) {}
}
