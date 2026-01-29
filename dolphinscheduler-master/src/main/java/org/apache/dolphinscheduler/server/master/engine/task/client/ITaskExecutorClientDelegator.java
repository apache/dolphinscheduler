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

package org.apache.dolphinscheduler.server.master.engine.task.client;

import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;

/**
 * The interface of task executor client delegator. It is used to send operation to task executor server.
 *
 * @see LogicTaskExecutorClientDelegator
 * @see PhysicalTaskExecutorClientDelegator
 */
public interface ITaskExecutorClientDelegator {

    /**
     * Dispatch the task to task executor.
     *
     * @throws TaskDispatchException If dispatch failed
     */
    void dispatch(final ITaskExecutionRunnable taskExecutionRunnable) throws TaskDispatchException;

    /**
     * Take over the task from task executor.
     */
    boolean reassignMasterHost(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Pause the task, this method doesn't guarantee the task is paused success.
     */
    void pause(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Kill the task, this method doesn't guarantee the task is killed success.
     */
    void kill(final ITaskExecutionRunnable taskExecutionRunnable);

    /**
     * Ack the task executor lifecycle event.
     */
    void ackTaskExecutorLifecycleEvent(
                                       final ITaskExecutionRunnable taskExecutionRunnable,
                                       final ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck taskExecutorLifecycleEventAck);
}
