package ru.citymonitor.fact.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="fact")
public class Fact {
    @Id @Column(name="fact_id") private UUID factId;
    @Column(name="object_id", nullable=false,length=40) private String objectId;
    @Column(name="occurred_at", nullable=false) private OffsetDateTime occurredAt;
    @Column(name="event_type", nullable=false,length=40) private String eventType;
    @Column(nullable=false,length=20) private String severity;
    @Column(nullable=false,length=500) private String description;
    @Column(name="sensor_id",length=80) private String sensorId;
    @Column(nullable=false) private double latitude;
    @Column(nullable=false) private double longitude;
    @Column(name="created_at", nullable=false) private OffsetDateTime createdAt;
    @Column(name="deleted_at") private OffsetDateTime deletedAt;
    protected Fact() {}
    public Fact(UUID id,String objectId,OffsetDateTime occurredAt,String eventType,String severity,String description,String sensorId,double latitude,double longitude,OffsetDateTime createdAt){
        this.factId=id;this.objectId=objectId;this.occurredAt=occurredAt;this.eventType=eventType;this.severity=severity;this.description=description;this.sensorId=sensorId;this.latitude=latitude;this.longitude=longitude;this.createdAt=createdAt;
    }
    public void delete(OffsetDateTime at){this.deletedAt=at;}
    public UUID getFactId(){return factId;} public String getObjectId(){return objectId;} public OffsetDateTime getOccurredAt(){return occurredAt;} public String getEventType(){return eventType;} public String getSeverity(){return severity;} public String getDescription(){return description;} public String getSensorId(){return sensorId;} public double getLatitude(){return latitude;} public double getLongitude(){return longitude;} public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getDeletedAt(){return deletedAt;}
}
