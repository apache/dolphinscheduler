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

import org.apache.dolphinscheduler.common.enums.Event;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRequestCommand;
import org.apache.dolphinscheduler.remote.command.TaskRecallCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRemoteChannel;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.worker.cache.ResponceCache;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.plugin.TaskPluginManager;
import org.apache.dolphinscheduler.server.worker.runner.TaskExecuteThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.queue.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.task.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * worker request processor
 */
public class TaskExecuteProcessor implements NettyRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteProcessor.class);

    /**
     * worker config
     */
    private final WorkerConfig workerConfig;

    /**
     * task callback service
     */
    private final TaskCallbackService taskCallbackService;

    /**
     * alert client service
     */
    private AlertClientService alertClientService;

    private TaskPluginManager taskPluginManager;

    /*
     * task execute manager
     */
    private final WorkerManagerThread workerManager;

    public TaskExecuteProcessor() {
        this.taskCallbackService = SpringApplicationContext.getBean(TaskCallbackService.class);
        this.workerConfig = SpringApplicationContext.getBean(WorkerConfig.class);
        this.workerManager = SpringApplicationContext.getBean(WorkerManagerThread.class);
    }

    /**
     * Pre-cache task to avoid extreme situations when kill task. There is no such task in the cache
     *
     * @param taskExecutionContext task
     */
    private void setTaskCache(TaskExecutionContext taskExecutionContext) {
        TaskExecutionContext preTaskCache = new TaskExecutionContext();
        preTaskCache.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        TaskRequest taskRequest = JSONUtils.parseObject(JSONUtils.toJsonString(taskExecutionContext), TaskRequest.class);
        TaskExecutionContextCacheManager.cacheTaskExecutionContext(taskRequest);
    }

    public TaskExecuteProcessor(AlertClientService alertClientService, TaskPluginManager taskPluginManager) {
        this();
        this.alertClientService = alertClientService;
        this.taskPluginManager = taskPluginManager;
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_EXECUTE_REQUEST == command.getType(),
                String.format("invalid command type : %s", command.getType()));
        TaskExecuteRequestCommand taskRequestCommand = JSONUtils.parseObject(
                command.getBody(), TaskExecuteRequestCommand.class);

        logger.info("received command : {}", taskRequestCommand);

        if (taskRequestCommand == null) {
            logger.error("task execute request command is null");
            return;
        }

        String contextJson = taskRequestCommand.getTaskExecutionContext();
        TaskExecutionContext taskExecutionContext = JSONUtils.parseObject(contextJson, TaskExecutionContext.class);

        if (taskExecutionContext == null) {
            logger.error("task execution context is null");
            return;
        }

        setTaskCache(taskExecutionContext);
        taskExecutionContext.setHost(NetUtils.getAddr(workerConfig.getListenPort()));
        if (CommonUtils.isSudoEnable() && workerConfig.getWorkerTenantAutoCreate()) {
            OSUtils.createUserIfAbsent(taskExecutionContext.getTenantCode());
        }

        ResponceCache.get().removeRecallCache(taskExecutionContext.getTaskInstanceId());
        taskCallbackService.addRemoteChannel(taskExecutionContext.getTaskInstanceId(), new NettyRemoteChannel(channel, command.getOpaque()));
        // submit task to manager
        boolean offer = workerManager.offer(new TaskExecuteThread(taskExecutionContext, taskCallbackService, alertClientService, taskPluginManager));
        if (!offer) {
            logger.warn("submit task to wait queue error, queue is full, queue size is {}, taskInstanceId: {}",
                workerManager.getWaitSubmitQueueSize(), taskExecutionContext.getTaskInstanceId());
            sendRecallCommand(taskExecutionContext, channel);
            TaskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        }
    }

    private void sendRecallCommand(TaskExecutionContext taskExecutionContext, Channel channel) {
        TaskRecallCommand taskRecallCommand = buildRecallCommand(taskExecutionContext);
        Command command = taskRecallCommand.convert2Command();
        ResponceCache.get().cache(taskExecutionContext.getTaskInstanceId(), command, Event.WORKER_REJECT);
        taskCallbackService.changeRemoteChannel(taskExecutionContext.getTaskInstanceId(), new NettyRemoteChannel(channel, command.getOpaque()));
        taskCallbackService.sendResult(taskExecutionContext.getTaskInstanceId(), command);
        logger.info("send recall command successfully, taskId:{}, opaque:{}", taskExecutionContext.getTaskInstanceId(), command.getOpaque());
    }

    private TaskRecallCommand buildRecallCommand(TaskExecutionContext taskExecutionContext) {
        TaskRecallCommand taskRecallCommand = new TaskRecallCommand();
        taskRecallCommand.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskRecallCommand.setProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        taskRecallCommand.setHost(taskExecutionContext.getHost());
        taskRecallCommand.setEvent(Event.WORKER_REJECT);
        taskRecallCommand.setStatus(ExecutionStatus.SUBMITTED_SUCCESS.getCode());
        return taskRecallCommand;
    }
}
