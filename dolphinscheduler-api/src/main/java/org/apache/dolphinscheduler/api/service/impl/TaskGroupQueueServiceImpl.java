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

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskGroupQueueService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;




/**
 *
 */
@Service("taskGroupQueueService")
public class TaskGroupQueueServiceImpl extends BaseServiceImpl implements TaskGroupQueueService {

    @Autowired
    TaskGroupQueueMapper taskGroupQueueMapper;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    private static final Logger logger = LoggerFactory.getLogger(TaskGroupQueueServiceImpl.class);

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
    public Map<String, Object> queryTasksByGroupId(User loginUser, Integer groupId, Integer pageNo, Integer pageSize) {
        return this.doQuery(loginUser, pageNo, pageSize, null, groupId);
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
    public Map<String, Object> queryTasksByProcessId(User loginUser, Integer pageNo, Integer pageSize, Integer processId) {
        return this.doQuery(loginUser, pageNo, pageSize, null, processId);
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
    public Map<String, Object> queryAllTasks(User loginUser, Integer pageNo, Integer pageSize) {
        return this.doQuery(loginUser, pageNo, pageSize, null, null);
    }

    public Map<String, Object> doQuery(User loginUser, Integer pageNo, Integer pageSize,
                                        Integer groupId,
                                        Integer processId) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }
        Page<TaskGroupQueue> page = new Page<>(pageNo, pageSize);
        IPage<TaskGroupQueue> taskGroupQueue = taskGroupQueueMapper.queryTaskGroupQueuePaging(page,groupId);

        PageInfo<TaskGroupQueue> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal((int) taskGroupQueue.getTotal());
        pageInfo.setTotalList(taskGroupQueue.getRecords());

        result.put(Constants.DATA_LIST, pageInfo);
        logger.info("select result:{}", taskGroupQueue);
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
    public boolean deleteByTaskId(Integer taskId) {
        return taskGroupQueueMapper.deleteByTaskId(taskId) == 1;
    }
}
