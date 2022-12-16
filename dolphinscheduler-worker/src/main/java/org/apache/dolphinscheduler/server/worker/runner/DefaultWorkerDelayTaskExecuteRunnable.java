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

package org.apache.dolphinscheduler.server.worker.runner;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;

import org.apache.dolphinscheduler.common.storage.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.ProcessUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;

import org.apache.commons.lang.StringUtils;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import lombok.NonNull;

public class DefaultWorkerDelayTaskExecuteRunnable extends WorkerDelayTaskExecuteRunnable {

    public DefaultWorkerDelayTaskExecuteRunnable(@NonNull TaskExecutionContext taskExecutionContext,
                                                 @NonNull WorkerConfig workerConfig,
                                                 @NonNull String workflowMaster,
                                                 @NonNull WorkerMessageSender workerMessageSender,
                                                 @NonNull AlertClientService alertClientService,
                                                 @NonNull TaskPluginManager taskPluginManager,
                                                 @Nullable StorageOperate storageOperate) {
        super(taskExecutionContext, workerConfig, workflowMaster, workerMessageSender, alertClientService,
                taskPluginManager, storageOperate);
    }

    @Override
    public void executeTask() throws TaskException {
        if (task == null) {
            throw new TaskException("The task plugin instance is not initialized");
        }

        // not retry submit task if appId exists
        if (StringUtils.isNotEmpty(taskExecutionContext.getAppIds())) {
            logger.info("task {} has already been submitted before", taskExecutionContext);
            task.setExitStatusCode(TaskConstants.EXIT_CODE_SUCCESS);
            task.setAppIds(taskExecutionContext.getAppIds());
            task.setProcessId(taskExecutionContext.getProcessId());
            taskExecutionContext.getCompletedCollectAppId().complete(true);
            sendTaskRunning();
            return;
        }
        task.handle();

        // send process id to master to kill process when worker crashes before get appId
        // if process exit after submit, process id is 0, not usable
        if (task.getProcessId() > 0) {
            taskExecutionContext.setProcessId(task.getProcessId());
            sendTaskRunning();
        }
        // wait appId, report status with appId in time
        Set<String> appIds = task.getApplicationIds();
        if (appIds.size() > 0) {
            task.setAppIds(String.join(TaskConstants.COMMA, appIds));
            sendTaskRunning();
        }

        if (Objects.nonNull(task.getProcess()) && task.exitAfterSubmitTask()) {
            // monitor by app id
            long startTime = System.currentTimeMillis();
            long remainTime = taskExecutionContext.getRemainTime();

            boolean status = false;
            try {
                status = task.getProcess().waitFor(remainTime, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.info("process {} interrupted", task.getProcessId());
            }
            if (status) {

                // SHELL task state
                task.setExitStatusCode(task.getProcess().exitValue());

            } else {
                logger.error("process has failure, the task timeout configuration value is:{}, ready to kill ...",
                        taskExecutionContext.getTaskTimeout());
                ProcessUtils.kill(taskExecutionContext);
                task.setExitStatusCode(EXIT_CODE_FAILURE);
            }
            logger.info(
                    "waiting process exit, execute path:{}, processId:{} ,exitStatusCode:{}, processWaitForStatus:{}, take {}",
                    taskExecutionContext.getExecutePath(), task.getProcessId(), task.getExitStatusCode(), status,
                    System.currentTimeMillis() - startTime);

        }
    }

    @Override
    protected void afterExecute() {
        super.afterExecute();
    }

    @Override
    protected void afterThrowing(Throwable throwable) throws TaskException {
        super.afterThrowing(throwable);
    }
}
