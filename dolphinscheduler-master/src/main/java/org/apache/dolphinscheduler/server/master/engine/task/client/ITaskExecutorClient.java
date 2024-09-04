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

import org.apache.dolphinscheduler.server.master.engine.exceptions.TaskKillException;
import org.apache.dolphinscheduler.server.master.engine.exceptions.TaskPauseException;
import org.apache.dolphinscheduler.server.master.engine.exceptions.TaskReassignMasterHostException;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;
import org.apache.dolphinscheduler.task.executor.TaskEngine;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;

/**
 * The client used to communicate with {@link TaskEngine}.
 */
public interface ITaskExecutorClient {

    /**
     * Dispatch the task to task executor.
     *
     * @throws TaskDispatchException If dispatch failed or error occurs.
     */
    void dispatch(final ITaskExecutionRunnable taskExecutionRunnable) throws TaskDispatchException;

    /**
     * Reassign the workflow instance host from task executor.
     *
     * @throws TaskReassignMasterHostException If an error occurs.
     */
    boolean reassignWorkflowInstanceHost(final ITaskExecutionRunnable taskExecutionRunnable) throws TaskReassignMasterHostException;

    /**
     * Pause task from task executor.
     * <p> This method is not a sync method, it will return immediately after send a kill request to remote executor and receive a response.
     * but not guarantee the task will be paused.
     * <p> Not all task can support pause operation, if the task doesn't support pause, then it will just ignore the pause request, then you need to wait it finished.
     *
     * @throws TaskPauseException If an error occurs.
     */
    void pause(final ITaskExecutionRunnable taskExecutionRunnable) throws TaskPauseException;

    /**
     * Kill task from task executor.
     * <p> This method is not a sync method, it will return immediately after send a kill request to remote executor.
     * but not guarantee the task will be killed.
     * <p> All task should support kill operation, but some kill operation might cost long time, so this is unstable.
     *
     * @throws TaskKillException If an error occurs.
     */
    void kill(final ITaskExecutionRunnable taskExecutionRunnable) throws TaskKillException;

    /**
     * Send TaskExecutorLifecycleEventAck to TaskEngine.
     * <p> This method will not throw exception, once send ack failed, the executor engine will retry.
     */
    void ackTaskExecutorLifecycleEvent(
                                       final ITaskExecutionRunnable taskExecutionRunnable,
                                       final ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck taskExecutorLifecycleEventAck);

}
