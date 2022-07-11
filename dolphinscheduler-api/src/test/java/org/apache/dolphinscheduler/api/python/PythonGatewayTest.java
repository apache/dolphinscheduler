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
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.spi.enums.ResourceType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.HashMap;
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
    public void testUpdateResource() {
        User user = getTestUser();
        Mockito.when(usersService.queryUser(user.getUserName())).thenReturn(user);

        String resourceDir = "/dev/";
        String resourceName = "test";
        String resourceSuffix = "py";
        String resourceFullName = resourceDir + resourceName + "." + resourceSuffix;

        Result<Object> queryMockResult = new Result<>();
        queryMockResult.setCode(Status.SUCCESS.getCode());
        Resource resource = getTestResource();
        queryMockResult.setData(resource);
        Mockito.when(resourcesService.queryResource(user, resourceFullName, null, ResourceType.FILE)).thenReturn(queryMockResult);

        Result<Object> updateMockResult = new Result<>();
        updateMockResult.setCode(Status.SUCCESS.getCode());

        Mockito.when(resourcesService.updateResourceContent(user, resource.getId(), "")).thenReturn(updateMockResult);

        int id = pythonGateway.createOrUpdateResource(
                user.getUserName(), resourceFullName, "", "");
        Assert.assertEquals(id, resource.getId());
    }

    @Test
    public void testCreateResource() {
        User user = getTestUser();
        Mockito.when(usersService.queryUser(user.getUserName())).thenReturn(user);

        String resourceDir = "/dir1/dir2/";
        String resourceName = "test";
        String resourceSuffix = "py";
        String desc = "desc";
        String content = "content";
        String resourceFullName = resourceDir + resourceName + "." + resourceSuffix;

        Result<Object> queryMockResult = new Result<>();
        queryMockResult.setCode(Status.RESOURCE_NOT_EXIST.getCode());
        Mockito.when(resourcesService.queryResource(user, resourceFullName, null, ResourceType.FILE))
                .thenReturn(queryMockResult);

        int resourceId = 3;
        Result<Object> createResourceResult = new Result<>();
        createResourceResult.setCode(Status.SUCCESS.getCode());
        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put("id", resourceId);
        createResourceResult.setData(resourceMap);

        Mockito.when(resourcesService.onlineCreateResourceWithDir(user, resourceName, resourceSuffix, desc, content, resourceDir))
                .thenReturn(createResourceResult);

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
        project.setCode(1L);
        project.setCreateTime(new Date());
        project.setUpdateTime(new Date());
        return project;
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
