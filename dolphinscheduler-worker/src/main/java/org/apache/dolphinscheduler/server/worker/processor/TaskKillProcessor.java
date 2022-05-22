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
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskKillRequestCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRemoteChannel;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.remote.utils.Pair;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.server.worker.runner.TaskExecuteThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.service.log.LogClientService;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * task kill processor
 */
@Component
public class TaskKillProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskKillProcessor.class);

    /**
     * task callback service
     */
    @Autowired
    private TaskCallbackService taskCallbackService;

    /**
     * task execute manager
     */
    @Autowired
    private WorkerManagerThread workerManager;

    /**
     * task kill process
     *
     * @param channel channel channel
     * @param command command command
     */
    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_KILL_REQUEST == command.getType(), String.format("invalid command type : %s", command.getType()));
        TaskKillRequestCommand killCommand = JSONUtils.parseObject(command.getBody(), TaskKillRequestCommand.class);
        if (killCommand == null) {
            logger.error("task kill request command is null");
            return;
        }
        logger.info("task kill command : {}", killCommand);

        int taskInstanceId = killCommand.getTaskInstanceId();
        TaskExecutionContext taskExecutionContext = TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId);
        if (taskExecutionContext == null) {
            logger.error("taskRequest cache is null, taskInstanceId: {}", killCommand.getTaskInstanceId());
            return;
        }

        int processId = taskExecutionContext.getProcessId();
        if (processId == 0) {
            this.cancelApplication(taskInstanceId);
            workerManager.killTaskBeforeExecuteByInstanceId(taskInstanceId);
            TaskExecutionContextCacheManager.removeByTaskInstanceId(taskInstanceId);
            logger.info("the task has not been executed and has been cancelled, task id:{}", taskInstanceId);
            return;
        }

        Pair<Boolean, List<String>> result = doKill(taskExecutionContext);

        taskCallbackService.addRemoteChannel(killCommand.getTaskInstanceId(),
                new NettyRemoteChannel(channel, command.getOpaque()));

        taskExecutionContext.setCurrentExecutionStatus(result.getLeft() ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE);
        taskExecutionContext.setAppIds(String.join(TaskConstants.COMMA, result.getRight()));

        taskCallbackService.sendTaskKillResponseCommand(taskExecutionContext);
        TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());

        logger.info("remove REMOTE_CHANNELS, task instance id:{}", killCommand.getTaskInstanceId());
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
        TaskExecuteThread taskExecuteThread = workerManager.getTaskExecuteThread(taskInstanceId);
        if (taskExecuteThread == null) {
            logger.warn("taskExecuteThread not found, taskInstanceId:{}", taskInstanceId);
            return;
        }
        AbstractTask task = taskExecuteThread.getTask();
        if (task == null) {
            logger.warn("task not found, taskInstanceId:{}", taskInstanceId);
            return;
        }
        try {
            task.cancelApplication(true);
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
            if (!StringUtils.isEmpty(pidsStr)) {
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
     * @param tenantCode tenantCode
     * @return Pair<Boolean, List < String>> yarn kill result
     */
    private Pair<Boolean, List<String>> killYarnJob(Host host, String logPath, String executePath, String tenantCode) {
        try (LogClientService logClient = new LogClientService();) {
            logger.info("log host : {} , logPath : {} , port : {}", host.getIp(), logPath,
                    host.getPort());
            String log = logClient.viewLog(host.getIp(), host.getPort(), logPath);
            List<String> appIds = Collections.emptyList();
            if (!StringUtils.isEmpty(log)) {
                appIds = LoggerUtils.getAppIds(log, logger);
                if (StringUtils.isEmpty(executePath)) {
                    logger.error("task instance execute path is empty");
                    throw new RuntimeException("task instance execute path is empty");
                }
                if (appIds.size() > 0) {
                    ProcessUtils.cancelApplication(appIds, logger, tenantCode, executePath);
                }
            }
            return Pair.of(true, appIds);
        } catch (Exception e) {
            logger.error("kill yarn job error", e);
        }
        return Pair.of(false, Collections.emptyList());
    }

}
