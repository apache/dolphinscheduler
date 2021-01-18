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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * task instance service
 */
@Service
public class TaskInstanceService extends BaseService {

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProcessService processService;

    @Autowired
    TaskInstanceMapper taskInstanceMapper;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    UsersService usersService;

    /**
     * query task list by project, process instance, task name, task start time, task end time, task status, keyword paging
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @param searchVal search value
     * @param taskName task name
     * @param stateType state type
     * @param host host
     * @param startDate start time
     * @param endDate end time
     * @param pageNo page number
     * @param pageSize page size
     * @return task list page
     */
    public Map<String, Object> queryTaskListPaging(User loginUser, String projectName,
                                                   Integer processInstanceId, String processInstanceName, String taskName, String executorName, String startDate,
                                                   String endDate, String searchVal, ExecutionStatus stateType, String host,
                                                   Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (status != Status.SUCCESS) {
            return checkResult;
        }

        int[] statusArray = null;
        if (stateType != null) {
            statusArray = new int[]{stateType.ordinal()};
        }

        Date start = null;
        Date end = null;
        if (StringUtils.isNotEmpty(startDate)) {
            start = DateUtils.getScheduleDate(startDate);
            if (start == null) {
                return generateInvalidParamRes(result, "startDate");
            }
        }
        if (StringUtils.isNotEmpty(endDate)) {
            end = DateUtils.getScheduleDate(endDate);
            if (end == null) {
                return generateInvalidParamRes(result, "endDate");
            }
        }

        Page<TaskInstance> page = new Page(pageNo, pageSize);
        PageInfo pageInfo = new PageInfo<TaskInstance>(pageNo, pageSize);
        int executorId = usersService.getUserIdByName(executorName);

        IPage<TaskInstance> taskInstanceIPage = taskInstanceMapper.queryTaskInstanceListPaging(
                page, project.getId(), processInstanceId, processInstanceName, searchVal, taskName, executorId, statusArray, host, start, end
        );
        Set<String> exclusionSet = new HashSet<>();
        exclusionSet.add(Constants.CLASS);
        exclusionSet.add("taskJson");
        List<TaskInstance> taskInstanceList = taskInstanceIPage.getRecords();

        for (TaskInstance taskInstance : taskInstanceList) {
            taskInstance.setDuration(DateUtils.differSec(taskInstance.getStartTime(), taskInstance.getEndTime()));
            User executor = usersService.queryUser(taskInstance.getExecutorId());
            if (null != executor) {
                taskInstance.setExecutorName(executor.getUserName());
            }
        }
        pageInfo.setTotalCount((int) taskInstanceIPage.getTotal());
        pageInfo.setLists(CollectionUtils.getListByExclusion(taskInstanceIPage.getRecords(), exclusionSet));
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * change one task instance's state from failure to forced success
     *
     * @param loginUser      login user
     * @param projectName    project name
     * @param taskInstanceId task instance id
     * @return the result code and msg
     */
    public Map<String, Object> forceTaskSuccess(User loginUser, String projectName, Integer taskInstanceId) {
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        // check user auth
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (status != Status.SUCCESS) {
            return checkResult;
        }

        // check whether the task instance can be found
        TaskInstance task = taskInstanceMapper.selectById(taskInstanceId);
        if (task == null) {
            putMsg(result, Status.TASK_INSTANCE_NOT_FOUND);
            return result;
        }

        // check whether the task instance state type is failure
        if (!task.getState().typeIsFailure()) {
            putMsg(result, Status.TASK_INSTANCE_STATE_OPERATION_ERROR, taskInstanceId, task.getState().toString());
            return result;
        }

        // change the state of the task instance
        task.setState(ExecutionStatus.FORCED_SUCCESS);
        int changedNum = taskInstanceMapper.updateById(task);
        if (changedNum > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.FORCE_TASK_SUCCESS_ERROR);
        }

        return result;
    }

    /***
     * generate {@link org.apache.dolphinscheduler.api.enums.Status#REQUEST_PARAMS_NOT_VALID_ERROR} res with  param name
     * @param result exist result map
     * @param params invalid params name
     * @return update result map
     */
    private Map<String, Object> generateInvalidParamRes(Map<String, Object> result, String params) {
        result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
        result.put(Constants.MSG, MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), params));
        return result;
    }
}
