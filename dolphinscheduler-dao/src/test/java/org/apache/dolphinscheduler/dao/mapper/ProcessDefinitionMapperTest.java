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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.dao.entity.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class ProcessDefinitionMapperTest {


    @Autowired
    ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    QueueMapper queueMapper;

    @Autowired
    TenantMapper tenantMapper;

    @Autowired
    ProjectMapper projectMapper;

    /**
     * insert
     * @return ProcessDefinition
     */
    private ProcessDefinition insertOne(){
        //insertOne
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setName("def 1");
        processDefinition.setProjectId(1010);
        processDefinition.setUserId(101);
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinitionMapper.insert(processDefinition);
        return processDefinition;
    }

    /**
     * insert
     * @return ProcessDefinition
     */
    private ProcessDefinition insertTwo(){
        //insertOne
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setName("def 2");
        processDefinition.setProjectId(1010);
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
    public void testUpdate(){
        //insertOne
        ProcessDefinition processDefinition = insertOne();
        //update
        processDefinition.setUpdateTime(new Date());
        int update = processDefinitionMapper.updateById(processDefinition);
        Assert.assertEquals(1, update);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        ProcessDefinition processDefinition = insertOne();
        int delete = processDefinitionMapper.deleteById(processDefinition.getId());
        Assert.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        ProcessDefinition processDefinition = insertOne();
        //query
        List<ProcessDefinition> dataSources = processDefinitionMapper.selectList(null);
        Assert.assertNotEquals(dataSources.size(), 0);
    }

    /**
     * test query by definition name
     */
    @Test
    public void testQueryByDefineName() {
        Project project = new Project();
        project.setName("ut project");
        project.setUserId(4);
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

        //insertOne
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setName("def 1");
        processDefinition.setProjectId(project.getId());
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        processDefinition.setTenantId(tenant.getId());
        processDefinition.setUserId(user.getId());
        processDefinitionMapper.insert(processDefinition);

        ProcessDefinition processDefinition1 = processDefinitionMapper.queryByDefineName(project.getId(), "def 1");
        Assert.assertNotEquals(processDefinition1, null);
    }

    /**
     * test page
     */
    @Test
    public void testQueryDefineListPaging() {
        ProcessDefinition processDefinition = insertOne();
        Page<ProcessDefinition> page = new Page(1,3);
        IPage<ProcessDefinition> processDefinitionIPage =  processDefinitionMapper.queryDefineListPaging(page, "def", 101, 1010,true);
        Assert.assertNotEquals(processDefinitionIPage.getTotal(), 0);
    }

    /**
     * test query all process definition
     */
    @Test
    public void testQueryAllDefinitionList() {
        ProcessDefinition processDefinition = insertOne();
        List<ProcessDefinition> processDefinitionIPage =  processDefinitionMapper.queryAllDefinitionList(1010);
        Assert.assertNotEquals(processDefinitionIPage.size(), 0);
    }

    /**
     * test query process definition by ids
     */
    @Test
    public void testQueryDefinitionListByIdList() {

        ProcessDefinition processDefinition = insertOne();
        ProcessDefinition processDefinition1 = insertTwo();

        Integer[] array = new Integer[2];
        array[0] = processDefinition.getId();
        array[1] = processDefinition1.getId();

        List<ProcessDefinition> processDefinitions = processDefinitionMapper.queryDefinitionListByIdList(array);
        Assert.assertEquals(2, processDefinitions.size());

    }

    /**
     * test count process definition group by user
     */
    @Test
    public void testCountDefinitionGroupByUser() {

        User user= new User();
        user.setUserName("user1");
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(1);
        user.setUpdateTime(new Date());
        userMapper.insert(user);

        ProcessDefinition processDefinition = insertOne();
        processDefinition.setUserId(user.getId());
        processDefinitionMapper.updateById(processDefinition);

        Integer[] projectIds = new Integer[1];
        projectIds[0] = processDefinition.getProjectId();
        List<DefinitionGroupByUser> processDefinitions = processDefinitionMapper.countDefinitionGroupByUser(
                processDefinition.getUserId(),
                projectIds,
                user.getUserType() == UserType.ADMIN_USER
        );
        Assert.assertNotEquals(processDefinitions.size(), 0);
    }

    @Test
    public void listResourcesTest(){
        ProcessDefinition processDefinition = insertOne();
        processDefinition.setResourceIds("3,5");
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        List<Map<String, Object>> maps = processDefinitionMapper.listResources();
        Assert.assertNotNull(maps);
    }

    @Test
    public void listResourcesByUserTest(){
        ProcessDefinition processDefinition = insertOne();
        processDefinition.setResourceIds("3,5");
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        List<Map<String, Object>> maps = processDefinitionMapper.listResourcesByUser(processDefinition.getUserId());
        Assert.assertNotNull(maps);
    }
}