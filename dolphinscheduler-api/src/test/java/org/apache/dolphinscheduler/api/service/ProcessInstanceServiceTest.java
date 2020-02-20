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
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.mapper.*;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.assertj.core.error.future.Warning;
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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class ProcessInstanceServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceServiceTest.class);

    @InjectMocks
    ProcessInstanceService processInstanceService;

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ProjectService projectService;

    @Mock
    ProcessService processService;

    @Mock
    ProcessInstanceMapper processInstanceMapper;

    @Mock
    ProcessDefinitionMapper processDefineMapper;

    @Mock
    ProcessDefinitionService processDefinitionService;

    @Mock
    ExecutorService execService;

    @Mock
    TaskInstanceMapper taskInstanceMapper;

    @Mock
    LoggerService loggerService;

    @Mock
    WorkerGroupMapper workerGroupMapper;

    @Mock
    UsersService usersService;

    @Test
    public void testQueryProcessInstanceList() {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser,null,projectName)).thenReturn(result);
        Map<String, Object> proejctAuthFailRes = processInstanceService.queryProcessInstanceList(loginUser, projectName, 46, "2020-01-01 00:00:00",
                "2020-01-02 00:00:00", "", "test_user", ExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", 1, 10);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, proejctAuthFailRes.get(Constants.STATUS));

        //project auth success
        putMsg(result, Status.SUCCESS, projectName);
        Project project = getProject(projectName);
        Date start = DateUtils.getScheduleDate("2020-01-01 00:00:00");
        Date end = DateUtils.getScheduleDate("2020-01-02 00:00:00");
        ProcessInstance processInstance = getProcessInstance();
        List<ProcessInstance> processInstanceList = new ArrayList<>();
        Page<ProcessInstance> pageReturn = new Page<>(1, 10);
        processInstanceList.add(processInstance);
        pageReturn.setRecords(processInstanceList);
        when(projectMapper.queryByName(projectName)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
        when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(loginUser.getId());
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class), eq(project.getId()), eq(1), eq(""), eq(-1), Mockito.any(),
                eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        when(usersService.queryUser(processInstance.getExecutorId())).thenReturn(loginUser);
        Map<String, Object> successRes = processInstanceService.queryProcessInstanceList(loginUser, projectName, 1, "2020-01-01 00:00:00",
                "2020-01-02 00:00:00", "", loginUser.getUserName(), ExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", 1, 10);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));

        //executor null
        when(usersService.queryUser(loginUser.getId())).thenReturn(null);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(-1);
        Map<String, Object> executorExistRes = processInstanceService.queryProcessInstanceList(loginUser, projectName, 1, "2020-01-01 00:00:00",
                "2020-01-02 00:00:00", "", "admin", ExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", 1, 10);
        Assert.assertEquals(Status.SUCCESS, executorExistRes.get(Constants.STATUS));

        //executor name empty
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class), eq(project.getId()), eq(1), eq(""), eq(0), Mockito.any(),
                eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        Map<String, Object> executorEmptyRes = processInstanceService.queryProcessInstanceList(loginUser, projectName, 1, "2020-01-01 00:00:00",
                "2020-01-02 00:00:00", "", "", ExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", 1, 10);
        Assert.assertEquals(Status.SUCCESS, executorEmptyRes.get(Constants.STATUS));

    }

    @Test
    public void testQueryProcessInstanceById() {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser,null,projectName)).thenReturn(result);
        Map<String, Object> proejctAuthFailRes = processInstanceService.queryProcessInstanceById(loginUser, projectName, 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, proejctAuthFailRes.get(Constants.STATUS));

        //project auth success
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setWorkerGroupId(-1);
        processInstance.setReceivers("xxx@qq.com");
        processInstance.setReceiversCc("xxx@qq.com");
        processInstance.setProcessDefinitionId(46);
        putMsg(result, Status.SUCCESS, projectName);
        Project project = getProject(projectName);
        ProcessDefinition processDefinition = getProcessDefinition();
        when(projectMapper.queryByName(projectName)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(processInstance.getId())).thenReturn(processInstance);
        when(processService.findProcessDefineById(processInstance.getProcessDefinitionId())).thenReturn(processDefinition);
        Map<String, Object> successRes = processInstanceService.queryProcessInstanceById(loginUser, projectName, 1);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));

        //worker group null
        processInstance.setWorkerGroupId(1);
        when(workerGroupMapper.selectById(processInstance.getWorkerGroupId())).thenReturn(null);
        Map<String, Object> workerNullRes = processInstanceService.queryProcessInstanceById(loginUser, projectName, 1);
        Assert.assertEquals(Status.SUCCESS, workerNullRes.get(Constants.STATUS));

        //worker group exist
        WorkerGroup workerGroup = getWorkGroup();
        when(workerGroupMapper.selectById(processInstance.getWorkerGroupId())).thenReturn(workerGroup);
        processInstance.setWorkerGroupId(1);
        when(workerGroupMapper.selectById(processInstance.getWorkerGroupId())).thenReturn(null);
        Map<String, Object> workerExistRes = processInstanceService.queryProcessInstanceById(loginUser, projectName, 1);
        Assert.assertEquals(Status.SUCCESS, workerExistRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryTaskListByProcessId() throws IOException {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser,null,projectName)).thenReturn(result);
        Map<String, Object> proejctAuthFailRes = processInstanceService.queryTaskListByProcessId(loginUser, projectName, 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, proejctAuthFailRes.get(Constants.STATUS));

        //project auth success
        putMsg(result, Status.SUCCESS, projectName);
        Project project = getProject(projectName);
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setState(ExecutionStatus.SUCCESS);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskType(TaskType.SHELL.getDescp());
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        taskInstanceList.add(taskInstance);
        Result res = new Result();
        res.setCode(Status.SUCCESS.ordinal());
        res.setData("xxx");
        when(projectMapper.queryByName(projectName)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(processInstance.getId())).thenReturn(processInstance);
        when(processService.findValidTaskListByProcessId(processInstance.getId())).thenReturn(taskInstanceList);
        when(loggerService.queryLog(taskInstance.getId(), 0, 4098)).thenReturn(res);
        Map<String, Object> successRes = processInstanceService.queryTaskListByProcessId(loginUser, projectName, 1);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }



    @Test
    public void testDependResult(){
        String logString = "[INFO] 2019-03-19 17:11:08.475 org.apache.dolphinscheduler.server.worker.log.TaskLogger:[172] - [taskAppId=TASK_223_10739_452334] dependent item complete :|| 223-ALL-day-last1Day,SUCCESS\n" +
                "[INFO] 2019-03-19 17:11:08.476 org.apache.dolphinscheduler.server.worker.runner.TaskScheduleThread:[172] - task : 223_10739_452334 exit status code : 0\n" +
                "[root@node2 current]# ";
        try {
            Map<String, DependResult> resultMap =
                    processInstanceService.parseLogForDependentResult(logString);
            Assert.assertEquals(1 , resultMap.size());
        } catch (IOException e) {

        }
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
        return processInstance;
    }

    /**
     * get mock processDefinition
     * @return ProcessDefinition
     */
    private ProcessDefinition getProcessDefinition(){
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(46);
        processDefinition.setName("test_pdf");
        processDefinition.setProjectId(2);
        processDefinition.setTenantId(1);
        processDefinition.setDescription("");
        return  processDefinition;
    }

    /**
     * get Mock worker group
     * @return worker group
     */
    private WorkerGroup getWorkGroup() {
        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.setId(1);
        workerGroup.setName("test_workergroup");
        return workerGroup;
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