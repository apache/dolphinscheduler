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

package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

/**
 * Flink Materialized Table Task Channel.
 * 
 * This class serves as the entry point for creating and managing Flink Materialized Table tasks.
 * It implements the TaskChannel interface to provide task creation and parameter parsing capabilities.
 */
public class FlinkMaterializedTableTaskChannel implements TaskChannel {

    /**
     * Cancels the application.
     * 
     * This method is not used in this implementation as the task is handled directly.
     *
     * @param status The status of the cancellation operation
     */
    @Override
    public void cancelApplication(boolean status) {
        // Not used in this implementation
    }

    /**
     * Creates a new Flink Materialized Table task instance.
     *
     * @param taskRequest The task execution context containing task parameters and runtime information
     * @return A new instance of FlinkMaterializedTableTask
     */
    @Override
    public AbstractTask createTask(TaskExecutionContext taskRequest) {
        return new FlinkMaterializedTableTask(taskRequest);
    }

    /**
     * Parses the task parameters from the parameters node.
     *
     * @param parametersNode The parameters node containing task configuration
     * @return Parsed FlinkMaterializedTableParameters instance
     */
    @Override
    public AbstractParameters parseParameters(ParametersNode parametersNode) {
        return JSONUtils.parseObject(parametersNode.getTaskParams(), FlinkMaterializedTableParameters.class);
    }

    /**
     * Gets the resource parameters helper for the task.
     *
     * @param parameters The task parameters as a JSON string
     * @return ResourceParametersHelper instance for managing task resources
     */
    @Override
    public ResourceParametersHelper getResources(String parameters) {
        return JSONUtils.parseObject(parameters, FlinkMaterializedTableParameters.class).getResources();
    }
}
