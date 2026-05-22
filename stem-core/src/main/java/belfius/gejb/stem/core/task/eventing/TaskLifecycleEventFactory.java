package belfius.gejb.stem.core.task.eventing;

import belfius.gejb.stem.common.task.auth.TaskUserRole;
import belfius.gejb.stem.common.task.event.TaskLifecycleEvent;
import belfius.gejb.stem.common.task.lifecycle.UserTaskAction;
import belfius.gejb.stem.common.task.lifecycle.UserTaskState;
import belfius.gejb.stem.core.task.persistence.CorrelationEntity;
import belfius.gejb.stem.core.task.persistence.UserTaskEntity;

import java.time.Instant;
import java.util.UUID;

public class TaskLifecycleEventFactory {

    public TaskLifecycleEvent create(
            UUID eventId,
            Instant occurredAt,
            UserTaskEntity task,
            UserTaskAction action,
            UserTaskState fromState,
            String actorUserId,
            TaskUserRole actorRole,
            String reason
    ) {
        CorrelationEntity correlation = task.getCorrelation();
        return new TaskLifecycleEvent(
                eventId,
                TaskLifecycleEvent.EVENT_TYPE,
                occurredAt,
                new TaskLifecycleEvent.Task(task.getId(), task.getTaskReference(), task.getState(), task.getOwnerUserId()),
                new TaskLifecycleEvent.Correlation(
                        correlation.getId(),
                        correlation.getReference(),
                        correlation.getEntityLabel(),
                        correlation.getType()
                ),
                new TaskLifecycleEvent.Transition(action, fromState, task.getState(), reason),
                new TaskLifecycleEvent.Actor(actorUserId, actorRole),
                new TaskLifecycleEvent.Delivery(null, null)
        );
    }
}

