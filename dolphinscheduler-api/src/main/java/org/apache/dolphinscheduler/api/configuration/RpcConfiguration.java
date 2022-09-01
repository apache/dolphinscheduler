package org.apache.dolphinscheduler.api.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(value = "rpc", ignoreUnknownFields = false)
public class RpcConfiguration {

    private String rpcPrefix = "http://";
    private String masterUrlPrefix = "/dolphinscheduler/master";
    private String workerUrlPrefix = "/dolphinscheduler/worker";
}
