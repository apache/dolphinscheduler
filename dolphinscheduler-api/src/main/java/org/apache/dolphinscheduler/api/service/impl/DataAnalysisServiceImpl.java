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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.PROJECT_OVERVIEW;

import org.apache.dolphinscheduler.api.dto.CommandStateCount;
import org.apache.dolphinscheduler.api.service.DataAnalysisService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.vo.TaskInstanceCountVO;
import org.apache.dolphinscheduler.api.vo.WorkflowDefinitionCountVO;
import org.apache.dolphinscheduler.api.vo.WorkflowInstanceCountVO;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.ErrorCommand;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.model.TaskInstanceStatusCountDto;
import org.apache.dolphinscheduler.dao.model.WorkflowDefinitionCountDto;
import org.apache.dolphinscheduler.dao.model.WorkflowInstanceStatusCountDto;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

@Service
@Slf4j
public class DataAnalysisServiceImpl extends BaseServiceImpl implements DataAnalysisService {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private ErrorCommandMapper errorCommandMapper;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Override
    public TaskInstanceCountVO getTaskInstanceStateCountByProject(User loginUser,
                                                                  Long projectCode,
                                                                  String startDate,
                                                                  String endDate) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, PROJECT_OVERVIEW);
        Date start = startDate == null ? null : transformDate(startDate);
        Date end = endDate == null ? null : transformDate(endDate);
        List<TaskInstanceStatusCountDto> taskInstanceStatusCounts =
                taskInstanceDao.countTaskInstanceStateByProjectCodes(start, end, Lists.newArrayList(projectCode));
        return TaskInstanceCountVO.of(taskInstanceStatusCounts);
    }

    @Override
    public TaskInstanceCountVO getAllTaskInstanceStateCount(User loginUser,
                                                            String startDate,
                                                            String endDate) {
        List<Long> projectCodes = projectService.getAuthorizedProjectCodes(loginUser);
        if (CollectionUtils.isEmpty(projectCodes)) {
            return TaskInstanceCountVO.empty();
        }
        Date start = startDate == null ? null : transformDate(startDate);
        Date end = endDate == null ? null : transformDate(endDate);
        List<TaskInstanceStatusCountDto> taskInstanceStatusCounts =
                taskInstanceDao.countTaskInstanceStateByProjectCodes(start, end, projectCodes);
        return TaskInstanceCountVO.of(taskInstanceStatusCounts);
    }

    @Override
    public WorkflowInstanceCountVO getWorkflowInstanceStateCountByProject(User loginUser,
                                                                          Long projectCode,
                                                                          String startDate,
                                                                          String endDate) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, PROJECT_OVERVIEW);
        Date start = startDate == null ? null : transformDate(startDate);
        Date end = endDate == null ? null : transformDate(endDate);
        List<WorkflowInstanceStatusCountDto> workflowInstanceStatusCountDtos = workflowInstanceDao
                .countWorkflowInstanceStateByProjectCodes(start, end, Lists.newArrayList(projectCode));
        return WorkflowInstanceCountVO.of(workflowInstanceStatusCountDtos);
    }

    @Override
    public WorkflowInstanceCountVO getAllWorkflowInstanceStateCount(User loginUser,
                                                                    String startDate,
                                                                    String endDate) {
        List<Long> projectCodes = projectService.getAuthorizedProjectCodes(loginUser);
        if (CollectionUtils.isEmpty(projectCodes)) {
            return WorkflowInstanceCountVO.empty();
        }
        Date start = startDate == null ? null : transformDate(startDate);
        Date end = endDate == null ? null : transformDate(endDate);

        List<WorkflowInstanceStatusCountDto> workflowInstanceStatusCountDtos =
                workflowInstanceDao.countWorkflowInstanceStateByProjectCodes(start, end, projectCodes);
        return WorkflowInstanceCountVO.of(workflowInstanceStatusCountDtos);
    }

    @Override
    public WorkflowDefinitionCountVO getWorkflowDefinitionCountByProject(User loginUser, Long projectCode) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, PROJECT_OVERVIEW);
        List<WorkflowDefinitionCountDto> workflowDefinitionCounts =
                workflowDefinitionDao.countDefinitionByProjectCodes(Lists.newArrayList(projectCode));
        return WorkflowDefinitionCountVO.of(workflowDefinitionCounts);
    }

    @Override
    public WorkflowDefinitionCountVO getAllWorkflowDefinitionCount(User loginUser) {
        List<Long> projectCodes = projectService.getAuthorizedProjectCodes(loginUser);
        if (CollectionUtils.isEmpty(projectCodes)) {
            return WorkflowDefinitionCountVO.empty();
        }
        return WorkflowDefinitionCountVO.of(workflowDefinitionDao.countDefinitionByProjectCodes(projectCodes));
    }

    @Override
    public List<CommandStateCount> countCommandState(User loginUser) {

        List<Long> projectCodes = projectService.getAuthorizedProjectCodes(loginUser);

        // count normal command state
        Map<CommandType, Integer> normalCountCommandCounts =
                commandMapper.countCommandState(null, null, projectCodes)
                        .stream()
                        .collect(Collectors.toMap(CommandCount::getCommandType, CommandCount::getCount));

        // count error command state
        Map<CommandType, Integer> errorCommandCounts =
                errorCommandMapper.countCommandState(null, null, projectCodes)
                        .stream()
                        .collect(Collectors.toMap(CommandCount::getCommandType, CommandCount::getCount));

        List<CommandStateCount> list = Arrays.stream(CommandType.values())
                .map(commandType -> new CommandStateCount(
                        errorCommandCounts.getOrDefault(commandType, 0),
                        normalCountCommandCounts.getOrDefault(commandType, 0),
                        commandType))
                .collect(Collectors.toList());
        return list;
    }

    /**
     * count queue state
     *
     * @return queue state count data
     */
    @Override
    public Map<String, Integer> countQueueState(User loginUser) {

        // TODO need to add detail data info
        // todo: refactor this method, don't use Map
        Map<String, Integer> dataMap = new HashMap<>();
        dataMap.put("taskQueue", 0);
        dataMap.put("taskKill", 0);
        return dataMap;
    }

    @Override
    public PageInfo<Command> listPendingCommands(User loginUser, Long projectCode, Integer pageNo, Integer pageSize) {
        Page<Command> page = new Page<>(pageNo, pageSize);
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            IPage<Command> commandIPage = commandMapper.queryCommandPage(page);
            return PageInfo.of(commandIPage);
        }

        List<Long> workflowDefinitionCodes = getAuthDefinitionCodes(loginUser, projectCode);

        if (workflowDefinitionCodes.isEmpty()) {
            return PageInfo.of(pageNo, pageSize);
        }

        IPage<Command> commandIPage =
                commandMapper.queryCommandPageByIds(page, new ArrayList<>(workflowDefinitionCodes));
        return PageInfo.of(commandIPage);
    }

    @Override
    public PageInfo<ErrorCommand> listErrorCommand(User loginUser, Long projectCode, Integer pageNo, Integer pageSize) {
        Page<ErrorCommand> page = new Page<>(pageNo, pageSize);
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            IPage<ErrorCommand> commandIPage = errorCommandMapper.queryErrorCommandPage(page);
            return PageInfo.of(commandIPage);
        }

        List<Long> workflowDefinitionCodes = getAuthDefinitionCodes(loginUser, projectCode);

        if (workflowDefinitionCodes.isEmpty()) {
            return PageInfo.of(pageNo, pageSize);
        }

        IPage<ErrorCommand> commandIPage =
                errorCommandMapper.queryErrorCommandPageByIds(page, new ArrayList<>(workflowDefinitionCodes));
        return PageInfo.of(commandIPage);
    }

    private List<Long> getAuthDefinitionCodes(User loginUser, Long projectCode) {
        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (CollectionUtils.isEmpty(projectIds)) {
            return Collections.emptyList();
        }
        List<Long> projectCodes = projectDao.queryByIds(projectIds)
                .stream()
                .map(Project::getCode)
                .collect(Collectors.toList());

        if (projectCode != null) {
            if (!projectCodes.contains(projectCode)) {
                return Collections.emptyList();
            }

            projectCodes = Collections.singletonList(projectCode);
        }

        return workflowDefinitionDao.queryDefinitionCodeListByProjectCodes(projectCodes);
    }

}
