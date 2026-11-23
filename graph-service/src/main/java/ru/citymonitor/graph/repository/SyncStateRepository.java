package ru.citymonitor.graph.repository;
import ru.citymonitor.graph.entity.SyncState;import org.springframework.data.jpa.repository.JpaRepository;
public interface SyncStateRepository extends JpaRepository<SyncState,Integer>{}
