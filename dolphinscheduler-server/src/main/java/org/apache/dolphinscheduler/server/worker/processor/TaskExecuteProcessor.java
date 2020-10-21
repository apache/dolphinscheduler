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

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.Preconditions;
import org.apache.dolphinscheduler.common.utils.RetryerUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRequestCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.JsonSerializer;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.runner.TaskExecuteThread;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rholder.retry.RetryException;

import io.netty.channel.Channel;

/**
 *  worker request processor
 */
public class TaskExecuteProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskExecuteProcessor.class);

    /**
     *  thread executor service
     */
    private final ExecutorService workerExecService;

    /**
     *  worker config
     */
    private final WorkerConfig workerConfig;

    /**
     *  task callback service
     */
    private final TaskCallbackService taskCallbackService;

    public TaskExecuteProcessor(){
        this.taskCallbackService = SpringApplicationContext.getBean(TaskCallbackService.class);
        this.workerConfig = SpringApplicationContext.getBean(WorkerConfig.class);
        this.workerExecService = ThreadUtils.newDaemonFixedThreadExecutor("Worker-Execute-Thread", workerConfig.getWorkerExecThreads());
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_EXECUTE_REQUEST == command.getType(),
                String.format("invalid command type : %s", command.getType()));

        TaskExecuteRequestCommand taskRequestCommand = JsonSerializer.deserialize(
                command.getBody(), TaskExecuteRequestCommand.class);

        logger.info("received command : {}", taskRequestCommand);

        if(taskRequestCommand == null){
            logger.error("task execute request command is null");
            return;
        }

        String contextJson = taskRequestCommand.getTaskExecutionContext();
        TaskExecutionContext taskExecutionContext = JSONUtils.parseObject(contextJson, TaskExecutionContext.class);

        if(taskExecutionContext == null){
            logger.error("task execution context is null");
            return;
        }

        taskExecutionContext.setHost(NetUtils.getHost() + ":" + workerConfig.getListenPort());

        // custom logger
        Logger taskLogger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                taskExecutionContext.getProcessDefineId(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId()));

        // local execute path
        String execLocalPath = getExecLocalPath(taskExecutionContext);
        logger.info("task instance  local execute path : {} ", execLocalPath);

        FileUtils.taskLoggerThreadLocal.set(taskLogger);
        try {
            FileUtils.createWorkDirAndUserIfAbsent(execLocalPath, taskExecutionContext.getTenantCode());
        } catch (Throwable ex) {
            String errorLog = String.format("create execLocalPath : %s", execLocalPath);
            LoggerUtils.logError(Optional.ofNullable(logger), errorLog, ex);
            LoggerUtils.logError(Optional.ofNullable(taskLogger), errorLog, ex);
        }
        FileUtils.taskLoggerThreadLocal.remove();

        taskCallbackService.addRemoteChannel(taskExecutionContext.getTaskInstanceId(),
                new NettyRemoteChannel(channel, command.getOpaque()));

        if (DateUtils.getRemainTime(taskExecutionContext.getFirstSubmitTime(), taskExecutionContext.getDelayTime() * 60L) > 0) {
            taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.DELAY_EXECUTION);
            taskExecutionContext.setStartTime(null);
        } else {
            taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
            taskExecutionContext.setStartTime(new Date());
        }

        // tell master the status of this task (RUNNING_EXECUTION or DELAY_EXECUTION)
        final Command ackCommand = buildAckCommand(taskExecutionContext).convert2Command();

        try {
            RetryerUtils.retryCall(() -> {
                taskCallbackService.sendAck(taskExecutionContext.getTaskInstanceId(),ackCommand);
                return Boolean.TRUE;
            });
            // submit task
            workerExecService.submit(new TaskExecuteThread(taskExecutionContext, taskCallbackService, taskLogger));
        } catch (ExecutionException | RetryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * build ack command
     * @param taskExecutionContext taskExecutionContext
     * @return TaskExecuteAckCommand
     */
    private TaskExecuteAckCommand buildAckCommand(TaskExecutionContext taskExecutionContext) {
        TaskExecuteAckCommand ackCommand = new TaskExecuteAckCommand();
        ackCommand.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        ackCommand.setStatus(taskExecutionContext.getCurrentExecutionStatus().getCode());
        ackCommand.setLogPath(LogUtils.getTaskLogPath(taskExecutionContext));
        ackCommand.setHost(taskExecutionContext.getHost());
        ackCommand.setStartTime(taskExecutionContext.getStartTime());
        if(taskExecutionContext.getTaskType().equals(TaskType.SQL.name()) || taskExecutionContext.getTaskType().equals(TaskType.PROCEDURE.name())){
            ackCommand.setExecutePath(null);
        }else{
            ackCommand.setExecutePath(taskExecutionContext.getExecutePath());
        }
        taskExecutionContext.setLogPath(ackCommand.getLogPath());
        return ackCommand;
    }

    /**
     * get execute local path
     * @param taskExecutionContext taskExecutionContext
     * @return execute local path
     */
    private String getExecLocalPath(TaskExecutionContext taskExecutionContext){
        return FileUtils.getProcessExecDir(taskExecutionContext.getProjectId(),
                taskExecutionContext.getProcessDefineId(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId());
    }
}