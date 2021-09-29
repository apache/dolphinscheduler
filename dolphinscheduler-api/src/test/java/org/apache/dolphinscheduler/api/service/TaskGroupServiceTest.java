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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.TaskGroupServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * project service test
 **/
@RunWith(MockitoJUnitRunner.class)
public class TaskGroupServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskGroupServiceTest.class);

    @InjectMocks
    private TaskGroupServiceImpl taskGroupService;

    @Mock
    private TaskGroupQueueService taskGroupQueueService;

    @Mock
    private ProcessService processService;

    @Mock
    private TaskGroupMapper taskGroupMapper;

    @Mock
    private TaskGroupQueueMapper taskGroupQueueMapper;


    @Mock
    private UserMapper userMapper;

    private String taskGroupName = "TaskGroupServiceTest";

    private String taskGroupDesc = "this is a task group";

    private String userName = "taskGroupServiceTest";

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

    private TaskGroup getTaskGroup() {
        TaskGroup taskGroup = new TaskGroup();
        taskGroup.setName(taskGroupName);
        taskGroup.setGroupSize(100);
        taskGroup.setId(1);
        taskGroup.setUserId(1);
        taskGroup.setStatus(1);
        Date date = new Date(System.currentTimeMillis());
        taskGroup.setUpdateTime(date);
        taskGroup.setCreateTime(date);
        return taskGroup;
    }

    private List<TaskGroup> getList() {
        List<TaskGroup> list = new ArrayList<>();
        list.add(getTaskGroup());
        return list;
    }

    @Test
    public void testCreate() {
        User loginUser = getLoginUser();
        System.out.println(loginUser);
        Map<String, Object> result = taskGroupService.createTaskGroup(loginUser, taskGroupName, taskGroupDesc,100);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        Mockito.when(taskGroupMapper.queryByName(loginUser.getId(), taskGroupName)).thenReturn(getTaskGroup());
        Map<String, Object> taskGroup = taskGroupService.createTaskGroup(loginUser, taskGroupName, taskGroupDesc, 10);
        Assert.assertEquals(Status.TASK_GROUP_NAME_EXSIT, taskGroup.get(Constants.STATUS));

    }

    @Test
    public void testQueryById() {
        User loginUser = getLoginUser();
        loginUser.setId(1);

        //not exist
        Map<String, Object> result = taskGroupService.queryTaskGroupById(loginUser, Integer.MAX_VALUE);
        logger.info(result.toString());
        Assert.assertEquals(null, result.get(Constants.DATA_LIST));

        //success
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(getTaskGroup());
        result = taskGroupService.queryTaskGroupById(loginUser, 1);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

    }

    @Test
    public void testQueryProjectListPaging() {

        IPage<TaskGroup> page = new Page<>(1, 10);
        page.setRecords(getList());
        User loginUser = getLoginUser();
        Mockito.when(taskGroupMapper.queryTaskGroupPaging(Mockito.any(Page.class), Mockito.eq(1), Mockito.eq(null), Mockito.eq(null))).thenReturn(page);

        // query all
        Map<String, Object> result = taskGroupService.queryAllTaskGroup(loginUser, 1, 10);
        PageInfo<TaskGroup> pageInfo = (PageInfo<TaskGroup>) result.get(Constants.DATA_LIST);
        List<TaskGroup> lists = pageInfo.getTotalList();
        for (TaskGroup list : lists) {
            System.out.println(list);
        }
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));
        System.out.println("-------------------------------");

        //by name
        Mockito.when(taskGroupMapper.queryTaskGroupPaging(Mockito.any(Page.class), Mockito.eq(1), Mockito.eq(taskGroupName), Mockito.eq(null))).thenReturn(page);
        Map<String, Object> result1 = taskGroupService.queryTaskGroupByName(loginUser, 1, 10, taskGroupName);
        PageInfo<TaskGroup> pageInfo1 = (PageInfo<TaskGroup>) result1.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo1.getTotalList()));

        //by status
        Mockito.when(taskGroupMapper.queryTaskGroupPaging(Mockito.any(Page.class), Mockito.eq(1), Mockito.eq(null), Mockito.eq(1))).thenReturn(page);
        Map<String, Object> result2 = taskGroupService.queryTaskGroupByStatus(loginUser, 1, 10, 1);

        PageInfo<TaskGroup> pageInfo2 = (PageInfo<TaskGroup>) result1.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo2.getTotalList()));
    }

    @Test
    public void testUpdate() {

        User loginUser = getLoginUser();
        TaskGroup taskGroup = getTaskGroup();

        // Task group status error
        Mockito.when(taskGroupMapper.queryByName(loginUser.getId(), taskGroupName)).thenReturn(taskGroup);
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);
        Map<String, Object> result = taskGroupService.updateTaskGroup(loginUser, 1, "newName", "desc", 100);
        logger.info(result.toString());
        Assert.assertEquals(Status.TASK_GROUP_STATUS_ERROR, result.get(Constants.STATUS));

        taskGroup.setStatus(0);

        // task group name exists
        Map<String, Object> result1 = taskGroupService.updateTaskGroup(loginUser, 1, taskGroupName, "desc", 100);
        Assert.assertEquals(Status.TASK_GROUP_NAME_EXSIT, result1.get(Constants.STATUS));

        // success
        Mockito.when(taskGroupMapper.selectById(1)).thenReturn(taskGroup);
        Map<String, Object> result2 = taskGroupService.updateTaskGroup(loginUser, 1, "newName", "desc", 100);
        logger.info(result2.toString());
        Assert.assertEquals(Status.SUCCESS, result2.get(Constants.STATUS));
    }

    @Test
    public void testCloseAndStart() {

        User loginUser = getLoginUser();

        TaskGroup taskGroup1 = getTaskGroup();
        taskGroup1.setStatus(1);
        taskGroup1.setUseSize(0);


        TaskGroup taskGroup2 = getTaskGroup();
        taskGroup2.setId(2);
        taskGroup2.setUseSize(1);


        Mockito.when(taskGroupMapper.updateById(Mockito.any(TaskGroup.class))).thenReturn(1);
        Mockito.when(taskGroupMapper.selectById(Mockito.eq(1))).thenReturn(taskGroup1);
        Mockito.when(taskGroupMapper.selectById(Mockito.eq(2))).thenReturn(taskGroup2);

         //close failed
        Map<String, Object> result1 = taskGroupService.closeTaskGroup(loginUser, 2);
        Assert.assertEquals(Status.TASK_GROUP_STATUS_ERROR, result1.get(Constants.STATUS));

        // close success
        Map<String, Object> result2 = taskGroupService.closeTaskGroup(loginUser, 1);
        Assert.assertEquals(Status.SUCCESS, result2.get(Constants.STATUS));

        // start failed
        Map<String, Object> result3 = taskGroupService.startTaskGroup(loginUser, 2);
        Assert.assertEquals(Status.TASK_GROUP_STATUS_ERROR, result3.get(Constants.STATUS));

        // start success
        Map<String, Object> result4 = taskGroupService.startTaskGroup(loginUser, 1);
        Assert.assertEquals(Status.SUCCESS, result4.get(Constants.STATUS));
    }

    @Test
    public void testWakeTaskFroceManually() {

        TreeMap<Integer, Integer> tm = new TreeMap<>();
        tm.put(1,1);
        Mockito.when(processService.getWaitingTaskCache()).thenReturn(tm);
        Map<String, Object> map1 = taskGroupService.wakeTaskcompulsively(getLoginUser(), 1);
        Assert.assertEquals(Status.SUCCESS, map1.get(Constants.STATUS));

    }


}
