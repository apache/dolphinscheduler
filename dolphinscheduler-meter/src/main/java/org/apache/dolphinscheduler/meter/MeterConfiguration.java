/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.dolphinscheduler.meter;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * This configuration class is used to config the metrics. We use <a href="https://micrometer.io/docs/concepts">micrometer</a> as the metrics fade.
 *
 * <p>To open the metrics, you need to set the property "metrics.enabled" to true. Right now, we only support expose the metrics to Prometheus,
 * after you open metrics expose, you can get the metrics data at: http://host:port/actuator/prometheus.
 * <p>You can use the below method to get a meter:
 * <pre>
 *     {@code
 *      Counter counter = Metrics.counter("name", "tag1", "tag2");
 *     }
 * </pre>
 */
@Configuration
@EnableAspectJAutoProxy
@EnableAutoConfiguration
@ConditionalOnProperty(prefix = "metrics", name = "enabled", havingValue = "true")
public class MeterConfiguration {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }
}
