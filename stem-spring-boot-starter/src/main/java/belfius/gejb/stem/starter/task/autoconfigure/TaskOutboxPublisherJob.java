package belfius.gejb.stem.starter.task.autoconfigure;

import belfius.gejb.stem.core.task.eventing.TaskOutboxProcessor;
import belfius.gejb.stem.starter.task.properties.TaskEngineProperties;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Scheduled;

public class TaskOutboxPublisherJob {

    private final TaskOutboxProcessor taskOutboxProcessor;
    private final TaskEngineProperties properties;
    private volatile boolean active = true;

    public TaskOutboxPublisherJob(TaskOutboxProcessor taskOutboxProcessor, TaskEngineProperties properties) {
        this.taskOutboxProcessor = taskOutboxProcessor;
        this.properties = properties;
    }

    @PreDestroy
    void stop() {
        this.active = false;
    }

    @Scheduled(fixedDelayString = "#{@taskEngineOutboxPollIntervalMillis}")
    public void publishPendingEvents() {
        if (!active) {
            return;
        }

        try {
            taskOutboxProcessor.publishPendingEvents(properties.getOutbox().getBatchSize(), properties.getOutbox().getRetryDelay());
        } catch (RuntimeException ex) {
            if (isContextShutdownFailure(ex)) {
                return;
            }
            throw ex;
        }
    }

    private boolean isContextShutdownFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof IllegalStateException illegalStateException
                    && illegalStateException.getMessage() != null
                    && illegalStateException.getMessage().contains("has been closed already")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

