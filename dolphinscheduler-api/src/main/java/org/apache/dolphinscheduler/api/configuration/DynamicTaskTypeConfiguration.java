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

import org.apache.dolphinscheduler.api.dto.taskType.DynamicTaskInfo;
import org.apache.dolphinscheduler.common.config.YamlPropertySourceFactory;
import org.apache.dolphinscheduler.common.constants.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@PropertySource(value = {"classpath:dynamic-task-type-config.yaml"}, factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "dynamic-task")
@Getter
@Setter
@Slf4j
public class DynamicTaskTypeConfiguration {

    private static final List<String> defaultTaskCategories =
            Arrays.asList(Constants.TYPE_UNIVERSAL, Constants.TYPE_DATA_INTEGRATION, Constants.TYPE_CLOUD,
                    Constants.TYPE_LOGIC, Constants.TYPE_DATA_QUALITY, Constants.TYPE_OTHER,
                    Constants.TYPE_MACHINE_LEARNING);
    private List<DynamicTaskInfo> universal;
    private List<DynamicTaskInfo> cloud;
    private List<DynamicTaskInfo> logic;
    private List<DynamicTaskInfo> dataIntegration;
    private List<DynamicTaskInfo> dataQuality;
    private List<DynamicTaskInfo> other;
    private List<DynamicTaskInfo> machineLearning;

    public List<String> getTaskCategories() {
        return defaultTaskCategories;
    }

    public List<DynamicTaskInfo> getTaskTypesByCategory(String category) {
        switch (category) {
            case Constants.TYPE_UNIVERSAL:
                return universal;
            case Constants.TYPE_DATA_INTEGRATION:
                return cloud;
            case Constants.TYPE_CLOUD:
                return logic;
            case Constants.TYPE_LOGIC:
                return dataIntegration;
            case Constants.TYPE_DATA_QUALITY:
                return dataQuality;
            case Constants.TYPE_OTHER:
                return other;
            case Constants.TYPE_MACHINE_LEARNING:
                return machineLearning;
            default:
                return new ArrayList<>();
        }

    }

    public void printDefaultTypes() {
        log.info("support default universal dynamic task types: {}", universal);
        log.info("support default cloud dynamic task types: {}", cloud);
        log.info("support default logic dynamic task types: {}", logic);
        log.info("support default dataIntegration dynamic task types: {}", dataIntegration);
        log.info("support default dataQuality dynamic task types: {}", dataQuality);
        log.info("support default machineLearning dynamic task types: {}", machineLearning);
        log.info("support default other dynamic task types: {}", other);
    }
}
