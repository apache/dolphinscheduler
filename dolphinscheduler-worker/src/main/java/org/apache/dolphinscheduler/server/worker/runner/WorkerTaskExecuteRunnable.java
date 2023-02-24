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

import static org.apache.dolphinscheduler.common.constants.Constants.DRY_RUN_FLAG_YES;
import static org.apache.dolphinscheduler.common.constants.Constants.SINGLE_SLASH;

import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.log.remote.RemoteLogUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginException;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendRequestCommand;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcClient;
import org.apache.dolphinscheduler.server.worker.utils.TaskExecutionCheckerUtils;
import org.apache.dolphinscheduler.server.worker.utils.TaskFilesTransferUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

import javax.annotation.Nullable;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public abstract class WorkerTaskExecuteRunnable implements Runnable {

    protected final Logger log = LoggerFactory
            .getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, WorkerTaskExecuteRunnable.class));

    protected final TaskExecutionContext taskExecutionContext;
    protected final WorkerConfig workerConfig;
    protected final String masterAddress;
    protected final WorkerMessageSender workerMessageSender;
    protected final TaskPluginManager taskPluginManager;
    protected final @Nullable StorageOperate storageOperate;
    protected final WorkerRpcClient workerRpcClient;

    protected @Nullable AbstractTask task;

    protected WorkerTaskExecuteRunnable(
                                        @NonNull TaskExecutionContext taskExecutionContext,
                                        @NonNull WorkerConfig workerConfig,
                                        @NonNull String masterAddress,
                                        @NonNull WorkerMessageSender workerMessageSender,
                                        @NonNull WorkerRpcClient workerRpcClient,
                                        @NonNull TaskPluginManager taskPluginManager,
                                        @Nullable StorageOperate storageOperate) {
        this.taskExecutionContext = taskExecutionContext;
        this.workerConfig = workerConfig;
        this.masterAddress = masterAddress;
        this.workerMessageSender = workerMessageSender;
        this.workerRpcClient = workerRpcClient;
        this.taskPluginManager = taskPluginManager;
        this.storageOperate = storageOperate;
        String taskLogName =
                LogUtils.buildTaskId(DateUtils.timeStampToDate(taskExecutionContext.getFirstSubmitTime()),
                        taskExecutionContext.getProcessDefineCode(),
                        taskExecutionContext.getProcessDefineVersion(),
                        taskExecutionContext.getProcessInstanceId(),
                        taskExecutionContext.getTaskInstanceId());
        taskExecutionContext.setTaskLogName(taskLogName);
        log.info("Set task log name: {}", taskLogName);
    }

    protected abstract void executeTask(TaskCallBack taskCallBack);

    protected void afterExecute() throws TaskException {
        if (task == null) {
            throw new TaskException("The current task instance is null");
        }
        sendAlertIfNeeded();

        sendTaskResult();

        TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        log.info("Remove the current task execute context from worker cache");
        clearTaskExecPathIfNeeded();

        sendTaskLogOnWorkerToRemoteIfNeeded();
    }

    protected void afterThrowing(Throwable throwable) throws TaskException {
        cancelTask();
        TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.FAILURE);
        taskExecutionContext.setEndTime(System.currentTimeMillis());
        workerMessageSender.sendMessageWithRetry(taskExecutionContext, masterAddress, CommandType.TASK_EXECUTE_RESULT);
        log.info(
                "Get a exception when execute the task, will send the task execute result to master, the current task execute result is {}",
                TaskExecutionStatus.FAILURE);

        sendTaskLogOnWorkerToRemoteIfNeeded();
    }

    public void cancelTask() {
        // cancel the task
        if (task != null) {
            try {
                task.cancel();
                ProcessUtils.cancelApplication(taskExecutionContext);
            } catch (Exception e) {
                log.error(
                        "Task execute failed and cancel the application failed, this will not affect the taskInstance status, but you need to check manual",
                        e);
            }
        }
    }

    @Override
    public void run() {
        try {
            // set the thread name to make sure the log be written to the task log file
            Thread.currentThread().setName(taskExecutionContext.getTaskLogName());

            LogUtils.setWorkflowAndTaskInstanceIDMDC(taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId());
            log.info("Begin to pulling task");

            initializeTask();

            if (DRY_RUN_FLAG_YES == taskExecutionContext.getDryRun()) {
                taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.SUCCESS);
                taskExecutionContext.setEndTime(System.currentTimeMillis());
                TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
                workerMessageSender.sendMessageWithRetry(taskExecutionContext, masterAddress,
                        CommandType.TASK_EXECUTE_RESULT);
                log.info(
                        "The current execute mode is dry run, will stop the subsequent process and set the taskInstance status to success");
                return;
            }

            beforeExecute();

            TaskCallBack taskCallBack = TaskCallbackImpl.builder().workerMessageSender(workerMessageSender)
                    .masterAddress(masterAddress).build();
            executeTask(taskCallBack);

            afterExecute();

        } catch (Throwable ex) {
            log.error("Task execute failed, due to meet an exception", ex);
            afterThrowing(ex);
        } finally {
            LogUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }

    protected void initializeTask() {
        log.info("Begin to initialize task");

        long taskStartTime = System.currentTimeMillis();
        taskExecutionContext.setStartTime(taskStartTime);
        log.info("Set task startTime: {}", taskStartTime);

        String taskAppId = String.format("%s_%s", taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId());
        taskExecutionContext.setTaskAppId(taskAppId);
        log.info("Set task appId: {}", taskAppId);

        log.info("End initialize task {}", JSONUtils.toPrettyJsonString(taskExecutionContext));
    }

    protected void beforeExecute() {
        taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.RUNNING_EXECUTION);
        workerMessageSender.sendMessageWithRetry(taskExecutionContext, masterAddress, CommandType.TASK_EXECUTE_RUNNING);
        log.info("Set task status to {}", TaskExecutionStatus.RUNNING_EXECUTION);

        TaskExecutionCheckerUtils.checkTenantExist(workerConfig, taskExecutionContext);
        log.info("TenantCode:{} check success", taskExecutionContext.getTenantCode());

        TaskExecutionCheckerUtils.createProcessLocalPathIfAbsent(taskExecutionContext);
        log.info("ProcessExecDir:{} check success", taskExecutionContext.getExecutePath());

        TaskExecutionCheckerUtils.downloadResourcesIfNeeded(storageOperate, taskExecutionContext, log);
        log.info("Resources:{} check success", taskExecutionContext.getResources());

        TaskFilesTransferUtils.downloadUpstreamFiles(taskExecutionContext, storageOperate);

        TaskChannel taskChannel = taskPluginManager.getTaskChannelMap().get(taskExecutionContext.getTaskType());
        if (null == taskChannel) {
            throw new TaskPluginException(String.format("%s task plugin not found, please check config file.",
                    taskExecutionContext.getTaskType()));
        }
        task = taskChannel.createTask(taskExecutionContext);
        if (task == null) {
            throw new TaskPluginException(String.format("%s task is null, please check the task plugin is correct",
                    taskExecutionContext.getTaskType()));
        }
        log.info("Task plugin: {} create success", taskExecutionContext.getTaskType());

        task.init();
        log.info("Success initialized task plugin instance success");

        task.getParameters().setVarPool(taskExecutionContext.getVarPool());
        log.info("Success set taskVarPool: {}", taskExecutionContext.getVarPool());

    }

    protected void sendAlertIfNeeded() {
        if (!task.getNeedAlert()) {
            return;
        }
        log.info("The current task need to send alert, begin to send alert");
        TaskExecutionStatus status = task.getExitStatus();
        TaskAlertInfo taskAlertInfo = task.getTaskAlertInfo();
        int strategy =
                status == TaskExecutionStatus.SUCCESS ? WarningType.SUCCESS.getCode() : WarningType.FAILURE.getCode();
        AlertSendRequestCommand alertCommand = new AlertSendRequestCommand(
                taskAlertInfo.getAlertGroupId(),
                taskAlertInfo.getTitle(),
                taskAlertInfo.getContent(),
                strategy);
        try {
            workerRpcClient.send(Host.of(workerConfig.getAlertListenHost()), alertCommand.convert2Command());
            log.info("Success send alert");
        } catch (RemotingException e) {
            log.error("Send alert failed, alertCommand: {}", alertCommand, e);
        }
    }

    protected void sendTaskResult() {
        taskExecutionContext.setCurrentExecutionStatus(task.getExitStatus());
        taskExecutionContext.setEndTime(System.currentTimeMillis());
        taskExecutionContext.setProcessId(task.getProcessId());
        taskExecutionContext.setAppIds(task.getAppIds());
        taskExecutionContext.setVarPool(JSONUtils.toJsonString(task.getParameters().getVarPool()));
        // upload out files and modify the "OUT FILE" property in VarPool
        TaskFilesTransferUtils.uploadOutputFiles(taskExecutionContext, storageOperate);
        workerMessageSender.sendMessageWithRetry(taskExecutionContext, masterAddress, CommandType.TASK_EXECUTE_RESULT);

        log.info("Send task execute result to master, the current task status: {}",
                taskExecutionContext.getCurrentExecutionStatus());
    }

    protected void sendTaskLogOnWorkerToRemoteIfNeeded() {
        if (taskExecutionContext.isLogBufferEnable()) {
            return;
        }

        if (RemoteLogUtils.isRemoteLoggingEnable()) {
            RemoteLogUtils.sendRemoteLog(taskExecutionContext.getLogPath());
            log.info("Worker sends task log {} to remote storage asynchronously.",
                    taskExecutionContext.getLogPath());
        }
    }

    protected void clearTaskExecPathIfNeeded() {
        String execLocalPath = taskExecutionContext.getExecutePath();
        if (!CommonUtils.isDevelopMode()) {
            log.info("The current execute mode isn't develop mode, will clear the task execute file: {}",
                    execLocalPath);
            // get exec dir
            if (Strings.isNullOrEmpty(execLocalPath)) {
                log.warn("The task execute file is {} no need to clear", taskExecutionContext.getTaskName());
                return;
            }

            if (SINGLE_SLASH.equals(execLocalPath)) {
                log.warn("The task execute file is '/', direct deletion is not allowed");
                return;
            }

            try {
                org.apache.commons.io.FileUtils.deleteDirectory(new File(execLocalPath));
                log.info("Success clear the task execute file: {}", execLocalPath);
            } catch (IOException e) {
                if (e instanceof NoSuchFileException) {
                    // this is expected
                } else {
                    log.error(
                            "Delete task execute file: {} failed, this will not affect the task status, but you need to clear this manually",
                            execLocalPath, e);
                }
            }
        } else {
            log.info("The current execute mode is develop mode, will not clear the task execute file: {}",
                    execLocalPath);
        }
    }

    public @NonNull TaskExecutionContext getTaskExecutionContext() {
        return taskExecutionContext;
    }

    public @Nullable AbstractTask getTask() {
        return task;
    }

}
