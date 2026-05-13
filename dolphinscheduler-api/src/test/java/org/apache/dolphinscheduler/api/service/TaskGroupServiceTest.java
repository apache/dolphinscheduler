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

package org.apache.dolphinscheduler.api.service;

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowsServiceException;

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.TaskGroupServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupMapper;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskGroupServiceTest {

    @InjectMocks
    private TaskGroupServiceImpl taskGroupService;

    @Mock
    private TaskGroupMapper taskGroupMapper;

    @Mock
    private ProjectMapper projectMapper;

    private String taskGroupName = "TaskGroupServiceTest";

    private String taskGroupDesc = "this is a task group";

    private String userName = "taskGroupServiceTest";

    private String projectName = "taskGroupServiceTest";

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    private static final Logger serviceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);

    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setUserName(userName);
        loginUser.setId(1);
        return loginUser;
    }

    private Project getProject() {
        Project project = new Project();
        project.setCode(1L);
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }

    private TaskGroup getTaskGroup() {
        return TaskGroup.builder()
                .name(taskGroupName)
                .projectCode(0)
                .description(taskGroupDesc)
                .groupSize(100)
                .userId(1)
                .status(Flag.YES)
                .build();
    }

    private List<TaskGroup> getList() {
        List<TaskGroup> list = new ArrayList<>();
        list.add(getTaskGroup());
        return list;
    }

    @Test
    public void forceStartTask() {
        User loginUser = getLoginUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TASK_GROUP,
                loginUser.getId(), ApiFuncIdentificationConstant.TASK_GROUP_QUEUE_START, serviceLogger))
                .thenReturn(false);
        assertThrowsServiceException(Status.NO_CURRENT_OPERATING_PERMISSION,
                () -> taskGroupService.forceStartTask(loginUser, 1));
    }

    @Test
    public void modifyPriority() {
        User loginUser = getLoginUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TASK_GROUP,
                loginUser.getId(), ApiFuncIdentificationConstant.TASK_GROUP_QUEUE_PRIORITY, serviceLogger))
                .thenReturn(false);
        assertThrowsServiceException(Status.NO_CURRENT_OPERATING_PERMISSION,
                () -> taskGroupService.modifyPriority(loginUser, 1, 1));
    }

    @Test
    public void testCreate() {
        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TASK_GROUP,
                loginUser.getId(), ApiFuncIdentificationConstant.TASK_GROUP_CREATE, serviceLogger)).thenReturn(true);
        Mockito.when(taskGroupMapper.insert(Mockito.any(TaskGroup.class))).thenReturn(1);
        Mockito.when(taskGroupMapper.queryByName(loginUser.getId(), taskGroupName)).thenReturn(null);
        Mockito.when(projectMapper.queryByCode(taskGroup.getProjectCode())).thenReturn(getProject());
        TaskGroup created = taskGroupService.createTaskGroup(loginUser, 0L, taskGroupName, taskGroupDesc, 100);
        Assertions.assertNotNull(created);
        Assertions.assertEquals(taskGroupName, created.getName());
    }

    @Test
    public void testQueryById() {
        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);
        TaskGroup result = taskGroupService.queryTaskGroupById(loginUser, 1);
        Assertions.assertNotNull(result);
    }

    @Test
    public void testQueryProjectListPaging() {

        IPage<TaskGroup> page = new Page<>(1, 10);
        page.setRecords(getList());
        User loginUser = getLoginUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TASK_GROUP,
                loginUser.getId(), ApiFuncIdentificationConstant.TASK_GROUP_VIEW, serviceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.TASK_GROUP, null,
                0, serviceLogger)).thenReturn(true);
        Mockito.when(taskGroupMapper.queryTaskGroupPaging(Mockito.any(Page.class), Mockito.eq(null), Mockito.eq(null)))
                .thenReturn(page);

        // query all
        PageInfo<TaskGroup> pageInfo = taskGroupService.queryAllTaskGroup(loginUser, null, null, 1, 10);
        Assertions.assertNotNull(pageInfo.getTotalList());
        Assertions.assertEquals(1, pageInfo.getTotalList().size());
    }

    @Test
    public void testUpdate() {

        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();
        taskGroup.setStatus(Flag.YES);

        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TASK_GROUP,
                loginUser.getId(), ApiFuncIdentificationConstant.TASK_GROUP_EDIT, serviceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.TASK_GROUP, null,
                0, serviceLogger)).thenReturn(true);
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);
        Mockito.when(taskGroupMapper.updateById(taskGroup)).thenReturn(1);
        Mockito.when(projectMapper.queryByCode(taskGroup.getProjectCode())).thenReturn(getProject());
        TaskGroup updated = taskGroupService.updateTaskGroup(loginUser, 1, "newName", "desc", 100);
        Assertions.assertNotNull(updated);
        Assertions.assertEquals("newName", updated.getName());
    }

    @Test
    public void testCloseAndStart() {

        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);

        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TASK_GROUP,
                loginUser.getId(), ApiFuncIdentificationConstant.TASK_GROUP_CLOSE, serviceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.TASK_GROUP, null,
                0, serviceLogger)).thenReturn(true);

        // close success path
        Assertions.assertDoesNotThrow(() -> taskGroupService.closeTaskGroup(loginUser, 1));

        // close already closed
        taskGroup.setStatus(Flag.NO);
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);
        assertThrowsServiceException(Status.TASK_GROUP_STATUS_CLOSED,
                () -> taskGroupService.closeTaskGroup(loginUser, 1));

        // start already started
        taskGroup.setStatus(Flag.YES);
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);
        assertThrowsServiceException(Status.TASK_GROUP_STATUS_OPENED,
                () -> taskGroupService.startTaskGroup(loginUser, 1));
    }
}
