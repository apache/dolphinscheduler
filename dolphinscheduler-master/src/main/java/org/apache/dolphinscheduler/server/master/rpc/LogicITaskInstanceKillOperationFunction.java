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

import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskKillRequest;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskKillResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.MasterDelayTaskExecuteRunnableDelayQueue;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecuteRunnableHolder;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecutionContextHolder;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogicITaskInstanceKillOperationFunction
        implements
            ITaskInstanceOperationFunction<LogicTaskKillRequest, LogicTaskKillResponse> {

    @Autowired
    private MasterDelayTaskExecuteRunnableDelayQueue masterDelayTaskExecuteRunnableDelayQueue;

    @Override
    public LogicTaskKillResponse operate(LogicTaskKillRequest taskKillRequest) {
        final int taskInstanceId = taskKillRequest.getTaskInstanceId();
        try {
            LogUtils.setTaskInstanceIdMDC(taskKillRequest.getTaskInstanceId());
            log.info("Received killLogicTask request: {}", taskKillRequest);
            final MasterTaskExecuteRunnable masterTaskExecuteRunnable =
                    MasterTaskExecuteRunnableHolder.getMasterTaskExecuteRunnable(taskInstanceId);
            if (masterTaskExecuteRunnable == null) {
                log.error("Cannot find the MasterTaskExecuteRunnable, this task may already been killed");
                return LogicTaskKillResponse.fail("Cannot find the MasterTaskExecuteRunnable");
            }
            try {
                masterTaskExecuteRunnable.cancelTask();
                masterDelayTaskExecuteRunnableDelayQueue
                        .removeMasterDelayTaskExecuteRunnable(masterTaskExecuteRunnable);
                return LogicTaskKillResponse.success();
            } catch (MasterTaskExecuteException e) {
                log.error("Cancel MasterTaskExecuteRunnable failed ", e);
                return LogicTaskKillResponse.fail("Cancel MasterTaskExecuteRunnable failed: " + e.getMessage());
            } finally {
                // todo: If cancel failed, we cannot remove the context?
                MasterTaskExecutionContextHolder.removeTaskExecutionContext(taskInstanceId);
                MasterTaskExecuteRunnableHolder.removeMasterTaskExecuteRunnable(taskInstanceId);
            }
        } finally {
            LogUtils.removeTaskInstanceIdMDC();
        }
    }
}
