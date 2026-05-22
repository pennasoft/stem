package belfius.gejb.stem.api.task.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCorrelationRequest(
        @NotBlank String reference,
        @NotBlank String entity,
        @NotBlank String type,
        String createdBy
) {
}

