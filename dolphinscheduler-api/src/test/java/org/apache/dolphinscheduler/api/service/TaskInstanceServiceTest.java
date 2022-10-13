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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.FORCED_SUCCESS;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_INSTANCE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.TaskInstanceServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * task instance service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest(classes = ApiApplicationServer.class)
public class TaskInstanceServiceTest {

    @InjectMocks
    private TaskInstanceServiceImpl taskInstanceService;

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ProjectServiceImpl projectService;

    @Mock
    ProcessService processService;

    @Mock
    TaskInstanceMapper taskInstanceMapper;

    @Mock
    UsersService usersService;

    @Mock
    TaskDefinitionMapper taskDefinitionMapper;

    @Test
    public void queryTaskListPaging() {
        long projectCode = 1L;
        User loginUser = getAdminUser();
        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project auth fail
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_INSTANCE)).thenReturn(result);
        Result projectAuthFailRes = taskInstanceService.queryTaskListPaging(loginUser,
                projectCode,
                0,
                "",
                "",
                "",
                "test_user",
                "2019-02-26 19:48:00",
                "2019-02-26 19:48:22",
                "",
                null,
                "",
                TaskExecuteType.BATCH,
                1,
                20);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), (int) projectAuthFailRes.getCode());

        // data parameter check
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_INSTANCE)).thenReturn(result);
        Result dataParameterRes = taskInstanceService.queryTaskListPaging(loginUser,
                projectCode,
                1,
                "",
                "",
                "",
                "test_user",
                "20200101 00:00:00",
                "2020-01-02 00:00:00",
                "",
                TaskExecutionStatus.SUCCESS,
                "192.168.xx.xx",
                TaskExecuteType.BATCH,
                1,
                20);
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) dataParameterRes.getCode());

        // project
        putMsg(result, Status.SUCCESS, projectCode);
        Date start = DateUtils.stringToDate("2020-01-01 00:00:00");
        Date end = DateUtils.stringToDate("2020-01-02 00:00:00");
        ProcessInstance processInstance = getProcessInstance();
        TaskInstance taskInstance = getTaskInstance();
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        Page<TaskInstance> pageReturn = new Page<>(1, 10);
        taskInstanceList.add(taskInstance);
        pageReturn.setRecords(taskInstanceList);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectCode, TASK_INSTANCE)).thenReturn(result);
        when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(loginUser.getId());
        when(taskInstanceMapper.queryTaskInstanceListPaging(Mockito.any(Page.class), eq(project.getCode()), eq(1),
                eq(""), eq(""), eq(""),
                eq(0), Mockito.any(), eq("192.168.xx.xx"), eq(TaskExecuteType.BATCH), eq(start), eq(end)))
                        .thenReturn(pageReturn);
        when(usersService.queryUser(processInstance.getExecutorId())).thenReturn(loginUser);
        when(processService.findProcessInstanceDetailById(taskInstance.getProcessInstanceId()))
                .thenReturn(Optional.of(processInstance));

        Result successRes = taskInstanceService.queryTaskListPaging(loginUser, projectCode, 1, "", "", "",
                "test_user", "2020-01-01 00:00:00", "2020-01-02 00:00:00", "", TaskExecutionStatus.SUCCESS,
                "192.168.xx.xx", TaskExecuteType.BATCH, 1, 20);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());

        // executor name empty
        when(taskInstanceMapper.queryTaskInstanceListPaging(Mockito.any(Page.class), eq(project.getCode()), eq(1),
                eq(""), eq(""), eq(""),
                eq(0), Mockito.any(), eq("192.168.xx.xx"), eq(TaskExecuteType.BATCH), eq(start), eq(end)))
                        .thenReturn(pageReturn);
        Result executorEmptyRes = taskInstanceService.queryTaskListPaging(loginUser, projectCode, 1, "", "", "",
                "", "2020-01-01 00:00:00", "2020-01-02 00:00:00", "", TaskExecutionStatus.SUCCESS, "192.168.xx.xx",
                TaskExecuteType.BATCH, 1, 20);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) executorEmptyRes.getCode());

        // executor null
        when(usersService.queryUser(loginUser.getId())).thenReturn(null);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(-1);

        Result executorNullRes = taskInstanceService.queryTaskListPaging(loginUser, projectCode, 1, "", "", "",
                "test_user", "2020-01-01 00:00:00", "2020-01-02 00:00:00", "", TaskExecutionStatus.SUCCESS,
                "192.168.xx.xx", TaskExecuteType.BATCH, 1, 20);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) executorNullRes.getCode());

        // start/end date null
        when(taskInstanceMapper.queryTaskInstanceListPaging(Mockito.any(Page.class), eq(project.getCode()), eq(1),
                eq(""), eq(""), eq(""),
                eq(0), Mockito.any(), eq("192.168.xx.xx"), eq(TaskExecuteType.BATCH), any(), any()))
                        .thenReturn(pageReturn);
        Result executorNullDateRes = taskInstanceService.queryTaskListPaging(loginUser, projectCode, 1, "", "", "",
                "", null, null, "", TaskExecutionStatus.SUCCESS, "192.168.xx.xx", TaskExecuteType.BATCH, 1, 20);
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) executorNullDateRes.getCode());

        // start date error format
        when(taskInstanceMapper.queryTaskInstanceListPaging(Mockito.any(Page.class), eq(project.getCode()), eq(1),
                eq(""), eq(""), eq(""),
                eq(0), Mockito.any(), eq("192.168.xx.xx"), eq(TaskExecuteType.BATCH), any(), any()))
                        .thenReturn(pageReturn);

        Result executorErrorStartDateRes = taskInstanceService.queryTaskListPaging(loginUser, projectCode, 1, "", "",
                "",
                "", "error date", null, "", TaskExecutionStatus.SUCCESS, "192.168.xx.xx", TaskExecuteType.BATCH, 1, 20);
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(),
                (int) executorErrorStartDateRes.getCode());
        Result executorErrorEndDateRes = taskInstanceService.queryTaskListPaging(loginUser, projectCode, 1, "", "", "",
                "", null, "error date", "", TaskExecutionStatus.SUCCESS, "192.168.xx.xx", TaskExecuteType.BATCH, 1, 20);
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(),
                (int) executorErrorEndDateRes.getCode());
    }

    /**
     * get Mock Admin User
     *
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
     *
     * @param projectCode projectCode
     * @return Project
     */
    private Project getProject(long projectCode) {
        Project project = new Project();
        project.setCode(projectCode);
        project.setId(1);
        project.setName("project_test1");
        project.setUserId(1);
        return project;
    }

    /**
     * get Mock process instance
     *
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
     *
     * @return task instance
     */
    private TaskInstance getTaskInstance() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setName("test_task_instance");
        taskInstance.setStartTime(new Date());
        taskInstance.setEndTime(new Date());
        taskInstance.setExecutorId(-1);
        taskInstance.setTaskExecuteType(TaskExecuteType.BATCH);
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
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

    @Test
    public void forceTaskSuccess() {
        User user = getAdminUser();
        long projectCode = 1L;
        Project project = getProject(projectCode);
        int taskId = 1;
        TaskInstance task = getTaskInstance();

        Map<String, Object> mockSuccess = new HashMap<>(5);
        putMsg(mockSuccess, Status.SUCCESS);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        // user auth failed
        Map<String, Object> mockFailure = new HashMap<>(5);
        putMsg(mockFailure, Status.USER_NO_OPERATION_PROJECT_PERM, user.getUserName(), projectCode);
        when(projectService.checkProjectAndAuth(user, project, projectCode, FORCED_SUCCESS)).thenReturn(mockFailure);
        Map<String, Object> authFailRes = taskInstanceService.forceTaskSuccess(user, projectCode, taskId);
        Assertions.assertNotSame(Status.SUCCESS, authFailRes.get(Constants.STATUS));

        // test task not found
        when(projectService.checkProjectAndAuth(user, project, projectCode, FORCED_SUCCESS)).thenReturn(mockSuccess);
        when(taskInstanceMapper.selectById(Mockito.anyInt())).thenReturn(null);
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        when(taskDefinitionMapper.queryByCode(task.getTaskCode())).thenReturn(taskDefinition);
        Map<String, Object> taskNotFoundRes = taskInstanceService.forceTaskSuccess(user, projectCode, taskId);
        Assertions.assertEquals(Status.TASK_INSTANCE_NOT_FOUND, taskNotFoundRes.get(Constants.STATUS));

        // test task instance state error
        task.setState(TaskExecutionStatus.SUCCESS);
        when(taskInstanceMapper.selectById(1)).thenReturn(task);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(user, project, projectCode, FORCED_SUCCESS)).thenReturn(result);
        Map<String, Object> taskStateErrorRes = taskInstanceService.forceTaskSuccess(user, projectCode, taskId);
        Assertions.assertEquals(Status.TASK_INSTANCE_STATE_OPERATION_ERROR, taskStateErrorRes.get(Constants.STATUS));

        // test error
        task.setState(TaskExecutionStatus.FAILURE);
        when(taskInstanceMapper.updateById(task)).thenReturn(0);
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(user, project, projectCode, FORCED_SUCCESS)).thenReturn(result);
        Map<String, Object> errorRes = taskInstanceService.forceTaskSuccess(user, projectCode, taskId);
        Assertions.assertEquals(Status.FORCE_TASK_SUCCESS_ERROR, errorRes.get(Constants.STATUS));

        // test success
        task.setState(TaskExecutionStatus.FAILURE);
        when(taskInstanceMapper.updateById(task)).thenReturn(1);
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(user, project, projectCode, FORCED_SUCCESS)).thenReturn(result);
        Map<String, Object> successRes = taskInstanceService.forceTaskSuccess(user, projectCode, taskId);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }
}
