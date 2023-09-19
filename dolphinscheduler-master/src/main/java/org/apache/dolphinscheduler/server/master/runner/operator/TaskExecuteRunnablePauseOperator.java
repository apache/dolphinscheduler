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

package org.apache.dolphinscheduler.server.master.runner.operator;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.worker.ITaskInstanceOperator;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstancePauseRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstancePauseResponse;
import org.apache.dolphinscheduler.server.master.runner.execute.DefaultTaskExecuteRunnable;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskExecuteRunnablePauseOperator implements TaskExecuteRunnableOperator {

    @Override
    public void operate(DefaultTaskExecuteRunnable taskExecuteRunnable) {
        try {
            pauseRemoteTaskInstanceInThreadPool(taskExecuteRunnable.getTaskInstance());
        } catch (Exception e) {
            log.error("Pause DefaultTaskExecuteRunnable failed", e);
        }
    }

    private void pauseRemoteTaskInstanceInThreadPool(TaskInstance taskInstance) {
        if (StringUtils.isEmpty(taskInstance.getHost())) {
            log.info("The TaskInstance: {} host is null, no need to pauseRemoteTaskInstance", taskInstance.getName());
            return;
        }
        final ITaskInstanceOperator taskInstanceOperator = SingletonJdkDynamicRpcClientProxyFactory.getInstance()
                .getProxyClient(taskInstance.getHost(), ITaskInstanceOperator.class);
        final TaskInstancePauseRequest taskInstancePauseRequest = new TaskInstancePauseRequest(taskInstance.getId());
        final TaskInstancePauseResponse taskInstancePauseResponse =
                taskInstanceOperator.pauseTask(taskInstancePauseRequest);
        log.info("Pause TaskInstance: {} on host: {} with response: {}", taskInstance.getName(), taskInstance.getHost(),
                taskInstancePauseResponse);
    }
}
