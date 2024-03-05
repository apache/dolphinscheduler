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

package org.apache.dolphinscheduler.workflow.engine.dag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The IWorkflowDAG represent the DAG of a workflow.
 */
public class WorkflowDAG implements DAG {

    private final Map<String, DAGNode> dagNodeMap;

    public WorkflowDAG(List<DAGNode> dagNodes) {
        this.dagNodeMap = dagNodes.stream().collect(Collectors.toMap(DAGNode::getNodeName, Function.identity()));
    }

    @Override
    public List<DAGNode> getDirectPostNodes(DAGNode dagNode) {
        final String nodeName = dagNode.getNodeName();
        if (!dagNodeMap.containsKey(nodeName)) {
            return Collections.emptyList();
        }
        DAGNode node = dagNodeMap.get(nodeName);
        List<DAGNode> dagNodes = new ArrayList<>();
        for (DAGEdge edge : node.getOutDegrees()) {
            if (dagNodeMap.containsKey(edge.getToNodeName())) {
                dagNodes.add(dagNodeMap.get(edge.getToNodeName()));
            }
        }
        return dagNodes;
    }

    @Override
    public List<DAGNode> getDirectPreNodes(DAGNode dagNode) {
        final String nodeName = dagNode.getNodeName();
        if (!dagNodeMap.containsKey(nodeName)) {
            return Collections.emptyList();
        }
        DAGNode node = dagNodeMap.get(nodeName);
        List<DAGNode> dagNodes = new ArrayList<>();
        for (DAGEdge edge : node.getInDegrees()) {
            if (dagNodeMap.containsKey(edge.getFromNodeName())) {
                dagNodes.add(dagNodeMap.get(edge.getFromNodeName()));
            }
        }
        return dagNodes;
    }

    @Override
    public DAGNode getDAGNode(String nodeName) {
        return dagNodeMap.get(nodeName);
    }

}
