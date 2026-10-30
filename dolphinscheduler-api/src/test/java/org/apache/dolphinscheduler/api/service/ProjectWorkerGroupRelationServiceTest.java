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

import static org.apache.dolphinscheduler.api.utils.ServiceTestUtil.getAdminUser;
import static org.apache.dolphinscheduler.api.utils.ServiceTestUtil.getGeneralUser;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.ProjectWorkerGroupRelationServiceImpl;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectResourceAuthorization;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;
import org.apache.dolphinscheduler.dao.repository.ProjectWorkerGroupDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectWorkerGroupRelationServiceTest {

    @InjectMocks
    private ProjectWorkerGroupRelationServiceImpl service;
    @Mock
    private ProjectDao projectDao;
    @Mock
    private ProjectService projectService;
    @Mock
    private ProjectResourceAuthorizationDao resourceAuthorizationDao;
    @Mock
    private ProjectResourceAuthorizationService resourceAuthorizationService;
    @Mock
    private ProjectWorkerGroupDao legacyDao;
    @Mock
    private TaskDefinitionDao taskDefinitionDao;

    @Test
    void onlyAdminCanAssignAndProjectMustExist() {
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(),
                service.assignWorkerGroupsToProject(getGeneralUser(), 100L, Collections.emptyList()).getCode());
        Assertions.assertEquals(Status.PROJECT_NOT_EXIST.getCode(),
                service.assignWorkerGroupsToProject(getAdminUser(), 100L, Collections.emptyList()).getCode());
        Mockito.verifyNoInteractions(resourceAuthorizationDao, resourceAuthorizationService, legacyDao);
    }

    @Test
    void assignmentReplacesNewGrantsWithoutChangingLegacyRelations() {
        User admin = getAdminUser();
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(new Project());
        ProjectResourceAuthorization old = new ProjectResourceAuthorization();
        old.setResourceKey("old");
        Mockito.when(resourceAuthorizationDao.queryByProjectAndType(100L, ProjectResourceType.WORKER_GROUP))
                .thenReturn(Collections.singletonList(old));

        Assertions.assertEquals(Status.SUCCESS.getCode(),
                service.assignWorkerGroupsToProject(admin, 100L, Arrays.asList("new", "new")).getCode());

        Mockito.verify(resourceAuthorizationService).grantResource(admin, 100L, ProjectResourceType.WORKER_GROUP,
                "new");
        Mockito.verify(resourceAuthorizationService).revokeResource(admin, 100L, ProjectResourceType.WORKER_GROUP,
                "old");
        Mockito.verifyNoInteractions(legacyDao, taskDefinitionDao);
    }

    @Test
    void clearingGrantsDoesNotConsultHistoricalResourceUse() {
        User admin = getAdminUser();
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(new Project());
        ProjectResourceAuthorization grant = new ProjectResourceAuthorization();
        grant.setResourceKey("used-before");
        Mockito.when(resourceAuthorizationDao.queryByProjectAndType(100L, ProjectResourceType.WORKER_GROUP))
                .thenReturn(Collections.singletonList(grant));
        service.assignWorkerGroupsToProject(admin, 100L, Collections.emptyList());
        Mockito.verify(resourceAuthorizationService).revokeResource(admin, 100L,
                ProjectResourceType.WORKER_GROUP, "used-before");
        Mockito.verifyNoInteractions(legacyDao, taskDefinitionDao);
    }

    @Test
    void queryReturnsOnlyExplicitNewGrants() {
        User user = getGeneralUser();
        Project project = new Project();
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(project);
        ProjectResourceAuthorization grant = new ProjectResourceAuthorization();
        grant.setResourceKey("allowed");
        Mockito.when(resourceAuthorizationDao.queryByProjectAndType(100L, ProjectResourceType.WORKER_GROUP))
                .thenReturn(Collections.singletonList(grant));
        Assertions.assertEquals("allowed",
                service.queryAssignedWorkerGroupsByProject(user, 100L).get(0).getWorkerGroup());
        Mockito.verify(projectService).checkProjectPermission(user, project, ProjectPermission.VIEW);
        Mockito.verifyNoInteractions(legacyDao, taskDefinitionDao);
    }

    @Test
    void queryChecksProjectPermissionBeforeReadingGrants() {
        User user = getGeneralUser();
        Project project = new Project();
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(project);
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM))
                .when(projectService).checkProjectPermission(user, project, ProjectPermission.VIEW);
        Assertions.assertThrows(ServiceException.class, () -> service.queryAssignedWorkerGroupsByProject(user, 100L));
        Mockito.verifyNoInteractions(resourceAuthorizationDao);
    }
}
