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

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskDispatchCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.metrics.TaskMetrics;
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

/**
 * Used to handle {@link CommandType#TASK_DISPATCH_REQUEST}
 */
@Component
@Slf4j
public class TaskDispatchProcessor implements NettyRequestProcessor {

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

    @Counted(value = "ds.task.execution.count", description = "task execute total count")
    @Timed(value = "ds.task.execution.duration", percentiles = {0.5, 0.75, 0.95, 0.99}, histogram = true)
    @Override
    public void process(Channel channel, Command command) {
        checkArgument(CommandType.TASK_DISPATCH_REQUEST == command.getType(),
                String.format("invalid command type : %s", command.getType()));

        TaskDispatchCommand taskDispatchCommand = JSONUtils.parseObject(command.getBody(), TaskDispatchCommand.class);

        if (taskDispatchCommand == null) {
            log.error("task execute request command content is null");
            return;
        }
        final String workflowMasterAddress = taskDispatchCommand.getMessageSenderAddress();
        log.info("Receive task dispatch request, command: {}", taskDispatchCommand);

        TaskExecutionContext taskExecutionContext = taskDispatchCommand.getTaskExecutionContext();

        if (taskExecutionContext == null) {
            log.error("task execution context is null");
            return;
        }
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
                workerMessageSender.sendMessage(taskExecutionContext, CommandType.TASK_EXECUTE_RESULT);
            }

            WorkerDelayTaskExecuteRunnable workerTaskExecuteRunnable = WorkerTaskExecuteRunnableFactoryBuilder
                    .createWorkerDelayTaskExecuteRunnableFactory(
                            taskExecutionContext,
                            workerConfig,
                            workerMessageSender,
                            workerRpcClient,
                            taskPluginManager,
                            storageOperate)
                    .createWorkerTaskExecuteRunnable();
            // submit task to manager
            boolean offer = workerManager.offer(workerTaskExecuteRunnable);
            if (!offer) {
                log.warn(
                        "submit task to wait queue error, queue is full, current queue size is {}, will send a task reject message to master",
                        workerManager.getWaitSubmitQueueSize());
                workerMessageSender.sendMessageWithRetry(taskExecutionContext, CommandType.TASK_REJECT);
            } else {
                log.info("Submit task to wait queue success, current queue size is {}",
                        workerManager.getWaitSubmitQueueSize());
            }
        }
    }

}
