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

package org.apache.dolphinscheduler.server.master.engine.task.runnable;

import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.WorkflowExecutionRunnable;

/**
 * The interface represent a running TaskInstance which belongs to a {@link WorkflowExecutionRunnable}.
 */
public interface ITaskExecutionRunnable
        extends
            Comparable<ITaskExecutionRunnable> {

    /**
     * Get the task instance id.
     * <p> Need to know the id might change since the task instance might be regenerated.
     */
    default int getId() {
        return getTaskInstance().getId();
    }

    default String getName() {
        return getTaskDefinition().getName();
    }

    /**
     * Whether the task instance is initialized.
     * <p> If the ITaskExecutionRunnable is never triggered, it is not initialized.
     * <p> If the ITaskExecutionRunnable is created by failover, recovered then it is initialized.
     */
    boolean isTaskInstanceInitialized();

    /**
     * Initialize the task instance with {@link FirstRunTaskInstanceFactory}
     */
    void initializeFirstRunTaskInstance();

    /**
     * Whether the task instance is running.
     */
    boolean isTaskInstanceCanRetry();

    /**
     * Retry the TaskExecutionRunnable.
     * <p> Will create retry task instance and start it.
     */
    void retry();

    /**
     * Failover the TaskExecutionRunnable.
     * <p> The failover logic is judged by the task instance state.
     */
    void failover();

    /**
     * Pause the TaskExecutionRunnable.
     */
    void pause();

    /**
     * Kill the TaskExecutionRunnable.
     */
    void kill();

    WorkflowEventBus getWorkflowEventBus();

    IWorkflowExecutionGraph getWorkflowExecutionGraph();

    WorkflowInstance getWorkflowInstance();

    TaskInstance getTaskInstance();

    TaskDefinition getTaskDefinition();

    TaskExecutionContext getTaskExecutionContext();
}
