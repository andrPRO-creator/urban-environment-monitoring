package ru.citymonitor.fact.entity;

import jakarta.persistence.*;

@Entity @Table(name="monitored_object")
public class MonitoredObject {
    @Id @Column(name="object_id") private String objectId;
    @Column(length=120) private String name;
    @Column(name="object_type",length=40) private String objectType;
    private double latitude;
    private double longitude;
    @Column(length=500) private String description;
    protected MonitoredObject() {}
    public String getObjectId(){return objectId;} public String getName(){return name;} public String getObjectType(){return objectType;}
    public double getLatitude(){return latitude;} public double getLongitude(){return longitude;} public String getDescription(){return description;}
}
