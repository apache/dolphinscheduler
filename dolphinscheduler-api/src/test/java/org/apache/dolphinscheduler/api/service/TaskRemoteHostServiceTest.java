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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_REMOTE_HOST_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_REMOTE_HOST_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_REMOTE_HOST_EDIT;
import static org.mockito.ArgumentMatchers.anyString;

import org.apache.dolphinscheduler.api.dto.TaskRemoteHostDTO;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.TaskRemoteHostServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.vo.TaskRemoteHostVO;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.TaskRemoteHost;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskRemoteHostMapper;
import org.apache.dolphinscheduler.service.utils.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@ExtendWith(MockitoExtension.class)
public class TaskRemoteHostServiceTest {

    public static final Logger logger = LoggerFactory.getLogger(TaskRemoteHostServiceTest.class);
    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);
    private static final Logger taskRemoteHostServiceLogger = LoggerFactory.getLogger(TaskRemoteHostServiceImpl.class);

    @InjectMocks
    private TaskRemoteHostServiceImpl taskRemoteHostService;

    @Mock
    private TaskRemoteHostMapper taskRemoteHostMapper;

    @Mock
    private TaskInstanceMapper taskInstanceMapper;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    protected Exception exception;

    @Test
    public void test_checkTaskRemoteHostDTO() {
        User user = createUser();
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            taskRemoteHostService.createTaskRemoteHost(user, null);
        });
        Assertions.assertEquals(Status.TASK_REMOTE_HOST_DTO_IS_NULL.getCode(),
                ((ServiceException) exception).getCode());

        TaskRemoteHostDTO taskRemoteHostDTO = new TaskRemoteHostDTO();
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            taskRemoteHostService.createTaskRemoteHost(user, taskRemoteHostDTO);
        });
        Assertions.assertEquals(Status.TASK_REMOTE_HOST_NAME_IS_NULL.getCode(),
                ((ServiceException) exception).getCode());

        taskRemoteHostDTO.setName("app01");
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            taskRemoteHostService.createTaskRemoteHost(user, taskRemoteHostDTO);
        });
        Assertions.assertEquals(Status.TASK_REMOTE_HOST_ACCOUNT_IS_NULL.getCode(),
                ((ServiceException) exception).getCode());

        taskRemoteHostDTO.setAccount("foo");
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            taskRemoteHostService.createTaskRemoteHost(user, taskRemoteHostDTO);
        });
        Assertions.assertEquals(Status.TASK_REMOTE_HOST_IP_IS_NULL.getCode(), ((ServiceException) exception).getCode());

        taskRemoteHostDTO.setIp("8.8.8");
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            taskRemoteHostService.createTaskRemoteHost(user, taskRemoteHostDTO);
        });
        Assertions.assertEquals(Status.TASK_REMOTE_HOST_IP_ILLEGAL.getCode(), ((ServiceException) exception).getCode());

        taskRemoteHostDTO.setIp("8.8.8.8");
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            taskRemoteHostService.createTaskRemoteHost(user, taskRemoteHostDTO);
        });
        Assertions.assertEquals(Status.TASK_REMOTE_HOST_PORT_IS_NULL.getCode(),
                ((ServiceException) exception).getCode());

        taskRemoteHostDTO.setPort(22);
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            taskRemoteHostService.createTaskRemoteHost(user, taskRemoteHostDTO);
        });
        Assertions.assertEquals(Status.TASK_REMOTE_HOST_PASSWORD_IS_NULL.getCode(),
                ((ServiceException) exception).getCode());

        taskRemoteHostDTO.setPassword("123456");
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            taskRemoteHostService.createTaskRemoteHost(user, taskRemoteHostDTO);
        });
        Assertions.assertEquals(Status.TASK_REMOTE_HOST_DESC_IS_NULL.getCode(),
                ((ServiceException) exception).getCode());
    }

    @Test
    public void test_createTaskRemoteHost() {
        User user = createUser();
        TaskRemoteHostDTO taskRemoteHostDTO = createTaskRemoteHostDTO();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TASK_REMOTE_TASK, null,
                user.getId(), TASK_REMOTE_HOST_CREATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.TASK_REMOTE_TASK, null,
                user.getId(), baseServiceLogger)).thenReturn(true);

        TaskRemoteHost taskRemoteHost = createTaskRemoteHost();
        Mockito.when(taskRemoteHostMapper.queryByTaskRemoteHostName(anyString())).thenReturn(taskRemoteHost);
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            taskRemoteHostService.createTaskRemoteHost(user, taskRemoteHostDTO);
        });
        Assertions.assertEquals(Status.TASK_REMOTE_HOST_EXIST.getCode(), ((ServiceException) exception).getCode());

        Mockito.when(taskRemoteHostMapper.queryByTaskRemoteHostName(anyString())).thenReturn(null);
        Mockito.when(taskRemoteHostMapper.insert(Mockito.any(TaskRemoteHost.class))).thenReturn(1);
        int result = taskRemoteHostService.createTaskRemoteHost(user, taskRemoteHostDTO);
        Assertions.assertEquals(1, result);
    }

    @Test
    public void test_updateTaskRemoteHost() {
        User user = createUser();
        TaskRemoteHostDTO taskRemoteHostDTO = createTaskRemoteHostDTO();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TASK_REMOTE_TASK, null,
                user.getId(), TASK_REMOTE_HOST_EDIT, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.TASK_REMOTE_TASK, null,
                user.getId(), baseServiceLogger)).thenReturn(true);

        Mockito.when(taskRemoteHostMapper.queryByTaskRemoteHostCode(Mockito.any(Long.class))).thenReturn(null);
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            taskRemoteHostService.updateTaskRemoteHost(1L, user, taskRemoteHostDTO);
        });
        Assertions.assertEquals(Status.TASK_REMOTE_HOST_NOT_FOUND.getCode(), ((ServiceException) exception).getCode());

        TaskRemoteHost taskRemoteHost = createTaskRemoteHost();
        Mockito.when(taskRemoteHostMapper.queryByTaskRemoteHostCode(Mockito.any(Long.class)))
                .thenReturn(taskRemoteHost);
        Mockito.when(taskRemoteHostMapper.updateById(Mockito.any(TaskRemoteHost.class))).thenReturn(1);
        int result = taskRemoteHostService.updateTaskRemoteHost(1L, user, taskRemoteHostDTO);
        Assertions.assertEquals(1, result);
    }

    @Test
    public void test_deleteByCode() {
        User user = createUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TASK_REMOTE_TASK, null,
                user.getId(), TASK_REMOTE_HOST_DELETE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.TASK_REMOTE_TASK, null,
                user.getId(), baseServiceLogger)).thenReturn(true);

        Mockito.when(taskRemoteHostMapper.queryByTaskRemoteHostCode(Mockito.any(Long.class))).thenReturn(null);
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            taskRemoteHostService.deleteByCode(1L, user);
        });
        Assertions.assertEquals(Status.TASK_REMOTE_HOST_NOT_FOUND.getCode(), ((ServiceException) exception).getCode());

        TaskRemoteHost taskRemoteHost = createTaskRemoteHost();
        Mockito.when(taskRemoteHostMapper.queryByTaskRemoteHostCode(Mockito.any(Long.class)))
                .thenReturn(taskRemoteHost);
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        taskInstanceList.add(createTaskInstance());
        Mockito.when(taskInstanceMapper.queryByTaskRemoteHostCodeAndStatus(Mockito.any(Long.class),
                Mockito.eq(Constants.TASK_NOT_TERMINATED_STATES))).thenReturn(taskInstanceList);
        exception = Assertions.assertThrows(ServiceException.class, () -> {
            taskRemoteHostService.deleteByCode(1L, user);
        });
        Assertions.assertEquals(Status.DELETE_TASK_REMOTE_HOST_FAIL.getCode(),
                ((ServiceException) exception).getCode());

        Mockito.when(taskInstanceMapper.queryByTaskRemoteHostCodeAndStatus(Mockito.any(Long.class),
                Mockito.eq(Constants.TASK_NOT_TERMINATED_STATES))).thenReturn(null);
        Mockito.when(taskRemoteHostMapper.deleteByCode(Mockito.any(Long.class))).thenReturn(1);
        int result = taskRemoteHostService.deleteByCode(1L, user);
        Assertions.assertEquals(1, result);
    }

    @Test
    public void test_queryAllTaskRemoteHosts() {
        User user = createUser();
        Set<Integer> ids = new HashSet<>();
        ids.add(1);
        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.TASK_REMOTE_TASK,
                1, taskRemoteHostServiceLogger)).thenReturn(ids);
        Mockito.when(taskRemoteHostMapper.selectBatchIds(ids)).thenReturn(Lists.newArrayList(createTaskRemoteHost()));

        List<TaskRemoteHostVO> result = taskRemoteHostService.queryAllTaskRemoteHosts(user);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("foo", result.get(0).getAccount());
    }

    @Test
    public void test_queryTaskRemoteHostListPaging() {
        User user = getAdminUser();
        IPage<TaskRemoteHost> page = new Page<>(1, 10);
        page.setRecords(Lists.newArrayList(createTaskRemoteHost()));
        page.setTotal(1L);
        Mockito.when(taskRemoteHostMapper.queryTaskRemoteHostListPaging(Mockito.any(Page.class), Mockito.anyString()))
                .thenReturn(page);
        PageInfo<TaskRemoteHostVO> pageInfo = taskRemoteHostService.queryTaskRemoteHostListPaging(user, "", 1, 10);
        Assertions.assertEquals(pageInfo.getTotal(), 1);
    }

    private TaskRemoteHostDTO createTaskRemoteHostDTO() {
        TaskRemoteHostDTO taskRemoteHostDTO = new TaskRemoteHostDTO();
        taskRemoteHostDTO.setName("app01");
        taskRemoteHostDTO.setIp("127.0.0.1");
        taskRemoteHostDTO.setPort(22);
        taskRemoteHostDTO.setAccount("foo");
        taskRemoteHostDTO.setPassword("123");
        taskRemoteHostDTO.setDescription("Test description");
        return taskRemoteHostDTO;
    }

    private User createUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("foo");
        loginUser.setId(1);
        return loginUser;
    }

    private TaskRemoteHost createTaskRemoteHost() {
        TaskRemoteHost taskRemoteHost = new TaskRemoteHost();
        taskRemoteHost.setId(1);
        taskRemoteHost.setCode(1L);
        taskRemoteHost.setName("app01");
        taskRemoteHost.setIp("localhost");
        taskRemoteHost.setAccount("foo");
        taskRemoteHost.setPort(22);
        taskRemoteHost.setPassword("123");
        return taskRemoteHost;
    }

    private TaskInstance createTaskInstance() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        return taskInstance;
    }

    private User getAdminUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setUserName("admin");
        loginUser.setId(1);
        return loginUser;
    }

}
