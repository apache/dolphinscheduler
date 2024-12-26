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
import static org.apache.dolphinscheduler.common.constants.Constants.EMPTY_STRING;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.dto.workflow.WorkflowCreateRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowFilterRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowUpdateRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.WorkflowDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskMainInfo;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.UserWithWorkflowDefinitionCode;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;
import org.apache.dolphinscheduler.dao.model.PageListingResult;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
public class WorkflowDefinitionServiceTest extends BaseServiceTestTool {

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
    private WorkflowDefinitionServiceImpl processDefinitionService;

    @Mock
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

    @Mock
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Mock
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;

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
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Mock
    private WorkflowInstanceService workflowInstanceService;

    @Mock
    private DataSourceMapper dataSourceMapper;

    @Mock
    private WorkflowLineageService workflowLineageService;

    @Mock
    private MetricsCleanUpService metricsCleanUpService;

    @Mock
    private TaskDefinitionService taskDefinitionService;

    @Mock
    private TaskDefinitionLogService taskDefinitionLogService;

    @Mock
    private WorkflowDefinitionLogDao workflowDefinitionLogDao;

    @Mock
    private UserMapper userMapper;

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

    @BeforeEach
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setTenantId(2);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");
        user = loginUser;
    }

    @Test()
    public void testCopyworkflowLogicalNodeSwitch() {
        long projectCode = 128645169571296L;
        String codes = "128645230604768";
        long targetProjectCode = 128645169571296L;
        long shellTaskDefinitionCode = 128645175846368L;
        long switchTaskDefinitionCode = 128645191546336L;

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Set<Long> definitionCodes = new HashSet<>();
        for (String code : String.valueOf(codes).split(Constants.COMMA)) {
            try {
                long parse = Long.parseLong(code);
                definitionCodes.add(parse);
            } catch (NumberFormatException e) {
                Assertions.fail();
            }
        }

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setId(1);
        workflowDefinition.setCode(Long.parseLong(codes));
        workflowDefinition.setName("workflow_switch");
        workflowDefinition.setDescription("");
        workflowDefinition.setVersion(1);
        workflowDefinition.setReleaseState(ReleaseState.OFFLINE);
        workflowDefinition.setProjectCode(projectCode);
        workflowDefinition.setUserId(user.getId());
        List<WorkflowDefinition> workflowDefinitionList = new ArrayList<>();
        workflowDefinitionList.add(workflowDefinition);

        Project project = new Project();
        project.setCode(projectCode);
        project.setId(1);
        project.setName("project_switch");
        project.setUserId(user.getId());

        List<WorkflowTaskRelation> workflowTaskRelations = new ArrayList<>();
        WorkflowTaskRelation workflowTaskRelationShell = getWorkflowTaskRelation(1, 1, projectCode,
                workflowDefinition.getCode(), 0L, 1, switchTaskDefinitionCode, 1);
        WorkflowTaskRelation workflowTaskRelationSwitch = getWorkflowTaskRelation(2, 1, projectCode,
                workflowDefinition.getCode(), switchTaskDefinitionCode, 1, shellTaskDefinitionCode, 1);

        workflowTaskRelations.add(workflowTaskRelationShell);
        workflowTaskRelations.add(workflowTaskRelationSwitch);

        String taskDefinitionLogJson =
                "[{\"id\":1,\"code\":128645175846368,\"name\":\"shellA\",\"version\":1,\"description\":\"\",\"projectCode\":128645169571296,\"userId\":1,\"taskType\":\"SHELL\","
                        +
                        "\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo 'A'\",\"resourceList\":[]},\"flag\":\"YES\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"environmentCode\":-1,"
                        +
                        "\"failRetryTimes\":0,\"failRetryInterval\":1,\"timeoutFlag\":\"CLOSE\",\"timeout\":0,\"delayTime\":0,\"createTime\":\"2024-12-25 01:15:08\",\"updateTime\":\"2024-12-25 01:15:08\","
                        +
                        "\"taskGroupId\":0,\"taskGroupPriority\":0,\"cpuQuota\":-1,\"memoryMax\":-1,\"taskExecuteType\":\"BATCH\"},{\"id\":2,\"code\":128645191546336,\"name\":\"switchA\",\"version\":1,"
                        +
                        "\"description\":\"\",\"projectCode\":128645169571296,\"userId\":1,\"taskType\":\"SWITCH\"," +
                        "\"taskParams\":{\"localParams\":[],\"rawScript\":\"\",\"resourceList\":[],\"switchResult\":{\"dependTaskList\":[{\"condition\":\"${value} == 'A'\",\"nextNode\":128645175846368}],"
                        +
                        "\"nextNode\":128645175846368}},\"flag\":\"YES\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"environmentCode\":-1,\"failRetryTimes\":0,\"failRetryInterval\":1,\"timeoutFlag\":\"CLOSE\","
                        +
                        "\"timeout\":0,\"delayTime\":0,\"createTime\":\"2024-12-25 01:15:08\",\"updateTime\":\"2024-12-25 01:15:08\",\"taskGroupId\":0,\"taskGroupPriority\":0,\"cpuQuota\":-1,\"memoryMax\":-1,\"taskExecuteType\":\"BATCH\"}]";

        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionLogJson, TaskDefinitionLog.class);

        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_BATCH_COPY)).thenReturn(result);
        when(workflowDefinitionMapper.queryByCodes(definitionCodes)).thenReturn(workflowDefinitionList);
        when(workflowTaskRelationMapper.queryByWorkflowDefinitionCode(Long.parseLong(codes)))
                .thenReturn(workflowTaskRelations);
        when(taskDefinitionLogDao.queryTaskDefineLogList(workflowTaskRelations)).thenReturn(taskDefinitionLogs);
        when(processService.saveTaskDefine(user, projectCode, taskDefinitionLogs, true)).thenReturn(1);
        when(processService.saveWorkflowDefine(user, workflowDefinition, true, true)).thenReturn(1);
        Map<String, Object> successRes =
                processDefinitionService.batchCopyWorkflowDefinition(user, projectCode, codes, targetProjectCode);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));

    }
    @Test
    public void testQueryWorkflowDefinitionList() {
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project not found
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryWorkflowDefinitionList(user, projectCode);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, map.get(Constants.STATUS));

        // project check auth success
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        List<WorkflowDefinition> resourceList = new ArrayList<>();
        resourceList.add(getWorkflowDefinition());
        when(workflowDefinitionMapper.queryAllDefinitionList(project.getCode())).thenReturn(resourceList);
        Map<String, Object> checkSuccessRes =
                processDefinitionService.queryWorkflowDefinitionList(user, projectCode);
        Assertions.assertEquals(Status.SUCCESS, checkSuccessRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryWorkflowDefinitionListPaging() {

        // project not found
        try {
            doThrow(new ServiceException(Status.PROJECT_NOT_EXIST)).when(projectService)
                    .checkProjectAndAuthThrowException(user, projectCode, WORKFLOW_DEFINITION);
            processDefinitionService.queryWorkflowDefinitionListPaging(user, projectCode, "", "", 1, 5, 0);
        } catch (ServiceException serviceException) {
            Assertions.assertEquals(Status.PROJECT_NOT_EXIST.getCode(), serviceException.getCode());
        }

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        user.setId(1);
        doNothing().when(projectService).checkProjectAndAuthThrowException(user, projectCode, WORKFLOW_DEFINITION);
        long processDefinitionCode1 = 1L;
        long processDefinitionCode2 = 2L;
        List<WorkflowDefinition> workflowDefinitions = Arrays.asList(
                WorkflowDefinition.builder().version(1).code(processDefinitionCode1).build(),
                WorkflowDefinition.builder().version(1).code(processDefinitionCode2).build());
        List<Long> processDefinitionCodes = workflowDefinitions.stream()
                .map(WorkflowDefinition::getCode).collect(Collectors.toList());
        PageListingResult<WorkflowDefinition> pageListingResult = PageListingResult.<WorkflowDefinition>builder()
                .records(workflowDefinitions)
                .currentPage(1)
                .pageSize(10)
                .totalCount(30)
                .build();
        when(workflowDefinitionDao.listingWorkflowDefinition(
                Mockito.eq(0),
                Mockito.eq(10),
                Mockito.eq(""),
                Mockito.eq(1),
                Mockito.eq(projectCode))).thenReturn(pageListingResult);
        String user1 = "user1";
        String user2 = "user2";
        when(userMapper.queryUserWithWorkflowDefinitionCode(processDefinitionCodes))
                .thenReturn(Arrays.asList(
                        UserWithWorkflowDefinitionCode.builder()
                                .workflowDefinitionCode(processDefinitionCode1)
                                .workflowDefinitionVersion(1)
                                .modifierName(user1).build(),
                        UserWithWorkflowDefinitionCode.builder()
                                .workflowDefinitionCode(processDefinitionCode2)
                                .workflowDefinitionVersion(1)
                                .modifierName(user2).build()));
        Schedule schedule1 = new Schedule();
        schedule1.setWorkflowDefinitionCode(processDefinitionCode1);
        schedule1.setReleaseState(ReleaseState.ONLINE);
        Schedule schedule2 = new Schedule();
        schedule2.setWorkflowDefinitionCode(processDefinitionCode2);
        schedule2.setReleaseState(ReleaseState.ONLINE);
        when(schedulerService.queryScheduleByWorkflowDefinitionCodes(processDefinitionCodes))
                .thenReturn(Arrays.asList(schedule1, schedule2));
        PageInfo<WorkflowDefinition> pageInfo = processDefinitionService.queryWorkflowDefinitionListPaging(
                user,
                projectCode,
                "",
                "",
                1,
                0,
                10);
        Assertions.assertNotNull(pageInfo);
        WorkflowDefinition pd1 = pageInfo.getTotalList().stream()
                .filter(pd -> pd.getCode() == processDefinitionCode1).findFirst().orElse(null);
        assert pd1 != null;
        Assertions.assertEquals(pd1.getModifyBy(), user1);
    }

    @Test
    public void testQueryWorkflowDefinitionByCode() {
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project check auth fail
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryWorkflowDefinitionByCode(user, 1L, 1L);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, map.get(Constants.STATUS));

        // project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        DagData dagData = new DagData(getWorkflowDefinition(), null, null);
        when(processService.genDagData(Mockito.any())).thenReturn(dagData);

        Map<String, Object> instanceNotexitRes =
                processDefinitionService.queryWorkflowDefinitionByCode(user, projectCode, 1L);
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST, instanceNotexitRes.get(Constants.STATUS));

        // instance exit
        when(workflowDefinitionMapper.queryByCode(46L)).thenReturn(getWorkflowDefinition());
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        Map<String, Object> successRes =
                processDefinitionService.queryWorkflowDefinitionByCode(user, projectCode, 46L);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryWorkflowDefinitionByName() {
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project check auth fail
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        Map<String, Object> map =
                processDefinitionService.queryWorkflowDefinitionByName(user, projectCode, "test_def");
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, map.get(Constants.STATUS));

        // project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        when(workflowDefinitionMapper.queryByDefineName(project.getCode(), "test_def")).thenReturn(null);

        Map<String, Object> instanceNotExitRes =
                processDefinitionService.queryWorkflowDefinitionByName(user, projectCode, "test_def");
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST, instanceNotExitRes.get(Constants.STATUS));

        // instance exit
        when(workflowDefinitionMapper.queryByDefineName(project.getCode(), "test"))
                .thenReturn(getWorkflowDefinition());
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        Map<String, Object> successRes =
                processDefinitionService.queryWorkflowDefinitionByName(user, projectCode, "test");
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testBatchCopyWorkflowDefinition() {
        Project project = getProject(projectCode);

        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.doReturn(result)
                .when(projectService)
                .checkProjectAndAuth(user, project, projectCode, WORKFLOW_BATCH_COPY);

        // copy project definition ids empty test
        Map<String, Object> map =
                processDefinitionService.batchCopyWorkflowDefinition(user, projectCode, StringUtils.EMPTY, 2L);
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_CODES_IS_EMPTY, map.get(Constants.STATUS));

        // project check auth fail
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_BATCH_COPY))
                .thenReturn(result);
        Map<String, Object> map1 = processDefinitionService.batchCopyWorkflowDefinition(
                user, projectCode, String.valueOf(project.getId()), 2L);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, map1.get(Constants.STATUS));

        // project check auth success, target project name not equal project name, check auth target project fail
        Project project1 = getProject(projectCodeOther);
        when(projectMapper.queryByCode(projectCodeOther)).thenReturn(project1);
        Mockito.doReturn(result)
                .when(projectService)
                .checkProjectAndAuth(user, project1, projectCodeOther, WORKFLOW_BATCH_COPY);

        putMsg(result, Status.SUCCESS, projectCodeOther);
        WorkflowDefinition definition = getWorkflowDefinition();
        List<WorkflowDefinition> workflowDefinitionList = new ArrayList<>();
        workflowDefinitionList.add(definition);
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
        when(workflowDefinitionMapper.queryByCodes(definitionCodes)).thenReturn(workflowDefinitionList);
        when(processService.saveWorkflowDefine(user, definition, Boolean.TRUE, Boolean.TRUE)).thenReturn(2);
        Map<String, Object> map3 = processDefinitionService.batchCopyWorkflowDefinition(
                user, projectCodeOther, String.valueOf(processDefinitionCode), projectCode);
        Assertions.assertEquals(Status.SUCCESS, map3.get(Constants.STATUS));
    }

    @Test
    public void testBatchMoveWorkflowDefinition() {
        Project project1 = getProject(projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project1);

        Project project2 = getProject(projectCodeOther);
        when(projectMapper.queryByCode(projectCodeOther)).thenReturn(project2);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);

        when(projectService.checkProjectAndAuth(user, project1, projectCode, TASK_DEFINITION_MOVE))
                .thenReturn(result);
        when(projectService.checkProjectAndAuth(user, project2, projectCodeOther, TASK_DEFINITION_MOVE))
                .thenReturn(result);

        WorkflowDefinition definition = getWorkflowDefinition();
        definition.setVersion(1);
        List<WorkflowDefinition> workflowDefinitionList = new ArrayList<>();
        workflowDefinitionList.add(definition);
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
        when(workflowDefinitionMapper.queryByCodes(definitionCodes)).thenReturn(workflowDefinitionList);
        when(processService.saveWorkflowDefine(user, definition, Boolean.TRUE, Boolean.TRUE)).thenReturn(2);
        when(workflowTaskRelationMapper.queryByWorkflowDefinitionCode(processDefinitionCode))
                .thenReturn(getProcessTaskRelation());
        putMsg(result, Status.SUCCESS);

        Map<String, Object> successRes = processDefinitionService.batchMoveWorkflowDefinition(
                user, projectCode, String.valueOf(processDefinitionCode), projectCodeOther);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void deleteWorkflowDefinitionByCodeTest() {
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        doNothing().when(metricsCleanUpService).cleanUpWorkflowMetricsByDefinitionCode(46L);

        Project project = getProject(projectCode);

        // process definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteWorkflowDefinitionByCode(user, 2L));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        // project check auth fail
        when(workflowDefinitionDao.queryByCode(6L)).thenReturn(Optional.of(getWorkflowDefinition()));
        doThrow(new ServiceException(Status.PROJECT_NOT_FOUND)).when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_DEFINITION_DELETE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteWorkflowDefinitionByCode(user, 6L));
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), ((ServiceException) exception).getCode());

        // project check auth success, instance not exist
        doNothing().when(projectService).checkProjectAndAuthThrowException(user, project,
                WORKFLOW_DEFINITION_DELETE);
        when(workflowDefinitionDao.queryByCode(1L)).thenReturn(Optional.empty());
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteWorkflowDefinitionByCode(user, 1L));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        // user no auth
        when(workflowDefinitionDao.queryByCode(46L)).thenReturn(Optional.of(workflowDefinition));
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteWorkflowDefinitionByCode(user, 46L));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), ((ServiceException) exception).getCode());

        // process definition online
        user.setUserType(UserType.ADMIN_USER);
        workflowDefinition.setReleaseState(ReleaseState.ONLINE);
        when(workflowDefinitionDao.queryByCode(46L)).thenReturn(Optional.of(workflowDefinition));
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteWorkflowDefinitionByCode(user, 46L));
        Assertions.assertEquals(Status.WORKFLOW_DEFINE_STATE_ONLINE.getCode(),
                ((ServiceException) exception).getCode());

        // scheduler list elements > 1
        workflowDefinition.setReleaseState(ReleaseState.OFFLINE);
        when(workflowDefinitionDao.queryByCode(46L)).thenReturn(Optional.of(workflowDefinition));
        when(scheduleMapper.queryByWorkflowDefinitionCode(46L)).thenReturn(getSchedule());
        when(scheduleMapper.deleteById(46)).thenReturn(1);
        when(workflowLineageService.taskDependentMsg(project.getCode(), workflowDefinition.getCode(), 0))
                .thenReturn(Optional.empty());
        processDefinitionService.deleteWorkflowDefinitionByCode(user, 46L);
        Mockito.verify(metricsCleanUpService, times(1)).cleanUpWorkflowMetricsByDefinitionCode(46L);

        // scheduler online
        Schedule schedule = getSchedule();
        schedule.setReleaseState(ReleaseState.ONLINE);
        when(scheduleMapper.queryByWorkflowDefinitionCode(46L)).thenReturn(schedule);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteWorkflowDefinitionByCode(user, 46L));
        Assertions.assertEquals(Status.SCHEDULE_STATE_ONLINE.getCode(), ((ServiceException) exception).getCode());

        // process used by other task, sub process
        user.setUserType(UserType.ADMIN_USER);
        TaskMainInfo taskMainInfo = getTaskMainInfo().get(0);
        when(workflowLineageService.taskDependentMsg(project.getCode(), workflowDefinition.getCode(), 0))
                .thenReturn(Optional.of(taskMainInfo.getTaskName()));
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.deleteWorkflowDefinitionByCode(user, 46L));

        // delete success
        schedule.setReleaseState(ReleaseState.OFFLINE);
        when(scheduleMapper.queryByWorkflowDefinitionCode(46L)).thenReturn(getSchedule());
        when(scheduleMapper.deleteById(schedule.getId())).thenReturn(1);
        when(workflowLineageService.taskDependentMsg(project.getCode(), workflowDefinition.getCode(), 0))
                .thenReturn(Optional.empty());
        Assertions.assertDoesNotThrow(() -> processDefinitionService.deleteWorkflowDefinitionByCode(user, 46L));
        Mockito.verify(metricsCleanUpService, times(2)).cleanUpWorkflowMetricsByDefinitionCode(46L);
    }

    @Test
    public void batchDeleteWorkflowDefinitionByCodeTest() {
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        // process check exists
        final String twoCodes = "11,12";
        Set<Long> definitionCodes = Lists.newArrayList(twoCodes.split(Constants.COMMA)).stream()
                .map(Long::parseLong).collect(Collectors.toSet());
        WorkflowDefinition process = getWorkflowDefinition();
        List<WorkflowDefinition> workflowDefinitionList = new ArrayList<>();
        workflowDefinitionList.add(process);
        when(workflowDefinitionMapper.queryByCodes(definitionCodes)).thenReturn(workflowDefinitionList);
        Throwable exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.batchDeleteWorkflowDefinitionByCodes(user, projectCode, twoCodes));
        String formatter = MessageFormat.format(Status.BATCH_DELETE_WORKFLOW_DEFINE_BY_CODES_ERROR.getMsg(),
                "12[workflow definition not exist]");
        Assertions.assertEquals(formatter, exception.getMessage());

        // return the right data
        Map<String, Object> result = new HashMap<>();
        final String singleCodes = "11";
        definitionCodes = Lists.newArrayList(singleCodes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        when(workflowDefinitionMapper.queryByCodes(definitionCodes)).thenReturn(workflowDefinitionList);
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(process));

        // process definition online
        user.setUserType(UserType.ADMIN_USER);
        putMsg(result, Status.SUCCESS, projectCode);
        process.setReleaseState(ReleaseState.ONLINE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.batchDeleteWorkflowDefinitionByCodes(user, projectCode, singleCodes));
        String subFormatter =
                MessageFormat.format(Status.WORKFLOW_DEFINE_STATE_ONLINE.getMsg(), process.getName());
        formatter =
                MessageFormat.format(Status.DELETE_WORKFLOW_DEFINE_ERROR.getMsg(), process.getName(), subFormatter);
        Assertions.assertEquals(formatter, exception.getMessage());

        // delete success
        process.setReleaseState(ReleaseState.OFFLINE);
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(process));
        when(workflowLineageService.taskDependentMsg(project.getCode(), process.getCode(), 0))
                .thenReturn(Optional.empty());
        putMsg(result, Status.SUCCESS, projectCode);
        doNothing().when(metricsCleanUpService).cleanUpWorkflowMetricsByDefinitionCode(11L);
        Map<String, Object> deleteSuccess =
                processDefinitionService.batchDeleteWorkflowDefinitionByCodes(user, projectCode, singleCodes);
        Assertions.assertEquals(Status.SUCCESS, deleteSuccess.get(Constants.STATUS));
        Mockito.verify(metricsCleanUpService, times(2)).cleanUpWorkflowMetricsByDefinitionCode(11L);
    }

    @Test
    public void testVerifyWorkflowDefinitionName() {
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Project project = getProject(projectCode);

        // project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_CREATE))
                .thenReturn(result);
        Map<String, Object> map = processDefinitionService.verifyWorkflowDefinitionName(user,
                projectCode, "test_pdf", 0);
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, map.get(Constants.STATUS));

        // project check auth success, process not exist
        putMsg(result, Status.SUCCESS, projectCode);
        when(workflowDefinitionMapper.verifyByDefineName(project.getCode(), "test_pdf")).thenReturn(null);
        Map<String, Object> processNotExistRes =
                processDefinitionService.verifyWorkflowDefinitionName(user, projectCode, "test_pdf", 0);
        Assertions.assertEquals(Status.SUCCESS, processNotExistRes.get(Constants.STATUS));

        // process exist
        when(workflowDefinitionMapper.verifyByDefineName(project.getCode(), "test_pdf"))
                .thenReturn(getWorkflowDefinition());
        Map<String, Object> processExistRes = processDefinitionService.verifyWorkflowDefinitionName(user,
                projectCode, "test_pdf", 0);
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NAME_EXIST, processExistRes.get(Constants.STATUS));
    }

    @Test
    public void testCheckWorkflowNodeList() {
        Map<String, Object> dataNotValidRes = processDefinitionService.checkWorkflowNodeList(null, null);
        Assertions.assertEquals(Status.DATA_IS_NOT_VALID, dataNotValidRes.get(Constants.STATUS));

        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);

        Map<String, Object> taskEmptyRes =
                processDefinitionService.checkWorkflowNodeList(taskRelationJson, taskDefinitionLogs);
        Assertions.assertEquals(Status.WORKFLOW_DAG_IS_EMPTY, taskEmptyRes.get(Constants.STATUS));
    }

    @Test
    public void testGetTaskNodeListByDefinitionCode() {
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Project project = getProject(projectCode);

        // project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectService.checkProjectAndAuth(user, project, projectCode, null)).thenReturn(result);
        // process definition not exist
        when(workflowDefinitionMapper.queryByCode(46L)).thenReturn(null);
        Map<String, Object> processDefinitionNullRes =
                processDefinitionService.getTaskNodeListByDefinitionCode(user, projectCode, 46L);
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST, processDefinitionNullRes.get(Constants.STATUS));

        // success
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        putMsg(result, Status.SUCCESS, projectCode);
        when(processService.genDagData(Mockito.any())).thenReturn(new DagData(workflowDefinition, null, null));
        when(workflowDefinitionMapper.queryByCode(46L)).thenReturn(workflowDefinition);
        Map<String, Object> dataNotValidRes =
                processDefinitionService.getTaskNodeListByDefinitionCode(user, projectCode, 46L);
        Assertions.assertEquals(Status.SUCCESS, dataNotValidRes.get(Constants.STATUS));
    }

    @Test
    public void testGetTaskNodeListByDefinitionCodes() {
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Project project = getProject(projectCode);

        // project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectService.checkProjectAndAuth(user, project, projectCode, null)).thenReturn(result);
        // process definition not exist
        String defineCodes = "46";
        Set<Long> defineCodeSet = Lists.newArrayList(defineCodes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        when(workflowDefinitionMapper.queryByCodes(defineCodeSet)).thenReturn(null);
        Map<String, Object> processNotExistRes =
                processDefinitionService.getNodeListMapByDefinitionCodes(user, projectCode, defineCodes);
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST, processNotExistRes.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectCode);
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        List<WorkflowDefinition> workflowDefinitionList = new ArrayList<>();
        workflowDefinitionList.add(workflowDefinition);

        when(workflowDefinitionMapper.queryByCodes(defineCodeSet)).thenReturn(workflowDefinitionList);
        when(processService.genDagData(Mockito.any())).thenReturn(new DagData(workflowDefinition, null, null));
        Project project1 = getProject(projectCode);
        List<Project> projects = new ArrayList<>();
        projects.add(project1);
        when(projectMapper.queryProjectCreatedAndAuthorizedByUserId(user.getId())).thenReturn(projects);

        Map<String, Object> successRes =
                processDefinitionService.getNodeListMapByDefinitionCodes(user, projectCode, defineCodes);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryAllWorkflowDefinitionByProjectCode() {
        Map<String, Object> result = new HashMap<>();
        Project project = getProject(projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        List<WorkflowDefinition> workflowDefinitionList = new ArrayList<>();
        workflowDefinitionList.add(workflowDefinition);
        when(workflowDefinitionMapper.queryAllDefinitionList(projectCode)).thenReturn(workflowDefinitionList);
        Map<String, Object> successRes =
                processDefinitionService.queryAllWorkflowDefinitionByProjectCode(user, projectCode);
        Assertions.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testViewTree() {
        Project project1 = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(1)).thenReturn(project1);
        when(projectService.checkProjectAndAuth(user, project1, projectCode, WORKFLOW_TREE_VIEW))
                .thenReturn(result);
        // process definition not exist
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        Map<String, Object> processDefinitionNullRes =
                processDefinitionService.viewTree(user, workflowDefinition.getProjectCode(), 46, 10);
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST, processDefinitionNullRes.get(Constants.STATUS));

        // task instance not existproject
        putMsg(result, Status.SUCCESS, projectCode);
        when(projectMapper.queryByCode(1)).thenReturn(project1);
        when(projectService.checkProjectAndAuth(user, project1, 1, WORKFLOW_TREE_VIEW)).thenReturn(result);
        when(workflowDefinitionMapper.queryByCode(46L)).thenReturn(workflowDefinition);
        when(processService.genDagGraph(workflowDefinition)).thenReturn(new DAG<>());
        Map<String, Object> taskNullRes =
                processDefinitionService.viewTree(user, workflowDefinition.getProjectCode(), 46, 10);
        Assertions.assertEquals(Status.SUCCESS, taskNullRes.get(Constants.STATUS));

        // task instance exist
        Map<String, Object> taskNotNuLLRes =
                processDefinitionService.viewTree(user, workflowDefinition.getProjectCode(), 46, 10);
        Assertions.assertEquals(Status.SUCCESS, taskNotNuLLRes.get(Constants.STATUS));
    }

    @Test
    public void testSubProcessViewTree() {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        when(workflowDefinitionMapper.queryByCode(46L)).thenReturn(workflowDefinition);

        Project project1 = getProject(1);
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, Status.SUCCESS);
        when(projectMapper.queryByCode(1)).thenReturn(project1);
        when(projectService.checkProjectAndAuth(user, project1, 1, WORKFLOW_TREE_VIEW)).thenReturn(result);
        when(processService.genDagGraph(workflowDefinition)).thenReturn(new DAG<>());
        Map<String, Object> taskNotNuLLRes =
                processDefinitionService.viewTree(user, workflowDefinition.getProjectCode(), 46, 10);
        Assertions.assertEquals(Status.SUCCESS, taskNotNuLLRes.get(Constants.STATUS));
    }

    @Test
    public void testUpdateWorkflowDefinition() {
        Map<String, Object> result = new HashMap<>();

        Project project = getProject(projectCode);
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        when(projectService.hasProjectAndWritePerm(user, project, result)).thenReturn(true);

        try {
            processDefinitionService.updateWorkflowDefinition(user, projectCode, "test", 1,
                    "", "", "", 0, null, "", WorkflowExecutionTypeEnum.PARALLEL);
            Assertions.fail();
        } catch (ServiceException ex) {
            Assertions.assertEquals(Status.DATA_IS_NOT_VALID.getCode(), ex.getCode());
        }
    }

    @Test
    public void testBatchExportWorkflowDefinitionByCodes() {
        processDefinitionService.batchExportWorkflowDefinitionByCodes(null, 1L, null, null);
        Project project = getProject(projectCode);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND);
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        processDefinitionService.batchExportWorkflowDefinitionByCodes(user, projectCode, "1", null);

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setId(1);
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        DagData dagData = new DagData(getWorkflowDefinition(), null, null);
        when(processService.genDagData(Mockito.any())).thenReturn(dagData);
        processDefinitionService.batchExportWorkflowDefinitionByCodes(user, projectCode, "1", response);
        Assertions.assertNotNull(processDefinitionService.exportWorkflowDagData(workflowDefinition));
    }

    @Test
    public void testImportSqlWorkflowDefinition() throws Exception {
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
        when(dataSource.getId()).thenReturn(1);
        when(dataSource.getType()).thenReturn(DbType.MYSQL);

        when(dataSourceMapper.queryDataSourceByNameAndUserId(user.getId(), "mysql_1")).thenReturn(dataSource);

        Project project = getProject(projectCode);
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, Status.SUCCESS);
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_IMPORT))
                .thenReturn(result);
        when(processService.saveTaskDefine(Mockito.same(user), Mockito.eq(projectCode), Mockito.notNull(),
                Mockito.anyBoolean())).thenReturn(2);
        when(processService.saveWorkflowDefine(Mockito.same(user), Mockito.notNull(), Mockito.notNull(),
                Mockito.anyBoolean())).thenReturn(1);
        when(
                processService.saveTaskRelation(Mockito.same(user), Mockito.eq(projectCode), Mockito.anyLong(),
                        Mockito.eq(1), Mockito.notNull(), Mockito.notNull(), Mockito.anyBoolean()))
                                .thenReturn(0);
        result = processDefinitionService.importSqlWorkflowDefinition(user, projectCode, mockMultipartFile);

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
    public void testCreateWorkflowDefinitionV2() {
        Project project = this.getProject(projectCode);

        WorkflowCreateRequest workflowCreateRequest = new WorkflowCreateRequest();
        workflowCreateRequest.setName(name);
        workflowCreateRequest.setProjectCode(projectCode);

        // project not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.createSingleWorkflowDefinition(user, workflowCreateRequest));
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), ((ServiceException) exception).getCode());

        // project permission error
        when(projectMapper.queryByCode(projectCode)).thenReturn(project);
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM)).when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_CREATE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.createSingleWorkflowDefinition(user, workflowCreateRequest));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // description too long
        workflowCreateRequest.setDescription(taskDefinitionJson);
        doThrow(new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR)).when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_CREATE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.createSingleWorkflowDefinition(user, workflowCreateRequest));
        Assertions.assertEquals(Status.DESCRIPTION_TOO_LONG_ERROR.getCode(), ((ServiceException) exception).getCode());
        workflowCreateRequest.setDescription(EMPTY_STRING);

        // duplicate process definition name
        doNothing().when(projectService).checkProjectAndAuthThrowException(user, project, WORKFLOW_CREATE);
        when(workflowDefinitionMapper.verifyByDefineName(project.getCode(), name))
                .thenReturn(this.getWorkflowDefinition());
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.createSingleWorkflowDefinition(user, workflowCreateRequest));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NAME_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        when(workflowDefinitionMapper.verifyByDefineName(project.getCode(), name)).thenReturn(null);

        // test success
        workflowCreateRequest.setDescription(description);
        workflowCreateRequest.setTimeout(timeout);
        workflowCreateRequest.setReleaseState(releaseState);
        workflowCreateRequest.setWarningGroupId(warningGroupId);
        workflowCreateRequest.setExecutionType(executionType);
        when(workflowDefinitionLogMapper.insert(Mockito.any())).thenReturn(1);
        when(workflowDefinitionMapper.insert(Mockito.any())).thenReturn(1);
        WorkflowDefinition workflowDefinition =
                processDefinitionService.createSingleWorkflowDefinition(user, workflowCreateRequest);

        Assertions.assertTrue(workflowDefinition.getCode() > 0L);
        Assertions.assertEquals(workflowCreateRequest.getName(), workflowDefinition.getName());
        Assertions.assertEquals(workflowCreateRequest.getDescription(), workflowDefinition.getDescription());
        Assertions.assertTrue(StringUtils.endsWithIgnoreCase(workflowCreateRequest.getReleaseState(),
                workflowDefinition.getReleaseState().getDescp()));
        Assertions.assertEquals(workflowCreateRequest.getTimeout(), workflowDefinition.getTimeout());
        Assertions.assertTrue(StringUtils.endsWithIgnoreCase(workflowCreateRequest.getExecutionType(),
                workflowDefinition.getExecutionType().getDescp()));
    }

    @Test
    public void testFilterWorkflowDefinition() {
        Project project = this.getProject(projectCode);
        WorkflowFilterRequest workflowFilterRequest = new WorkflowFilterRequest();
        workflowFilterRequest.setProjectName(project.getName());

        // project permission error
        when(projectMapper.queryByName(project.getName())).thenReturn(project);
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM, user.getUserName(), projectCode))
                .when(projectService).checkProjectAndAuthThrowException(user, project, WORKFLOW_DEFINITION);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.filterWorkflowDefinition(user, workflowFilterRequest));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());
    }

    @Test
    public void testGetWorkflowDefinition() {
        // process definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.getWorkflowDefinition(user, processDefinitionCode));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        // project permission error
        when(workflowDefinitionMapper.queryByCode(processDefinitionCode))
                .thenReturn(this.getWorkflowDefinition());
        when(projectMapper.queryByCode(projectCode)).thenReturn(this.getProject(projectCode));
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM, user.getUserName(), projectCode))
                .when(projectService)
                .checkProjectAndAuthThrowException(user, this.getProject(projectCode), WORKFLOW_DEFINITION);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.getWorkflowDefinition(user, processDefinitionCode));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // success
        doNothing().when(projectService).checkProjectAndAuthThrowException(user, this.getProject(projectCode),
                WORKFLOW_DEFINITION);
        WorkflowDefinition workflowDefinition =
                processDefinitionService.getWorkflowDefinition(user, processDefinitionCode);
        Assertions.assertEquals(this.getWorkflowDefinition(), workflowDefinition);
    }

    @Test
    public void testUpdateWorkflowDefinitionV2() {
        WorkflowDefinition workflowDefinition;

        WorkflowUpdateRequest workflowUpdateRequest = new WorkflowUpdateRequest();
        workflowUpdateRequest.setName(name);

        // error process definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.getWorkflowDefinition(user, processDefinitionCode));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        // error old process definition in release state
        workflowDefinition = this.getWorkflowDefinition();
        workflowDefinition.setReleaseState(ReleaseState.ONLINE);
        when(workflowDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(workflowDefinition);
        exception = Assertions.assertThrows(ServiceException.class, () -> processDefinitionService
                .updateSingleWorkflowDefinition(user, processDefinitionCode, workflowUpdateRequest));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_ALLOWED_EDIT.getCode(),
                ((ServiceException) exception).getCode());

        // error project permission
        workflowDefinition = this.getWorkflowDefinition();
        when(workflowDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(workflowDefinition);
        when(projectMapper.queryByCode(projectCode)).thenReturn(this.getProject(projectCode));
        doThrow(new ServiceException(Status.USER_NO_OPERATION_PROJECT_PERM, user.getUserName(), projectCode))
                .when(projectService)
                .checkProjectAndAuthThrowException(user, this.getProject(projectCode), WORKFLOW_DEFINITION);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> processDefinitionService.getWorkflowDefinition(user, processDefinitionCode));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(),
                ((ServiceException) exception).getCode());

        // error description too long
        workflowUpdateRequest.setDescription(taskDefinitionJson);
        doThrow(new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR)).when(projectService)
                .checkProjectAndAuthThrowException(user, this.getProject(projectCode), WORKFLOW_UPDATE);
        exception = Assertions.assertThrows(ServiceException.class, () -> processDefinitionService
                .updateSingleWorkflowDefinition(user, processDefinitionCode, workflowUpdateRequest));
        Assertions.assertEquals(Status.DESCRIPTION_TOO_LONG_ERROR.getCode(), ((ServiceException) exception).getCode());
        workflowUpdateRequest.setDescription(EMPTY_STRING);

        // error new definition name already exists
        doNothing().when(projectService).checkProjectAndAuthThrowException(user, this.getProject(projectCode),
                WORKFLOW_UPDATE);
        when(workflowDefinitionMapper.verifyByDefineName(projectCode, workflowUpdateRequest.getName()))
                .thenReturn(this.getWorkflowDefinition());
        exception = Assertions.assertThrows(ServiceException.class, () -> processDefinitionService
                .updateSingleWorkflowDefinition(user, processDefinitionCode, workflowUpdateRequest));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NAME_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        when(workflowDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(workflowDefinition);
        when(workflowDefinitionMapper.verifyByDefineName(projectCode, workflowUpdateRequest.getName()))
                .thenReturn(null);
        // error update process definition mapper
        workflowUpdateRequest.setName(name);
        when(workflowDefinitionMapper.queryByCode(processDefinitionCode)).thenReturn(workflowDefinition);
        when(workflowDefinitionLogMapper.insert(Mockito.any())).thenReturn(1);
        exception = Assertions.assertThrows(ServiceException.class, () -> processDefinitionService
                .updateSingleWorkflowDefinition(user, processDefinitionCode, workflowUpdateRequest));
        Assertions.assertEquals(Status.UPDATE_WORKFLOW_DEFINITION_ERROR.getCode(),
                ((ServiceException) exception).getCode());

        // success
        when(workflowDefinitionLogMapper.queryMaxVersionForDefinition(workflowDefinition.getCode()))
                .thenReturn(workflowDefinition.getVersion());
        when(workflowDefinitionMapper.updateById(isA(WorkflowDefinition.class))).thenReturn(1);
        WorkflowDefinition workflowDefinitionUpdate =
                processDefinitionService.updateSingleWorkflowDefinition(user, processDefinitionCode,
                        workflowUpdateRequest);
        Assertions.assertNotNull(workflowDefinitionUpdate);

        // check version
        Assertions.assertEquals(workflowDefinition.getVersion() + 1, workflowDefinitionUpdate.getVersion());
    }

    @Test
    public void testCheckVersion() {
        WorkflowFilterRequest workflowFilterRequest = new WorkflowFilterRequest();
        workflowFilterRequest.setWorkflowName(name);

    }

    @Test
    public void testViewVariables() {
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        WorkflowDefinition workflowDefinition = getWorkflowDefinition();

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);

        // project check auth fail
        when(projectService.checkProjectAndAuth(user, project, projectCode, WORKFLOW_DEFINITION))
                .thenReturn(result);

        Map<String, Object> map =
                processDefinitionService.viewVariables(user, workflowDefinition.getProjectCode(),
                        workflowDefinition.getCode());

        Assertions.assertEquals(Status.PROJECT_NOT_FOUND, map.get(Constants.STATUS));
    }

    /**
     * get mock processDefinition
     *
     * @return ProcessDefinition
     */
    private WorkflowDefinition getWorkflowDefinition() {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setId(46);
        workflowDefinition.setProjectCode(1L);
        workflowDefinition.setName("test_pdf");
        workflowDefinition.setDescription("");
        workflowDefinition.setCode(processDefinitionCode);
        workflowDefinition.setProjectCode(projectCode);
        workflowDefinition.setVersion(1);
        return workflowDefinition;
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

    private List<WorkflowTaskRelation> getProcessTaskRelation() {
        List<WorkflowTaskRelation> workflowTaskRelations = new ArrayList<>();
        WorkflowTaskRelation workflowTaskRelation = new WorkflowTaskRelation();
        workflowTaskRelation.setProjectCode(projectCode);
        workflowTaskRelation.setWorkflowDefinitionCode(46L);
        workflowTaskRelation.setWorkflowDefinitionVersion(1);
        workflowTaskRelation.setPreTaskCode(100);
        workflowTaskRelation.setPostTaskCode(200);
        workflowTaskRelations.add(workflowTaskRelation);
        return workflowTaskRelations;
    }

    private WorkflowTaskRelation getWorkflowTaskRelation(int id, int workflowDefinitionVersion, long projectCode,
                                                         long workflowDefinitionCode, long preTaskCode,
                                                         int preTaskVersion,
                                                         long postTaskCode, int postTaskVersion) {
        WorkflowTaskRelation workflowTaskRelation = new WorkflowTaskRelation();
        workflowTaskRelation.setId(id);
        workflowTaskRelation.setWorkflowDefinitionVersion(workflowDefinitionVersion);
        workflowTaskRelation.setProjectCode(projectCode);
        workflowTaskRelation.setWorkflowDefinitionCode(workflowDefinitionCode);
        workflowTaskRelation.setPreTaskCode(preTaskCode);
        workflowTaskRelation.setPreTaskVersion(preTaskVersion);
        workflowTaskRelation.setPostTaskCode(postTaskCode);
        workflowTaskRelation.setPostTaskVersion(postTaskVersion);
        return workflowTaskRelation;
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
        schedule.setWorkflowDefinitionCode(1);
        schedule.setStartTime(date);
        schedule.setEndTime(date);
        schedule.setCrontab("0 0 5 * * ? *");
        schedule.setFailureStrategy(FailureStrategy.END);
        schedule.setUserId(1);
        schedule.setReleaseState(ReleaseState.OFFLINE);
        schedule.setWorkflowInstancePriority(Priority.MEDIUM);
        schedule.setWarningType(WarningType.NONE);
        schedule.setWarningGroupId(1);
        schedule.setWorkerGroup(WorkerGroupUtils.getDefaultWorkerGroup());
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
