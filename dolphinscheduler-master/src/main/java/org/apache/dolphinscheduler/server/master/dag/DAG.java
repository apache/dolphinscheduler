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

import java.util.List;
import java.util.stream.Collectors;

/**
 * The Directed Acyclic Graph class.
 * <p>
 * The DAG is a directed graph, which contains the nodes and the edges, the nodeName is the unique identifier of the node.
 * The nodes are the tasks, the edges are the dependencies between the tasks.
 * The DAG is acyclic, which means there is no cycle in the graph.
 * The DAG is a directed graph, which means the edges have direction.
 *
 */
public interface DAG {

    /**
     * Get the direct post node of given dagNode, if the dagNode is null, return the nodes which doesn't have inDegrees.
     * e.g. The DAG is:
     * <pre>
     *      {@code
     *          1 -> 2 -> 3
     *          4 -> 5
     *          6
     *      }
     * </pre>
     * <li> The post node of 1 is 2.
     * <li> The post node of 3 is empty.
     * <li> The post node of null is 1,4,6.
     *
     * @param dagNode the node of the DAG, can be null.
     * @return post node list, sort by priority.
     */
    List<DAGNode> getDirectPostNodes(DAGNode dagNode);

    /**
     * Same with {@link #getDirectPostNodes(DAGNode)}.
     * <p>
     * If the dagNodeName is null, return the nodes which doesn't have inDegrees. Otherwise, return the post nodes of
     * the given dagNodeName. If the dagNodeName is not null and cannot find the node in DAG, throw IllegalArgumentException.
     *
     * @param dagNodeName task name, can be null.
     * @return post task name list, sort by priority.
     * @throws IllegalArgumentException if the dagNodeName is not null and cannot find the node in DAG.
     */
    default List<DAGNode> getDirectPostNodes(String dagNodeName) {
        DAGNode dagNode = getDAGNode(dagNodeName);
        if (dagNodeName != null && dagNode == null) {
            throw new IllegalArgumentException("Cannot find the Node: " + dagNodeName + " in DAG");
        }
        return getDirectPostNodes(dagNode);
    }

    /**
     * Same with {@link #getDirectPostNodes(String)}. Return the post node names.
     *
     * @param dagNodeName task name, can be null.
     * @return post task name list, sort by priority.
     * @throws IllegalArgumentException if the dagNodeName is not null and cannot find the node in DAG.
     */
    default List<String> getDirectPostNodeNames(String dagNodeName) {
        DAGNode dagNode = getDAGNode(dagNodeName);
        if (dagNodeName != null && dagNode == null) {
            throw new IllegalArgumentException("Cannot find the Node: " + dagNodeName + " in DAG");
        }
        return getDirectPostNodes(dagNode).stream().map(DAGNode::getNodeName).collect(Collectors.toList());
    }

    /**
     * Get the direct pre node of given dagNode, if the dagNode is null, return the nodes which doesn't have outDegrees.
     * e.g. The DAG is:
     * <pre>
     *      {@code
     *          1 -> 2 -> 3
     *          4 -> 5
     *          6
     *      }
     * </pre>
     * <li> The pre node of 1 is empty.
     * <li> The pre node of 3 is 2.
     * <li> The pre node of null is 3,5,6.
     *
     * @param dagNode the node of the DAG, can be null.
     * @return pre node list, sort by priority.
     */
    List<DAGNode> getDirectPreNodes(DAGNode dagNode);

    /**
     * Same with {@link #getDirectPreNodes(DAGNode)}.
     * <p>
     * If the dagNodeName is null, return the nodes which doesn't have outDegrees. Otherwise, return the pre nodes of
     * the given dagNodeName. If the dagNodeName is not null and cannot find the node in DAG, throw IllegalArgumentException.
     *
     * @param dagNodeName task name, can be null.
     * @return pre task name list, sort by priority.
     * @throws IllegalArgumentException if the dagNodeName is not null and cannot find the node in DAG.
     */
    default List<DAGNode> getDirectPreNodes(String dagNodeName) {
        DAGNode dagNode = getDAGNode(dagNodeName);
        if (dagNodeName != null && dagNode == null) {
            throw new IllegalArgumentException("Cannot find the Node: " + dagNodeName + " in DAG");
        }
        return getDirectPreNodes(dagNode);
    }

    /**
     * Same with {@link #getDirectPreNodes(String)}. Return the pre node names.
     *
     * @param dagNodeName task name, can be null.
     * @return pre task name list, sort by priority.
     * @throws IllegalArgumentException if the dagNodeName is not null and cannot find the node in DAG.
     */
    default List<String> getDirectPreNodeNames(String dagNodeName) {
        DAGNode dagNode = getDAGNode(dagNodeName);
        if (dagNodeName != null && dagNode == null) {
            throw new IllegalArgumentException("Cannot find the Node: " + dagNodeName + " in DAG");
        }
        return getDirectPreNodes(dagNode).stream().map(DAGNode::getNodeName).collect(Collectors.toList());
    }

    /**
     * Get the node of the DAG by the node name.
     *
     * @param nodeName the node name.
     * @return the node of the DAG, return null if cannot find the node.
     */
    DAGNode getDAGNode(String nodeName);

}
