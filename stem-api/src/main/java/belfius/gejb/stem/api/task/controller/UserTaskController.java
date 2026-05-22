package belfius.gejb.stem.api.task.controller;

import belfius.gejb.stem.api.task.ActorContextResolver;
import belfius.gejb.stem.api.task.request.CompleteTaskRequest;
import belfius.gejb.stem.api.task.request.CreateUserTaskRequest;
import belfius.gejb.stem.api.task.request.TaskActionRequest;
import belfius.gejb.stem.common.task.model.CompleteUserTaskCommand;
import belfius.gejb.stem.common.task.model.CreateUserTaskCommand;
import belfius.gejb.stem.common.task.model.TaskActionCommand;
import belfius.gejb.stem.common.task.model.UserTaskOperationResult;
import belfius.gejb.stem.common.task.model.UserTaskView;
import belfius.gejb.stem.core.task.service.UserTaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/stem/tasks")
public class UserTaskController {

    private final UserTaskService userTaskService;
    private final ActorContextResolver actorContextResolver;

    public UserTaskController(UserTaskService userTaskService, ActorContextResolver actorContextResolver) {
        this.userTaskService = userTaskService;
        this.actorContextResolver = actorContextResolver;
    }

    @PostMapping
    public ResponseEntity<UserTaskOperationResult> createTask(
            @Valid @RequestBody CreateUserTaskRequest request,
            HttpServletRequest httpRequest
    ) {
        String actorId = actorContextResolver.resolveActorId(httpRequest, request.actorId());
        UserTaskOperationResult result = userTaskService.createTask(new CreateUserTaskCommand(
                request.correlationId(),
                request.taskReference(),
                request.title(),
                request.description(),
                actorId,
                request.ownerUserId(),
                request.potentialOwnerUserIds(),
                request.administratorUserIds()
        ));
        return ResponseEntity.status(result.success() ? HttpStatus.CREATED : mapStatus(result)).body(result);
    }

    @GetMapping("/{taskId}")
    public UserTaskView getTask(@PathVariable("taskId") UUID taskId) {
        return userTaskService.getTask(taskId);
    }

    @PostMapping("/{taskId}/assign")
    public ResponseEntity<UserTaskOperationResult> assignTask(
            @PathVariable("taskId") UUID taskId,
            @Valid @RequestBody TaskActionRequest request,
            HttpServletRequest httpRequest
    ) {
        return respond(userTaskService.assignTask(taskId, map(request, httpRequest)));
    }

    @PostMapping("/{taskId}/refuse")
    public ResponseEntity<UserTaskOperationResult> refuseTask(
            @PathVariable("taskId") UUID taskId,
            @Valid @RequestBody TaskActionRequest request,
            HttpServletRequest httpRequest
    ) {
        return respond(userTaskService.refuseTask(taskId, map(request, httpRequest)));
    }

    @PostMapping("/{taskId}/start")
    public ResponseEntity<UserTaskOperationResult> startTask(
            @PathVariable("taskId") UUID taskId,
            @Valid @RequestBody TaskActionRequest request,
            HttpServletRequest httpRequest
    ) {
        return respond(userTaskService.startTask(taskId, map(request, httpRequest)));
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<UserTaskOperationResult> completeTask(
            @PathVariable("taskId") UUID taskId,
            @Valid @RequestBody CompleteTaskRequest request,
            HttpServletRequest httpRequest
    ) {
        String actorId = actorContextResolver.resolveActorId(httpRequest, request.actorId());
        return respond(userTaskService.completeTask(taskId, new CompleteUserTaskCommand(actorId, request.reason(), request.completionOutcome())));
    }

    private TaskActionCommand map(TaskActionRequest request, HttpServletRequest httpRequest) {
        String actorId = actorContextResolver.resolveActorId(httpRequest, request.actorId());
        return new TaskActionCommand(actorId, request.ownerUserId(), request.reason());
    }

    private ResponseEntity<UserTaskOperationResult> respond(UserTaskOperationResult result) {
        return ResponseEntity.status(result.success() ? HttpStatus.OK : mapStatus(result)).body(result);
    }

    private HttpStatus mapStatus(UserTaskOperationResult result) {
        if (result.rejectionCode() == null) {
            return HttpStatus.BAD_REQUEST;
        }
        return switch (result.rejectionCode()) {
            case CORRELATION_NOT_FOUND, TASK_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case UNAUTHORIZED -> HttpStatus.FORBIDDEN;
            case CONCURRENCY_CONFLICT, INVALID_STATE, DUPLICATE_TASK_REFERENCE -> HttpStatus.CONFLICT;
            case DUPLICATE_CORRELATION -> HttpStatus.CONFLICT;
            case VALIDATION_FAILED -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }
}

