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
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * task instance service impl
 */
@Service
public class TaskInstanceServiceImpl extends BaseServiceImpl implements TaskInstanceService {

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

    @Autowired
    TaskDefinitionMapper taskDefinitionMapper;

    /**
     * query task list by project, process instance, task name, task start time, task end time, task status, keyword paging
     *
     * @param loginUser login user
     * @param projectCode project code
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
    @Override
    public Result queryTaskListPaging(User loginUser,
                                                   long projectCode,
                                                   Integer processInstanceId,
                                                   String processInstanceName,
                                                   String taskName,
                                                   String executorName,
                                                   String startDate,
                                                   String endDate,
                                                   String searchVal,
                                                   ExecutionStatus stateType,
                                                   String host,
                                                   Integer pageNo,
                                                   Integer pageSize) {
        Result result = new Result();
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (status != Status.SUCCESS) {
            putMsg(result,status);
            return result;
        }
        int[] statusArray = null;
        if (stateType != null) {
            statusArray = new int[]{stateType.ordinal()};
        }
        Map<String, Object> checkAndParseDateResult = checkAndParseDateParameters(startDate, endDate);
        status = (Status) checkAndParseDateResult.get(Constants.STATUS);
        if (status != Status.SUCCESS) {
            putMsg(result,status);
            return result;
        }
        Date start = (Date) checkAndParseDateResult.get(Constants.START_TIME);
        Date end = (Date) checkAndParseDateResult.get(Constants.END_TIME);
        Page<TaskInstance> page = new Page<>(pageNo, pageSize);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(pageNo, pageSize);
        int executorId = usersService.getUserIdByName(executorName);
        IPage<TaskInstance> taskInstanceIPage = taskInstanceMapper.queryTaskInstanceListPaging(
            page, project.getCode(), processInstanceId, processInstanceName, searchVal, taskName, executorId, statusArray, host, start, end
        );
        Set<String> exclusionSet = new HashSet<>();
        exclusionSet.add(Constants.CLASS);
        exclusionSet.add("taskJson");
        List<TaskInstance> taskInstanceList = taskInstanceIPage.getRecords();
        List<Integer> executorIds = taskInstanceList.stream().map(TaskInstance::getExecutorId).distinct().collect(Collectors.toList());
        List<User> users = usersService.queryUser(executorIds);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(User::getId, v -> v));
        for (TaskInstance taskInstance : taskInstanceList) {
            taskInstance.setDuration(DateUtils.format2Duration(taskInstance.getStartTime(), taskInstance.getEndTime()));
            User user = userMap.get(taskInstance.getExecutorId());
            if (user != null) {
                taskInstance.setExecutorName(user.getUserName());
            }
        }
        pageInfo.setTotal((int) taskInstanceIPage.getTotal());
        pageInfo.setTotalList(CollectionUtils.getListByExclusion(taskInstanceIPage.getRecords(), exclusionSet));
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * change one task instance's state from failure to forced success
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskInstanceId task instance id
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> forceTaskSuccess(User loginUser, long projectCode, Integer taskInstanceId) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        // check whether the task instance can be found
        TaskInstance task = taskInstanceMapper.selectById(taskInstanceId);
        if (task == null) {
            putMsg(result, Status.TASK_INSTANCE_NOT_FOUND);
            return result;
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(task.getTaskCode());
        if (taskDefinition != null && projectCode != taskDefinition.getProjectCode()) {
            putMsg(result, Status.TASK_INSTANCE_NOT_FOUND, taskInstanceId);
            return result;
        }

        // check whether the task instance state type is failure or cancel
        if (!task.getState().typeIsFailure() && !task.getState().typeIsCancel()) {
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
}
