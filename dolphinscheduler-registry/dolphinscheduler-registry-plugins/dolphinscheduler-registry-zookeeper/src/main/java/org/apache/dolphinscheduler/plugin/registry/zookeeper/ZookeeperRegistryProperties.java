package org.apache.dolphinscheduler.plugin.registry.zookeeper;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "zookeeper")
@ConfigurationProperties(prefix = "registry")
public class ZookeeperRegistryProperties {
    private Type type;
    private ZookeeperProperties zookeeper = new ZookeeperProperties();

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ZookeeperProperties getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(ZookeeperProperties zookeeper) {
        this.zookeeper = zookeeper;
    }

    public enum Type {
        ZOOKEEPER
    }

    public static final class ZookeeperProperties {
        private String namespace;
        private String connectString;
        private RetryPolicy retryPolicy = new RetryPolicy();
        private String digest;
        private Duration sessionTimeout = Duration.ofSeconds(30);
        private Duration connectionTimeout = Duration.ofSeconds(9);
        private Duration blockUntilConnected = Duration.ofMillis(600);

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getConnectString() {
            return connectString;
        }

        public void setConnectString(String connectString) {
            this.connectString = connectString;
        }

        public RetryPolicy getRetryPolicy() {
            return retryPolicy;
        }

        public void setRetryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
        }

        public String getDigest() {
            return digest;
        }

        public void setDigest(String digest) {
            this.digest = digest;
        }

        public Duration getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(Duration sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        public Duration getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(Duration connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public Duration getBlockUntilConnected() {
            return blockUntilConnected;
        }

        public void setBlockUntilConnected(Duration blockUntilConnected) {
            this.blockUntilConnected = blockUntilConnected;
        }

        public static final class RetryPolicy {
            private Duration baseSleepTime = Duration.ofMillis(60);
            private int maxRetries;
            private Duration maxSleep = Duration.ofMillis(300);

            public Duration getBaseSleepTime() {
                return baseSleepTime;
            }

            public void setBaseSleepTime(Duration baseSleepTime) {
                this.baseSleepTime = baseSleepTime;
            }

            public int getMaxRetries() {
                return maxRetries;
            }

            public void setMaxRetries(int maxRetries) {
                this.maxRetries = maxRetries;
            }

            public Duration getMaxSleep() {
                return maxSleep;
            }

            public void setMaxSleep(Duration maxSleep) {
                this.maxSleep = maxSleep;
            }
        }
    }

}
