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
    private ProcessInstanceMapMapper processInstanceMapMapper;

    /**
     * insert
     *
     * @return ProcessInstanceMap
     */
    private WorkflowInstanceRelation insertOne() {
        // insertOne
        WorkflowInstanceRelation workflowInstanceRelation = new WorkflowInstanceRelation();
        workflowInstanceRelation.setProcessInstanceId(0);
        workflowInstanceRelation.setParentTaskInstanceId(0);
        workflowInstanceRelation.setParentProcessInstanceId(0);
        processInstanceMapMapper.insert(workflowInstanceRelation);
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
        workflowInstanceRelation.setParentProcessInstanceId(1);
        int update = processInstanceMapMapper.updateById(workflowInstanceRelation);
        Assertions.assertEquals(1, update);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        WorkflowInstanceRelation workflowInstanceRelation = insertOne();
        int delete = processInstanceMapMapper.deleteById(workflowInstanceRelation.getId());
        Assertions.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        WorkflowInstanceRelation workflowInstanceRelation = insertOne();
        // query
        List<WorkflowInstanceRelation> dataSources = processInstanceMapMapper.selectList(null);
        Assertions.assertNotEquals(0, dataSources.size());
    }

    /**
     * test query by process instance parentId
     */
    @Test
    public void testQueryByParentId() {
        WorkflowInstanceRelation workflowInstanceRelation = insertOne();

        workflowInstanceRelation.setParentProcessInstanceId(100);
        processInstanceMapMapper.updateById(workflowInstanceRelation);

    }

    /**
     * test delete by parent process instance id
     */
    @Test
    public void testDeleteByParentProcessId() {
        WorkflowInstanceRelation workflowInstanceRelation = insertOne();

        workflowInstanceRelation.setParentProcessInstanceId(100);
        processInstanceMapMapper.updateById(workflowInstanceRelation);
        int delete = processInstanceMapMapper.deleteByParentProcessId(
                workflowInstanceRelation.getParentProcessInstanceId());
        Assertions.assertEquals(1, delete);
    }

    /**
     *
     * test query sub ids by process instance parentId
     */
    @Test
    public void querySubIdListByParentId() {
        WorkflowInstanceRelation workflowInstanceRelation = insertOne();
        workflowInstanceRelation.setProcessInstanceId(1);
        workflowInstanceRelation.setParentProcessInstanceId(1010);

        processInstanceMapMapper.updateById(workflowInstanceRelation);

        List<Integer> subIds =
                processInstanceMapMapper.querySubIdListByParentId(workflowInstanceRelation.getParentProcessInstanceId());

        Assertions.assertNotEquals(0, subIds.size());

    }
}
