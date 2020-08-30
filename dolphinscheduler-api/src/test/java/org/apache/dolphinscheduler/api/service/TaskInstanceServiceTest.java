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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
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
import org.springframework.boot.test.context.SpringBootTest;

import java.text.MessageFormat;
import java.util.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class TaskInstanceServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(TaskInstanceServiceTest.class);

    @InjectMocks
    private TaskInstanceService taskInstanceService;

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ProjectService projectService;

    @Mock
    ProcessService processService;

    @Mock
    TaskInstanceMapper taskInstanceMapper;

    @Mock
    ProcessInstanceService processInstanceService;

    @Mock
    UsersService usersService;

    @Test
    public void queryTaskListPaging(){

        String projectName = "project_test1";
        User loginUser = getAdminUser();
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser,null,projectName)).thenReturn(result);
        Map<String, Object> proejctAuthFailRes = taskInstanceService.queryTaskListPaging(loginUser, "project_test1", 0, "",
                "test_user", "2019-02-26 19:48:00", "2019-02-26 19:48:22", "", null, "", 1, 20);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, proejctAuthFailRes.get(Constants.STATUS));


        //project
        putMsg(result, Status.SUCCESS, projectName);
        Project project = getProject(projectName);
        Date start = DateUtils.getScheduleDate("2020-01-01 00:00:00");
        Date end = DateUtils.getScheduleDate("2020-01-02 00:00:00");
        ProcessInstance processInstance = getProcessInstance();
        TaskInstance taskInstance = getTaskInstance();
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        Page<TaskInstance> pageReturn = new Page<>(1, 10);
        taskInstanceList.add(taskInstance);
        pageReturn.setRecords(taskInstanceList);
        when(projectMapper.queryByName(Mockito.anyString())).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
        when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(loginUser.getId());
        when(taskInstanceMapper.queryTaskInstanceListPaging(Mockito.any(Page.class), eq(project.getId()), eq(1), eq(""), eq(""),
                eq(0), Mockito.any(), eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        when(usersService.queryUser(processInstance.getExecutorId())).thenReturn(loginUser);
        when(processService.findProcessInstanceDetailById(taskInstance.getProcessInstanceId())).thenReturn(processInstance);

        Map<String, Object> successRes = taskInstanceService.queryTaskListPaging(loginUser, projectName, 1, "",
                "test_user", "2020-01-01 00:00:00", "2020-01-02 00:00:00", "", ExecutionStatus.SUCCESS, "192.168.xx.xx", 1, 20);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));

        //executor name empty
        when(taskInstanceMapper.queryTaskInstanceListPaging(Mockito.any(Page.class), eq(project.getId()), eq(1), eq(""), eq(""),
                eq(0), Mockito.any(), eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        Map<String, Object> executorEmptyRes = taskInstanceService.queryTaskListPaging(loginUser, projectName, 1, "",
                "", "2020-01-01 00:00:00", "2020-01-02 00:00:00", "", ExecutionStatus.SUCCESS, "192.168.xx.xx", 1, 20);
        Assert.assertEquals(Status.SUCCESS, executorEmptyRes.get(Constants.STATUS));

        //executor null
        when(usersService.queryUser(loginUser.getId())).thenReturn(null);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(-1);
        Map<String, Object> executorNullRes = taskInstanceService.queryTaskListPaging(loginUser, projectName, 1, "",
                "test_user", "2020-01-01 00:00:00", "2020-01-02 00:00:00", "", ExecutionStatus.SUCCESS, "192.168.xx.xx", 1, 20);
        Assert.assertEquals(Status.SUCCESS, executorNullRes.get(Constants.STATUS));
    }

    /**
     * get Mock Admin User
     * @return admin user
     */
    private User getAdminUser() {
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserName("admin");
        loginUser.setUserType(UserType.GENERAL_USER);
        return loginUser;
    }

    /**
     * get mock Project
     * @param projectName projectName
     * @return Project
     */
    private Project getProject(String projectName){
        Project project = new Project();
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return  project;
    }

    /**
     * get Mock process instance
     * @return process instance
     */
    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setName("test_process_instance");
        processInstance.setStartTime(new Date());
        processInstance.setEndTime(new Date());
        processInstance.setExecutorId(-1);
        return processInstance;
    }

    /**
     * get Mock task instance
     * @return task instance
     */
    private TaskInstance getTaskInstance() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setName("test_task_instance");
        taskInstance.setStartTime(new Date());
        taskInstance.setEndTime(new Date());
        taskInstance.setExecutorId(-1);
        return taskInstance;
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }
}