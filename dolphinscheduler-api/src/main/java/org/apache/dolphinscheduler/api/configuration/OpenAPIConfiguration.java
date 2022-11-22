/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.api.configuration;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * swager2 config class
 */
@Configuration
@ConditionalOnWebApplication
@PropertySource("classpath:swagger.properties")
public class OpenAPIConfiguration implements WebMvcConfigurer {

    @Bean
    public OpenAPI apiV1Info1() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dolphin Scheduler Api Docs")
                        .description("Dolphin Scheduler Api Docs")
                        .version("V1"));
    }

    @Bean
    public GroupedOpenApi publicApi1() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToExclude("/v2/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicApi2() {
        return GroupedOpenApi.builder()
                .group("v2")
                .pathsToMatch("/v2/**")
                .build();
    }
}
