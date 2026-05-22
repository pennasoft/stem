package belfius.gejb.stem.common.task.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CreateUserTaskCommand(
        UUID correlationId,
        String taskReference,
        String title,
        String description,
        String actorId,
        String ownerUserId,
        List<String> potentialOwnerUserIds,
        List<String> administratorUserIds
) {

    public CreateUserTaskCommand {
        Objects.requireNonNull(correlationId, "correlationId must not be null");
        requireText(taskReference, "taskReference");
        requireText(title, "title");
        requireText(actorId, "actorId");
        potentialOwnerUserIds = potentialOwnerUserIds == null ? List.of() : List.copyOf(potentialOwnerUserIds);
        administratorUserIds = administratorUserIds == null ? List.of() : List.copyOf(administratorUserIds);
    }

    private static void requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}

