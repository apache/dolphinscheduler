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

package org.apache.dolphinscheduler.server.master.runner.execute;

import static ch.qos.logback.classic.ClassicConstants.FINALIZE_SESSION_MARKER;

import org.apache.dolphinscheduler.common.log.remote.RemoteLogUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.log.TaskInstanceLogHeader;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;
import org.apache.dolphinscheduler.server.master.exception.LogicTaskFactoryNotFoundException;
import org.apache.dolphinscheduler.server.master.exception.LogicTaskInitializeException;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.message.MasterMessageSenderManager;
import org.apache.dolphinscheduler.server.master.runner.task.ILogicTask;
import org.apache.dolphinscheduler.server.master.runner.task.LogicTaskPluginFactoryBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MasterTaskExecuteRunnable implements Runnable {

    protected final TaskExecutionContext taskExecutionContext;
    protected final LogicTaskPluginFactoryBuilder logicTaskPluginFactoryBuilder;
    protected final MasterMessageSenderManager masterMessageSenderManager;
    protected ILogicTask logicTask;

    public MasterTaskExecuteRunnable(TaskExecutionContext taskExecutionContext,
                                     LogicTaskPluginFactoryBuilder logicTaskPluginFactoryBuilder,
                                     MasterMessageSenderManager masterMessageSenderManager) {
        this.taskExecutionContext = taskExecutionContext;
        this.logicTaskPluginFactoryBuilder = logicTaskPluginFactoryBuilder;
        this.masterMessageSenderManager = masterMessageSenderManager;
    }

    protected abstract void executeTask() throws MasterTaskExecuteException;

    protected abstract void afterExecute() throws MasterTaskExecuteException;

    protected void afterThrowing(Throwable throwable) {
        try {
            cancelTask();
            log.error("Get a exception when execute the task, canceled the task", throwable);
        } catch (Exception e) {
            log.error("Cancel task failed,", e);
        }
        taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.FAILURE);
        sendTaskResult();
        log.info(
                "Get a exception when execute the task, sent the task execute result to master, the current task execute result is {}",
                taskExecutionContext.getCurrentExecutionStatus());
        MasterTaskExecutionContextHolder.removeTaskExecutionContext(taskExecutionContext.getTaskInstanceId());
        MasterTaskExecuteRunnableHolder.removeMasterTaskExecuteRunnable(taskExecutionContext.getTaskInstanceId());
        log.info("Get a exception when execute the task, removed the TaskExecutionContext");
    }

    public void cancelTask() throws MasterTaskExecuteException {
        if (logicTask != null) {
            logicTask.kill();
        }
    }

    public void pauseTask() throws MasterTaskExecuteException {
        if (logicTask != null) {
            logicTask.pause();
        }
    }

    public TaskExecutionContext getTaskExecutionContext() {
        return taskExecutionContext;
    }

    public ILogicTask getILogicTask() {
        return logicTask;
    }

    @Override
    public void run() {
        try {
            LogUtils.setWorkflowAndTaskInstanceIDMDC(taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId());
            LogUtils.setTaskInstanceLogFullPathMDC(taskExecutionContext.getLogPath());

            TaskInstanceLogHeader.printInitializeTaskContextHeader();
            initializeTask();

            TaskInstanceLogHeader.printLoadTaskInstancePluginHeader();
            beforeExecute();

            TaskInstanceLogHeader.printExecuteTaskHeader();
            executeTask();

            afterExecute();
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

    protected void beforeExecute() throws LogicTaskFactoryNotFoundException, LogicTaskInitializeException, RemotingException {
        taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.RUNNING_EXECUTION);
        masterMessageSenderManager.getMasterTaskExecuteRunningMessageSender().sendMessage(taskExecutionContext);
        log.info("Send task status {} to master {}", taskExecutionContext.getCurrentExecutionStatus().name(),
                taskExecutionContext.getWorkflowInstanceHost());

        logicTask = logicTaskPluginFactoryBuilder.createILogicTaskPluginFactory(taskExecutionContext.getTaskType())
                .createLogicTask(taskExecutionContext);
        log.info("Initialized task plugin instance: {} successfully", taskExecutionContext.getTaskType());

        logicTask.getTaskParameters().setVarPool(taskExecutionContext.getVarPool());
        log.info("Initialize taskVarPool: {} successfully", taskExecutionContext.getVarPool());

    }

    protected void closeLogAppender() {
        try {
            if (RemoteLogUtils.isRemoteLoggingEnable()) {
                RemoteLogUtils.sendRemoteLog(taskExecutionContext.getLogPath());
                log.info("Send task log {} to remote storage successfully", taskExecutionContext.getLogPath());
            }
        } catch (Exception ex) {
            log.error("Send task log {} to remote storage failed", taskExecutionContext.getLogPath(), ex);
        } finally {
            log.info(FINALIZE_SESSION_MARKER, FINALIZE_SESSION_MARKER.toString());
        }
    }

    protected void sendTaskResult() {
        try {
            taskExecutionContext.setEndTime(System.currentTimeMillis());
            taskExecutionContext.setVarPool(JSONUtils.toJsonString(logicTask.getTaskParameters().getVarPool()));
            masterMessageSenderManager.getMasterTaskExecuteResultMessageSender().sendMessage(taskExecutionContext);
            log.info("Send task status: {} to master: {} successfully",
                    taskExecutionContext.getCurrentExecutionStatus().name(),
                    taskExecutionContext.getWorkflowInstanceHost());
        } catch (Exception ex) {
            log.error("Send task status: {} to master: {} failed",
                    taskExecutionContext.getCurrentExecutionStatus().name(),
                    taskExecutionContext.getWorkflowInstanceHost(), ex);
        }
    }

}
