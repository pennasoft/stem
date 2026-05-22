package belfius.gejb.stem.api.task.request;

import jakarta.validation.constraints.NotBlank;

public record TaskActionRequest(@NotBlank String actorId, String ownerUserId, String reason) {
}

