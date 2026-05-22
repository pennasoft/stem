package belfius.gejb.stem.common.task.model;

import belfius.gejb.stem.common.task.auth.TaskUserRole;

public record TaskParticipantView(String userId, TaskUserRole role, boolean active) {
}

