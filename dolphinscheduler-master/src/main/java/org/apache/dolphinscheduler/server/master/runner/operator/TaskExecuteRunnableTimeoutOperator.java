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
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.worker.ITaskInstanceOperator;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceKillRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceKillResponse;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskExecuteRunnableTimeoutOperator extends BaseTaskExecuteRunnableTimeoutOperator {

    public TaskExecuteRunnableTimeoutOperator(TaskInstanceDao taskInstanceDao) {
        super(taskInstanceDao);
    }

    @Override
    protected void killRemoteTaskInstanceInThreadPool(TaskInstance taskInstance) {
        if (StringUtils.isEmpty(taskInstance.getHost())) {
            log.info("TaskInstance {} host is empty, no need to killRemoteTask", taskInstance.getName());
            return;
        }

        final ITaskInstanceOperator iTaskInstanceOperator = SingletonJdkDynamicRpcClientProxyFactory.getInstance()
                .getProxyClient(taskInstance.getHost(), ITaskInstanceOperator.class);

        final TaskInstanceKillRequest taskInstanceKillRequest = new TaskInstanceKillRequest(taskInstance.getId());
        final TaskInstanceKillResponse taskInstanceKillResponse =
                iTaskInstanceOperator.killTask(taskInstanceKillRequest);
        log.info("Timeout kill TaskInstance {} in host {} with response {}", taskInstance.getName(),
                taskInstance.getHost(), taskInstanceKillResponse);
    }
}
