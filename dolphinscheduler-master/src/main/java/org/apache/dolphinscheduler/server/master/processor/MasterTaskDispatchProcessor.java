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

package org.apache.dolphinscheduler.server.master.processor;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageType;
import org.apache.dolphinscheduler.remote.command.task.TaskDispatchRequest;
import org.apache.dolphinscheduler.remote.command.task.TaskDispatchResponse;
import org.apache.dolphinscheduler.remote.processor.MasterRpcProcessor;
import org.apache.dolphinscheduler.server.master.runner.MasterDelayTaskExecuteRunnableDelayQueue;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterDelayTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecuteRunnableFactoryBuilder;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecutionContextHolder;
import org.apache.dolphinscheduler.server.master.runner.message.MasterMessageSenderManager;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

@Slf4j
@Component
public class MasterTaskDispatchProcessor implements MasterRpcProcessor {

    @Autowired
    private MasterTaskExecuteRunnableFactoryBuilder masterTaskExecuteRunnableFactoryBuilder;

    @Autowired
    private MasterMessageSenderManager masterMessageSenderManager;

    @Autowired
    private MasterDelayTaskExecuteRunnableDelayQueue masterDelayTaskExecuteRunnableDelayQueue;

    @Override
    public void process(Channel channel, Message message) {
        TaskDispatchRequest taskDispatchRequest = JSONUtils.parseObject(message.getBody(), TaskDispatchRequest.class);
        log.info("Receive task dispatch request, command: {}", taskDispatchRequest);
        TaskExecutionContext taskExecutionContext = taskDispatchRequest.getTaskExecutionContext();
        taskExecutionContext.setLogPath(LogUtils.getTaskInstanceLogFullPath(taskExecutionContext));
        try {
            // Since we need to make sure remove MDC key after cache, so we use finally to remove MDC key
            LogUtils.setWorkflowAndTaskInstanceIDMDC(taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId());
            LogUtils.setTaskInstanceLogFullPathMDC(taskExecutionContext.getLogPath());
            MasterTaskExecutionContextHolder.putTaskExecutionContext(taskExecutionContext);
            // todo: calculate the delay in master dispatcher then we don't need to use a queue to store the task
            long remainTime =
                    DateUtils.getRemainTime(DateUtils.timeStampToDate(taskExecutionContext.getFirstSubmitTime()),
                            TimeUnit.SECONDS.toMillis(taskExecutionContext.getDelayTime()));

            // todo: move this to the master delay queue
            if (remainTime > 0) {
                log.info("Current taskInstance: {} is choose delay execution, delay time: {}ms, remainTime: {}ms",
                        taskExecutionContext.getTaskName(),
                        TimeUnit.SECONDS.toMillis(taskExecutionContext.getDelayTime()), remainTime);
                taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.DELAY_EXECUTION);
                masterMessageSenderManager.getMasterTaskExecuteResultMessageSender()
                        .sendMessage(taskExecutionContext);
            }

            MasterDelayTaskExecuteRunnable masterDelayTaskExecuteRunnable = masterTaskExecuteRunnableFactoryBuilder
                    .createWorkerDelayTaskExecuteRunnableFactory(taskExecutionContext.getTaskType())
                    .createWorkerTaskExecuteRunnable(taskExecutionContext);
            if (masterDelayTaskExecuteRunnableDelayQueue
                    .submitMasterDelayTaskExecuteRunnable(masterDelayTaskExecuteRunnable)) {
                log.info(
                        "Submit task: {} to MasterDelayTaskExecuteRunnableDelayQueue success",
                        taskExecutionContext.getTaskName());
                sendDispatchSuccessResult(channel, message, taskExecutionContext);
            } else {
                log.error(
                        "Submit task: {} to MasterDelayTaskExecuteRunnableDelayQueue failed, current task waiting queue size: {} is full",
                        taskExecutionContext.getTaskName(), masterDelayTaskExecuteRunnableDelayQueue.size());
                sendDispatchRejectResult(channel, message, taskExecutionContext);
            }
        } catch (Exception ex) {
            log.error("Handle task dispatch request error, command: {}", taskDispatchRequest, ex);
            MasterTaskExecutionContextHolder.removeTaskExecutionContext(taskExecutionContext.getTaskInstanceId());
            sendDispatchFailedResult(channel, message, taskExecutionContext, ex);
        } finally {
            LogUtils.removeWorkflowAndTaskInstanceIdMDC();
            LogUtils.removeTaskInstanceLogFullPathMDC();
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

    private void sendDispatchFailedResult(Channel channel, Message dispatchRequest,
                                          TaskExecutionContext taskExecutionContext, Throwable throwable) {
        TaskDispatchResponse taskDispatchResponse =
                TaskDispatchResponse.failed(taskExecutionContext.getTaskInstanceId(), throwable.getMessage());
        channel.writeAndFlush(taskDispatchResponse.convert2Command(dispatchRequest.getOpaque()));
    }

    @Override
    public MessageType getCommandType() {
        return MessageType.TASK_DISPATCH_REQUEST;
    }
}
