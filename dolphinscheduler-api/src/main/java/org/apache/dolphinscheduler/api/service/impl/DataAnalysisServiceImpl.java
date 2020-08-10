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
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.BaseService;
import org.apache.dolphinscheduler.api.service.DataAnalysisService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
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
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * data analysis service impl
 */
@Service
public class DataAnalysisServiceImpl extends BaseService implements DataAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(DataAnalysisServiceImpl.class);

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

    private static final String COMMAND_STATE = "commandState";

    private static final String ERROR_COMMAND_STATE = "errorCommandState";

    /**
     * statistical task instance status data
     *
     * @param loginUser login user
     * @param projectId project id
     * @param startDate start date
     * @param endDate end date
     * @return task state count data
     */
    public Map<String, Object> countTaskStateByProject(User loginUser, int projectId, String startDate, String endDate) {

        return countStateByProject(
                loginUser,
                projectId,
                startDate,
                endDate,
                (start, end, projectIds) -> this.taskInstanceMapper.countTaskInstanceStateByUser(start, end, projectIds));
    }

    /**
     * statistical process instance status data
     *
     * @param loginUser login user
     * @param projectId project id
     * @param startDate start date
     * @param endDate end date
     * @return process instance state count data
     */
    public Map<String, Object> countProcessInstanceStateByProject(User loginUser, int projectId, String startDate, String endDate) {
        return this.countStateByProject(
                loginUser,
                projectId,
                startDate,
                endDate,
                (start, end, projectIds) -> this.processInstanceMapper.countInstanceStateByUser(start, end, projectIds));
    }

    private Map<String, Object> countStateByProject(User loginUser, int projectId, String startDate, String endDate
            , TriFunction<Date, Date, Integer[], List<ExecuteStatusCount>> instanceStateCounter) {
        Map<String, Object> result = new HashMap<>(5);
        boolean checkProject = checkProject(loginUser, projectId, result);
        if (!checkProject) {
            return result;
        }

        Date start;
        Date end;
        try {
            start = DateUtils.getScheduleDate(startDate);
            end = DateUtils.getScheduleDate(endDate);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            putErrorRequestParamsMsg(result);
            return result;
        }
        Integer[] projectIdArray = getProjectIdsArrays(loginUser, projectId);
        List<ExecuteStatusCount> processInstanceStateCounts =
                instanceStateCounter.apply(start, end, projectIdArray);

        if (processInstanceStateCounts != null) {
            TaskCountDto taskCountResult = new TaskCountDto(processInstanceStateCounts);
            result.put(Constants.DATA_LIST, taskCountResult);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }


    /**
     * statistics the process definition quantities of certain person
     *
     * @param loginUser login user
     * @param projectId project id
     * @return definition count data
     */
    public Map<String, Object> countDefinitionByUser(User loginUser, int projectId) {
        Map<String, Object> result = new HashMap<>();


        Integer[] projectIdArray = getProjectIdsArrays(loginUser, projectId);
        List<DefinitionGroupByUser> defineGroupByUsers = processDefinitionMapper.countDefinitionGroupByUser(
                loginUser.getId(), projectIdArray, isAdmin(loginUser));

        DefineUserDto dto = new DefineUserDto(defineGroupByUsers);
        result.put(Constants.DATA_LIST, dto);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * statistical command status data
     *
     * @param loginUser login user
     * @param projectId project id
     * @param startDate start date
     * @param endDate end date
     * @return command state count data
     */
    public Map<String, Object> countCommandState(User loginUser, int projectId, String startDate, String endDate) {

        Map<String, Object> result = new HashMap<>(5);
        boolean checkProject = checkProject(loginUser, projectId, result);
        if (!checkProject) {
            return result;
        }

        /**
         * find all the task lists in the project under the user
         * statistics based on task status execution, failure, completion, wait, total
         */
        Date start = null;
        Date end = null;

        if (startDate != null && endDate != null) {
            try {
                start = DateUtils.getScheduleDate(startDate);
                end = DateUtils.getScheduleDate(endDate);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                putErrorRequestParamsMsg(result);
                return result;
            }
        }


        Integer[] projectIdArray = getProjectIdsArrays(loginUser, projectId);
        // count command state
        List<CommandCount> commandStateCounts =
                commandMapper.countCommandState(
                        loginUser.getId(),
                        start,
                        end,
                        projectIdArray);

        // count error command state
        List<CommandCount> errorCommandStateCounts =
                errorCommandMapper.countCommandState(
                        start, end, projectIdArray);

        // enumMap
        Map<CommandType, Map<String, Integer>> dataMap = new EnumMap<>(CommandType.class);

        Map<String, Integer> commonCommand = new HashMap<>();
        commonCommand.put(COMMAND_STATE, 0);
        commonCommand.put(ERROR_COMMAND_STATE, 0);


        // init data map
        /**
         * START_PROCESS, START_CURRENT_TASK_PROCESS, RECOVER_TOLERANCE_FAULT_PROCESS, RECOVER_SUSPENDED_PROCESS,
         START_FAILURE_TASK_PROCESS,COMPLEMENT_DATA,SCHEDULER, REPEAT_RUNNING,PAUSE,STOP,RECOVER_WAITTING_THREAD;
         */
        dataMap.put(CommandType.START_PROCESS, commonCommand);
        dataMap.put(CommandType.START_CURRENT_TASK_PROCESS, commonCommand);
        dataMap.put(CommandType.RECOVER_TOLERANCE_FAULT_PROCESS, commonCommand);
        dataMap.put(CommandType.RECOVER_SUSPENDED_PROCESS, commonCommand);
        dataMap.put(CommandType.START_FAILURE_TASK_PROCESS, commonCommand);
        dataMap.put(CommandType.COMPLEMENT_DATA, commonCommand);
        dataMap.put(CommandType.SCHEDULER, commonCommand);
        dataMap.put(CommandType.REPEAT_RUNNING, commonCommand);
        dataMap.put(CommandType.PAUSE, commonCommand);
        dataMap.put(CommandType.STOP, commonCommand);
        dataMap.put(CommandType.RECOVER_WAITTING_THREAD, commonCommand);

        // put command state
        for (CommandCount executeStatusCount : commandStateCounts) {
            Map<String, Integer> commandStateCountsMap = new HashMap<>(dataMap.get(executeStatusCount.getCommandType()));
            commandStateCountsMap.put(COMMAND_STATE, executeStatusCount.getCount());
            dataMap.put(executeStatusCount.getCommandType(), commandStateCountsMap);
        }

        // put error command state
        for (CommandCount errorExecutionStatus : errorCommandStateCounts) {
            Map<String, Integer> errorCommandStateCountsMap = new HashMap<>(dataMap.get(errorExecutionStatus.getCommandType()));
            errorCommandStateCountsMap.put(ERROR_COMMAND_STATE, errorExecutionStatus.getCount());
            dataMap.put(errorExecutionStatus.getCommandType(), errorCommandStateCountsMap);
        }

        List<CommandStateCount> list = new ArrayList<>();
        for (Map.Entry<CommandType, Map<String, Integer>> next : dataMap.entrySet()) {
            CommandStateCount commandStateCount = new CommandStateCount(next.getValue().get(ERROR_COMMAND_STATE),
                    next.getValue().get(COMMAND_STATE), next.getKey());
            list.add(commandStateCount);
        }

        result.put(Constants.DATA_LIST, list);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private Integer[] getProjectIdsArrays(User loginUser, int projectId) {
        List<Integer> projectIds = new ArrayList<>();
        if (projectId != 0) {
            projectIds.add(projectId);
        } else if (loginUser.getUserType() == UserType.GENERAL_USER) {
            projectIds = processService.getProjectIdListHavePerm(loginUser.getId());
            if (projectIds.isEmpty()) {
                projectIds.add(0);
            }
        }
        return projectIds.toArray(new Integer[0]);
    }

    /**
     * count queue state
     *
     * @param loginUser login user
     * @param projectId project id
     * @return queue state count data
     */
    public Map<String, Object> countQueueState(User loginUser, int projectId) {
        Map<String, Object> result = new HashMap<>(5);

        boolean checkProject = checkProject(loginUser, projectId, result);
        if (!checkProject) {
            return result;
        }

        // TODO tasksQueueList and tasksKillList is never updated.
        List<String> tasksQueueList = new ArrayList<>();
        List<String> tasksKillList = new ArrayList<>();

        Map<String, Integer> dataMap = new HashMap<>();
        if (loginUser.getUserType() == UserType.ADMIN_USER) {
            dataMap.put("taskQueue", tasksQueueList.size());
            dataMap.put("taskKill", tasksKillList.size());

            result.put(Constants.DATA_LIST, dataMap);
            putMsg(result, Status.SUCCESS);
            return result;
        }

        int[] tasksQueueIds = new int[tasksQueueList.size()];
        int[] tasksKillIds = new int[tasksKillList.size()];

        int i = 0;
        for (String taskQueueStr : tasksQueueList) {
            if (StringUtils.isNotEmpty(taskQueueStr)) {
                String[] splits = taskQueueStr.split("_");
                if (splits.length >= 4) {
                    tasksQueueIds[i++] = Integer.parseInt(splits[3]);
                }
            }
        }

        i = 0;
        for (String taskKillStr : tasksKillList) {
            if (StringUtils.isNotEmpty(taskKillStr)) {
                String[] splits = taskKillStr.split("-");
                if (splits.length == 2) {
                    tasksKillIds[i++] = Integer.parseInt(splits[1]);
                }
            }
        }
        Integer taskQueueCount = 0;
        Integer taskKillCount = 0;

        Integer[] projectIds = getProjectIdsArrays(loginUser, projectId);
        if (tasksQueueIds.length != 0) {
            taskQueueCount = taskInstanceMapper.countTask(
                    projectIds,
                    tasksQueueIds);
        }

        if (tasksKillIds.length != 0) {
            taskKillCount = taskInstanceMapper.countTask(projectIds, tasksKillIds);
        }

        dataMap.put("taskQueue", taskQueueCount);
        dataMap.put("taskKill", taskKillCount);

        result.put(Constants.DATA_LIST, dataMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private boolean checkProject(User loginUser, int projectId, Map<String, Object> result) {
        if (projectId != 0) {
            Project project = projectMapper.selectById(projectId);
            return projectService.hasProjectAndPerm(loginUser, project, result);
        }
        return true;
    }

    private void putErrorRequestParamsMsg(Map<String, Object> result) {
        result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
        result.put(Constants.MSG, MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), "startDate,endDate"));
    }
}
