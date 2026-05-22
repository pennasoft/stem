package belfius.gejb.stem.common.task.auth;

import java.util.Objects;

public record ActorContext(String userId) {

    public ActorContext {
        Objects.requireNonNull(userId, "userId must not be null");
        if (userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
    }
}

