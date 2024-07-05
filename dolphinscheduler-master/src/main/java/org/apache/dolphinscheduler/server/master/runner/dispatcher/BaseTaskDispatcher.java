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

import org.apache.dolphinscheduler.extract.base.utils.Host;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;
import org.apache.dolphinscheduler.server.master.exception.dispatch.WorkerGroupNotFoundException;
import org.apache.dolphinscheduler.server.master.runner.TaskExecuteRunnable;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseTaskDispatcher implements TaskDispatcher {

    @Override
    public void dispatchTask(TaskExecuteRunnable taskExecuteRunnable) throws TaskDispatchException {
        String taskName = taskExecuteRunnable.getTaskExecutionContext().getTaskName();
        String taskInstanceDispatchAddress;
        try {
            taskInstanceDispatchAddress = getTaskInstanceDispatchHost(taskExecuteRunnable)
                    .map(Host::getAddress)
                    .orElseThrow(() -> new TaskDispatchException("Cannot find the host to execute task: " + taskName));
        } catch (WorkerGroupNotFoundException workerGroupNotFoundException) {
            // todo: this is a temporary solution, we should refactor the ServerNodeManager to make sure there won't
            // throw WorkerGroupNotFoundException unless the worker group is not exist in database
            throw new TaskDispatchException("Dispatch task: " + taskName + " failed", workerGroupNotFoundException);
        }
        taskExecuteRunnable.getTaskExecutionContext().setHost(taskInstanceDispatchAddress);
        // todo: add dispatch address here to avoid set host in TaskExecuteRunnable before
        doDispatch(taskExecuteRunnable);
        taskExecuteRunnable.getTaskInstance().setHost(taskInstanceDispatchAddress);
        log.info("Success dispatch task {} to {}.", taskName, taskInstanceDispatchAddress);
    }

    protected abstract void doDispatch(TaskExecuteRunnable taskExecuteRunnable) throws TaskDispatchException;

    protected abstract Optional<Host> getTaskInstanceDispatchHost(TaskExecuteRunnable taskExecutionContext) throws TaskDispatchException, WorkerGroupNotFoundException;

}
