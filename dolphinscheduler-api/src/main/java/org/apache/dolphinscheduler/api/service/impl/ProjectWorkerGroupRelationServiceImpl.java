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
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectWorkerGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectWorkerGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * task definition service impl
 */
@Service
@Slf4j
public class ProjectWorkerGroupRelationServiceImpl extends BaseServiceImpl
        implements
            ProjectWorkerGroupRelationService {

    @Autowired
    private ProjectWorkerGroupMapper projectWorkerGroupMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private WorkerGroupMapper workerGroupMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProjectService projectService;

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
            putMsg(result, Status.WORKER_GROUP_TO_PROJECT_IS_EMPTY);
            return result;
        }

        Project project = projectMapper.queryByCode(projectCode);
        if (Objects.isNull(project)) {
            putMsg(result, Status.PROJECT_NOT_EXIST);
            return result;
        }

        Set<String> workerGroupNames =
                workerGroupMapper.queryAllWorkerGroup().stream().map(WorkerGroup::getName).collect(
                        Collectors.toSet());

        workerGroupNames.add(WorkerGroupUtils.getDefaultWorkerGroup());

        Set<String> assignedWorkerGroupNames = new HashSet<>(workerGroups);

        Set<String> difference = SetUtils.difference(assignedWorkerGroupNames, workerGroupNames);

        if (!difference.isEmpty()) {
            putMsg(result, Status.WORKER_GROUP_NOT_EXIST, difference.toString());
            return result;
        }

        Set<String> projectWorkerGroupNames = projectWorkerGroupMapper.selectList(new QueryWrapper<ProjectWorkerGroup>()
                .lambda()
                .eq(ProjectWorkerGroup::getProjectCode, projectCode))
                .stream()
                .map(ProjectWorkerGroup::getWorkerGroup)
                .collect(Collectors.toSet());

        difference = SetUtils.difference(projectWorkerGroupNames, assignedWorkerGroupNames);

        if (CollectionUtils.isNotEmpty(difference)) {
            Set<String> usedWorkerGroups = getAllUsedWorkerGroups(project);

            if (CollectionUtils.isNotEmpty(usedWorkerGroups) && usedWorkerGroups.containsAll(difference)) {
                throw new ServiceException(Status.USED_WORKER_GROUP_EXISTS,
                        SetUtils.intersection(usedWorkerGroups, difference).toSet());
            }

            int deleted = projectWorkerGroupMapper.delete(
                    new QueryWrapper<ProjectWorkerGroup>().lambda().eq(ProjectWorkerGroup::getProjectCode, projectCode)
                            .in(ProjectWorkerGroup::getWorkerGroup, difference));
            if (deleted > 0) {
                log.info("Success to delete worker groups [{}] for the project [{}] .", difference, project.getName());
            } else {
                log.error("Failed to delete worker groups [{}] for the project [{}].", difference, project.getName());
                throw new ServiceException(Status.ASSIGN_WORKER_GROUP_TO_PROJECT_ERROR);
            }
        }

        difference = SetUtils.difference(assignedWorkerGroupNames, projectWorkerGroupNames);
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

    /**
     * query worker groups that assigned to the project
     *
     * @param projectCode project code
     */
    @Override
    public Map<String, Object> queryWorkerGroupsByProject(User loginUser, Long projectCode) {
        Map<String, Object> result = new HashMap<>();

        Project project = projectMapper.queryByCode(projectCode);
        // check project auth
        boolean hasProjectAndPerm = projectService.hasProjectAndPerm(loginUser, project, result, null);
        if (!hasProjectAndPerm) {
            return result;
        }

        Set<String> assignedWorkerGroups = getAllUsedWorkerGroups(project);

        projectWorkerGroupMapper.selectList(
                new QueryWrapper<ProjectWorkerGroup>().lambda().eq(ProjectWorkerGroup::getProjectCode, projectCode))
                .stream().forEach(projectWorkerGroup -> assignedWorkerGroups.add(projectWorkerGroup.getWorkerGroup()));

        List<ProjectWorkerGroup> projectWorkerGroups = assignedWorkerGroups.stream().map(workerGroup -> {
            ProjectWorkerGroup projectWorkerGroup = new ProjectWorkerGroup();
            projectWorkerGroup.setProjectCode(projectCode);
            projectWorkerGroup.setWorkerGroup(workerGroup);
            return projectWorkerGroup;
        }).collect(Collectors.toList());

        result.put(Constants.DATA_LIST, projectWorkerGroups);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private Set<String> getAllUsedWorkerGroups(Project project) {
        Set<String> usedWorkerGroups = new TreeSet<>();
        // query all worker groups that tasks depend on
        taskDefinitionMapper.queryAllDefinitionList(project.getCode()).stream().forEach(taskDefinition -> {
            if (StringUtils.isNotEmpty(taskDefinition.getWorkerGroup())) {
                usedWorkerGroups.add(taskDefinition.getWorkerGroup());
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
