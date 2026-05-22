package belfius.gejb.stem.api.task.controller;

import belfius.gejb.stem.api.task.ActorContextResolver;
import belfius.gejb.stem.common.task.lifecycle.UserTaskState;
import belfius.gejb.stem.common.task.model.TaskRejectionCode;
import belfius.gejb.stem.common.task.model.UserTaskOperationResult;
import belfius.gejb.stem.common.task.model.UserTaskView;
import belfius.gejb.stem.core.task.service.CorrelationService;
import belfius.gejb.stem.core.task.service.UserTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.format.support.DefaultFormattingConversionService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserTaskControllerTest {

    @Mock
    private UserTaskService userTaskService;

    @Mock
    private CorrelationService correlationService;

    @Mock
    private ActorContextResolver actorContextResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new CorrelationController(correlationService),
                        new UserTaskController(userTaskService, actorContextResolver)
                )
                .setControllerAdvice(new TaskApiExceptionHandler())
                .setConversionService(new DefaultFormattingConversionService())
                .build();
    }

    @Test
    void createsTaskAndReturnsCreatedStatus() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        given(actorContextResolver.resolveActorId(any(), eq("owner-1"))).willReturn("owner-1");
        given(userTaskService.createTask(any())).willReturn(
                new UserTaskOperationResult(true, taskId, UserTaskState.CREATED, null, "created", correlationId, true)
        );

        mockMvc.perform(post("/api/stem/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"correlationId\": \"%s\",
                                  \"taskReference\": \"TASK-1\",
                                  \"title\": \"Review\",
                                  \"actorId\": \"owner-1\"
                                }
                                """.formatted(correlationId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.state").value("CREATED"));
    }

    @Test
    void mapsUnauthorizedLifecycleActionToForbidden() throws Exception {
        UUID taskId = UUID.randomUUID();
        given(actorContextResolver.resolveActorId(any(), eq("intruder"))).willReturn("intruder");
        given(userTaskService.startTask(eq(taskId), any())).willReturn(
                UserTaskOperationResult.failure(taskId, UUID.randomUUID(), UserTaskState.ASSIGNED, TaskRejectionCode.UNAUTHORIZED, "denied")
        );

        mockMvc.perform(post("/api/stem/tasks/{taskId}/start", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"actorId\": \"intruder\"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.rejectionCode").value("UNAUTHORIZED"));
    }

    @Test
    void getsTaskById() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        given(userTaskService.getTask(taskId)).willReturn(new UserTaskView(
                taskId,
                correlationId,
                "TASK-2",
                "Review",
                null,
                UserTaskState.CREATED,
                null,
                List.of(),
                Instant.parse("2026-04-17T00:00:00Z"),
                Instant.parse("2026-04-17T00:00:00Z"),
                null,
                null,
                null,
                0L
        ));

        mockMvc.perform(get("/api/stem/tasks/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskReference").value("TASK-2"))
                .andExpect(jsonPath("$.state").value("CREATED"));
    }
}


