package belfius.gejb.stem.core.task.service;

import belfius.gejb.stem.common.task.model.CompleteUserTaskCommand;
import belfius.gejb.stem.common.task.model.CreateUserTaskCommand;
import belfius.gejb.stem.common.task.model.TaskActionCommand;
import belfius.gejb.stem.common.task.model.UserTaskOperationResult;
import belfius.gejb.stem.common.task.model.UserTaskView;

import java.util.UUID;

public interface UserTaskService {

    UserTaskOperationResult createTask(CreateUserTaskCommand command);

    UserTaskView getTask(UUID taskId);

    UserTaskOperationResult assignTask(UUID taskId, TaskActionCommand command);

    UserTaskOperationResult refuseTask(UUID taskId, TaskActionCommand command);

    UserTaskOperationResult startTask(UUID taskId, TaskActionCommand command);

    UserTaskOperationResult completeTask(UUID taskId, CompleteUserTaskCommand command);
}

