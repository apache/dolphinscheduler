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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.model.TaskWorkflowSearchResult;

import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class TaskWorkflowSearchMapperTest extends BaseDaoTest {

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Autowired
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;

    @Test
    public void testFuzzySearchFindsTaskWithoutInstances() {
        insertTask(101L, 1L, 1, "load_orders_daily");
        insertWorkflow(201L, 1L, 1, "orders_workflow");
        insertRelation(1L, 201L, 1, 0L, 101L, 1);

        IPage<TaskWorkflowSearchResult> result = search(1, 10, 1L, "orders");
        Assertions.assertEquals(1, result.getTotal());
        TaskWorkflowSearchResult row = result.getRecords().get(0);
        Assertions.assertEquals(101L, row.getTaskCode());
        Assertions.assertEquals("load_orders_daily", row.getTaskName());
        Assertions.assertEquals("SHELL", row.getTaskType());
        Assertions.assertEquals(201L, row.getWorkflowDefinitionCode());
        Assertions.assertEquals("orders_workflow", row.getWorkflowDefinitionName());
        Assertions.assertEquals(0, search(1, 10, 1L, "missing").getTotal());
    }

    @Test
    public void testDeduplicatesUpstreamRelationsBeforePagingAndKeepsAllWorkflows() {
        insertTask(101L, 1L, 1, "load_orders");
        insertWorkflow(201L, 1L, 1, "workflow_a");
        insertWorkflow(202L, 1L, 1, "workflow_b");
        insertRelation(1L, 201L, 1, 99L, 101L, 1);
        insertRelation(1L, 201L, 1, 100L, 101L, 1);
        insertRelation(1L, 202L, 1, 0L, 101L, 1);

        IPage<TaskWorkflowSearchResult> first = search(1, 1, 1L, "orders");
        IPage<TaskWorkflowSearchResult> second = search(2, 1, 1L, "orders");
        Assertions.assertEquals(2, first.getTotal());
        Assertions.assertEquals(2, second.getTotal());
        Assertions.assertEquals(1, first.getRecords().size());
        Assertions.assertEquals(1, second.getRecords().size());
        Assertions.assertEquals(201L, first.getRecords().get(0).getWorkflowDefinitionCode());
        Assertions.assertEquals(202L, second.getRecords().get(0).getWorkflowDefinitionCode());
        Assertions.assertTrue(search(3, 1, 1L, "orders").getRecords().isEmpty());
    }

    @Test
    public void testScopesToProjectAndExcludesTasksWithoutExistingWorkflows() {
        insertTask(101L, 1L, 1, "orders");
        insertTask(102L, 2L, 1, "orders_other_project");
        insertTask(103L, 1L, 1, "orders_orphan");
        insertTask(104L, 1L, 1, "orders_deleted_workflow");
        insertWorkflow(201L, 1L, 1, "workflow_a");
        insertWorkflow(202L, 2L, 1, "workflow_b");
        insertRelation(1L, 201L, 1, 0L, 101L, 1);
        insertRelation(2L, 202L, 1, 0L, 102L, 1);
        insertRelation(1L, 203L, 1, 0L, 104L, 1);
        // An inconsistent cross-project relation must not expose another project's task.
        insertRelation(1L, 201L, 1, 0L, 102L, 1);

        IPage<TaskWorkflowSearchResult> result = search(1, 10, 1L, null);
        Assertions.assertEquals(1, result.getTotal());
        Assertions.assertEquals(101L, result.getRecords().get(0).getTaskCode());
        Assertions.assertEquals(1, search(1, 10, 2L, "orders").getTotal());
        Assertions.assertEquals(0, search(1, 10, 3L, "orders").getTotal());
    }

    @Test
    public void testUsesCurrentWorkflowAndReferencedTaskVersions() {
        insertTask(101L, 1L, 1, "orders_old");
        insertTask(101L, 1L, 2, "orders_current");
        insertTask(102L, 1L, 1, "orders_removed");
        insertWorkflow(201L, 1L, 2, "workflow");
        insertRelation(1L, 201L, 2, 0L, 101L, 2);
        insertRelation(1L, 201L, 1, 0L, 101L, 1);
        insertRelation(1L, 201L, 1, 0L, 102L, 1);

        IPage<TaskWorkflowSearchResult> result = search(1, 10, 1L, "orders");
        Assertions.assertEquals(1, result.getTotal());
        Assertions.assertEquals("orders_current", result.getRecords().get(0).getTaskName());
        Assertions.assertEquals(0, search(1, 10, 1L, "old").getTotal());
        Assertions.assertEquals(0, search(1, 10, 1L, "removed").getTotal());
    }

    @Test
    public void testFindsTaskVersionStillReferencedByCurrentWorkflow() {
        insertTask(101L, 1L, 1, "orders_referenced");
        insertTask(101L, 1L, 2, "orders_newer");
        insertWorkflow(201L, 1L, 1, "workflow");
        insertRelation(1L, 201L, 1, 0L, 101L, 1);

        Assertions.assertEquals("orders_referenced",
                search(1, 10, 1L, "orders").getRecords().get(0).getTaskName());
        Assertions.assertEquals(0, search(1, 10, 1L, "newer").getTotal());
    }

    @Test
    public void testSupportsLiteralUnderscorePercentAndEscapeCharacter() {
        insertTask(101L, 1L, 1, "orders_100%!");
        insertTask(102L, 1L, 1, "ordersX100anything");
        insertWorkflow(201L, 1L, 1, "workflow");
        insertRelation(1L, 201L, 1, 0L, 101L, 1);
        insertRelation(1L, 201L, 1, 0L, 102L, 1);

        Assertions.assertEquals(1, search(1, 10, 1L, "orders!_100!%!!").getTotal());
        Assertions.assertEquals(2, search(1, 10, 1L, "").getTotal());
    }

    private IPage<TaskWorkflowSearchResult> search(int pageNo, int pageSize, long projectCode, String searchVal) {
        return taskDefinitionMapper.searchTaskWorkflows(new Page<>(pageNo, pageSize), projectCode, searchVal);
    }

    private void insertTask(long code, long projectCode, int version, String name) {
        TaskDefinitionLog task = new TaskDefinitionLog();
        task.setCode(code);
        task.setProjectCode(projectCode);
        task.setVersion(version);
        task.setName(name);
        task.setTaskType("SHELL");
        task.setUserId(1);
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        taskDefinitionLogMapper.insert(task);
    }

    private void insertWorkflow(long code, long projectCode, int version, String name) {
        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setCode(code);
        workflow.setProjectCode(projectCode);
        workflow.setVersion(version);
        workflow.setName(name);
        workflow.setUserId(1);
        workflow.setCreateTime(new Date());
        workflow.setUpdateTime(new Date());
        workflowDefinitionMapper.insert(workflow);
    }

    private void insertRelation(long projectCode, long workflowCode, int workflowVersion, long preTaskCode,
                                long taskCode, int taskVersion) {
        WorkflowTaskRelation relation = new WorkflowTaskRelation();
        relation.setProjectCode(projectCode);
        relation.setWorkflowDefinitionCode(workflowCode);
        relation.setWorkflowDefinitionVersion(workflowVersion);
        relation.setPreTaskCode(preTaskCode);
        relation.setPreTaskVersion(preTaskCode == 0 ? 0 : 1);
        relation.setPostTaskCode(taskCode);
        relation.setPostTaskVersion(taskVersion);
        relation.setCreateTime(new Date());
        relation.setUpdateTime(new Date());
        workflowTaskRelationMapper.insert(relation);
    }
}
