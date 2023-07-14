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

package org.apache.dolphinscheduler.server.master.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.service.model.TaskNode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WorkflowGraph implements IWorkflowGraph {

    private final Map<Long, TaskNode> taskNodeMap;
    private final DAG<Long, TaskNode, TaskNodeRelation> dag;

    private final Set<Long> forbiddenTaskCodes;

    public WorkflowGraph(List<TaskNode> taskNodes,
                         DAG<Long, TaskNode, TaskNodeRelation> dag) {
        checkNotNull(taskNodes, "taskNodes can not be null");
        checkNotNull(dag, "dag can not be null");

        this.taskNodeMap = taskNodes.stream().collect(Collectors.toMap(TaskNode::getCode, Function.identity()));
        this.dag = dag;
        forbiddenTaskCodes =
                taskNodes.stream().filter(TaskNode::isForbidden).map(TaskNode::getCode).collect(Collectors.toSet());
    }

    @Override
    public TaskNode getTaskNodeByCode(Long taskCode) {
        TaskNode taskNode = taskNodeMap.get(taskCode);
        if (taskNode == null) {
            throw new IllegalArgumentException("task node not found, taskCode: " + taskCode);
        }
        return taskNode;
    }

    @Override
    public DAG<Long, TaskNode, TaskNodeRelation> getDag() {
        return dag;
    }

    @Override
    public boolean isForbiddenTask(Long taskCode) {
        return forbiddenTaskCodes.contains(taskCode);
    }

}
