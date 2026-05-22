package belfius.gejb.stem.common.task.model;

import belfius.gejb.stem.common.task.lifecycle.UserTaskState;

import java.util.UUID;

public record UserTaskOperationResult(
        boolean success,
        UUID taskId,
        UserTaskState state,
        TaskRejectionCode rejectionCode,
        String message,
        UUID correlationId,
        boolean eventQueued
) {

    public static UserTaskOperationResult success(UUID taskId, UUID correlationId, UserTaskState state, String message) {
        return new UserTaskOperationResult(true, taskId, state, null, message, correlationId, true);
    }

    public static UserTaskOperationResult failure(
            UUID taskId,
            UUID correlationId,
            UserTaskState state,
            TaskRejectionCode rejectionCode,
            String message
    ) {
        return new UserTaskOperationResult(false, taskId, state, rejectionCode, message, correlationId, false);
    }
}

