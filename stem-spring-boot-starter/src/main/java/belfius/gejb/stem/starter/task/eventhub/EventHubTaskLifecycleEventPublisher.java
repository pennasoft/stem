package belfius.gejb.stem.starter.task.eventhub;

import belfius.gejb.stem.common.task.event.TaskLifecycleEvent;
import belfius.gejb.stem.core.task.eventing.TaskLifecycleEventPublishResult;
import belfius.gejb.stem.core.task.eventing.TaskLifecycleEventPublisher;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class EventHubTaskLifecycleEventPublisher implements TaskLifecycleEventPublisher, AutoCloseable {

    private final EventHubProducerClient producerClient;
    private final ObjectMapper objectMapper;

    public EventHubTaskLifecycleEventPublisher(EventHubProducerClient producerClient, ObjectMapper objectMapper) {
        this.producerClient = producerClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public TaskLifecycleEventPublishResult publish(TaskLifecycleEvent event) {
        try {
            byte[] payload = objectMapper.writeValueAsBytes(event);
            producerClient.send(List.of(new EventData(BinaryData.fromBytes(payload))));
            return new TaskLifecycleEventPublishResult(null, "azure-event-hubs");
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to publish lifecycle event to Azure Event Hubs", ex);
        }
    }

    @Override
    public void close() {
        producerClient.close();
    }
}


