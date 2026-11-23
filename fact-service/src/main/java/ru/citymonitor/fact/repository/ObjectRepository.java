package ru.citymonitor.fact.repository;
import ru.citymonitor.fact.entity.MonitoredObject; import org.springframework.data.jpa.repository.JpaRepository;
public interface ObjectRepository extends JpaRepository<MonitoredObject,String> {}
