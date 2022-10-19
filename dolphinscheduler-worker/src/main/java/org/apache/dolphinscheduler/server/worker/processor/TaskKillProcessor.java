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
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskKillRequestCommand;
import org.apache.dolphinscheduler.remote.command.TaskKillResponseCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.remote.utils.Pair;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecuteRunnable;
import org.apache.dolphinscheduler.service.log.LogClient;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.apache.dolphinscheduler.service.utils.ProcessUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.micrometer.core.lang.NonNull;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * task kill processor
 */
@Component
public class TaskKillProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskKillProcessor.class);

    @Autowired
    private WorkerManagerThread workerManager;

    @Autowired
    private MessageRetryRunner messageRetryRunner;

    @Autowired
    private LogClient logClient;

    /**
     * task kill process
     *
     * @param channel channel channel
     * @param command command command
     */
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_KILL_REQUEST == command.getType(),
                String.format("invalid command type : %s", command.getType()));
        TaskKillRequestCommand killCommand = JSONUtils.parseObject(command.getBody(), TaskKillRequestCommand.class);
        if (killCommand == null) {
            logger.error("task kill request command is null");
            return;
        }
        logger.info("task kill command : {}", killCommand);

        int taskInstanceId = killCommand.getTaskInstanceId();
        try {
            LoggerUtils.setTaskInstanceIdMDC(taskInstanceId);
            TaskExecutionContext taskExecutionContext =
                    TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId);
            if (taskExecutionContext == null) {
                logger.error("taskRequest cache is null, taskInstanceId: {}", killCommand.getTaskInstanceId());
                return;
            }

            int processId = taskExecutionContext.getProcessId();
            if (processId == 0) {
                this.cancelApplication(taskInstanceId);
                workerManager.killTaskBeforeExecuteByInstanceId(taskInstanceId);
                taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.KILL);
                TaskExecutionContextCacheManager.removeByTaskInstanceId(taskInstanceId);
                sendTaskKillResponseCommand(channel, taskExecutionContext);
                logger.info("the task has not been executed and has been cancelled, task id:{}", taskInstanceId);
                return;
            }

            // if processId > 0, it should call cancelApplication to cancel remote application too.
            this.cancelApplication(taskInstanceId);
            Pair<Boolean, List<String>> result = doKill(taskExecutionContext);

            taskExecutionContext.setCurrentExecutionStatus(
                    result.getLeft() ? TaskExecutionStatus.SUCCESS : TaskExecutionStatus.FAILURE);
            taskExecutionContext.setAppIds(String.join(TaskConstants.COMMA, result.getRight()));
            sendTaskKillResponseCommand(channel, taskExecutionContext);

            TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
            messageRetryRunner.removeRetryMessages(taskExecutionContext.getTaskInstanceId());

            logger.info("remove REMOTE_CHANNELS, task instance id:{}", killCommand.getTaskInstanceId());
        } finally {
            LoggerUtils.removeTaskInstanceIdMDC();
        }
    }

    private void sendTaskKillResponseCommand(Channel channel, TaskExecutionContext taskExecutionContext) {
        TaskKillResponseCommand taskKillResponseCommand = new TaskKillResponseCommand();
        taskKillResponseCommand.setStatus(taskExecutionContext.getCurrentExecutionStatus());
        if (taskExecutionContext.getAppIds() != null) {
            taskKillResponseCommand
                    .setAppIds(Arrays.asList(taskExecutionContext.getAppIds().split(TaskConstants.COMMA)));
        }
        taskKillResponseCommand.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskKillResponseCommand.setHost(taskExecutionContext.getHost());
        taskKillResponseCommand.setProcessId(taskExecutionContext.getProcessId());
        channel.writeAndFlush(taskKillResponseCommand.convert2Command()).addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    logger.error("Submit kill response to master error, kill command: {}", taskKillResponseCommand);
                }
            }
        });
    }

    /**
     * do kill
     *
     * @return kill result
     */
    private Pair<Boolean, List<String>> doKill(TaskExecutionContext taskExecutionContext) {
        // kill system process
        boolean processFlag = killProcess(taskExecutionContext.getTenantCode(), taskExecutionContext.getProcessId());

        // find log and kill yarn job
        Pair<Boolean, List<String>> yarnResult = killYarnJob(Host.of(taskExecutionContext.getHost()),
                taskExecutionContext.getLogPath(),
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTenantCode());
        return Pair.of(processFlag && yarnResult.getLeft(), yarnResult.getRight());
    }

    /**
     * kill task by cancel application
     * @param taskInstanceId
     */
    protected void cancelApplication(int taskInstanceId) {
        WorkerTaskExecuteRunnable workerTaskExecuteRunnable = workerManager.getTaskExecuteThread(taskInstanceId);
        if (workerTaskExecuteRunnable == null) {
            logger.warn("taskExecuteThread not found, taskInstanceId:{}", taskInstanceId);
            return;
        }
        AbstractTask task = workerTaskExecuteRunnable.getTask();
        if (task == null) {
            logger.warn("task not found, taskInstanceId:{}", taskInstanceId);
            return;
        }
        try {
            task.cancel();
        } catch (Exception e) {
            logger.error("kill task error", e);
        }
        logger.info("kill task by cancelApplication, task id:{}", taskInstanceId);
    }

    /**
     * kill system process
     * @param tenantCode
     * @param processId
     */
    protected boolean killProcess(String tenantCode, Integer processId) {
        boolean processFlag = true;
        if (processId == null || processId.equals(0)) {
            return true;
        }
        try {
            String pidsStr = ProcessUtils.getPidsStr(processId);
            if (!Strings.isNullOrEmpty(pidsStr)) {
                String cmd = String.format("kill -9 %s", pidsStr);
                cmd = OSUtils.getSudoCmd(tenantCode, cmd);
                logger.info("process id:{}, cmd:{}", processId, cmd);
                OSUtils.exeCmd(cmd);
            }
        } catch (Exception e) {
            processFlag = false;
            logger.error("kill task error", e);
        }
        return processFlag;
    }

    /**
     * kill yarn job
     *
     * @param host host
     * @param logPath logPath
     * @param executePath executePath
     * @param tenantCode  tenantCode
     * @return Pair<Boolean, List < String>> yarn kill result
     */
    private Pair<Boolean, List<String>> killYarnJob(@NonNull Host host,
                                                    String logPath,
                                                    String executePath,
                                                    String tenantCode) {
        if (logPath == null || executePath == null || tenantCode == null) {
            logger.error(
                    "Kill yarn job error, the input params is illegal, host: {}, logPath: {}, executePath: {}, tenantCode: {}",
                    host, logPath, executePath, tenantCode);
            return Pair.of(false, Collections.emptyList());
        }
        try {
            logger.info("Get appIds from worker {}:{} taskLogPath: {}", host.getIp(), host.getPort(), logPath);
            List<String> appIds = logClient.getAppIds(host.getIp(), host.getPort(), logPath);
            if (CollectionUtils.isEmpty(appIds)) {
                logger.info("The appId is empty");
                return Pair.of(true, Collections.emptyList());
            }

            ProcessUtils.cancelApplication(appIds, logger, tenantCode, executePath);
            return Pair.of(true, appIds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("kill yarn job error, the current thread has been interrtpted", e);
        } catch (Exception e) {
            logger.error("Kill yarn job error, host: {}, logPath: {}, executePath: {}, tenantCode: {}", host, logPath,
                    executePath, tenantCode, e);
        }
        return Pair.of(false, Collections.emptyList());
    }

}
