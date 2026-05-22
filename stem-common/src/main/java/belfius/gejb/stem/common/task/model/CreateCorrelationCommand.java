package belfius.gejb.stem.common.task.model;

import java.util.Objects;

public record CreateCorrelationCommand(String reference, String entity, String type, String createdBy) {

    public CreateCorrelationCommand {
        requireText(reference, "reference");
        requireText(entity, "entity");
        requireText(type, "type");
    }

    private static void requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}

