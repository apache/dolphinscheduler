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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.fake;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.parameters.LogicFakeTaskParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.AbstractLogicTask;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.ITaskParameterDeserializer;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;

/**
 * This task is used for testing purposes only.
 * <p> More details about the task can be found in the `it/cases`.
 */
@Slf4j
@VisibleForTesting
public class LogicFakeTask extends AbstractLogicTask<LogicFakeTaskParameters> {

    private Process process;

    public LogicFakeTask(final TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        onTaskRunning();
    }

    @Override
    public void start() throws MasterTaskExecuteException {
        try {
            log.info("Begin to execute LogicFakeTask: {}", taskExecutionContext.getTaskName());

            final String shellScript = ParameterUtils.convertParameterPlaceholders(
                    taskParameters.getShellScript(),
                    ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap()));
            final String[] cmd = {"/bin/sh", "-c", shellScript};
            process = Runtime.getRuntime().exec(cmd);
            int exitCode = process.waitFor();
            if (taskExecutionStatus != TaskExecutionStatus.RUNNING_EXECUTION) {
                // The task has been killed
                return;
            }
            if (exitCode == 0) {
                log.info("LogicFakeTask: {} execute success with exit code: {}",
                        taskExecutionContext.getTaskName(),
                        exitCode);
                onTaskSuccess();
            } else {
                log.info("LogicFakeTask: {} execute failed with exit code: {}",
                        taskExecutionContext.getTaskName(),
                        exitCode);
                onTaskFailed();
            }
        } catch (Exception ex) {
            throw new MasterTaskExecuteException("FakeTask execute failed", ex);
        }
    }

    @Override
    public void pause() throws MasterTaskExecuteException {
        log.info("LogicFakeTask: {} doesn't support pause", taskExecutionContext.getTaskName());
    }

    @Override
    public void kill() throws MasterTaskExecuteException {
        if (process != null && process.isAlive()) {
            // todo: use shell script to kill the process?
            onTaskKilled();
            process.destroyForcibly();
            log.info("Kill LogicFakeTask: {} succeed", taskExecutionContext.getTaskName());
        }
    }

    @Override
    public ITaskParameterDeserializer<LogicFakeTaskParameters> getTaskParameterDeserializer() {
        return taskParamsJson -> JSONUtils.parseObject(taskParamsJson, new TypeReference<LogicFakeTaskParameters>() {
        });
    }

}
