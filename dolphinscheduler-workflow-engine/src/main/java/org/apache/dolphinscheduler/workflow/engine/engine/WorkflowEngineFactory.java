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

package org.apache.dolphinscheduler.workflow.engine.engine;

import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionRunnableRepository;
import org.apache.dolphinscheduler.workflow.engine.workflow.SingletonWorkflowExecutionRunnableRepository;

public class WorkflowEngineFactory implements IWorkflowEngineFactory {

    private static final IWorkflowExecutionRunnableRepository DEFAULT_WORKFLOW_EXECUTION_RUNNABLE_FACTORY =
            SingletonWorkflowExecutionRunnableRepository.getInstance();

    private IWorkflowExecutionRunnableRepository workflowExecuteRunnableRepository =
            DEFAULT_WORKFLOW_EXECUTION_RUNNABLE_FACTORY;

    private IEventEngine eventEngine;

    private WorkflowEngineFactory() {
    }

    public static WorkflowEngineFactory newWorkflowEngineFactory() {
        return new WorkflowEngineFactory();
    }

    public WorkflowEngineFactory withWorkflowExecuteRunnableRepository(IWorkflowExecutionRunnableRepository workflowExecuteRunnableRepository) {
        this.workflowExecuteRunnableRepository = workflowExecuteRunnableRepository;
        return this;
    }

    public WorkflowEngineFactory withEventEngine(IEventEngine eventEngine) {
        this.eventEngine = eventEngine;
        return this;
    }

    @Override
    public IWorkflowEngine createWorkflowEngine() {
        return new WorkflowEngine(workflowExecuteRunnableRepository, eventEngine);
    }
}
