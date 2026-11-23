package ru.citymonitor.fact.repository;
import ru.citymonitor.fact.entity.Fact;
import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.data.domain.Page;import org.springframework.data.domain.Pageable;import org.springframework.data.jpa.repository.Query;import org.springframework.data.repository.query.Param;
import java.util.*; import java.time.OffsetDateTime;
public interface FactRepository extends JpaRepository<Fact,UUID> {
 List<Fact> findAllByObjectId(String objectId); List<Fact> findAllByDeletedAtIsNull(); List<Fact> findAllByDeletedAtIsNotNull();
 List<Fact> findAllByObjectIdAndDeletedAtIsNull(String objectId);
 List<Fact> findAllByOccurredAtBetween(OffsetDateTime from,OffsetDateTime to);
 @Query("select f from Fact f where (:objectId is null or f.objectId=:objectId) and (:eventType is null or f.eventType=:eventType) and (cast(:fromTime as timestamp) is null or f.occurredAt>=:fromTime) and (cast(:toTime as timestamp) is null or f.occurredAt<=:toTime) and ((:deleted=true and f.deletedAt is not null) or (:deleted=false and f.deletedAt is null)) order by f.occurredAt desc")
 Page<Fact> search(@Param("objectId") String objectId,@Param("eventType") String eventType,@Param("fromTime") OffsetDateTime from,@Param("toTime") OffsetDateTime to,@Param("deleted") boolean deleted,Pageable pageable);
}
