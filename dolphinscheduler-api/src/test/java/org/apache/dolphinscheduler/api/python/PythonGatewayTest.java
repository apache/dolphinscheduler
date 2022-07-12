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

package org.apache.dolphinscheduler.api.python;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.spi.enums.ResourceType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.Map;

/**
 * python gate test
 */
@RunWith(MockitoJUnitRunner.class)
public class PythonGatewayTest {

    @InjectMocks
    private PythonGateway pythonGateway;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private ResourcesService resourcesService;

    @Mock
    private UsersService usersService;

    @Test
    public void testGetCodeAndVersion() throws CodeGenerateUtils.CodeGenerateException {
        Project project = getTestProject();
        Mockito.when(projectMapper.queryByName(project.getName())).thenReturn(project);

        ProcessDefinition processDefinition = getTestProcessDefinition();
        Mockito.when(processDefinitionMapper.queryByDefineName(project.getCode(), processDefinition.getName())).thenReturn(processDefinition);

        TaskDefinition taskDefinition = getTestTaskDefinition();
        Mockito.when(taskDefinitionMapper.queryByName(project.getCode(), processDefinition.getCode(), taskDefinition.getName())).thenReturn(taskDefinition);

        Map<String, Long> result = pythonGateway.getCodeAndVersion(project.getName(), processDefinition.getName(), taskDefinition.getName());
        Assert.assertEquals(result.get("code").longValue(), taskDefinition.getCode());
    }

    @Test
    public void testGetDependentInfo() {
        Project project = getTestProject();
        Mockito.when(projectMapper.queryByName(project.getName())).thenReturn(project);

        ProcessDefinition processDefinition = getTestProcessDefinition();
        Mockito.when(processDefinitionMapper.queryByDefineName(project.getCode(), processDefinition.getName())).thenReturn(processDefinition);

        TaskDefinition taskDefinition = getTestTaskDefinition();
        Mockito.when(taskDefinitionMapper.queryByName(project.getCode(), processDefinition.getCode(), taskDefinition.getName())).thenReturn(taskDefinition);

        Map<String, Object> result = pythonGateway.getDependentInfo(project.getName(), processDefinition.getName(), taskDefinition.getName());
        Assert.assertEquals((long) result.get("taskDefinitionCode"), taskDefinition.getCode());
    }

    @Test
    public void testQueryProjectByName() {
        Project project = getTestProject();
        Mockito.when(projectMapper.queryByName(project.getName())).thenReturn(project);
        Project result = pythonGateway.queryProjectByName(project.getUserName() ,project.getName());
        Assert.assertEquals(result.getName(), project.getName());
    }

    @Test
    public void testUpdateProject() {
        Project project = getTestProject();
        Mockito.when(projectMapper.queryByName(project.getName())).thenReturn(project);
        pythonGateway.updateProject("test-proj-user-name", project.getCode(), "test-proj-name", "test-proj-desc");
        Project result = projectMapper.queryByCode(project.getCode());
        Assert.assertEquals(result.getName(), project.getName());
        Assert.assertEquals(result.getDescription(), project.getDescription());
        Assert.assertEquals(result.getUserName(), project.getUserName());
    }

    @Test
    public void testDeleteProject() {
        Project project = getTestProject();
        Mockito.when(projectMapper.queryByName(project.getName())).thenReturn(project);
        pythonGateway.deleteProject(project.getUserName(), project.getCode());
        Project result = projectMapper.queryByCode(project.getCode());
        Assert.assertNull(result);
    }

    @Test
    public void testCreateTenant() {
        String tenantCode = "test-tenant-code";
        String tenantName = "test-tenant-name";
        String tenantDesc = "test-tenant-desc";
        pythonGateway.createTenant(tenantCode, tenantName, tenantDesc);
        Tenant result = tenantMapper.queryByTenantCode(tenantCode);
        Assert.assertEquals(result.getDescription(), tenantDesc);
    }

    @Test
    public void testUpdateTenant() throws Exception {
        String tenantCode = "test-tenant-code";
        String tenantDesc = "test-tenant-desc";
        Tenant tenant = getTestTenant();
        User user = getTestUser();
        User user1 = getTestUser();
        tenant.setId(user.getId());
        Mockito.when(tenantMapper.queryByTenantCode(tenantCode)).thenReturn(tenant);
        pythonGateway.updateTenant(user1.getUserName(), user1.getId(), tenantCode, 1, tenantDesc);
        Tenant result = tenantMapper.queryByTenantCode(tenantCode);
        Assert.assertEquals(result.getDescription(), tenantDesc);
        Assert.assertEquals(result.getId(), user1.getId());
        Assert.assertEquals(result.getQueueId(), 1);
        Assert.assertEquals(result.getDescription(), tenantDesc);
    }

    @Test
    public void testDeleteTenantById() throws Exception {
        Tenant tenant = getTestTenant();
        User user = getTestUser();
        tenant.setId(user.getId());
        Mockito.when(tenantMapper.queryByTenantCode(tenant.getTenantCode())).thenReturn(tenant);
        pythonGateway.deleteTenantById(user.getUserName(), tenant.getTenantCode());
        Tenant result = tenantMapper.queryByTenantCode(tenant.getTenantCode());
        Assert.assertNull(result);
    }

    @Test
    public void testQueryUser() {
        User user = getTestUser();
        Mockito.when(usersService.queryUser(user.getId())).thenReturn(user);
        User result = pythonGateway.queryUser(user.getId());
        Assert.assertEquals(result.getUserName(), user.getUserName());
    }

    @Test
    public void testDeleteUser() throws Exception {
        User user = getTestUser();
        Mockito.when(usersService.queryUser(user.getId())).thenReturn(user);
        pythonGateway.deleteUser(user.getUserName(), user.getId());
        User result = usersService.queryUser(user.getId());
        Assert.assertNull(result);
    }

    public void testCreateResource() {
        User user = getTestUser();
        String resourceDir = "/dir1/dir2/";
        String resourceName = "test";
        String resourceSuffix = "py";
        String desc = "desc";
        String content = "content";
        String resourceFullName = resourceDir + resourceName + "." + resourceSuffix;

        int resourceId = 3;

        Mockito.when(resourcesService.createOrUpdateResource(user.getUserName(), resourceFullName, desc, content))
                .thenReturn(resourceId);

        int id = pythonGateway.createOrUpdateResource(
                user.getUserName(), resourceFullName, desc, content);
        Assert.assertEquals(id, resourceId);
    }

    @Test
    public void testQueryResourcesFileInfo() {
        User user = getTestUser();
        Mockito.when(usersService.queryUser(user.getUserName())).thenReturn(user);

        Result<Object> mockResult = new Result<>();
        mockResult.setCode(Status.SUCCESS.getCode());
        Resource resource = getTestResource();
        mockResult.setData(resource);
        Mockito.when(resourcesService.queryResource(user, resource.getFullName(), null, ResourceType.FILE)).thenReturn(mockResult);

        Map<String, Object> result = pythonGateway.queryResourcesFileInfo(user.getUserName(), resource.getFullName());
        Assert.assertEquals((int) result.get("id"), resource.getId());
    }

    private Resource getTestResource() {
        Resource resource = new Resource();
        resource.setId(1);
        resource.setType(ResourceType.FILE);
        resource.setFullName("/dev/test.py");
        return resource;
    }

    private User getTestUser() {
        User user = new User();
        user.setId(1);
        user.setUserName("ut-user");
        return user;
    }

    private Project getTestProject() {
        Project project = new Project();
        project.setName("ut-project");
        project.setUserId(111);
        project.setUserName("user111");
        project.setCode(1L);
        project.setCreateTime(new Date());
        project.setUpdateTime(new Date());
        return project;
    }

    private Tenant getTestTenant() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("ut-tenant-code");
        tenant.setDescription("ut-tenant-desc");
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());
        return tenant;
    }

    private ProcessDefinition getTestProcessDefinition() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setName("ut-process-definition");
        processDefinition.setProjectCode(1L);
        processDefinition.setUserId(111);
        processDefinition.setUpdateTime(new Date());
        processDefinition.setCreateTime(new Date());
        return processDefinition;
    }

    private TaskDefinition getTestTaskDefinition() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setCode(888888L);
        taskDefinition.setName("ut-task-definition");
        taskDefinition.setProjectCode(1L);
        taskDefinition.setTaskType("SHELL");
        taskDefinition.setUserId(111);
        taskDefinition.setResourceIds("1");
        taskDefinition.setWorkerGroup("default");
        taskDefinition.setEnvironmentCode(1L);
        taskDefinition.setVersion(1);
        taskDefinition.setCreateTime(new Date());
        taskDefinition.setUpdateTime(new Date());
        return taskDefinition;
    }

}
