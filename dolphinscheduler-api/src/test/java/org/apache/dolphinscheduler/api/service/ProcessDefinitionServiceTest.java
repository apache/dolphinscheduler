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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_DEFINITION_MOVE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_BATCH_COPY;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_DEFINITION;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_DEFINITION_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_IMPORT;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_TREE_VIEW;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_UPDATE;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.ProcessDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskMainInfo;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * process definition service test
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessDefinitionServiceTest {

    private static final String taskRelationJson = "[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":123456789,"
            + "\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":\"{}\"},{\"name\":\"\",\"preTaskCode\":123456789,"
            + "\"preTaskVersion\":1,\"postTaskCode\":123451234,\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":\"{}\"}]";

    private static final String taskDefinitionJson = "[{\"code\":123456789,\"name\":\"test1\",\"version\":1,\"description\":\"\",\"delayTime\":0,\"taskType\":\"SHELL\","
            + "\"taskParams\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 1\",\"dependence\":{},\"conditionResult\":{\"successNode\":[],\"failedNode\":[]},\"waitStartTimeout\":{},"
            + "\"switchResult\":{}},\"flag\":\"YES\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":1,\"timeoutFlag\":\"CLOSE\","
            + "\"timeoutNotifyStrategy\":null,\"timeout\":0,\"environmentCode\":-1},{\"code\":123451234,\"name\":\"test2\",\"version\":1,\"description\":\"\",\"delayTime\":0,\"taskType\":\"SHELL\","
            + "\"taskParams\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 2\",\"dependence\":{},\"conditionResult\":{\"successNode\":[],\"failedNode\":[]},\"waitStartTimeout\":{},"
            + "\"switchResult\":{}},\"flag\":\"YES\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":1,\"timeoutFlag\":\"CLOSE\","
            + "\"timeoutNotifyStrategy\":\"WARN\",\"timeout\":0,\"environmentCode\":-1}]";

    @InjectMocks
    private ProcessDefinitionServiceImpl processDefinitionService;

    @Mock
    private ProcessDefinitionMapper processDefineMapper;

    @Mock
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private ProcessService processService;

    @Mock
    private ProcessInstanceService processInstanceService;

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private DataSourceMapper dataSourceMapper;

    @Mock
    private WorkFlowLineageService workFlowLineageService;

    @Test
    public void testQueryProcessDefinitionList() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Result result = new Result();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), eq(project), eq(projectCode), eq(WORKFLOW_DEFINITION));
        // project not found
        Result map = processDefinitionService.queryProcessDefinitionList(loginUser, projectCode);
        Assert.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), (int)map.getCode());

        // project check auth success
        putMsg(result, Status.SUCCESS, projectCode);
        List<ProcessDefinition> resourceList = new ArrayList<>();
        resourceList.add(getProcessDefinition());
        Mockito.when(processDefineMapper.queryAllDefinitionList(project.getCode())).thenReturn(resourceList);
        Result checkSuccessRes = processDefinitionService.queryProcessDefinitionList(loginUser, projectCode);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)checkSuccessRes.getCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testQueryProcessDefinitionListPaging() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        //project not found
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_DEFINITION)).thenReturn(result);
        Result map = processDefinitionService.queryProcessDefinitionListPaging(loginUser, projectCode, "", "", 1, 5, 0);
        Assert.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), (int) map.getCode());

        putMsg(result, Status.SUCCESS, projectCode);
        loginUser.setId(1);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectCode,WORKFLOW_DEFINITION)).thenReturn(result);
        Page<ProcessDefinition> page = new Page<>(1, 10);
        page.setTotal(30);
        Mockito.when(processDefineMapper.queryDefineListPaging(
                Mockito.any(IPage.class)
                , Mockito.eq("")
                , Mockito.eq(loginUser.getId())
                , Mockito.eq(project.getCode())
                , Mockito.anyBoolean())).thenReturn(page);

        Result map1 = processDefinitionService.queryProcessDefinitionListPaging(
                loginUser, 1L, "", "",1, 10, loginUser.getId());

        Assert.assertEquals(Status.SUCCESS.getMsg(), map1.getMsg());
    }

    @Test
    public void testQueryProcessDefinitionByCode() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Tenant tenant = new Tenant();
        tenant.setId(1);
        tenant.setTenantCode("root");
        Result result = new Result();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), any(), anyLong(), eq(WORKFLOW_DEFINITION));

        // project check auth fail
        Result map = processDefinitionService.queryProcessDefinitionByCode(loginUser, 1L, 1L);
        Assert.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), (int)map.getCode());

        // project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectCode);
        DagData dagData = new DagData(getProcessDefinition(), null, null);
        Mockito.when(processService.genDagData(Mockito.any())).thenReturn(dagData);

        Result instanceNotExistRes = processDefinitionService.queryProcessDefinitionByCode(loginUser, projectCode, 1L);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), (int)instanceNotExistRes.getCode());

        // instance exit
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(getProcessDefinition());
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(tenantMapper.queryById(1)).thenReturn(tenant);
        Result successRes = processDefinitionService.queryProcessDefinitionByCode(loginUser, projectCode, 46L);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)successRes.getCode());
    }

    @Test
    public void testQueryProcessDefinitionByName() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Result result = new Result();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), any(), eq(projectCode), eq(WORKFLOW_DEFINITION));


        // project check auth fail
        Result map = processDefinitionService.queryProcessDefinitionByName(loginUser, projectCode, "test_def");
        Assert.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), (int)map.getCode());

        // project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(processDefineMapper.queryByDefineName(project.getCode(), "test_def")).thenReturn(null);

        Result instanceNotExistRes = processDefinitionService.queryProcessDefinitionByName(loginUser, projectCode, "test_def");
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), (int)instanceNotExistRes.getCode());

        // instance exit
        Mockito.when(processDefineMapper.queryByDefineName(project.getCode(), "test")).thenReturn(getProcessDefinition());
        putMsg(result, Status.SUCCESS, projectCode);
        Result successRes = processDefinitionService.queryProcessDefinitionByName(loginUser, projectCode, "test");
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)successRes.getCode());
    }

    @Test
    public void testBatchCopyProcessDefinition() {
        long projectCode = 1L;
        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Result result = new Result();
        putMsg(result, Status.SUCCESS, projectCode);
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), any(), anyLong(), eq(WORKFLOW_BATCH_COPY));

        // copy project definition ids empty test
        Result map = processDefinitionService.batchCopyProcessDefinition(loginUser, projectCode, StringUtils.EMPTY, 2L);
        Assert.assertEquals(Status.PROCESS_DEFINITION_CODES_IS_EMPTY.getCode(), (int)map.getCode());

        // project check auth fail
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        Result map1 = processDefinitionService.batchCopyProcessDefinition(loginUser, projectCode, String.valueOf(project.getId()), 2L);
        Assert.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), (int)map1.getCode());

        // project check auth success, target project name not equal project name, check auth target project fail
        projectCode = 2L;
        Project project1 = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project1);

        putMsg(result, Status.SUCCESS, projectCode);
        ProcessDefinition definition = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(definition);
        Set<Long> definitionCodes = Arrays.stream("46".split(Constants.COMMA)).map(Long::parseLong).collect(Collectors.toSet());
        Mockito.when(processDefineMapper.queryByCodes(definitionCodes)).thenReturn(processDefinitionList);
        Mockito.when(processService.saveProcessDefine(loginUser, definition, Boolean.TRUE, Boolean.TRUE)).thenReturn(2);
        Result map3 = processDefinitionService.batchCopyProcessDefinition(loginUser, projectCode, "46", 1L);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)map3.getCode());
    }

    @Test
    public void testBatchMoveProcessDefinition() {
        long projectCode = 1L;
        Project project1 = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project1);

        long projectCode2 = 2L;
        Project project2 = getProject(projectCode2);
        Mockito.when(projectMapper.queryByCode(projectCode2)).thenReturn(project2);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Result result = new Result();
        putMsg(result, Status.SUCCESS, projectCode);

        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), any(), anyLong(), eq(TASK_DEFINITION_MOVE));

        ProcessDefinition definition = getProcessDefinition();
        definition.setVersion(1);
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(definition);
        Set<Long> definitionCodes = Arrays.stream("46".split(Constants.COMMA)).map(Long::parseLong).collect(Collectors.toSet());
        Mockito.when(processDefineMapper.queryByCodes(definitionCodes)).thenReturn(processDefinitionList);
        Mockito.when(processService.saveProcessDefine(loginUser, definition, Boolean.TRUE, Boolean.TRUE)).thenReturn(2);
        Mockito.when(processTaskRelationMapper.queryByProcessCode(projectCode, 46L)).thenReturn(getProcessTaskRelation(projectCode));
        putMsg(result, Status.SUCCESS);

        Result successRes = processDefinitionService.batchMoveProcessDefinition(loginUser, projectCode, "46", projectCode2);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)successRes.getCode());
    }

    @Test
    public void deleteProcessDefinitionByCodeTest() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        // project check auth fail
        Result result = new Result();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), any(), anyLong(), eq(WORKFLOW_DEFINITION_DELETE));

        Result map = processDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, 6L);
        Assert.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), (int)map.getCode());

        // project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(processDefineMapper.queryByCode(1L)).thenReturn(null);
        Result instanceNotExistRes = processDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, 1L);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), (int)instanceNotExistRes.getCode());

        ProcessDefinition processDefinition = getProcessDefinition();
        putMsg(result, Status.SUCCESS, projectCode);
        // user no auth
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);
        Result userNoAuthRes = processDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, 46L);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int)userNoAuthRes.getCode());

        // process definition online
        loginUser.setUserType(UserType.ADMIN_USER);
        putMsg(result, Status.SUCCESS, projectCode);
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);
        Throwable exception = Assertions.assertThrows(ServiceException.class, () -> processDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, 46L));
        String formatter = MessageFormat.format(Status.PROCESS_DEFINE_STATE_ONLINE.getMsg(), processDefinition.getName());
        Assertions.assertEquals(formatter, exception.getMessage());

        // scheduler list elements > 1
        processDefinition.setReleaseState(ReleaseState.OFFLINE);
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(scheduleMapper.queryByProcessDefinitionCode(46L)).thenReturn(getSchedule());
        Mockito.when(scheduleMapper.deleteById(46)).thenReturn(1);
        Mockito.when(processDefineMapper.deleteById(processDefinition.getId())).thenReturn(1);
        Mockito.when(processTaskRelationMapper.deleteByCode(project.getCode(), processDefinition.getCode())).thenReturn(1);
        Mockito.when(workFlowLineageService.queryTaskDepOnProcess(project.getCode(), processDefinition.getCode())).thenReturn(Collections.emptySet());
        Result schedulerGreaterThanOneRes = processDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, 46L);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)schedulerGreaterThanOneRes.getCode());

        // scheduler online
        Schedule schedule = getSchedule();
        schedule.setReleaseState(ReleaseState.ONLINE);
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(scheduleMapper.queryByProcessDefinitionCode(46L)).thenReturn(schedule);
        Mockito.when(workFlowLineageService.queryTaskDepOnProcess(project.getCode(), processDefinition.getCode())).thenReturn(Collections.emptySet());
        Result schedulerOnlineRes = processDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, 46L);
        Assert.assertEquals(Status.SCHEDULE_CRON_STATE_ONLINE.getCode(), (int)schedulerOnlineRes.getCode());

        // process used by other task, sub process
        loginUser.setUserType(UserType.ADMIN_USER);
        putMsg(result, Status.SUCCESS, projectCode);
        TaskMainInfo taskMainInfo = getTaskMainInfo().get(0);
        Mockito.when(workFlowLineageService.queryTaskDepOnProcess(project.getCode(), processDefinition.getCode())).thenReturn(ImmutableSet.copyOf(getTaskMainInfo()));
        exception = Assertions.assertThrows(ServiceException.class, () -> processDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, 46L));
        formatter = MessageFormat.format(Status.DELETE_PROCESS_DEFINITION_USE_BY_OTHER_FAIL.getMsg(), String.format("%s:%s", taskMainInfo.getProcessDefinitionName(), taskMainInfo.getTaskName()));
        Assertions.assertEquals(formatter, exception.getMessage());

        // delete success
        schedule.setReleaseState(ReleaseState.OFFLINE);
        Mockito.when(processDefineMapper.deleteById(46)).thenReturn(1);
        Mockito.when(scheduleMapper.deleteById(schedule.getId())).thenReturn(1);
        Mockito.when(processTaskRelationMapper.deleteByCode(project.getCode(), processDefinition.getCode())).thenReturn(1);
        Mockito.when(scheduleMapper.queryByProcessDefinitionCode(46L)).thenReturn(getSchedule());
        Mockito.when(workFlowLineageService.queryTaskDepOnProcess(project.getCode(), processDefinition.getCode())).thenReturn(Collections.emptySet());
        putMsg(result, Status.SUCCESS, projectCode);
        Result deleteSuccess = processDefinitionService.deleteProcessDefinitionByCode(loginUser, projectCode, 46L);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)deleteSuccess.getCode());
    }

    @Test
    @Ignore
    public void testReleaseProcessDefinition() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);

        // project check auth fail
        Result result = new Result();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), eq(project), eq(projectCode), null);

        Result map = processDefinitionService.releaseProcessDefinition(loginUser, projectCode, 6, ReleaseState.OFFLINE);
        Assert.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), (int)map.getCode());

        // project check auth success, processs definition online
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(getProcessDefinition());
        List<ProcessTaskRelation> processTaskRelationList = new ArrayList<>();
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setProjectCode(projectCode);
        processTaskRelation.setProcessDefinitionCode(46L);
        processTaskRelation.setPostTaskCode(123L);
        processTaskRelationList.add(processTaskRelation);
        Mockito.when(processService.findRelationByCode(46L, 1)).thenReturn(processTaskRelationList);
        Result onlineRes = processDefinitionService.releaseProcessDefinition(
                loginUser, projectCode, 46, ReleaseState.ONLINE);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)onlineRes.getCode());

        // project check auth success, processs definition online
        Result onlineWithResourceRes = processDefinitionService.releaseProcessDefinition(
                loginUser, projectCode, 46, ReleaseState.ONLINE);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)onlineWithResourceRes.getCode());

        // release error code
        Result failRes = processDefinitionService.releaseProcessDefinition(
                loginUser, projectCode, 46, ReleaseState.getEnum(2));
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int)failRes.getCode());
    }

    @Test
    public void testVerifyProcessDefinitionName() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        // project check auth fail
        Result result = new Result();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), eq(project), eq(projectCode), eq(WORKFLOW_CREATE));

        Result map = processDefinitionService.verifyProcessDefinitionName(loginUser,
                projectCode, "test_pdf");
        Assert.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), (int)map.getCode());

        // project check auth success, process not exist
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(processDefineMapper.verifyByDefineName(project.getCode(), "test_pdf")).thenReturn(null);
        Result processNotExistRes = processDefinitionService.verifyProcessDefinitionName(loginUser, projectCode, "test_pdf");
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)processNotExistRes.getCode());

        // process exist
        Mockito.when(processDefineMapper.verifyByDefineName(project.getCode(), "test_pdf")).thenReturn(getProcessDefinition());
        Result processExistRes = processDefinitionService.verifyProcessDefinitionName(loginUser,
                projectCode, "test_pdf");
        Assert.assertEquals(Status.PROCESS_DEFINITION_NAME_EXIST.getCode(), (int)processExistRes.getCode());
    }

    @Test
    public void testCheckProcessNodeList() {
        Result dataNotValidRes = processDefinitionService.checkProcessNodeList(null, null);
        Assert.assertEquals(Status.DATA_IS_NOT_VALID.getCode(), (int)dataNotValidRes.getCode());

        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);

        Result taskEmptyRes = processDefinitionService.checkProcessNodeList(taskRelationJson, taskDefinitionLogs);
        Assert.assertEquals(Status.PROCESS_DAG_IS_EMPTY.getCode(), (int)taskEmptyRes.getCode());
    }

    @Test
    public void testGetTaskNodeListByDefinitionCode() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        // project check auth fail
        Result result = new Result();
        putMsg(result, Status.SUCCESS, projectCode);
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), eq(project), anyLong(), eq(null));

        // process definition not exist
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(null);
        Result processDefinitionNullRes = processDefinitionService.getTaskNodeListByDefinitionCode(loginUser, projectCode, 46L);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), (int)processDefinitionNullRes.getCode());

        // success
        ProcessDefinition processDefinition = getProcessDefinition();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(processService.genDagData(Mockito.any())).thenReturn(new DagData(processDefinition, null, null));
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);
        Result dataNotValidRes = processDefinitionService.getTaskNodeListByDefinitionCode(loginUser, projectCode, 46L);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)dataNotValidRes.getCode());
    }

    @Test
    public void testGetTaskNodeListByDefinitionCodes() {
        long projectCode = 1L;
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        // project check auth fail
        Result result = new Result();
        putMsg(result, Status.SUCCESS, projectCode);
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), eq(project), anyLong(), eq(null));

        // process definition not exist
        String defineCodes = "46";
        Set<Long> defineCodeSet = Lists.newArrayList(defineCodes.split(Constants.COMMA)).stream().map(Long::parseLong).collect(Collectors.toSet());
        Mockito.when(processDefineMapper.queryByCodes(defineCodeSet)).thenReturn(null);
        Result processNotExistRes = processDefinitionService.getNodeListMapByDefinitionCodes(loginUser, projectCode, defineCodes);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), (int)processNotExistRes.getCode());

        putMsg(result, Status.SUCCESS, projectCode);
        ProcessDefinition processDefinition = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(processDefinition);

        Mockito.when(processDefineMapper.queryByCodes(defineCodeSet)).thenReturn(processDefinitionList);
        Mockito.when(processService.genDagData(Mockito.any())).thenReturn(new DagData(processDefinition, null, null));
        Project project1 = getProject(projectCode);
        List<Project> projects = new ArrayList<>();
        projects.add(project1);
        Mockito.when(projectMapper.queryProjectCreatedAndAuthorizedByUserId(loginUser.getId())).thenReturn(projects);

        Result successRes = processDefinitionService.getNodeListMapByDefinitionCodes(loginUser, projectCode, defineCodes);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)successRes.getCode());
    }

    @Test
    public void testQueryAllProcessDefinitionByProjectCode() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        long projectCode = 2L;
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        Result result = new Result();
        putMsg(result, Status.SUCCESS, projectCode);
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), eq(project), anyLong(), eq(WORKFLOW_DEFINITION));

        ProcessDefinition processDefinition = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(processDefinition);
        Mockito.when(processDefineMapper.queryAllDefinitionList(projectCode)).thenReturn(processDefinitionList);
        Result successRes = processDefinitionService.queryAllProcessDefinitionByProjectCode(loginUser, projectCode);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)successRes.getCode());
    }

    @Test
    public void testViewTree() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setTenantId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        long projectCode =  1;
        Project project1 = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(1)).thenReturn(project1);

        Result result = new Result();
        putMsg(result, Status.SUCCESS, projectCode);
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), eq(project1), anyLong(), eq(WORKFLOW_TREE_VIEW));

        // process definition not exist
        ProcessDefinition processDefinition = getProcessDefinition();
        Result processDefinitionNullRes = processDefinitionService.viewTree(loginUser,processDefinition.getProjectCode(),46, 10);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), (int)processDefinitionNullRes.getCode());

        // task instance not existproject
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectMapper.queryByCode(1)).thenReturn(project1);
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);
        Mockito.when(processService.genDagGraph(processDefinition)).thenReturn(new DAG<>());
        Result taskNullRes = processDefinitionService.viewTree(loginUser,processDefinition.getProjectCode(),46, 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)taskNullRes.getCode());

        // task instance exist
        Result taskNotNuLLRes = processDefinitionService.viewTree(loginUser,processDefinition.getProjectCode(),46, 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)taskNotNuLLRes.getCode());
    }

    @Test
    public void testSubProcessViewTree() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        ProcessDefinition processDefinition = getProcessDefinition();
        Mockito.when(processDefineMapper.queryByCode(46L)).thenReturn(processDefinition);

        Project project1 = getProject(1);
        Mockito.when(projectMapper.queryByCode(1)).thenReturn(project1);
        Result result = new Result();
        putMsg(result, Status.SUCCESS);
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            res.setCode(result.getCode());
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), eq(project1), anyLong(), eq(WORKFLOW_TREE_VIEW));

        Mockito.when(processService.genDagGraph(processDefinition)).thenReturn(new DAG<>());
        Result taskNotNuLLRes = processDefinitionService.viewTree(loginUser,processDefinition.getProjectCode(), 46, 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int)taskNotNuLLRes.getCode());
    }

    @Test
    public void testUpdateProcessDefinition() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        long projectCode = 1L;
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            putMsg(res, Status.SUCCESS);
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), eq(project), eq(projectCode), eq(WORKFLOW_UPDATE));

        Result updateResult = processDefinitionService.updateProcessDefinition(loginUser, projectCode, "test", 1,
                "", "", "", 0, "root", null,"",null, ProcessExecutionTypeEnum.PARALLEL);
        Assert.assertEquals(Status.DATA_IS_NOT_VALID.getCode(), (int)updateResult.getCode());
    }

    @Test
    public void testBatchExportProcessDefinitionByCodes() {
        processDefinitionService.batchExportProcessDefinitionByCodes(null, 1L, null, null);

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        long projectCode = 1L;
        Project project = getProject(projectCode);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        processDefinitionService.batchExportProcessDefinitionByCodes(
                loginUser, projectCode, "1", null);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(1);
        Map<String, Object> checkResult = new HashMap<>();
        checkResult.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        HttpServletResponse response = mock(HttpServletResponse.class);

        DagData dagData = new DagData(getProcessDefinition(), null, null);
        Mockito.when(processService.genDagData(Mockito.any())).thenReturn(dagData);
        processDefinitionService.batchExportProcessDefinitionByCodes(loginUser, projectCode, "1", response);
        Assert.assertNotNull(processDefinitionService.exportProcessDagData(processDefinition));
    }

    @Test
    public void testImportSqlProcessDefinition() throws Exception {
        int userId = 10;
        User loginUser = Mockito.mock(User.class);
        Mockito.when(loginUser.getId()).thenReturn(userId);
        Mockito.when(loginUser.getTenantId()).thenReturn(2);
        Mockito.when(loginUser.getUserType()).thenReturn(UserType.GENERAL_USER);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream outputStream = new ZipOutputStream(byteArrayOutputStream);
        outputStream.putNextEntry(new ZipEntry("import_sql/"));

        outputStream.putNextEntry(new ZipEntry("import_sql/a.sql"));
        outputStream.write("-- upstream: start_auto_dag\n-- datasource: mysql_1\nselect 1;".getBytes(StandardCharsets.UTF_8));

        outputStream.putNextEntry(new ZipEntry("import_sql/b.sql"));
        outputStream.write("-- name: start_auto_dag\n-- datasource: mysql_1\nselect 1;".getBytes(StandardCharsets.UTF_8));

        outputStream.close();

        MockMultipartFile mockMultipartFile = new MockMultipartFile("import_sql.zip", byteArrayOutputStream.toByteArray());

        DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(dataSource.getId()).thenReturn(1);
        Mockito.when(dataSource.getType()).thenReturn(DbType.MYSQL);

        Mockito.when(dataSourceMapper.queryDataSourceByNameAndUserId(userId, "mysql_1")).thenReturn(dataSource);

        long projectCode =  1001;
        Project project1 = getProject(projectCode);

        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        doAnswer(invocation -> {
            Result res = invocation.getArgument(0);
            putMsg(res, Status.SUCCESS);
            return null;
        }).when(projectService).checkProjectAndAuth(any(), eq(loginUser), eq(project1), eq(projectCode), eq(WORKFLOW_IMPORT));
        Mockito.when(processService.saveTaskDefine(Mockito.same(loginUser), Mockito.eq(projectCode), Mockito.notNull(), Mockito.anyBoolean())).thenReturn(2);
        Mockito.when(processService.saveProcessDefine(Mockito.same(loginUser), Mockito.notNull(), Mockito.notNull(), Mockito.anyBoolean())).thenReturn(1);
        Mockito.when(processService.saveTaskRelation(Mockito.same(loginUser), Mockito.eq(projectCode), Mockito.anyLong(),
            Mockito.eq(1), Mockito.notNull(), Mockito.notNull(), Mockito.anyBoolean())).thenReturn(0);
        Result result = processDefinitionService.importSqlProcessDefinition(loginUser, projectCode, mockMultipartFile);

        Assert.assertEquals(Status.SUCCESS.getCode(), (int)result.getCode());
    }

    /**
     * get mock processDefinition
     *
     * @return ProcessDefinition
     */
    private ProcessDefinition getProcessDefinition() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(46);
        processDefinition.setProjectCode(1L);
        processDefinition.setName("test_pdf");
        processDefinition.setTenantId(1);
        processDefinition.setDescription("");
        processDefinition.setCode(46L);
        processDefinition.setVersion(1);
        return processDefinition;
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
        project.setName("test");
        project.setUserId(1);
        return project;
    }

    private List<ProcessTaskRelation> getProcessTaskRelation(long projectCode) {
        List<ProcessTaskRelation> processTaskRelations = new ArrayList<>();
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setProjectCode(projectCode);
        processTaskRelation.setProcessDefinitionCode(46L);
        processTaskRelation.setProcessDefinitionVersion(1);
        processTaskRelations.add(processTaskRelation);
        return processTaskRelations;
    }

    /**
     * get mock schedule
     *
     * @return schedule
     */
    private Schedule getSchedule() {
        Date date = new Date();
        Schedule schedule = new Schedule();
        schedule.setId(46);
        schedule.setProcessDefinitionCode(1);
        schedule.setStartTime(date);
        schedule.setEndTime(date);
        schedule.setCrontab("0 0 5 * * ? *");
        schedule.setFailureStrategy(FailureStrategy.END);
        schedule.setUserId(1);
        schedule.setReleaseState(ReleaseState.OFFLINE);
        schedule.setProcessInstancePriority(Priority.MEDIUM);
        schedule.setWarningType(WarningType.NONE);
        schedule.setWarningGroupId(1);
        schedule.setWorkerGroup(Constants.DEFAULT_WORKER_GROUP);
        return schedule;
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    private void putMsg(Result result, Status status, Object... statusParams) {
        result.setCode(status.getCode());
        if (statusParams != null && statusParams.length > 0) {
            result.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.setMsg(status.getMsg());
        }
    }

    /**
     * get mock task main info
     *
     * @return schedule
     */
    private List<TaskMainInfo> getTaskMainInfo() {
        List<TaskMainInfo> taskMainInfos = new ArrayList<>();
        TaskMainInfo taskMainInfo = new TaskMainInfo();
        taskMainInfo.setId(1);
        taskMainInfo.setProcessDefinitionName("process");
        taskMainInfo.setTaskName("task");
        taskMainInfos.add(taskMainInfo);
        return taskMainInfos;
    }

}
