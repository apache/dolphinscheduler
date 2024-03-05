package org.apache.dolphinscheduler.workflow.engine.dag;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.dolphinscheduler.workflow.engine.assertions.WorkflowDAGAssertion.NodeAssertion.node;
import static org.apache.dolphinscheduler.workflow.engine.assertions.WorkflowDAGAssertion.workflowDag;

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
        DAGNodeDefinition dagNodeDefinition = dagNode(nodeName);
        WorkflowDAG workflowDAG = WorkflowDAGBuilder.newBuilder()
                .addTaskNode(dagNodeDefinition)
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
        DAGNodeDefinition taskNodeA = dagNode(nodeNameA);
        String nodeNameB = "B";
        DAGNodeDefinition taskNodeB = dagNode(nodeNameB);
        String nodeNameC = "C";
        DAGNodeDefinition taskNodeC = dagNode(nodeNameC);

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
        DAGNodeDefinition taskNodeA = dagNode(nodeNameA);
        String nodeNameB1 = "B1";
        DAGNodeDefinition taskNodeB1 = dagNode(nodeNameB1);
        String nodeNameB2 = "B2";
        DAGNodeDefinition taskNodeB2 = dagNode(nodeNameB2);
        String nodeNameC1 = "C1";
        DAGNodeDefinition taskNodeC1 = dagNode(nodeNameC1);
        String nodeNameC2 = "C2";
        DAGNodeDefinition taskNodeC2 = dagNode(nodeNameC2);
        String nodeNameD = "D";
        DAGNodeDefinition taskNodeD = dagNode(nodeNameD);

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
        DAGNodeDefinition taskNodeA1 = dagNode(nodeNameA1);
        String nodeNameA2 = "A2";
        DAGNodeDefinition taskNodeA2 = dagNode(nodeNameA2);

        String nodeNameB1 = "B1";
        DAGNodeDefinition taskNodeB1 = dagNode(nodeNameB1);
        String nodeNameB2 = "B2";
        DAGNodeDefinition taskNodeB2 = dagNode(nodeNameB2);
        String nodeNameB3 = "B3";
        DAGNodeDefinition taskNodeB3 = dagNode(nodeNameB3);
        String nodeNameB4 = "B4";
        DAGNodeDefinition taskNodeB4 = dagNode(nodeNameB4);

        String nodeNameC1 = "C1";
        DAGNodeDefinition taskNodeC1 = dagNode(nodeNameC1);
        String nodeNameC2 = "C2";
        DAGNodeDefinition taskNodeC2 = dagNode(nodeNameC2);
        String nodeNameC3 = "C3";
        DAGNodeDefinition taskNodeC3 = dagNode(nodeNameC3);
        String nodeNameC4 = "C4";
        DAGNodeDefinition taskNodeC4 = dagNode(nodeNameC4);

        String nodeNameD1 = "D1";
        DAGNodeDefinition taskNodeD1 = dagNode(nodeNameD1);
        String nodeNameD2 = "D2";
        DAGNodeDefinition taskNodeD2 = dagNode(nodeNameD2);

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

    private DAGNodeDefinition dagNode(String nodeName) {
        DAGNodeDefinition dagNodeDefinition = new DAGNodeDefinition();
        dagNodeDefinition.setNodeName(nodeName);
        return dagNodeDefinition;
    }

    private DAGEdge edge(DAGNodeDefinition from, DAGNodeDefinition to) {
        DAGEdge dagEdge = new DAGEdge();
        dagEdge.setFromNodeName(from == null ? null : from.getNodeName());
        dagEdge.setToNodeName(to == null ? null : to.getNodeName());
        return dagEdge;
    }

}
