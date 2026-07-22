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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;

@Slf4j
public abstract class AbstractLogicTask<T extends AbstractParameters> implements ILogicTask<T> {

    protected final TaskExecutionContext taskExecutionContext;

    protected final T taskParameters;

    protected TaskExecutionStatus taskExecutionStatus;

    public AbstractLogicTask(final TaskExecutionContext taskExecutionContext) {
        this.taskExecutionContext = taskExecutionContext;
        this.taskParameters = getTaskParameterDeserializer().deserialize(taskExecutionContext.getTaskParams());
        Preconditions.checkNotNull(taskParameters,
                "Deserialize task parameters: " + taskExecutionContext.getTaskParams());
        log.info("Success initialize parameters: \n{}", JSONUtils.toPrettyJsonString(taskParameters));
    }

    @Override
    public TaskExecutionStatus getTaskExecutionState() {
        return taskExecutionStatus;
    }

    protected boolean isRunning() {
        return taskExecutionStatus == TaskExecutionStatus.RUNNING_EXECUTION;
    }

    protected void onTaskRunning() {
        taskExecutionStatus = TaskExecutionStatus.RUNNING_EXECUTION;
    }

    protected void onTaskSuccess() {
        taskExecutionStatus = TaskExecutionStatus.SUCCESS;
    }

    protected void onTaskFailed() {
        taskExecutionStatus = TaskExecutionStatus.FAILURE;
    }

    protected void onTaskKilled() {
        taskExecutionStatus = TaskExecutionStatus.KILL;
    }

    protected void onTaskPaused() {
        taskExecutionStatus = TaskExecutionStatus.PAUSE;
    }
}
