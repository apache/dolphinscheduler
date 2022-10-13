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

import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.DefinitionGroupByUser;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class ProcessDefinitionMapperTest extends BaseDaoTest {

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

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
    private ProcessDefinition insertOne(String name) {
        // insertOne
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(atomicLong.getAndIncrement());
        processDefinition.setName(name);
        processDefinition.setProjectCode(1010L);
        processDefinition.setUserId(101);
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinitionMapper.insert(processDefinition);
        return processDefinition;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        // insertOne
        ProcessDefinition processDefinition = insertOne("def 1");
        // update
        processDefinition.setUpdateTime(new Date());
        int update = processDefinitionMapper.updateById(processDefinition);
        Assertions.assertEquals(1, update);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        ProcessDefinition processDefinition = insertOne("def 1");
        int delete = processDefinitionMapper.deleteById(processDefinition.getId());
        Assertions.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        insertOne("def 1");
        // query
        List<ProcessDefinition> dataSources = processDefinitionMapper.selectList(null);
        Assertions.assertNotEquals(dataSources.size(), 0);
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
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setName("def 1");
        processDefinition.setProjectCode(project.getCode());
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinition.setTenantId(tenant.getId());
        processDefinition.setUserId(user.getId());
        processDefinitionMapper.insert(processDefinition);
        ProcessDefinition definition = processDefinitionMapper.verifyByDefineName(10L, "xxx");
        Assertions.assertEquals(definition, null);
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
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setName("def 1");
        processDefinition.setProjectCode(project.getCode());
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinition.setTenantId(tenant.getId());
        processDefinition.setUserId(user.getId());
        processDefinitionMapper.insert(processDefinition);

        ProcessDefinition processDefinition1 = processDefinitionMapper.queryByDefineName(project.getCode(), "def 1");
        Assertions.assertNotEquals(processDefinition1, null);
    }

    /**
     * test queryDefinitionListByTenant
     */
    @Test
    public void testQueryDefinitionListByTenant() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setName("def 1");
        processDefinition.setProjectCode(888L);
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinition.setTenantId(999);
        processDefinition.setUserId(1234);
        processDefinitionMapper.insert(processDefinition);
        List<ProcessDefinition> definitions = processDefinitionMapper.queryDefinitionListByTenant(999);
        Assertions.assertNotEquals(definitions.size(), 0);
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
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setName("def 1");
        processDefinition.setProjectCode(project.getCode());
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinition.setTenantId(tenant.getId());
        processDefinition.setUserId(user.getId());
        processDefinitionMapper.insert(processDefinition);
        ProcessDefinition definition = processDefinitionMapper.queryByDefineId(333);
        Assertions.assertEquals(definition, null);
    }

    /**
     * test page
     */
    @Test
    public void testQueryDefineListPaging() {
        insertOne("def 1");
        Page<ProcessDefinition> page = new Page(1, 3);
        IPage<ProcessDefinition> processDefinitionIPage =
                processDefinitionMapper.queryDefineListPaging(page, "def", 101, 1010L);
        Assertions.assertNotEquals(processDefinitionIPage.getTotal(), 0);
    }

    /**
     * test query all process definition
     */
    @Test
    public void testQueryAllDefinitionList() {
        insertOne("def 1");
        List<ProcessDefinition> processDefinitionIPage = processDefinitionMapper.queryAllDefinitionList(1010L);
        Assertions.assertNotEquals(processDefinitionIPage.size(), 0);
    }

    /**
     * test query process definition by ids
     */
    @Test
    public void testQueryDefinitionListByIdList() {

        ProcessDefinition processDefinition = insertOne("def 1");
        ProcessDefinition processDefinition1 = insertOne("def 2");

        Integer[] array = new Integer[2];
        array[0] = processDefinition.getId();
        array[1] = processDefinition1.getId();

        List<ProcessDefinition> processDefinitions = processDefinitionMapper.queryDefinitionListByIdList(array);
        Assertions.assertEquals(2, processDefinitions.size());

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

        ProcessDefinition processDefinition = insertOne("def 1");
        processDefinition.setUserId(user.getId());
        processDefinitionMapper.updateById(processDefinition);

        Long[] projectCodes = new Long[1];
        projectCodes[0] = processDefinition.getProjectCode();
        List<DefinitionGroupByUser> processDefinitions =
                processDefinitionMapper.countDefinitionByProjectCodes(projectCodes);
        Assertions.assertNotEquals(processDefinitions.size(), 0);
    }

    @Test
    public void listResourcesTest() {
        ProcessDefinition processDefinition = insertOne("def 1");
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        List<Map<String, Object>> maps = processDefinitionMapper.listResources();
        Assertions.assertNotNull(maps);
    }

    @Test
    public void listResourcesByUserTest() {
        ProcessDefinition processDefinition = insertOne("def 1");
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        List<Map<String, Object>> maps = processDefinitionMapper.listResourcesByUser(processDefinition.getUserId());
        Assertions.assertNotNull(maps);
    }

    @Test
    public void listProjectIds() {
        insertOne("def 1");
        List<Integer> projectIds = processDefinitionMapper.listProjectIds();
        Assertions.assertNotNull(projectIds);
    }

}
