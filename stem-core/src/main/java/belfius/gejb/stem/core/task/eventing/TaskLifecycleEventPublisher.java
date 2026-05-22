package belfius.gejb.stem.core.task.eventing;

import belfius.gejb.stem.common.task.event.TaskLifecycleEvent;

public interface TaskLifecycleEventPublisher {

    TaskLifecycleEventPublishResult publish(TaskLifecycleEvent event);
}

