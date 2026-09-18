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
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.TaskDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.model.TaskWorkflowSearchResult;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@ExtendWith(MockitoExtension.class)
public class TaskWorkflowSearchServiceTest {

    @InjectMocks
    private TaskDefinitionServiceImpl taskDefinitionService;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskDefinitionDao taskDefinitionDao;

    @Test
    public void testSearchReturnsPageAndEscapesLiteralTaskName() {
        User user = new User();
        TaskWorkflowSearchResult task = new TaskWorkflowSearchResult();
        task.setTaskName("orders_100%!");
        Page<TaskWorkflowSearchResult> page = new Page<>(2, 10);
        page.setTotal(11);
        page.setRecords(Collections.singletonList(task));
        when(taskDefinitionDao.searchTaskWorkflows(2, 10, 1L, "orders!_100!%!!")).thenReturn(page);

        PageInfo<TaskWorkflowSearchResult> result =
                taskDefinitionService.searchTaskWorkflows(user, 1L, " orders_100%! ", 2, 10);

        Assertions.assertEquals(11, result.getTotal());
        Assertions.assertEquals(2, result.getCurrentPage());
        Assertions.assertEquals(10, result.getPageSize());
        Assertions.assertEquals(Collections.singletonList(task), result.getTotalList());
        verify(projectService).checkProjectAndAuthThrowException(user, 1L, TASK_DEFINITION);
        verify(projectService, never()).checkHasProjectWritePermissionThrowException(user, 1L);
    }

    @Test
    public void testEmptySearchReturnsEmptyPage() {
        User user = new User();
        when(taskDefinitionDao.searchTaskWorkflows(1, 10, 1L, "")).thenReturn(new Page<>(1, 10));

        PageInfo<TaskWorkflowSearchResult> result =
                taskDefinitionService.searchTaskWorkflows(user, 1L, null, 1, 10);

        Assertions.assertEquals(0, result.getTotal());
        Assertions.assertTrue(result.getTotalList().isEmpty());
    }

    @Test
    public void testRejectsUnauthorizedProjectBeforeQueryingTasks() {
        User user = new User();
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM, "user", 1L))
                .when(projectService).checkProjectAndAuthThrowException(user, 1L, TASK_DEFINITION);

        assertThrowsServiceException(Status.USER_NO_OPERATION_PROJECT_PERM,
                () -> taskDefinitionService.searchTaskWorkflows(user, 1L, "orders", 1, 10));
        verify(taskDefinitionDao, never()).searchTaskWorkflows(anyInt(), anyInt(), anyLong(), anyString());
    }

    @Test
    public void testRejectsMissingProjectBeforeQueryingTasks() {
        User user = new User();
        doThrow(new ServiceException(Status.PROJECT_NOT_EXIST))
                .when(projectService).checkProjectAndAuthThrowException(user, 1L, TASK_DEFINITION);

        assertThrowsServiceException(Status.PROJECT_NOT_EXIST,
                () -> taskDefinitionService.searchTaskWorkflows(user, 1L, "orders", 1, 10));
        verify(taskDefinitionDao, never()).searchTaskWorkflows(anyInt(), anyInt(), anyLong(), anyString());
    }
}
