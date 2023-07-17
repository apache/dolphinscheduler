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

package org.apache.dolphinscheduler.server.worker.processor;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.task.TaskKillRequest;
import org.apache.dolphinscheduler.remote.command.task.TaskKillResponse;
import org.apache.dolphinscheduler.remote.processor.WorkerRpcProcessor;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecuteRunnable;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * task kill processor
 */
@Slf4j
@Component
public class WorkerTaskKillProcessor implements WorkerRpcProcessor {

    @Autowired
    private WorkerManagerThread workerManager;

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    /**
     * task kill process
     *
     * @param channel channel channel
     * @param message command command
     */
    @Override
    public void process(Channel channel, Message message) {
        TaskKillRequest killCommand = JSONUtils.parseObject(message.getBody(), TaskKillRequest.class);
        if (killCommand == null) {
            log.error("task kill request command is null");
            return;
        }
        log.info("task kill command : {}", killCommand);

        int taskInstanceId = killCommand.getTaskInstanceId();
        try (LogUtils.MDCAutoClosableContext mdcAutoClosableContext = LogUtils.setTaskInstanceIdMDC(taskInstanceId)) {
            TaskExecutionContext taskExecutionContext =
                    TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId);
            if (taskExecutionContext == null) {
                log.error("taskRequest cache is null, taskInstanceId: {}", killCommand.getTaskInstanceId());
                return;
            }

            boolean result = doKill(taskExecutionContext);
            this.cancelApplication(taskInstanceId);

            int processId = taskExecutionContext.getProcessId();
            if (processId == 0) {
                workerManager.killTaskBeforeExecuteByInstanceId(taskInstanceId);
                taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.KILL);
                TaskExecutionContextCacheManager.removeByTaskInstanceId(taskInstanceId);
                sendTaskKillResponseCommand(channel, message.getOpaque(), taskExecutionContext);
                log.info("the task has not been executed and has been cancelled, task id:{}", taskInstanceId);
                return;
            }

            taskExecutionContext.setCurrentExecutionStatus(
                    result ? TaskExecutionStatus.SUCCESS : TaskExecutionStatus.FAILURE);
            sendTaskKillResponseCommand(channel, message.getOpaque(), taskExecutionContext);

            TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
            messageRetryRunner.removeRetryMessages(taskExecutionContext.getTaskInstanceId());

            log.info("remove REMOTE_CHANNELS, task instance id:{}", killCommand.getTaskInstanceId());
        }
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_KILL_REQUEST;
    }

    private void sendTaskKillResponseCommand(Channel channel, long opaque, TaskExecutionContext taskExecutionContext) {
        TaskKillResponse taskKillResponse = new TaskKillResponse();
        taskKillResponse.setStatus(taskExecutionContext.getCurrentExecutionStatus());
        if (taskExecutionContext.getAppIds() != null) {
            taskKillResponse
                    .setAppIds(Arrays.asList(taskExecutionContext.getAppIds().split(TaskConstants.COMMA)));
        }
        taskKillResponse.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskKillResponse.setHost(taskExecutionContext.getHost());
        taskKillResponse.setProcessId(taskExecutionContext.getProcessId());
        channel.writeAndFlush(taskKillResponse.convert2Command(opaque)).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                log.error("Submit kill response to master error, kill command: {}", taskKillResponse);
            }
        });
    }

    /**
     * do kill
     *
     * @return kill result
     */
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

    /**
     * kill task by cancel application
     * @param taskInstanceId
     */
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

    /**
     * kill system process
     * @param tenantCode
     * @param processId
     */
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
