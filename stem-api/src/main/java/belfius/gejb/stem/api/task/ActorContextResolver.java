package belfius.gejb.stem.api.task;

import jakarta.servlet.http.HttpServletRequest;

public interface ActorContextResolver {

    String resolveActorId(HttpServletRequest request, String requestedActorId);
}

