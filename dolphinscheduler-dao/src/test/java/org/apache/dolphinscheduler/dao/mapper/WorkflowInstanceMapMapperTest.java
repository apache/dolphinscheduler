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
import org.apache.dolphinscheduler.dao.entity.WorkflowInstanceRelation;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkflowInstanceMapMapperTest extends BaseDaoTest {

    @Autowired
    private WorkflowInstanceRelationMapper workflowInstanceRelationMapper;

    /**
     * insert
     *
     * @return ProcessInstanceMap
     */
    private WorkflowInstanceRelation insertOne() {
        // insertOne
        WorkflowInstanceRelation workflowInstanceRelation = new WorkflowInstanceRelation();
        workflowInstanceRelation.setWorkflowInstanceId(0);
        workflowInstanceRelation.setParentTaskInstanceId(0);
        workflowInstanceRelation.setParentWorkflowInstanceId(0);
        workflowInstanceRelationMapper.insert(workflowInstanceRelation);
        return workflowInstanceRelation;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        // insertOne
        WorkflowInstanceRelation workflowInstanceRelation = insertOne();
        // update
        workflowInstanceRelation.setParentWorkflowInstanceId(1);
        int update = workflowInstanceRelationMapper.updateById(workflowInstanceRelation);
        Assertions.assertEquals(1, update);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        WorkflowInstanceRelation workflowInstanceRelation = insertOne();
        int delete = workflowInstanceRelationMapper.deleteById(workflowInstanceRelation.getId());
        Assertions.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        WorkflowInstanceRelation workflowInstanceRelation = insertOne();
        // query
        List<WorkflowInstanceRelation> dataSources = workflowInstanceRelationMapper.selectList(null);
        Assertions.assertNotEquals(0, dataSources.size());
    }

    /**
     * test query by process instance parentId
     */
    @Test
    public void testQueryByParentId() {
        WorkflowInstanceRelation workflowInstanceRelation = insertOne();

        workflowInstanceRelation.setParentWorkflowInstanceId(100);
        workflowInstanceRelationMapper.updateById(workflowInstanceRelation);

    }

    /**
     * test delete by parent process instance id
     */
    @Test
    public void testDeleteByParentProcessId() {
        WorkflowInstanceRelation workflowInstanceRelation = insertOne();

        workflowInstanceRelation.setParentWorkflowInstanceId(100);
        workflowInstanceRelationMapper.updateById(workflowInstanceRelation);
        int delete = workflowInstanceRelationMapper.deleteByParentWorkflowInstanceId(
                workflowInstanceRelation.getParentWorkflowInstanceId());
        Assertions.assertEquals(1, delete);
    }

    /**
     *
     * test query sub ids by process instance parentId
     */
    @Test
    public void querySubIdListByParentId() {
        WorkflowInstanceRelation workflowInstanceRelation = insertOne();
        workflowInstanceRelation.setWorkflowInstanceId(1);
        workflowInstanceRelation.setParentWorkflowInstanceId(1010);

        workflowInstanceRelationMapper.updateById(workflowInstanceRelation);

        List<Integer> subIds =
                workflowInstanceRelationMapper
                        .querySubIdListByParentId(workflowInstanceRelation.getParentWorkflowInstanceId());

        Assertions.assertNotEquals(0, subIds.size());

    }
}
