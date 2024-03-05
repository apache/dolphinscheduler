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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The node of the DAG.
 * <p>
 * The node contains the node name, the content of the node, the inDegrees and the outDegrees.
 * The inDegrees is the edge from other nodes to the current node, the outDegrees is the edge from the current
 * node to other nodes.
 */
@Getter
@Builder
@NoArgsConstructor
public class DAGNode {

    private String nodeName;

    /**
     * whether the node is skipped, default is false, which means the node is not skipped.
     * If the node is skipped, the node will not be executed.
     */
    @Builder.Default
    private boolean skip = false;

    private List<DAGEdge> inDegrees;
    private List<DAGEdge> outDegrees;

    public DAGNode(String nodeName,
                   List<DAGEdge> inDegrees,
                   List<DAGEdge> outDegrees) {
        this(nodeName, false, inDegrees, outDegrees);
    }

    public DAGNode(String nodeName,
                   boolean skip,
                   List<DAGEdge> inDegrees,
                   List<DAGEdge> outDegrees) {
        if (StringUtils.isEmpty(nodeName)) {
            throw new IllegalArgumentException("nodeName cannot be empty");
        }

        if (CollectionUtils.isNotEmpty(inDegrees)) {
            inDegrees.forEach(dagEdge -> {
                if (!nodeName.equals(dagEdge.getToNodeName())) {
                    throw new IllegalArgumentException(
                            "The toNodeName of inDegree should be the nodeName of the node: "
                                    + nodeName + ", inDegree: " + dagEdge);
                }
            });
        }

        if (CollectionUtils.isNotEmpty(outDegrees)) {
            outDegrees.forEach(dagEdge -> {
                if (!nodeName.equals(dagEdge.getFromNodeName())) {
                    throw new IllegalArgumentException(
                            "The fromNodeName of outDegree should be the nodeName of the node: "
                                    + nodeName + ", outDegree: " + dagEdge);
                }
            });
        }

        this.nodeName = nodeName;
        this.inDegrees = inDegrees;
        this.outDegrees = outDegrees;
        this.skip = skip;
    }
}
