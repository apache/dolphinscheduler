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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorEventBus;

public interface ITaskExecutor extends ITaskExecutorStateTracker {

    /**
     * Get the id of the task executor.
     * <p> Each task executor has a unique id.
     */
    default Integer getId() {
        return getTaskExecutionContext().getTaskInstanceId();
    }

    /**
     * Get the name of the task executor.
     */
    default String getName() {
        return getTaskExecutionContext().getTaskName();
    }

    /**
     * Get the type of the task executor.
     */
    default String getTaskType() {
        return getTaskExecutionContext().getTaskType();
    }

    /**
     * Get the TaskExecutionContext of the task executor.
     */
    TaskExecutionContext getTaskExecutionContext();

    /**
     * Whether the task executor is started.
     */
    boolean isStarted();

    /**
     * Start the task executor.
     */
    void start();

    /**
     * Pause the task executor.
     */
    void pause();

    /**
     * Kill the task executor.
     */
    void kill();

    /**
     * Get the EventBus belongs to the task executor.
     */
    TaskExecutorEventBus getTaskExecutorEventBus();

}
