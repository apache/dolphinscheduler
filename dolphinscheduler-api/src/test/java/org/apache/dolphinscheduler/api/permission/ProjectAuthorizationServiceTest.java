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

package org.apache.dolphinscheduler.api.permission;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectAuthorizationService;
import org.apache.dolphinscheduler.api.service.impl.ProjectAuthorizationServiceImpl;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUserPermission;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;
import org.apache.dolphinscheduler.dao.repository.ProjectUserPermissionDao;
import org.apache.dolphinscheduler.dao.repository.UserDao;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectAuthorizationServiceTest {

    @Mock
    private ProjectDao projectDao;
    @Mock
    private ProjectUserPermissionDao userPermissionDao;
    @Mock
    private ProjectResourceAuthorizationDao resourceAuthorizationDao;

    @Mock
    private UserDao userDao;

    private ProjectAuthorizationService authorizationService;
    private User user;

    @BeforeEach
    void setUp() {
        authorizationService =
                new ProjectAuthorizationServiceImpl(projectDao, userPermissionDao, resourceAuthorizationDao, userDao);
        user = new User();
        user.setId(1);
        user.setUserName("member");
        user.setUserType(UserType.GENERAL_USER);
    }

    @Test
    void resourceUseRequiresBothProjectPermissionAndResourceGrant() {
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(Project.builder().code(100L).build());
        Mockito.when(userPermissionDao.hasPermission(100L, 1, ProjectPermission.DEVELOP)).thenReturn(true);

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> authorizationService.requireResourceUse(user, 100L, ProjectPermission.DEVELOP,
                        ProjectResourceType.DATASOURCE, "42"));
        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());

        Mockito.when(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.DATASOURCE, "42"))
                .thenReturn(true);
        Assertions.assertDoesNotThrow(() -> authorizationService.requireResourceUse(user, 100L,
                ProjectPermission.DEVELOP, ProjectResourceType.DATASOURCE, "42"));
    }

    @Test
    void projectOwnershipDoesNotRestoreRevokedPermission() {
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(Project.builder().code(100L).userId(1).build());

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> authorizationService.requireResourceUse(user, 100L, ProjectPermission.DEVELOP,
                        ProjectResourceType.DATASOURCE, "42"));

        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(), exception.getCode());
        Mockito.verifyNoInteractions(resourceAuthorizationDao);
    }

    @Test
    void permissionDoesNotCarryAcrossProjectsOrOperations() {
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(Project.builder().code(100L).build());
        Mockito.when(projectDao.queryByCode(200L)).thenReturn(Project.builder().code(200L).build());
        Mockito.when(userPermissionDao.hasPermission(100L, 1, ProjectPermission.VIEW)).thenReturn(true);

        Assertions.assertDoesNotThrow(() -> authorizationService.requirePermission(user, 100L, ProjectPermission.VIEW));
        Assertions.assertThrows(ServiceException.class,
                () -> authorizationService.requirePermission(user, 100L, ProjectPermission.DEVELOP));
        Assertions.assertThrows(ServiceException.class,
                () -> authorizationService.requirePermission(user, 200L, ProjectPermission.VIEW));
    }

    @Test
    void adminCanManageProjectWithoutMembershipButResourceUseStillRequiresProjectGrant() {
        user.setUserType(UserType.ADMIN_USER);
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(Project.builder().code(100L).build());

        Assertions.assertDoesNotThrow(
                () -> authorizationService.requirePermission(user, 100L, ProjectPermission.MANAGE_MEMBERS));
        Assertions.assertThrows(ServiceException.class,
                () -> authorizationService.requireResourceUse(user, 100L, ProjectPermission.DEVELOP,
                        ProjectResourceType.DATASOURCE, "42"));
        Mockito.verifyNoInteractions(userPermissionDao);
    }

    @Test
    void missingProjectIsRejectedEvenForAdmin() {
        user.setUserType(UserType.ADMIN_USER);

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> authorizationService.requirePermission(user, 100L, ProjectPermission.VIEW));

        Assertions.assertEquals(Status.PROJECT_NOT_EXIST.getCode(), exception.getCode());
        Mockito.verifyNoInteractions(userPermissionDao, resourceAuthorizationDao);
    }
    @Test
    void projectMemberAdministratorCanReplacePermissionsInOwnProject() {
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(Project.builder().code(100L).build());
        Mockito.when(userPermissionDao.hasPermission(100L, 1, ProjectPermission.MANAGE_MEMBERS)).thenReturn(true);
        Mockito.when(userDao.queryById(2)).thenReturn(new User());

        authorizationService.replaceMemberPermissions(user, 100L, 2,
                EnumSet.of(ProjectPermission.VIEW, ProjectPermission.EXECUTE));

        Mockito.verify(userPermissionDao).revokeAllPermissions(100L, 2);
        ArgumentCaptor<ProjectUserPermission> grants = ArgumentCaptor.forClass(ProjectUserPermission.class);
        Mockito.verify(userPermissionDao, Mockito.times(2)).insert(grants.capture());
        Assertions.assertEquals(Arrays.asList(ProjectPermission.VIEW, ProjectPermission.EXECUTE),
                Arrays.asList(grants.getAllValues().get(0).getPermission(),
                        grants.getAllValues().get(1).getPermission()));
        grants.getAllValues().forEach(grant -> {
            Assertions.assertEquals(100L, grant.getProjectCode());
            Assertions.assertEquals(2, grant.getUserId());
            Assertions.assertNotNull(grant.getCreateTime());
        });
        Mockito.verifyNoInteractions(resourceAuthorizationDao);
    }

    @Test
    void memberManagementCannotCrossProjectBoundary() {
        Mockito.when(projectDao.queryByCode(200L)).thenReturn(Project.builder().code(200L).build());
        Assertions.assertThrows(ServiceException.class, () -> authorizationService.replaceMemberPermissions(
                user, 200L, 2, EnumSet.allOf(ProjectPermission.class)));
        Mockito.verify(userPermissionDao, Mockito.never()).revokeAllPermissions(Mockito.anyLong(), Mockito.anyInt());
        Mockito.verify(userPermissionDao, Mockito.never()).insert(Mockito.any(ProjectUserPermission.class));
        Mockito.verifyNoInteractions(userDao);
    }

    @Test
    void adminCanRevokeAllPermissionsWithEmptySet() {
        user.setUserType(UserType.ADMIN_USER);
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(Project.builder().code(100L).build());
        Mockito.when(userDao.queryById(2)).thenReturn(new User());
        authorizationService.replaceMemberPermissions(user, 100L, 2, Collections.emptySet());
        Mockito.verify(userPermissionDao).revokeAllPermissions(100L, 2);
        Mockito.verify(userPermissionDao, Mockito.never()).insert(Mockito.any(ProjectUserPermission.class));
    }

    @Test
    void missingUserAndInvalidPermissionSetDoNotChangeGrants() {
        user.setUserType(UserType.ADMIN_USER);
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(Project.builder().code(100L).build());
        Assertions.assertThrows(ServiceException.class, () -> authorizationService.replaceMemberPermissions(
                user, 100L, 2, Collections.emptySet()));
        Mockito.when(userDao.queryById(2)).thenReturn(new User());
        Assertions.assertThrows(ServiceException.class, () -> authorizationService.replaceMemberPermissions(
                user, 100L, 2, null));
        Assertions.assertThrows(ServiceException.class, () -> authorizationService.replaceMemberPermissions(
                user, 100L, 2, Collections.singleton(null)));
        Mockito.verifyNoInteractions(userPermissionDao);
    }

    @Test
    void queryMemberPermissionsRequiresMemberManagementPermission() {
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(Project.builder().code(100L).build());
        Assertions.assertThrows(ServiceException.class,
                () -> authorizationService.queryMemberPermissions(user, 100L, 2));
        Mockito.verify(userPermissionDao, Mockito.never()).queryByProjectAndUser(Mockito.anyLong(), Mockito.anyInt());
        Mockito.when(userPermissionDao.hasPermission(100L, 1, ProjectPermission.MANAGE_MEMBERS)).thenReturn(true);
        Mockito.when(userDao.queryById(2)).thenReturn(new User());
        ProjectUserPermission grant = new ProjectUserPermission();
        grant.setPermission(ProjectPermission.VIEW);
        Mockito.when(userPermissionDao.queryByProjectAndUser(100L, 2)).thenReturn(Collections.singletonList(grant));
        Assertions.assertEquals(EnumSet.of(ProjectPermission.VIEW),
                authorizationService.queryMemberPermissions(user, 100L, 2));
    }

    @Test
    void viewerCanReadOwnPermissionsButCannotInspectOtherMembers() {
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(Project.builder().code(100L).build());
        Mockito.when(userPermissionDao.hasPermission(100L, 1, ProjectPermission.VIEW)).thenReturn(true);
        Mockito.when(userDao.queryById(1)).thenReturn(user);
        ProjectUserPermission grant = new ProjectUserPermission();
        grant.setPermission(ProjectPermission.VIEW);
        Mockito.when(userPermissionDao.queryByProjectAndUser(100L, 1)).thenReturn(Collections.singletonList(grant));

        Assertions.assertEquals(EnumSet.of(ProjectPermission.VIEW),
                authorizationService.queryMemberPermissions(user, 100L, 1));
        Assertions.assertThrows(ServiceException.class,
                () -> authorizationService.queryMemberPermissions(user, 100L, 2));
        Mockito.verify(userPermissionDao, Mockito.never()).queryByProjectAndUser(100L, 2);
    }

    @Test
    void memberCandidatesRequireManageMembersBeforeUserLookup() {
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(Project.builder().code(100L).build());
        Assertions.assertThrows(ServiceException.class, () -> authorizationService.queryAssignableMembers(user, 100L));
        Mockito.verifyNoInteractions(userDao);
    }

    @Test
    void memberCandidatesExposeOnlyTheUserDirectoryFields() {
        Mockito.when(projectDao.queryByCode(100L)).thenReturn(Project.builder().code(100L).build());
        Mockito.when(userPermissionDao.hasPermission(100L, 1, ProjectPermission.MANAGE_MEMBERS)).thenReturn(true);
        User candidate = new User();
        candidate.setId(2);
        candidate.setUserName("candidate");
        Mockito.when(userDao.queryAll()).thenReturn(Collections.singletonList(candidate));
        org.apache.dolphinscheduler.api.vo.UserSimpleInfoVO info =
                authorizationService.queryAssignableMembers(user, 100L).get(0);
        Assertions.assertEquals(2, info.getId());
        Assertions.assertEquals("candidate", info.getUserName());
    }

}
