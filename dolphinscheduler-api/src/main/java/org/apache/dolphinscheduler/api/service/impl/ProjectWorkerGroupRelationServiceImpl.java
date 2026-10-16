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
import org.apache.dolphinscheduler.api.permission.ProjectResourceAuthorizationService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.ProjectWorkerGroupRelationService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectWorkerGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectWorkerGroupRelationServiceImpl extends BaseServiceImpl
        implements
            ProjectWorkerGroupRelationService {

    @Autowired
    private ProjectDao projectDao;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectResourceAuthorizationDao resourceAuthorizationDao;
    @Autowired
    private ProjectResourceAuthorizationService resourceAuthorizationService;

    @Override
    @Transactional
    public Result assignWorkerGroupsToProject(User loginUser, Long projectCode, List<String> workerGroups) {
        Result result = new Result();
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        if (projectCode == null || projectDao.queryByCode(projectCode) == null) {
            putMsg(result, Status.PROJECT_NOT_EXIST);
            return result;
        }
        Set<String> requested = workerGroups == null ? Collections.emptySet() : new TreeSet<>(workerGroups);
        for (String workerGroup : requested) {
            resourceAuthorizationService.grantResource(loginUser, projectCode, ProjectResourceType.WORKER_GROUP,
                    workerGroup);
        }
        resourceAuthorizationDao.queryByProjectAndType(projectCode, ProjectResourceType.WORKER_GROUP)
                .stream().filter(grant -> !requested.contains(grant.getResourceKey()))
                .forEach(grant -> resourceAuthorizationService.revokeResource(loginUser, projectCode,
                        ProjectResourceType.WORKER_GROUP, grant.getResourceKey()));
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public List<ProjectWorkerGroup> queryAssignedWorkerGroupsByProject(User loginUser, Long projectCode) {
        Project project = projectDao.queryByCode(projectCode);
        projectService.checkProjectPermission(loginUser, project, ProjectPermission.VIEW);
        return resourceAuthorizationDao.queryByProjectAndType(projectCode, ProjectResourceType.WORKER_GROUP)
                .stream().map(grant -> {
                    ProjectWorkerGroup workerGroup = new ProjectWorkerGroup();
                    workerGroup.setProjectCode(projectCode);
                    workerGroup.setWorkerGroup(grant.getResourceKey());
                    return workerGroup;
                }).sorted(java.util.Comparator.comparing(ProjectWorkerGroup::getWorkerGroup))
                .collect(Collectors.toList());
    }
}
