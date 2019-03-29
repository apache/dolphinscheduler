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
package cn.escheduler.api.service;


import cn.escheduler.api.dto.DefineUserDto;
import cn.escheduler.api.dto.TaskCountDto;
import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.common.utils.DateUtils;
import cn.escheduler.dao.mapper.ProcessDefinitionMapper;
import cn.escheduler.dao.mapper.ProcessInstanceMapper;
import cn.escheduler.dao.mapper.ProjectMapper;
import cn.escheduler.dao.mapper.TaskInstanceMapper;
import cn.escheduler.dao.model.DefinitionGroupByUser;
import cn.escheduler.dao.model.ExecuteStatusCount;
import cn.escheduler.dao.model.Project;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * data analysis service
 */
@Service
public class DataAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(DataAnalysisService.class);

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ProjectService projectService;

    @Autowired
    TaskInstanceMapper taskInstanceMapper;

    @Autowired
    ProcessInstanceMapper processInstanceMapper;

    @Autowired
    ProcessDefinitionMapper processDefinitionMapper;

    /**
     * statistical task instance status data
     *
     * @param loginUser
     * @param projectId
     * @param startDate
     * @param endDate
     * @return
     */
    public Map<String,Object> countTaskStateByProject(User loginUser, int projectId, String startDate, String endDate) {

        Map<String, Object> result = new HashMap<>(5);
        if(projectId != 0){
            Project project = projectMapper.queryById(projectId);
            result = projectService.checkProjectAndAuth(loginUser, project, String.valueOf(projectId));

            if (getResultStatus(result)){
                return result;
            }
        }

        /**
         * find all the task lists in the project under the user
         * statistics based on task status execution, failure, completion, wait, total
         */
        Date start = null;
        Date end = null;

        try {
            start = DateUtils.getScheduleDate(startDate);
            end = DateUtils.getScheduleDate(endDate);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            putErrorRequestParamsMsg(result);
            return result;
        }

        List<ExecuteStatusCount> taskInstanceStateCounts =
                taskInstanceMapper.countTaskInstanceStateByUser(loginUser.getId(),
                       loginUser.getUserType(),  start, end, projectId);

        TaskCountDto taskCountResult = new TaskCountDto(taskInstanceStateCounts);
        if (taskInstanceStateCounts != null) {
            result.put(Constants.DATA_LIST, taskCountResult);
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.TASK_INSTANCE_STATE_COUNT_ERROR);
        }
        return  result;
    }

    private void putErrorRequestParamsMsg(Map<String, Object> result) {
        result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
        result.put(Constants.MSG, MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), "startDate,endDate"));
    }

    /**
     * statistical process instance status data
     *
     * @param loginUser
     * @param projectId
     * @param startDate
     * @param endDate
     * @return
     */
    public Map<String,Object> countProcessInstanceStateByProject(User loginUser, int projectId, String startDate, String endDate) {

        Map<String, Object> result = new HashMap<>(5);
        if(projectId != 0){
            Project project = projectMapper.queryById(projectId);
            result = projectService.checkProjectAndAuth(loginUser, project, String.valueOf(projectId));

            if (getResultStatus(result)){
                return result;
            }
        }

        Date start = null;
        Date end = null;
        try {
            start = DateUtils.getScheduleDate(startDate);
            end = DateUtils.getScheduleDate(endDate);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            putErrorRequestParamsMsg(result);
            return result;
        }
        List<ExecuteStatusCount> processInstanceStateCounts =
                processInstanceMapper.countInstanceStateByUser(loginUser.getId(),
                        loginUser.getUserType(), start, end, projectId );

        TaskCountDto taskCountResult = new TaskCountDto(processInstanceStateCounts);
        if (processInstanceStateCounts != null) {
            result.put(Constants.DATA_LIST, taskCountResult);
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.COUNT_PROCESS_INSTANCE_STATE_ERROR);
        }
        return  result;
    }


    /**
     * statistics the process definition quantities of certain person
     *
     * @param loginUser
     * @param projectId
     * @return
     */
    public Map<String,Object> countDefinitionByUser(User loginUser, int projectId) {
        Map<String, Object> result = new HashMap<>();

        List<DefinitionGroupByUser> defineGroupByUsers = processDefinitionMapper.countDefinitionGroupByUser(loginUser.getId(), loginUser.getUserType(), projectId);

        DefineUserDto dto = new DefineUserDto(defineGroupByUsers);
        result.put(Constants.DATA_LIST, dto);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     *
     * @param result
     * @param status
     */
    private void putMsg(Map<String, Object> result, Status status) {
        result.put(Constants.STATUS, status);
        result.put(Constants.MSG, status.getMsg());
    }

    /**
     * get result status
     * @param result
     * @return
     */
    private boolean getResultStatus(Map<String, Object> result) {
        Status resultEnum = (Status) result.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return true;
        }
        return false;
    }
}
