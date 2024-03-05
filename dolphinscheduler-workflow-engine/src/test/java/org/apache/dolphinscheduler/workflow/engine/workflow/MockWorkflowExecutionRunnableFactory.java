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

package org.apache.dolphinscheduler.workflow.engine.workflow;

import org.apache.dolphinscheduler.workflow.engine.engine.DAGEngineFactory;
import org.apache.dolphinscheduler.workflow.engine.event.MemoryEventRepository;

import org.apache.commons.lang3.RandomUtils;

public class MockWorkflowExecutionRunnableFactory {

    private static final ITaskExecutionContextFactory taskExecutionContextFactory = new TaskExecutionContextFactory();
    private static final ITaskExecutionRunnableFactory taskExecutionRunnableFactory =
            new TaskExecutionRunnableFactory(taskExecutionContextFactory);
    private static final DAGEngineFactory dagEngineFactory = new DAGEngineFactory(taskExecutionRunnableFactory);
    private static final WorkflowExecutionRunnableFactory workflowExecutionRunnableFactory =
            new WorkflowExecutionRunnableFactory(dagEngineFactory);

    public static WorkflowExecutionRunnable createWorkflowExecutionRunnable() {
        int workflowInstanceId = RandomUtils.nextInt();
        String workflowInstanceName = "MockWorkflowInstance-" + workflowInstanceId;
        WorkflowInstance workflowInstance = WorkflowInstance.of(workflowInstanceId, workflowInstanceName);
        IWorkflowExecutionContext workflowExecutionContext = WorkflowExecutionContext.builder()
                .workflowInstance(workflowInstance)
                .eventRepository(MemoryEventRepository.newInstance())
                .build();

        return workflowExecutionRunnableFactory.createWorkflowExecutionRunnable(workflowExecutionContext);
    }

}
