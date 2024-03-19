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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

/**
 * The ITaskExecutionRunnable represent a running task instance, it is responsible for operate the task instance. e.g. dispatch, kill, pause, timeout.
 */
public interface ITaskExecutionRunnable {

    /**
     * Dispatch the task instance.
     */
    void dispatch();

    /**
     * Kill the task instance.
     */
    void kill();

    /**
     * Pause the task instance.
     */
    void pause();

    /**
     * Timeout the task instance.
     */
    void timeout();

    /**
     * Get the task execution runnable context.
     *
     * @return the task execution runnable context
     */
    TaskExecutionRunnableContext getTaskExecutionRunnableContext();

    /**
     * Get the {@link TaskExecutionStatus} of the TaskExecutionRunnable.
     *
     * @return the task execution status
     */
    default TaskExecutionStatus getTaskExecutionStatus() {
        return getTaskExecutionRunnableContext().getTaskInstance().getState();
    }

    /**
     * Get the task instance id of the TaskExecutionRunnable.
     *
     * @return the task instance id
     */
    default int getTaskInstanceId() {
        return getTaskExecutionRunnableContext().getTaskInstance().getId();
    }

}
