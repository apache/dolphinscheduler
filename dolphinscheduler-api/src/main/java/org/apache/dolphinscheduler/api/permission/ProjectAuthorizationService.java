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
import org.apache.dolphinscheduler.api.vo.UserSimpleInfoVO;
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

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.NonNull;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectAuthorizationService {

    private final UserDao userDao;
    private final ProjectDao projectDao;
    private final ProjectUserPermissionDao userPermissionDao;
    private final ProjectResourceAuthorizationDao resourceAuthorizationDao;

    public ProjectAuthorizationService(ProjectDao projectDao,
                                       ProjectUserPermissionDao userPermissionDao,
                                       ProjectResourceAuthorizationDao resourceAuthorizationDao,
                                       UserDao userDao) {
        this.userDao = userDao;
        this.projectDao = projectDao;
        this.userPermissionDao = userPermissionDao;
        this.resourceAuthorizationDao = resourceAuthorizationDao;
    }

    public List<UserSimpleInfoVO> queryAssignableMembers(User loginUser, long projectCode) {
        requirePermission(loginUser, projectCode, ProjectPermission.MANAGE_MEMBERS);
        return userDao.queryAll().stream().map(UserSimpleInfoVO::new).collect(Collectors.toList());
    }

    public Set<ProjectPermission> queryMemberPermissions(User loginUser, long projectCode, int userId) {
        requirePermission(loginUser, projectCode,
                loginUser.getId() == userId ? ProjectPermission.VIEW : ProjectPermission.MANAGE_MEMBERS);
        requireUser(userId);
        Set<ProjectPermission> permissions = EnumSet.noneOf(ProjectPermission.class);
        userPermissionDao.queryByProjectAndUser(projectCode, userId)
                .forEach(grant -> permissions.add(grant.getPermission()));
        return permissions;
    }

    @Transactional
    public void replaceMemberPermissions(User loginUser, long projectCode, int userId,
                                         Set<ProjectPermission> permissions) {
        requirePermission(loginUser, projectCode, ProjectPermission.MANAGE_MEMBERS);
        requireUser(userId);
        if (permissions == null || permissions.stream().anyMatch(java.util.Objects::isNull)) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, "permissions");
        }
        userPermissionDao.revokeAllPermissions(projectCode, userId);
        Date now = new Date();
        for (ProjectPermission permission : permissions) {
            ProjectUserPermission grant = new ProjectUserPermission();
            grant.setProjectCode(projectCode);
            grant.setUserId(userId);
            grant.setPermission(permission);
            grant.setCreateTime(now);
            userPermissionDao.insert(grant);
        }
    }

    private void requireUser(int userId) {
        if (userDao.queryById(userId) == null) {
            throw new ServiceException(Status.USER_NOT_EXIST, userId);
        }
    }

    public Project requirePermission(@NonNull User user, long projectCode, @NonNull ProjectPermission permission) {
        Project project = projectDao.queryByCode(projectCode);
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_EXIST);
        }
        if (user.getUserType() != UserType.ADMIN_USER
                && !userPermissionDao.hasPermission(projectCode, user.getId(), permission)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM, user.getUserName(), projectCode);
        }
        return project;
    }

    public void requireResourceUse(@NonNull User user, long projectCode, @NonNull ProjectPermission permission,
                                   @NonNull ProjectResourceType resourceType, @NonNull String resourceKey) {
        requirePermission(user, projectCode, permission);
        if (!resourceAuthorizationDao.isAuthorized(projectCode, resourceType, resourceKey)) {
            throw new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION);
        }
    }
}
