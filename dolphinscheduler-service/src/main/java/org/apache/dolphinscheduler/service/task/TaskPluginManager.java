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

package org.apache.dolphinscheduler.service.task;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannelFactory;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.BlockingParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ConditionsParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SubProcessParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPIFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaskPluginManager {

    private static final Logger logger = LoggerFactory.getLogger(TaskPluginManager.class);

    private final Map<String, TaskChannelFactory> taskChannelFactoryMap = new HashMap<>();
    private final Map<String, TaskChannel> taskChannelMap = new HashMap<>();

    private final AtomicBoolean loadedFlag = new AtomicBoolean(false);

    /**
     * Load task plugins from classpath.
     */
    public void loadPlugin() {
        if (!loadedFlag.compareAndSet(false, true)) {
            logger.warn("The task plugin has already been loaded");
            return;
        }
        PrioritySPIFactory<TaskChannelFactory> prioritySPIFactory = new PrioritySPIFactory<>(TaskChannelFactory.class);
        for (Map.Entry<String, TaskChannelFactory> entry : prioritySPIFactory.getSPIMap().entrySet()) {
            String factoryName = entry.getKey();
            TaskChannelFactory factory = entry.getValue();

            logger.info("Registering task plugin: {} - {}", factoryName, factory.getClass());

            taskChannelFactoryMap.put(factoryName, factory);
            taskChannelMap.put(factoryName, factory.create());

            logger.info("Registered task plugin: {} - {}", factoryName, factory.getClass());
        }

    }

    public Map<String, TaskChannel> getTaskChannelMap() {
        return Collections.unmodifiableMap(taskChannelMap);
    }

    public Map<String, TaskChannelFactory> getTaskChannelFactoryMap() {
        return Collections.unmodifiableMap(taskChannelFactoryMap);
    }

    public TaskChannel getTaskChannel(String type) {
        return this.getTaskChannelMap().get(type);
    }

    public boolean checkTaskParameters(ParametersNode parametersNode) {
        AbstractParameters abstractParameters = this.getParameters(parametersNode);
        return abstractParameters != null && abstractParameters.checkParameters();
    }

    public AbstractParameters getParameters(ParametersNode parametersNode) {
        String taskType = parametersNode.getTaskType();
        if (Objects.isNull(taskType)) {
            return null;
        }
        switch (taskType) {
            case TaskConstants.TASK_TYPE_CONDITIONS:
                return JSONUtils.parseObject(parametersNode.getTaskParams(), ConditionsParameters.class);
            case TaskConstants.TASK_TYPE_SWITCH:
                return JSONUtils.parseObject(parametersNode.getTaskParams(), SwitchParameters.class);
            case TaskConstants.TASK_TYPE_SUB_PROCESS:
                return JSONUtils.parseObject(parametersNode.getTaskParams(), SubProcessParameters.class);
            case TaskConstants.TASK_TYPE_DEPENDENT:
                return JSONUtils.parseObject(parametersNode.getTaskParams(), DependentParameters.class);
            case TaskConstants.TASK_TYPE_BLOCKING:
                return JSONUtils.parseObject(parametersNode.getTaskParams(), BlockingParameters.class);
            default:
                TaskChannel taskChannel = this.getTaskChannelMap().get(taskType);
                if (Objects.isNull(taskChannel)) {
                    return null;
                }
                return taskChannel.parseParameters(parametersNode);
        }
    }

}
