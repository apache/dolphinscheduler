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
import org.apache.dolphinscheduler.api.service.TaskGroupQueueService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;

import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * task group queue service
 */
@Service
@Slf4j
public class TaskGroupQueueServiceImpl extends BaseServiceImpl implements TaskGroupQueueService {

    @Autowired
    TaskGroupQueueMapper taskGroupQueueMapper;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * query tasks in task group queue by group id
     *
     * @param loginUser login user
     * @param groupId   group id
     * @param pageNo    page no
     * @param pageSize  page size
     * @return tasks list
     */
    @Override
    public Map<String, Object> queryTasksByGroupId(User loginUser, String taskName, String processName, Integer status,
                                                   int groupId, int pageNo, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        Page<TaskGroupQueue> page = new Page<>(pageNo, pageSize);
        PageInfo<TaskGroupQueue> pageInfo = new PageInfo<>(pageNo, pageSize);
        Set<Integer> projectIds = resourcePermissionCheckService
                .userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS, loginUser.getId(), log);
        if (projectIds.isEmpty()) {
            result.put(Constants.DATA_LIST, pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<Project> projects = projectMapper.selectBatchIds(projectIds);
        IPage<TaskGroupQueue> taskGroupQueue = taskGroupQueueMapper.queryTaskGroupQueueByTaskGroupIdPaging(page,
                taskName, processName, status, groupId, projects);

        pageInfo.setTotal((int) taskGroupQueue.getTotal());
        pageInfo.setTotalList(taskGroupQueue.getRecords());

        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query tasks in task group queue by project id
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @param processId process id
     * @return tasks list
     */
    @Override
    public Map<String, Object> queryTasksByProcessId(User loginUser, int pageNo, int pageSize, int processId) {
        return this.doQuery(loginUser, pageNo, pageSize, processId);
    }

    /**
     * query all tasks in task group queue
     *
     * @param loginUser login user
     * @param pageNo    page no
     * @param pageSize  page size
     * @return tasks list
     */
    @Override
    public Map<String, Object> queryAllTasks(User loginUser, int pageNo, int pageSize) {
        return this.doQuery(loginUser, pageNo, pageSize, 0);
    }

    public Map<String, Object> doQuery(User loginUser, int pageNo, int pageSize,
                                       int groupId) {
        Map<String, Object> result = new HashMap<>();

        Page<TaskGroupQueue> page = new Page<>(pageNo, pageSize);
        IPage<TaskGroupQueue> taskGroupQueue = taskGroupQueueMapper.queryTaskGroupQueuePaging(page, groupId);

        PageInfo<TaskGroupQueue> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) taskGroupQueue.getTotal());
        pageInfo.setTotalList(taskGroupQueue.getRecords());

        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * delete by task id
     *
     * @param taskId task id
     * @return TaskGroupQueue entity
     */

    @Override
    public boolean deleteByTaskId(int taskId) {
        return taskGroupQueueMapper.deleteByTaskId(taskId) == 1;
    }

    @Override
    public void deleteByTaskInstanceIds(List<Integer> taskInstanceIds) {
        if (CollectionUtils.isEmpty(taskInstanceIds)) {
            return;
        }
        taskGroupQueueMapper.deleteByTaskInstanceIds(taskInstanceIds);
    }

    @Override
    public void deleteByWorkflowInstanceId(Integer workflowInstanceId) {
        taskGroupQueueMapper.deleteByWorkflowInstanceId(workflowInstanceId);
    }

    @Override
    public void forceStartTask(int queueId, int forceStart) {
        taskGroupQueueMapper.updateForceStart(queueId, forceStart);
    }

    @Override
    public void modifyPriority(Integer queueId, Integer priority) {
        taskGroupQueueMapper.modifyPriority(queueId, priority);
    }

    @Override
    public void deleteByTaskGroupIds(List<Integer> taskGroupIds) {
        if (CollectionUtils.isEmpty(taskGroupIds)) {
            return;
        }
        taskGroupQueueMapper.deleteByTaskGroupIds(taskGroupIds);
    }
}
