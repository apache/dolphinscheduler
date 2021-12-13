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

package org.apache.dolphinscheduler.server.master.runner.task;

import static org.apache.dolphinscheduler.common.Constants.COMMON_TASK_TYPE;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * the factory to create task processor
 */
@Service
public class TaskProcessorFactory {

    private static final String DEFAULT_PROCESSOR = COMMON_TASK_TYPE;

    private Map<String, ITaskProcessor> taskProcessorMap;

    @Autowired
    public TaskProcessorFactory(List<ITaskProcessor> taskProcessors) {
        taskProcessorMap = taskProcessors.stream().collect(Collectors.toMap(ITaskProcessor::getType, Function.identity(), (v1, v2) -> v2));
    }

    public ITaskProcessor getTaskProcessor(String key) {
        if (StringUtils.isEmpty(key)) {
            key = DEFAULT_PROCESSOR;
        }
        ITaskProcessor taskProcessor = taskProcessorMap.get(key);
        if (Objects.isNull(taskProcessor)) {
            taskProcessor = taskProcessorMap.get(DEFAULT_PROCESSOR);
        }

        return taskProcessor;
    }
}
