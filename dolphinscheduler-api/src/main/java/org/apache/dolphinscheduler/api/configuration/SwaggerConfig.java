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

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * swager2 config class
 */
@Configuration
@EnableSwagger2
@EnableSwaggerBootstrapUI
@ConditionalOnWebApplication
@PropertySource("classpath:swagger.properties")
public class SwaggerConfig implements WebMvcConfigurer {

    @Bean
    public Docket createV1RestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("v1(current)")
            .apiInfo(apiV1Info())
            .select()
            .apis(RequestHandlerSelectors.basePackage("org.apache.dolphinscheduler.api.controller"))
            .paths(PathSelectors.any())
            .paths(PathSelectors.regex("^(?!/v2).*"))
            .build();
    }

    private ApiInfo apiV1Info() {
        return new ApiInfoBuilder()
            .title("Dolphin Scheduler Api Docs")
            .description("Dolphin Scheduler Api Docs")
            .version("V1")
            .build();
    }

    @Bean
    public Docket createV2RestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("v2")
            .apiInfo(apiV2Info())
            .select()
            .apis(RequestHandlerSelectors.basePackage("org.apache.dolphinscheduler.api.controller"))
            .paths(PathSelectors.ant("/v2/**"))
            .build();
    }

    private ApiInfo apiV2Info() {
        return new ApiInfoBuilder()
            .title("Dolphin Scheduler Api Docs")
            .description("Dolphin Scheduler Api Docs")
            .version("V2")
            .build();
    }
}
