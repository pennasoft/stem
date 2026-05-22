package belfius.gejb.stem.common.task.model;

public sealed interface TaskOperationCommand permits CompleteUserTaskCommand, TaskActionCommand {

    String actorId();

    String ownerUserId();

    String reason();
}

