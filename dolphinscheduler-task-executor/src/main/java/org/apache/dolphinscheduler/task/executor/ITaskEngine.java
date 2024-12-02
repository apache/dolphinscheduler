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

package org.apache.dolphinscheduler.task.executor;

import org.apache.dolphinscheduler.task.executor.exceptions.TaskExecutorNotFoundException;
import org.apache.dolphinscheduler.task.executor.exceptions.TaskExecutorRuntimeException;

/**
 * The TaskEngine interface used to responsible for task runtime.
 */
public interface ITaskEngine extends AutoCloseable {

    /**
     * Start the task engine.
     */
    void start();

    /**
     * Submit the task to the task engine.
     *
     * @param taskExecutor the task executor to be submitted
     * @throws TaskExecutorRuntimeException if submit failed.
     */
    void submitTask(final ITaskExecutor taskExecutor) throws TaskExecutorRuntimeException;

    /**
     * Pause the task by the task id.
     * <p> This method will not block, only send the pause signal to the task executor
     *
     * @param taskExecutorId the id of the task executor will be paused
     * @throws TaskExecutorNotFoundException If cannot find the task executor.
     */
    void pauseTask(final int taskExecutorId) throws TaskExecutorNotFoundException;

    /**
     * Kill the task by the task id.
     * <p> This method will not block, only send the kill signal to the task executor.
     *
     * @param taskExecutorId the id of the task executor will be killed.
     * @throws TaskExecutorNotFoundException If cannot find the task executor.
     */
    void killTask(final int taskExecutorId) throws TaskExecutorNotFoundException;

    /**
     * Shutdown the task engine.
     */
    @Override
    void close();

}
