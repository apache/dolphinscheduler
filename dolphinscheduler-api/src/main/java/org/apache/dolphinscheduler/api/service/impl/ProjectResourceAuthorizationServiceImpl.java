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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectAuthorizationService;
import org.apache.dolphinscheduler.api.service.ProjectResourceAuthorizationService;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.common.constants.TenantConstants;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProjectResourceAuthorization;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.AlertGroupDao;
import org.apache.dolphinscheduler.dao.repository.DataSourceDao;
import org.apache.dolphinscheduler.dao.repository.IEnvironmentDao;
import org.apache.dolphinscheduler.dao.repository.K8sNamespaceDao;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;
import org.apache.dolphinscheduler.dao.repository.QueueDao;
import org.apache.dolphinscheduler.dao.repository.TaskGroupDao;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.dao.repository.WorkerGroupDao;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;

import java.util.Date;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectResourceAuthorizationServiceImpl implements ProjectResourceAuthorizationService {

    private final ProjectAuthorizationService projectAuthorizationService;
    private final ProjectResourceAuthorizationDao resourceAuthorizationDao;
    private final DataSourceDao dataSourceDao;
    private final WorkerGroupDao workerGroupDao;
    private final WorkerGroupService workerGroupService;
    private final TenantDao tenantDao;
    private final QueueDao queueDao;
    private final IEnvironmentDao environmentDao;
    private final AlertGroupDao alertGroupDao;
    private final K8sNamespaceDao namespaceDao;
    private final TaskGroupDao taskGroupDao;

    @Override
    public List<ProjectResourceAuthorization> queryAuthorizations(User loginUser, long projectCode,
                                                                  ProjectResourceType resourceType) {
        projectAuthorizationService.requirePermission(loginUser, projectCode, ProjectPermission.VIEW);
        return resourceAuthorizationDao.queryByProjectAndType(projectCode, resourceType);
    }

    @Transactional
    @Override
    public void grantResource(User loginUser, long projectCode, ProjectResourceType resourceType, String resourceKey) {
        requireAdministrator(loginUser, projectCode);
        if (!resourceExists(resourceType, resourceKey)) {
            throw new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION);
        }
        if (resourceAuthorizationDao.isAuthorized(projectCode, resourceType, resourceKey)) {
            return;
        }
        ProjectResourceAuthorization grant = new ProjectResourceAuthorization();
        grant.setProjectCode(projectCode);
        grant.setResourceType(resourceType);
        grant.setResourceKey(resourceKey);
        grant.setCreateTime(new Date());
        resourceAuthorizationDao.insert(grant);
    }

    @Override
    public void revokeResource(User loginUser, long projectCode, ProjectResourceType resourceType, String resourceKey) {
        requireAdministrator(loginUser, projectCode);
        resourceAuthorizationDao.revokeAuthorization(projectCode, resourceType, resourceKey);
    }

    private void requireAdministrator(User loginUser, long projectCode) {
        if (loginUser.getUserType() != UserType.ADMIN_USER) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        projectAuthorizationService.requirePermission(loginUser, projectCode, ProjectPermission.VIEW);
    }

    // Worker groups are referenced by name; other resources use their database primary key.
    private boolean resourceExists(ProjectResourceType resourceType, String resourceKey) {
        if (resourceType == null || resourceKey == null || resourceKey.isEmpty() || resourceKey.length() > 255) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, "resource");
        }
        if (resourceType == ProjectResourceType.WORKER_GROUP) {
            return WorkerGroupUtils.getDefaultWorkerGroup().equals(resourceKey)
                    || workerGroupDao.queryWorkerGroupByName(resourceKey).stream()
                            .anyMatch(group -> resourceKey.equals(group.getName()))
                    || workerGroupService.getConfigWorkerGroupPageDetail().stream()
                            .anyMatch(group -> resourceKey.equals(group.getName()));
        }
        if (resourceType == ProjectResourceType.TENANT && "-1".equals(resourceKey)) {
            Tenant tenant = tenantDao.queryById(-1);
            return tenant != null && TenantConstants.DEFAULT_TENANT_CODE.equals(tenant.getTenantCode());
        }
        final int resourceId;
        try {
            resourceId = Integer.parseInt(resourceKey);
        } catch (NumberFormatException ex) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, "resourceKey");
        }
        if (resourceId <= 0 || !Integer.toString(resourceId).equals(resourceKey)) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, "resourceKey");
        }
        switch (resourceType) {
            case DATASOURCE:
                return dataSourceDao.queryById(resourceId) != null;
            case TENANT:
                return tenantDao.queryById(resourceId) != null;
            case QUEUE:
                return queueDao.queryById(resourceId) != null;
            case ENVIRONMENT:
                return environmentDao.queryById(resourceId) != null;
            case ALERT_GROUP:
                return alertGroupDao.queryById(resourceId) != null;
            case K8S_NAMESPACE:
                return namespaceDao.queryById(resourceId) != null;
            case TASK_GROUP:
                return taskGroupDao.queryById(resourceId) != null;
            default:
                throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, "resourceType");
        }
    }
}
