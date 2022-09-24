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

package org.apache.dolphinscheduler.plugin.task.api.loop.template.http;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.loop.BaseLoopTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.loop.LoopTaskInstanceInfo;
import org.apache.dolphinscheduler.plugin.task.api.loop.LoopTaskInstanceStatus;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.http.parser.HttpTaskDefinitionParser;

import javax.annotation.Nullable;

import lombok.NonNull;

public abstract class BaseHttpTemplateLoopTaskExecutor extends BaseLoopTaskExecutor {

    private final HttpLoopTaskDefinition httpLoopTaskDefinition;

    public BaseHttpTemplateLoopTaskExecutor(@NonNull TaskExecutionContext taskExecutionContext,
                                            @NonNull String taskDefinitionYamlFile) {
        super(taskExecutionContext);
        this.httpLoopTaskDefinition = new HttpTaskDefinitionParser().parse(taskDefinitionYamlFile);
    }

    @Override
    public @NonNull LoopTaskInstanceInfo submitLoopTask() {
        return httpLoopTaskDefinition.getSubmitTaskMethod().submitLoopTask();
    }

    @Override
    public @NonNull LoopTaskInstanceStatus queryTaskInstanceStatus(@NonNull LoopTaskInstanceInfo taskInstanceInfo) {
        return httpLoopTaskDefinition.getQueryTaskStateMethod().queryTaskInstanceStatus(taskInstanceInfo);
    }

    @Override
    public void cancelLoopTaskInstance(@Nullable LoopTaskInstanceInfo taskInstanceInfo) {
        if (taskInstanceInfo == null) {
            return;
        }
        httpLoopTaskDefinition.getCancelTaskMethod().cancelTaskInstance(taskInstanceInfo);
    }
}
