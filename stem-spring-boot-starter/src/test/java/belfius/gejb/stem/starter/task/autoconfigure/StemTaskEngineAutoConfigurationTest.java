package belfius.gejb.stem.starter.task.autoconfigure;

import belfius.gejb.stem.core.task.eventing.TaskLifecycleEventPublishResult;
import belfius.gejb.stem.core.task.eventing.TaskLifecycleEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StemTaskEngineAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    JdbcTemplateAutoConfiguration.class,
                    TransactionAutoConfiguration.class,
                    HibernateJpaAutoConfiguration.class,
                    JacksonAutoConfiguration.class,
                    StemTaskEngineAutoConfiguration.class,
                    StemTaskEngineApiAutoConfiguration.class
            ));

    @Test
    void taskEngineIsDisabledByDefault() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean("correlationService");
            assertThat(context).doesNotHaveBean("userTaskService");
        });
    }

    @Test
    void taskEngineStartsWhenEnabledWithCustomPublisher() {
        contextRunner
                .withBean(TaskLifecycleEventPublisher.class, () -> event -> new TaskLifecycleEventPublishResult(null, "test"))
                .withPropertyValues(
                        "stem.task-engine.enabled=true",
                        "spring.datasource.url=jdbc:h2:mem:stem;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                        "spring.datasource.driver-class-name=org.h2.Driver",
                        "spring.jpa.hibernate.ddl-auto=create-drop"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(belfius.gejb.stem.core.task.service.CorrelationService.class);
                    assertThat(context).hasSingleBean(belfius.gejb.stem.core.task.service.UserTaskService.class);
                    assertThat(context).hasSingleBean(belfius.gejb.stem.core.task.eventing.TaskOutboxProcessor.class);
                });
    }

    @Test
    void apiControllersAreOnlyRegisteredWhenApiPropertyIsEnabled() {
        contextRunner
                .withBean(TaskLifecycleEventPublisher.class, () -> event -> new TaskLifecycleEventPublishResult(null, "test"))
                .withPropertyValues(
                        "stem.task-engine.enabled=true",
                        "stem.task-engine.api.enabled=true",
                        "spring.datasource.url=jdbc:h2:mem:stem-api;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                        "spring.datasource.driver-class-name=org.h2.Driver",
                        "spring.jpa.hibernate.ddl-auto=create-drop"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(belfius.gejb.stem.api.task.controller.UserTaskController.class);
                    assertThat(context).hasSingleBean(belfius.gejb.stem.api.task.controller.CorrelationController.class);
                });
    }
}

