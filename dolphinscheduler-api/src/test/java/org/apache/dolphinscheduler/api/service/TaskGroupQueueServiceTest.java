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

import org.apache.dolphinscheduler.api.service.impl.TaskGroupQueueServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * project service test
 **/
@ExtendWith(MockitoExtension.class)
public class TaskGroupQueueServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskGroupQueueServiceTest.class);

    @InjectMocks
    private TaskGroupQueueServiceImpl taskGroupQueueService;

    @Mock
    private TaskGroupQueueMapper taskGroupQueueMapper;

    private String userName = "test";

    private String taskName = "taskGroupQueueServiceTest";

    /**
     * create admin user
     */
    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setUserName(userName);
        loginUser.setId(1);
        return loginUser;
    }

    private TaskGroupQueue getTaskGroupQueue() {
        TaskGroupQueue taskGroupQueue = new TaskGroupQueue();
        taskGroupQueue.setTaskName(taskName);
        taskGroupQueue.setId(1);
        taskGroupQueue.setGroupId(1);
        taskGroupQueue.setTaskId(1);
        taskGroupQueue.setPriority(1);
        taskGroupQueue.setStatus(TaskGroupQueueStatus.ACQUIRE_SUCCESS);
        Date date = new Date(System.currentTimeMillis());
        taskGroupQueue.setUpdateTime(date);
        taskGroupQueue.setCreateTime(date);
        return taskGroupQueue;
    }

    @Test
    public void testDoQuery() {
        User user = getLoginUser();
        IPage<TaskGroupQueue> page = new Page<>(1, 10);
        page.setTotal(1L);
        List<TaskGroupQueue> records = new ArrayList<>();
        records.add(getTaskGroupQueue());
        page.setRecords(records);
        Mockito.when(taskGroupQueueMapper.queryTaskGroupQueuePaging(Mockito.any(Page.class), Mockito.eq(10)))
                .thenReturn(page);
        Map<String, Object> result = taskGroupQueueService.doQuery(user, 1, 1, 10);
        PageInfo<TaskGroupQueue> pageInfo = (PageInfo<TaskGroupQueue>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));
    }
}
