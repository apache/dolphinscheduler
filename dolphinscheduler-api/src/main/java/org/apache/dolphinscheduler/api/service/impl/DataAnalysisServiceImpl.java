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
import org.apache.dolphinscheduler.api.dto.DefineUserDto;
import org.apache.dolphinscheduler.api.dto.TaskCountDto;
import org.apache.dolphinscheduler.api.dto.project.StatisticsStateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.DataAnalysisService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.vo.TaskInstanceCountVO;
import org.apache.dolphinscheduler.api.vo.WorkflowDefinitionCountVO;
import org.apache.dolphinscheduler.api.vo.WorkflowInstanceCountVO;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.ErrorCommand;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.model.TaskInstanceStatusCountDto;
import org.apache.dolphinscheduler.dao.model.WorkflowDefinitionCountDto;
import org.apache.dolphinscheduler.dao.model.WorkflowInstanceStatusCountDto;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

/**
 * data analysis service impl
 */
@Service
@Slf4j
public class DataAnalysisServiceImpl extends BaseServiceImpl implements DataAnalysisService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private ErrorCommandMapper errorCommandMapper;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Override
    public TaskInstanceCountVO getTaskInstanceStateCountByProject(User loginUser,
                                                                  Long projectCode,
                                                                  String startDate,
                                                                  String endDate) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, PROJECT_OVERVIEW);
        Date start = startDate == null ? null : transformDate(startDate);
        Date end = endDate == null ? null : transformDate(endDate);
        List<TaskInstanceStatusCountDto> taskInstanceStatusCounts =
                taskInstanceMapper.countTaskInstanceStateByProjectCodes(start, end, Lists.newArrayList(projectCode));
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
                taskInstanceMapper.countTaskInstanceStateByProjectCodes(start, end, projectCodes);
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
        List<WorkflowInstanceStatusCountDto> workflowInstanceStatusCountDtos = processInstanceMapper
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
                processInstanceMapper.countWorkflowInstanceStateByProjectCodes(start, end, projectCodes);
        return WorkflowInstanceCountVO.of(workflowInstanceStatusCountDtos);
    }

    @Override
    public WorkflowDefinitionCountVO getWorkflowDefinitionCountByProject(User loginUser, Long projectCode) {
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, PROJECT_OVERVIEW);
        List<WorkflowDefinitionCountDto> workflowDefinitionCounts =
                processDefinitionMapper.countDefinitionByProjectCodes(Lists.newArrayList(projectCode));
        return WorkflowDefinitionCountVO.of(workflowDefinitionCounts);
    }

    @Override
    public WorkflowDefinitionCountVO getAllWorkflowDefinitionCount(User loginUser) {
        List<Long> projectCodes = projectService.getAuthorizedProjectCodes(loginUser);
        if (CollectionUtils.isEmpty(projectCodes)) {
            return WorkflowDefinitionCountVO.empty();
        }
        return WorkflowDefinitionCountVO.of(processDefinitionMapper.countDefinitionByProjectCodes(projectCodes));
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

    /**
     * query all workflow states count
     *
     * @param loginUser              login user
     * @param statisticsStateRequest statisticsStateRequest
     * @return workflow states count
     */
    @Override
    public TaskCountDto countWorkflowStates(User loginUser,
                                            StatisticsStateRequest statisticsStateRequest) {
        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (projectIds.isEmpty()) {
            return new TaskCountDto(Collections.emptyList());
        }

        String projectName = statisticsStateRequest.getProjectName();
        String workflowName = statisticsStateRequest.getWorkflowName();
        Long projectCode = statisticsStateRequest.getProjectCode();
        Long workflowCode = statisticsStateRequest.getWorkflowCode();
        Integer model = Constants.QUERY_ALL_ON_SYSTEM;

        if (!StringUtils.isBlank(projectName) || null != projectCode) {
            model = Constants.QUERY_ALL_ON_PROJECT;
        }
        if (!StringUtils.isBlank(workflowName) || null != workflowCode) {
            model = Constants.QUERY_ALL_ON_WORKFLOW;
        }
        try {
            if (null == workflowCode || null == projectCode) {
                projectCode = projectMapper.queryByName(projectName).getCode();
                workflowCode = processDefinitionMapper.queryByDefineName(projectCode, workflowName).getCode();
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        Date date = new Date();
        Date startTime = statisticsStateRequest.getStartTime() == null ? DateUtils.addMonths(date, -1)
                : statisticsStateRequest.getStartTime();
        Date endTime = statisticsStateRequest.getEndTime() == null ? date : statisticsStateRequest.getEndTime();

        List<ExecuteStatusCount> executeStatusCounts = processInstanceMapper.countInstanceStateV2(
                startTime, endTime, projectCode, workflowCode, model, projectIds);
        return new TaskCountDto(executeStatusCounts);
    }

    /**
     * query one workflow states count
     *
     * @param loginUser    login user
     * @param workflowCode workflowCode
     * @return workflow states count
     */
    @Override
    public TaskCountDto countOneWorkflowStates(User loginUser, Long workflowCode) {
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(workflowCode);
        if (processDefinition == null) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST, workflowCode);
        }
        projectService.checkHasProjectWritePermissionThrowException(loginUser, processDefinition.getProjectCode());

        List<ExecuteStatusCount> executeStatusCounts = processInstanceMapper.countInstanceStateV2(null, null, null,
                workflowCode, Constants.QUERY_ALL_ON_WORKFLOW, null);
        return new TaskCountDto(executeStatusCounts);
    }

    /**
     * query all task states count
     *
     * @param loginUser              login user
     * @param statisticsStateRequest statisticsStateRequest
     * @return tasks states count
     */
    @Override
    public TaskCountDto countTaskStates(User loginUser, StatisticsStateRequest statisticsStateRequest) {
        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (projectIds.isEmpty()) {
            return new TaskCountDto(Collections.emptyList());
        }
        String projectName = statisticsStateRequest.getProjectName();
        String workflowName = statisticsStateRequest.getWorkflowName();
        String taskName = statisticsStateRequest.getTaskName();
        Long projectCode = statisticsStateRequest.getProjectCode();
        Long workflowCode = statisticsStateRequest.getWorkflowCode();
        Long taskCode = statisticsStateRequest.getTaskCode();
        Integer model = Constants.QUERY_ALL_ON_SYSTEM;

        if (!StringUtils.isBlank(projectName) || null != projectCode) {
            model = Constants.QUERY_ALL_ON_PROJECT;
        }
        if (!StringUtils.isBlank(workflowName) || null != workflowCode) {
            model = Constants.QUERY_ALL_ON_WORKFLOW;
        }
        if (!StringUtils.isBlank(taskName) || null != taskCode) {
            model = Constants.QUERY_ALL_ON_TASK;
        }

        try {
            if (null == taskCode || null == workflowCode || null == projectCode) {
                projectCode = projectMapper.queryByName(projectName).getCode();
                workflowCode = processDefinitionMapper.queryByDefineName(projectCode, workflowName).getCode();
                // todo The comment can be canceled after repairing the duplicate taskname of the existing workflow
                // taskCode = relationMapper.queryTaskCodeByTaskName(workflowCode, taskName);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        Date date = new Date();
        Date startTime = statisticsStateRequest.getStartTime() == null ? DateUtils.addMonths(date, -1)
                : statisticsStateRequest.getStartTime();
        Date endTime = statisticsStateRequest.getEndTime() == null ? date : statisticsStateRequest.getEndTime();

        Optional<List<ExecuteStatusCount>> startTimeStates = Optional.ofNullable(
                taskInstanceMapper.countTaskInstanceStateByProjectIdsV2(startTime, endTime, projectIds));
        List<TaskExecutionStatus> needRecountState = setOptional(startTimeStates);
        if (needRecountState.size() == 0) {
            return new TaskCountDto(startTimeStates.get());
        }
        List<ExecuteStatusCount> recounts = this.taskInstanceMapper
                .countTaskInstanceStateByProjectCodesAndStatesBySubmitTimeV2(startTime, endTime, projectCode,
                        workflowCode, taskCode, model, projectIds,
                        needRecountState);
        startTimeStates.orElseGet(ArrayList::new).addAll(recounts);
        List<ExecuteStatusCount> executeStatusCounts = startTimeStates.orElse(null);
        return new TaskCountDto(executeStatusCounts);
    }

    /**
     * query one task states count
     *
     * @param loginUser login user
     * @param taskCode  taskCode
     * @return tasks states count
     */
    @Override
    public TaskCountDto countOneTaskStates(User loginUser, Long taskCode) {
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        long projectCode = taskDefinition.getProjectCode();
        Project project = projectMapper.queryByCode(projectCode);
        projectService.checkHasProjectWritePermissionThrowException(loginUser, project);

        Set<Integer> projectId = Collections.singleton(project.getId());
        Optional<List<ExecuteStatusCount>> startTimeStates = Optional.ofNullable(
                taskInstanceMapper.countTaskInstanceStateByProjectIdsV2(null, null, projectId));
        List<TaskExecutionStatus> needRecountState = setOptional(startTimeStates);
        if (needRecountState.size() == 0) {
            return new TaskCountDto(startTimeStates.get());
        }
        List<ExecuteStatusCount> recounts = this.taskInstanceMapper
                .countTaskInstanceStateByProjectCodesAndStatesBySubmitTimeV2(null, null, projectCode, null, taskCode,
                        Constants.QUERY_ALL_ON_TASK, projectId,
                        needRecountState);
        startTimeStates.orElseGet(ArrayList::new).addAll(recounts);
        List<ExecuteStatusCount> executeStatusCounts = startTimeStates.orElse(null);
        return new TaskCountDto(executeStatusCounts);
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
        List<Long> projectCodes = projectMapper.selectBatchIds(projectIds)
                .stream()
                .map(Project::getCode)
                .collect(Collectors.toList());

        if (projectCode != null) {
            if (!projectCodes.contains(projectCode)) {
                return Collections.emptyList();
            }

            projectCodes = Collections.singletonList(projectCode);
        }

        return processDefinitionMapper.queryDefinitionCodeListByProjectCodes(projectCodes);
    }

    /**
     * statistics the process definition quantities of a certain person
     * <p>
     * We only need projects which users have permission to see to determine whether the definition belongs to the user or not.
     *
     * @param loginUser login user
     * @return definition count data
     */
    @Override
    public DefineUserDto countDefinitionByUserV2(User loginUser,
                                                 Integer userId,
                                                 Integer releaseState) {
        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (CollectionUtils.isEmpty(projectIds)) {
            return new DefineUserDto(Collections.emptyList());
        }
        List<Long> projectCodes = projectMapper.selectBatchIds(projectIds)
                .stream()
                .map(Project::getCode)
                .collect(Collectors.toList());

        List<WorkflowDefinitionCountDto> workflowDefinitionCountDtos =
                processDefinitionMapper.countDefinitionByProjectCodesV2(projectCodes, userId, releaseState);

        return new DefineUserDto(workflowDefinitionCountDtos);
    }

    private List<TaskExecutionStatus> setOptional(Optional<List<ExecuteStatusCount>> startTimeStates) {
        List<TaskExecutionStatus> allState = Arrays.stream(TaskExecutionStatus.values()).collect(Collectors.toList());
        if (startTimeStates.isPresent() && startTimeStates.get().size() != 0) {
            List<TaskExecutionStatus> instanceState =
                    startTimeStates.get().stream().map(ExecuteStatusCount::getState).collect(Collectors.toList());
            // value 0 state need to recount by submit time
            return allState.stream().filter(ele -> !instanceState.contains(ele)).collect(Collectors.toList());
        } else {
            return allState;
        }
    }
}
