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
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.TriFunction;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.DefinitionGroupByUser;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private ProcessTaskRelationMapper relationMapper;

    /**
     * statistical task instance status data
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param startDate   start date
     * @param endDate     end date
     * @return task state count data
     */
    @Override
    public TaskCountDto countTaskStateByProject(User loginUser, long projectCode, String startDate,
                                                String endDate) {

        return countStateByProject(
                loginUser,
                projectCode,
                startDate,
                endDate,
                this::countTaskInstanceAllStatesByProjectCodes);
    }

    /**
     * statistical process instance status data
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param startDate   start date
     * @param endDate     end date
     * @return process instance state count data
     */
    @Override
    public TaskCountDto countProcessInstanceStateByProject(User loginUser, long projectCode, String startDate,
                                                           String endDate) {
        TaskCountDto taskCountDto = countStateByProject(
                loginUser,
                projectCode,
                startDate,
                endDate,
                (start, end, projectCodes) -> processInstanceMapper.countInstanceStateByProjectCodes(start, end,
                        projectCodes));

        // process state count needs to remove state of forced success
        if (taskCountDto != null) {
            taskCountDto.removeStateFromCountList(TaskExecutionStatus.FORCED_SUCCESS);
        }
        return taskCountDto;
    }

    /**
     * Wrapper function of counting process instance state and task state
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param startDate   start date
     * @param endDate     end date
     */
    private TaskCountDto countStateByProject(User loginUser,
                                             long projectCode,
                                             String startDate,
                                             String endDate,
                                             TriFunction<Date, Date, Long[], List<ExecuteStatusCount>> instanceStateCounter) {
        if (projectCode != 0) {
            projectService.checkProjectAndAuthThrowException(loginUser, projectCode, PROJECT_OVERVIEW);
        }

        Date start = null;
        Date end = null;
        if (!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
            start = DateUtils.stringToDate(startDate);
            end = DateUtils.stringToDate(endDate);
            if (Objects.isNull(start) || Objects.isNull(end)) {
                throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
            }
        }
        Pair<Set<Integer>, TaskCountDto> projectIds = getProjectIds(loginUser);
        if (projectIds.getRight() != null) {
            return projectIds.getRight();
        }
        Long[] projectCodeArray =
                projectCode == 0 ? getProjectCodesArrays(projectIds.getLeft()) : new Long[]{projectCode};
        List<ExecuteStatusCount> processInstanceStateCounts = new ArrayList<>();

        if (projectCodeArray.length != 0 || loginUser.getUserType() == UserType.ADMIN_USER) {
            processInstanceStateCounts = instanceStateCounter.apply(start, end, projectCodeArray);
        }

        if (processInstanceStateCounts != null) {
            return new TaskCountDto(processInstanceStateCounts);
        }
        return null;
    }

    /**
     * statistics the process definition quantities of a certain person
     * <p>
     * We only need projects which users have permission to see to determine whether the definition belongs to the user or not.
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return definition count data
     */
    @Override
    public DefineUserDto countDefinitionByUser(User loginUser, long projectCode) {
        if (projectCode != 0) {
            projectService.checkProjectAndAuthThrowException(loginUser, projectCode, PROJECT_OVERVIEW);
        }

        // todo: refactor this method, don't use Pair
        Pair<Set<Integer>, TaskCountDto> projectIds = getProjectIds(loginUser);
        if (projectIds.getRight() != null) {
            List<DefinitionGroupByUser> emptyList = new ArrayList<>();
            return new DefineUserDto(emptyList);
        }

        List<DefinitionGroupByUser> defineGroupByUsers = new ArrayList<>();
        Long[] projectCodeArray =
                projectCode == 0 ? getProjectCodesArrays(projectIds.getLeft()) : new Long[]{projectCode};
        if (projectCodeArray.length != 0 || loginUser.getUserType() == UserType.ADMIN_USER) {
            defineGroupByUsers = processDefinitionMapper.countDefinitionByProjectCodes(projectCodeArray);
        }

        return new DefineUserDto(defineGroupByUsers);
    }

    /**
     * statistical command status data
     *
     * @param loginUser login user
     * @return command state count data
     */
    @Override
    public List<CommandStateCount> countCommandState(User loginUser) {

        /**
         * find all the task lists in the project under the user
         * statistics based on task status execution, failure, completion, wait, total
         */
        Date start = null;
        Date end = null;
        Pair<Set<Integer>, TaskCountDto> projectIds = getProjectIds(loginUser);
        if (projectIds.getRight() != null) {
            List<CommandStateCount> noData = Arrays.stream(CommandType.values())
                    .map(commandType -> new CommandStateCount(0, 0, commandType))
                    .collect(Collectors.toList());
            return noData;
        }
        Long[] projectCodeArray = getProjectCodesArrays(projectIds.getLeft());
        // count normal command state
        Map<CommandType, Integer> normalCountCommandCounts =
                commandMapper.countCommandState(start, end, projectCodeArray)
                        .stream()
                        .collect(Collectors.toMap(CommandCount::getCommandType, CommandCount::getCount));

        // count error command state
        Map<CommandType, Integer> errorCommandCounts =
                errorCommandMapper.countCommandState(start, end, projectCodeArray)
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

    private Pair<Set<Integer>, TaskCountDto> getProjectIds(User loginUser) {
        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (projectIds.isEmpty()) {
            List<ExecuteStatusCount> taskInstanceStateCounts = new ArrayList<>();
            return Pair.of(null, new TaskCountDto(taskInstanceStateCounts));
        }
        return Pair.of(projectIds, null);
    }

    private Long[] getProjectCodesArrays(Set<Integer> projectIds) {
        List<Project> projects = projectMapper.selectBatchIds(projectIds);
        List<Long> codeList = projects.stream().map(Project::getCode).collect(Collectors.toList());
        Long[] projectCodeArray = new Long[codeList.size()];
        codeList.toArray(projectCodeArray);
        return projectCodeArray;
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
    public List<ExecuteStatusCount> countTaskInstanceAllStatesByProjectCodes(Date startTime, Date endTime,
                                                                             Long[] projectCodes) {
        Optional<List<ExecuteStatusCount>> startTimeStates = Optional.ofNullable(
                this.taskInstanceMapper.countTaskInstanceStateByProjectCodes(startTime, endTime, projectCodes));

        List<TaskExecutionStatus> allState = Arrays.stream(TaskExecutionStatus.values()).collect(Collectors.toList());
        List<TaskExecutionStatus> needRecountState;
        if (startTimeStates.isPresent() && startTimeStates.get().size() != 0) {
            List<TaskExecutionStatus> instanceState =
                    startTimeStates.get().stream().map(ExecuteStatusCount::getState).collect(Collectors.toList());
            // value 0 state need to recount by submit time
            needRecountState =
                    allState.stream().filter(ele -> !instanceState.contains(ele)).collect(Collectors.toList());
            if (needRecountState.size() == 0) {
                return startTimeStates.get();
            }
        } else {
            needRecountState = allState;
        }

        // use submit time to recount when 0
        // if have any issues with this code, should change to specified states 0 8 9 17 not state count is 0
        List<ExecuteStatusCount> recounts = this.taskInstanceMapper
                .countTaskInstanceStateByProjectCodesAndStatesBySubmitTime(startTime, endTime, projectCodes,
                        needRecountState);
        startTimeStates.orElseGet(ArrayList::new).addAll(recounts);

        return startTimeStates.orElse(null);
    }
    /**
     * query all workflow count
     *
     * @param loginUser login user
     * @return workflow count
     */
    @Override
    public Map<String, Object> queryAllWorkflowCounts(User loginUser) {
        Map<String, Object> result = new HashMap<>();
        int count = 0;
        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (!projectIds.isEmpty()) {
            List<Project> projects = projectMapper.selectBatchIds(projectIds);
            List<Long> projectCodes = projects.stream().map(project -> project.getCode()).collect(Collectors.toList());
            count = projectMapper.queryAllWorkflowCounts(projectCodes);
        }
        // todo: refactor this method, don't use Map
        result.put("data", "AllWorkflowCounts = " + count);
        putMsg(result, Status.SUCCESS);
        return result;
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

    /**
     * statistics the process definition quantities of a certain person
     * <p>
     * We only need projects which users have permission to see to determine whether the definition belongs to the user or not.
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @return definition count data
     */
    @Override
    public DefineUserDto countDefinitionByUserV2(User loginUser, Long projectCode, Integer userId,
                                                 Integer releaseState) {
        if (null != projectCode) {
            Project project = projectMapper.queryByCode(projectCode);
            projectService.checkProjectAndAuth(loginUser, project, projectCode, PROJECT_OVERVIEW);
        }

        List<DefinitionGroupByUser> defineGroupByUsers = new ArrayList<>();
        Pair<Set<Integer>, TaskCountDto> projectIds = getProjectIds(loginUser);
        if (projectIds.getRight() != null) {
            List<DefinitionGroupByUser> emptyList = new ArrayList<>();
            return new DefineUserDto(emptyList);
        }
        Long[] projectCodeArray =
                projectCode == null ? getProjectCodesArrays(projectIds.getLeft()) : new Long[]{projectCode};
        if (projectCodeArray.length != 0 || loginUser.getUserType() == UserType.ADMIN_USER) {
            defineGroupByUsers =
                    processDefinitionMapper.countDefinitionByProjectCodesV2(projectCodeArray, userId, releaseState);
        }

        return new DefineUserDto(defineGroupByUsers);
    }

    @Override
    public Long getProjectCodeByName(String projectName) {
        Project project = projectMapper.queryByName(projectName);
        return project == null ? 0 : project.getCode();
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
