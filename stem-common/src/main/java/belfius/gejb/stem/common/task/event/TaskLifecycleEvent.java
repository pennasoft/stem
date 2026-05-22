package belfius.gejb.stem.common.task.event;

import belfius.gejb.stem.common.task.auth.TaskUserRole;
import belfius.gejb.stem.common.task.lifecycle.UserTaskAction;
import belfius.gejb.stem.common.task.lifecycle.UserTaskState;

import java.time.Instant;
import java.util.UUID;

public record TaskLifecycleEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Task task,
        Correlation correlation,
        Transition transition,
        Actor actor,
        Delivery delivery
) {
    public static final String EVENT_TYPE = "stem.task.lifecycle.changed.v1";

    public record Task(UUID id, String reference, UserTaskState state, String ownerUserId) {
    }

    public record Correlation(UUID id, String reference, String entity, String type) {
    }

    public record Transition(UserTaskAction action, UserTaskState fromState, UserTaskState toState, String reason) {
    }

    public record Actor(String userId, TaskUserRole role) {
    }

    public record Delivery(Long sequence, String publisher) {
    }
}

