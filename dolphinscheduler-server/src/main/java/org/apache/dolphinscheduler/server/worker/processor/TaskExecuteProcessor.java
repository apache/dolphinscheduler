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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.sift.SiftingAppender;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Event;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.Preconditions;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRequestCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.log.TaskLogDiscriminator;
import org.apache.dolphinscheduler.server.worker.cache.ResponceCache;
import org.apache.dolphinscheduler.server.worker.cache.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.server.worker.cache.impl.TaskExecutionContextCacheManagerImpl;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.runner.TaskExecuteThread;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

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
     * worker config
     */
    private final WorkerConfig workerConfig;

    /**
     * task callback service
     */
    private final TaskCallbackService taskCallbackService;

    /**
     * taskExecutionContextCacheManager
     */
    private TaskExecutionContextCacheManager taskExecutionContextCacheManager;

    public TaskExecuteProcessor() {
        this.taskCallbackService = SpringApplicationContext.getBean(TaskCallbackService.class);
        this.workerConfig = SpringApplicationContext.getBean(WorkerConfig.class);
        this.workerExecService = ThreadUtils.newDaemonFixedThreadExecutor("Worker-Execute-Thread", workerConfig.getWorkerExecThreads());
        this.taskExecutionContextCacheManager = SpringApplicationContext.getBean(TaskExecutionContextCacheManagerImpl.class);
    }

    /**
     * Pre-cache task to avoid extreme situations when kill task. There is no such task in the cache
     *
     * @param taskExecutionContext task
     */
    private void setTaskCache(TaskExecutionContext taskExecutionContext) {
        TaskExecutionContext preTaskCache = new TaskExecutionContext();
        preTaskCache.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_EXECUTE_REQUEST == command.getType(),
            String.format("invalid command type : %s", command.getType()));

        TaskExecuteRequestCommand taskRequestCommand = FastJsonSerializer.deserialize(
            command.getBody(), TaskExecuteRequestCommand.class);

        logger.info("received command : {}", taskRequestCommand);

        if (taskRequestCommand == null) {
            logger.error("task execute request command is null");
            return;
        }

        String contextJson = taskRequestCommand.getTaskExecutionContext();
        TaskExecutionContext taskExecutionContext = JSONObject.parseObject(contextJson, TaskExecutionContext.class);

        if (taskExecutionContext == null) {
            logger.error("task execution context is null");
            return;
        }
        setTaskCache(taskExecutionContext);
        // custom logger
        Logger taskLogger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                taskExecutionContext.getProcessDefineId(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId()));

        taskExecutionContext.setHost(OSUtils.getHost() + ":" + workerConfig.getListenPort());
        taskExecutionContext.setStartTime(new Date());
        taskExecutionContext.setLogPath(getTaskLogPath(taskExecutionContext));

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
            taskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        }
        FileUtils.taskLoggerThreadLocal.remove();

        taskCallbackService.addRemoteChannel(taskExecutionContext.getTaskInstanceId(),
                new NettyRemoteChannel(channel, command.getOpaque()));

        this.doAck(taskExecutionContext);

        // submit task
        workerExecService.submit(new TaskExecuteThread(taskExecutionContext, taskCallbackService, taskLogger));
    }

    private void doAck(TaskExecutionContext taskExecutionContext){
        // tell master that task is in executing
        TaskExecuteAckCommand ackCommand = buildAckCommand(taskExecutionContext);
        ResponceCache.get().cache(taskExecutionContext.getTaskInstanceId(),ackCommand.convert2Command(),Event.ACK);
        taskCallbackService.sendAck(taskExecutionContext.getTaskInstanceId(), ackCommand.convert2Command());
    }

    /**
     * get task log path
     * @return log path
     */
    private String getTaskLogPath(TaskExecutionContext taskExecutionContext) {
        String baseLog = ((TaskLogDiscriminator) ((SiftingAppender) ((LoggerContext) LoggerFactory.getILoggerFactory())
                .getLogger("ROOT")
                .getAppender("TASKLOGFILE"))
                .getDiscriminator()).getLogBase();
        if (baseLog.startsWith(Constants.SINGLE_SLASH)){
            return baseLog + Constants.SINGLE_SLASH +
                    taskExecutionContext.getProcessDefineId() + Constants.SINGLE_SLASH  +
                    taskExecutionContext.getProcessInstanceId() + Constants.SINGLE_SLASH  +
                    taskExecutionContext.getTaskInstanceId() + ".log";
        }
        return System.getProperty("user.dir") + Constants.SINGLE_SLASH +
                baseLog +  Constants.SINGLE_SLASH +
                taskExecutionContext.getProcessDefineId() + Constants.SINGLE_SLASH  +
                taskExecutionContext.getProcessInstanceId() + Constants.SINGLE_SLASH  +
                taskExecutionContext.getTaskInstanceId() + ".log";
    }

    /**
     * build ack command
     * @param taskExecutionContext taskExecutionContext
     * @return TaskExecuteAckCommand
     */
    private TaskExecuteAckCommand buildAckCommand(TaskExecutionContext taskExecutionContext) {
        TaskExecuteAckCommand ackCommand = new TaskExecuteAckCommand();
        ackCommand.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        ackCommand.setStatus(ExecutionStatus.RUNNING_EXEUTION.getCode());
        ackCommand.setLogPath(taskExecutionContext.getLogPath() );
        ackCommand.setHost(taskExecutionContext.getHost());
        ackCommand.setStartTime(taskExecutionContext.getStartTime());
        if(taskExecutionContext.getTaskType().equals(TaskType.SQL.name()) || taskExecutionContext.getTaskType().equals(TaskType.PROCEDURE.name())){
            ackCommand.setExecutePath(null);
        }else{
            ackCommand.setExecutePath(taskExecutionContext.getExecutePath());
        }
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
