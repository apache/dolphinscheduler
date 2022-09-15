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

package org.apache.dolphinscheduler.plugin.task.java;

import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

public class JavaTaskChannel implements TaskChannel {

    /**
     * Cancel the mission
     *
     * @param status
     * @return void
     **/
    @Override
    public void cancelApplication(boolean status) {

    }

    /**
     * Create a task
     *
     * @param taskRequest This parameter is the Echternach of the mission
     * @return JavaTask
     **/
    @Override
    public JavaTask createTask(TaskExecutionContext taskRequest) {
        return new JavaTask(taskRequest);
    }

    /**
     * Parses Java task parameters
     *
     * @param parametersNode
     * @return: org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters
     **/
    @Override
    public AbstractParameters parseParameters(ParametersNode parametersNode) {
        return JSONUtils.parseObject(parametersNode.getTaskParams(), JavaParameters.class);
    }

    /**
     * Gets a list of the resources that the task depends on
     *
     * @param parameters
     * @return ResourceParametersHelper
     **/
    @Override
    public ResourceParametersHelper getResources(String parameters) {
        return null;
    }
}
