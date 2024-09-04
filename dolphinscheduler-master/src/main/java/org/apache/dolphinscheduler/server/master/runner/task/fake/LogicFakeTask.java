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

package org.apache.dolphinscheduler.server.master.runner.task.fake;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.parameters.LogicFakeTaskParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.task.BaseSyncLogicTask;

import lombok.extern.slf4j.Slf4j;

import com.google.common.annotations.VisibleForTesting;

/**
 * This task is used for testing purposes only.
 * <p> More details about the task can be found in the `it/cases`.
 */
@Slf4j
@VisibleForTesting
public class LogicFakeTask extends BaseSyncLogicTask<LogicFakeTaskParameters> {

    private volatile boolean killFlag;

    private Process process;

    public LogicFakeTask(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                         final TaskExecutionContext taskExecutionContext) {
        super(workflowExecutionRunnable, taskExecutionContext,
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), LogicFakeTaskParameters.class));
    }

    @Override
    public void handle() throws MasterTaskExecuteException {
        try {
            final String shellScript = ParameterUtils.convertParameterPlaceholders(
                    taskParameters.getShellScript(),
                    ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap()));
            final String[] cmd = {"/bin/sh", "-c", shellScript};
            process = Runtime.getRuntime().exec(cmd);
            int exitCode = process.waitFor();
            if (killFlag) {
                taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.KILL);
                return;
            }
            if (exitCode == 0) {
                taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.SUCCESS);
            } else {
                taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.FAILURE);
            }
        } catch (Exception ex) {
            throw new MasterTaskExecuteException("FakeTask execute failed", ex);
        }
    }

    @Override
    public void kill() throws MasterTaskExecuteException {
        log.info("kill task : {}", taskExecutionContext.getTaskName());
        if (process != null && process.isAlive()) {
            killFlag = true;
            process.destroy();
            log.info("kill task : {} succeed", taskExecutionContext.getTaskName());
        }
    }

}
