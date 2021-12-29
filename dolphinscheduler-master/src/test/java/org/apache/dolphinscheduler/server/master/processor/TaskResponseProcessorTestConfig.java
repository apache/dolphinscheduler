package org.apache.dolphinscheduler.server.master.processor;

import org.apache.dolphinscheduler.server.utils.DataQualityResultOperator;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * dependency config
 */
@Configuration
public class TaskResponseProcessorTestConfig {

    @Bean
    public DataQualityResultOperator dataQualityResultOperator() {
        return Mockito.mock(DataQualityResultOperator.class);
    }
}