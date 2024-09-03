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

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WorkflowGraph implements IWorkflowGraph {

    private final Map<Long, TaskDefinition> taskDefinitionCodeMap;
    private final Map<String, TaskDefinition> taskDefinitionMap;

    private final Map<String, List<String>> predecessors;

    private final Map<String, List<String>> successors;

    public WorkflowGraph(List<WorkflowTaskRelation> workflowTaskRelations, List<TaskDefinition> taskDefinitions) {
        checkNotNull(taskDefinitions, "taskDefinitions can not be null");
        checkNotNull(workflowTaskRelations, "taskDefinitions can not be null");
        this.predecessors = new HashMap<>();
        this.successors = new HashMap<>();

        this.taskDefinitionMap = taskDefinitions
                .stream()
                .collect(Collectors.toMap(TaskDefinition::getName, Function.identity()));
        this.taskDefinitionCodeMap = taskDefinitions
                .stream()
                .collect(Collectors.toMap(TaskDefinition::getCode, Function.identity()));

        addTaskNodes(taskDefinitions);
        addTaskEdge(workflowTaskRelations);
    }

    @Override
    public List<String> getStartNodes() {
        return predecessors.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getPredecessors(String taskName) {
        return new HashSet<>(predecessors.get(taskName));
    }

    @Override
    public Set<String> getSuccessors(String taskName) {
        return new HashSet<>(successors.get(taskName));
    }

    @Override
    public TaskDefinition getTaskNodeByName(String taskName) {
        TaskDefinition taskDefinition = taskDefinitionMap.get(taskName);
        if (taskDefinition == null) {
            throw new IllegalArgumentException("Cannot find task: " + taskName);
        }
        return taskDefinition;
    }

    @Override
    public TaskDefinition getTaskNodeByCode(Long taskCode) {
        TaskDefinition taskDefinition = taskDefinitionCodeMap.get(taskCode);
        if (taskDefinition == null) {
            throw new IllegalArgumentException("Cannot find task: " + taskCode);
        }
        return taskDefinition;
    }

    @Override
    public List<TaskDefinition> getAllTaskNodes() {
        return new ArrayList<>(taskDefinitionMap.values());
    }

    private void addTaskNodes(List<TaskDefinition> taskDefinitions) {
        taskDefinitions
                .stream()
                .map(TaskDefinition::getName)
                .forEach(taskDefinition -> {
                    if (predecessors.containsKey(taskDefinition) || successors.containsKey(taskDefinition)) {
                        throw new IllegalArgumentException("The task " + taskDefinition + " is already exists");
                    }
                    predecessors.put(taskDefinition, new ArrayList<>());
                    successors.put(taskDefinition, new ArrayList<>());
                });
    }

    private void addTaskEdge(List<WorkflowTaskRelation> workflowTaskRelations) {
        for (WorkflowTaskRelation workflowTaskRelation : workflowTaskRelations) {
            long pre = workflowTaskRelation.getPreTaskCode();
            long post = workflowTaskRelation.getPostTaskCode();
            if (pre > 0 && post > 0) {

                if (!taskDefinitionCodeMap.containsKey(pre)) {
                    throw new IllegalArgumentException("Cannot find task: " + pre);
                }
                if (!taskDefinitionCodeMap.containsKey(post)) {
                    throw new IllegalArgumentException("Cannot find task: " + post);
                }
                TaskDefinition preTask = checkNotNull(taskDefinitionCodeMap.get(pre), "Cannot find task: " + pre);
                TaskDefinition postTask = checkNotNull(taskDefinitionCodeMap.get(post), "Cannot find task: " + pre);
                List<String> predecessorsTasks = predecessors.get(postTask.getName());
                if (predecessorsTasks.contains(preTask.getName())) {
                    throw new IllegalArgumentException("The task relation from " + preTask.getName() + " to "
                            + postTask.getName() + " is already exists");
                }
                predecessorsTasks.add(preTask.getName());

                List<String> successTasks = successors.get(preTask.getName());
                if (successTasks.contains(postTask.getName())) {
                    throw new IllegalArgumentException("The task relation from " + preTask.getName() + " to "
                            + postTask.getName() + " is already exists");
                }
                successTasks.add(postTask.getName());
            }

            if (pre <= 0 && post <= 0) {
                throw new IllegalArgumentException("The task relation from " + pre + " to " + post + " is invalid");
            }

        }
    }
}
