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

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.dolphinscheduler.server.master.dag.WorkflowDAGAssertion.NodeAssertion.node;
import static org.apache.dolphinscheduler.server.master.dag.WorkflowDAGAssertion.workflowDag;

import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;

import org.junit.jupiter.api.Test;

class WorkflowDAGBuilderTest {

    /**
     * Test DAG with single node:
     * <pre>
     *     {@code
     *         Node(A)
     *     }
     *  </pre>
     */
    @Test
    void build_SingleTaskNode() {
        String nodeName = "A";
        TaskDefinitionLog taskDefinitionLog = taskNode(nodeName);
        WorkflowDAG workflowDAG = WorkflowDAGBuilder.newBuilder()
                .addTaskNode(taskDefinitionLog)
                .build();
        workflowDag(workflowDAG)
                .nodeAssertion(node(nodeName).exist().noEdge())
                .doAssertion();
    }

    /**
     * Test DAG with multiple nodes:
     * <pre>
     *     {@code
     *        Node(A)
     *        Node(B)
     *        Node(C)
     *     }
     * <pre/>
     */
    @Test
    void build_MULTIPLE_NODE() {
        String nodeNameA = "A";
        TaskDefinitionLog taskNodeA = taskNode(nodeNameA);
        String nodeNameB = "B";
        TaskDefinitionLog taskNodeB = taskNode(nodeNameB);
        String nodeNameC = "C";
        TaskDefinitionLog taskNodeC = taskNode(nodeNameC);

        WorkflowDAG workflowDAG = WorkflowDAGBuilder.newBuilder()
                .addTaskNode(taskNodeA)
                .addTaskNode(taskNodeB)
                .addTaskNode(taskNodeC)
                .build();
        workflowDag(workflowDAG)
                .nodeAssertion(node(nodeNameA).exist().noEdge())
                .nodeAssertion(node(nodeNameB).exist().noEdge())
                .nodeAssertion(node(nodeNameC).exist().noEdge())
                .doAssertion();
    }

    /**
     * Test DAG with multiple nodes:
     * <pre>
     *     {@code
     *          Node(A) -> Node(B1) -> Node(C1) -> Node(D)
     *                  -> Node(B2) -> Node(C2) ->
     *     }
     * <pre/>
     */
    @Test
    void build_DAG() {
        String nodeNameA = "A";
        TaskDefinitionLog taskNodeA = taskNode(nodeNameA);
        String nodeNameB1 = "B1";
        TaskDefinitionLog taskNodeB1 = taskNode(nodeNameB1);
        String nodeNameB2 = "B2";
        TaskDefinitionLog taskNodeB2 = taskNode(nodeNameB2);
        String nodeNameC1 = "C1";
        TaskDefinitionLog taskNodeC1 = taskNode(nodeNameC1);
        String nodeNameC2 = "C2";
        TaskDefinitionLog taskNodeC2 = taskNode(nodeNameC2);
        String nodeNameD = "D";
        TaskDefinitionLog taskNodeD = taskNode(nodeNameD);

        WorkflowDAG workflowDAG = WorkflowDAGBuilder.newBuilder()
                .addTaskNode(taskNodeA)
                .addTaskNodes(newArrayList(taskNodeB1, taskNodeB2))
                .addTaskNodes(newArrayList(taskNodeC1, taskNodeC2))
                .addTaskNode(taskNodeD)
                .addTaskEdge(edge(null, taskNodeA))
                .addTaskEdge(edge(taskNodeA, taskNodeB1))
                .addTaskEdge(edge(taskNodeA, taskNodeB2))
                .addTaskEdge(edge(taskNodeB1, taskNodeC1))
                .addTaskEdge(edge(taskNodeB2, taskNodeC2))
                .addTaskEdge(edge(taskNodeC1, taskNodeD))
                .addTaskEdge(edge(taskNodeC2, taskNodeD))
                .addTaskEdge(edge(taskNodeD, null))
                .build();
        workflowDag(workflowDAG)
                .nodeAssertion(node(nodeNameA)
                        .exist()
                        .noInDegree()
                        .outDegrees(newArrayList(nodeNameB1, nodeNameB2)))
                .nodeAssertion(node(nodeNameB1)
                        .exist()
                        .inDegrees(nodeNameA)
                        .outDegrees(nodeNameC1))
                .nodeAssertion(node(nodeNameB2)
                        .exist()
                        .inDegrees(nodeNameA)
                        .outDegrees(nodeNameC2))
                .nodeAssertion(node(nodeNameC1)
                        .exist()
                        .inDegrees(nodeNameB1)
                        .outDegrees(nodeNameD))
                .nodeAssertion(node(nodeNameC2)
                        .exist()
                        .inDegrees(nodeNameB2)
                        .outDegrees(nodeNameD))
                .nodeAssertion(node(nodeNameD)
                        .exist()
                        .inDegrees(newArrayList(nodeNameC1, nodeNameC2))
                        .noOutDegree())
                .doAssertion();
    }

    /**
     * Test DAG with multiple sub dags:
     * <pre>
     *     {@code
     *          Node(A1) -> Node(B1) -> Node(C1) -> Node(D1)
     *                  -> Node(B2) -> Node(C2) ->
     *
     *          Node(A2) -> Node(B3) -> Node(C3) -> Node(D2)
     *                   -> Node(B4) -> Node(C4) ->
     *     }
     * <pre/>
     */
    @Test
    void build_MULTIPLE_SUB_DAG() {
        String nodeNameA1 = "A1";
        TaskDefinitionLog taskNodeA1 = taskNode(nodeNameA1);
        String nodeNameA2 = "A2";
        TaskDefinitionLog taskNodeA2 = taskNode(nodeNameA2);

        String nodeNameB1 = "B1";
        TaskDefinitionLog taskNodeB1 = taskNode(nodeNameB1);
        String nodeNameB2 = "B2";
        TaskDefinitionLog taskNodeB2 = taskNode(nodeNameB2);
        String nodeNameB3 = "B3";
        TaskDefinitionLog taskNodeB3 = taskNode(nodeNameB3);
        String nodeNameB4 = "B4";
        TaskDefinitionLog taskNodeB4 = taskNode(nodeNameB4);

        String nodeNameC1 = "C1";
        TaskDefinitionLog taskNodeC1 = taskNode(nodeNameC1);
        String nodeNameC2 = "C2";
        TaskDefinitionLog taskNodeC2 = taskNode(nodeNameC2);
        String nodeNameC3 = "C3";
        TaskDefinitionLog taskNodeC3 = taskNode(nodeNameC3);
        String nodeNameC4 = "C4";
        TaskDefinitionLog taskNodeC4 = taskNode(nodeNameC4);

        String nodeNameD1 = "D1";
        TaskDefinitionLog taskNodeD1 = taskNode(nodeNameD1);
        String nodeNameD2 = "D2";
        TaskDefinitionLog taskNodeD2 = taskNode(nodeNameD2);

        WorkflowDAG workflowDAG = WorkflowDAGBuilder.newBuilder()
                .addTaskNodes(newArrayList(taskNodeA1, taskNodeA2))
                .addTaskNodes(newArrayList(taskNodeB1, taskNodeB2, taskNodeB3, taskNodeB4))
                .addTaskNodes(newArrayList(taskNodeC1, taskNodeC2, taskNodeC3, taskNodeC4))
                .addTaskNodes(newArrayList(taskNodeD1, taskNodeD2))
                .addTaskEdge(edge(null, taskNodeA1))
                .addTaskEdge(edge(taskNodeA1, taskNodeB1))
                .addTaskEdge(edge(taskNodeA1, taskNodeB2))
                .addTaskEdge(edge(taskNodeB1, taskNodeC1))
                .addTaskEdge(edge(taskNodeB2, taskNodeC2))
                .addTaskEdge(edge(taskNodeC1, taskNodeD1))
                .addTaskEdge(edge(taskNodeC2, taskNodeD1))
                .addTaskEdge(edge(taskNodeD1, null))
                .addTaskEdge(edge(null, taskNodeA2))
                .addTaskEdge(edge(taskNodeA2, taskNodeB3))
                .addTaskEdge(edge(taskNodeA2, taskNodeB4))
                .addTaskEdge(edge(taskNodeB3, taskNodeC3))
                .addTaskEdge(edge(taskNodeB4, taskNodeC4))
                .addTaskEdge(edge(taskNodeC3, taskNodeD2))
                .addTaskEdge(edge(taskNodeC4, taskNodeD2))
                .addTaskEdge(edge(taskNodeD2, null))
                .build();

        workflowDag(workflowDAG)
                .nodeAssertion(node(nodeNameA1)
                        .exist()
                        .noInDegree()
                        .outDegrees(newArrayList(nodeNameB1, nodeNameB2)))
                .nodeAssertion(node(nodeNameB1)
                        .exist()
                        .inDegrees(nodeNameA1)
                        .outDegrees(nodeNameC1))
                .nodeAssertion(node(nodeNameB2)
                        .exist()
                        .inDegrees(nodeNameA1)
                        .outDegrees(nodeNameC2))
                .nodeAssertion(node(nodeNameC1)
                        .exist()
                        .inDegrees(nodeNameB1)
                        .outDegrees(nodeNameD1))
                .nodeAssertion(node(nodeNameC2)
                        .exist()
                        .inDegrees(nodeNameB2)
                        .outDegrees(nodeNameD1))
                .nodeAssertion(node(nodeNameD1)
                        .exist()
                        .inDegrees(newArrayList(nodeNameC1, nodeNameC2))
                        .noOutDegree())
                .doAssertion();
        workflowDag(workflowDAG)
                .nodeAssertion(node(nodeNameA2)
                        .exist()
                        .noInDegree()
                        .outDegrees(newArrayList(nodeNameB3, nodeNameB4)))
                .nodeAssertion(node(nodeNameB3)
                        .exist()
                        .inDegrees(nodeNameA2)
                        .outDegrees(nodeNameC3))
                .nodeAssertion(node(nodeNameB4)
                        .exist()
                        .inDegrees(nodeNameA2)
                        .outDegrees(nodeNameC4))
                .nodeAssertion(node(nodeNameC3)
                        .exist()
                        .inDegrees(nodeNameB3)
                        .outDegrees(nodeNameD2))
                .nodeAssertion(node(nodeNameC4)
                        .exist()
                        .inDegrees(nodeNameB4)
                        .outDegrees(nodeNameD2))
                .nodeAssertion(node(nodeNameD2)
                        .exist()
                        .inDegrees(newArrayList(nodeNameC3, nodeNameC4))
                        .noOutDegree())
                .doAssertion();
    }

    private TaskDefinitionLog taskNode(String nodeName) {
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
        taskDefinitionLog.setCode(CodeGenerateUtils.getInstance().genCode());
        taskDefinitionLog.setName(nodeName);
        return taskDefinitionLog;
    }

    private ProcessTaskRelationLog edge(TaskDefinitionLog from, TaskDefinitionLog to) {
        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.setPreTaskCode(from == null ? 0 : from.getCode());
        processTaskRelationLog.setPostTaskCode(to == null ? 0 : to.getCode());
        return processTaskRelationLog;
    }

}
