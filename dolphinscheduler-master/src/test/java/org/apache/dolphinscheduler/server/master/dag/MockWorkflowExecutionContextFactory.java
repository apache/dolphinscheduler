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

import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.server.master.events.MemoryEventRepository;

import java.util.Collections;

class MockWorkflowExecutionContextFactory {

    public static WorkflowExecutionContext createWorkflowExecutionContext() {
        long workflowDefinitionCode = CodeGenerateUtils.getInstance().genCode();
        int workflowDefinitionVersion = 1;

        int workflowInstanceId = 1;
        String workflowInstanceName = "MockWorkflow" + CodeGenerateUtils.getInstance().genCode();

        ProcessDefinition processDefinition = ProcessDefinition.builder()
                .id(1)
                .name("TestWorkflow")
                .code(workflowDefinitionCode)
                .version(workflowDefinitionVersion)
                .build();

        ProcessInstance processInstance = ProcessInstance.builder()
                .id(workflowInstanceId)
                .name(workflowInstanceName)
                .build();

        WorkflowExecutionDAG workflowExecutionDAG = createWorkflowExecutionDAG();

        return WorkflowExecutionContext.builder()
                .workflowExecutionDAG(workflowExecutionDAG)
                .eventRepository(new MemoryEventRepository())
                .beginNodeNames(Collections.emptyList())
                .workflowDefinition(processDefinition)
                .workflowInstance(processInstance)
                .build();
    }

    private static WorkflowExecutionDAG createWorkflowExecutionDAG() {
        TaskDefinitionLog taskA = taskNode("A");
        WorkflowDAG workflowDAG = WorkflowDAGBuilder.newBuilder()
                .addTaskNode(taskA)
                .addTaskEdge(edge(null, taskA))
                .addTaskEdge(edge(taskA, null))
                .build();

        return WorkflowExecutionDAG.builder()
                .workflowDAG(workflowDAG)
                .taskExecutionRunnableRepository(new TaskExecutionRunnableRepository())
                .build();
    }

    private static TaskDefinitionLog taskNode(String nodeName) {
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
        taskDefinitionLog.setCode(CodeGenerateUtils.getInstance().genCode());
        taskDefinitionLog.setName(nodeName);
        return taskDefinitionLog;
    }

    private static ProcessTaskRelationLog edge(TaskDefinitionLog from, TaskDefinitionLog to) {
        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.setPreTaskCode(from == null ? 0 : from.getCode());
        processTaskRelationLog.setPostTaskCode(to == null ? 0 : to.getCode());
        return processTaskRelationLog;
    }
}
