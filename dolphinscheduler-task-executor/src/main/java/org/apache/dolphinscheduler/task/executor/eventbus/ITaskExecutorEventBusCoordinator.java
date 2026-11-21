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

package org.apache.dolphinscheduler.task.executor.eventbus;

import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.listener.ITaskExecutorLifecycleEventListener;

/**
 * The coordinator responsible to fire the {@link  TaskExecutorEventBus}.
 */
public interface ITaskExecutorEventBusCoordinator extends AutoCloseable {

    /**
     * Start the coordinator.
     */
    void start();

    /**
     * Register the task executor event listener
     */
    void registerTaskExecutorLifecycleEventListener(final ITaskExecutorLifecycleEventListener taskExecutorLifecycleEventListener);

    /**
     * Fire the kill event of the task executor and wait until the listeners have killed it.
     *
     * <p>The queued events are fired by a worker thread, which cannot report the result of the kill to
     * the caller: the kill is fired on the calling thread instead, and an exception of a listener is
     * thrown to the caller, so that a caller which has to stop a task knows that the task is not
     * stopped. The event is not queued, the state of the task executor is reported by the listeners as
     * it is for a queued event.
     *
     * @param taskExecutor the task executor to kill
     * @throws RuntimeException when the task could not be killed
     */
    void fireTaskExecutorKillEvent(final ITaskExecutor taskExecutor);

    /**
     * Shutdown the coordinator
     */
    @Override
    void close();

}
