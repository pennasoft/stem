package belfius.gejb.stem.api.task.request;

import jakarta.validation.constraints.NotBlank;

public record CompleteTaskRequest(@NotBlank String actorId, String reason, String completionOutcome) {
}

