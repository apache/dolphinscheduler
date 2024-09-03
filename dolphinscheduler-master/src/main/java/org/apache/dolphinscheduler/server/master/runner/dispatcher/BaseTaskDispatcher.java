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
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;
import org.apache.dolphinscheduler.server.master.exception.dispatch.WorkerGroupNotFoundException;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseTaskDispatcher implements TaskDispatcher {

    @Override
    public void dispatchTask(ITaskExecutionRunnable taskExecutionRunnable) throws TaskDispatchException {
        final TaskExecutionContext taskExecutionContext = taskExecutionRunnable.getTaskExecutionContext();
        final String taskName = taskExecutionRunnable.getTaskExecutionContext().getTaskName();
        final String taskInstanceDispatchAddress;
        try {
            taskInstanceDispatchAddress = getTaskInstanceDispatchHost(taskExecutionRunnable)
                    .map(Host::getAddress)
                    .orElseThrow(() -> new TaskDispatchException("Cannot find the host to execute task: " + taskName));
        } catch (WorkerGroupNotFoundException workerGroupNotFoundException) {
            // todo: this is a temporary solution, we should refactor the ServerNodeManager to make sure there won't
            // throw WorkerGroupNotFoundException unless the worker group is not exist in database
            throw new TaskDispatchException("Dispatch task: " + taskName + " failed", workerGroupNotFoundException);
        }
        // We inject the host here to avoid when we dispatched the task to worker, but the worker is crash.
        // Then we can use the host to do worker failover.
        taskExecutionContext.setHost(taskInstanceDispatchAddress);
        taskExecutionRunnable.getTaskInstance().setHost(taskInstanceDispatchAddress);
        doDispatch(taskExecutionRunnable);
        // todo: update the task state and host here, otherwise when the master failover the task host is null
        // but it already dispatched to worker
        // Or when the worker receive the task, it should wait the master send a start event to it.
        // the second solution is better
        log.info("Success dispatch task {} to {}.", taskName, taskInstanceDispatchAddress);
    }

    protected abstract void doDispatch(ITaskExecutionRunnable ITaskExecutionRunnable) throws TaskDispatchException;

    protected abstract Optional<Host> getTaskInstanceDispatchHost(ITaskExecutionRunnable taskExecutionContext) throws TaskDispatchException, WorkerGroupNotFoundException;

}
