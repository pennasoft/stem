package belfius.gejb.stem.common.task.model;

import java.time.Instant;
import java.util.UUID;

public record CorrelationView(
        UUID id,
        String reference,
        String entity,
        String type,
        Instant createdAt,
        Instant updatedAt,
        String createdBy
) {
}

