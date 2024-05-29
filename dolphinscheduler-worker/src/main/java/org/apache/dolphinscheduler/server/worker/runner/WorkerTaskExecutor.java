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

import static ch.qos.logback.classic.ClassicConstants.FINALIZE_SESSION_MARKER;
import static org.apache.dolphinscheduler.common.constants.Constants.DRY_RUN_FLAG_YES;
import static org.apache.dolphinscheduler.common.constants.Constants.K8S_CONFIG_REGEX;
import static org.apache.dolphinscheduler.common.constants.Constants.SINGLE_SLASH;

import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.log.SensitiveDataConverter;
import org.apache.dolphinscheduler.common.log.remote.RemoteLogUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.extract.alert.IAlertOperator;
import org.apache.dolphinscheduler.extract.alert.request.AlertSendRequest;
import org.apache.dolphinscheduler.extract.alert.request.AlertSendResponse;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.base.utils.Host;
import org.apache.dolphinscheduler.extract.master.transportor.ITaskInstanceExecutionEvent;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginException;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.log.TaskInstanceLogHeader;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClient;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.apache.dolphinscheduler.server.worker.utils.TaskExecutionContextUtils;
import org.apache.dolphinscheduler.server.worker.utils.TaskFilesTransferUtils;
import org.apache.dolphinscheduler.server.worker.utils.TenantUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Optional;

import javax.annotation.Nullable;

import lombok.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public abstract class WorkerTaskExecutor implements Runnable {

    protected static final Logger log = LoggerFactory.getLogger(WorkerTaskExecutor.class);

    protected final TaskExecutionContext taskExecutionContext;
    protected final WorkerConfig workerConfig;
    protected final WorkerMessageSender workerMessageSender;
    protected final @Nullable StorageOperate storageOperate;
    protected final WorkerRegistryClient workerRegistryClient;

    protected @Nullable AbstractTask task;

    protected WorkerTaskExecutor(
                                 @NonNull TaskExecutionContext taskExecutionContext,
                                 @NonNull WorkerConfig workerConfig,
                                 @NonNull WorkerMessageSender workerMessageSender,
                                 @Nullable StorageOperate storageOperate,
                                 @NonNull WorkerRegistryClient workerRegistryClient) {
        this.taskExecutionContext = taskExecutionContext;
        this.workerConfig = workerConfig;
        this.workerMessageSender = workerMessageSender;
        this.storageOperate = storageOperate;
        this.workerRegistryClient = workerRegistryClient;
        SensitiveDataConverter.addMaskPattern(K8S_CONFIG_REGEX);
    }

    protected abstract void executeTask(TaskCallBack taskCallBack);

    protected void afterExecute() throws TaskException {
        if (task == null) {
            throw new TaskException("The current task instance is null");
        }
        sendAlertIfNeeded();

        sendTaskResult();

        WorkerTaskExecutorHolder.remove(taskExecutionContext.getTaskInstanceId());
        log.info("Remove the current task execute context from worker cache");
        clearTaskExecPathIfNeeded();

    }

    protected void afterThrowing(Throwable throwable) throws TaskException {
        if (cancelTask()) {
            log.info("Cancel the task successfully");
        }
        WorkerTaskExecutorHolder.remove(taskExecutionContext.getTaskInstanceId());
        taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.FAILURE);
        taskExecutionContext.setEndTime(System.currentTimeMillis());
        workerMessageSender.sendMessageWithRetry(taskExecutionContext,
                ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType.FINISH);
        log.info("Get a exception when execute the task, will send the task status: {} to master: {}",
                TaskExecutionStatus.FAILURE.name(), taskExecutionContext.getHost());

    }

    protected boolean cancelTask() {
        // cancel the task
        if (task == null) {
            return true;
        }
        try {
            task.cancel();
            ProcessUtils.cancelApplication(taskExecutionContext);
            return true;
        } catch (Exception e) {
            log.error("Cancel task failed, this will not affect the taskInstance status, but you need to check manual",
                    e);
            return false;
        }
    }

    @Override
    public void run() {
        try {
            LogUtils.setWorkflowAndTaskInstanceIDMDC(taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId());
            LogUtils.setTaskInstanceLogFullPathMDC(taskExecutionContext.getLogPath());

            TaskInstanceLogHeader.printInitializeTaskContextHeader();
            initializeTask();

            if (DRY_RUN_FLAG_YES == taskExecutionContext.getDryRun()) {
                taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.SUCCESS);
                taskExecutionContext.setEndTime(System.currentTimeMillis());
                WorkerTaskExecutorHolder.remove(taskExecutionContext.getTaskInstanceId());
                workerMessageSender.sendMessageWithRetry(taskExecutionContext,
                        ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType.FINISH);
                log.info(
                        "The current execute mode is dry run, will stop the subsequent process and set the taskInstance status to success");
                return;
            }
            TaskInstanceLogHeader.printLoadTaskInstancePluginHeader();
            beforeExecute();

            TaskCallBack taskCallBack = TaskCallbackImpl.builder()
                    .workerMessageSender(workerMessageSender)
                    .taskExecutionContext(taskExecutionContext)
                    .build();

            TaskInstanceLogHeader.printExecuteTaskHeader();
            executeTask(taskCallBack);

            TaskInstanceLogHeader.printFinalizeTaskHeader();
            afterExecute();
            closeLogAppender();
        } catch (Throwable ex) {
            log.error("Task execute failed, due to meet an exception", ex);
            afterThrowing(ex);
            closeLogAppender();
        } finally {
            LogUtils.removeWorkflowAndTaskInstanceIdMDC();
            LogUtils.removeTaskInstanceLogFullPathMDC();
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
        workerMessageSender.sendMessageWithRetry(taskExecutionContext,
                ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType.RUNNING);
        log.info("Send task status {} master: {}", TaskExecutionStatus.RUNNING_EXECUTION.name(),
                taskExecutionContext.getHost());

        // In most of case the origin tenant is the same as the current tenant
        // Except `default` tenant. The originTenant is used to download the resources
        String originTenant = taskExecutionContext.getTenantCode();
        taskExecutionContext.setTenantCode(TenantUtils.getOrCreateActualTenant(workerConfig, taskExecutionContext));
        log.info("TenantCode: {} check successfully", taskExecutionContext.getTenantCode());

        TaskExecutionContextUtils.createTaskInstanceWorkingDirectory(taskExecutionContext);
        log.info("WorkflowInstanceExecDir: {} check successfully", taskExecutionContext.getExecutePath());

        TaskChannel taskChannel =
                Optional.ofNullable(TaskPluginManager.getTaskChannelMap().get(taskExecutionContext.getTaskType()))
                        .orElseThrow(() -> new TaskPluginException(taskExecutionContext.getTaskType()
                                + " task plugin not found, please check the task type is correct."));

        log.info("Create TaskChannel: {} successfully", taskChannel.getClass().getName());

        ResourceContext resourceContext = TaskExecutionContextUtils.downloadResourcesIfNeeded(originTenant, taskChannel,
                storageOperate, taskExecutionContext);
        taskExecutionContext.setResourceContext(resourceContext);
        log.info("Download resources successfully: \n{}", taskExecutionContext.getResourceContext());

        TaskFilesTransferUtils.downloadUpstreamFiles(taskExecutionContext, storageOperate);
        log.info("Download upstream files: {} successfully",
                TaskFilesTransferUtils.getFileLocalParams(taskExecutionContext, Direct.IN));

        task = taskChannel.createTask(taskExecutionContext);
        log.info("Task plugin instance: {} create successfully", taskExecutionContext.getTaskType());

        // todo: remove the init method, this should initialize in constructor method
        task.init();
        log.info("Success initialized task plugin instance successfully");

        task.getParameters().setVarPool(taskExecutionContext.getVarPool());
        log.info("Set taskVarPool: {} successfully", taskExecutionContext.getVarPool());

    }

    protected void sendAlertIfNeeded() {
        if (!task.getNeedAlert()) {
            return;
        }

        // todo: We need to send the alert to the master rather than directly send to the alert server
        Optional<Host> alertServerAddressOptional = workerRegistryClient.getAlertServerAddress();
        if (!alertServerAddressOptional.isPresent()) {
            log.error("Cannot get alert server address, please check the alert server is running");
            return;
        }
        Host alertServerAddress = alertServerAddressOptional.get();

        TaskAlertInfo taskAlertInfo = task.getTaskAlertInfo();
        AlertSendRequest alertSendRequest = new AlertSendRequest(
                taskAlertInfo.getAlertGroupId(),
                taskAlertInfo.getTitle(),
                taskAlertInfo.getContent(),
                task.getExitStatus() == TaskExecutionStatus.SUCCESS ? WarningType.SUCCESS.getCode()
                        : WarningType.FAILURE.getCode());
        try {
            IAlertOperator alertOperator = SingletonJdkDynamicRpcClientProxyFactory
                    .getProxyClient(alertServerAddress.getAddress(), IAlertOperator.class);
            AlertSendResponse alertSendResponse = alertOperator.sendAlert(alertSendRequest);
            log.info("Send alert to: {} successfully, response: {}", alertServerAddress, alertSendResponse);
        } catch (Exception e) {
            log.error("Send alert: {} to: {} failed", alertSendRequest, alertServerAddress, e);
        }
    }

    protected void sendTaskResult() {
        taskExecutionContext.setCurrentExecutionStatus(task.getExitStatus());
        taskExecutionContext.setProcessId(task.getProcessId());
        taskExecutionContext.setAppIds(task.getAppIds());
        taskExecutionContext.setVarPool(JSONUtils.toJsonString(task.getParameters().getVarPool()));
        taskExecutionContext.setEndTime(System.currentTimeMillis());

        // upload out files and modify the "OUT FILE" property in VarPool
        TaskFilesTransferUtils.uploadOutputFiles(taskExecutionContext, storageOperate);

        log.info("Upload output files: {} successfully",
                TaskFilesTransferUtils.getFileLocalParams(taskExecutionContext, Direct.OUT));

        workerMessageSender.sendMessageWithRetry(taskExecutionContext,
                ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType.FINISH);
        log.info("Send task execute status: {} to master : {}", taskExecutionContext.getCurrentExecutionStatus().name(),
                taskExecutionContext.getHost());
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

    protected void closeLogAppender() {
        try {
            if (RemoteLogUtils.isRemoteLoggingEnable()) {
                RemoteLogUtils.sendRemoteLog(taskExecutionContext.getLogPath());
                log.info("Log handler sends task log {} to remote storage asynchronously.",
                        taskExecutionContext.getLogPath());
            }
        } catch (Exception ex) {
            log.error("Send remote log failed", ex);
        } finally {
            log.info(FINALIZE_SESSION_MARKER, FINALIZE_SESSION_MARKER.toString());
        }
    }

    public @NonNull TaskExecutionContext getTaskExecutionContext() {
        return taskExecutionContext;
    }

    public @Nullable AbstractTask getTask() {
        return task;
    }

}
