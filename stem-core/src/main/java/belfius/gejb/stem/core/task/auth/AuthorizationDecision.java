package belfius.gejb.stem.core.task.auth;

import belfius.gejb.stem.common.task.auth.TaskUserRole;
import belfius.gejb.stem.common.task.model.TaskRejectionCode;

public record AuthorizationDecision(boolean allowed, TaskRejectionCode rejectionCode, String message, TaskUserRole matchedRole) {

    public static AuthorizationDecision allow(TaskUserRole matchedRole) {
        return new AuthorizationDecision(true, null, null, matchedRole);
    }

    public static AuthorizationDecision deny(String message) {
        return new AuthorizationDecision(false, TaskRejectionCode.UNAUTHORIZED, message, null);
    }
}

