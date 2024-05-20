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
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.model.WorkflowDefinitionCountDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

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
     * test query by code
     */
    @Test
    public void testQueryByCode() {
        // insertOne
        ProcessDefinition expectedDefinition = insertOne("def 1");
        ProcessDefinition actualDefinition = processDefinitionMapper.queryByCode(expectedDefinition.getCode());
        Assertions.assertEquals(expectedDefinition.getCode(), actualDefinition.getCode());
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
        Assertions.assertNotEquals(0, dataSources.size());
    }

    /**
     * test query by codes
     */
    @Test
    public void testQueryByCodes() {
        ProcessDefinition processDefinition = insertOne("def 1");
        List<ProcessDefinition> processDefinitions = processDefinitionMapper
                .queryByCodes(Collections.singletonList(processDefinition.getCode()));
        Assertions.assertNotEquals(0, processDefinitions.size());
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
        processDefinition.setUserId(user.getId());
        processDefinitionMapper.insert(processDefinition);
        ProcessDefinition definition = processDefinitionMapper.verifyByDefineName(10L, "xxx");
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
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setName("def 1");
        processDefinition.setProjectCode(project.getCode());
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinition.setUserId(user.getId());
        processDefinitionMapper.insert(processDefinition);

        ProcessDefinition processDefinition1 = processDefinitionMapper.queryByDefineName(project.getCode(), "def 1");
        Assertions.assertNotEquals(null, processDefinition1);
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
        processDefinition.setUserId(user.getId());
        processDefinitionMapper.insert(processDefinition);
        ProcessDefinition definition = processDefinitionMapper.queryByDefineId(333);
        Assertions.assertEquals(null, definition);
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
        Assertions.assertNotEquals(0, processDefinitionIPage.getTotal());
    }

    /**
     * test filter process definition
     */
    @Test
    public void testFilterProcessDefinition() {
        ProcessDefinition processDefinition = insertOne("def 1");
        processDefinition.setReleaseState(ReleaseState.ONLINE);

        Page<ProcessDefinition> page = new Page<>(1, 3);
        IPage<ProcessDefinition> processDefinitionIPage =
                processDefinitionMapper.filterProcessDefinition(page, processDefinition);
        Assertions.assertEquals(0, processDefinitionIPage.getTotal());
    }

    /**
     * test query all process definition
     */
    @Test
    public void testQueryAllDefinitionList() {
        insertOne("def 1");
        List<ProcessDefinition> processDefinitionIPage = processDefinitionMapper.queryAllDefinitionList(1010L);
        Assertions.assertNotEquals(0, processDefinitionIPage.size());
    }

    /**
     * test query definition list by project code and process definition codes
     */
    @Test
    public void testQueryDefinitionListByProjectCodeAndProcessDefinitionCodes() {
        ProcessDefinition processDefinition = insertOne("def 1");
        List<DependentSimplifyDefinition> dependentSimplifyDefinitions = processDefinitionMapper.queryDefinitionListByProjectCodeAndProcessDefinitionCodes(
                processDefinition.getProjectCode(),
                Collections.singletonList(processDefinition.getCode())
        );
        Assertions.assertNotEquals(0, dependentSimplifyDefinitions.size());
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

        List<WorkflowDefinitionCountDto> processDefinitions = processDefinitionMapper
                .countDefinitionByProjectCodes(Lists.newArrayList(processDefinition.getProjectCode()));
        Assertions.assertNotEquals(0, processDefinitions.size());
    }

    /**
     * test count definition by project codes(V2)
     */
    @Test
    public void testCountDefinitionByProjectCodesV2() {
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

        List<Long> projectCodes = Collections.singletonList(processDefinition.getProjectCode());
        List<WorkflowDefinitionCountDto> processDefinitions = processDefinitionMapper
                .countDefinitionByProjectCodesV2(projectCodes, user.getId(), null);
        Assertions.assertEquals(1, processDefinitions.size());

        processDefinitions = processDefinitionMapper
                .countDefinitionByProjectCodesV2(projectCodes, user.getId(), ReleaseState.ONLINE.getCode());
        Assertions.assertEquals(0, processDefinitions.size());
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

    /**
     * test query definition code list by project codes
     */
    @Test
    public void testQueryDefinitionCodeListByProjectCodes() {
        Date now = new Date();

        ProcessDefinition processDefinition1 = new ProcessDefinition();
        processDefinition1.setCode(0L);
        processDefinition1.setName("def 1");
        processDefinition1.setProjectCode(1010L);
        processDefinition1.setUserId(101);
        processDefinition1.setUpdateTime(now);
        processDefinition1.setCreateTime(now);
        processDefinitionMapper.insert(processDefinition1);

        ProcessDefinition processDefinition2 = new ProcessDefinition();
        processDefinition2.setCode(1L);
        processDefinition2.setName("def 2");
        processDefinition2.setProjectCode(2020L);
        processDefinition2.setUserId(101);
        processDefinition2.setUpdateTime(now);
        processDefinition2.setCreateTime(now);
        processDefinitionMapper.insert(processDefinition2);

        List<Long> codes = processDefinitionMapper.queryDefinitionCodeListByProjectCodes(Arrays.asList(1010L, 2020L));
        codes.sort(Comparator.naturalOrder());
        Assertions.assertEquals(Arrays.asList(0L, 1L), codes);
    }

}
