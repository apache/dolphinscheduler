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

import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.model.WorkflowDefinitionCountDto;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

public class WorkflowDefinitionMapperTest extends BaseDaoTest {

    @Autowired
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QueueMapper queueMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private ProjectMapper projectMapper;

    private AtomicLong atomicLong = new AtomicLong(0);

    /**
     * insert
     *
     * @return ProcessDefinition
     */
    private WorkflowDefinition insertOne(String name) {
        // insertOne
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(atomicLong.getAndIncrement());
        workflowDefinition.setName(name);
        workflowDefinition.setProjectCode(1010L);
        workflowDefinition.setUserId(101);
        workflowDefinition.setUpdateTime(new Date());
        workflowDefinition.setCreateTime(new Date());
        workflowDefinitionMapper.insert(workflowDefinition);
        return workflowDefinition;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        // insertOne
        WorkflowDefinition workflowDefinition = insertOne("def 1");
        // update
        workflowDefinition.setUpdateTime(new Date());
        int update = workflowDefinitionMapper.updateById(workflowDefinition);
        Assertions.assertEquals(1, update);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        WorkflowDefinition workflowDefinition = insertOne("def 1");
        int delete = workflowDefinitionMapper.deleteById(workflowDefinition.getId());
        Assertions.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        insertOne("def 1");
        // query
        List<WorkflowDefinition> dataSources = workflowDefinitionMapper.selectList(null);
        Assertions.assertNotEquals(0, dataSources.size());
    }

    /**
     * test verifyByDefineName
     */
    @Test
    public void testVerifyByDefineName() {
        Project project = new Project();
        project.setCode(1L);
        project.setName("ut project");
        project.setUserId(4);
        project.setCreateTime(new Date());
        projectMapper.insert(project);
        Queue queue = new Queue();
        queue.setQueue("queue");
        queue.setQueueName("queue name");
        queueMapper.insert(queue);
        Tenant tenant = new Tenant();
        tenant.setTenantCode("tenant");
        tenant.setQueueId(queue.getId());
        tenant.setDescription("t");
        tenantMapper.insert(tenant);
        User user = new User();
        user.setUserName("hello");
        user.setUserPassword("pwd");
        user.setUserType(UserType.GENERAL_USER);
        user.setTenantId(tenant.getId());
        userMapper.insert(user);
        // insertOne
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(1L);
        workflowDefinition.setName("def 1");
        workflowDefinition.setProjectCode(project.getCode());
        workflowDefinition.setUpdateTime(new Date());
        workflowDefinition.setCreateTime(new Date());
        workflowDefinition.setUserId(user.getId());
        workflowDefinitionMapper.insert(workflowDefinition);
        WorkflowDefinition definition = workflowDefinitionMapper.verifyByDefineName(10L, "xxx");
        Assertions.assertEquals(null, definition);
    }

    /**
     * test query by definition name
     */
    @Test
    public void testQueryByDefineName() {
        Project project = new Project();
        project.setName("ut project");
        project.setCode(1L);
        project.setUserId(4);
        project.setCreateTime(new Date());
        projectMapper.insert(project);

        Queue queue = new Queue();
        queue.setQueue("queue");
        queue.setQueueName("queue name");
        queueMapper.insert(queue);

        Tenant tenant = new Tenant();
        tenant.setTenantCode("tenant");
        tenant.setQueueId(queue.getId());
        tenant.setDescription("t");
        tenantMapper.insert(tenant);

        User user = new User();
        user.setUserName("hello");
        user.setUserPassword("pwd");
        user.setUserType(UserType.GENERAL_USER);
        user.setTenantId(tenant.getId());
        userMapper.insert(user);

        // insertOne
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(1L);
        workflowDefinition.setName("def 1");
        workflowDefinition.setProjectCode(project.getCode());
        workflowDefinition.setUpdateTime(new Date());
        workflowDefinition.setCreateTime(new Date());
        workflowDefinition.setUserId(user.getId());
        workflowDefinitionMapper.insert(workflowDefinition);

        WorkflowDefinition workflowDefinition1 = workflowDefinitionMapper.queryByDefineName(project.getCode(), "def 1");
        Assertions.assertNotEquals(null, workflowDefinition1);
    }

    /**
     * test queryByDefineId
     */
    @Test
    public void testQueryByDefineId() {
        Project project = new Project();
        project.setCode(1L);
        project.setName("ut project");
        project.setUserId(4);
        project.setCreateTime(new Date());
        projectMapper.insert(project);

        Queue queue = new Queue();
        queue.setQueue("queue");
        queue.setQueueName("queue name");
        queueMapper.insert(queue);

        Tenant tenant = new Tenant();
        tenant.setTenantCode("tenant");
        tenant.setQueueId(queue.getId());
        tenant.setDescription("t");
        tenantMapper.insert(tenant);

        User user = new User();
        user.setUserName("hello");
        user.setUserPassword("pwd");
        user.setUserType(UserType.GENERAL_USER);
        user.setTenantId(tenant.getId());
        userMapper.insert(user);

        // insertOne
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(1L);
        workflowDefinition.setName("def 1");
        workflowDefinition.setProjectCode(project.getCode());
        workflowDefinition.setUpdateTime(new Date());
        workflowDefinition.setCreateTime(new Date());
        workflowDefinition.setUserId(user.getId());
        workflowDefinitionMapper.insert(workflowDefinition);
        WorkflowDefinition definition = workflowDefinitionMapper.queryByDefineId(333);
        Assertions.assertEquals(null, definition);
    }

    /**
     * test page
     */
    @Test
    public void testQueryDefineListPaging() {
        insertOne("def 1");
        Page<WorkflowDefinition> page = new Page(1, 3);
        IPage<WorkflowDefinition> processDefinitionIPage =
                workflowDefinitionMapper.queryDefineListPaging(page, "def", 101, 1010L);
        Assertions.assertNotEquals(0, processDefinitionIPage.getTotal());
    }

    /**
     * test query all process definition
     */
    @Test
    public void testQueryAllDefinitionList() {
        insertOne("def 1");
        List<WorkflowDefinition> workflowDefinitionIPage = workflowDefinitionMapper.queryAllDefinitionList(1010L);
        Assertions.assertNotEquals(0, workflowDefinitionIPage.size());
    }

    /**
     * test query process definition by ids
     */
    @Test
    public void testQueryDefinitionListByIdList() {

        WorkflowDefinition workflowDefinition = insertOne("def 1");
        WorkflowDefinition workflowDefinition1 = insertOne("def 2");

        Integer[] array = new Integer[2];
        array[0] = workflowDefinition.getId();
        array[1] = workflowDefinition1.getId();

        List<WorkflowDefinition> workflowDefinitions = workflowDefinitionMapper.queryDefinitionListByIdList(array);
        Assertions.assertEquals(2, workflowDefinitions.size());

    }

    /**
     * test count process definition group by user
     */
    @Test
    public void testCountDefinitionGroupByUser() {

        User user = new User();
        user.setUserName("user1");
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(1);
        user.setUpdateTime(new Date());
        userMapper.insert(user);

        WorkflowDefinition workflowDefinition = insertOne("def 1");
        workflowDefinition.setUserId(user.getId());
        workflowDefinitionMapper.updateById(workflowDefinition);

        List<WorkflowDefinitionCountDto> processDefinitions = workflowDefinitionMapper
                .countDefinitionByProjectCodes(Lists.newArrayList(workflowDefinition.getProjectCode()));
        Assertions.assertNotEquals(0, processDefinitions.size());
    }

    @Test
    public void listProjectIds() {
        insertOne("def 1");
        List<Integer> projectIds = workflowDefinitionMapper.listProjectIds();
        Assertions.assertNotNull(projectIds);
    }

}
