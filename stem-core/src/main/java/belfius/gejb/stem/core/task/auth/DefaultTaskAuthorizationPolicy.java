package belfius.gejb.stem.core.task.auth;

import belfius.gejb.stem.common.task.auth.TaskUserRole;
import belfius.gejb.stem.common.task.lifecycle.UserTaskAction;
import belfius.gejb.stem.core.task.persistence.TaskUserEntity;
import belfius.gejb.stem.core.task.persistence.UserTaskEntity;

public class DefaultTaskAuthorizationPolicy implements TaskAuthorizationPolicy {

    @Override
    public AuthorizationDecision authorize(UserTaskAction action, UserTaskEntity task, String actorId, String requestedOwnerUserId) {
        if (actorId == null || actorId.isBlank()) {
            return AuthorizationDecision.deny("Actor id is required");
        }

        TaskUserRole actorRole = resolveRole(task, actorId);
        if (action == UserTaskAction.CREATE) {
            return AuthorizationDecision.allow(actorRole);
        }
        if (actorRole == TaskUserRole.ADMINISTRATOR) {
            return AuthorizationDecision.allow(TaskUserRole.ADMINISTRATOR);
        }
        return switch (action) {
            case ASSIGN -> authorizeAssign(task, actorId, requestedOwnerUserId, actorRole);
            case REFUSE -> task.getOwnerUserId() != null && task.getOwnerUserId().equals(actorId)
                    ? AuthorizationDecision.allow(TaskUserRole.OWNER)
                    : AuthorizationDecision.deny("Only the current owner or an administrator may refuse the task");
            case START, COMPLETE -> task.getOwnerUserId() != null && task.getOwnerUserId().equals(actorId)
                    ? AuthorizationDecision.allow(TaskUserRole.OWNER)
                    : AuthorizationDecision.deny("Only the current owner or an administrator may continue this task");
            default -> AuthorizationDecision.deny("Unsupported action");
        };
    }

    private AuthorizationDecision authorizeAssign(UserTaskEntity task, String actorId, String requestedOwnerUserId, TaskUserRole actorRole) {
        if (requestedOwnerUserId == null || requestedOwnerUserId.isBlank()) {
            return AuthorizationDecision.deny("An ownerUserId is required for assignment");
        }
        if (requestedOwnerUserId.equals(actorId) && (actorRole == TaskUserRole.POTENTIAL_OWNER || actorRole == TaskUserRole.OWNER)) {
            return AuthorizationDecision.allow(actorRole);
        }
        return AuthorizationDecision.deny("Only a potential owner claiming the task or an administrator may assign it");
    }

    private TaskUserRole resolveRole(UserTaskEntity task, String actorId) {
        return task.getParticipants().stream()
                .filter(TaskUserEntity::isActive)
                .filter(participant -> participant.getUserId().equals(actorId))
                .map(TaskUserEntity::getRole)
                .sorted((left, right) -> Integer.compare(priority(right), priority(left)))
                .findFirst()
                .orElse(null);
    }

    private int priority(TaskUserRole role) {
        if (role == null) {
            return 0;
        }
        return switch (role) {
            case ADMINISTRATOR -> 3;
            case OWNER -> 2;
            case POTENTIAL_OWNER -> 1;
        };
    }
}


