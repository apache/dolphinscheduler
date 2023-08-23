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

package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskDispatchRequest;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskDispatchResponse;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.runner.MasterDelayTaskExecuteRunnableDelayQueue;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterDelayTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecuteRunnableFactoryBuilder;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecutionContextHolder;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogicITaskInstanceDispatchOperationFunction
        implements
            ITaskInstanceOperationFunction<LogicTaskDispatchRequest, LogicTaskDispatchResponse> {

    @Autowired
    private MasterTaskExecuteRunnableFactoryBuilder masterTaskExecuteRunnableFactoryBuilder;

    @Autowired
    private MasterDelayTaskExecuteRunnableDelayQueue masterDelayTaskExecuteRunnableDelayQueue;

    @Override
    public LogicTaskDispatchResponse operate(LogicTaskDispatchRequest taskDispatchRequest) {
        log.info("Received dispatchLogicTask request: {}", taskDispatchRequest);
        TaskExecutionContext taskExecutionContext = taskDispatchRequest.getTaskExecutionContext();
        try {
            final int taskInstanceId = taskExecutionContext.getTaskInstanceId();
            final int workflowInstanceId = taskExecutionContext.getProcessInstanceId();
            final String taskInstanceName = taskExecutionContext.getTaskName();

            LogUtils.setWorkflowAndTaskInstanceIDMDC(workflowInstanceId, taskInstanceId);
            LogUtils.setTaskInstanceLogFullPathMDC(taskExecutionContext.getLogPath());

            MasterTaskExecutionContextHolder.putTaskExecutionContext(taskExecutionContext);
            // todo: calculate the delay in master dispatcher then we don't need to use a queue to store the task
            final long remainTime =
                    DateUtils.getRemainTime(DateUtils.timeStampToDate(taskExecutionContext.getFirstSubmitTime()),
                            TimeUnit.SECONDS.toMillis(taskExecutionContext.getDelayTime()));
            if (remainTime > 0) {
                log.info("Current taskInstance: {} is choosing delay execution, delay time: {}/ms, remainTime: {}/ms",
                        taskExecutionContext.getTaskName(),
                        TimeUnit.SECONDS.toMillis(taskExecutionContext.getDelayTime()), remainTime);
                taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.DELAY_EXECUTION);
                // todo: send delay execution message
                return LogicTaskDispatchResponse.success(taskExecutionContext.getTaskInstanceId());
            }

            final MasterDelayTaskExecuteRunnable masterDelayTaskExecuteRunnable =
                    masterTaskExecuteRunnableFactoryBuilder
                            .createWorkerDelayTaskExecuteRunnableFactory(taskExecutionContext.getTaskType())
                            .createWorkerTaskExecuteRunnable(taskExecutionContext);
            if (masterDelayTaskExecuteRunnableDelayQueue
                    .submitMasterDelayTaskExecuteRunnable(masterDelayTaskExecuteRunnable)) {
                log.info("Submit LogicTask: {} to MasterDelayTaskExecuteRunnableDelayQueue success", taskInstanceName);
                return LogicTaskDispatchResponse.success(taskInstanceId);
            } else {
                log.error(
                        "Submit LogicTask: {} to MasterDelayTaskExecuteRunnableDelayQueue failed, current task waiting queue size: {} is full",
                        taskInstanceName, masterDelayTaskExecuteRunnableDelayQueue.size());
                return LogicTaskDispatchResponse.failed(taskInstanceId,
                        "MasterDelayTaskExecuteRunnableDelayQueue is full");
            }
        } finally {
            LogUtils.removeWorkflowAndTaskInstanceIdMDC();
            LogUtils.removeTaskInstanceLogFullPathMDC();
        }
    }
}
