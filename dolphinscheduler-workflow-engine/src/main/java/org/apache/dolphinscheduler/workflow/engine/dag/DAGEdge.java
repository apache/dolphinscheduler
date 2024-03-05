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

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The edge of the DAG.
 * <p>
 * The edge contains the fromNodeName and the toNodeName, the fromNodeName is the node name of the from node, the toNodeName is the node name of the to node.
 * <p>
 * The formNodeName can be null, which means the edge is from the start node of the DAG.
 * The toNodeName can be null, which means the edge is to the end node of the DAG.
 * The fromNodeName and the toNodeName cannot be null at the same time.
 */
@Data
@Builder
@NoArgsConstructor
public class DAGEdge {

    private String fromNodeName;
    private String toNodeName;

    public DAGEdge(String fromNodeName, String toNodeName) {
        if (fromNodeName == null && toNodeName == null) {
            throw new IllegalArgumentException("fromNodeName and toNodeName cannot be null at the same time"
                    + "fromNodeName: " + fromNodeName + ", toNodeName: " + toNodeName);
        }
        if (fromNodeName != null && fromNodeName.equals(toNodeName)) {
            throw new IllegalArgumentException("fromNodeName and toNodeName cannot be the same"
                    + "fromNodeName: " + fromNodeName + ", toNodeName: " + toNodeName);
        }
        this.fromNodeName = fromNodeName;
        this.toNodeName = toNodeName;
    }
}
