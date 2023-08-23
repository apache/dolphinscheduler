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

import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskPauseRequest;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskPauseResponse;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecuteRunnableHolder;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogicITaskInstancePauseOperationFunction
        implements
            ITaskInstanceOperationFunction<LogicTaskPauseRequest, LogicTaskPauseResponse> {

    @Override
    public LogicTaskPauseResponse operate(LogicTaskPauseRequest taskPauseRequest) {
        try {
            LogUtils.setTaskInstanceIdMDC(taskPauseRequest.getTaskInstanceId());
            final MasterTaskExecuteRunnable masterTaskExecuteRunnable =
                    MasterTaskExecuteRunnableHolder.getMasterTaskExecuteRunnable(taskPauseRequest.getTaskInstanceId());
            if (masterTaskExecuteRunnable == null) {
                log.info("Cannot find the MasterTaskExecuteRunnable");
                return LogicTaskPauseResponse.fail("Cannot find the MasterTaskExecuteRunnable");
            }
            final TaskExecutionContext taskExecutionContext = masterTaskExecuteRunnable.getTaskExecutionContext();
            LogUtils.setWorkflowAndTaskInstanceIDMDC(taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId());
            masterTaskExecuteRunnable.pauseTask();
            return LogicTaskPauseResponse.success();
        } catch (MasterTaskExecuteException e) {
            log.error("Pause MasterTaskExecuteRunnable failed", e);
            return LogicTaskPauseResponse.fail("Pause MasterTaskExecuteRunnable failed: " + e.getMessage());
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
            LogUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }

}
