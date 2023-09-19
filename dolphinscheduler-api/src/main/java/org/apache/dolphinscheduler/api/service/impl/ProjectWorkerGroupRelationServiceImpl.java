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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import jdk.nashorn.internal.ir.ReturnNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProjectWorkerGroupRelationService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectWorkerGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * task definition service impl
 */
@Service
@Slf4j
public class ProjectWorkerGroupRelationServiceImpl extends BaseServiceImpl implements ProjectWorkerGroupRelationService {

    @Autowired
    private ProjectWorkerGroupMapper projectWorkerGroupMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private WorkerGroupMapper workerGroupMapper;

    /**
     * assign worker groups to a project
     *
     * @param loginUser the login user
     * @param projectCode the project code
     * @param workerGroups assigned worker group names
     */
    @Override
    public Result assignWorkerGroupsToProject(User loginUser, Long projectCode, List<String> workerGroups) {

        Result result = new Result();

        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        if (Objects.nonNull(projectCode)) {
            putMsg(result, Status.PROJECT_NOT_EXIST);
            return result;
        }

        Project project = projectMapper.queryByCode(projectCode);
        if (Objects.isNull(project)) {
            putMsg(result, Status.PROJECT_NOT_EXIST);
            return result;
        }

        if (CollectionUtils.isEmpty(workerGroups)) {
            putMsg(result, Status.DELETE_WORKER_GROUP_NOT_EXIST);
            return result;
        }

        putMsg(result, Status.SUCCESS);

        return result;
    }

}
