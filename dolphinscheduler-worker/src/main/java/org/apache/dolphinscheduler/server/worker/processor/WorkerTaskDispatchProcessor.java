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

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.task.TaskDispatchRequest;
import org.apache.dolphinscheduler.remote.command.task.TaskDispatchResponse;
import org.apache.dolphinscheduler.remote.processor.WorkerRpcProcessor;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClient;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcClient;
import org.apache.dolphinscheduler.server.worker.runner.WorkerDelayTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecuteRunnableFactoryBuilder;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.netty.channel.Channel;

@Component
@Slf4j
public class WorkerTaskDispatchProcessor implements WorkerRpcProcessor {

    @Autowired
    private WorkerConfig workerConfig;

    @Autowired
    private WorkerMessageSender workerMessageSender;

    @Autowired
    private WorkerRpcClient workerRpcClient;

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Autowired
    private WorkerManagerThread workerManager;

    @Autowired(required = false)
    private StorageOperate storageOperate;

    @Autowired
    private WorkerRegistryClient workerRegistryClient;

    @Counted(value = "ds.task.execution.count", description = "task execute total count")
    @Timed(value = "ds.task.execution.duration", percentiles = {0.5, 0.75, 0.95, 0.99}, histogram = true)
    @Override
    public void process(Channel channel, Message message) {
        TaskDispatchRequest taskDispatchRequest = JSONUtils.parseObject(message.getBody(), TaskDispatchRequest.class);
        log.info("Receive TaskDispatchMessage, command: {}", taskDispatchRequest);
        TaskExecutionContext taskExecutionContext = taskDispatchRequest.getTaskExecutionContext();
        try (
                final LogUtils.MDCAutoClosableContext mdcAutoClosableContext = LogUtils.setWorkflowAndTaskInstanceIDMDC(
                        taskExecutionContext.getProcessInstanceId(), taskExecutionContext.getTaskInstanceId())) {
            TaskMetrics.incrTaskTypeExecuteCount(taskExecutionContext.getTaskType());
            // set cache, it will be used when kill task
            TaskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);
            taskExecutionContext.setHost(workerConfig.getWorkerAddress());
            taskExecutionContext.setLogPath(LogUtils.getTaskInstanceLogFullPath(taskExecutionContext));

            // delay task process
            long remainTime =
                    DateUtils.getRemainTime(DateUtils.timeStampToDate(taskExecutionContext.getFirstSubmitTime()),
                            taskExecutionContext.getDelayTime() * 60L);
            if (remainTime > 0) {
                log.info("Current taskInstance is choose delay execution, delay time: {}s", remainTime);
                taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.DELAY_EXECUTION);
                workerMessageSender.sendMessage(taskExecutionContext, MessageType.TASK_EXECUTE_RESULT_MESSAGE);
            }

            WorkerDelayTaskExecuteRunnable workerTaskExecuteRunnable = WorkerTaskExecuteRunnableFactoryBuilder
                    .createWorkerDelayTaskExecuteRunnableFactory(
                            taskExecutionContext,
                            workerConfig,
                            workerMessageSender,
                            workerRpcClient,
                            taskPluginManager,
                            storageOperate,
                            workerRegistryClient)
                    .createWorkerTaskExecuteRunnable();
            if (!workerManager.offer(workerTaskExecuteRunnable)) {
                log.error("submit task: {} to wait queue error, current queue size: {} is full",
                        taskExecutionContext.getTaskName(), workerManager.getWaitSubmitQueueSize());
                sendDispatchRejectResult(channel, message, taskExecutionContext);
            } else {
                sendDispatchSuccessResult(channel, message, taskExecutionContext);
                log.info("Submit task: {} to wait queue success", taskExecutionContext.getTaskName());
            }
        }
    }

    private void sendDispatchSuccessResult(Channel channel, Message dispatchRequest,
                                           TaskExecutionContext taskExecutionContext) {
        TaskDispatchResponse taskDispatchResponse =
                TaskDispatchResponse.success(taskExecutionContext.getTaskInstanceId());
        channel.writeAndFlush(taskDispatchResponse.convert2Command(dispatchRequest.getOpaque()));
    }

    private void sendDispatchRejectResult(Channel channel, Message dispatchRequest,
                                          TaskExecutionContext taskExecutionContext) {
        TaskDispatchResponse taskDispatchResponse =
                TaskDispatchResponse.failed(taskExecutionContext.getTaskInstanceId(), "Task dispatch queue is full");
        channel.writeAndFlush(taskDispatchResponse.convert2Command(dispatchRequest.getOpaque()));
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_DISPATCH_REQUEST;
    }

}
