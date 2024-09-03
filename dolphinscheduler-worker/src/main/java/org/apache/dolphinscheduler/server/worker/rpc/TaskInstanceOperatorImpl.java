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

package org.apache.dolphinscheduler.server.worker.rpc;

import org.apache.dolphinscheduler.extract.worker.ITaskInstanceOperator;
import org.apache.dolphinscheduler.extract.worker.transportor.TakeOverTaskRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TakeOverTaskResponse;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceDispatchRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceDispatchResponse;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceKillRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceKillResponse;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstancePauseRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstancePauseResponse;
import org.apache.dolphinscheduler.server.worker.runner.operator.TaskInstanceOperationFunctionManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskInstanceOperatorImpl implements ITaskInstanceOperator {

    @Autowired
    private TaskInstanceOperationFunctionManager taskInstanceOperationFunctionManager;

    @Override
    public TaskInstanceDispatchResponse dispatchTask(final TaskInstanceDispatchRequest taskInstanceDispatchRequest) {
        return taskInstanceOperationFunctionManager.getTaskInstanceDispatchOperationFunction()
                .operate(taskInstanceDispatchRequest);
    }

    @Override
    public TaskInstanceKillResponse killTask(final TaskInstanceKillRequest taskInstanceKillRequest) {
        return taskInstanceOperationFunctionManager.getTaskInstanceKillOperationFunction()
                .operate(taskInstanceKillRequest);
    }

    @Override
    public TaskInstancePauseResponse pauseTask(final TaskInstancePauseRequest taskPauseRequest) {
        return taskInstanceOperationFunctionManager.getTaskInstancePauseOperationFunction()
                .operate(taskPauseRequest);
    }

    @Override
    public TakeOverTaskResponse takeOverTask(final TakeOverTaskRequest takeOverTaskRequest) {
        return taskInstanceOperationFunctionManager.getUpdateWorkflowHostOperationFunction()
                .operate(takeOverTaskRequest);
    }
}
