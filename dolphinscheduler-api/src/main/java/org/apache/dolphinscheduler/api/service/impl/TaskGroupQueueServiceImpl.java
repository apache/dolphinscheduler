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
import org.apache.dolphinscheduler.api.permission.ProjectAuthorizationService;
import org.apache.dolphinscheduler.api.service.TaskGroupQueueService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
@Slf4j
public class TaskGroupQueueServiceImpl extends BaseServiceImpl implements TaskGroupQueueService {

    @Autowired
    TaskGroupQueueMapper taskGroupQueueMapper;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private ProjectAuthorizationService projectAuthorizationService;

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
    public PageInfo<TaskGroupQueue> queryTasksByGroupId(User loginUser,
                                                        String taskName,
                                                        String workflowInstanceName,
                                                        Integer status,
                                                        int groupId,
                                                        int pageNo,
                                                        int pageSize) {
        PageInfo<TaskGroupQueue> pageInfo = new PageInfo<>(pageNo, pageSize);
        List<Project> projects = projectDao.queryVisibleProjects(loginUser.getId(),
                loginUser.getUserType() == UserType.ADMIN_USER);
        if (projects.isEmpty()) {
            return pageInfo;
        }
        Page<TaskGroupQueue> page = new Page<>(pageNo, pageSize);
        IPage<TaskGroupQueue> taskGroupQueue = taskGroupQueueMapper.queryTaskGroupQueueByTaskGroupIdPaging(
                page,
                taskName,
                workflowInstanceName,
                status,
                groupId,
                projects);

        pageInfo.setTotal((int) taskGroupQueue.getTotal());
        pageInfo.setTotalList(taskGroupQueue.getRecords());
        return pageInfo;
    }

    @Override
    public void deleteByWorkflowInstanceId(Integer workflowInstanceId) {
        taskGroupQueueMapper.deleteByWorkflowInstanceId(workflowInstanceId);
    }

    @Override
    public void modifyPriority(User loginUser, Integer queueId, Integer priority) {
        TaskGroupQueue queue = taskGroupQueueMapper.selectById(queueId);
        if (queue == null) {
            throw new ServiceException(Status.RESOURCE_NOT_EXIST);
        }
        WorkflowInstance instance = workflowInstanceDao.queryOptionalById(queue.getWorkflowInstanceId())
                .orElseThrow(
                        () -> new ServiceException(Status.WORKFLOW_INSTANCE_NOT_EXIST, queue.getWorkflowInstanceId()));
        projectAuthorizationService.requirePermission(loginUser, instance.getProjectCode(), ProjectPermission.EXECUTE);
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
