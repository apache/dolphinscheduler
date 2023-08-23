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

package org.apache.dolphinscheduler.server.worker.runner.operator;

import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceKillRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceKillResponse;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecuteRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Slf4j
@Component
public class TaskInstanceKillOperationFunction
        implements
            ITaskInstanceOperationFunction<TaskInstanceKillRequest, TaskInstanceKillResponse> {

    @Autowired
    private WorkerManagerThread workerManager;

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Override
    public TaskInstanceKillResponse operate(TaskInstanceKillRequest taskInstanceKillRequest) {
        log.info("Receive TaskInstanceKillRequest: {}", taskInstanceKillRequest);

        int taskInstanceId = taskInstanceKillRequest.getTaskInstanceId();
        try {
            LogUtils.setTaskInstanceIdMDC(taskInstanceId);
            TaskExecutionContext taskExecutionContext =
                    TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId);
            if (taskExecutionContext == null) {
                log.error("Cannot find TaskExecutionContext for taskInstance: {}", taskInstanceId);
                return TaskInstanceKillResponse.fail("Cannot find TaskExecutionContext");
            }
            LogUtils.setTaskInstanceLogFullPathMDC(taskExecutionContext.getLogPath());

            boolean result = doKill(taskExecutionContext);
            this.cancelApplication(taskInstanceId);

            int processId = taskExecutionContext.getProcessId();
            if (processId == 0) {
                workerManager.killTaskBeforeExecuteByInstanceId(taskInstanceId);
                taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.KILL);
                TaskExecutionContextCacheManager.removeByTaskInstanceId(taskInstanceId);
                log.info("The task has not been executed and has been cancelled, task id:{}", taskInstanceId);
                return TaskInstanceKillResponse.success(taskExecutionContext);
            }

            taskExecutionContext
                    .setCurrentExecutionStatus(result ? TaskExecutionStatus.SUCCESS : TaskExecutionStatus.FAILURE);

            TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
            messageRetryRunner.removeRetryMessages(taskExecutionContext.getTaskInstanceId());
            return TaskInstanceKillResponse.success(taskExecutionContext);
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
            LogUtils.removeTaskInstanceLogFullPathMDC();
        }
    }

    private boolean doKill(TaskExecutionContext taskExecutionContext) {
        // kill system process
        boolean processFlag = killProcess(taskExecutionContext.getTenantCode(), taskExecutionContext.getProcessId());

        // kill yarn or k8s application
        try {
            ProcessUtils.cancelApplication(taskExecutionContext);
        } catch (TaskException e) {
            return false;
        }
        return processFlag;
    }

    protected void cancelApplication(int taskInstanceId) {
        WorkerTaskExecuteRunnable workerTaskExecuteRunnable = workerManager.getTaskExecuteThread(taskInstanceId);
        if (workerTaskExecuteRunnable == null) {
            log.warn("taskExecuteThread not found, taskInstanceId:{}", taskInstanceId);
            return;
        }
        AbstractTask task = workerTaskExecuteRunnable.getTask();
        if (task == null) {
            log.warn("task not found, taskInstanceId:{}", taskInstanceId);
            return;
        }
        try {
            task.cancel();
        } catch (Exception e) {
            log.error("kill task error", e);
        }
        log.info("kill task by cancelApplication, task id:{}", taskInstanceId);
    }

    protected boolean killProcess(String tenantCode, Integer processId) {
        // todo: directly interrupt the process
        boolean processFlag = true;
        if (processId == null || processId.equals(0)) {
            return true;
        }
        try {
            String pidsStr = ProcessUtils.getPidsStr(processId);
            if (!Strings.isNullOrEmpty(pidsStr)) {
                String cmd = String.format("kill -9 %s", pidsStr);
                cmd = OSUtils.getSudoCmd(tenantCode, cmd);
                log.info("process id:{}, cmd:{}", processId, cmd);
                OSUtils.exeCmd(cmd);
            }
        } catch (Exception e) {
            processFlag = false;
            log.error("kill task error", e);
        }
        return processFlag;
    }
}
