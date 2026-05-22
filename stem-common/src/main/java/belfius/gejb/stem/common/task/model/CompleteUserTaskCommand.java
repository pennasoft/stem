package belfius.gejb.stem.common.task.model;

public record CompleteUserTaskCommand(String actorId, String reason, String completionOutcome)
        implements TaskOperationCommand {

    public CompleteUserTaskCommand {
        if (actorId == null || actorId.isBlank()) {
            throw new IllegalArgumentException("actorId must not be blank");
        }
    }

    @Override
    public String ownerUserId() {
        return null;
    }
}

