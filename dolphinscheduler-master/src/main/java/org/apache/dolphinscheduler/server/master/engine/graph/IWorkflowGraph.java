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

package org.apache.dolphinscheduler.server.master.engine.graph;

import org.apache.dolphinscheduler.dao.entity.TaskDefinition;

import java.util.List;
import java.util.Set;

public interface IWorkflowGraph {

    /**
     * Get the start nodes of the workflow graph.
     * <p> The start nodes are the tasks which has no predecessors.
     */
    List<String> getStartNodes();

    /**
     * Get the tasks which is the parent of given task.
     */
    Set<String> getPredecessors(String taskName);

    /**
     * Return the tasks which is post of given taskCode and should be triggered next.
     * <p> This method will not return the task which is forbiddenTask.
     */
    Set<String> getSuccessors(String taskName);

    /**
     * Get the task by task code.
     */
    TaskDefinition getTaskNodeByCode(Long taskCode);

    /**
     * Get the task by task name.
     */
    TaskDefinition getTaskNodeByName(String taskName);

    /**
     * Get all the task nodes in the workflow graph.
     */
    List<TaskDefinition> getAllTaskNodes();

}
