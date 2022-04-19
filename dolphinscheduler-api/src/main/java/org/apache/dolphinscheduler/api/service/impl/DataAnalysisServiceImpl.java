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

import org.apache.dolphinscheduler.api.dto.CommandStateCount;
import org.apache.dolphinscheduler.api.dto.DefineUserDto;
import org.apache.dolphinscheduler.api.dto.TaskCountDto;
import org.apache.dolphinscheduler.api.dto.TaskStateCount;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.DataAnalysisService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.TriFunction;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.DefinitionGroupByUser;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * data analysis service impl
 */
@Service
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
    private ProcessService processService;

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
    public Map<String, Object> countTaskStateByProject(User loginUser, long projectCode, String startDate, String endDate) {

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
    public Map<String, Object> countProcessInstanceStateByProject(User loginUser, long projectCode, String startDate, String endDate) {
        Map<String, Object> result = this.countStateByProject(
                loginUser,
                projectCode,
                startDate,
                endDate,
                (start, end, projectCodes) -> this.processInstanceMapper.countInstanceStateByProjectCodes(start, end, projectCodes));
        // process state count needs to remove state of forced success
        if (result.containsKey(Constants.STATUS) && result.get(Constants.STATUS).equals(Status.SUCCESS)) {
            ((TaskCountDto) result.get(Constants.DATA_LIST)).removeStateFromCountList(ExecutionStatus.FORCED_SUCCESS);
        }
        return result;
    }

    /**
     * Wrapper function of counting process instance state and task state
     *
     * @param loginUser   login user
     * @param projectCode project code
     * @param startDate   start date
     * @param endDate     end date
     */
    private Map<String, Object> countStateByProject(User loginUser, long projectCode, String startDate, String endDate
            , TriFunction<Date, Date, Long[], List<ExecuteStatusCount>> instanceStateCounter) {
        Map<String, Object> result = new HashMap<>();

        if (projectCode != 0) {
            Project project = projectMapper.queryByCode(projectCode);
            result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
            if (result.get(Constants.STATUS) != Status.SUCCESS) {
                return result;
            }
        }

        Date start = null;
        Date end = null;
        if (!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
            start = DateUtils.getScheduleDate(startDate);
            end = DateUtils.getScheduleDate(endDate);
            if (Objects.isNull(start) || Objects.isNull(end)) {
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
                return result;
            }
        }

        Long[] projectCodeArray = projectCode == 0 ? getProjectCodesArrays(loginUser)
                : new Long[]{projectCode};
        List<ExecuteStatusCount> processInstanceStateCounts = new ArrayList<>();

        if (projectCodeArray.length != 0 || loginUser.getUserType() == UserType.ADMIN_USER) {
            processInstanceStateCounts = instanceStateCounter.apply(start, end, projectCodeArray);
        }

        if (processInstanceStateCounts != null) {
            TaskCountDto taskCountResult = new TaskCountDto(processInstanceStateCounts);
            result.put(Constants.DATA_LIST, taskCountResult);
            putMsg(result, Status.SUCCESS);
        }
        return result;
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
    public Map<String, Object> countDefinitionByUser(User loginUser, long projectCode) {
        Map<String, Object> result = new HashMap<>();

        if (projectCode != 0) {
            Project project = projectMapper.queryByCode(projectCode);
            result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
            if (result.get(Constants.STATUS) != Status.SUCCESS) {
                return result;
            }
        }

        List<DefinitionGroupByUser> defineGroupByUsers = new ArrayList<>();
        Long[] projectCodeArray = projectCode == 0 ? getProjectCodesArrays(loginUser)
                : new Long[]{projectCode};
        if (projectCodeArray.length != 0 || loginUser.getUserType() == UserType.ADMIN_USER) {
            defineGroupByUsers = processDefinitionMapper.countDefinitionByProjectCodes(projectCodeArray);
        }

        DefineUserDto dto = new DefineUserDto(defineGroupByUsers);
        result.put(Constants.DATA_LIST, dto);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * statistical command status data
     *
     * @param loginUser login user
     * @return command state count data
     */
    @Override
    public Map<String, Object> countCommandState(User loginUser) {
        Map<String, Object> result = new HashMap<>();

        /**
         * find all the task lists in the project under the user
         * statistics based on task status execution, failure, completion, wait, total
         */
        Date start = null;
        Date end = null;
        Long[] projectCodeArray = getProjectCodesArrays(loginUser);

        // admin can view all
        int userId = loginUser.getUserType() == UserType.ADMIN_USER ? 0 : loginUser.getId();

        // count normal command state
        Map<CommandType, Integer> normalCountCommandCounts = commandMapper.countCommandState(userId, start, end, projectCodeArray)
                .stream()
                .collect(Collectors.toMap(CommandCount::getCommandType, CommandCount::getCount));

        // count error command state
        Map<CommandType, Integer> errorCommandCounts = errorCommandMapper.countCommandState(userId, start, end, projectCodeArray)
                .stream()
                .collect(Collectors.toMap(CommandCount::getCommandType, CommandCount::getCount));

        List<CommandStateCount> list = Arrays.stream(CommandType.values())
                .map(commandType -> new CommandStateCount(
                        errorCommandCounts.getOrDefault(commandType, 0),
                        normalCountCommandCounts.getOrDefault(commandType, 0),
                        commandType)
                ).collect(Collectors.toList());

        result.put(Constants.DATA_LIST, list);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private Long[] getProjectCodesArrays(User loginUser) {
        List<Project> projectList = projectMapper.queryRelationProjectListByUserId(
                loginUser.getUserType() == UserType.ADMIN_USER ? 0 : loginUser.getId());
        Set<Long> projectCodes = new HashSet<>();
        projectList.forEach(project -> projectCodes.add(project.getCode()));
        if (loginUser.getUserType() == UserType.GENERAL_USER) {
            List<Project> createProjects = projectMapper.queryProjectCreatedByUser(loginUser.getId());
            createProjects.forEach(project -> projectCodes.add(project.getCode()));
        }
        return projectCodes.toArray(new Long[0]);
    }

    /**
     * count queue state
     *
     * @return queue state count data
     */
    @Override
    public Map<String, Object> countQueueState(User loginUser) {
        Map<String, Object> result = new HashMap<>();

        //TODO need to add detail data info
        Map<String, Integer> dataMap = new HashMap<>();
        dataMap.put("taskQueue", 0);
        dataMap.put("taskKill", 0);
        result.put(Constants.DATA_LIST, dataMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public List<ExecuteStatusCount> countTaskInstanceAllStatesByProjectCodes(Date startTime, Date endTime, Long[] projectCodes) {
        Optional<List<ExecuteStatusCount>> startTimeStates = Optional.ofNullable(this.taskInstanceMapper.countTaskInstanceStateByProjectCodes(startTime, endTime, projectCodes));

        List<ExecutionStatus> allState = Arrays.stream(ExecutionStatus.values()).collect(Collectors.toList());
        List<ExecutionStatus> needRecountState;
        if (startTimeStates.isPresent() && startTimeStates.get().size() != 0) {
            List<ExecutionStatus> instanceState = startTimeStates.get().stream().map(ExecuteStatusCount::getExecutionStatus).collect(Collectors.toList());
            //value 0 state need to recount by submit time
            needRecountState = allState.stream().filter(ele -> !instanceState.contains(ele)).collect(Collectors.toList());
            if (needRecountState.size() == 0) {
                return startTimeStates.get();
            }
        } else {
            needRecountState = allState;
        }

        //use submit time to recount when 0
        //if have any issues with this code, should change to specified states 0 8 9 17 not state count is 0
        List<ExecuteStatusCount> recounts = this.taskInstanceMapper
                .countTaskInstanceStateByProjectCodesAndStatesBySubmitTime(startTime, endTime, projectCodes, needRecountState);
        startTimeStates.orElseGet(ArrayList::new).addAll(recounts);

        return startTimeStates.orElse(null);
    }
}
