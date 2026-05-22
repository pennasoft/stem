package belfius.gejb.stem.core.task.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserTaskRepository extends JpaRepository<UserTaskEntity, UUID> {

    boolean existsByTaskReference(String taskReference);

    @EntityGraph(attributePaths = {"correlation", "participants"})
    Optional<UserTaskEntity> findWithParticipantsById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"correlation", "participants"})
    @Query("select t from UserTaskEntity t where t.id = :id")
    Optional<UserTaskEntity> findWithParticipantsByIdForUpdate(@Param("id") UUID id);
}

