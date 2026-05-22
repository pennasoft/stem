package belfius.gejb.stem.core.task.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserTaskOutboxEventRepository extends JpaRepository<UserTaskOutboxEventEntity, UUID> {

    @Query("select e from UserTaskOutboxEventEntity e where e.status in ('PENDING', 'FAILED') and e.availableAt <= :availableAt order by e.createdAt asc")
    List<UserTaskOutboxEventEntity> findReadyForPublication(@Param("availableAt") Instant availableAt);
}

