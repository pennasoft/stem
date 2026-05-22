package belfius.gejb.stem.core.task.persistence;

import belfius.gejb.stem.common.task.auth.TaskUserRole;
import belfius.gejb.stem.common.task.lifecycle.UserTaskState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stem_user_task", uniqueConstraints = {
        @UniqueConstraint(name = "uk_stem_user_task_reference", columnNames = "task_reference")
})
public class UserTaskEntity {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "correlation_id")
    private CorrelationEntity correlation;

    @Column(name = "task_reference", nullable = false)
    private String taskReference;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserTaskState state;

    private String ownerUserId;

    @Version
    @Column(nullable = false)
    private Long lockVersion;

    @Column(nullable = false)
    private Instant createdAt;

    private String createdBy;

    @Column(nullable = false)
    private Instant updatedAt;

    private Instant startedAt;

    private Instant completedAt;

    private String completionOutcome;

    private Instant lastRefusedAt;

    @OneToMany(mappedBy = "userTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskUserEntity> participants = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CorrelationEntity getCorrelation() {
        return correlation;
    }

    public void setCorrelation(CorrelationEntity correlation) {
        this.correlation = correlation;
    }

    public String getTaskReference() {
        return taskReference;
    }

    public void setTaskReference(String taskReference) {
        this.taskReference = taskReference;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserTaskState getState() {
        return state;
    }

    public void setState(UserTaskState state) {
        this.state = state;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public Long getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(Long lockVersion) {
        this.lockVersion = lockVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getCompletionOutcome() {
        return completionOutcome;
    }

    public void setCompletionOutcome(String completionOutcome) {
        this.completionOutcome = completionOutcome;
    }

    public Instant getLastRefusedAt() {
        return lastRefusedAt;
    }

    public void setLastRefusedAt(Instant lastRefusedAt) {
        this.lastRefusedAt = lastRefusedAt;
    }

    public List<TaskUserEntity> getParticipants() {
        return participants;
    }

    public void setParticipants(List<TaskUserEntity> participants) {
        this.participants = participants;
    }

    public void addParticipant(TaskUserEntity participant) {
        participant.setUserTask(this);
        participants.add(participant);
    }

    public TaskUserEntity findParticipant(String userId, TaskUserRole role) {
        return participants.stream()
                .filter(participant -> participant.getUserId().equals(userId) && participant.getRole() == role)
                .findFirst()
                .orElse(null);
    }
}

