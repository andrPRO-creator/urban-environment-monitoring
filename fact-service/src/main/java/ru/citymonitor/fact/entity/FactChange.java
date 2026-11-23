package ru.citymonitor.fact.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="fact_change")
public class FactChange {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="sequence_id") private Long sequenceId;
    @Column(name="fact_id",nullable=false) private UUID factId;
    @Column(nullable=false,length=12) private String operation;
    @Column(name="operated_at",nullable=false) private OffsetDateTime operatedAt;
    @Column(name="object_id",nullable=false,length=40) private String objectId;
    @Column(name="occurred_at",nullable=false) private OffsetDateTime occurredAt;
    @Column(name="event_type",nullable=false,length=40) private String eventType;
    @Column(nullable=false,length=20) private String severity;
    @Column(nullable=false,length=500) private String description;
    @Column(name="sensor_id",length=80) private String sensorId;
    @Column(nullable=false) private double latitude;
    @Column(nullable=false) private double longitude;
    @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
    @Column(name="deleted_at") private OffsetDateTime deletedAt;
    protected FactChange(){}
    public FactChange(Fact f,String operation,OffsetDateTime at){this.factId=f.getFactId();this.operation=operation;this.operatedAt=at;this.objectId=f.getObjectId();this.occurredAt=f.getOccurredAt();this.eventType=f.getEventType();this.severity=f.getSeverity();this.description=f.getDescription();this.sensorId=f.getSensorId();this.latitude=f.getLatitude();this.longitude=f.getLongitude();this.createdAt=f.getCreatedAt();this.deletedAt=f.getDeletedAt();}
    public Long getSequenceId(){return sequenceId;} public UUID getFactId(){return factId;} public String getOperation(){return operation;} public OffsetDateTime getOperatedAt(){return operatedAt;} public String getObjectId(){return objectId;} public OffsetDateTime getOccurredAt(){return occurredAt;} public String getEventType(){return eventType;} public String getSeverity(){return severity;} public String getDescription(){return description;} public String getSensorId(){return sensorId;} public double getLatitude(){return latitude;} public double getLongitude(){return longitude;} public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getDeletedAt(){return deletedAt;}
}
