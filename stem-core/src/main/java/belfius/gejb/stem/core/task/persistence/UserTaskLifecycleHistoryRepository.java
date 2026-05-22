package belfius.gejb.stem.core.task.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserTaskLifecycleHistoryRepository extends JpaRepository<UserTaskLifecycleHistoryEntity, UUID> {
}

