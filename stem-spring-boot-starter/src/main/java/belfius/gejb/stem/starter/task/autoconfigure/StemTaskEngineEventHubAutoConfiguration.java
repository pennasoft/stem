package belfius.gejb.stem.starter.task.autoconfigure;

import belfius.gejb.stem.core.task.eventing.TaskLifecycleEventPublisher;
import belfius.gejb.stem.starter.task.eventhub.EventHubTaskLifecycleEventPublisher;
import belfius.gejb.stem.starter.task.properties.TaskEngineProperties;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = StemTaskEngineAutoConfiguration.class)
@ConditionalOnClass(EventHubProducerClient.class)
@ConditionalOnProperty(prefix = "stem.task-engine.eventhub", name = "enabled", havingValue = "true")
public class StemTaskEngineEventHubAutoConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public EventHubProducerClient taskEngineEventHubProducerClient(TaskEngineProperties taskEngineProperties) {
        String connectionString = requireText(taskEngineProperties.getEventhub().getConnectionString(), "stem.task-engine.eventhub.connection-string");
        String hubName = requireText(taskEngineProperties.getEventhub().getHubName(), "stem.task-engine.eventhub.hub-name");
        return new EventHubClientBuilder()
                .connectionString(connectionString, hubName)
                .buildProducerClient();
    }

    @Bean
    @ConditionalOnMissingBean(TaskLifecycleEventPublisher.class)
    public TaskLifecycleEventPublisher taskLifecycleEventPublisher(
            EventHubProducerClient taskEngineEventHubProducerClient,
            ObjectMapper taskEngineObjectMapper
    ) {
        return new EventHubTaskLifecycleEventPublisher(taskEngineEventHubProducerClient, taskEngineObjectMapper);
    }

    private String requireText(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " must be configured when Event Hubs publishing is enabled");
        }
        return value;
    }
}

