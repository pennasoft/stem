package belfius.gejb.stem.starter.task.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "stem.task-engine")
public class TaskEngineProperties {

    private boolean enabled;
    private final Api api = new Api();
    private final EventHub eventhub = new EventHub();
    private final Lock lock = new Lock();
    private final Outbox outbox = new Outbox();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Api getApi() {
        return api;
    }

    public boolean isApiEnabled() {
        return api.isEnabled();
    }

    public static class Api {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public EventHub getEventhub() {
        return eventhub;
    }

    public Lock getLock() {
        return lock;
    }

    public Outbox getOutbox() {
        return outbox;
    }

    public static class EventHub {
        private boolean enabled;
        private String namespace;
        private String hubName;
        private String connectionString;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getHubName() {
            return hubName;
        }

        public void setHubName(String hubName) {
            this.hubName = hubName;
        }

        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }
    }

    public static class Lock {
        private Duration timeout = Duration.ofSeconds(5);

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
    }

    public static class Outbox {
        private Duration pollInterval = Duration.ofSeconds(2);
        private int batchSize = 100;
        private Duration retryDelay = Duration.ofSeconds(10);

        public Duration getPollInterval() {
            return pollInterval;
        }

        public void setPollInterval(Duration pollInterval) {
            this.pollInterval = pollInterval;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public Duration getRetryDelay() {
            return retryDelay;
        }

        public void setRetryDelay(Duration retryDelay) {
            this.retryDelay = retryDelay;
        }
    }
}


