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
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.SnowFlakeUtils;
import org.apache.dolphinscheduler.common.utils.SnowFlakeUtils.SnowFlakeException;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * task definition service impl
 */
@Service
public class TaskDefinitionServiceImpl extends BaseServiceImpl implements TaskDefinitionService {

    private static final Logger logger = LoggerFactory.getLogger(TaskDefinitionServiceImpl.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired
    private ProcessService processService;

    @Autowired
    private UserMapper userMapper;

    /**
     * create task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskDefinitionJson task definition json
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> createTaskDefinition(User loginUser,
                                                    long projectCode,
                                                    String taskDefinitionJson) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);
        if (taskDefinitionLogs.isEmpty()) {
            logger.error("taskDefinitionJson invalid: {}", taskDefinitionJson);
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJson);
            return result;
        }
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            if (!CheckUtils.checkTaskDefinitionParameters(taskDefinitionLog)) {
                logger.error("task definition {} parameter invalid", taskDefinitionLog.getName());
                putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskDefinitionLog.getName());
                return result;
            }
            TaskDefinition taskDefinition = taskDefinitionMapper.queryByName(projectCode, taskDefinitionLog.getName());
            if (taskDefinition != null) {
                logger.error("task definition name {} already exists", taskDefinitionLog.getName());
                putMsg(result, Status.TASK_DEFINITION_NAME_EXISTED, taskDefinitionLog.getName());
                return result;
            }
        }
        int saveTaskResult = processService.saveTaskDefine(loginUser, projectCode, taskDefinitionLogs);
        if (saveTaskResult == Constants.DEFINITION_FAILURE) {
            putMsg(result, Status.CREATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.CREATE_TASK_DEFINITION_ERROR);
        }
        Map<String, Object> resData = new HashMap<>();
        resData.put("total", taskDefinitionLogs.size());
        resData.put("code", StringUtils.join(taskDefinitionLogs.stream().map(TaskDefinition::getCode).collect(Collectors.toList()), ","));
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, resData);
        return result;
    }

    /**
     * query task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskName task name
     */
    @Override
    public Map<String, Object> queryTaskDefinitionByName(User loginUser, long projectCode, String taskName) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByName(project.getCode(), taskName);
        if (taskDefinition == null) {
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, taskName);
        } else {
            result.put(Constants.DATA_LIST, taskDefinition);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * delete task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskCode task code
     */
    @Override
    public Map<String, Object> deleteTaskDefinitionByCode(User loginUser, long projectCode, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryByTaskCode(taskCode);
        if (!processTaskRelationList.isEmpty()) {
            Set<Long> processDefinitionCodes = processTaskRelationList
                .stream()
                .map(ProcessTaskRelation::getProcessDefinitionCode)
                .collect(Collectors.toSet());
            putMsg(result, Status.PROCESS_TASK_RELATION_EXIST, StringUtils.join(processDefinitionCodes, ","));
            return result;
        }
        int delete = taskDefinitionMapper.deleteByCode(taskCode);
        if (delete > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_TASK_DEFINE_BY_CODE_ERROR);
        }
        return result;
    }

    /**
     * update task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskCode task code
     * @param taskDefinitionJsonObj task definition json object
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public Map<String, Object> updateTaskDefinition(User loginUser, long projectCode, long taskCode, String taskDefinitionJsonObj) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (processService.isTaskOnline(taskCode)) {
            putMsg(result, Status.PROCESS_DEFINE_STATE_ONLINE);
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null) {
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, taskCode);
            return result;
        }
        TaskDefinitionLog taskDefinitionToUpdate = JSONUtils.parseObject(taskDefinitionJsonObj, TaskDefinitionLog.class);
        if (taskDefinitionToUpdate == null) {
            logger.error("taskDefinitionJson is not valid json");
            putMsg(result, Status.DATA_IS_NOT_VALID, taskDefinitionJsonObj);
            return result;
        }
        if (!CheckUtils.checkTaskDefinitionParameters(taskDefinitionToUpdate)) {
            logger.error("task definition {} parameter invalid", taskDefinitionToUpdate.getName());
            putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskDefinitionToUpdate.getName());
            return result;
        }
        Integer version = taskDefinitionLogMapper.queryMaxVersionForDefinition(taskCode);
        if (version == null || version == 0) {
            putMsg(result, Status.DATA_IS_NOT_VALID, taskCode);
            return result;
        }
        Date now = new Date();
        taskDefinitionToUpdate.setCode(taskCode);
        taskDefinitionToUpdate.setId(taskDefinition.getId());
        taskDefinitionToUpdate.setProjectCode(projectCode);
        taskDefinitionToUpdate.setUserId(taskDefinition.getUserId());
        taskDefinitionToUpdate.setVersion(version + 1);
        taskDefinitionToUpdate.setTaskType(taskDefinitionToUpdate.getTaskType().toUpperCase());
        taskDefinitionToUpdate.setResourceIds(processService.getResourceIds(taskDefinitionToUpdate));
        taskDefinitionToUpdate.setUpdateTime(now);
        int update = taskDefinitionMapper.updateById(taskDefinitionToUpdate);
        taskDefinitionToUpdate.setOperator(loginUser.getId());
        taskDefinitionToUpdate.setOperateTime(now);
        taskDefinitionToUpdate.setCreateTime(now);
        int insert = taskDefinitionLogMapper.insert(taskDefinitionToUpdate);
        if ((update & insert) != 1) {
            putMsg(result, Status.UPDATE_TASK_DEFINITION_ERROR);
            throw new ServiceException(Status.UPDATE_TASK_DEFINITION_ERROR);
        }
        result.put(Constants.DATA_LIST, taskCode);
        putMsg(result, Status.SUCCESS, update);
        return result;
    }

    /**
     * update task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskCode task code
     * @param version the version user want to switch
     */
    @Override
    public Map<String, Object> switchVersion(User loginUser, long projectCode, long taskCode, int version) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (processService.isTaskOnline(taskCode)) {
            putMsg(result, Status.PROCESS_DEFINE_STATE_ONLINE);
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null) {
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, taskCode);
            return result;
        }
        TaskDefinitionLog taskDefinitionLog = taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, version);
        taskDefinitionLog.setUserId(loginUser.getId());
        taskDefinitionLog.setUpdateTime(new Date());
        int switchVersion = taskDefinitionMapper.updateById(taskDefinitionLog);
        if (switchVersion > 0) {
            result.put(Constants.DATA_LIST, taskCode);
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.SWITCH_TASK_DEFINITION_VERSION_ERROR);
        }
        return result;
    }

    @Override
    public Result queryTaskDefinitionVersions(User loginUser,
                                              long projectCode,
                                              long taskCode,
                                              int pageNo,
                                              int pageSize) {
        Result result = new Result();
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            putMsg(result, resultStatus);
            return result;
        }
        PageInfo<TaskDefinitionLog> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<TaskDefinitionLog> page = new Page<>(pageNo, pageSize);
        IPage<TaskDefinitionLog> taskDefinitionVersionsPaging = taskDefinitionLogMapper.queryTaskDefinitionVersionsPaging(page, taskCode);
        List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionVersionsPaging.getRecords();

        pageInfo.setTotalList(taskDefinitionLogs);
        pageInfo.setTotal((int) taskDefinitionVersionsPaging.getTotal());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Map<String, Object> deleteByCodeAndVersion(User loginUser, long projectCode, long taskCode, int version) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);

        if (taskDefinition == null) {
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, taskCode);
        } else {
            int delete = taskDefinitionLogMapper.deleteByCodeAndVersion(taskCode, version);
            if (delete > 0) {
                putMsg(result, Status.SUCCESS);
            } else {
                putMsg(result, Status.DELETE_TASK_DEFINITION_VERSION_ERROR);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> queryTaskDefinitionDetail(User loginUser, long projectCode, long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null) {
            putMsg(result, Status.TASK_DEFINE_NOT_EXIST, taskCode);
        } else {
            result.put(Constants.DATA_LIST, taskDefinition);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    @Override
    public Result queryTaskDefinitionListPaging(User loginUser,
                                                long projectCode,
                                                String taskType,
                                                String searchVal,
                                                Integer userId,
                                                Integer pageNo,
                                                Integer pageSize) {
        Result result = new Result();
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectCode);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            putMsg(result, resultStatus);
            return result;
        }
        if (StringUtils.isNotBlank(taskType)) {
            taskType = taskType.toUpperCase();
        }
        Page<TaskDefinition> page = new Page<>(pageNo, pageSize);
        IPage<TaskDefinition> taskDefinitionIPage = taskDefinitionMapper.queryDefineListPaging(
            page, projectCode, taskType, searchVal, userId, isAdmin(loginUser));
        if (StringUtils.isNotBlank(taskType)) {
            List<TaskDefinition> records = taskDefinitionIPage.getRecords();
            for (TaskDefinition pd : records) {
                TaskDefinitionLog taskDefinitionLog = taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(pd.getCode(), pd.getVersion());
                User user = userMapper.selectById(taskDefinitionLog.getOperator());
                pd.setModifyBy(user.getUserName());
            }
            taskDefinitionIPage.setRecords(records);
        }
        PageInfo<TaskDefinition> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) taskDefinitionIPage.getTotal());
        pageInfo.setTotalList(taskDefinitionIPage.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Map<String, Object> genTaskCodeList(Integer genNum) {
        Map<String, Object> result = new HashMap<>();
        if (genNum == null || genNum < 1 || genNum > 100) {
            logger.error("the genNum must be great than 1 and less than 100");
            putMsg(result, Status.DATA_IS_NOT_VALID, genNum);
            return result;
        }
        List<Long> taskCodes = new ArrayList<>();
        try {
            for (int i = 0; i < genNum; i++) {
                taskCodes.add(SnowFlakeUtils.getInstance().nextId());
            }
        } catch (SnowFlakeException e) {
            logger.error("Task code get error, ", e);
            putMsg(result, Status.INTERNAL_SERVER_ERROR_ARGS, "Error generating task definition code");
        }
        putMsg(result, Status.SUCCESS);
        // return processDefinitionCode
        result.put(Constants.DATA_LIST, taskCodes);
        return result;
    }
}
