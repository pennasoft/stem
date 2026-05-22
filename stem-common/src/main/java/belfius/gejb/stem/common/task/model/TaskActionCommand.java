package belfius.gejb.stem.common.task.model;

import java.util.Objects;

public record TaskActionCommand(String actorId, String ownerUserId, String reason) implements TaskOperationCommand {

    public TaskActionCommand {
        Objects.requireNonNull(actorId, "actorId must not be null");
        if (actorId.isBlank()) {
            throw new IllegalArgumentException("actorId must not be blank");
        }
    }
}


