package org.apache.dolphinscheduler.extract.base.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rpc.ssl")
@Data
public class NettySslConfig {

    public boolean enabled;

    public String certFilePath;

    public String keyFilePath;

}
