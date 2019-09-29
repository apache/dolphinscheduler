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
package org.apache.dolphinscheduler.common.process;



import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;

import java.util.List;

public class ProcessDag {

  /**
   * DAG edge list
   **/
  private List<TaskNodeRelation> edges;

  /**
   * DAG node list
   */
  private List<TaskNode> nodes;

  /**
   * getter method
   *
   * @return the edges
   * @see ProcessDag#edges
   */
  public List<TaskNodeRelation> getEdges() {
    return edges;
  }

  /**
   * setter method
   *
   * @param edges the edges to set
   * @see ProcessDag#edges
   */
  public void setEdges(List<TaskNodeRelation> edges) {
    this.edges = edges;
  }

  /**
   * getter method
   *
   * @return the nodes
   * @see ProcessDag#nodes
   */
  public List<TaskNode> getNodes() {
    return nodes;
  }

  /**
   * setter method
   *
   * @param nodes the nodes to set
   * @see ProcessDag#nodes
   */
  public void setNodes(List<TaskNode> nodes) {
    this.nodes = nodes;
  }

  @Override
  public String toString() {
    return "ProcessDag{" +
            "edges=" + edges +
            ", nodes=" + nodes +
            '}';
  }
}
