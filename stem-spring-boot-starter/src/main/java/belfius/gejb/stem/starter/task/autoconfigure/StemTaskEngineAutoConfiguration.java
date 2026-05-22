package belfius.gejb.stem.starter.task.autoconfigure;

import belfius.gejb.stem.core.task.auth.DefaultTaskAuthorizationPolicy;
import belfius.gejb.stem.core.task.auth.TaskAuthorizationPolicy;
import belfius.gejb.stem.core.task.domain.DefaultTaskLifecyclePolicy;
import belfius.gejb.stem.core.task.domain.TaskLifecyclePolicy;
import belfius.gejb.stem.core.task.eventing.TaskLifecycleEventFactory;
import belfius.gejb.stem.core.task.eventing.TaskLifecycleEventPublisher;
import belfius.gejb.stem.core.task.eventing.TaskOutboxProcessor;
import belfius.gejb.stem.core.task.persistence.CorrelationRepository;
import belfius.gejb.stem.core.task.persistence.TaskEnginePersistenceMarker;
import belfius.gejb.stem.core.task.persistence.UserTaskLifecycleHistoryRepository;
import belfius.gejb.stem.core.task.persistence.UserTaskOutboxEventRepository;
import belfius.gejb.stem.core.task.persistence.UserTaskRepository;
import belfius.gejb.stem.core.task.service.CorrelationService;
import belfius.gejb.stem.core.task.service.JpaCorrelationService;
import belfius.gejb.stem.core.task.service.JpaUserTaskService;
import belfius.gejb.stem.core.task.service.UserTaskService;
import belfius.gejb.stem.starter.task.properties.TaskEngineProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@AutoConfiguration
@ConditionalOnProperty(prefix = "stem.task-engine", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(TaskEngineProperties.class)
@EntityScan(basePackageClasses = TaskEnginePersistenceMarker.class)
@EnableJpaRepositories(basePackageClasses = CorrelationRepository.class)
@EnableScheduling
public class StemTaskEngineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock taskEngineClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper taskEngineObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskAuthorizationPolicy taskAuthorizationPolicy() {
        return new DefaultTaskAuthorizationPolicy();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskLifecyclePolicy taskLifecyclePolicy() {
        return new DefaultTaskLifecyclePolicy();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskLifecycleEventFactory taskLifecycleEventFactory() {
        return new TaskLifecycleEventFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public CorrelationService correlationService(CorrelationRepository correlationRepository, Clock taskEngineClock) {
        return new JpaCorrelationService(correlationRepository, taskEngineClock);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserTaskService userTaskService(
            CorrelationRepository correlationRepository,
            UserTaskRepository userTaskRepository,
            UserTaskLifecycleHistoryRepository historyRepository,
            UserTaskOutboxEventRepository outboxRepository,
            TaskAuthorizationPolicy taskAuthorizationPolicy,
            TaskLifecyclePolicy taskLifecyclePolicy,
            TaskLifecycleEventFactory taskLifecycleEventFactory,
            ObjectMapper taskEngineObjectMapper,
            Clock taskEngineClock
    ) {
        return new JpaUserTaskService(
                correlationRepository,
                userTaskRepository,
                historyRepository,
                outboxRepository,
                taskAuthorizationPolicy,
                taskLifecyclePolicy,
                taskLifecycleEventFactory,
                taskEngineObjectMapper,
                taskEngineClock
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskOutboxProcessor taskOutboxProcessor(
            UserTaskOutboxEventRepository outboxRepository,
            TaskLifecycleEventPublisher taskLifecycleEventPublisher,
            ObjectMapper taskEngineObjectMapper,
            Clock taskEngineClock
    ) {
        return new TaskOutboxProcessor(outboxRepository, taskLifecycleEventPublisher, taskEngineObjectMapper, taskEngineClock);
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskOutboxPublisherJob taskOutboxPublisherJob(TaskOutboxProcessor taskOutboxProcessor, TaskEngineProperties taskEngineProperties) {
        return new TaskOutboxPublisherJob(taskOutboxProcessor, taskEngineProperties);
    }

    @Bean("taskEngineOutboxPollIntervalMillis")
    public Long taskEngineOutboxPollIntervalMillis(TaskEngineProperties taskEngineProperties) {
        return taskEngineProperties.getOutbox().getPollInterval().toMillis();
    }
}



