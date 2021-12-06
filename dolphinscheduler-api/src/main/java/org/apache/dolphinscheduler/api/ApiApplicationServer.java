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

package org.apache.dolphinscheduler.api;

import static org.apache.dolphinscheduler.common.Constants.SPRING_DATASOURCE_DRIVER_CLASS_NAME;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@EnableAutoConfiguration
@ServletComponentScan
@ComponentScan(value = "org.apache.dolphinscheduler", excludeFilters = {
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
        "org.apache.dolphinscheduler.server.*",
        "org.apache.dolphinscheduler.alert.*"
    })
})
public class ApiApplicationServer extends SpringBootServletInitializer {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ApiApplicationServer.class).profiles("api").run(args);
    }

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @PostConstruct
    public void run() {
        PropertyUtils.setValue(SPRING_DATASOURCE_DRIVER_CLASS_NAME, driverClassName);
    }
}
