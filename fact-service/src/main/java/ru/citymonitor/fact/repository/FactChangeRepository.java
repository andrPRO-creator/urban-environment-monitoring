package ru.citymonitor.fact.repository;
import ru.citymonitor.fact.entity.FactChange; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.domain.Page;import org.springframework.data.domain.Pageable;import java.util.*;
public interface FactChangeRepository extends JpaRepository<FactChange,Long> { Page<FactChange> findBySequenceIdGreaterThanAndSequenceIdLessThanEqualOrderBySequenceIdAsc(long after,long through,Pageable pageable); long countBySequenceIdLessThanEqual(long sequence); Optional<FactChange> findTopByOrderBySequenceIdDesc(); }
