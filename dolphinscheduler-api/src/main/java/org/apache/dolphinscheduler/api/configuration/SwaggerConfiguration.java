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

import org.apache.dolphinscheduler.dao.entity.DsVersion;
import org.apache.dolphinscheduler.dao.repository.DsVersionDao;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Swagger configuration, only enabled when the configuration item api.swagger.enable is true.
 * The swagger ui is under <a href="http://${host}:${port}/dolphinscheduler/swagger-ui.html">http://${host}:${port}/dolphinscheduler/swagger-ui.html</a>
 */
@Configuration
@ConditionalOnWebApplication
@PropertySource("classpath:swagger.properties")
public class SwaggerConfiguration implements WebMvcConfigurer {

    @Autowired
    private DsVersionDao dsVersionDao;

    private volatile String dsVersion;

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Apache DolphinScheduler Api Docs")
                .description("Apache DolphinScheduler Api Docs")
                .version(getDsVersion());
        return new OpenAPI().info(info);
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

    private String getDsVersion() {
        if (dsVersion != null) {
            return dsVersion;
        }
        dsVersion = dsVersionDao.selectVersion().map(DsVersion::getVersion).orElse("unknown");
        return dsVersion;
    }
}
