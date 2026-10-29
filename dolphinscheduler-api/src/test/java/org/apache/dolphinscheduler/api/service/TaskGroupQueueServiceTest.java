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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.TaskGroupQueueServiceImpl;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@ExtendWith(MockitoExtension.class)
class TaskGroupQueueServiceTest {

    @InjectMocks
    private TaskGroupQueueServiceImpl service;
    @Mock
    private TaskGroupQueueMapper mapper;
    @Mock
    private WorkflowInstanceDao workflowInstanceDao;
    @Mock
    private ProjectAuthorizationService authorization;
    @Mock
    private ProjectDao projectDao;

    private User user() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.GENERAL_USER);
        return user;
    }

    @Test
    void priorityRequiresExecuteOnTheActualWorkflowProject() {
        User user = user();
        TaskGroupQueue queue = new TaskGroupQueue();
        queue.setWorkflowInstanceId(7);
        WorkflowInstance instance = new WorkflowInstance();
        instance.setProjectCode(200L);
        Mockito.when(mapper.selectById(1)).thenReturn(queue);
        Mockito.when(workflowInstanceDao.queryOptionalById(7)).thenReturn(Optional.of(instance));
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(authorization)
                .requirePermission(user, 200L, ProjectPermission.EXECUTE);
        Assertions.assertThrows(ServiceException.class, () -> service.modifyPriority(user, 1, 10));
        Mockito.verify(mapper, Mockito.never()).modifyPriority(Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void priorityUpdatesAfterExecutePermissionCheck() {
        User user = user();
        TaskGroupQueue queue = new TaskGroupQueue();
        queue.setWorkflowInstanceId(7);
        WorkflowInstance instance = new WorkflowInstance();
        instance.setProjectCode(200L);
        Mockito.when(mapper.selectById(1)).thenReturn(queue);
        Mockito.when(workflowInstanceDao.queryOptionalById(7)).thenReturn(Optional.of(instance));
        service.modifyPriority(user, 1, 10);
        org.mockito.InOrder order = Mockito.inOrder(authorization, mapper);
        order.verify(authorization).requirePermission(user, 200L, ProjectPermission.EXECUTE);
        order.verify(mapper).modifyPriority(1, 10);
    }

    @Test
    void missingQueueCannotBeModified() {
        Assertions.assertThrows(ServiceException.class, () -> service.modifyPriority(user(), 1, 10));
        Mockito.verifyNoInteractions(workflowInstanceDao, authorization);
        Mockito.verify(mapper, Mockito.never()).modifyPriority(Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void queueListWithNoVisibleProjectsSkipsUnfilteredQuery() {
        User user = user();
        Assertions.assertTrue(service.queryTasksByGroupId(user, null, null, null, -1, 1, 10).getTotalList().isEmpty());
        Mockito.verify(projectDao).queryVisibleProjects(1, false);
        Mockito.verifyNoInteractions(mapper);
    }

    @Test
    void queueListPassesVisibleProjectsToSqlPagination() {
        User user = user();
        Project project = new Project();
        project.setCode(100L);
        java.util.List<Project> projects = Collections.singletonList(project);
        Mockito.when(projectDao.queryVisibleProjects(1, false)).thenReturn(projects);
        Page<TaskGroupQueue> page = new Page<>(1, 10);
        page.setTotal(3);
        Mockito.when(mapper.queryTaskGroupQueueByTaskGroupIdPaging(Mockito.any(), Mockito.isNull(), Mockito.isNull(),
                Mockito.isNull(), Mockito.eq(-1), Mockito.eq(projects))).thenReturn(page);
        Assertions.assertEquals(3, service.queryTasksByGroupId(user, null, null, null, -1, 1, 10).getTotal());
    }
}
