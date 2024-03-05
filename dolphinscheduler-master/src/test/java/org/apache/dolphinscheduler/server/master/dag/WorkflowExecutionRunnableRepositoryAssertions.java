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

package org.apache.dolphinscheduler.server.master.dag;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorkflowExecutionRunnableRepositoryAssertions {

    private final WorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;

    private WorkflowExecutionRunnableRepositoryAssertions(WorkflowExecuteRunnableRepository workflowExecuteRunnableRepository) {
        this.workflowExecuteRunnableRepository = workflowExecuteRunnableRepository;
    }

    public static WorkflowExecutionRunnableRepositoryAssertions workflowExecutionRunnableRepository(WorkflowExecuteRunnableRepository workflowExecuteRunnableRepository) {
        return new WorkflowExecutionRunnableRepositoryAssertions(workflowExecuteRunnableRepository);
    }

    public void existWorkflowExecutionRunnable(Integer workflowInstanceId) {
        assertNotNull(workflowExecuteRunnableRepository.getWorkflowExecutionRunnableById(workflowInstanceId));
    }

    public void notExistWorkflowExecutionRunnable(Integer workflowInstanceId) {
        assertNull(workflowExecuteRunnableRepository.getWorkflowExecutionRunnableById(workflowInstanceId));
    }

}
