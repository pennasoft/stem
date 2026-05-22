package belfius.gejb.stem.common.task.model;

import belfius.gejb.stem.common.task.lifecycle.UserTaskState;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserTaskView(
        UUID id,
        UUID correlationId,
        String taskReference,
        String title,
        String description,
        UserTaskState state,
        String ownerUserId,
        List<TaskParticipantView> participants,
        Instant createdAt,
        Instant updatedAt,
        Instant startedAt,
        Instant completedAt,
        String completionOutcome,
        Long lockVersion
) {
}

