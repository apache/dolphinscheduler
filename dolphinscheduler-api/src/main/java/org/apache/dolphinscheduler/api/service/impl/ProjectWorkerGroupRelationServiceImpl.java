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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectWorkerGroupRelationService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectWorkerGroup;
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

        if (Objects.isNull(projectCode)) {
            putMsg(result, Status.PROJECT_NOT_EXIST);
            return result;
        }

        if (CollectionUtils.isEmpty(workerGroups)) {
            putMsg(result, Status.WORKER_GROUP_NOT_EXIST);
            return result;
        }

        Project project = projectMapper.queryByCode(projectCode);
        if (Objects.isNull(project)) {
            putMsg(result, Status.PROJECT_NOT_EXIST);
            return result;
        }

        Set<String> workerGroupNames = workerGroupMapper.queryAllWorkerGroup().stream().map(item -> item.getName()).collect(
            Collectors.toSet());

        Set<String> assignedWorkerGroupNames = workerGroups.stream().collect(Collectors.toSet());

        Set<String> difference = SetUtils.difference(assignedWorkerGroupNames, workerGroupNames);

        if (difference.size() > 0) {
            putMsg(result, Status.WORKER_GROUP_NOT_EXIST, difference.toString());
            return result;
        }

        Set<String> projectWorkerGroupNames = projectWorkerGroupMapper.selectList(new QueryWrapper<ProjectWorkerGroup>()
            .lambda()
            .eq(ProjectWorkerGroup::getProjectCode, projectCode)
        ).stream().map(item -> item.getWorkerGroup()).collect(Collectors.toSet());


        difference = SetUtils.difference(assignedWorkerGroupNames ,projectWorkerGroupNames);

        if (CollectionUtils.isNotEmpty(difference)) {
            int deleted = projectWorkerGroupMapper.delete(new QueryWrapper<ProjectWorkerGroup>().lambda().eq(ProjectWorkerGroup::getProjectCode, projectCode
            ).in(ProjectWorkerGroup::getWorkerGroup, difference));
            if (deleted > 0) {
                log.info("Success to delete worker groups [{}] for the project [{}] .", difference, project.getName());
            } else {
                log.error("Failed to delete worker groups [{}] for the project [{}].", difference, project.getName());
                throw new ServiceException(Status.ASSIGN_WORKER_GROUP_TO_PROJECT_ERROR, project.getName());
            }
        }

        difference = SetUtils.difference(projectWorkerGroupNames, assignedWorkerGroupNames);
        Date now = new Date();
        if (CollectionUtils.isNotEmpty(difference)) {
            difference.stream().forEach(workerGroupName -> {
                ProjectWorkerGroup projectWorkerGroup = new ProjectWorkerGroup();
                projectWorkerGroup.setProjectCode(projectCode);
                projectWorkerGroup.setWorkerGroup(workerGroupName);
                projectWorkerGroup.setCreateTime(now);
                projectWorkerGroup.setUpdateTime(now);
                int create = projectWorkerGroupMapper.insert(projectWorkerGroup);
                if (create > 0) {
                    log.info("Success to add worker group [{}] for the project [{}] .", workerGroupName, project.getName());
                } else {
                    log.error("Failed to add worker group [{}] for the project [{}].", workerGroupName, project.getName());
                    throw new ServiceException(Status.ASSIGN_WORKER_GROUP_TO_PROJECT_ERROR, project.getName());
                }
            });
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query worker groups that assigned to the project
     *
     * @param projectCode project code
     */
    @Override
    public Map<String, Object> queryWorkerGroupsByProject(Long projectCode) {
        Map<String, Object> result = new HashMap<>();

        List<ProjectWorkerGroup> projectWorkerGroups = projectWorkerGroupMapper.selectList(new QueryWrapper<ProjectWorkerGroup>().lambda().eq(ProjectWorkerGroup::getProjectCode, projectCode));
        result.put(Constants.DATA_LIST, projectWorkerGroups);
        putMsg(result, Status.SUCCESS);
        return result;
    }

}
