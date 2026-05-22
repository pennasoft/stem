package belfius.gejb.stem.core.task.eventing;

import belfius.gejb.stem.common.task.event.TaskLifecycleEvent;
import belfius.gejb.stem.core.task.persistence.TaskOutboxStatus;
import belfius.gejb.stem.core.task.persistence.UserTaskOutboxEventEntity;
import belfius.gejb.stem.core.task.persistence.UserTaskOutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class TaskOutboxProcessor {

    private final UserTaskOutboxEventRepository outboxRepository;
    private final TaskLifecycleEventPublisher publisher;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public TaskOutboxProcessor(
            UserTaskOutboxEventRepository outboxRepository,
            TaskLifecycleEventPublisher publisher,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.outboxRepository = outboxRepository;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public int publishPendingEvents(int batchSize, Duration retryDelay) {
        List<UserTaskOutboxEventEntity> readyEvents = outboxRepository.findReadyForPublication(Instant.now(clock)).stream()
                .limit(batchSize)
                .toList();
        int publishedCount = 0;
        for (UserTaskOutboxEventEntity outboxEvent : readyEvents) {
            try {
                TaskLifecycleEvent event = objectMapper.readValue(outboxEvent.getPayloadJson(), TaskLifecycleEvent.class);
                publisher.publish(event);
                outboxEvent.setStatus(TaskOutboxStatus.PUBLISHED);
                outboxEvent.setPublishedAt(Instant.now(clock));
                outboxEvent.setLastError(null);
                publishedCount++;
            } catch (Exception ex) {
                outboxEvent.setStatus(TaskOutboxStatus.FAILED);
                outboxEvent.setRetryCount(outboxEvent.getRetryCount() + 1);
                outboxEvent.setAvailableAt(Instant.now(clock).plus(retryDelay));
                outboxEvent.setLastError(sanitizeMessage(ex));
            }
        }
        return publishedCount;
    }

    private String sanitizeMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return message.length() > 300 ? message.substring(0, 300) : message;
    }
}


