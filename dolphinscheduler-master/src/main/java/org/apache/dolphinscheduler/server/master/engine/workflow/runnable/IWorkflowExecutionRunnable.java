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

package org.apache.dolphinscheduler.server.master.engine.workflow.runnable;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.workflow.listener.IWorkflowLifecycleListener;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;

import java.util.List;

public interface IWorkflowExecutionRunnable {

    /**
     * Get the id of the WorkflowExecutionRunnable.
     */
    int getId();

    /**
     * Get the name of the WorkflowExecutionRunnable.
     */
    String getName();

    IWorkflowExecuteContext getWorkflowExecuteContext();

    /**
     * Get the WorkflowInstance belongs to the WorkflowExecutionRunnable.
     */
    ProcessInstance getWorkflowInstance();

    /**
     * Get the state of the WorkflowExecutionRunnable.
     */
    WorkflowExecutionStatus getState();

    /**
     * Get the WorkflowEventBus belongs to the Workflow instance.
     */
    WorkflowEventBus getWorkflowEventBus();

    /**
     * Get the WorkflowExecutionGraph belongs to the Workflow instance.
     */
    IWorkflowExecutionGraph getWorkflowExecutionGraph();

    /**
     * Get the WorkflowInstanceLifecycleListeners belongs to the Workflow instance.
     */
    List<IWorkflowLifecycleListener> getWorkflowLifecycleListeners();

    /**
     * Whether the workflow is ready to pause.
     */
    boolean isWorkflowReadyPause();

    boolean isWorkflowReadyStop();

    void registerWorkflowInstanceLifecycleListener(IWorkflowLifecycleListener listener);

}
