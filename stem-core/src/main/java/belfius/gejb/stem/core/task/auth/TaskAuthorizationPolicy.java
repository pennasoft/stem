package belfius.gejb.stem.core.task.auth;

import belfius.gejb.stem.common.task.lifecycle.UserTaskAction;
import belfius.gejb.stem.core.task.persistence.UserTaskEntity;

public interface TaskAuthorizationPolicy {

    AuthorizationDecision authorize(UserTaskAction action, UserTaskEntity task, String actorId, String requestedOwnerUserId);
}

