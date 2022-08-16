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

import com.google.common.base.Strings;
import lombok.NonNull;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.exception.StorageOperateNoConfiguredException;
import org.apache.dolphinscheduler.common.storage.StorageOperate;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.server.worker.metrics.WorkerServerMetrics;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import static org.apache.dolphinscheduler.common.Constants.SINGLE_SLASH;

/**
 * task scheduler thread
 */
public class TaskExecuteThread implements Runnable, Delayed {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(TaskExecuteThread.class);

    /**
     * task instance
     */
    private final TaskExecutionContext taskExecutionContext;

    private final String masterAddress;

    private final StorageOperate storageOperate;

    /**
     * abstract task
     */
    private AbstractTask task;

    /**
     * task callback service
     */
    private final WorkerMessageSender workerMessageSender;

    /**
     * alert client server
     */
    private final AlertClientService alertClientService;

    private TaskPluginManager taskPluginManager;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     * @param workerMessageSender  used for worker send message to master
     */
    public TaskExecuteThread(@NonNull TaskExecutionContext taskExecutionContext,
                             @NonNull String masterAddress,
                             @NonNull WorkerMessageSender workerMessageSender,
                             @NonNull AlertClientService alertClientService,
                             StorageOperate storageOperate) {
        this.taskExecutionContext = taskExecutionContext;
        this.masterAddress = masterAddress;
        this.workerMessageSender = workerMessageSender;
        this.alertClientService = alertClientService;
        this.storageOperate = storageOperate;
    }

    public TaskExecuteThread(@NonNull TaskExecutionContext taskExecutionContext,
                             @NonNull String masterAddress,
                             @NonNull WorkerMessageSender workerMessageSender,
                             @NonNull AlertClientService alertClientService,
                             @NonNull TaskPluginManager taskPluginManager,
                             StorageOperate storageOperate) {
        this.taskExecutionContext = taskExecutionContext;
        this.masterAddress = masterAddress;
        this.workerMessageSender = workerMessageSender;
        this.alertClientService = alertClientService;
        this.taskPluginManager = taskPluginManager;
        this.storageOperate = storageOperate;
    }

    @Override
    public void run() {
        try {
            LoggerUtils.setWorkflowAndTaskInstanceIDMDC(taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId());
            if (Constants.DRY_RUN_FLAG_YES == taskExecutionContext.getDryRun()) {
                taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.SUCCESS);
                taskExecutionContext.setStartTime(new Date());
                taskExecutionContext.setEndTime(new Date());
                TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
                workerMessageSender.sendMessageWithRetry(taskExecutionContext,
                        masterAddress,
                        CommandType.TASK_EXECUTE_RESULT);
                logger.info("Task dry run success");
                return;
            }
        } finally {
            LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
        try {
            LoggerUtils.setWorkflowAndTaskInstanceIDMDC(taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId());
            logger.info("script path : {}", taskExecutionContext.getExecutePath());
            if (taskExecutionContext.getStartTime() == null) {
                taskExecutionContext.setStartTime(new Date());
            }
            logger.info("the task begins to execute. task instance id: {}", taskExecutionContext.getTaskInstanceId());

            // callback task execute running
            taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.RUNNING_EXECUTION);
            workerMessageSender.sendMessageWithRetry(taskExecutionContext,
                    masterAddress,
                    CommandType.TASK_EXECUTE_RUNNING);

            // copy hdfs/minio file to local
            List<Pair<String, String>> fileDownloads = downloadCheck(taskExecutionContext.getExecutePath(),
                    taskExecutionContext.getResources());
            if (!fileDownloads.isEmpty()) {
                downloadResource(taskExecutionContext.getExecutePath(), logger, fileDownloads);
            }

            taskExecutionContext.setEnvFile(CommonUtils.getSystemEnvPath());

            taskExecutionContext.setTaskAppId(String.format("%s_%s",
                    taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId()));

            TaskChannel taskChannel = taskPluginManager.getTaskChannelMap().get(taskExecutionContext.getTaskType());
            if (null == taskChannel) {
                throw new ServiceException(String.format("%s Task Plugin Not Found,Please Check Config File.",
                        taskExecutionContext.getTaskType()));
            }
            String taskLogName = LoggerUtils.buildTaskId(taskExecutionContext.getFirstSubmitTime(),
                    taskExecutionContext.getProcessDefineCode(),
                    taskExecutionContext.getProcessDefineVersion(),
                    taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId());
            taskExecutionContext.setTaskLogName(taskLogName);

            // set the name of the current thread
            Thread.currentThread().setName(taskLogName);

            task = taskChannel.createTask(taskExecutionContext);

            // task init
            this.task.init();

            // init varPool
            this.task.getParameters().setVarPool(taskExecutionContext.getVarPool());

            // task handle
            this.task.handle();

            // task result process
            if (this.task.getNeedAlert()) {
                sendAlert(this.task.getTaskAlertInfo(), this.task.getExitStatus());
            }

            taskExecutionContext.setCurrentExecutionStatus(this.task.getExitStatus());
            taskExecutionContext.setEndTime(DateUtils.getCurrentDate());
            taskExecutionContext.setProcessId(this.task.getProcessId());
            taskExecutionContext.setAppIds(this.task.getAppIds());
            taskExecutionContext.setVarPool(JSONUtils.toJsonString(this.task.getParameters().getVarPool()));
            logger.info("task instance id : {},task final status : {}", taskExecutionContext.getTaskInstanceId(),
                    this.task.getExitStatus());
        } catch (Throwable e) {
            logger.error("task scheduler failure", e);
            kill();
            taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.FAILURE);
            taskExecutionContext.setEndTime(DateUtils.getCurrentDate());
            taskExecutionContext.setProcessId(this.task.getProcessId());
            taskExecutionContext.setAppIds(this.task.getAppIds());
        } finally {
            TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
            workerMessageSender.sendMessageWithRetry(taskExecutionContext,
                    masterAddress,
                    CommandType.TASK_EXECUTE_RESULT);
            clearTaskExecPath();
            LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }

    private void sendAlert(TaskAlertInfo taskAlertInfo, TaskExecutionStatus status) {
        int strategy =
                status == TaskExecutionStatus.SUCCESS ? WarningType.SUCCESS.getCode() : WarningType.FAILURE.getCode();
        alertClientService.sendAlert(taskAlertInfo.getAlertGroupId(), taskAlertInfo.getTitle(),
                taskAlertInfo.getContent(), strategy);
    }

    /**
     * when task finish, clear execute path.
     */
    private void clearTaskExecPath() {
        logger.info("develop mode is: {}", CommonUtils.isDevelopMode());

        if (!CommonUtils.isDevelopMode()) {
            // get exec dir
            String execLocalPath = taskExecutionContext.getExecutePath();

            if (Strings.isNullOrEmpty(execLocalPath)) {
                logger.warn("task: {} exec local path is empty.", taskExecutionContext.getTaskName());
                return;
            }

            if (SINGLE_SLASH.equals(execLocalPath)) {
                logger.warn("task: {} exec local path is '/', direct deletion is not allowed",
                        taskExecutionContext.getTaskName());
                return;
            }

            try {
                org.apache.commons.io.FileUtils.deleteDirectory(new File(execLocalPath));
                logger.info("exec local path: {} cleared.", execLocalPath);
            } catch (IOException e) {
                if (e instanceof NoSuchFileException) {
                    // this is expected
                } else {
                    logger.error("Delete exec dir failed.", e);
                }
            }
        }
    }

    /**
     * kill task
     */
    public void kill() {
        if (task != null) {
            try {
                task.cancelApplication(true);
                ProcessUtils.killYarnJob(taskExecutionContext);
            } catch (Exception e) {
                logger.error("Kill task failed", e);
            }
        }
    }

    /**
     * download resource file
     *
     * @param execLocalPath execLocalPath
     * @param fileDownloads projectRes
     * @param logger logger
     */
    public void downloadResource(String execLocalPath, Logger logger, List<Pair<String, String>> fileDownloads) {
        for (Pair<String, String> fileDownload : fileDownloads) {
            try {
                // query the tenant code of the resource according to the name of the resource
                String fullName = fileDownload.getLeft();
                String tenantCode = fileDownload.getRight();
                String resPath = storageOperate.getResourceFileName(tenantCode, fullName);
                logger.info("get resource file from path:{}", resPath);
                long resourceDownloadStartTime = System.currentTimeMillis();
                storageOperate.download(tenantCode, resPath, execLocalPath + File.separator + fullName, false, true);
                WorkerServerMetrics
                        .recordWorkerResourceDownloadTime(System.currentTimeMillis() - resourceDownloadStartTime);
                WorkerServerMetrics.recordWorkerResourceDownloadSize(
                        Files.size(Paths.get(execLocalPath, fullName)));
                WorkerServerMetrics.incWorkerResourceDownloadSuccessCount();
            } catch (Exception e) {
                WorkerServerMetrics.incWorkerResourceDownloadFailureCount();
                logger.error(e.getMessage(), e);
                throw new ServiceException(e.getMessage());
            }
        }
    }

    /**
     * download resource check
     *
     * @param execLocalPath
     * @param projectRes
     * @return
     */
    public List<Pair<String, String>> downloadCheck(String execLocalPath, Map<String, String> projectRes) {
        if (MapUtils.isEmpty(projectRes)) {
            return Collections.emptyList();
        }
        List<Pair<String, String>> downloadFile = new ArrayList<>();
        projectRes.forEach((key, value) -> {
            File resFile = new File(execLocalPath, key);
            boolean notExist = !resFile.exists();
            if (notExist) {
                downloadFile.add(Pair.of(key, value));
            } else {
                logger.info("file : {} exists ", resFile.getName());
            }
        });
        if (!downloadFile.isEmpty() && !PropertyUtils.getResUploadStartupState()) {
            throw new StorageOperateNoConfiguredException("Storage service config does not exist!");
        }
        return downloadFile;
    }

    /**
     * get current TaskExecutionContext
     *
     * @return TaskExecutionContext
     */
    public TaskExecutionContext getTaskExecutionContext() {
        return this.taskExecutionContext;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(DateUtils.getRemainTime(taskExecutionContext.getFirstSubmitTime(),
                taskExecutionContext.getDelayTime() * 60L), TimeUnit.SECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o == null) {
            return 1;
        }
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }

    public AbstractTask getTask() {
        return task;
    }
}
