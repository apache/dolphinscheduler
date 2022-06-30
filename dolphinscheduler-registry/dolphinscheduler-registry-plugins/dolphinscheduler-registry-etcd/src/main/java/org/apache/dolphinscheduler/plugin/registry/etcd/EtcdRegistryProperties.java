package org.apache.dolphinscheduler.plugin.registry.etcd;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "etcd")
@ConfigurationProperties(prefix = "registry")
public class EtcdRegistryProperties {
    private String endpoints;
    private String namespace="dolphinscheduler";
    private Duration connectionTimeout = Duration.ofSeconds(9);

    // auth
    private String User;
    private String password;
    private String authority;

    // RetryPolicy
    private Long retryDelay=60L;
    private Long retryMaxDelay=300L;
    private Duration retryMaxDuration=Duration.ofMillis(1500);;

    // loadBalancerPolicy
    private String loadBalancerPolicy;
}
