package belfius.gejb.stem.core.task.domain;

import belfius.gejb.stem.common.task.lifecycle.UserTaskState;
import belfius.gejb.stem.core.task.persistence.UserTaskEntity;

public class DefaultTaskLifecyclePolicy implements TaskLifecyclePolicy {

    @Override
    public UserTaskState refusalTargetState(UserTaskEntity task) {
        return UserTaskState.CREATED;
    }
}

