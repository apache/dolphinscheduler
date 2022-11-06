package org.apache.dolphinscheduler.server.worker.config;

import java.time.Duration;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ssh.session")
public class SSHSessionConfig {

    private PoolProperties pool = new PoolProperties();

    private SftpProperties sftp = new SftpProperties();

    @Data
    public static final class PoolProperties {

        private int maxIdlePerKey;

        private int maxTotalPerKey;

        private int maxTotal;

        private boolean blockWhenExhausted;

        private Duration maxWaitDuration;

        private Duration minEvictableIdleDuration;

        private Duration durationBetweenEvictionRuns;

        private boolean removeAbandonedOnBorrow;

        private Duration removeAbandonedTimeoutDuration;

    }

    @Data
    public static final class SftpProperties {

        private boolean enableUploadMonitor;

        private int maxUploadRate;

        private int maxFileSize;

    }

}
