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
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_ONLINE_OFFLINE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_TREE_VIEW;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_UPDATE;
import static org.apache.dolphinscheduler.common.Constants.DEFAULT;
import static org.apache.dolphinscheduler.common.Constants.EMPTY_STRING;
import static org.mockito.ArgumentMatchers.isA;

import org.apache.dolphinscheduler.api.dto.workflow.WorkflowCreateRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowFilterRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowUpdateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.ProcessDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.utils.DateUtils;
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
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.model.PageListingResult;
import org.apache.dolphinscheduler.dao.repository.ProcessDefinitionDao;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
public class ProcessDefinitionServiceTest extends BaseServiceTestTool {

    private static final String taskRelationJson =
            "[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":123456789,"
                    + "\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":\"{}\"},{\"name\":\"\",\"preTaskCode\":123456789,"
                    + "\"preTaskVersion\":1,\"postTaskCode\":123451234,\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":\"{}\"}]";

    private static final String taskDefinitionJson =
            "[{\"code\":123456789,\"name\":\"test1\",\"version\":1,\"description\":\"\",\"delayTime\":0,\"taskType\":\"SHELL\","
                    + "\"taskParams\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 1\",\"dependence\":{},\"conditionResult\":{\"successNode\":[],\"failedNode\":[]},\"waitStartTimeout\":{},"
                    + "\"switchResult\":{}},\"flag\":\"YES\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":1,\"timeoutFlag\":\"CLOSE\","
                    + "\"timeoutNotifyStrategy\":null,\"timeout\":0,\"environmentCode\":-1},{\"code\":123451234,\"name\":\"test2\",\"version\":1,\"description\":\"\",\"delayTime\":0,\"taskType\":\"SHELL\","
                    + "\"taskParams\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 2\",\"dependence\":{},\"conditionResult\":{\"successNode\":[],\"failedNode\":[]},\"waitStartTimeout\":{},"
                    + "\"switchResult\":{}},\"flag\":\"YES\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":1,\"timeoutFlag\":\"CLOSE\","
                    + "\"timeoutNotifyStrategy\":\"WARN\",\"timeout\":0,\"environmentCode\":-1}]";

    @InjectMocks
    private ProcessDefinitionServiceImpl processDefinitionService;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private ProcessDefinitionLogMapper processDefinitionLogMapper;

    @Mock
    private ProcessDefinitionDao processDefinitionDao;

    @Mock
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private SchedulerService schedulerService;

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

    protected User user;
    protected Exception exception;
    protected final static long projectCode = 1L;
    protected final static long projectCodeOther = 2L;
    protected final static long processDefinitionCode = 11L;
    protected final static String name = "testProcessDefinitionName";
    protected final static String description = "this is a description";
    protected final static String releaseState = "ONLINE";
    protected final static int warningGroupId = 1;
    protected final static int timeout = 60;
    protected final static String executionType = "PARALLEL";
    protected final static String tenantCode = "tenant";

    @BeforeEach
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setTenantId(2);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");
        user = loginUser;
    }

    @Test
    public void testQueryProcessDefinitionList() {
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project not found
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionList(user, projectCode);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, map.get(Constants.STATUS));

        // project check auth success
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        List<ProcessDefinition> resourceList = new ArrayList<>();
        resourceList.add(getProcessDefinition());
        Mockito.when(processDefinitionMapper.queryAllDefinitionList(project.getCode())).thenReturn(resourceList);
        Map<String, Object> checkSuccessRes =
                processDefinitionService.queryProcessDefinitionList(user, projectCode);
        Assertions.assertEquals(Status.SUCCESS, checkSuccessRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryProcessDefinitionListPaging() {
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        // project not found
        try {
            Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(null);
            Mockito.doThrow(new ServiceException(Status.PROJECT_NOT_EXIST)).when(projectService)
                    .checkProjectAndAuthThrowException(user, null, WORKFLOW_DEFINITION);
            processDefinitionService.queryProcessDefinitionListPaging(user, projectCode, "", "", 1, 5, 0);
        } catch (ServiceException serviceException) {
            Assertions.assertEquals(Status.PROJECT_NOT_EXIST.getCode(), serviceException.getCode());
        }

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        user.setId(1);
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, project,
                WORKFLOW_DEFINITION);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        PageListingResult<ProcessDefinition> pageListingResult = PageListingResult.<ProcessDefinition>builder()
                .records(Collections.emptyList())
                .currentPage(1)
                .pageSize(10)
                .totalCount(30)
                .build();
        Mockito.when(processDefinitionDao.listingProcessDefinition(
                Mockito.eq(0),
                Mockito.eq(10),
                Mockito.eq(""),
                Mockito.eq(1),
                Mockito.eq(project.getCode()))).thenReturn(pageListingResult);

        PageInfo<ProcessDefinition> pageInfo = processDefinitionService.queryProcessDefinitionListPaging(
                user, project.getCode(), "", "", 1, 0, 10);

        Assertions.assertNotNull(pageInfo);
    }

    @Test
    public void testQueryProcessDefinitionByCode() {
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        Tenant tenant = new Tenant();
        tenant.setId(1);
        tenant.setTenantCode("root");
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project check auth fail
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionByCode(user, 1L, 1L);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, map.get(Constants.STATUS));

        // project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        DagData dagData = new DagData(getProcessDefinition(), null, null);
        Mockito.when(processService.genDagData(Mockito.any())).thenReturn(dagData);

        Map<String, Object> instanceNotexitRes =
                processDefinitionService.queryProcessDefinitionByCode(user, projectCode, 1L);
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, instanceNotexitRes.get(Constants.STATUS));

        // instance exit
        Mockito.when(processDefinitionMapper.queryByCode(46L)).thenReturn(getProcessDefinition());
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        Mockito.when(tenantMapper.queryById(1)).thenReturn(tenant);
        Map<String, Object> successRes =
                processDefinitionService.queryProcessDefinitionByCode(user, projectCode, 46L);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryProcessDefinitionByName() {
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project check auth fail
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        Map<String, Object> map =
                processDefinitionService.queryProcessDefinitionByName(user, projectCode, "test_def");
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, map.get(Constants.STATUS));

        // project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        Mockito.when(processDefinitionMapper.queryByDefineName(project.getCode(), "test_def")).thenReturn(null);

        Map<String, Object> instanceNotExitRes =
                processDefinitionService.queryProcessDefinitionByName(user, projectCode, "test_def");
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, instanceNotExitRes.get(Constants.STATUS));

        // instance exit
        Mockito.when(processDefinitionMapper.queryByDefineName(project.getCode(), "test"))
                .thenReturn(getProcessDefinition());
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        Map<String, Object> successRes =
                processDefinitionService.queryProcessDefinitionByName(user, projectCode, "test");
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testBatchCopyProcessDefinition() {
        Project project = getProject(projectCode);

        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_BATCH_COPY))
                .thenReturn(result);

        // copy project definition ids empty test
        Map<String, Object> map =
                processDefinitionService.batchCopyProcessDefinition(user, projectCode, StringUtils.EMPTY, 2L);
        Assertions.assertEquals(Status.PROCESS_DEFINITION_CODES_IS_EMPTY, map.get(Constants.STATUS));

        // project check auth fail
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_BATCH_COPY))
                .thenReturn(result);
        Map<String, Object> map1 = processDefinitionService.batchCopyProcessDefinition(
                user, projectCode, String.valueOf(project.getId()), 2L);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, map1.get(Constants.STATUS));

        // project check auth success, target project name not equal project name, check auth target project fail
        Project project1 = getProject(projectCodeOther);
        Mockito.when(projectMapper.queryByCode(projectCodeOther)).thenReturn(project1);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCodeOther, WORKFLOW_BATCH_COPY))
                .thenReturn(result);

        putMsg(result, Status.SUCCESS, projectCodeOther);
        ProcessDefinition definition = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(definition);
        Set<Long> definitionCodes = new HashSet<>();
        // Change this catch NumberFormatException
        for (String code : String.valueOf(processDefinitionCode).split(Constants.COMMA)) {
            try {
                long parse = Long.parseLong(code);
                definitionCodes.add(parse);
            } catch (NumberFormatException e) {
                Assertions.fail();
            }
        }
        Mockito.when(processDefinitionMapper.queryByCodes(definitionCodes)).thenReturn(processDefinitionList);
        Mockito.when(processService.saveProcessDefine(user, definition, Boolean.TRUE, Boolean.TRUE)).thenReturn(2);
        Map<String, Object> map3 = processDefinitionService.batchCopyProcessDefinition(
                user, projectCodeOther, String.valueOf(processDefinitionCode), projectCode);
        Assertions.assertEquals(Status.SUCCESS, map3.get(Constants.STATUS));
    }

    @Test
    public void testBatchMoveProcessDefinition() {
        Project project1 = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project1);

        Project project2 = getProject(projectCodeOther);
        Mockito.when(projectMapper.queryByCode(projectCodeOther)).thenReturn(project2);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);

        Mockito.when(projectService.checkProjectAndAuth(user, project1, projectCode, TASK_DEFINITION_MOVE))
                .thenReturn(result);
        Mockito.when(projectService.checkProjectAndAuth(user, project2, projectCodeOther, TASK_DEFINITION_MOVE))
                .thenReturn(result);

        ProcessDefinition definition = getProcessDefinition();
        definition.setVersion(1);
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(definition);
        Set<Long> definitionCodes = new HashSet<>();
        // Change this catch NumberFormatException
        for (String code : String.valueOf(processDefinitionCode).split(Constants.COMMA)) {
            try {
                long parse = Long.parseLong(code);
                definitionCodes.add(parse);
            } catch (NumberFormatException e) {
                Assertions.fail();
            }
        }
        Mockito.when(processDefinitionMapper.queryByCodes(definitionCodes)).thenReturn(processDefinitionList);
        Mockito.when(processService.saveProcessDefine(user, definition, Boolean.TRUE, Boolean.TRUE)).thenReturn(2);
        Mockito.when(processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode))
                .thenReturn(getProcessTaskRelation());
        putMsg(result, Status.SUCCESS);

        Map<String, Object> successRes = processDefinitionService.batchMoveProcessDefinition(
                user, projectCode, String.valueOf(processDefinitionCode), projectCodeOther);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void deleteProcessDefinitionByCodeTest() {
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        // process definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteProcessDefinitionByCode(user, 2L));
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // project check auth fail
        Mockito.when(processDefinitionMapper.queryByCode(6L)).thenReturn(this.getProcessDefinition());
        Mockito.doThrow(new ServiceException(Status.PROJECT_NOT_FOUND)).when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_DEFINITION_DELETE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteProcessDefinitionByCode(user, 6L));
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), ((ServiceException) exception).getCode());

        // project check auth success, instance not exist
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, project,
                WORKFLOW_DEFINITION_DELETE);
        Mockito.when(processDefinitionMapper.queryByCode(1L)).thenReturn(null);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteProcessDefinitionByCode(user, 1L));
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        ProcessDefinition processDefinition = getProcessDefinition();
        // user no auth
        Mockito.when(processDefinitionMapper.queryByCode(46L)).thenReturn(processDefinition);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteProcessDefinitionByCode(user, 46L));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), ((ServiceException) exception).getCode());

        // process definition online
        user.setUserType(UserType.ADMIN_USER);
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        Mockito.when(processDefinitionMapper.queryByCode(46L)).thenReturn(processDefinition);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteProcessDefinitionByCode(user, 46L));
        Assertions.assertEquals(Status.PROCESS_DEFINE_STATE_ONLINE.getCode(), ((ServiceException) exception).getCode());

        // scheduler list elements > 1
        processDefinition.setReleaseState(ReleaseState.OFFLINE);
        Mockito.when(processDefinitionMapper.queryByCode(46L)).thenReturn(processDefinition);
        Mockito.when(scheduleMapper.queryByProcessDefinitionCode(46L)).thenReturn(getSchedule());
        Mockito.when(scheduleMapper.deleteById(46)).thenReturn(1);
        Mockito.when(processDefinitionMapper.deleteById(processDefinition.getId())).thenReturn(1);
        Mockito.when(processTaskRelationMapper.deleteByCode(project.getCode(), processDefinition.getCode()))
                .thenReturn(1);
        Mockito.when(workFlowLineageService.queryTaskDepOnProcess(project.getCode(), processDefinition.getCode()))
                .thenReturn(Collections.emptySet());
        processDefinitionService.deleteProcessDefinitionByCode(user, 46L);

        // scheduler online
        Schedule schedule = getSchedule();
        schedule.setReleaseState(ReleaseState.ONLINE);
        Mockito.when(scheduleMapper.queryByProcessDefinitionCode(46L)).thenReturn(schedule);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteProcessDefinitionByCode(user, 46L));
        Assertions.assertEquals(Status.SCHEDULE_STATE_ONLINE.getCode(), ((ServiceException) exception).getCode());

        // process used by other task, sub process
        user.setUserType(UserType.ADMIN_USER);
        TaskMainInfo taskMainInfo = getTaskMainInfo().get(0);
        Mockito.when(workFlowLineageService.queryTaskDepOnProcess(project.getCode(), processDefinition.getCode()))
                .thenReturn(ImmutableSet.copyOf(getTaskMainInfo()));
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteProcessDefinitionByCode(user, 46L));
        Assertions.assertEquals(Status.DELETE_PROCESS_DEFINITION_USE_BY_OTHER_FAIL.getCode(),
                ((ServiceException) exception).getCode());

        // delete success
        schedule.setReleaseState(ReleaseState.OFFLINE);
        Mockito.when(processDefinitionMapper.deleteById(46)).thenReturn(1);
        Mockito.when(scheduleMapper.deleteById(schedule.getId())).thenReturn(1);
        Mockito.when(processTaskRelationMapper.deleteByCode(project.getCode(), processDefinition.getCode()))
                .thenReturn(1);
        Mockito.when(scheduleMapper.queryByProcessDefinitionCode(46L)).thenReturn(getSchedule());
        Mockito.when(workFlowLineageService.queryTaskDepOnProcess(project.getCode(), processDefinition.getCode()))
                .thenReturn(Collections.emptySet());
        Assertions.assertDoesNotThrow(() -> processDefinitionService.deleteProcessDefinitionByCode(user, 46L));
    }

    @Test
    public void batchDeleteProcessDefinitionByCodeTest() {
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        // process check exists
        final String twoCodes = "11,12";
        Set<Long> definitionCodes = Lists.newArrayList(twoCodes.split(Constants.COMMA)).stream()
                .map(Long::parseLong).collect(Collectors.toSet());
        ProcessDefinition process = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(process);
        Mockito.when(processDefinitionMapper.queryByCodes(definitionCodes)).thenReturn(processDefinitionList);
        Throwable exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.batchDeleteProcessDefinitionByCodes(user, projectCode, twoCodes));
        String formatter = MessageFormat.format(Status.BATCH_DELETE_PROCESS_DEFINE_BY_CODES_ERROR.getMsg(),
                "12[process definition not exist]");
        Assertions.assertEquals(formatter, exception.getMessage());

        // return the right data
        Map<String, Object> result = new HashMap<>();
        final String singleCodes = "11";
        definitionCodes = Lists.newArrayList(singleCodes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        Mockito.when(processDefinitionMapper.queryByCodes(definitionCodes)).thenReturn(processDefinitionList);
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(process);

        // process definition online
        user.setUserType(UserType.ADMIN_USER);
        putMsg(result, Status.SUCCESS, projectCode);
        process.setReleaseState(ReleaseState.ONLINE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.batchDeleteProcessDefinitionByCodes(user, projectCode, singleCodes));
        String subFormatter =
                MessageFormat.format(Status.PROCESS_DEFINE_STATE_ONLINE.getMsg(), process.getName());
        formatter =
                MessageFormat.format(Status.DELETE_PROCESS_DEFINE_ERROR.getMsg(), process.getName(), subFormatter);
        Assertions.assertEquals(formatter, exception.getMessage());

        // delete success
        process.setReleaseState(ReleaseState.OFFLINE);
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(process);
        Mockito.when(processDefinitionMapper.deleteById(process.getId())).thenReturn(1);
        Mockito.when(processTaskRelationMapper.deleteByCode(project.getCode(), process.getCode()))
                .thenReturn(1);
        Mockito.when(workFlowLineageService.queryTaskDepOnProcess(project.getCode(), process.getCode()))
                .thenReturn(Collections.emptySet());
        putMsg(result, Status.SUCCESS, projectCode);
        Map<String, Object> deleteSuccess =
                processDefinitionService.batchDeleteProcessDefinitionByCodes(user, projectCode, singleCodes);
        Assertions.assertEquals(Status.SUCCESS, deleteSuccess.get(Constants.STATUS));
    }

    @Test
    public void testReleaseProcessDefinition() {
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        // project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_ONLINE_OFFLINE))
                .thenReturn(result);
        Map<String, Object> map = processDefinitionService.releaseProcessDefinition(user, projectCode,
                processDefinitionCode, ReleaseState.OFFLINE);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, map.get(Constants.STATUS));

        // project check auth success, processes definition online
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(processDefinitionMapper.queryByCode(46L)).thenReturn(getProcessDefinition());
        List<ProcessTaskRelation> processTaskRelationList = new ArrayList<>();
        ProcessTaskRelation processTaskRelation = new ProcessTaskRelation();
        processTaskRelation.setProjectCode(projectCode);
        processTaskRelation.setProcessDefinitionCode(46L);
        processTaskRelation.setPostTaskCode(123L);
        processTaskRelationList.add(processTaskRelation);
        Mockito.when(processService.findRelationByCode(46L, 1)).thenReturn(processTaskRelationList);
        Map<String, Object> onlineRes =
                processDefinitionService.releaseProcessDefinition(user, projectCode, 46, ReleaseState.ONLINE);
        Assertions.assertEquals(Status.SUCCESS, onlineRes.get(Constants.STATUS));

        // project check auth success, processes definition online
        Map<String, Object> onlineWithResourceRes =
                processDefinitionService.releaseProcessDefinition(user, projectCode, 46, ReleaseState.ONLINE);
        Assertions.assertEquals(Status.SUCCESS, onlineWithResourceRes.get(Constants.STATUS));

        // release error code
        Map<String, Object> failRes =
                processDefinitionService.releaseProcessDefinition(user, projectCode, 46, ReleaseState.getEnum(2));
        Assertions.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, failRes.get(Constants.STATUS));
    }

    @Test
    public void testVerifyProcessDefinitionName() {
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Project project = getProject(projectCode);

        // project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_CREATE))
                .thenReturn(result);
        Map<String, Object> map = processDefinitionService.verifyProcessDefinitionName(user,
                projectCode, "test_pdf", 0);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, map.get(Constants.STATUS));

        // project check auth success, process not exist
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(processDefinitionMapper.verifyByDefineName(project.getCode(), "test_pdf")).thenReturn(null);
        Map<String, Object> processNotExistRes =
                processDefinitionService.verifyProcessDefinitionName(user, projectCode, "test_pdf", 0);
        Assertions.assertEquals(Status.SUCCESS, processNotExistRes.get(Constants.STATUS));

        // process exist
        Mockito.when(processDefinitionMapper.verifyByDefineName(project.getCode(), "test_pdf"))
                .thenReturn(getProcessDefinition());
        Map<String, Object> processExistRes = processDefinitionService.verifyProcessDefinitionName(user,
                projectCode, "test_pdf", 0);
        Assertions.assertEquals(Status.PROCESS_DEFINITION_NAME_EXIST, processExistRes.get(Constants.STATUS));
    }

    @Test
    public void testCheckProcessNodeList() {
        Map<String, Object> dataNotValidRes = processDefinitionService.checkProcessNodeList(null, null);
        Assertions.assertEquals(Status.DATA_IS_NOT_VALID, dataNotValidRes.get(Constants.STATUS));

        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);

        Map<String, Object> taskEmptyRes =
                processDefinitionService.checkProcessNodeList(taskRelationJson, taskDefinitionLogs);
        Assertions.assertEquals(Status.PROCESS_DAG_IS_EMPTY, taskEmptyRes.get(Constants.STATUS));
    }

    @Test
    public void testGetTaskNodeListByDefinitionCode() {
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Project project = getProject(projectCode);

        // project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, null)).thenReturn(result);
        // process definition not exist
        Mockito.when(processDefinitionMapper.queryByCode(46L)).thenReturn(null);
        Map<String, Object> processDefinitionNullRes =
                processDefinitionService.getTaskNodeListByDefinitionCode(user, projectCode, 46L);
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionNullRes.get(Constants.STATUS));

        // success
        ProcessDefinition processDefinition = getProcessDefinition();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(processService.genDagData(Mockito.any())).thenReturn(new DagData(processDefinition, null, null));
        Mockito.when(processDefinitionMapper.queryByCode(46L)).thenReturn(processDefinition);
        Map<String, Object> dataNotValidRes =
                processDefinitionService.getTaskNodeListByDefinitionCode(user, projectCode, 46L);
        Assertions.assertEquals(Status.SUCCESS, dataNotValidRes.get(Constants.STATUS));
    }

    @Test
    public void testGetTaskNodeListByDefinitionCodes() {
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Project project = getProject(projectCode);

        // project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, null)).thenReturn(result);
        // process definition not exist
        String defineCodes = "46";
        Set<Long> defineCodeSet = Lists.newArrayList(defineCodes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        Mockito.when(processDefinitionMapper.queryByCodes(defineCodeSet)).thenReturn(null);
        Map<String, Object> processNotExistRes =
                processDefinitionService.getNodeListMapByDefinitionCodes(user, projectCode, defineCodes);
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processNotExistRes.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectCode);
        ProcessDefinition processDefinition = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(processDefinition);

        Mockito.when(processDefinitionMapper.queryByCodes(defineCodeSet)).thenReturn(processDefinitionList);
        Mockito.when(processService.genDagData(Mockito.any())).thenReturn(new DagData(processDefinition, null, null));
        Project project1 = getProject(projectCode);
        List<Project> projects = new ArrayList<>();
        projects.add(project1);
        Mockito.when(projectMapper.queryProjectCreatedAndAuthorizedByUserId(user.getId())).thenReturn(projects);

        Map<String, Object> successRes =
                processDefinitionService.getNodeListMapByDefinitionCodes(user, projectCode, defineCodes);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryAllProcessDefinitionByProjectCode() {
        Map<String, Object> result = new HashMap<>();
        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        ProcessDefinition processDefinition = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(processDefinition);
        Mockito.when(processDefinitionMapper.queryAllDefinitionList(projectCode)).thenReturn(processDefinitionList);
        Map<String, Object> successRes =
                processDefinitionService.queryAllProcessDefinitionByProjectCode(user, projectCode);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testViewTree() {
        Project project1 = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectMapper.queryByCode(1)).thenReturn(project1);
        Mockito.when(projectService.checkProjectAndAuth(user, project1, projectCode, WORKFLOW_TREE_VIEW))
                .thenReturn(result);
        // process definition not exist
        ProcessDefinition processDefinition = getProcessDefinition();
        Map<String, Object> processDefinitionNullRes =
                processDefinitionService.viewTree(user, processDefinition.getProjectCode(), 46, 10);
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionNullRes.get(Constants.STATUS));

        // task instance not existproject
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectMapper.queryByCode(1)).thenReturn(project1);
        Mockito.when(projectService.checkProjectAndAuth(user, project1, 1, WORKFLOW_TREE_VIEW)).thenReturn(result);
        Mockito.when(processDefinitionMapper.queryByCode(46L)).thenReturn(processDefinition);
        Mockito.when(processService.genDagGraph(processDefinition)).thenReturn(new DAG<>());
        Map<String, Object> taskNullRes =
                processDefinitionService.viewTree(user, processDefinition.getProjectCode(), 46, 10);
        Assertions.assertEquals(Status.SUCCESS, taskNullRes.get(Constants.STATUS));

        // task instance exist
        Map<String, Object> taskNotNuLLRes =
                processDefinitionService.viewTree(user, processDefinition.getProjectCode(), 46, 10);
        Assertions.assertEquals(Status.SUCCESS, taskNotNuLLRes.get(Constants.STATUS));
    }

    @Test
    public void testSubProcessViewTree() {
        ProcessDefinition processDefinition = getProcessDefinition();
        Mockito.when(processDefinitionMapper.queryByCode(46L)).thenReturn(processDefinition);

        Project project1 = getProject(1);
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(projectMapper.queryByCode(1)).thenReturn(project1);
        Mockito.when(projectService.checkProjectAndAuth(user, project1, 1, WORKFLOW_TREE_VIEW)).thenReturn(result);

        Mockito.when(processService.genDagGraph(processDefinition)).thenReturn(new DAG<>());
        Map<String, Object> taskNotNuLLRes =
                processDefinitionService.viewTree(user, processDefinition.getProjectCode(), 46, 10);
        Assertions.assertEquals(Status.SUCCESS, taskNotNuLLRes.get(Constants.STATUS));
    }

    @Test
    public void testUpdateProcessDefinition() {
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_UPDATE))
                .thenReturn(result);

        try {
            processDefinitionService.updateProcessDefinition(user, projectCode, "test", 1,
                    "", "", "", 0, "root", null, "", null, ProcessExecutionTypeEnum.PARALLEL);
            Assertions.fail();
        } catch (ServiceException ex) {
            Assertions.assertEquals(Status.DATA_IS_NOT_VALID.getCode(), ex.getCode());
        }
    }

    @Test
    public void testBatchExportProcessDefinitionByCodes() {
        processDefinitionService.batchExportProcessDefinitionByCodes(null, 1L, null, null);
        Project project = getProject(projectCode);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        processDefinitionService.batchExportProcessDefinitionByCodes(user, projectCode, "1", null);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(1);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        DagData dagData = new DagData(getProcessDefinition(), null, null);
        Mockito.when(processService.genDagData(Mockito.any())).thenReturn(dagData);
        processDefinitionService.batchExportProcessDefinitionByCodes(user, projectCode, "1", response);
        Assertions.assertNotNull(processDefinitionService.exportProcessDagData(processDefinition));
    }

    @Test
    public void testImportSqlProcessDefinition() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream outputStream = new ZipOutputStream(byteArrayOutputStream);
        outputStream.putNextEntry(new ZipEntry("import_sql/"));

        outputStream.putNextEntry(new ZipEntry("import_sql/a.sql"));
        outputStream.write(
                "-- upstream: start_auto_dag\n-- datasource: mysql_1\nselect 1;".getBytes(StandardCharsets.UTF_8));

        outputStream.putNextEntry(new ZipEntry("import_sql/b.sql"));
        outputStream
                .write("-- name: start_auto_dag\n-- datasource: mysql_1\nselect 1;".getBytes(StandardCharsets.UTF_8));

        outputStream.close();

        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("import_sql.zip", byteArrayOutputStream.toByteArray());

        DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(dataSource.getId()).thenReturn(1);
        Mockito.when(dataSource.getType()).thenReturn(DbType.MYSQL);

        Mockito.when(dataSourceMapper.queryDataSourceByNameAndUserId(user.getId(), "mysql_1")).thenReturn(dataSource);

        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Mockito.when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_IMPORT))
                .thenReturn(result);
        Mockito.when(processService.saveTaskDefine(Mockito.same(user), Mockito.eq(projectCode), Mockito.notNull(),
                Mockito.anyBoolean())).thenReturn(2);
        Mockito.when(processService.saveProcessDefine(Mockito.same(user), Mockito.notNull(), Mockito.notNull(),
                Mockito.anyBoolean())).thenReturn(1);
        Mockito.when(
                processService.saveTaskRelation(Mockito.same(user), Mockito.eq(projectCode), Mockito.anyLong(),
                        Mockito.eq(1), Mockito.notNull(), Mockito.notNull(), Mockito.anyBoolean()))
                .thenReturn(0);
        result = processDefinitionService.importSqlProcessDefinition(user, projectCode, mockMultipartFile);

        Assertions.assertEquals(result.get(Constants.STATUS), Status.SUCCESS);
    }

    @Test
    public void testGetNewProcessName() {
        String processName1 = "test_copy_" + DateUtils.getCurrentTimeStamp();
        final String newName1 = processDefinitionService.getNewName(processName1, Constants.COPY_SUFFIX);
        Assertions.assertEquals(2, newName1.split(Constants.COPY_SUFFIX).length);
        String processName2 = "wf_copy_all_ods_data_to_d";
        final String newName2 = processDefinitionService.getNewName(processName2, Constants.COPY_SUFFIX);
        Assertions.assertEquals(3, newName2.split(Constants.COPY_SUFFIX).length);
        String processName3 = "test_import_" + DateUtils.getCurrentTimeStamp();
        final String newName3 = processDefinitionService.getNewName(processName3, Constants.IMPORT_SUFFIX);
        Assertions.assertEquals(2, newName3.split(Constants.IMPORT_SUFFIX).length);
    }

    @Test
    public void testCreateProcessDefinitionV2() {
        Project project = this.getProject(projectCode);

        WorkflowCreateRequest workflowCreateRequest = new WorkflowCreateRequest();
        workflowCreateRequest.setName(name);
        workflowCreateRequest.setProjectCode(projectCode);

        // project not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.createSingleProcessDefinition(user, workflowCreateRequest));
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), ((ServiceException) exception).getCode());

        // project permission error
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_CREATE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.createSingleProcessDefinition(user, workflowCreateRequest));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // description too long
        workflowCreateRequest.setDescription(taskDefinitionJson);
        Mockito.doThrow(new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR)).when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_CREATE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.createSingleProcessDefinition(user, workflowCreateRequest));
        Assertions.assertEquals(Status.DESCRIPTION_TOO_LONG_ERROR.getCode(), ((ServiceException) exception).getCode());
        workflowCreateRequest.setDescription(EMPTY_STRING);

        // duplicate process definition name
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, project, WORKFLOW_CREATE);
        Mockito.when(processDefinitionMapper.verifyByDefineName(project.getCode(), name))
                .thenReturn(this.getProcessDefinition());
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.createSingleProcessDefinition(user, workflowCreateRequest));
        Assertions.assertEquals(Status.PROCESS_DEFINITION_NAME_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        // tenant not exists
        Mockito.when(processDefinitionMapper.verifyByDefineName(project.getCode(), name)).thenReturn(null);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.createSingleProcessDefinition(user, workflowCreateRequest));
        Assertions.assertEquals(Status.TENANT_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // test success
        workflowCreateRequest.setTenantCode(DEFAULT);
        workflowCreateRequest.setDescription(description);
        workflowCreateRequest.setTimeout(timeout);
        workflowCreateRequest.setReleaseState(releaseState);
        workflowCreateRequest.setWarningGroupId(warningGroupId);
        workflowCreateRequest.setExecutionType(executionType);
        Mockito.when(processDefinitionLogMapper.insert(Mockito.any())).thenReturn(1);
        Mockito.when(processDefinitionMapper.insert(Mockito.any())).thenReturn(1);
        ProcessDefinition processDefinition =
                processDefinitionService.createSingleProcessDefinition(user, workflowCreateRequest);

        Assertions.assertTrue(processDefinition.getCode() > 0L);
        Assertions.assertEquals(workflowCreateRequest.getName(), processDefinition.getName());
        Assertions.assertEquals(workflowCreateRequest.getDescription(), processDefinition.getDescription());
        Assertions.assertTrue(StringUtils.endsWithIgnoreCase(workflowCreateRequest.getReleaseState(),
                processDefinition.getReleaseState().getDescp()));
        Assertions.assertEquals(workflowCreateRequest.getTimeout(), processDefinition.getTimeout());
        Assertions.assertTrue(StringUtils.endsWithIgnoreCase(workflowCreateRequest.getExecutionType(),
                processDefinition.getExecutionType().getDescp()));
    }

    @Test
    public void testFilterProcessDefinition() {
        Project project = this.getProject(projectCode);
        WorkflowFilterRequest workflowFilterRequest = new WorkflowFilterRequest();
        workflowFilterRequest.setProjectName(project.getName());

        // project permission error
        Mockito.when(projectMapper.queryByName(project.getName())).thenReturn(project);
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM, user.getUserName(), projectCode))
                .when(projectService).checkProjectAndAuthThrowException(user, project, WORKFLOW_DEFINITION);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.filterProcessDefinition(user, workflowFilterRequest));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());
    }

    @Test
    public void testGetProcessDefinition() {
        // process definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.getProcessDefinition(user, processDefinitionCode));
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // project permission error
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode))
                .thenReturn(this.getProcessDefinition());
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(this.getProject(projectCode));
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM, user.getUserName(), projectCode))
                .when(projectService)
                .checkProjectAndAuthThrowException(user, this.getProject(projectCode), WORKFLOW_DEFINITION);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.getProcessDefinition(user, processDefinitionCode));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // success
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, this.getProject(projectCode),
                WORKFLOW_DEFINITION);
        ProcessDefinition processDefinition =
                processDefinitionService.getProcessDefinition(user, processDefinitionCode);
        Assertions.assertEquals(this.getProcessDefinition(), processDefinition);
    }

    @Test
    public void testUpdateProcessDefinitionV2() {
        ProcessDefinition processDefinition;

        WorkflowUpdateRequest workflowUpdateRequest = new WorkflowUpdateRequest();
        workflowUpdateRequest.setName(name);

        // error process definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.getProcessDefinition(user, processDefinitionCode));
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());

        // error old process definition in release state
        processDefinition = this.getProcessDefinition();
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(processDefinition);
        exception = Assertions.assertThrows(ServiceException.class, () -> processDefinitionService
                .updateSingleProcessDefinition(user, processDefinitionCode, workflowUpdateRequest));
        Assertions.assertEquals(Status.PROCESS_DEFINE_NOT_ALLOWED_EDIT.getCode(),
                ((ServiceException) exception).getCode());

        // error project permission
        processDefinition = this.getProcessDefinition();
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(processDefinition);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(this.getProject(projectCode));
        Mockito.doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM, user.getUserName(), projectCode))
                .when(projectService)
                .checkProjectAndAuthThrowException(user, this.getProject(projectCode), WORKFLOW_DEFINITION);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.getProcessDefinition(user, processDefinitionCode));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // error description too long
        workflowUpdateRequest.setDescription(taskDefinitionJson);
        Mockito.doThrow(new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR)).when(projectService)
                .checkProjectAndAuthThrowException(user, this.getProject(projectCode), WORKFLOW_UPDATE);
        exception = Assertions.assertThrows(ServiceException.class, () -> processDefinitionService
                .updateSingleProcessDefinition(user, processDefinitionCode, workflowUpdateRequest));
        Assertions.assertEquals(Status.DESCRIPTION_TOO_LONG_ERROR.getCode(), ((ServiceException) exception).getCode());
        workflowUpdateRequest.setDescription(EMPTY_STRING);

        // error new definition name already exists
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, this.getProject(projectCode),
                WORKFLOW_UPDATE);
        Mockito.when(processDefinitionMapper.verifyByDefineName(projectCode, workflowUpdateRequest.getName()))
                .thenReturn(this.getProcessDefinition());
        exception = Assertions.assertThrows(ServiceException.class, () -> processDefinitionService
                .updateSingleProcessDefinition(user, processDefinitionCode, workflowUpdateRequest));
        Assertions.assertEquals(Status.PROCESS_DEFINITION_NAME_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        // error tenant code not exists
        processDefinition = this.getProcessDefinition();
        workflowUpdateRequest.setTenantCode(tenantCode);
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(processDefinition);
        Mockito.when(processDefinitionMapper.verifyByDefineName(projectCode, workflowUpdateRequest.getName()))
                .thenReturn(null);
        Mockito.when(tenantMapper.queryByTenantCode(workflowUpdateRequest.getTenantCode())).thenReturn(null);
        exception = Assertions.assertThrows(ServiceException.class, () -> processDefinitionService
                .updateSingleProcessDefinition(user, processDefinitionCode, workflowUpdateRequest));
        Assertions.assertEquals(Status.TENANT_NOT_EXIST.getCode(), ((ServiceException) exception).getCode());
        workflowUpdateRequest.setTenantCode(null);

        // error update process definition mapper
        workflowUpdateRequest.setName(name);
        Mockito.when(processDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(processDefinition);
        Mockito.when(processDefinitionLogMapper.insert(Mockito.any())).thenReturn(1);
        exception = Assertions.assertThrows(ServiceException.class, () -> processDefinitionService
                .updateSingleProcessDefinition(user, processDefinitionCode, workflowUpdateRequest));
        Assertions.assertEquals(Status.UPDATE_PROCESS_DEFINITION_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // success
        Mockito.when(processDefinitionMapper.updateById(isA(ProcessDefinition.class))).thenReturn(1);
        ProcessDefinition processDefinitionUpdate =
                processDefinitionService.updateSingleProcessDefinition(user, processDefinitionCode,
                        workflowUpdateRequest);
        Assertions.assertEquals(processDefinition, processDefinitionUpdate);
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
        processDefinition.setCode(processDefinitionCode);
        processDefinition.setProjectCode(projectCode);
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

    private List<ProcessTaskRelation> getProcessTaskRelation() {
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
