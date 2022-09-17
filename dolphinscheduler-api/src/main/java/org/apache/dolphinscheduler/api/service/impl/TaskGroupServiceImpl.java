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

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.TaskGroupQueueService;
import org.apache.dolphinscheduler.api.service.TaskGroupService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * task Group Service
 */
@Service
public class TaskGroupServiceImpl extends BaseServiceImpl implements TaskGroupService {

    @Autowired
    private TaskGroupMapper taskGroupMapper;

    @Autowired
    private TaskGroupQueueService taskGroupQueueService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ExecutorService executorService;

    private static final Logger logger = LoggerFactory.getLogger(TaskGroupServiceImpl.class);

    /**
     * create a Task group
     *
     * @param loginUser   login user
     * @param name        task group name
     * @param description task group description
     * @param groupSize   task group total size
     * @return the result code and msg
     */
    @Override
    @Transactional
    public Map<String, Object> createTaskGroup(User loginUser, Long projectCode, String name, String description,
                                               int groupSize) {
        Map<String, Object> result = new HashMap<>();
        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.TASK_GROUP,
                ApiFuncIdentificationConstant.TASK_GROUP_CREATE);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        if (checkDescriptionLength(description)) {
            logger.warn("Parameter description is too long.");
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        if (name == null) {
            logger.warn("Parameter name can ot be null.");
            putMsg(result, Status.NAME_NULL);
            return result;
        }
        if (groupSize <= 0) {
            logger.warn("Parameter task group size is must bigger than 1.");
            putMsg(result, Status.TASK_GROUP_SIZE_ERROR);
            return result;
        }
        TaskGroup taskGroup1 = taskGroupMapper.queryByName(loginUser.getId(), name);
        if (taskGroup1 != null) {
            logger.warn("Task group with the same name already exists, taskGroupName:{}.", taskGroup1.getName());
            putMsg(result, Status.TASK_GROUP_NAME_EXSIT);
            return result;
        }
        TaskGroup taskGroup = new TaskGroup(name, projectCode, description,
                groupSize, loginUser.getId(), Flag.YES.getCode());

        taskGroup.setCreateTime(new Date());
        taskGroup.setUpdateTime(new Date());
        if (taskGroupMapper.insert(taskGroup) > 0) {
            permissionPostHandle(AuthorizationType.TASK_GROUP, loginUser.getId(), Collections.singletonList(taskGroup.getId()),logger);
            logger.info("Create task group complete, taskGroupName:{}.", taskGroup.getName());
            putMsg(result, Status.SUCCESS);
        } else {
            logger.error("Create task group error, taskGroupName:{}.", taskGroup.getName());
            putMsg(result, Status.CREATE_TASK_GROUP_ERROR);
            return result;
        }

        return result;
    }

    /**
     * update the task group
     *
     * @param loginUser   login user
     * @param name        task group name
     * @param description task group description
     * @param groupSize   task group total size
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> updateTaskGroup(User loginUser, int id, String name, String description, int groupSize) {
        Map<String, Object> result = new HashMap<>();
        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.TASK_GROUP,
                ApiFuncIdentificationConstant.TASK_GROUP_EDIT);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        if (checkDescriptionLength(description)) {
            logger.warn("Parameter description is too long.");
            putMsg(result, Status.DESCRIPTION_TOO_LONG_ERROR);
            return result;
        }
        if (name == null) {
            logger.warn("Parameter name can ot be null.");
            putMsg(result, Status.NAME_NULL);
            return result;
        }
        if (groupSize <= 0) {
            logger.warn("Parameter task group size is must bigger than 1.");
            putMsg(result, Status.TASK_GROUP_SIZE_ERROR);
            return result;
        }
        Long exists = taskGroupMapper.selectCount(new QueryWrapper<TaskGroup>().lambda()
                .eq(TaskGroup::getName, name)
                .eq(TaskGroup::getUserId, loginUser.getId())
                .ne(TaskGroup::getId, id));

        if (exists > 0) {
            logger.error("Task group with the same name already exists.");
            putMsg(result, Status.TASK_GROUP_NAME_EXSIT);
            return result;
        }
        TaskGroup taskGroup = taskGroupMapper.selectById(id);
        if (taskGroup.getStatus() != Flag.YES.getCode()) {
            logger.warn("Task group has been closed, taskGroupId:{}.", id);
            putMsg(result, Status.TASK_GROUP_STATUS_ERROR);
            return result;
        }
        taskGroup.setGroupSize(groupSize);
        taskGroup.setDescription(description);
        taskGroup.setUpdateTime(new Date());
        if (StringUtils.isNotEmpty(name)) {
            taskGroup.setName(name);
        }
        int i = taskGroupMapper.updateById(taskGroup);
        if (i > 0) {
            logger.info("Update task group complete, taskGroupId:{}.", id);
            putMsg(result, Status.SUCCESS);
        } else {
            logger.error("Update task group error, taskGroupId:{}.", id);
            putMsg(result, Status.UPDATE_TASK_GROUP_ERROR);
        }
        return result;
    }

    /**
     * get task group status
     *
     * @param id task group id
     * @return is the task group available
     */
    @Override
    public boolean isTheTaskGroupAvailable(int id) {
        return taskGroupMapper.selectCountByIdStatus(id, Flag.YES.getCode()) == 1;
    }

    /**
     * query all task group by user id
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> queryAllTaskGroup(User loginUser, String name, Integer status, int pageNo,
                                                 int pageSize) {
        return this.doQuery(loginUser, pageNo, pageSize, loginUser.getId(), name, status);
    }

    /**
     * query all task group by status
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @param status    status
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> queryTaskGroupByStatus(User loginUser, int pageNo, int pageSize, int status) {
        return this.doQuery(loginUser, pageNo, pageSize, loginUser.getId(), null, status);
    }

    /**
     * query all task group by name
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @param projectCode project code
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> queryTaskGroupByProjectCode(User loginUser, int pageNo, int pageSize, Long projectCode) {
        Map<String, Object> result = new HashMap<>();
        Page<TaskGroup> page = new Page<>(pageNo, pageSize);
        PageInfo<TaskGroup> emptyPageInfo = new PageInfo<>(pageNo, pageSize);
        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.TASK_GROUP,
                loginUser.getId(), logger);
        if (ids.isEmpty()) {
            result.put(Constants.DATA_LIST, emptyPageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        IPage<TaskGroup> taskGroupPaging =
                taskGroupMapper.queryTaskGroupPagingByProjectCode(page, new ArrayList<>(ids), projectCode);

        return getStringObjectMap(pageNo, pageSize, result, taskGroupPaging);
    }

    private Map<String, Object> getStringObjectMap(int pageNo, int pageSize, Map<String, Object> result,
                                                   IPage<TaskGroup> taskGroupPaging) {
        PageInfo<TaskGroup> pageInfo = new PageInfo<>(pageNo, pageSize);
        int total = taskGroupPaging == null ? 0 : (int) taskGroupPaging.getTotal();
        List<TaskGroup> list = taskGroupPaging == null ? new ArrayList<TaskGroup>() : taskGroupPaging.getRecords();
        pageInfo.setTotal(total);
        pageInfo.setTotalList(list);

        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query all task group by id
     *
     * @param loginUser login user
     * @param id        id
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> queryTaskGroupById(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        TaskGroup taskGroup = taskGroupMapper.selectById(id);
        result.put(Constants.DATA_LIST, taskGroup);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query
     *
     * @param pageNo   page no
     * @param pageSize page size
     * @param userId   user id
     * @param name     name
     * @param status   status
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> doQuery(User loginUser, int pageNo, int pageSize, int userId, String name,
                                       Integer status) {
        Map<String, Object> result = new HashMap<>();
        Page<TaskGroup> page = new Page<>(pageNo, pageSize);
        PageInfo<TaskGroup> pageInfo = new PageInfo<>(pageNo, pageSize);
        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.TASK_GROUP,
                userId, logger);
        if (ids.isEmpty()) {
            result.put(Constants.DATA_LIST, pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        IPage<TaskGroup> taskGroupPaging =
                taskGroupMapper.queryTaskGroupPaging(page, new ArrayList<>(ids), name, status);

        return getStringObjectMap(pageNo, pageSize, result, taskGroupPaging);
    }

    /**
     * close a task group
     *
     * @param loginUser login user
     * @param id        task group id
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> closeTaskGroup(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();

        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.TASK_GROUP,
                ApiFuncIdentificationConstant.TASK_GROUP_CLOSE);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        TaskGroup taskGroup = taskGroupMapper.selectById(id);
        if (taskGroup.getStatus() == Flag.NO.getCode()) {
            logger.info("Task group has been closed, taskGroupId:{}.", id);
            putMsg(result, Status.TASK_GROUP_STATUS_CLOSED);
            return result;
        }
        taskGroup.setStatus(Flag.NO.getCode());
        int update = taskGroupMapper.updateById(taskGroup);
        if (update > 0)
            logger.info("Task group close complete, taskGroupId:{}.", id);
        else
            logger.error("Task group close error, taskGroupId:{}.", id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * start a task group
     *
     * @param loginUser login user
     * @param id        task group id
     * @return the result code and msg
     */
    @Override
    public Map<String, Object> startTaskGroup(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();

        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.TASK_GROUP,
                ApiFuncIdentificationConstant.TASK_GROUP_CLOSE);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        TaskGroup taskGroup = taskGroupMapper.selectById(id);
        if (taskGroup.getStatus() == Flag.YES.getCode()) {
            logger.info("Task group has been started, taskGroupId:{}.", id);
            putMsg(result, Status.TASK_GROUP_STATUS_OPENED);
            return result;
        }
        taskGroup.setStatus(Flag.YES.getCode());
        taskGroup.setUpdateTime(new Date(System.currentTimeMillis()));
        int update = taskGroupMapper.updateById(taskGroup);
        if (update > 0)
            logger.info("Task group start complete, taskGroupId:{}.", id);
        else
            logger.error("Task group start error, taskGroupId:{}.", id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * wake a task manually
     *
     * @param loginUser
     * @param queueId   task group queue id
     * @return result
     */
    @Override
    public Map<String, Object> forceStartTask(User loginUser, int queueId) {
        Map<String, Object> result = new HashMap<>();
        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.TASK_GROUP,
                ApiFuncIdentificationConstant.TASK_GROUP_QUEUE_START);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        return executorService.forceStartTaskInstance(loginUser, queueId);
    }

    @Override
    public Map<String, Object> modifyPriority(User loginUser, Integer queueId, Integer priority) {
        Map<String, Object> result = new HashMap<>();

        boolean canOperatorPermissions = canOperatorPermissions(loginUser, null, AuthorizationType.TASK_GROUP,
                ApiFuncIdentificationConstant.TASK_GROUP_QUEUE_PRIORITY);
        if (!canOperatorPermissions) {
            putMsg(result, Status.NO_CURRENT_OPERATING_PERMISSION);
            return result;
        }
        taskGroupQueueService.modifyPriority(queueId, priority);
        logger.info("Modify task group queue priority complete, queueId:{}, priority:{}.", queueId, priority);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
