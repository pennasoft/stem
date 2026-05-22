package belfius.gejb.stem.core.task.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CorrelationRepository extends JpaRepository<CorrelationEntity, UUID> {

    boolean existsByReferenceAndType(String reference, String type);

    Optional<CorrelationEntity> findByReferenceAndType(String reference, String type);
}

