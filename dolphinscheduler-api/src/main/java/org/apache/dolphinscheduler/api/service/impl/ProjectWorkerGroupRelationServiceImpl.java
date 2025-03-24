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
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.ProjectWorkerGroupRelationService;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectWorkerGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.repository.ProjectWorkerGroupDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkerGroupDao;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProjectWorkerGroupRelationServiceImpl extends BaseServiceImpl
        implements
            ProjectWorkerGroupRelationService {

    @Autowired
    private ProjectWorkerGroupDao projectWorkerGroupDao;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private WorkerGroupDao workerGroupDao;

    @Autowired
    private WorkerGroupService workerGroupService;

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
            boolean deleted = projectWorkerGroupDao.deleteByProjectCode(projectCode);
            if (deleted) {
                putMsg(result, Status.SUCCESS);
            } else {
                putMsg(result, Status.ASSIGN_WORKER_GROUP_TO_PROJECT_ERROR);
            }
            return result;
        }

        Project project = projectMapper.queryByCode(projectCode);
        if (Objects.isNull(project)) {
            putMsg(result, Status.PROJECT_NOT_EXIST);
            return result;
        }

        Set<String> allWorkerGroupNames = new HashSet<>(workerGroupDao.queryAllWorkerGroupNames());
        workerGroupService.getConfigWorkerGroupPageDetail().forEach(
                workerGroupPageDetail -> allWorkerGroupNames.add(workerGroupPageDetail.getName()));
        Set<String> unauthorizedWorkerGroupNames = new HashSet<>(workerGroups);

        // check if assign worker group exists in the system
        Set<String> difference = SetUtils.difference(unauthorizedWorkerGroupNames, allWorkerGroupNames);
        if (!difference.isEmpty()) {
            putMsg(result, Status.WORKER_GROUP_NOT_EXIST, difference.toString());
            return result;
        }

        // check if assign worker group exists in the project
        Set<String> projectWorkerGroupNames =
                projectWorkerGroupDao.queryAssignedWorkerGroupNamesByProjectCode(projectCode);
        difference = SetUtils.difference(unauthorizedWorkerGroupNames, projectWorkerGroupNames);
        Date now = new Date();
        if (CollectionUtils.isNotEmpty(difference)) {
            Set<String> usedWorkerGroups = getAllUsedWorkerGroups(project);

            if (CollectionUtils.isNotEmpty(usedWorkerGroups) && usedWorkerGroups.containsAll(difference)) {
                throw new ServiceException(Status.USED_WORKER_GROUP_EXISTS,
                        SetUtils.intersection(usedWorkerGroups, difference).toSet());
            }

            boolean deleted =
                    projectWorkerGroupDao.deleteByProjectCodeAndWorkerGroups(projectCode, new ArrayList<>(difference));
            if (deleted) {
                log.info("Success to delete worker groups [{}] for the project [{}] .", difference, project.getName());
            } else {
                log.error("Failed to delete worker groups [{}] for the project [{}].", difference, project.getName());
                throw new ServiceException(Status.ASSIGN_WORKER_GROUP_TO_PROJECT_ERROR);
            }

            difference.forEach(workerGroupName -> {
                ProjectWorkerGroup projectWorkerGroup = new ProjectWorkerGroup();
                projectWorkerGroup.setProjectCode(projectCode);
                projectWorkerGroup.setWorkerGroup(workerGroupName);
                projectWorkerGroup.setCreateTime(now);
                projectWorkerGroup.setUpdateTime(now);
                int create = projectWorkerGroupDao.insert(projectWorkerGroup);
                if (create > 0) {
                    log.info("Success to add worker group [{}] for the project [{}] .", workerGroupName,
                            project.getName());
                } else {
                    log.error("Failed to add worker group [{}] for the project [{}].", workerGroupName,
                            project.getName());
                    throw new ServiceException(Status.ASSIGN_WORKER_GROUP_TO_PROJECT_ERROR);
                }
            });
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Map<String, Object> queryAssignedWorkerGroupsByProject(User loginUser, Long projectCode) {
        Map<String, Object> result = new HashMap<>();

        Project project = projectMapper.queryByCode(projectCode);
        // check project auth
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, null);
        if (!hasProjectAndPerm) {
            return result;
        }

        Set<String> assignedWorkerGroups = getAllUsedWorkerGroups(project);

        projectWorkerGroupDao.queryByProjectCode(projectCode)
                .forEach(projectWorkerGroup -> assignedWorkerGroups.add(projectWorkerGroup.getWorkerGroup()));

        List<ProjectWorkerGroup> projectWorkerGroups = assignedWorkerGroups.stream().map(workerGroup -> {
            ProjectWorkerGroup projectWorkerGroup = new ProjectWorkerGroup();
            projectWorkerGroup.setProjectCode(projectCode);
            projectWorkerGroup.setWorkerGroup(workerGroup);
            return projectWorkerGroup;
        }).distinct().collect(Collectors.toList());

        result.put(Constants.DATA_LIST, projectWorkerGroups);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private Set<String> getAllUsedWorkerGroups(Project project) {
        Set<String> usedWorkerGroups = new TreeSet<>();
        // query all worker groups that tasks depend on
        taskDefinitionDao.queryAllTaskDefinitionWorkerGroups(project.getCode()).forEach(workerGroupName -> {
            if (StringUtils.isNotEmpty(workerGroupName)) {
                usedWorkerGroups.add(workerGroupName);
            }
        });

        // query all worker groups that timings depend on
        scheduleMapper.querySchedulerListByProjectName(project.getName())
                .stream()
                .filter(schedule -> StringUtils.isNotEmpty(schedule.getWorkerGroup()))
                .forEach(schedule -> usedWorkerGroups.add(schedule.getWorkerGroup()));

        return usedWorkerGroups;
    }

}
