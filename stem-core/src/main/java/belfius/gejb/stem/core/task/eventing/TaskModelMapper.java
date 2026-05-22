package belfius.gejb.stem.core.task.eventing;

import belfius.gejb.stem.common.task.model.CorrelationView;
import belfius.gejb.stem.common.task.model.TaskParticipantView;
import belfius.gejb.stem.common.task.model.UserTaskView;
import belfius.gejb.stem.core.task.persistence.CorrelationEntity;
import belfius.gejb.stem.core.task.persistence.TaskUserEntity;
import belfius.gejb.stem.core.task.persistence.UserTaskEntity;

import java.util.Comparator;
import java.util.List;

public final class TaskModelMapper {

    private TaskModelMapper() {
    }

    public static CorrelationView toView(CorrelationEntity entity) {
        return new CorrelationView(
                entity.getId(),
                entity.getReference(),
                entity.getEntityLabel(),
                entity.getType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy()
        );
    }

    public static UserTaskView toView(UserTaskEntity entity) {
        List<TaskParticipantView> participants = entity.getParticipants().stream()
                .sorted(Comparator.comparing(TaskUserEntity::getUserId).thenComparing(participant -> participant.getRole().name()))
                .map(participant -> new TaskParticipantView(participant.getUserId(), participant.getRole(), participant.isActive()))
                .toList();
        return new UserTaskView(
                entity.getId(),
                entity.getCorrelation().getId(),
                entity.getTaskReference(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getState(),
                entity.getOwnerUserId(),
                participants,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getCompletionOutcome(),
                entity.getLockVersion()
        );
    }
}

