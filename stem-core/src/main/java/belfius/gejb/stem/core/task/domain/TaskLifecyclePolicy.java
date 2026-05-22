package belfius.gejb.stem.core.task.domain;

import belfius.gejb.stem.common.task.lifecycle.UserTaskState;
import belfius.gejb.stem.core.task.persistence.UserTaskEntity;

public interface TaskLifecyclePolicy {

    UserTaskState refusalTargetState(UserTaskEntity task);
}

