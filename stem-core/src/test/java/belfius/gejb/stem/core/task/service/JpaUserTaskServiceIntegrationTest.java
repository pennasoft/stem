package belfius.gejb.stem.core.task.service;

import belfius.gejb.stem.common.task.lifecycle.UserTaskState;
import belfius.gejb.stem.common.task.model.CompleteUserTaskCommand;
import belfius.gejb.stem.common.task.model.CreateCorrelationCommand;
import belfius.gejb.stem.common.task.model.CreateUserTaskCommand;
import belfius.gejb.stem.common.task.model.TaskActionCommand;
import belfius.gejb.stem.common.task.model.TaskRejectionCode;
import belfius.gejb.stem.core.task.auth.DefaultTaskAuthorizationPolicy;
import belfius.gejb.stem.core.task.auth.TaskAuthorizationPolicy;
import belfius.gejb.stem.core.task.domain.DefaultTaskLifecyclePolicy;
import belfius.gejb.stem.core.task.domain.TaskLifecyclePolicy;
import belfius.gejb.stem.core.task.eventing.TaskLifecycleEventFactory;
import belfius.gejb.stem.core.task.persistence.CorrelationRepository;
import belfius.gejb.stem.core.task.persistence.TaskEnginePersistenceMarker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.time.Clock;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.jdbc.time_zone=UTC"
})
@Import(JpaUserTaskServiceIntegrationTest.CoreBeansConfiguration.class)
class JpaUserTaskServiceIntegrationTest {

    @Autowired
    private CorrelationService correlationService;

    @Autowired
    private UserTaskService userTaskService;

    @Autowired
    private belfius.gejb.stem.core.task.persistence.UserTaskLifecycleHistoryRepository historyRepository;

    @Autowired
    private belfius.gejb.stem.core.task.persistence.UserTaskOutboxEventRepository outboxRepository;

    @Test
    void createsAndAdvancesTaskLifecycleWhileRecordingHistoryAndOutbox() {
        var correlation = correlationService.registerCorrelation(new CreateCorrelationCommand("CORR-1", "Client", "CASE", "creator"));
        var created = userTaskService.createTask(new CreateUserTaskCommand(
                correlation.id(),
                "TASK-1",
                "Review documents",
                "Check the submitted files",
                "creator",
                "owner-1",
                List.of("owner-1"),
                List.of("admin-1")
        ));

        assertThat(created.success()).isTrue();
        assertThat(created.state()).isEqualTo(UserTaskState.CREATED);

        var assigned = userTaskService.assignTask(created.taskId(), new TaskActionCommand("owner-1", "owner-1", "claim"));
        var started = userTaskService.startTask(created.taskId(), new TaskActionCommand("owner-1", null, null));
        var completed = userTaskService.completeTask(created.taskId(), new CompleteUserTaskCommand("owner-1", null, "DONE"));

        assertThat(assigned.success()).isTrue();
        assertThat(started.success()).isTrue();
        assertThat(completed.success()).isTrue();
        assertThat(completed.state()).isEqualTo(UserTaskState.COMPLETED);
        assertThat(historyRepository.findAll()).hasSize(4);
        assertThat(outboxRepository.findAll()).hasSize(4);
        assertThat(userTaskService.getTask(created.taskId()).completionOutcome()).isEqualTo("DONE");
    }

    @Test
    void rejectsUnauthorizedTransitionWithoutChangingState() {
        var correlation = correlationService.registerCorrelation(new CreateCorrelationCommand("CORR-2", "Client", "CASE", "creator"));
        var created = userTaskService.createTask(new CreateUserTaskCommand(
                correlation.id(),
                "TASK-2",
                "Review identity",
                null,
                "creator",
                "owner-2",
                List.of("owner-2"),
                List.of("admin-2")
        ));
        userTaskService.assignTask(created.taskId(), new TaskActionCommand("owner-2", "owner-2", null));

        var rejected = userTaskService.startTask(created.taskId(), new TaskActionCommand("intruder", null, null));

        assertThat(rejected.success()).isFalse();
        assertThat(rejected.rejectionCode()).isEqualTo(TaskRejectionCode.UNAUTHORIZED);
        assertThat(userTaskService.getTask(created.taskId()).state()).isEqualTo(UserTaskState.ASSIGNED);
    }

    @Test
    void rejectsTaskCreationWhenCorrelationDoesNotExist() {
        var rejected = userTaskService.createTask(new CreateUserTaskCommand(
                java.util.UUID.randomUUID(),
                "TASK-404",
                "Impossible task",
                null,
                "creator",
                null,
                List.of(),
                List.of()
        ));

        assertThat(rejected.success()).isFalse();
        assertThat(rejected.rejectionCode()).isEqualTo(TaskRejectionCode.CORRELATION_NOT_FOUND);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = TaskEnginePersistenceMarker.class)
    @EnableJpaRepositories(basePackageClasses = CorrelationRepository.class)
    static class TestApplication {
    }

    @TestConfiguration
    static class CoreBeansConfiguration {

        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }

        @Bean
        TaskAuthorizationPolicy taskAuthorizationPolicy() {
            return new DefaultTaskAuthorizationPolicy();
        }

        @Bean
        TaskLifecyclePolicy taskLifecyclePolicy() {
            return new DefaultTaskLifecyclePolicy();
        }

        @Bean
        TaskLifecycleEventFactory taskLifecycleEventFactory() {
            return new TaskLifecycleEventFactory();
        }

        @Bean
        CorrelationService correlationService(
                belfius.gejb.stem.core.task.persistence.CorrelationRepository correlationRepository,
                Clock clock
        ) {
            return new JpaCorrelationService(correlationRepository, clock);
        }

        @Bean
        UserTaskService userTaskService(
                belfius.gejb.stem.core.task.persistence.CorrelationRepository correlationRepository,
                belfius.gejb.stem.core.task.persistence.UserTaskRepository userTaskRepository,
                belfius.gejb.stem.core.task.persistence.UserTaskLifecycleHistoryRepository historyRepository,
                belfius.gejb.stem.core.task.persistence.UserTaskOutboxEventRepository outboxRepository,
                TaskAuthorizationPolicy taskAuthorizationPolicy,
                TaskLifecyclePolicy taskLifecyclePolicy,
                TaskLifecycleEventFactory taskLifecycleEventFactory,
                ObjectMapper objectMapper,
                Clock clock
        ) {
            return new JpaUserTaskService(
                    correlationRepository,
                    userTaskRepository,
                    historyRepository,
                    outboxRepository,
                    taskAuthorizationPolicy,
                    taskLifecyclePolicy,
                    taskLifecycleEventFactory,
                    objectMapper,
                    clock
            );
        }
    }
}


