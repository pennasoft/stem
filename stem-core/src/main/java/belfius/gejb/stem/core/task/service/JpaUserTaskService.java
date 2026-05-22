package belfius.gejb.stem.core.task.service;

import belfius.gejb.stem.common.task.auth.TaskUserRole;
import belfius.gejb.stem.common.task.event.TaskLifecycleEvent;
import belfius.gejb.stem.common.task.lifecycle.UserTaskAction;
import belfius.gejb.stem.common.task.lifecycle.UserTaskState;
import belfius.gejb.stem.common.task.model.CompleteUserTaskCommand;
import belfius.gejb.stem.common.task.model.CreateUserTaskCommand;
import belfius.gejb.stem.common.task.model.TaskActionCommand;
import belfius.gejb.stem.common.task.model.TaskOperationCommand;
import belfius.gejb.stem.common.task.model.TaskRejectionCode;
import belfius.gejb.stem.common.task.model.UserTaskOperationResult;
import belfius.gejb.stem.common.task.model.UserTaskView;
import belfius.gejb.stem.core.task.auth.AuthorizationDecision;
import belfius.gejb.stem.core.task.auth.TaskAuthorizationPolicy;
import belfius.gejb.stem.core.task.domain.TaskLifecyclePolicy;
import belfius.gejb.stem.core.task.eventing.TaskLifecycleEventFactory;
import belfius.gejb.stem.core.task.eventing.TaskModelMapper;
import belfius.gejb.stem.core.task.persistence.CorrelationEntity;
import belfius.gejb.stem.core.task.persistence.CorrelationRepository;
import belfius.gejb.stem.core.task.persistence.TaskOutboxStatus;
import belfius.gejb.stem.core.task.persistence.TaskUserEntity;
import belfius.gejb.stem.core.task.persistence.UserTaskEntity;
import belfius.gejb.stem.core.task.persistence.UserTaskLifecycleHistoryEntity;
import belfius.gejb.stem.core.task.persistence.UserTaskLifecycleHistoryRepository;
import belfius.gejb.stem.core.task.persistence.UserTaskOutboxEventEntity;
import belfius.gejb.stem.core.task.persistence.UserTaskOutboxEventRepository;
import belfius.gejb.stem.core.task.persistence.UserTaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

public class JpaUserTaskService implements UserTaskService {

    private final CorrelationRepository correlationRepository;
    private final UserTaskRepository userTaskRepository;
    private final UserTaskLifecycleHistoryRepository historyRepository;
    private final UserTaskOutboxEventRepository outboxRepository;
    private final TaskAuthorizationPolicy authorizationPolicy;
    private final TaskLifecyclePolicy lifecyclePolicy;
    private final TaskLifecycleEventFactory eventFactory;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public JpaUserTaskService(
            CorrelationRepository correlationRepository,
            UserTaskRepository userTaskRepository,
            UserTaskLifecycleHistoryRepository historyRepository,
            UserTaskOutboxEventRepository outboxRepository,
            TaskAuthorizationPolicy authorizationPolicy,
            TaskLifecyclePolicy lifecyclePolicy,
            TaskLifecycleEventFactory eventFactory,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.correlationRepository = correlationRepository;
        this.userTaskRepository = userTaskRepository;
        this.historyRepository = historyRepository;
        this.outboxRepository = outboxRepository;
        this.authorizationPolicy = authorizationPolicy;
        this.lifecyclePolicy = lifecyclePolicy;
        this.eventFactory = eventFactory;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UserTaskOperationResult createTask(CreateUserTaskCommand command) {
        CorrelationEntity correlation = correlationRepository.findById(command.correlationId()).orElse(null);
        if (correlation == null) {
            return UserTaskOperationResult.failure(null, command.correlationId(), null, TaskRejectionCode.CORRELATION_NOT_FOUND,
                    "The referenced correlation does not exist");
        }
        if (userTaskRepository.existsByTaskReference(command.taskReference())) {
            return UserTaskOperationResult.failure(null, correlation.getId(), null, TaskRejectionCode.DUPLICATE_TASK_REFERENCE,
                    "Task reference already exists");
        }

        Instant now = Instant.now(clock);
        UserTaskEntity task = new UserTaskEntity();
        task.setId(UUID.randomUUID());
        task.setCorrelation(correlation);
        task.setTaskReference(command.taskReference());
        task.setTitle(command.title());
        task.setDescription(command.description());
        task.setState(UserTaskState.CREATED);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        task.setCreatedBy(command.actorId());
        seedParticipants(task, command, now);

        try {
            userTaskRepository.save(task);
            persistMutation(task, UserTaskAction.CREATE, null, command.actorId(), null, null, now);
            return UserTaskOperationResult.success(task.getId(), correlation.getId(), task.getState(), "Task created");
        } catch (DataIntegrityViolationException ex) {
            return UserTaskOperationResult.failure(null, correlation.getId(), null, TaskRejectionCode.DUPLICATE_TASK_REFERENCE,
                    "Task reference already exists");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserTaskView getTask(UUID taskId) {
        return userTaskRepository.findWithParticipantsById(taskId)
                .map(TaskModelMapper::toView)
                .orElseThrow(() -> new TaskNotFoundException("Task not found: " + taskId));
    }

    @Override
    @Transactional
    public UserTaskOperationResult assignTask(UUID taskId, TaskActionCommand command) {
        return mutateTask(taskId, UserTaskAction.ASSIGN, command, (task, now) -> {
            requireState(task, UserTaskState.CREATED, "Only created tasks can be assigned");
            String ownerUserId = requireText(command.ownerUserId(), "ownerUserId is required for assignment");
            if (!hasEligibleOwner(task, ownerUserId)) {
                throw new IllegalStateException("Requested owner must be an active owner or potential owner participant");
            }
            deactivateOtherOwners(task, ownerUserId, now);
            ensureOwnerParticipant(task, ownerUserId, command.actorId(), now);
            task.setOwnerUserId(ownerUserId);
            task.setState(UserTaskState.ASSIGNED);
        });
    }

    @Override
    @Transactional
    public UserTaskOperationResult refuseTask(UUID taskId, TaskActionCommand command) {
        return mutateTask(taskId, UserTaskAction.REFUSE, command, (task, now) -> {
            requireState(task, UserTaskState.ASSIGNED, "Only assigned tasks can be refused");
            deactivateOtherOwners(task, null, now);
            task.setOwnerUserId(null);
            task.setLastRefusedAt(now);
            task.setState(lifecyclePolicy.refusalTargetState(task));
        });
    }

    @Override
    @Transactional
    public UserTaskOperationResult startTask(UUID taskId, TaskActionCommand command) {
        return mutateTask(taskId, UserTaskAction.START, command, (task, now) -> {
            requireState(task, UserTaskState.ASSIGNED, "Only assigned tasks can be started");
            task.setState(UserTaskState.IN_PROGRESS);
            if (task.getStartedAt() == null) {
                task.setStartedAt(now);
            }
        });
    }

    @Override
    @Transactional
    public UserTaskOperationResult completeTask(UUID taskId, CompleteUserTaskCommand command) {
        return mutateTask(taskId, UserTaskAction.COMPLETE, command, (task, now) -> {
            requireState(task, UserTaskState.IN_PROGRESS, "Only in-progress tasks can be completed");
            task.setState(UserTaskState.COMPLETED);
            task.setCompletedAt(now);
            task.setCompletionOutcome(command.completionOutcome());
        });
    }

    private UserTaskOperationResult mutateTask(
            UUID taskId,
            UserTaskAction action,
            TaskOperationCommand command,
            BiConsumer<UserTaskEntity, Instant> mutation
    ) {
        UserTaskEntity task;
        try {
            task = userTaskRepository.findWithParticipantsByIdForUpdate(taskId).orElse(null);
        } catch (CannotAcquireLockException | OptimisticLockingFailureException ex) {
            return UserTaskOperationResult.failure(taskId, null, null, TaskRejectionCode.CONCURRENCY_CONFLICT,
                    "Another runtime is currently changing this task");
        }
        if (task == null) {
            return UserTaskOperationResult.failure(taskId, null, null, TaskRejectionCode.TASK_NOT_FOUND, "Task not found");
        }

        AuthorizationDecision decision = authorizationPolicy.authorize(action, task, command.actorId(), command.ownerUserId());
        if (!decision.allowed()) {
            return UserTaskOperationResult.failure(task.getId(), task.getCorrelation().getId(), task.getState(),
                    decision.rejectionCode(), decision.message());
        }

        Instant now = Instant.now(clock);
        UserTaskState fromState = task.getState();
        try {
            mutation.accept(task, now);
            task.setUpdatedAt(now);
            userTaskRepository.save(task);
            persistMutation(task, action, fromState, command.actorId(), decision.matchedRole(), command.reason(), now);
            return UserTaskOperationResult.success(task.getId(), task.getCorrelation().getId(), task.getState(),
                    "Task action " + action.name().toLowerCase() + " accepted");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return UserTaskOperationResult.failure(task.getId(), task.getCorrelation().getId(), task.getState(),
                    TaskRejectionCode.INVALID_STATE, ex.getMessage());
        } catch (CannotAcquireLockException | OptimisticLockingFailureException ex) {
            return UserTaskOperationResult.failure(task.getId(), task.getCorrelation().getId(), task.getState(),
                    TaskRejectionCode.CONCURRENCY_CONFLICT, "Another runtime committed a conflicting change");
        }
    }

    private void persistMutation(
            UserTaskEntity task,
            UserTaskAction action,
            UserTaskState fromState,
            String actorId,
            TaskUserRole actorRole,
            String reason,
            Instant occurredAt
    ) {
        UserTaskLifecycleHistoryEntity history = new UserTaskLifecycleHistoryEntity();
        history.setId(UUID.randomUUID());
        history.setUserTaskId(task.getId());
        history.setCorrelationId(task.getCorrelation().getId());
        history.setAction(action);
        history.setFromState(fromState);
        history.setToState(task.getState());
        history.setActorUserId(actorId);
        history.setReason(reason);
        history.setOccurredAt(occurredAt);
        historyRepository.save(history);

        UUID eventId = UUID.randomUUID();
        TaskLifecycleEvent event = eventFactory.create(eventId, occurredAt, task, action, fromState, actorId, actorRole, reason);
        UserTaskOutboxEventEntity outboxEvent = new UserTaskOutboxEventEntity();
        outboxEvent.setId(eventId);
        outboxEvent.setUserTaskId(task.getId());
        outboxEvent.setCorrelationId(task.getCorrelation().getId());
        outboxEvent.setEventType(TaskLifecycleEvent.EVENT_TYPE);
        outboxEvent.setPayloadJson(serialize(event));
        outboxEvent.setStatus(TaskOutboxStatus.PENDING);
        outboxEvent.setRetryCount(0);
        outboxEvent.setAvailableAt(occurredAt);
        outboxEvent.setCreatedAt(occurredAt);
        outboxRepository.save(outboxEvent);
    }

    private String serialize(TaskLifecycleEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize lifecycle event", ex);
        }
    }

    private void seedParticipants(UserTaskEntity task, CreateUserTaskCommand command, Instant now) {
        Set<String> potentialOwners = new HashSet<>(command.potentialOwnerUserIds());
        for (String potentialOwner : potentialOwners) {
            addParticipant(task, potentialOwner, TaskUserRole.POTENTIAL_OWNER, command.actorId(), now);
        }
        if (command.ownerUserId() != null && !command.ownerUserId().isBlank()) {
            addParticipant(task, command.ownerUserId(), TaskUserRole.OWNER, command.actorId(), now);
        }
        Set<String> administrators = new HashSet<>(command.administratorUserIds());
        for (String administrator : administrators) {
            addParticipant(task, administrator, TaskUserRole.ADMINISTRATOR, command.actorId(), now);
        }
    }

    private void addParticipant(UserTaskEntity task, String userId, TaskUserRole role, String createdBy, Instant now) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        TaskUserEntity existing = task.findParticipant(userId, role);
        if (existing != null) {
            existing.setActive(true);
            existing.setUpdatedAt(now);
            return;
        }
        TaskUserEntity participant = new TaskUserEntity();
        participant.setId(UUID.randomUUID());
        participant.setUserTask(task);
        participant.setUserId(userId);
        participant.setRole(role);
        participant.setActive(true);
        participant.setCreatedAt(now);
        participant.setUpdatedAt(now);
        participant.setCreatedBy(createdBy);
        task.addParticipant(participant);
    }

    private boolean hasEligibleOwner(UserTaskEntity task, String ownerUserId) {
        return task.getParticipants().stream()
                .anyMatch(participant -> participant.isActive()
                        && participant.getUserId().equals(ownerUserId)
                        && (participant.getRole() == TaskUserRole.OWNER || participant.getRole() == TaskUserRole.POTENTIAL_OWNER));
    }

    private void deactivateOtherOwners(UserTaskEntity task, String retainedOwnerUserId, Instant now) {
        task.getParticipants().stream()
                .filter(participant -> participant.getRole() == TaskUserRole.OWNER)
                .filter(participant -> retainedOwnerUserId == null || !participant.getUserId().equals(retainedOwnerUserId))
                .forEach(participant -> {
                    participant.setActive(false);
                    participant.setUpdatedAt(now);
                });
    }

    private void ensureOwnerParticipant(UserTaskEntity task, String ownerUserId, String actorId, Instant now) {
        TaskUserEntity owner = task.findParticipant(ownerUserId, TaskUserRole.OWNER);
        if (owner == null) {
            addParticipant(task, ownerUserId, TaskUserRole.OWNER, actorId, now);
            owner = task.findParticipant(ownerUserId, TaskUserRole.OWNER);
        }
        owner.setActive(true);
        owner.setUpdatedAt(now);
    }

    private void requireState(UserTaskEntity task, UserTaskState expected, String message) {
        if (task.getState() != expected) {
            throw new IllegalStateException(message);
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}


