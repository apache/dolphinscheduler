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
import org.apache.dolphinscheduler.extract.master.ILogicTaskInstanceOperator;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskPauseRequest;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskPauseResponse;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogicTaskExecuteRunnablePauseOperator extends BaseTaskExecuteRunnablePauseOperator {

    @Override
    protected void pauseRemoteTaskInstanceInThreadPool(TaskInstance taskInstance) {
        if (StringUtils.isEmpty(taskInstance.getHost())) {
            log.info("The LogicTaskInstance: {}'s host is null, no need to pauseRemoteTaskInstance",
                    taskInstance.getName());
            return;
        }
        final ILogicTaskInstanceOperator taskInstanceOperator = SingletonJdkDynamicRpcClientProxyFactory
                .getProxyClient(taskInstance.getHost(), ILogicTaskInstanceOperator.class);
        final LogicTaskPauseRequest logicTaskPauseRequest = new LogicTaskPauseRequest(taskInstance.getId());
        final LogicTaskPauseResponse logicTaskPauseResponse =
                taskInstanceOperator.pauseLogicTask(logicTaskPauseRequest);
        log.info("Pause LogicTaskInstance: {} on host: {} with response: {}", taskInstance.getName(),
                taskInstance.getHost(), logicTaskPauseResponse);
    }

}
