package belfius.gejb.stem.starter.task.autoconfigure;

import belfius.gejb.stem.api.task.ActorContextResolver;
import belfius.gejb.stem.api.task.DefaultActorContextResolver;
import belfius.gejb.stem.api.task.controller.CorrelationController;
import belfius.gejb.stem.api.task.controller.TaskApiExceptionHandler;
import belfius.gejb.stem.api.task.controller.UserTaskController;
import belfius.gejb.stem.core.task.service.CorrelationService;
import belfius.gejb.stem.core.task.service.UserTaskService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration(after = StemTaskEngineAutoConfiguration.class)
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnProperty(prefix = "stem.task-engine.api", name = "enabled", havingValue = "true")
public class StemTaskEngineApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ActorContextResolver actorContextResolver() {
        return new DefaultActorContextResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public CorrelationController correlationController(CorrelationService correlationService) {
        return new CorrelationController(correlationService);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserTaskController userTaskController(UserTaskService userTaskService, ActorContextResolver actorContextResolver) {
        return new UserTaskController(userTaskService, actorContextResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskApiExceptionHandler taskApiExceptionHandler() {
        return new TaskApiExceptionHandler();
    }
}


