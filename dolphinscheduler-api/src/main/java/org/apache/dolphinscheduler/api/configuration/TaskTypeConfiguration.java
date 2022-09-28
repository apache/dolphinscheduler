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

import org.apache.dolphinscheduler.api.dto.FavTaskDto;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.config.YamlPropertySourceFactory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@EnableConfigurationProperties
@PropertySource(value = {"classpath:task-type-config.yaml"}, factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "task")
@Getter
@Setter
public class TaskTypeConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TaskTypeConfiguration.class);

    private List<String> universal;
    private List<String> cloud;
    private List<String> logic;
    private List<String> dataIntegration;
    private List<String> dataQuality;
    private List<String> other;
    private List<String> machineLearning;

    private static final List<FavTaskDto> defaultTaskTypes = new ArrayList<>();

    public List<FavTaskDto> getDefaultTaskTypes() {
        if (!defaultTaskTypes.isEmpty()) {
            return defaultTaskTypes;
        }
        printDefaultTypes();
        universal.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_UNIVERSAL)));
        cloud.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_CLOUD)));
        logic.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_LOGIC)));
        dataIntegration.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_DATA_INTEGRATION)));
        dataQuality.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_DATA_QUALITY)));
        machineLearning.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_MACHINE_LEARNING)));
        other.forEach(task -> defaultTaskTypes.add(new FavTaskDto(task, false, Constants.TYPE_OTHER)));
        return defaultTaskTypes;
    }

    public void printDefaultTypes() {
        logger.info("support default universal task types: {}", universal);
        logger.info("support default cloud task types: {}", cloud);
        logger.info("support default logic task types: {}", logic);
        logger.info("support default dataIntegration task types: {}", dataIntegration);
        logger.info("support default dataQuality task types: {}", dataQuality);
        logger.info("support default machineLearning task types: {}", machineLearning);
        logger.info("support default other task types: {}", other);
    }
}
