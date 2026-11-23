package ru.citymonitor.graph.entity;
import jakarta.persistence.*;
@Entity @Table(name="sync_state") public class SyncState {@Id private Integer id;@Column(name="last_sequence",nullable=false)private long lastSequence;protected SyncState(){}public Integer getId(){return id;}public long getLastSequence(){return lastSequence;}}
