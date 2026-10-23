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
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.K8sNamespace;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.repository.IEnvironmentDao;
import org.apache.dolphinscheduler.dao.repository.K8sNamespaceDao;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskResourcePermissionChecker {

    private final TaskDatasourcePermissionChecker datasourcePermissionChecker;
    private final ProjectResourceAuthorizationDao resourceAuthorizationDao;
    private final IEnvironmentDao environmentDao;
    private final TenantDao tenantDao;
    private final K8sNamespaceDao namespaceDao;

    public void checkPermission(long projectCode, Collection<? extends TaskDefinition> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        datasourcePermissionChecker.checkPermission(projectCode, tasks);
        Set<String> workerGroups = new HashSet<>();
        Set<Long> environmentCodes = new HashSet<>();
        Set<Integer> taskGroupIds = new HashSet<>();
        Set<Integer> alertGroupIds = new HashSet<>();
        Set<Pair<String, String>> namespaces = new HashSet<>();
        for (TaskDefinition task : tasks) {
            workerGroups.add(WorkerGroupUtils.getWorkerGroupOrDefault(task.getWorkerGroup()));
            if (task.getEnvironmentCode() > 0) {
                environmentCodes.add(task.getEnvironmentCode());
            }
            if (task.getTaskGroupId() > 0) {
                taskGroupIds.add(task.getTaskGroupId());
            }
            if ("SQL".equals(task.getTaskType())) {
                SqlParameters parameters = JSONUtils.parseObject(task.getTaskParams(), SqlParameters.class);
                if (Boolean.TRUE.equals(parameters.getSendEmail()) && parameters.getGroupId() > 0) {
                    alertGroupIds.add(parameters.getGroupId());
                }
            }
            if ("K8S".equals(task.getTaskType()) || "KUBEFLOW".equals(task.getTaskType())
                    || "SPARK".equals(task.getTaskType())) {
                String namespace = JSONUtils.toMap(task.getTaskParams()).get("namespace");
                if (StringUtils.isNotEmpty(namespace)) {
                    Map<String, String> selection = JSONUtils.toMap(namespace);
                    if (selection == null || StringUtils.isBlank(selection.get("name"))
                            || StringUtils.isBlank(selection.get("cluster"))) {
                        throw new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION);
                    }
                    namespaces.add(ImmutablePair.of(selection.get("name"), selection.get("cluster")));
                }
            }
        }
        for (String workerGroup : workerGroups) {
            requireGrant(projectCode, ProjectResourceType.WORKER_GROUP, workerGroup);
        }
        for (Long environmentCode : environmentCodes) {
            requireEnvironmentGrant(projectCode, environmentCode);
        }
        for (Integer taskGroupId : taskGroupIds) {
            requireGrant(projectCode, ProjectResourceType.TASK_GROUP, taskGroupId.toString());
        }
        for (Integer alertGroupId : alertGroupIds) {
            requireGrant(projectCode, ProjectResourceType.ALERT_GROUP, alertGroupId.toString());
        }
        if (!namespaces.isEmpty()) {
            List<K8sNamespace> grantedNamespaces = namespaceDao.queryByProject(projectCode);
            Set<Pair<String, String>> availableNamespaces = new HashSet<>();
            for (K8sNamespace namespace : grantedNamespaces) {
                availableNamespaces.add(ImmutablePair.of(namespace.getNamespace(), namespace.getClusterName()));
            }
            if (!availableNamespaces.containsAll(namespaces)) {
                throw new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION);
            }
        }
    }

    public void checkExecutionSettings(long projectCode, String workerGroup, Long environmentCode,
                                       String tenantCode, Integer warningGroupId) {
        requireGrant(projectCode, ProjectResourceType.WORKER_GROUP,
                WorkerGroupUtils.getWorkerGroupOrDefault(workerGroup));
        if (environmentCode != null && environmentCode > 0) {
            requireEnvironmentGrant(projectCode, environmentCode);
        }
        Tenant tenant = tenantDao.queryByCode(tenantCode)
                .orElseThrow(() -> new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION));
        requireGrant(projectCode, ProjectResourceType.TENANT, tenant.getId().toString());
        if (tenant.getQueueId() > 0) {
            requireGrant(projectCode, ProjectResourceType.QUEUE, Integer.toString(tenant.getQueueId()));
        }
        if (warningGroupId != null && warningGroupId > 0) {
            requireGrant(projectCode, ProjectResourceType.ALERT_GROUP, warningGroupId.toString());
        }
    }

    private void requireEnvironmentGrant(long projectCode, long environmentCode) {
        Environment environment = environmentDao.queryByEnvironmentCode(environmentCode)
                .orElseThrow(() -> new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION));
        requireGrant(projectCode, ProjectResourceType.ENVIRONMENT, environment.getId().toString());
    }

    private void requireGrant(long projectCode, ProjectResourceType type, String key) {
        if (!resourceAuthorizationDao.isAuthorized(projectCode, type, key)) {
            throw new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION);
        }
    }
}
