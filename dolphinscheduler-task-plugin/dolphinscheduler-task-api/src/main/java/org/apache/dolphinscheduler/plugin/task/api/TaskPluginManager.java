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

package org.apache.dolphinscheduler.plugin.task.api;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPIFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskPluginManager {

    private static final Map<String, TaskChannel> taskChannelMap = new HashMap<>();

    private static final AtomicBoolean loadedFlag = new AtomicBoolean(false);

    static {
        loadTaskPlugin();
    }

    public static void loadTaskPlugin() {
        if (!loadedFlag.compareAndSet(false, true)) {
            log.warn("The task plugin has already been loaded");
            return;
        }
        PrioritySPIFactory<TaskChannelFactory> prioritySPIFactory = new PrioritySPIFactory<>(TaskChannelFactory.class);
        for (Map.Entry<String, TaskChannelFactory> entry : prioritySPIFactory.getSPIMap().entrySet()) {
            String factoryName = entry.getKey();
            TaskChannelFactory factory = entry.getValue();

            taskChannelMap.put(factoryName, factory.create());
            log.info("Success register task plugin: {}", factoryName);
        }

    }

    /**
     * Get the TaskChannel by type, if the TaskChannel is not found, will throw
     * @param type task type, cannot be null
     * @throws IllegalArgumentException if the TaskChannel is not found
     */
    public static TaskChannel getTaskChannel(String type) {
        checkNotNull(type, "type cannot be null");
        TaskChannel taskChannel = taskChannelMap.get(type);
        if (taskChannel == null) {
            throw new IllegalArgumentException("Cannot find TaskChannel for : " + type);
        }
        return taskChannel;
    }

    /**
     * Check if the task parameters is validated
     * @param taskType task type, cannot be null
     * @param taskParams task parameters
     * @return true if the task parameters is validated, otherwise false
     * @throws IllegalArgumentException if the TaskChannel is not found
     * @throws IllegalArgumentException if cannot deserialize the task parameters
     */
    public static boolean checkTaskParameters(String taskType, String taskParams) {
        AbstractParameters abstractParameters = parseTaskParameters(taskType, taskParams);
        return abstractParameters.checkParameters();
    }

    /**
     * Parse the task parameters
     * @param taskType task type, cannot be null
     * @param taskParams task parameters
     * @return AbstractParameters
     * @throws IllegalArgumentException if the TaskChannel is not found
     * @throws IllegalArgumentException if cannot deserialize the task parameters
     */
    public static AbstractParameters parseTaskParameters(String taskType, String taskParams) {
        checkNotNull(taskType, "taskType cannot be null");
        TaskChannel taskChannel = getTaskChannel(taskType);
        AbstractParameters abstractParameters = taskChannel.parseParameters(taskParams);
        if (abstractParameters == null) {
            throw new IllegalArgumentException("Cannot parse task parameters: " + taskParams + " for : " + taskType);
        }
        return abstractParameters;
    }

}
