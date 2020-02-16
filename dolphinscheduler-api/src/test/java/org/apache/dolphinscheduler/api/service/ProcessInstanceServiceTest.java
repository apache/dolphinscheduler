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
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.*;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Assert;
import org.junit.Ignore;
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

    @Ignore
    @Test
    public void testQueryProcessInstanceList() {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(null);
        Mockito.when(projectService.checkProjectAndAuth(loginUser,null,projectName)).thenReturn(result);
        Map<String, Object> proejctAuthFailRes = processInstanceService.queryProcessInstanceList(loginUser, projectName, 46, "2020-01-01 00:00:00",
                "2020-01-02 00:00:00", "", "test_user", ExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", 1, 10);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, proejctAuthFailRes.get(Constants.STATUS));

        //
        putMsg(result, Status.SUCCESS, projectName);
        Project project = getProject(projectName);
        int[] statusArray = new int[]{ExecutionStatus.SUBMITTED_SUCCESS.ordinal()};
        Page<ProcessInstance> page = new Page(1, 10);
        Date start = DateUtils.getScheduleDate("2020-01-01 00:00:00");
        Date end = DateUtils.getScheduleDate("2020-01-02 00:00:00");
        ProcessInstance processInstance = getProcessInstance();
        List<ProcessInstance> processInstanceList = new ArrayList<>();
        Page<ProcessInstance> pageReturn = new Page(1, 10);
        processInstanceList.add(processInstance);
        pageReturn.setRecords(processInstanceList);
        Mockito.when(projectService.checkProjectAndAuth(loginUser,project,projectName)).thenReturn(result);
        Mockito.when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        Mockito.when(usersService.queryUser(loginUser.getUserName())).thenReturn(loginUser);
        Mockito.when(processInstanceMapper.queryProcessInstanceListPaging(page, project.getId(), 1, "", -1, statusArray,
                "192.168.xx.xx", start, end)).thenReturn(pageReturn);

        Map<String, Object> successRes = processInstanceService.queryProcessInstanceList(loginUser, projectName, 1, "2020-01-01 00:00:00",
                "2020-01-02 00:00:00", "", loginUser.getUserName(), ExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", 1, 10);
        Assert.assertEquals(Status.SUCCESS, proejctAuthFailRes.get(Constants.STATUS));

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

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }


}