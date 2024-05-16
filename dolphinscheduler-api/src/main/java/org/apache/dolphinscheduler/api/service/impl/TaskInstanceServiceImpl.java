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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.FORCED_SUCCESS;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.INSTANCE_UPDATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_INSTANCE;

import org.apache.dolphinscheduler.api.dto.taskInstance.TaskInstanceRemoveCacheResponse;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.TaskGroupQueueService;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.repository.DqExecuteResultDao;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.utils.TaskCacheUtils;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.common.ILogService;
import org.apache.dolphinscheduler.extract.worker.IStreamingTaskInstanceOperator;
import org.apache.dolphinscheduler.extract.worker.ITaskInstanceOperator;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceKillRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceKillResponse;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceTriggerSavepointRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceTriggerSavepointResponse;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * task instance service impl
 */
@Service
@Slf4j
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
    TaskInstanceDao taskInstanceDao;

    @Autowired
    UsersService usersService;

    @Autowired
    TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private DqExecuteResultDao dqExecuteResultDao;

    @Autowired
    private TaskGroupQueueService taskGroupQueueService;

    @Autowired
    private ProcessInstanceDao workflowInstanceDao;

    /**
     * query task list by project, process instance, task name, task start time, task end time, task status, keyword paging
     *
     * @param loginUser         login user
     * @param projectCode       project code
     * @param processInstanceId process instance id
     * @param searchVal         search value
     * @param taskName          task name
     * @param taskCode          task code
     * @param stateType         state type
     * @param host              host
     * @param startDate         start time
     * @param endDate           end time
     * @param pageNo            page number
     * @param pageSize          page size
     * @return task list page
     */
    @Override
    public Result queryTaskListPaging(User loginUser,
                                      long projectCode,
                                      Integer processInstanceId,
                                      String processInstanceName,
                                      String processDefinitionName,
                                      String taskName,
                                      Long taskCode,
                                      String executorName,
                                      String startDate,
                                      String endDate,
                                      String searchVal,
                                      TaskExecutionStatus stateType,
                                      String host,
                                      TaskExecuteType taskExecuteType,
                                      Integer pageNo,
                                      Integer pageSize) {
        Result result = new Result();
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, TASK_INSTANCE);
        int[] statusArray = null;
        if (stateType != null) {
            statusArray = new int[]{stateType.getCode()};
        }
        Date start = checkAndParseDateParameters(startDate);
        Date end = checkAndParseDateParameters(endDate);
        Page<TaskInstance> page = new Page<>(pageNo, pageSize);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(pageNo, pageSize);
        IPage<TaskInstance> taskInstanceIPage;
        if (taskExecuteType == TaskExecuteType.STREAM) {
            // stream task without process instance
            taskInstanceIPage = taskInstanceMapper.queryStreamTaskInstanceListPaging(
                    page,
                    projectCode,
                    processDefinitionName,
                    searchVal,
                    taskName,
                    taskCode,
                    executorName,
                    statusArray,
                    host,
                    taskExecuteType,
                    start,
                    end);
        } else {
            taskInstanceIPage = taskInstanceMapper.queryTaskInstanceListPaging(
                    page,
                    projectCode,
                    processInstanceId,
                    processInstanceName,
                    searchVal,
                    taskName,
                    taskCode,
                    executorName,
                    statusArray,
                    host,
                    taskExecuteType,
                    start,
                    end);
        }
        Set<String> exclusionSet = new HashSet<>();
        exclusionSet.add(Constants.CLASS);
        exclusionSet.add("taskJson");
        List<TaskInstance> taskInstanceList = taskInstanceIPage.getRecords();
        List<Integer> executorIds =
                taskInstanceList.stream().map(TaskInstance::getExecutorId).distinct().collect(Collectors.toList());
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
     * @param loginUser      login user
     * @param projectCode    project code
     * @param taskInstanceId task instance id
     * @return the result code and msg
     */
    @Transactional
    @Override
    public void forceTaskSuccess(User loginUser, long projectCode, Integer taskInstanceId) {
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode, FORCED_SUCCESS);

        TaskInstance task = taskInstanceDao.queryOptionalById(taskInstanceId)
                .orElseThrow(() -> new ServiceException(Status.TASK_INSTANCE_NOT_FOUND));

        if (task.getProjectCode() != projectCode) {
            throw new ServiceException("The task instance is not under the project: " + projectCode);
        }

        ProcessInstance processInstance = workflowInstanceDao.queryOptionalById(task.getProcessInstanceId())
                .orElseThrow(
                        () -> new ServiceException(Status.PROCESS_INSTANCE_NOT_EXIST, task.getProcessInstanceId()));
        if (!processInstance.getState().isFinished()) {
            throw new ServiceException("The workflow instance is not finished: " + processInstance.getState()
                    + " cannot force start task instance");
        }

        // check whether the task instance state type is failure or cancel
        if (!task.getState().isFailure() && !task.getState().isKill()) {
            throw new ServiceException(Status.TASK_INSTANCE_STATE_OPERATION_ERROR, taskInstanceId, task.getState());
        }

        // change the state of the task instance
        task.setState(TaskExecutionStatus.FORCED_SUCCESS);
        task.setEndTime(new Date());
        int changedNum = taskInstanceMapper.updateById(task);
        if (changedNum <= 0) {
            throw new ServiceException(Status.FORCE_TASK_SUCCESS_ERROR);
        }
        processService.forceProcessInstanceSuccessByTaskInstanceId(task);
        log.info("Force success task instance:{} success", taskInstanceId);
    }

    @Override
    public Result taskSavePoint(User loginUser, long projectCode, Integer taskInstanceId) {
        Result result = new Result();

        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> checkResult =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, FORCED_SUCCESS);
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (status != Status.SUCCESS) {
            putMsg(result, status);
            return result;
        }

        TaskInstance taskInstance = taskInstanceMapper.selectById(taskInstanceId);
        if (taskInstance == null) {
            log.error("Task definition can not be found, projectCode:{}, taskInstanceId:{}.", projectCode,
                    taskInstanceId);
            putMsg(result, Status.TASK_INSTANCE_NOT_FOUND);
            return result;
        }
        IStreamingTaskInstanceOperator streamingTaskInstanceOperator =
                SingletonJdkDynamicRpcClientProxyFactory
                        .getProxyClient(taskInstance.getHost(), IStreamingTaskInstanceOperator.class);
        TaskInstanceTriggerSavepointResponse taskInstanceTriggerSavepointResponse =
                streamingTaskInstanceOperator.triggerSavepoint(new TaskInstanceTriggerSavepointRequest(taskInstanceId));
        log.info("StreamingTaskInstance trigger savepoint response: {}", taskInstanceTriggerSavepointResponse);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Result stopTask(User loginUser, long projectCode, Integer taskInstanceId) {
        Result result = new Result();

        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> checkResult =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, FORCED_SUCCESS);
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (status != Status.SUCCESS) {
            putMsg(result, status);
            return result;
        }

        TaskInstance taskInstance = taskInstanceMapper.selectById(taskInstanceId);
        if (taskInstance == null) {
            log.error("Task definition can not be found, projectCode:{}, taskInstanceId:{}.", projectCode,
                    taskInstanceId);
            putMsg(result, Status.TASK_INSTANCE_NOT_FOUND);
            return result;
        }

        // todo: we only support streaming task for now
        ITaskInstanceOperator iTaskInstanceOperator = SingletonJdkDynamicRpcClientProxyFactory
                .getProxyClient(taskInstance.getHost(), ITaskInstanceOperator.class);
        TaskInstanceKillResponse taskInstanceKillResponse =
                iTaskInstanceOperator.killTask(new TaskInstanceKillRequest(taskInstanceId));
        log.info("TaskInstance kill response: {}", taskInstanceKillResponse);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public TaskInstance queryTaskInstanceById(User loginUser, long projectCode, Long taskInstanceId) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, project, FORCED_SUCCESS);
        TaskInstance taskInstance = taskInstanceMapper.selectById(taskInstanceId);
        if (taskInstance == null) {
            log.error("Task instance can not be found, projectCode:{}, taskInstanceId:{}.", projectCode,
                    taskInstanceId);
        }
        return taskInstance;
    }

    @Override
    public TaskInstanceRemoveCacheResponse removeTaskInstanceCache(User loginUser, long projectCode,
                                                                   Integer taskInstanceId) {
        Result result = new Result();

        Project project = projectMapper.queryByCode(projectCode);
        projectService.checkProjectAndAuthThrowException(loginUser, project, INSTANCE_UPDATE);

        TaskInstance taskInstance = taskInstanceMapper.selectById(taskInstanceId);
        if (taskInstance == null) {
            log.error("Task definition can not be found, projectCode:{}, taskInstanceId:{}.", projectCode,
                    taskInstanceId);
            putMsg(result, Status.TASK_INSTANCE_NOT_FOUND);
            return new TaskInstanceRemoveCacheResponse(result);
        }
        String tagCacheKey = taskInstance.getCacheKey();
        Pair<Integer, String> taskIdAndCacheKey = TaskCacheUtils.revertCacheKey(tagCacheKey);
        String cacheKey = taskIdAndCacheKey.getRight();
        if (StringUtils.isNotEmpty(cacheKey)) {
            taskInstanceDao.clearCacheByCacheKey(cacheKey);
        }
        putMsg(result, Status.SUCCESS);
        return new TaskInstanceRemoveCacheResponse(result, cacheKey);
    }

    @Override
    public void deleteByWorkflowInstanceId(Integer workflowInstanceId) {
        List<TaskInstance> needToDeleteTaskInstances =
                taskInstanceDao.queryByWorkflowInstanceId(workflowInstanceId);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(needToDeleteTaskInstances)) {
            return;
        }
        for (TaskInstance taskInstance : needToDeleteTaskInstances) {
            if (StringUtils.isNotBlank(taskInstance.getLogPath())) {
                try {
                    // Remove task instance log failed will not affect the deletion of task instance
                    ILogService iLogService =
                            SingletonJdkDynamicRpcClientProxyFactory.getProxyClient(taskInstance.getHost(),
                                    ILogService.class);
                    iLogService.removeTaskInstanceLog(taskInstance.getLogPath());
                } catch (Exception ex) {
                    log.error("Remove task instance log error", ex);
                }
            }
        }

        dqExecuteResultDao.deleteByWorkflowInstanceId(workflowInstanceId);
        taskGroupQueueService.deleteByWorkflowInstanceId(workflowInstanceId);
        taskInstanceDao.deleteByWorkflowInstanceId(workflowInstanceId);
    }

}
