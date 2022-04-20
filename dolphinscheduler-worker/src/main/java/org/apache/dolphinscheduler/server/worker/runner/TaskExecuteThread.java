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

import static org.apache.dolphinscheduler.common.Constants.SINGLE_SLASH;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.storage.StorageOperate;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.server.worker.processor.TaskCallbackService;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private TaskExecutionContext taskExecutionContext;

    public StorageOperate getStorageOperate() {
        return storageOperate;
    }

    public void setStorageOperate(StorageOperate storageOperate) {
        this.storageOperate = storageOperate;
    }

    private StorageOperate storageOperate;

    /**
     * abstract task
     */
    private AbstractTask task;

    /**
     * task callback service
     */
    private TaskCallbackService taskCallbackService;

    /**
     * alert client server
     */
    private AlertClientService alertClientService;

    private TaskPluginManager taskPluginManager;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     * @param taskCallbackService taskCallbackService
     */
    public TaskExecuteThread(TaskExecutionContext taskExecutionContext,
                             TaskCallbackService taskCallbackService,
                             AlertClientService alertClientService) {
        this.taskExecutionContext = taskExecutionContext;
        this.taskCallbackService = taskCallbackService;
        this.alertClientService = alertClientService;
    }

    public TaskExecuteThread(TaskExecutionContext taskExecutionContext,
                             TaskCallbackService taskCallbackService,
                             AlertClientService alertClientService,
                             TaskPluginManager taskPluginManager,
                             StorageOperate storageOperate) {
        this.taskExecutionContext = taskExecutionContext;
        this.taskCallbackService = taskCallbackService;
        this.alertClientService = alertClientService;
        this.taskPluginManager = taskPluginManager;
        this.storageOperate = storageOperate;
    }

    @Override
    public void run() {
        if (Constants.DRY_RUN_FLAG_YES == taskExecutionContext.getDryRun()) {
            taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.SUCCESS);
            taskExecutionContext.setStartTime(new Date());
            taskExecutionContext.setEndTime(new Date());
            TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
            taskCallbackService.sendTaskExecuteResponseCommand(taskExecutionContext);
            return;
        }

        try {
            logger.info("script path : {}", taskExecutionContext.getExecutePath());
            if (taskExecutionContext.getStartTime() == null) {
                taskExecutionContext.setStartTime(new Date());
            }
            logger.info("the task begins to execute. task instance id: {}", taskExecutionContext.getTaskInstanceId());

            // callback task execute running
            taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
            taskCallbackService.sendTaskExecuteRunningCommand(taskExecutionContext);

            // copy hdfs/minio file to local
            downloadResource(taskExecutionContext.getExecutePath(), taskExecutionContext.getResources(), logger);

            taskExecutionContext.setEnvFile(CommonUtils.getSystemEnvPath());
            taskExecutionContext.setDefinedParams(getGlobalParamsMap());

            taskExecutionContext.setTaskAppId(String.format("%s_%s",
                    taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId()));

            preBuildBusinessParams();

            TaskChannel taskChannel = taskPluginManager.getTaskChannelMap().get(taskExecutionContext.getTaskType());
            if (null == taskChannel) {
                throw new ServiceException(String.format("%s Task Plugin Not Found,Please Check Config File.", taskExecutionContext.getTaskType()));
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

            //init varPool
            this.task.getParameters().setVarPool(taskExecutionContext.getVarPool());

            // task handle
            this.task.handle();

            // task result process
            if (this.task.getNeedAlert()) {
                sendAlert(this.task.getTaskAlertInfo(), this.task.getExitStatus().getCode());
            }

            taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.of(this.task.getExitStatus().getCode()));
            taskExecutionContext.setEndTime(DateUtils.getCurrentDate());
            taskExecutionContext.setProcessId(this.task.getProcessId());
            taskExecutionContext.setAppIds(this.task.getAppIds());
            taskExecutionContext.setVarPool(JSONUtils.toJsonString(this.task.getParameters().getVarPool()));
            logger.info("task instance id : {},task final status : {}", taskExecutionContext.getTaskInstanceId(), this.task.getExitStatus());
        } catch (Throwable e) {
            logger.error("task scheduler failure", e);
            kill();
            taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.FAILURE);
            taskExecutionContext.setEndTime(DateUtils.getCurrentDate());
            taskExecutionContext.setProcessId(this.task.getProcessId());
            taskExecutionContext.setAppIds(this.task.getAppIds());
        } finally {
            TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
            taskCallbackService.sendTaskExecuteResponseCommand(taskExecutionContext);
            clearTaskExecPath();
        }
    }

    private void sendAlert(TaskAlertInfo taskAlertInfo, int status) {
        int strategy = status == ExecutionStatus.SUCCESS.getCode() ? WarningType.SUCCESS.getCode() : WarningType.FAILURE.getCode();
        alertClientService.sendAlert(taskAlertInfo.getAlertGroupId(), taskAlertInfo.getTitle(), taskAlertInfo.getContent(), strategy);
    }

    /**
     * when task finish, clear execute path.
     */
    private void clearTaskExecPath() {
        logger.info("develop mode is: {}", CommonUtils.isDevelopMode());

        if (!CommonUtils.isDevelopMode()) {
            // get exec dir
            String execLocalPath = taskExecutionContext.getExecutePath();

            if (StringUtils.isEmpty(execLocalPath)) {
                logger.warn("task: {} exec local path is empty.", taskExecutionContext.getTaskName());
                return;
            }

            if (SINGLE_SLASH.equals(execLocalPath)) {
                logger.warn("task: {} exec local path is '/', direct deletion is not allowed", taskExecutionContext.getTaskName());
                return;
            }

            try {
                org.apache.commons.io.FileUtils.deleteDirectory(new File(execLocalPath));
                logger.info("exec local path: {} cleared.", execLocalPath);
            } catch (IOException e) {
                logger.error("delete exec dir failed : {}", e.getMessage(), e);
            }
        }
    }

    /**
     * get global paras map
     *
     * @return map
     */
    private Map<String, String> getGlobalParamsMap() {
        Map<String, String> globalParamsMap = new HashMap<>(16);

        // global params string
        String globalParamsStr = taskExecutionContext.getGlobalParams();
        if (globalParamsStr != null) {
            List<Property> globalParamsList = JSONUtils.toList(globalParamsStr, Property.class);
            globalParamsMap.putAll(globalParamsList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue)));
        }
        return globalParamsMap;
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
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * download resource file
     *
     * @param execLocalPath execLocalPath
     * @param projectRes projectRes
     * @param logger logger
     */
    private void downloadResource(String execLocalPath, Map<String, String> projectRes, Logger logger) {
        if (MapUtils.isEmpty(projectRes)) {
            return;
        }

        Set<Map.Entry<String, String>> resEntries = projectRes.entrySet();

        for (Map.Entry<String, String> resource : resEntries) {
            String fullName = resource.getKey();
            String tenantCode = resource.getValue();
            File resFile = new File(execLocalPath, fullName);
            if (!resFile.exists()) {
                try {
                    // query the tenant code of the resource according to the name of the resource
                    String resHdfsPath = storageOperate.getResourceFileName(tenantCode, fullName);
                    logger.info("get resource file from hdfs :{}", resHdfsPath);
                    storageOperate.download(tenantCode, resHdfsPath, execLocalPath + File.separator + fullName, false, true);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw new ServiceException(e.getMessage());
                }
            } else {
                logger.info("file : {} exists ", resFile.getName());
            }
        }
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

    private void preBuildBusinessParams() {
        Map<String, Property> paramsMap = new HashMap<>();
        // replace variable TIME with $[YYYYmmddd...] in shell file when history run job and batch complement job
        if (taskExecutionContext.getScheduleTime() != null) {
            Date date = taskExecutionContext.getScheduleTime();
            String dateTime = DateUtils.format(date, Constants.PARAMETER_FORMAT_TIME, null);
            Property p = new Property();
            p.setValue(dateTime);
            p.setProp(Constants.PARAMETER_DATETIME);
            paramsMap.put(Constants.PARAMETER_DATETIME, p);
        }
        taskExecutionContext.setParamsMap(paramsMap);
    }
}
