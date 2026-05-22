package belfius.gejb.stem.api.task;

import jakarta.servlet.http.HttpServletRequest;

public class DefaultActorContextResolver implements ActorContextResolver {

    @Override
    public String resolveActorId(HttpServletRequest request, String requestedActorId) {
        if (requestedActorId == null || requestedActorId.isBlank()) {
            throw new IllegalArgumentException("actorId must not be blank");
        }
        return requestedActorId;
    }
}

