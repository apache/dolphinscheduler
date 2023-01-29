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

import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * python gate test
 */
@ExtendWith(MockitoExtension.class)
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

    @Test
    public void testGetCodeAndVersion() throws CodeGenerateUtils.CodeGenerateException {
        Project project = getTestProject();
        Mockito.when(projectMapper.queryByName(project.getName())).thenReturn(project);

        ProcessDefinition processDefinition = getTestProcessDefinition();
        Mockito.when(processDefinitionMapper.queryByDefineName(project.getCode(), processDefinition.getName()))
                .thenReturn(processDefinition);

        TaskDefinition taskDefinition = getTestTaskDefinition();
        Mockito.when(taskDefinitionMapper.queryByName(project.getCode(), processDefinition.getCode(),
                taskDefinition.getName())).thenReturn(taskDefinition);

        Map<String, Long> result = pythonGateway.getCodeAndVersion(project.getName(), processDefinition.getName(),
                taskDefinition.getName());
        Assertions.assertEquals(result.get("code").longValue(), taskDefinition.getCode());
    }

    @Test
    public void testGetDependentInfo() {
        Project project = getTestProject();
        Mockito.when(projectMapper.queryByName(project.getName())).thenReturn(project);

        ProcessDefinition processDefinition = getTestProcessDefinition();
        Mockito.when(processDefinitionMapper.queryByDefineName(project.getCode(), processDefinition.getName()))
                .thenReturn(processDefinition);

        TaskDefinition taskDefinition = getTestTaskDefinition();
        Mockito.when(taskDefinitionMapper.queryByName(project.getCode(), processDefinition.getCode(),
                taskDefinition.getName())).thenReturn(taskDefinition);

        Map<String, Object> result = pythonGateway.getDependentInfo(project.getName(), processDefinition.getName(),
                taskDefinition.getName());
        Assertions.assertEquals((long) result.get("taskDefinitionCode"), taskDefinition.getCode());
    }

    @Test
    public void testCreateResource() {
        User user = getTestUser();
        String resourceDir = "/dir1/dir2/";
        String resourceName = "test";
        String resourceSuffix = "py";
        String content = "content";
        String resourceFullName = resourceDir + resourceName + "." + resourceSuffix;

        Assertions.assertDoesNotThrow(
                () -> pythonGateway.createOrUpdateResource(user.getUserName(), resourceFullName, content));
    }

    @Test
    public void testQueryResourcesFileInfo() throws Exception {
        User user = getTestUser();
        StorageEntity storageEntity = getTestResource();

        Mockito.when(resourcesService.queryFileStatus(user.getUserName(), storageEntity.getFullName()))
                .thenReturn(storageEntity);
        StorageEntity result = pythonGateway.queryResourcesFileInfo(user.getUserName(), storageEntity.getFullName());
        Assertions.assertEquals(result.getId(), storageEntity.getId());
    }

    private StorageEntity getTestResource() {
        StorageEntity storageEntity = new StorageEntity();
        storageEntity.setId(1);
        storageEntity.setType(ResourceType.FILE);
        storageEntity.setFullName("/dev/test.py");
        return storageEntity;
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
