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

package org.apache.dolphinscheduler.task.executor.container;

import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.exceptions.TaskExecutorRuntimeException;

/**
 * The task executor container used to execute the submitted ILogicTaskExecutor.
 */
public interface ITaskExecutorContainer {

    /**
     * Dispatch the task executor to the container.
     * <p> If dispatch failed, throw TaskExecutorRuntimeException.
     */
    void dispatch(final ITaskExecutor taskExecutor) throws TaskExecutorRuntimeException;

    /**
     * Start the task executor in the container.
     */
    void start(final ITaskExecutor taskExecutor);

    /**
     * Pause the task executor by the task instance id.
     */
    void pause(final ITaskExecutor taskExecutor);

    /**
     * Kill the task executor by the task instance id.
     */
    void kill(final ITaskExecutor taskExecutor);

    /**
     * Finalize the task executor.
     */
    void finalize(final ITaskExecutor taskExecutor);

    /**
     * Get the task executor slot usage which can represent the load of the container.
     * <p> If the value is 1.0, the container is full.
     */
    double slotUsage();

}
