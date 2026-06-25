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
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkflowGraphCheckIfDAGTest {

    private List<TaskDefinition> taskDefinitions;
    private List<WorkflowTaskRelation> workflowTaskRelations;

    @BeforeEach
    public void setUp() {
        taskDefinitions = new ArrayList<>();
        workflowTaskRelations = new ArrayList<>();
    }

    @Test
    public void checkIfDAGSingleTaskNoException() {
        TaskDefinition taskDefinition = new TaskDefinition(1L, 1);
        taskDefinitions.add(taskDefinition);

        WorkflowTaskRelation relation = new WorkflowTaskRelation();
        relation.setPreTaskCode(0);
        relation.setPostTaskCode(1);
        workflowTaskRelations.add(relation);

        Assertions.assertDoesNotThrow(() -> new WorkflowGraph(workflowTaskRelations, taskDefinitions));
    }

    @Test
    public void checkIfDAGSimpleDAGNoException() {
        TaskDefinition task1 = new TaskDefinition(1L, 1);
        task1.setName("task1");
        TaskDefinition task2 = new TaskDefinition(2L, 1);
        task2.setName("task2");
        taskDefinitions.add(task1);
        taskDefinitions.add(task2);

        WorkflowTaskRelation relation = new WorkflowTaskRelation();
        relation.setPreTaskCode(1L);
        relation.setPreTaskVersion(1);
        relation.setPostTaskCode(2L);
        relation.setPostTaskVersion(1);
        workflowTaskRelations.add(relation);

        Assertions.assertDoesNotThrow(() -> new WorkflowGraph(workflowTaskRelations, taskDefinitions));
    }

    @Test
    public void checkIfDAGCycleThrowsException() {
        TaskDefinition task1 = new TaskDefinition(1L, 1);
        TaskDefinition task2 = new TaskDefinition(2L, 1);
        taskDefinitions.add(task1);
        taskDefinitions.add(task2);

        WorkflowTaskRelation relation1 = new WorkflowTaskRelation();
        relation1.setPreTaskCode(1L);
        relation1.setPreTaskVersion(1);
        relation1.setPostTaskCode(2L);
        relation1.setPostTaskVersion(1);
        WorkflowTaskRelation relation2 = new WorkflowTaskRelation();
        relation2.setPreTaskCode(2L);
        relation2.setPreTaskVersion(1);
        relation2.setPostTaskCode(1L);
        relation2.setPostTaskVersion(1);
        workflowTaskRelations.add(relation1);
        workflowTaskRelations.add(relation2);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new WorkflowGraph(workflowTaskRelations, taskDefinitions));
    }

    @Test
    public void checkIfDAGMultipleZeroInDegreeNoException() {
        TaskDefinition task1 = new TaskDefinition(1L, 1);
        task1.setName("task1");
        TaskDefinition task2 = new TaskDefinition(2L, 1);
        task2.setName("task2");
        TaskDefinition task3 = new TaskDefinition(3L, 1);
        task3.setName("task3");
        taskDefinitions.add(task1);
        taskDefinitions.add(task2);
        taskDefinitions.add(task3);

        WorkflowTaskRelation relation1 = new WorkflowTaskRelation();
        relation1.setPreTaskCode(1L);
        relation1.setPreTaskVersion(1);
        relation1.setPostTaskCode(2L);
        relation1.setPostTaskVersion(1);
        WorkflowTaskRelation relation2 = new WorkflowTaskRelation();
        relation2.setPreTaskCode(1L);
        relation2.setPreTaskVersion(1);
        relation2.setPostTaskCode(3L);
        relation2.setPostTaskVersion(1);
        workflowTaskRelations.add(relation1);
        workflowTaskRelations.add(relation2);

        Assertions.assertDoesNotThrow(() -> new WorkflowGraph(workflowTaskRelations, taskDefinitions));
    }

    @Test
    public void checkIfDAGComplexDAGNoException() {
        TaskDefinition task1 = new TaskDefinition(1L, 1);
        task1.setName("task1");
        TaskDefinition task2 = new TaskDefinition(2L, 1);
        task2.setName("task2");
        TaskDefinition task3 = new TaskDefinition(3L, 1);
        task3.setName("task3");
        TaskDefinition task4 = new TaskDefinition(4L, 1);
        task4.setName("task4");
        taskDefinitions.add(task1);
        taskDefinitions.add(task2);
        taskDefinitions.add(task3);
        taskDefinitions.add(task4);

        WorkflowTaskRelation relation1 = new WorkflowTaskRelation();
        relation1.setPreTaskCode(1L);
        relation1.setPreTaskVersion(1);
        relation1.setPostTaskCode(2L);
        relation1.setPostTaskVersion(1);

        WorkflowTaskRelation relation2 = new WorkflowTaskRelation();
        relation2.setPreTaskCode(1L);
        relation2.setPreTaskVersion(1);
        relation2.setPostTaskCode(3L);
        relation2.setPostTaskVersion(1);

        WorkflowTaskRelation relation3 = new WorkflowTaskRelation();
        relation3.setPreTaskCode(2L);
        relation3.setPreTaskVersion(1);
        relation3.setPostTaskCode(4L);
        relation3.setPostTaskVersion(1);

        WorkflowTaskRelation relation4 = new WorkflowTaskRelation();
        relation4.setPreTaskCode(3L);
        relation4.setPreTaskVersion(1);
        relation4.setPostTaskCode(4L);
        relation4.setPostTaskVersion(1);

        workflowTaskRelations.add(relation1);
        workflowTaskRelations.add(relation2);
        workflowTaskRelations.add(relation3);
        workflowTaskRelations.add(relation4);

        Assertions.assertDoesNotThrow(() -> new WorkflowGraph(workflowTaskRelations, taskDefinitions));
    }

    @Test
    public void checkIfDAGComplexDAGThrowsException() {
        TaskDefinition task1 = new TaskDefinition(1L, 1);
        TaskDefinition task2 = new TaskDefinition(2L, 1);
        TaskDefinition task3 = new TaskDefinition(3L, 1);
        TaskDefinition task4 = new TaskDefinition(4L, 1);
        taskDefinitions.add(task1);
        taskDefinitions.add(task2);
        taskDefinitions.add(task3);
        taskDefinitions.add(task4);

        WorkflowTaskRelation relation1 = new WorkflowTaskRelation();
        relation1.setPreTaskCode(1L);
        relation1.setPreTaskVersion(1);
        relation1.setPostTaskCode(2L);
        relation1.setPostTaskVersion(1);

        WorkflowTaskRelation relation2 = new WorkflowTaskRelation();
        relation2.setPreTaskCode(1L);
        relation2.setPreTaskVersion(1);
        relation2.setPostTaskCode(3L);
        relation2.setPostTaskVersion(1);

        WorkflowTaskRelation relation3 = new WorkflowTaskRelation();
        relation3.setPreTaskCode(2L);
        relation3.setPreTaskVersion(1);
        relation3.setPostTaskCode(4L);
        relation3.setPostTaskVersion(1);

        WorkflowTaskRelation relation4 = new WorkflowTaskRelation();
        relation4.setPreTaskCode(3L);
        relation4.setPreTaskVersion(1);
        relation4.setPostTaskCode(4L);
        relation4.setPostTaskVersion(1);

        WorkflowTaskRelation relation5 = new WorkflowTaskRelation();
        relation5.setPreTaskCode(4L);
        relation5.setPreTaskVersion(1);
        relation5.setPostTaskCode(3L);
        relation5.setPostTaskVersion(1);

        workflowTaskRelations.add(relation1);
        workflowTaskRelations.add(relation2);
        workflowTaskRelations.add(relation3);
        workflowTaskRelations.add(relation4);
        workflowTaskRelations.add(relation5);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new WorkflowGraph(workflowTaskRelations, taskDefinitions));
    }
}
