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

package org.apache.dolphinscheduler.server.master.runner.dispatcher;

import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.base.utils.Host;
import org.apache.dolphinscheduler.extract.master.ILogicTaskInstanceOperator;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskDispatchRequest;
import org.apache.dolphinscheduler.extract.master.transportor.LogicTaskDispatchResponse;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.exception.TaskDispatchException;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;
import org.apache.dolphinscheduler.server.master.runner.BaseTaskDispatcher;
import org.apache.dolphinscheduler.server.master.runner.execute.TaskExecuteRunnable;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterTaskDispatcher extends BaseTaskDispatcher {

    private final Optional<Host> masterTaskExecuteHost;

    public MasterTaskDispatcher(TaskEventService taskEventService,
                                MasterConfig masterConfig) {
        super(taskEventService, masterConfig);
        masterTaskExecuteHost = Optional.of(Host.of(masterConfig.getMasterAddress()));
    }

    @Override
    protected void doDispatch(TaskExecuteRunnable taskExecuteRunnable) throws TaskDispatchException {
        TaskExecutionContext taskExecutionContext = taskExecuteRunnable.getTaskExecutionContext();
        try {
            ILogicTaskInstanceOperator taskInstanceOperator = SingletonJdkDynamicRpcClientProxyFactory.getInstance()
                    .getProxyClient(taskExecutionContext.getHost(), ILogicTaskInstanceOperator.class);
            LogicTaskDispatchResponse logicTaskDispatchResponse = taskInstanceOperator
                    .dispatchLogicTask(new LogicTaskDispatchRequest(taskExecuteRunnable.getTaskExecutionContext()));
            if (!logicTaskDispatchResponse.isDispatchSuccess()) {
                throw new TaskDispatchException(String.format("Dispatch LogicTask to %s failed, response is: %s",
                        taskExecutionContext.getHost(), logicTaskDispatchResponse));
            }
        } catch (TaskDispatchException e) {
            throw e;
        } catch (Exception e) {
            throw new TaskDispatchException(String.format("Dispatch task to %s failed",
                    taskExecutionContext.getHost()), e);
        }
    }

    @Override
    protected Optional<Host> getTaskInstanceDispatchHost(TaskExecuteRunnable taskExecutionContext) {
        return masterTaskExecuteHost;
    }
}
