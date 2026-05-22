package belfius.gejb.stem.core.task.persistence;

import belfius.gejb.stem.common.task.lifecycle.UserTaskAction;
import belfius.gejb.stem.common.task.lifecycle.UserTaskState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stem_user_task_history")
public class UserTaskLifecycleHistoryEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userTaskId;

    @Column(nullable = false)
    private UUID correlationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserTaskAction action;

    @Enumerated(EnumType.STRING)
    private UserTaskState fromState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserTaskState toState;

    @Column(nullable = false)
    private String actorUserId;

    private String reason;

    @Column(nullable = false)
    private Instant occurredAt;

    @Lob
    private String metadataJson;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserTaskId() {
        return userTaskId;
    }

    public void setUserTaskId(UUID userTaskId) {
        this.userTaskId = userTaskId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public UserTaskAction getAction() {
        return action;
    }

    public void setAction(UserTaskAction action) {
        this.action = action;
    }

    public UserTaskState getFromState() {
        return fromState;
    }

    public void setFromState(UserTaskState fromState) {
        this.fromState = fromState;
    }

    public UserTaskState getToState() {
        return toState;
    }

    public void setToState(UserTaskState toState) {
        this.toState = toState;
    }

    public String getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(String actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }
}

