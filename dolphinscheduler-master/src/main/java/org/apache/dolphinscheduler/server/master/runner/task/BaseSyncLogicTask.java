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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseSyncLogicTask<T extends AbstractParameters> implements ISyncLogicTask {

    protected final TaskExecutionContext taskExecutionContext;
    protected final T taskParameters;

    protected BaseSyncLogicTask(TaskExecutionContext taskExecutionContext, T taskParameters) {
        this.taskExecutionContext = taskExecutionContext;
        this.taskParameters = taskParameters;
        log.info("Success initialize task parameters: \n{}", JSONUtils.toPrettyJsonString(taskParameters));
    }

    @Override
    public void pause() throws MasterTaskExecuteException {
        log.warn("The Sync task does not support pause operation");
    }

    @Override
    public void kill() throws MasterTaskExecuteException {
        log.warn("The Sync task does not support kill operation");
    }

    @Override
    public TaskExecutionContext getTaskExecutionContext() {
        return taskExecutionContext;
    }

    @Override
    public AbstractParameters getTaskParameters() {
        return taskParameters;
    }
}
