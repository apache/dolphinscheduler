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

package org.apache.dolphinscheduler.server.master.workflow;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.master.dag.IDAGEngine;
import org.apache.dolphinscheduler.server.master.runner.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecutionContext;

/**
 * The IWorkflowExecuteRunnable represent a running workflow instance, it is responsible for operate the workflow instance. e.g. start, kill, pause, timeout.
 */
public interface IWorkflowExecutionRunnable extends IEventfulExecutionRunnable {

    /**
     * Start the workflow instance.
     */
    void start();

    /**
     * Kill the workflow instance.
     */
    void kill();

    /**
     * Pause the workflow instance.
     */
    void pause();

    /**
     * Get {@link ITaskExecutionRunnable} by task instance id.
     *
     * @param taskInstanceId task instance id
     * @return the task execution runnable
     */
    ITaskExecutionRunnable getTaskExecutionRunnableById(int taskInstanceId);

    /**
     * Get the workflow execution context.
     *
     * @return the workflow execution context
     */
    WorkflowExecutionContext getWorkflowExecutionContext();

    /**
     * Get the {@link IDAGEngine} which used to execute the dag of the workflow instance.
     *
     * @return dag engine.
     */
    IDAGEngine getDagEngine();

    default Integer getWorkflowInstanceId() {
        return getWorkflowExecutionContext().getWorkflowInstance().getId();
    }

    default String getWorkflowInstanceName() {
        return getWorkflowExecutionContext().getWorkflowInstance().getName();
    }

    default ProcessInstance getWorkflowInstance() {
        return getWorkflowExecutionContext().getWorkflowInstance();
    }

}
