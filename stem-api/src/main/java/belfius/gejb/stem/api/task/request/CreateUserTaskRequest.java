package belfius.gejb.stem.api.task.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateUserTaskRequest(
        @NotNull UUID correlationId,
        @NotBlank String taskReference,
        @NotBlank String title,
        String description,
        @NotBlank String actorId,
        String ownerUserId,
        List<String> potentialOwnerUserIds,
        List<String> administratorUserIds
) {
}

