package org.apache.dolphinscheduler.server.worker.config;

import org.apache.dolphinscheduler.service.alert.AlertClientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Autowired
    private WorkerConfig workerConfig;

    @Bean
    public AlertClientService alertClientService() {
        // alert-server client registry
        return new AlertClientService(
                workerConfig.getAlertListenHost(),
                workerConfig.getAlertListenPort());
    }
}
