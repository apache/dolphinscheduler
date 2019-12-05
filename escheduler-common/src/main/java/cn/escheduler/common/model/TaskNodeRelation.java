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
package cn.escheduler.common.model;

public class TaskNodeRelation {

  /**
   * task start node name
   */
  private String startNode;

  /**
   * task end node name
   */
  private String endNode;

  public TaskNodeRelation() {
  }

  public TaskNodeRelation(String startNode, String endNode) {
    this.startNode = startNode;
    this.endNode = endNode;
  }

  public String getStartNode() {
    return startNode;
  }

  public void setStartNode(String startNode) {
    this.startNode = startNode;
  }

  public String getEndNode() {
    return endNode;
  }

  public void setEndNode(String endNode) {
    this.endNode = endNode;
  }


  public boolean equals(TaskNodeRelation e){
      return (e.getStartNode() == this.startNode && e.getEndNode() == this.endNode);
  }

  @Override
  public String toString() {
    return "TaskNodeRelation{" +
            "startNode='" + startNode + '\'' +
            ", endNode='" + endNode + '\'' +
            '}';
  }
}
