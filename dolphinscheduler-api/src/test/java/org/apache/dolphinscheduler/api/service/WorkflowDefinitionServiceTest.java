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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_BATCH_COPY;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_DEFINITION;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_TREE_VIEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.permission.TaskDatasourcePermissionChecker;
import org.apache.dolphinscheduler.api.permission.TaskSubWorkflowPermissionChecker;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.WorkflowDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.validator.GlobalParamsValidator;
import org.apache.dolphinscheduler.api.validator.TaskProjectValidator;
import org.apache.dolphinscheduler.api.validator.WorkflowRelationValidator;
import org.apache.dolphinscheduler.api.vo.WorkflowDefinitionCopyVO;
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
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskMainInfo;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.UserWithWorkflowDefinitionCode;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.model.PageListingResult;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.ScheduleDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.UserDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowTaskRelationDao;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.model.ConditionDependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.ConditionDependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ConditionsParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.process.ProcessServiceImpl;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private WorkflowDefinitionServiceImpl workflowDefinitionService;

    @Mock
    private TaskProjectValidator taskProjectValidator;

    @Mock
    private WorkflowRelationValidator workflowRelationValidator;

    @Mock
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Mock
    private WorkflowDefinitionLogDao workflowDefinitionLogDao;

    @Mock
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

    @Mock
    private WorkflowInstanceMapper workflowInstanceMapper;

    @Mock
    private WorkflowTaskRelationDao workflowTaskRelationDao;

    @Mock
    private WorkflowTaskRelationLogMapper workflowTaskRelationLogMapper;

    @Mock
    private ProjectDao projectDao;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ScheduleDao scheduleDao;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private ProcessService processService;

    @Mock
    private WorkflowInstanceService workflowInstanceService;

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Mock
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Mock
    private TaskDatasourcePermissionChecker taskDatasourcePermissionChecker;

    @Mock
    private TaskSubWorkflowPermissionChecker taskSubWorkflowPermissionChecker;

    @Mock
    private TaskDefinitionService taskDefinitionService;

    @Mock
    private TaskDefinitionLogService taskDefinitionLogService;

    @Mock
    private TaskDefinitionDao taskDefinitionDao;

    @Mock
    private WorkflowLineageService workflowLineageService;

    @Mock
    private GlobalParamsValidator globalParamsValidator;

    @Mock
    private UserDao userDao;

    protected User user;
    protected Exception exception;
    protected final static long projectCode = 1L;
    protected final static long projectCodeOther = 2L;
    protected final static long processDefinitionCode = 11L;
    protected final static String name = "testProcessDefinitionName";
    protected final static String description = "this is a description";
    protected final static int timeout = 60;

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

        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_BATCH_COPY);
        when(workflowDefinitionDao.queryByCodes(definitionCodes)).thenReturn(workflowDefinitionList);
        when(workflowTaskRelationDao.queryByWorkflowDefinitionCode(Long.parseLong(codes)))
                .thenReturn(workflowTaskRelations);
        when(taskDefinitionLogDao.queryTaskDefineLogList(workflowTaskRelations)).thenReturn(taskDefinitionLogs);
        when(processService.saveTaskDefine(user, projectCode, taskDefinitionLogs, true)).thenReturn(1);
        when(processService.saveWorkflowDefine(user, workflowDefinition, true, true)).thenReturn(1);
        Assertions.assertDoesNotThrow(
                () -> workflowDefinitionService.batchCopyWorkflowDefinition(user, projectCode, codes,
                        targetProjectCode));
    }
    @Test
    public void testQueryWorkflowDefinitionList() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);

        // project not found
        Mockito.doThrow(new ServiceException(Status.PROJECT_NOT_FOUND))
                .when(projectService).checkProjectAndAuthThrowException(user, project, WORKFLOW_DEFINITION);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.queryWorkflowDefinitionList(user, projectCode));
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), ex.getCode());

        // project check auth success
        Mockito.doNothing().when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_DEFINITION);
        List<WorkflowDefinition> resourceList = new ArrayList<>();
        resourceList.add(getWorkflowDefinition());
        when(workflowDefinitionDao.queryAllDefinitionList(project.getCode())).thenReturn(resourceList);
        List<DagData> dagDataList = workflowDefinitionService.queryWorkflowDefinitionList(user, projectCode);
        Assertions.assertNotNull(dagDataList);
    }

    @Test
    public void testQueryWorkflowDefinitionListPaging() {

        // project not found
        try {
            doThrow(new ServiceException(Status.PROJECT_NOT_EXIST)).when(projectService)
                    .checkProjectAndAuthThrowException(user, projectCode, WORKFLOW_DEFINITION);
            workflowDefinitionService.queryWorkflowDefinitionListPaging(user, projectCode, "", "", 1, 5, 0, null,
                    null);
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
                eq(0),
                eq(10),
                eq(""),
                eq(1),
                eq(projectCode),
                any(),
                any())).thenReturn(pageListingResult);
        String user1 = "user1";
        String user2 = "user2";
        when(userDao.queryUserWithWorkflowDefinitionCode(processDefinitionCodes))
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
        PageInfo<WorkflowDefinition> pageInfo = workflowDefinitionService.queryWorkflowDefinitionListPaging(
                user,
                projectCode,
                "",
                "",
                1,
                0,
                10,
                null,
                null);
        Assertions.assertNotNull(pageInfo);
        WorkflowDefinition pd1 = pageInfo.getTotalList().stream()
                .filter(pd -> pd.getCode() == processDefinitionCode1).findFirst().orElse(null);
        assert pd1 != null;
        Assertions.assertEquals(pd1.getModifyBy(), user1);
    }

    @Test
    public void testQueryWorkflowDefinitionByCode() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);

        // project check auth fail
        Mockito.doThrow(new ServiceException(Status.PROJECT_NOT_FOUND))
                .when(projectService).checkProjectAndAuthThrowException(user, project, WORKFLOW_DEFINITION);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.queryWorkflowDefinitionByCode(user, projectCode, 1L));
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), ex.getCode());

        // project check auth success, instance not exist
        Mockito.doNothing().when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_DEFINITION);
        DagData dagData = new DagData(getWorkflowDefinition(), null, null);
        when(processService.genDagData(any())).thenReturn(dagData);

        ServiceException notFound = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.queryWorkflowDefinitionByCode(user, projectCode, 1L));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(), notFound.getCode());

        // instance exit
        when(workflowDefinitionDao.queryByCode(46L)).thenReturn(Optional.of(getWorkflowDefinition()));
        DagData successRes = workflowDefinitionService.queryWorkflowDefinitionByCode(user, projectCode, 46L);
        Assertions.assertNotNull(successRes);
    }

    @Test
    public void testQueryWorkflowDefinitionByName() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);

        // project check auth fail
        Mockito.doThrow(new ServiceException(Status.PROJECT_NOT_FOUND))
                .when(projectService).checkProjectAndAuthThrowException(user, project, WORKFLOW_DEFINITION);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.queryWorkflowDefinitionByName(user, projectCode, "test_def"));
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), ex.getCode());

        // project check auth success, instance not exist
        Mockito.doNothing().when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_DEFINITION);
        when(workflowDefinitionDao.queryByDefineName(project.getCode(), "test_def")).thenReturn(null);

        ServiceException notFoundEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.queryWorkflowDefinitionByName(user, projectCode, "test_def"));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(), notFoundEx.getCode());

        // instance exit
        when(workflowDefinitionDao.queryByDefineName(project.getCode(), "test"))
                .thenReturn(getWorkflowDefinition());
        when(processService.genDagData(any())).thenReturn(new DagData(getWorkflowDefinition(), null, null));
        DagData successRes = workflowDefinitionService.queryWorkflowDefinitionByName(user, projectCode, "test");
        Assertions.assertNotNull(successRes);
    }

    @Test
    public void testBatchCopyWorkflowDefinition() {
        Project project = getProject(projectCode);

        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_BATCH_COPY);

        // copy project definition ids empty test
        ServiceException emptyEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.batchCopyWorkflowDefinition(user, projectCode, StringUtils.EMPTY, 2L));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_CODES_IS_EMPTY.getCode(), emptyEx.getCode());

        // project check auth fail
        Mockito.doThrow(new ServiceException(Status.PROJECT_NOT_FOUND))
                .when(projectService).checkProjectAndAuthThrowException(user, project, WORKFLOW_BATCH_COPY);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.batchCopyWorkflowDefinition(
                        user, projectCode, String.valueOf(project.getId()), 2L));
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), ex.getCode());

        // project check auth success, target project name not equal project name
        Project project1 = getProject(projectCodeOther);
        when(projectDao.queryByCode(projectCodeOther)).thenReturn(project1);
        Mockito.doNothing().when(projectService)
                .checkProjectAndAuthThrowException(user, project1, WORKFLOW_BATCH_COPY);
        Mockito.doNothing().when(projectService).checkHasProjectWritePermissionThrowException(user, project);

        WorkflowDefinition definition = getWorkflowDefinition();
        definition.setProjectCode(projectCodeOther);
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
        when(workflowDefinitionDao.queryByCodes(definitionCodes)).thenReturn(workflowDefinitionList);
        when(processService.saveWorkflowDefine(user, definition, Boolean.TRUE, Boolean.TRUE)).thenReturn(2);
        List<WorkflowDefinitionCopyVO> copyResponses = Assertions.assertDoesNotThrow(
                () -> workflowDefinitionService.batchCopyWorkflowDefinition(
                        user, projectCodeOther, String.valueOf(processDefinitionCode), projectCode));

        Assertions.assertEquals(1, copyResponses.size());
        Assertions.assertEquals(projectCode, copyResponses.get(0).getProjectCode());
        Assertions.assertEquals(definition.getCode(), copyResponses.get(0).getWorkflowDefinitionCode());
    }

    @ParameterizedTest
    @CsvSource({"1,101", "1,0", "1,-1", "2,101", "2,0", "2,-1"})
    public void testBatchCopyWorkflowDefinitionRemapsSelfDependencies(long targetProjectCode, long depTaskCode) {
        ReflectionTestUtils.setField(workflowDefinitionService, "taskProjectValidator",
                new TaskProjectValidator(taskDefinitionDao, taskDefinitionLogMapper));
        ReflectionTestUtils.setField(workflowDefinitionService, "workflowRelationValidator",
                new WorkflowRelationValidator(new TaskProjectValidator(taskDefinitionDao, taskDefinitionLogMapper)));
        when(projectDao.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        if (targetProjectCode != projectCode) {
            when(projectDao.queryByCode(targetProjectCode)).thenReturn(getProject(targetProjectCode));
        }
        WorkflowDefinition definition = getWorkflowDefinition();
        when(workflowDefinitionDao.queryByCodes(Collections.singleton(processDefinitionCode)))
                .thenReturn(Collections.singletonList(definition));
        when(processService.saveWorkflowDefine(eq(user), any(), eq(true), eq(true))).thenReturn(1);

        TaskDefinitionLog shellTask = new TaskDefinitionLog();
        shellTask.setCode(101L);
        shellTask.setProjectCode(projectCode);
        shellTask.setVersion(1);
        shellTask.setTaskType("SHELL");
        shellTask.setTaskParams("{\"rawScript\":\"echo test\"}");
        TaskDefinitionLog dependentTask = new TaskDefinitionLog();
        dependentTask.setCode(102L);
        dependentTask.setProjectCode(projectCode);
        dependentTask.setVersion(1);
        dependentTask.setTaskType("DEPENDENT");
        String taskParams = String.format(
                "{\"localParams\":[{\"prop\":\"runDate\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"${bizdate}\"}],"
                        + "\"dependence\":{\"relation\":\"AND\",\"checkInterval\":15,"
                        + "\"failurePolicy\":\"DEPENDENT_FAILURE_WAITING\",\"failureWaitingTime\":30,"
                        + "\"dependTaskList\":[{\"relation\":\"AND\",\"dependItemList\":["
                        + "{\"projectCode\":1,\"definitionCode\":11,\"depTaskCode\":%d,"
                        + "\"cycle\":\"day\",\"dateValue\":\"last1\",\"parameterPassing\":true}]},"
                        + "{\"relation\":\"OR\",\"dependItemList\":["
                        + "{\"projectCode\":1,\"definitionCode\":99,\"depTaskCode\":101,\"cycle\":\"day\",\"dateValue\":\"today\"},"
                        + "{\"projectCode\":3,\"definitionCode\":98,\"depTaskCode\":999,\"cycle\":\"day\",\"dateValue\":\"last1\"},"
                        + "{\"projectCode\":3,\"definitionCode\":11,\"depTaskCode\":101,\"cycle\":\"day\",\"dateValue\":\"last1\"},"
                        + "{\"projectCode\":1,\"definitionCode\":11,\"depTaskCode\":102,\"cycle\":\"day\",\"dateValue\":\"last2\"}]}]}}",
                depTaskCode);
        dependentTask.setTaskParams(taskParams);
        List<TaskDefinitionLog> tasks = Arrays.asList(shellTask, dependentTask);
        WorkflowTaskRelation relation = getWorkflowTaskRelation(1, 1, projectCode, processDefinitionCode,
                102L, 1, 101L, 1);
        List<WorkflowTaskRelation> relations = Collections.singletonList(relation);
        when(workflowTaskRelationDao.queryByWorkflowDefinitionCode(processDefinitionCode)).thenReturn(relations);
        when(taskDefinitionLogDao.queryTaskDefineLogList(relations)).thenReturn(tasks);

        List<WorkflowDefinitionCopyVO> copies = workflowDefinitionService.batchCopyWorkflowDefinition(
                user, projectCode, String.valueOf(processDefinitionCode), targetProjectCode);

        Assertions.assertEquals(1, copies.size());
        long copiedWorkflowCode = copies.get(0).getWorkflowDefinitionCode();
        Assertions.assertNotEquals(processDefinitionCode, copiedWorkflowCode);
        ArgumentCaptor<List<TaskDefinitionLog>> tasksCaptor = ArgumentCaptor.forClass(List.class);
        verify(processService).saveTaskDefine(eq(user), eq(targetProjectCode), tasksCaptor.capture(), eq(true));
        List<TaskDefinitionLog> copiedTasks = tasksCaptor.getValue();
        TaskDefinitionLog copiedShell = copiedTasks.stream().filter(task -> "SHELL".equals(task.getTaskType()))
                .findFirst().get();
        TaskDefinitionLog copiedDependent = copiedTasks.stream().filter(task -> "DEPENDENT".equals(task.getTaskType()))
                .findFirst().get();
        Assertions.assertNotEquals(101L, copiedShell.getCode());
        Assertions.assertNotEquals(102L, copiedDependent.getCode());
        Assertions.assertEquals(targetProjectCode, copiedDependent.getProjectCode());
        Assertions.assertEquals("{\"rawScript\":\"echo test\"}", copiedShell.getTaskParams());
        ObjectNode expectedParams = JSONUtils.parseObject(taskParams, ObjectNode.class);
        ObjectNode selfDependency = (ObjectNode) expectedParams.path("dependence").path("dependTaskList")
                .get(0).path("dependItemList").get(0);
        selfDependency.put("projectCode", targetProjectCode);
        selfDependency.put("definitionCode", copiedWorkflowCode);
        selfDependency.put("depTaskCode", depTaskCode > 0 ? copiedShell.getCode() : depTaskCode);
        ObjectNode otherSelfDependency = (ObjectNode) expectedParams.path("dependence").path("dependTaskList")
                .get(1).path("dependItemList").get(3);
        otherSelfDependency.put("projectCode", targetProjectCode);
        otherSelfDependency.put("definitionCode", copiedWorkflowCode);
        otherSelfDependency.put("depTaskCode", copiedDependent.getCode());
        Assertions.assertEquals(JSONUtils.parseObject(JSONUtils.toJsonString(expectedParams), ObjectNode.class),
                JSONUtils.parseObject(copiedDependent.getTaskParams(), ObjectNode.class));
    }

    @Test
    public void testBatchCopyWorkflowDefinitionRequiresTargetProjectWritePermission() {
        Project sourceProject = getProject(projectCode);
        Project targetProject = getProject(projectCodeOther);
        when(projectDao.queryByCode(projectCode)).thenReturn(sourceProject);
        when(projectDao.queryByCode(projectCodeOther)).thenReturn(targetProject);
        doNothing().when(projectService)
                .checkProjectAndAuthThrowException(user, sourceProject, WORKFLOW_BATCH_COPY);
        doThrow(new ServiceException(Status.USER_NO_WRITE_PROJECT_PERM))
                .when(projectService).checkHasProjectWritePermissionThrowException(user, targetProject);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.batchCopyWorkflowDefinition(
                        user, projectCode, String.valueOf(processDefinitionCode), projectCodeOther));

        Assertions.assertEquals(Status.USER_NO_WRITE_PROJECT_PERM.getCode(), ex.getCode());
        verify(workflowDefinitionDao, Mockito.never()).queryByCodes(Mockito.anySet());
    }

    @Test
    public void testReadOnlyUserCannotSwitchOrReleaseWorkflowDefinition() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        doThrow(new ServiceException(Status.USER_NO_WRITE_PROJECT_PERM))
                .when(projectService).checkHasProjectWritePermissionThrowException(user, project);
        doThrow(new ServiceException(Status.USER_NO_WRITE_PROJECT_PERM))
                .when(projectService).checkHasProjectWritePermissionThrowException(user, projectCode);

        ServiceException switchEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.switchWorkflowDefinitionVersion(
                        user, projectCode, processDefinitionCode, 1));
        ServiceException onlineEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.onlineWorkflowDefinition(
                        user, projectCode, processDefinitionCode));
        ServiceException offlineEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.offlineWorkflowDefinition(
                        user, projectCode, processDefinitionCode));

        Assertions.assertEquals(Status.USER_NO_WRITE_PROJECT_PERM.getCode(), switchEx.getCode());
        Assertions.assertEquals(Status.USER_NO_WRITE_PROJECT_PERM.getCode(), onlineEx.getCode());
        Assertions.assertEquals(Status.USER_NO_WRITE_PROJECT_PERM.getCode(), offlineEx.getCode());
        verify(workflowDefinitionDao, Mockito.never()).updateById(Mockito.any());
    }

    @Test
    public void testProjectCodeCannotAuthorizeWorkflowDefinitionFromAnotherProject() {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        workflowDefinition.setProjectCode(projectCodeOther);
        when(workflowDefinitionDao.queryByCode(processDefinitionCode))
                .thenReturn(Optional.of(workflowDefinition));

        ServiceException onlineEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.onlineWorkflowDefinition(
                        user, projectCode, processDefinitionCode));
        ServiceException offlineEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.offlineWorkflowDefinition(
                        user, projectCode, processDefinitionCode));

        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(), onlineEx.getCode());
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(), offlineEx.getCode());
        verify(workflowDefinitionDao, Mockito.never()).updateById(Mockito.any());
    }

    @Test
    public void deleteWorkflowDefinitionByCodeTest() {
        when(projectDao.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        // process definition not exists
        exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.deleteWorkflowDefinitionByCode(user, 2L));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        // project check auth fail
        when(workflowDefinitionDao.queryByCode(6L)).thenReturn(Optional.of(getWorkflowDefinition()));
        doThrow(new ServiceException(Status.USER_NO_WRITE_PROJECT_PERM)).when(projectService)
                .checkHasProjectWritePermissionThrowException(user, project);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.deleteWorkflowDefinitionByCode(user, 6L));
        Assertions.assertEquals(Status.USER_NO_WRITE_PROJECT_PERM.getCode(), ((ServiceException) exception).getCode());

        // project check auth success, instance not exist
        doNothing().when(projectService).checkHasProjectWritePermissionThrowException(user, project);
        when(workflowDefinitionDao.queryByCode(1L)).thenReturn(Optional.empty());
        exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.deleteWorkflowDefinitionByCode(user, 1L));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(),
                ((ServiceException) exception).getCode());

        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        // user no auth
        when(workflowDefinitionDao.queryByCode(46L)).thenReturn(Optional.of(workflowDefinition));
        exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.deleteWorkflowDefinitionByCode(user, 46L));
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), ((ServiceException) exception).getCode());

        // process definition online
        user.setUserType(UserType.ADMIN_USER);
        workflowDefinition.setReleaseState(ReleaseState.ONLINE);
        when(workflowDefinitionDao.queryByCode(46L)).thenReturn(Optional.of(workflowDefinition));
        exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.deleteWorkflowDefinitionByCode(user, 46L));
        Assertions.assertEquals(Status.WORKFLOW_DEFINE_STATE_ONLINE.getCode(),
                ((ServiceException) exception).getCode());

        // scheduler list elements > 1
        workflowDefinition.setReleaseState(ReleaseState.OFFLINE);
        when(workflowDefinitionDao.queryByCode(46L)).thenReturn(Optional.of(workflowDefinition));
        when(scheduleDao.queryByWorkflowDefinitionCode(46L)).thenReturn(getSchedule());
        when(scheduleDao.deleteById(46)).thenReturn(true);
        when(workflowLineageService.taskDependentMsg(user, project.getCode(), workflowDefinition.getCode(), 0))
                .thenReturn(Optional.empty());
        when(workflowLineageService.deleteWorkflowLineage(anyList())).thenReturn(1);
        workflowDefinitionService.deleteWorkflowDefinitionByCode(user, 46L);

        // scheduler online
        Schedule schedule = getSchedule();
        schedule.setReleaseState(ReleaseState.ONLINE);
        when(scheduleDao.queryByWorkflowDefinitionCode(46L)).thenReturn(schedule);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.deleteWorkflowDefinitionByCode(user, 46L));
        Assertions.assertEquals(Status.SCHEDULE_STATE_ONLINE.getCode(), ((ServiceException) exception).getCode());

        // process used by other task, sub process
        user.setUserType(UserType.ADMIN_USER);
        TaskMainInfo taskMainInfo = getTaskMainInfo().get(0);
        when(workflowLineageService.taskDependentMsg(user, project.getCode(), workflowDefinition.getCode(), 0))
                .thenReturn(Optional.of(taskMainInfo.getTaskName()));
        exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.deleteWorkflowDefinitionByCode(user, 46L));

        // delete success
        schedule.setReleaseState(ReleaseState.OFFLINE);
        when(scheduleDao.queryByWorkflowDefinitionCode(46L)).thenReturn(getSchedule());
        when(scheduleDao.deleteById(schedule.getId())).thenReturn(true);
        when(workflowLineageService.taskDependentMsg(user, project.getCode(), workflowDefinition.getCode(), 0))
                .thenReturn(Optional.empty());
        when(workflowLineageService.deleteWorkflowLineage(anyList())).thenReturn(1);
        Assertions.assertDoesNotThrow(() -> workflowDefinitionService.deleteWorkflowDefinitionByCode(user, 46L));

        // delete success with no lineage (deleteWorkflowLineageResult == 0)
        // This tests the new logic that handles idempotent deletion gracefully
        when(workflowLineageService.deleteWorkflowLineage(anyList())).thenReturn(0);
        Assertions.assertDoesNotThrow(() -> workflowDefinitionService.deleteWorkflowDefinitionByCode(user, 46L));
        Mockito.verify(workflowLineageService, times(3)).deleteWorkflowLineage(anyList());
    }

    @Test
    public void batchDeleteWorkflowDefinitionByCodeTest() {
        when(projectDao.queryByCode(projectCode)).thenReturn(getProject(projectCode));

        Project project = getProject(projectCode);

        // process check exists
        final String twoCodes = "11,12";
        Set<Long> definitionCodes = Lists.newArrayList(twoCodes.split(Constants.COMMA)).stream()
                .map(Long::parseLong).collect(Collectors.toSet());
        WorkflowDefinition process = getWorkflowDefinition();
        List<WorkflowDefinition> workflowDefinitionList = new ArrayList<>();
        workflowDefinitionList.add(process);
        when(workflowDefinitionDao.queryByCodes(definitionCodes)).thenReturn(workflowDefinitionList);
        Throwable exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.batchDeleteWorkflowDefinitionByCodes(user, projectCode, twoCodes));
        String formatter = MessageFormat.format(Status.BATCH_DELETE_WORKFLOW_DEFINE_BY_CODES_ERROR.getMsg(),
                "12[workflow definition not exist]");
        Assertions.assertEquals(formatter, exception.getMessage());

        // return the right data
        Map<String, Object> result = new HashMap<>();
        final String singleCodes = "11";
        definitionCodes = Lists.newArrayList(singleCodes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        when(workflowDefinitionDao.queryByCodes(definitionCodes)).thenReturn(workflowDefinitionList);
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(process));

        // process definition online
        user.setUserType(UserType.ADMIN_USER);
        putMsg(result, Status.SUCCESS, projectCode);
        process.setReleaseState(ReleaseState.ONLINE);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.batchDeleteWorkflowDefinitionByCodes(user, projectCode, singleCodes));
        String subFormatter =
                MessageFormat.format(Status.WORKFLOW_DEFINE_STATE_ONLINE.getMsg(), process.getName());
        formatter =
                MessageFormat.format(Status.DELETE_WORKFLOW_DEFINE_ERROR.getMsg(), process.getName(), subFormatter);
        Assertions.assertEquals(formatter, exception.getMessage());

        // delete success
        process.setReleaseState(ReleaseState.OFFLINE);
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(process));
        when(workflowLineageService.taskDependentMsg(user, project.getCode(), process.getCode(), 0))
                .thenReturn(Optional.empty());
        Assertions.assertDoesNotThrow(
                () -> workflowDefinitionService.batchDeleteWorkflowDefinitionByCodes(user, projectCode, singleCodes));
    }

    @Test
    public void testVerifyWorkflowDefinitionName() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);

        // project check auth fail
        Mockito.doThrow(new ServiceException(Status.PROJECT_NOT_FOUND))
                .when(projectService).checkProjectAndAuthThrowException(user, project, WORKFLOW_CREATE);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.verifyWorkflowDefinitionName(user, projectCode, "test_pdf", 0));
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), ex.getCode());

        // project check auth success, process not exist
        Mockito.doNothing().when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_CREATE);
        when(workflowDefinitionDao.verifyByDefineName(project.getCode(), "test_pdf")).thenReturn(null);
        Assertions.assertDoesNotThrow(
                () -> workflowDefinitionService.verifyWorkflowDefinitionName(user, projectCode, "test_pdf", 0));

        // process exist
        when(workflowDefinitionDao.verifyByDefineName(project.getCode(), "test_pdf"))
                .thenReturn(getWorkflowDefinition());
        ServiceException existsEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.verifyWorkflowDefinitionName(user, projectCode, "test_pdf", 0));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NAME_EXIST.getCode(), existsEx.getCode());
    }

    @Test
    public void testCheckWorkflowNodeList() {
        ServiceException nullJsonEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.checkWorkflowNodeList(null, null));
        Assertions.assertEquals(Status.DATA_IS_NOT_VALID.getCode(), nullJsonEx.getCode());

        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);

        ServiceException emptyDagEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.checkWorkflowNodeList(taskRelationJson, taskDefinitionLogs));
        Assertions.assertEquals(Status.WORKFLOW_DAG_IS_EMPTY.getCode(), emptyDagEx.getCode());
    }

    @Test
    public void testTransformTaskShouldRejectMissingPreTaskDefinition() {
        // relation references preTaskCode 123451235 which has no task definition; the shared
        // transformation must fail explicitly instead of silently dropping the dependency
        String taskRelationJsonWithUnknownPreTaskCode =
                "[{\"name\":\"\",\"preTaskCode\":123451235,\"preTaskVersion\":1,\"postTaskCode\":123456789,"
                        + "\"postTaskVersion\":1,\"conditionType\":\"NONE\",\"conditionParams\":\"{}\"}]";
        List<WorkflowTaskRelation> taskRelations =
                JSONUtils.toList(taskRelationJsonWithUnknownPreTaskCode, WorkflowTaskRelation.class);
        List<TaskDefinitionLog> taskDefinitionLogs =
                JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);

        org.apache.dolphinscheduler.service.exceptions.ServiceException exception =
                Assertions.assertThrows(org.apache.dolphinscheduler.service.exceptions.ServiceException.class,
                        () -> new ProcessServiceImpl().transformTask(taskRelations, taskDefinitionLogs));
        Assertions.assertTrue(exception.getMessage().contains("123451235"));
    }

    @Test
    public void testCheckWorkflowNodeListShouldReportMissingTaskCode() {
        // the workflow-instance update path goes through checkWorkflowNodeList -> transformTask,
        // the missing task code must be reported as an invalid dag naming the missing definitions
        when(processService.transformTask(anyList(), anyList())).thenThrow(
                new org.apache.dolphinscheduler.service.exceptions.ServiceException(
                        "The task definitions of pre taskCodes: [123451235] do not exist"));
        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskDefinitionJson, TaskDefinitionLog.class);

        String taskRelationJsonWithUnknownPreTaskCode =
                "[{\"name\":\"\",\"preTaskCode\":123451235,\"preTaskVersion\":1,\"postTaskCode\":123456789,"
                        + "\"postTaskVersion\":1,\"conditionType\":\"NONE\",\"conditionParams\":\"{}\"}]";
        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.checkWorkflowNodeList(taskRelationJsonWithUnknownPreTaskCode,
                        taskDefinitionLogs));
        Assertions.assertEquals(Status.WORKFLOW_DAG_IS_INVALID.getCode(), exception.getCode());
        Assertions.assertTrue(exception.getMessage().contains("123451235"));
    }

    @Test
    public void testGetTaskNodeListByDefinitionCode() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, project, null);

        // process definition not exist
        when(workflowDefinitionDao.queryByCode(46L)).thenReturn(Optional.empty());
        ServiceException notFoundEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.getTaskNodeListByDefinitionCode(user, projectCode, 46L));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(), notFoundEx.getCode());

        // success
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        when(processService.genDagData(any()))
                .thenReturn(new DagData(workflowDefinition, null, Collections.emptyList()));
        when(workflowDefinitionDao.queryByCode(46L)).thenReturn(Optional.of(workflowDefinition));
        Assertions.assertNotNull(
                workflowDefinitionService.getTaskNodeListByDefinitionCode(user, projectCode, 46L));
    }

    @Test
    public void testGetTaskNodeListByDefinitionCodes() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkProjectAndAuthThrowException(user, project, null);

        // process definition not exist
        String defineCodes = "46";
        Set<Long> defineCodeSet = Lists.newArrayList(defineCodes.split(Constants.COMMA)).stream().map(Long::parseLong)
                .collect(Collectors.toSet());
        when(workflowDefinitionDao.queryByCodes(defineCodeSet)).thenReturn(null);
        ServiceException notExistEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.getNodeListMapByDefinitionCodes(user, projectCode, defineCodes));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(), notExistEx.getCode());

        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        List<WorkflowDefinition> workflowDefinitionList = new ArrayList<>();
        workflowDefinitionList.add(workflowDefinition);

        when(workflowDefinitionDao.queryByCodes(defineCodeSet)).thenReturn(workflowDefinitionList);
        when(processService.genDagData(any())).thenReturn(new DagData(workflowDefinition, null, null));
        Project project1 = getProject(projectCode);
        List<Project> projects = new ArrayList<>();
        projects.add(project1);
        when(projectDao.queryProjectCreatedAndAuthorizedByUserId(user.getId())).thenReturn(projects);

        Assertions.assertNotNull(
                workflowDefinitionService.getNodeListMapByDefinitionCodes(user, projectCode, defineCodes));
    }

    @Test
    public void testQueryAllWorkflowDefinitionByProjectCode() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService)
                .checkProjectAndAuthThrowException(user, project, WORKFLOW_DEFINITION);
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        List<WorkflowDefinition> workflowDefinitionList = new ArrayList<>();
        workflowDefinitionList.add(workflowDefinition);
        when(workflowDefinitionDao.queryAllDefinitionList(projectCode)).thenReturn(workflowDefinitionList);
        Assertions.assertNotNull(
                workflowDefinitionService.queryAllWorkflowDefinitionByProjectCode(user, projectCode));
    }

    @Test
    public void testViewTree() {
        Project project1 = getProject(projectCode);
        when(projectDao.queryByCode(1L)).thenReturn(project1);
        Mockito.doNothing().when(projectService)
                .checkProjectAndAuthThrowException(user, project1, WORKFLOW_TREE_VIEW);
        // process definition not exist
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        ServiceException notFoundEx = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.viewTree(user, workflowDefinition.getProjectCode(), 46, 10));
        Assertions.assertEquals(Status.WORKFLOW_DEFINITION_NOT_EXIST.getCode(), notFoundEx.getCode());

        // task instance not existproject
        when(workflowDefinitionDao.queryByCode(46L)).thenReturn(Optional.of(workflowDefinition));
        when(processService.genDagGraph(workflowDefinition)).thenReturn(new DAG<>());
        Assertions.assertNotNull(
                workflowDefinitionService.viewTree(user, workflowDefinition.getProjectCode(), 46, 10));

        // task instance exist
        Assertions.assertNotNull(
                workflowDefinitionService.viewTree(user, workflowDefinition.getProjectCode(), 46, 10));
    }

    @Test
    public void testSubProcessViewTree() {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        when(workflowDefinitionDao.queryByCode(46L)).thenReturn(Optional.of(workflowDefinition));

        Project project1 = getProject(1);
        when(projectDao.queryByCode(1L)).thenReturn(project1);
        Mockito.doNothing().when(projectService)
                .checkProjectAndAuthThrowException(user, project1, WORKFLOW_TREE_VIEW);
        when(processService.genDagGraph(workflowDefinition)).thenReturn(new DAG<>());
        Assertions.assertNotNull(
                workflowDefinitionService.viewTree(user, workflowDefinition.getProjectCode(), 46, 10));
    }

    @Test
    public void testUpdateWorkflowDefinition() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        Mockito.doNothing().when(projectService).checkHasProjectWritePermissionThrowException(user, project);

        try {
            workflowDefinitionService.updateWorkflowDefinition(user, projectCode, "test", 1,
                    "", "", "", 0, null, "", WorkflowExecutionTypeEnum.PARALLEL);
            Assertions.fail();
        } catch (ServiceException ex) {
            Assertions.assertEquals(Status.DATA_IS_NOT_VALID.getCode(), ex.getCode());
        }
    }

    @Test
    public void testCreateWorkflowDefinitionShouldSyncVersionToResponse() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkHasProjectWritePermissionThrowException(eq(user), eq(project));
        when(workflowDefinitionDao.verifyByDefineName(projectCode, name)).thenReturn(null);
        when(processService.transformTask(anyList(), anyList())).thenReturn(getTaskNodeList());
        when(processService.saveTaskDefine(eq(user), eq(projectCode), anyList(), eq(Boolean.TRUE))).thenReturn(1);
        when(processService.saveWorkflowDefine(any(User.class), any(WorkflowDefinition.class), eq(Boolean.TRUE),
                eq(Boolean.TRUE))).thenReturn(1);
        when(processService.saveTaskRelation(eq(user), eq(projectCode), anyLong(), eq(1), anyList(), anyList(),
                eq(Boolean.TRUE))).thenReturn(Constants.EXIT_CODE_SUCCESS);

        WorkflowDefinition workflowDefinition = workflowDefinitionService.createWorkflowDefinition(
                user, projectCode, name, description, "[]", "[]", timeout,
                taskRelationJson, taskDefinitionJson, null, WorkflowExecutionTypeEnum.PARALLEL);

        Assertions.assertNotNull(workflowDefinition);
        Assertions.assertEquals(1, workflowDefinition.getVersion());
    }

    @Test
    public void testCreateWorkflowDefinitionPersistsPlaintextSensitiveGlobalParams() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkHasProjectWritePermissionThrowException(eq(user), eq(project));
        when(workflowDefinitionDao.verifyByDefineName(projectCode, name)).thenReturn(null);
        when(processService.transformTask(anyList(), anyList())).thenReturn(getTaskNodeList());
        when(processService.saveTaskDefine(eq(user), eq(projectCode), anyList(), eq(Boolean.TRUE))).thenReturn(1);
        when(processService.saveTaskRelation(eq(user), eq(projectCode), anyLong(), eq(1), anyList(), anyList(),
                eq(Boolean.TRUE))).thenReturn(Constants.EXIT_CODE_SUCCESS);

        ArgumentCaptor<WorkflowDefinition> persisted = ArgumentCaptor.forClass(WorkflowDefinition.class);
        when(processService.saveWorkflowDefine(any(User.class), persisted.capture(), eq(Boolean.TRUE),
                eq(Boolean.TRUE))).thenReturn(1);

        String sensitiveGlobalParams =
                "[{\"prop\":\"pwd\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"Secret123\",\"sensitive\":true}]";
        WorkflowDefinition created = workflowDefinitionService.createWorkflowDefinition(
                user, projectCode, name, description, sensitiveGlobalParams, "[]", timeout,
                taskRelationJson, taskDefinitionJson, null, WorkflowExecutionTypeEnum.PARALLEL);

        Assertions.assertTrue(created.getGlobalParams().contains("Secret123"));
        Assertions.assertTrue(persisted.getValue().getGlobalParams().contains("Secret123"));
        Assertions.assertSame(created, persisted.getValue());
    }

    @Test
    public void testCreateWorkflowDefinitionRejectsSensitivePlaceholder() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkHasProjectWritePermissionThrowException(eq(user), eq(project));
        when(workflowDefinitionDao.verifyByDefineName(projectCode, name)).thenReturn(null);

        String placeholderGlobalParams =
                "[{\"prop\":\"pwd\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"******\",\"sensitive\":true}]";
        Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.createWorkflowDefinition(
                        user, projectCode, name, description, placeholderGlobalParams, "[]", timeout,
                        taskRelationJson, taskDefinitionJson, null, WorkflowExecutionTypeEnum.PARALLEL));
        Mockito.verify(processService, Mockito.never())
                .saveWorkflowDefine(any(User.class), any(WorkflowDefinition.class), eq(Boolean.TRUE), eq(Boolean.TRUE));
    }

    @Test
    public void testCreateWorkflowDefinitionShouldRejectUnauthorizedDatasource() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkHasProjectWritePermissionThrowException(eq(user), eq(project));
        when(workflowDefinitionDao.verifyByDefineName(projectCode, name)).thenReturn(null);
        when(processService.transformTask(anyList(), anyList())).thenReturn(getTaskNodeList());
        doThrow(new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION))
                .when(taskDatasourcePermissionChecker).checkPermission(eq(user), anyList());

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.createWorkflowDefinition(
                        user, projectCode, name, description, "[]", "[]", timeout,
                        taskRelationJson, taskDefinitionJson, null, WorkflowExecutionTypeEnum.PARALLEL));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verify(processService, Mockito.never())
                .saveTaskDefine(eq(user), eq(projectCode), anyList(), eq(Boolean.TRUE));
    }

    @Test
    public void testCreateWorkflowDefinitionShouldRejectUnknownPreTaskCode() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkHasProjectWritePermissionThrowException(eq(user), eq(project));
        when(workflowDefinitionDao.verifyByDefineName(projectCode, name)).thenReturn(null);
        // mimic the real transformTask: one node for the (existing) post task of the single relation;
        // lenient since with the fix the validation fails fast before transformTask is even called
        TaskNode taskNode = new TaskNode();
        taskNode.setCode(123456789L);
        taskNode.setPreTasks(JSONUtils.toJsonString(Collections.emptyList()));
        lenient().when(processService.transformTask(anyList(), anyList()))
                .thenReturn(Collections.singletonList(taskNode));

        // preTaskCode 123451235 is referenced by the relation but missing in taskDefinitionJson,
        // this used to end up as a NullPointerException in transformTask and was returned to the client
        // as a generic REQUEST_PARAMS_NOT_VALID_ERROR
        String taskRelationJsonWithUnknownPreTaskCode =
                "[{\"name\":\"\",\"preTaskCode\":123451235,\"preTaskVersion\":1,\"postTaskCode\":123456789,"
                        + "\"postTaskVersion\":1,\"conditionType\":0,\"conditionParams\":\"{}\"}]";

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.createWorkflowDefinition(
                        user, projectCode, name, description, "[]", "[]", timeout,
                        taskRelationJsonWithUnknownPreTaskCode, taskDefinitionJson, null,
                        WorkflowExecutionTypeEnum.PARALLEL));

        Assertions.assertEquals(Status.WORKFLOW_DAG_IS_INVALID.getCode(), exception.getCode());
        Mockito.verify(processService, Mockito.never())
                .saveTaskDefine(eq(user), eq(projectCode), anyList(), eq(Boolean.TRUE));
    }

    @Test
    public void testCreateWorkflowDefinitionShouldRejectUnavailableSubWorkflow() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkHasProjectWritePermissionThrowException(eq(user), eq(project));
        when(workflowDefinitionDao.verifyByDefineName(projectCode, name)).thenReturn(null);
        when(processService.transformTask(anyList(), anyList())).thenReturn(getTaskNodeList());
        doThrow(new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION))
                .when(taskSubWorkflowPermissionChecker).checkPermission(eq(user), anyList());

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.createWorkflowDefinition(
                        user, projectCode, name, description, "[]", "[]", timeout,
                        taskRelationJson, taskDefinitionJson, null, WorkflowExecutionTypeEnum.PARALLEL));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verify(processService, Mockito.never())
                .saveTaskDefine(eq(user), eq(projectCode), anyList(), eq(Boolean.TRUE));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L})
    public void testCopyPreservesProjectOwnership(long targetProjectCode) {
        ReflectionTestUtils.setField(workflowDefinitionService, "taskProjectValidator",
                new TaskProjectValidator(taskDefinitionDao, taskDefinitionLogMapper));
        ReflectionTestUtils.setField(workflowDefinitionService, "workflowRelationValidator",
                new WorkflowRelationValidator(new TaskProjectValidator(taskDefinitionDao, taskDefinitionLogMapper)));
        when(projectDao.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        if (targetProjectCode != projectCode) {
            when(projectDao.queryByCode(targetProjectCode)).thenReturn(getProject(targetProjectCode));
        }
        WorkflowDefinition definition = getWorkflowDefinition();
        when(workflowDefinitionDao.queryByCodes(Collections.singleton(processDefinitionCode)))
                .thenReturn(Collections.singletonList(definition));
        WorkflowTaskRelation relation = getWorkflowTaskRelation(1, 1, projectCode, processDefinitionCode,
                0, 0, 101L, 1);
        List<WorkflowTaskRelation> relations = Collections.singletonList(relation);
        when(workflowTaskRelationDao.queryByWorkflowDefinitionCode(processDefinitionCode)).thenReturn(relations);
        TaskDefinitionLog task = new TaskDefinitionLog();
        task.setCode(101L);
        task.setProjectCode(projectCode);
        task.setVersion(1);
        task.setTaskType("SHELL");
        when(taskDefinitionLogDao.queryTaskDefineLogList(relations)).thenReturn(Collections.singletonList(task));
        when(processService.saveWorkflowDefine(eq(user), any(), eq(true), eq(true))).thenReturn(1);

        List<WorkflowDefinitionCopyVO> copies = workflowDefinitionService.batchCopyWorkflowDefinition(user,
                projectCode, String.valueOf(processDefinitionCode), targetProjectCode);

        Assertions.assertEquals(1, copies.size());
        Assertions.assertNotEquals(processDefinitionCode, copies.get(0).getWorkflowDefinitionCode());
        Assertions.assertNotEquals(101L, task.getCode());
        Assertions.assertEquals(targetProjectCode, task.getProjectCode());
        verify(processService).saveTaskRelation(eq(user), eq(targetProjectCode),
                eq(copies.get(0).getWorkflowDefinitionCode()), eq(1), Mockito.argThat(logs -> logs.stream()
                        .allMatch(log -> log.getProjectCode() == targetProjectCode
                                && log.getWorkflowDefinitionCode() == copies.get(0).getWorkflowDefinitionCode())),
                anyList(), eq(true));
        Assertions.assertEquals(projectCode, relation.getProjectCode());
        Assertions.assertEquals(processDefinitionCode, relation.getWorkflowDefinitionCode());
    }

    @Test
    public void testCreateRejectsPersistedForeignTaskBeforeSaving() {
        ReflectionTestUtils.setField(workflowDefinitionService, "taskProjectValidator",
                new TaskProjectValidator(taskDefinitionDao, taskDefinitionLogMapper));
        ReflectionTestUtils.setField(workflowDefinitionService, "workflowRelationValidator",
                new WorkflowRelationValidator(new TaskProjectValidator(taskDefinitionDao, taskDefinitionLogMapper)));
        when(projectDao.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        when(processService.transformTask(anyList(), anyList())).thenReturn(getTaskNodeList());
        TaskDefinitionLog foreignTask = new TaskDefinitionLog();
        foreignTask.setProjectCode(projectCodeOther);
        when(taskDefinitionDao.queryByCodes(Mockito.anySet())).thenReturn(Collections.singletonList(foreignTask));

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.createWorkflowDefinition(user, projectCode, name, description,
                        "[]", "[]", timeout, taskRelationJson, taskDefinitionJson, null,
                        WorkflowExecutionTypeEnum.PARALLEL));

        Assertions.assertEquals(Status.PROJECT_WORKFLOW_NOT_MATCH.getCode(), error.getCode());
        verify(processService, Mockito.never()).saveTaskDefine(any(), anyLong(), anyList(), any());
        verify(processService, Mockito.never()).saveWorkflowDefine(any(), any(), any(), any());
    }

    @Test
    public void testSwitchWorkflowDefinitionVersionRejectsForeignCurrentRelationBeforeSaving() {
        ReflectionTestUtils.setField(workflowDefinitionService, "workflowRelationValidator",
                new WorkflowRelationValidator(new TaskProjectValidator(taskDefinitionDao, taskDefinitionLogMapper)));
        WorkflowDefinition definition = getWorkflowDefinition();
        WorkflowDefinitionLog version = new WorkflowDefinitionLog(definition);
        version.setVersion(1);
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(definition));
        when(workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(version);
        WorkflowTaskRelation targetRelation = getWorkflowTaskRelation(1, 1, projectCode, processDefinitionCode,
                0, 0, 101L, 1);
        Mockito.lenient().when(workflowTaskRelationLogMapper.queryByWorkflowCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(Collections.singletonList(new WorkflowTaskRelationLog(targetRelation)));
        WorkflowTaskRelation currentRelation = new WorkflowTaskRelation(new WorkflowTaskRelationLog(targetRelation));
        currentRelation.setProjectCode(projectCodeOther);
        when(workflowTaskRelationDao.queryByWorkflowDefinitionCode(processDefinitionCode))
                .thenReturn(Collections.singletonList(currentRelation));

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.switchWorkflowDefinitionVersion(user, projectCode,
                        processDefinitionCode, 1));

        Assertions.assertEquals(Status.PROJECT_WORKFLOW_NOT_MATCH.getCode(), error.getCode());
        verify(processService, Mockito.never()).switchVersion(any(), any());
        verify(schedulerService, Mockito.never()).offlineSchedulerByWorkflowCode(anyLong());
    }

    @Test
    public void testUpdateWorkflowDefinitionRejectsForeignCurrentRelationBeforeSaving() {
        ReflectionTestUtils.setField(workflowDefinitionService, "workflowRelationValidator",
                new WorkflowRelationValidator(new TaskProjectValidator(taskDefinitionDao, taskDefinitionLogMapper)));
        WorkflowDefinition definition = getWorkflowDefinition();
        when(projectDao.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(definition));
        when(processService.transformTask(anyList(), anyList())).thenReturn(getTaskNodeList());
        WorkflowTaskRelation currentRelation = getWorkflowTaskRelation(1, 1, projectCodeOther, processDefinitionCode,
                0, 0, 101L, 1);
        when(workflowTaskRelationDao.queryByWorkflowDefinitionCode(processDefinitionCode))
                .thenReturn(Collections.singletonList(currentRelation));

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.updateWorkflowDefinition(user, projectCode, definition.getName(),
                        processDefinitionCode, description, "[]", "[]", timeout, taskRelationJson,
                        taskDefinitionJson, WorkflowExecutionTypeEnum.PARALLEL));

        Assertions.assertEquals(Status.PROJECT_WORKFLOW_NOT_MATCH.getCode(), error.getCode());
        verify(processService, Mockito.never()).saveTaskDefine(any(), anyLong(), anyList(), any());
        verify(processService, Mockito.never()).saveWorkflowDefine(any(), any(), any(), any());
        verify(processService, Mockito.never()).saveTaskRelation(any(), anyLong(), anyLong(), Mockito.anyInt(),
                anyList(), anyList(), any());
    }

    @Test
    public void testDeleteUnreferencedWorkflowDefinitionVersion() {
        WorkflowDefinition definition = getWorkflowDefinition();
        definition.setVersion(2);
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(definition));
        when(workflowDefinitionLogMapper.deleteByWorkflowDefinitionCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(1);
        when(workflowTaskRelationLogMapper.deleteByCode(processDefinitionCode, 1)).thenReturn(1);

        workflowDefinitionService.deleteWorkflowDefinitionVersion(user, projectCode, processDefinitionCode, 1);

        verify(workflowDefinitionLogMapper).deleteByWorkflowDefinitionCodeAndVersion(processDefinitionCode, 1);
        verify(workflowTaskRelationLogMapper).deleteByCode(processDefinitionCode, 1);
    }

    @Test
    public void testSwitchWorkflowDefinitionVersionRejectsForeignProject() {
        WorkflowDefinition definition = getWorkflowDefinition();
        WorkflowDefinitionLog version = new WorkflowDefinitionLog(definition);
        version.setProjectCode(projectCodeOther);
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(definition));
        when(workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(version);
        Mockito.lenient().when(processService.switchVersion(any(), any())).thenReturn(1);

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.switchWorkflowDefinitionVersion(user, projectCode,
                        processDefinitionCode, 1));

        Assertions.assertEquals(Status.PROJECT_WORKFLOW_NOT_MATCH.getCode(), error.getCode());
        verify(processService, Mockito.never()).switchVersion(any(), any());
    }

    @ParameterizedTest
    @ValueSource(longs = {101L, 102L})
    public void testSwitchWorkflowDefinitionVersionRejectsMissingTaskVersionBeforeSaving(long availableTaskCode) {
        WorkflowDefinition currentDefinition = getWorkflowDefinition();
        currentDefinition.setVersion(3);
        WorkflowDefinitionLog targetVersion = new WorkflowDefinitionLog(currentDefinition);
        targetVersion.setVersion(1);
        WorkflowTaskRelation currentRelation = getWorkflowTaskRelation(1, 3, projectCode, processDefinitionCode,
                0, 0, 101L, 2);
        List<WorkflowTaskRelation> currentRelations = Collections.singletonList(currentRelation);
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(currentDefinition));
        when(workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(targetVersion);
        when(workflowTaskRelationDao.queryByWorkflowDefinitionCode(processDefinitionCode)).thenReturn(currentRelations);
        List<WorkflowTaskRelationLog> targetRelations = Arrays.asList(
                new WorkflowTaskRelationLog(getWorkflowTaskRelation(1, 1, projectCode, processDefinitionCode,
                        0, 0, 101L, 1)),
                new WorkflowTaskRelationLog(getWorkflowTaskRelation(2, 1, projectCode, processDefinitionCode,
                        101L, 1, 102L, 1)));
        when(workflowTaskRelationLogMapper.queryByWorkflowCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(targetRelations);
        TaskDefinitionLog availableTask = new TaskDefinitionLog();
        availableTask.setCode(availableTaskCode);
        availableTask.setVersion(1);
        availableTask.setProjectCode(projectCode);
        availableTask.setTaskType("SHELL");
        when(taskDefinitionLogMapper.queryByTaskDefinitions(anyList()))
                .thenReturn(Collections.singletonList(availableTask));
        Mockito.lenient().when(processService.switchVersion(currentDefinition, targetVersion)).thenReturn(1);

        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.switchWorkflowDefinitionVersion(user, projectCode,
                        processDefinitionCode, 1));

        Assertions.assertEquals(Status.TASK_DEFINE_NOT_EXIST.getCode(), error.getCode());
        long missingTaskCode = availableTaskCode == 101L ? 102L : 101L;
        Assertions.assertTrue(error.getMessage().contains(missingTaskCode + " (version 1)"));
        Assertions.assertEquals(3, currentDefinition.getVersion());
        Assertions.assertEquals(3, currentRelation.getWorkflowDefinitionVersion());
        Assertions.assertEquals(2, currentRelation.getPostTaskVersion());
        verify(processService, Mockito.never()).switchVersion(any(), any());
        verify(workflowDefinitionDao, Mockito.never()).updateById(any());
        verify(workflowTaskRelationDao, Mockito.never()).batchInsert(anyList());
        verify(workflowTaskRelationDao, Mockito.never())
                .deleteByWorkflowDefinitionCodeAndVersion(anyLong(), Mockito.anyInt());
        Mockito.verifyNoInteractions(schedulerService, scheduleDao, workflowLineageService);
    }

    @Test
    public void testSwitchWorkflowDefinitionVersionShouldRejectUnauthorizedDatasource() {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        WorkflowDefinitionLog workflowDefinitionLog = new WorkflowDefinitionLog();
        workflowDefinitionLog.setCode(processDefinitionCode);
        workflowDefinitionLog.setProjectCode(projectCode);
        workflowDefinitionLog.setVersion(1);
        WorkflowTaskRelation workflowTaskRelation =
                getWorkflowTaskRelation(1, 1, projectCode, processDefinitionCode, 0, 0, 123456789L, 1);

        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(workflowDefinition));
        when(workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(workflowDefinitionLog);
        when(workflowTaskRelationLogMapper.queryByWorkflowCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(Collections.singletonList(new WorkflowTaskRelationLog(workflowTaskRelation)));
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
        taskDefinitionLog.setCode(workflowTaskRelation.getPostTaskCode());
        taskDefinitionLog.setVersion(workflowTaskRelation.getPostTaskVersion());
        when(taskDefinitionLogMapper.queryByTaskDefinitions(anyList()))
                .thenReturn(Collections.singletonList(taskDefinitionLog));
        doThrow(new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION))
                .when(taskDatasourcePermissionChecker).checkPermission(eq(user), anyList());

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.switchWorkflowDefinitionVersion(
                        user, projectCode, processDefinitionCode, 1));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        verify(workflowTaskRelationLogMapper).queryByWorkflowCodeAndVersion(processDefinitionCode, 1);
        verify(workflowTaskRelationDao, Mockito.never())
                .queryWorkflowTaskRelationsByWorkflowDefinitionCode(anyLong(), Mockito.anyInt());
        verify(taskDefinitionLogMapper)
                .queryByTaskDefinitions(Mockito.argThat(taskDefinitions -> taskDefinitions.size() == 1
                        && taskDefinitions.iterator().next().getCode() == workflowTaskRelation.getPostTaskCode()
                        && taskDefinitions.iterator().next().getVersion() == workflowTaskRelation
                                .getPostTaskVersion()));
        Mockito.verify(processService, Mockito.never())
                .switchVersion(any(WorkflowDefinition.class), any(WorkflowDefinitionLog.class));
    }

    @Test
    public void testSwitchWorkflowDefinitionVersionShouldNotQueryAllTasksWhenTargetVersionHasNoRelations() {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        WorkflowDefinitionLog workflowDefinitionLog = new WorkflowDefinitionLog();
        workflowDefinitionLog.setCode(processDefinitionCode);
        workflowDefinitionLog.setProjectCode(projectCode);
        workflowDefinitionLog.setVersion(1);

        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(workflowDefinition));
        when(workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(workflowDefinitionLog);
        when(workflowTaskRelationLogMapper.queryByWorkflowCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(Collections.emptyList());

        Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.switchWorkflowDefinitionVersion(
                        user, projectCode, processDefinitionCode, 1));

        verify(taskDefinitionLogMapper, Mockito.never()).queryByTaskDefinitions(anyList());
        verify(processService).switchVersion(workflowDefinition, workflowDefinitionLog);
    }

    @Test
    public void testSwitchWorkflowDefinitionVersionShouldRejectUnavailableSubWorkflow() {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        WorkflowDefinitionLog workflowDefinitionLog = new WorkflowDefinitionLog();
        workflowDefinitionLog.setCode(processDefinitionCode);
        workflowDefinitionLog.setProjectCode(projectCode);
        workflowDefinitionLog.setVersion(1);
        WorkflowTaskRelation workflowTaskRelation =
                getWorkflowTaskRelation(1, 1, projectCode, processDefinitionCode, 0, 0, 123456789L, 1);

        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(workflowDefinition));
        when(workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(workflowDefinitionLog);
        when(workflowTaskRelationLogMapper.queryByWorkflowCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(Collections.singletonList(new WorkflowTaskRelationLog(workflowTaskRelation)));
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
        taskDefinitionLog.setCode(workflowTaskRelation.getPostTaskCode());
        taskDefinitionLog.setVersion(workflowTaskRelation.getPostTaskVersion());
        when(taskDefinitionLogMapper.queryByTaskDefinitions(anyList()))
                .thenReturn(Collections.singletonList(taskDefinitionLog));
        doThrow(new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION))
                .when(taskSubWorkflowPermissionChecker).checkPermission(eq(user), anyList());

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.switchWorkflowDefinitionVersion(
                        user, projectCode, processDefinitionCode, 1));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verify(processService, Mockito.never())
                .switchVersion(any(WorkflowDefinition.class), any(WorkflowDefinitionLog.class));
    }

    @Test
    public void testSwitchWorkflowDefinitionVersionShouldOfflineSchedule() {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        WorkflowDefinitionLog workflowDefinitionLog = new WorkflowDefinitionLog();
        workflowDefinitionLog.setCode(processDefinitionCode);
        workflowDefinitionLog.setProjectCode(projectCode);
        workflowDefinitionLog.setVersion(1);
        WorkflowTaskRelation workflowTaskRelation =
                getWorkflowTaskRelation(1, 1, projectCode, processDefinitionCode, 0, 0, 123456789L, 1);
        TaskDefinitionLog taskDefinitionLog = new TaskDefinitionLog();
        taskDefinitionLog.setCode(123456789L);
        taskDefinitionLog.setVersion(1);
        taskDefinitionLog.setTaskType("SHELL");
        TaskDefinitionLog successorTask = new TaskDefinitionLog();
        successorTask.setCode(123451234L);
        successorTask.setVersion(1);
        successorTask.setTaskType("SHELL");
        WorkflowTaskRelation successorRelation =
                getWorkflowTaskRelation(2, 1, projectCode, processDefinitionCode, 123456789L, 1, 123451234L, 1);

        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(workflowDefinition));
        when(workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(workflowDefinitionLog);
        when(workflowTaskRelationLogMapper.queryByWorkflowCodeAndVersion(processDefinitionCode, 1))
                .thenReturn(Arrays.asList(new WorkflowTaskRelationLog(workflowTaskRelation),
                        new WorkflowTaskRelationLog(successorRelation)));
        when(taskDefinitionLogMapper.queryByTaskDefinitions(anyList()))
                .thenReturn(Arrays.asList(successorTask, taskDefinitionLog));
        when(processService.switchVersion(workflowDefinition, workflowDefinitionLog)).thenReturn(1);

        workflowDefinitionService.switchWorkflowDefinitionVersion(user, projectCode, processDefinitionCode, 1);

        verify(taskDefinitionLogMapper).queryByTaskDefinitions(Mockito.argThat(tasks -> tasks.size() == 2
                && tasks.stream().allMatch(task -> task.getVersion() == 1)
                && tasks.stream().map(task -> task.getCode()).collect(Collectors.toSet())
                        .equals(new HashSet<>(Arrays.asList(123456789L, 123451234L)))));
        verify(processService).switchVersion(workflowDefinition, workflowDefinitionLog);
        verify(schedulerService).offlineSchedulerByWorkflowCode(processDefinitionCode);
    }

    @Test
    public void testOnlineWorkflowDefinitionShouldRejectUnauthorizedDatasource() {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        WorkflowTaskRelation workflowTaskRelation =
                getWorkflowTaskRelation(1, 1, projectCode, processDefinitionCode, 0, 0, 123456789L, 1);
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(workflowDefinition));
        when(workflowTaskRelationDao.queryByWorkflowDefinitionCode(processDefinitionCode))
                .thenReturn(Collections.singletonList(workflowTaskRelation));
        when(taskDefinitionLogDao.queryTaskDefineLogList(anyList()))
                .thenReturn(Collections.singletonList(new TaskDefinitionLog()));
        doThrow(new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION))
                .when(taskDatasourcePermissionChecker).checkPermission(eq(user), anyList());

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.onlineWorkflowDefinition(user, projectCode, processDefinitionCode));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verify(workflowDefinitionDao, Mockito.never()).updateById(any(WorkflowDefinition.class));
    }

    @Test
    public void testOnlineWorkflowDefinitionShouldRejectUnavailableSubWorkflow() {
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        WorkflowTaskRelation workflowTaskRelation =
                getWorkflowTaskRelation(1, 1, projectCode, processDefinitionCode, 0, 0, 123456789L, 1);
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(workflowDefinition));
        when(workflowTaskRelationDao.queryByWorkflowDefinitionCode(processDefinitionCode))
                .thenReturn(Collections.singletonList(workflowTaskRelation));
        when(taskDefinitionLogDao.queryTaskDefineLogList(anyList()))
                .thenReturn(Collections.singletonList(new TaskDefinitionLog()));
        doThrow(new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION))
                .when(taskSubWorkflowPermissionChecker).checkPermission(eq(user), anyList());

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.onlineWorkflowDefinition(user, projectCode, processDefinitionCode));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verify(workflowDefinitionDao, Mockito.never()).updateById(any(WorkflowDefinition.class));
    }

    @Test
    public void testUpdateWorkflowDefinitionShouldSyncVersionToResponse() {
        Project project = getProject(projectCode);
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        workflowDefinition.setName("origin-name");
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkHasProjectWritePermissionThrowException(eq(user), eq(project));
        when(processService.transformTask(anyList(), anyList())).thenReturn(getTaskNodeList());
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(workflowDefinition));
        when(workflowDefinitionDao.verifyByDefineName(projectCode, name)).thenReturn(null);
        when(processService.saveTaskDefine(eq(user), eq(projectCode), anyList(), eq(Boolean.TRUE))).thenReturn(1);
        when(processService.saveWorkflowDefine(any(User.class), any(WorkflowDefinition.class), eq(Boolean.TRUE),
                eq(Boolean.TRUE))).thenReturn(2);
        when(workflowTaskRelationDao.queryByWorkflowDefinitionCode(processDefinitionCode))
                .thenReturn(Collections.emptyList());
        when(processService.saveTaskRelation(eq(user), eq(projectCode), eq(processDefinitionCode), eq(2), anyList(),
                anyList(), eq(Boolean.TRUE))).thenReturn(Constants.EXIT_CODE_SUCCESS);

        WorkflowDefinition resultDefinition = workflowDefinitionService.updateWorkflowDefinition(
                user, projectCode, name, processDefinitionCode, description, "[]", "[]", timeout,
                taskRelationJson, taskDefinitionJson, WorkflowExecutionTypeEnum.PARALLEL);

        Assertions.assertNotNull(resultDefinition);
        Assertions.assertEquals(2, resultDefinition.getVersion());
    }

    @Test
    public void testUpdateWorkflowDefinitionMergesSensitiveLocalParamsFromMatchingTaskVersion() {
        Project project = getProject(projectCode);
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        workflowDefinition.setName("origin-name");
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkHasProjectWritePermissionThrowException(eq(user), eq(project));
        when(processService.transformTask(anyList(), anyList())).thenReturn(getTaskNodeList());
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(workflowDefinition));
        when(workflowDefinitionDao.verifyByDefineName(projectCode, name)).thenReturn(null);
        when(processService.saveWorkflowDefine(any(User.class), any(WorkflowDefinition.class), eq(Boolean.TRUE),
                eq(Boolean.TRUE))).thenReturn(2);
        when(workflowTaskRelationDao.queryByWorkflowDefinitionCode(processDefinitionCode))
                .thenReturn(Collections.emptyList());
        when(processService.saveTaskRelation(eq(user), eq(projectCode), eq(processDefinitionCode), eq(2), anyList(),
                anyList(), eq(Boolean.TRUE))).thenReturn(Constants.EXIT_CODE_SUCCESS);

        TaskDefinitionLog version1Log = new TaskDefinitionLog();
        version1Log.setCode(123456789L);
        version1Log.setVersion(1);
        version1Log.setTaskParams(
                "{\"localParams\":[{\"prop\":\"token\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"old-secret\",\"sensitive\":true}],\"rawScript\":\"echo 1\"}");
        when(taskDefinitionLogMapper.queryByTaskDefinitions(anyList()))
                .thenReturn(Collections.singletonList(version1Log));

        String submittedTaskDefinitionJson =
                "[{\"code\":123456789,\"name\":\"test1\",\"version\":1,\"description\":\"\",\"delayTime\":0,\"taskType\":\"SHELL\","
                        + "\"taskParams\":{\"resourceList\":[],\"localParams\":[{\"prop\":\"token\",\"direct\":\"IN\",\"type\":\"VARCHAR\","
                        + "\"value\":\"******\",\"sensitive\":true}],\"rawScript\":\"echo 1\",\"dependence\":{},\"conditionResult\":{\"successNode\":[],\"failedNode\":[]},\"waitStartTimeout\":{},"
                        + "\"switchResult\":{}},\"flag\":\"YES\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":1,\"timeoutFlag\":\"CLOSE\","
                        + "\"timeoutNotifyStrategy\":null,\"timeout\":0,\"environmentCode\":-1},{\"code\":123451234,\"name\":\"test2\",\"version\":1,\"description\":\"\",\"delayTime\":0,\"taskType\":\"SHELL\","
                        + "\"taskParams\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo 2\",\"dependence\":{},\"conditionResult\":{\"successNode\":[],\"failedNode\":[]},\"waitStartTimeout\":{},"
                        + "\"switchResult\":{}},\"flag\":\"YES\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":0,\"failRetryInterval\":1,\"timeoutFlag\":\"CLOSE\","
                        + "\"timeoutNotifyStrategy\":\"WARN\",\"timeout\":0,\"environmentCode\":-1}]";

        ArgumentCaptor<List> savedTaskLogs = ArgumentCaptor.forClass(List.class);
        when(processService.saveTaskDefine(eq(user), eq(projectCode), savedTaskLogs.capture(), eq(Boolean.TRUE)))
                .thenReturn(1);

        workflowDefinitionService.updateWorkflowDefinition(
                user, projectCode, name, processDefinitionCode, description, "[]", "[]", timeout,
                taskRelationJson, submittedTaskDefinitionJson, WorkflowExecutionTypeEnum.PARALLEL);

        TaskDefinitionLog merged = ((List<TaskDefinitionLog>) savedTaskLogs.getValue()).stream()
                .filter(log -> log.getCode() == 123456789L)
                .findFirst()
                .orElseThrow(AssertionError::new);
        Assertions.assertTrue(merged.getTaskParams().contains("old-secret"));
        Assertions.assertFalse(merged.getTaskParams().contains(TaskConstants.SENSITIVE_DATA_MASK));
        verify(taskDefinitionDao, Mockito.never()).queryByCodes(any());
        verify(taskDefinitionLogMapper).queryByTaskDefinitions(anyList());
    }

    @Test
    public void testUpdateWorkflowDefinitionPersistsPlaintextNewSensitiveGlobalParams() {
        Project project = getProject(projectCode);
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        workflowDefinition.setName("origin-name");
        workflowDefinition.setGlobalParams(
                "[{\"prop\":\"pwd\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"old-secret\",\"sensitive\":true}]");
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkHasProjectWritePermissionThrowException(eq(user), eq(project));
        when(processService.transformTask(anyList(), anyList())).thenReturn(getTaskNodeList());
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(workflowDefinition));
        when(workflowDefinitionDao.verifyByDefineName(projectCode, name)).thenReturn(null);
        when(processService.saveTaskDefine(eq(user), eq(projectCode), anyList(), eq(Boolean.TRUE))).thenReturn(1);
        when(workflowTaskRelationDao.queryByWorkflowDefinitionCode(processDefinitionCode))
                .thenReturn(Collections.emptyList());
        when(processService.saveTaskRelation(eq(user), eq(projectCode), eq(processDefinitionCode), eq(2), anyList(),
                anyList(), eq(Boolean.TRUE))).thenReturn(Constants.EXIT_CODE_SUCCESS);

        ArgumentCaptor<WorkflowDefinition> persisted = ArgumentCaptor.forClass(WorkflowDefinition.class);
        when(processService.saveWorkflowDefine(any(User.class), persisted.capture(), eq(Boolean.TRUE),
                eq(Boolean.TRUE))).thenReturn(2);

        String submittedGlobalParams =
                "[{\"prop\":\"pwd\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"new-secret\",\"sensitive\":true}]";
        WorkflowDefinition updated = workflowDefinitionService.updateWorkflowDefinition(
                user, projectCode, name, processDefinitionCode, description, submittedGlobalParams, "[]", timeout,
                taskRelationJson, taskDefinitionJson, WorkflowExecutionTypeEnum.PARALLEL);

        Assertions.assertTrue(updated.getGlobalParams().contains("new-secret"));
        Assertions.assertFalse(updated.getGlobalParams().contains("old-secret"));
        Assertions.assertTrue(persisted.getValue().getGlobalParams().contains("new-secret"));
        Assertions.assertSame(updated, persisted.getValue());
    }

    @Test
    public void testUpdateWorkflowDefinitionShouldRejectUnavailableSubWorkflow() {
        Project project = getProject(projectCode);
        WorkflowDefinition workflowDefinition = getWorkflowDefinition();
        workflowDefinition.setName("origin-name");
        when(projectDao.queryByCode(projectCode)).thenReturn(project);
        Mockito.doNothing().when(projectService).checkHasProjectWritePermissionThrowException(eq(user), eq(project));
        when(processService.transformTask(anyList(), anyList())).thenReturn(getTaskNodeList());
        when(workflowDefinitionDao.queryByCode(processDefinitionCode)).thenReturn(Optional.of(workflowDefinition));
        when(workflowDefinitionDao.verifyByDefineName(projectCode, name)).thenReturn(null);
        doThrow(new ServiceException(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION))
                .when(taskSubWorkflowPermissionChecker).checkPermission(eq(user), anyList());

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.updateWorkflowDefinition(
                        user, projectCode, name, processDefinitionCode, description, "[]", "[]", timeout,
                        taskRelationJson, taskDefinitionJson, WorkflowExecutionTypeEnum.PARALLEL));

        Assertions.assertEquals(Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION.getCode(), exception.getCode());
        Mockito.verify(processService, Mockito.never())
                .saveTaskDefine(eq(user), eq(projectCode), anyList(), eq(Boolean.TRUE));
    }

    @Test
    public void testGetNewProcessName() {
        String processName1 = "test_copy_" + DateUtils.getCurrentTimeStamp();
        final String newName1 = workflowDefinitionService.getNewName(processName1, Constants.COPY_SUFFIX);
        Assertions.assertEquals(2, newName1.split(Constants.COPY_SUFFIX).length);
        String processName2 = "wf_copy_all_ods_data_to_d";
        final String newName2 = workflowDefinitionService.getNewName(processName2, Constants.COPY_SUFFIX);
        Assertions.assertEquals(3, newName2.split(Constants.COPY_SUFFIX).length);
        String processName3 = "test_import_" + DateUtils.getCurrentTimeStamp();
        final String newName3 = workflowDefinitionService.getNewName(processName3, Constants.IMPORT_SUFFIX);
        Assertions.assertEquals(2, newName3.split(Constants.IMPORT_SUFFIX).length);
    }

    @Test
    public void testViewVariables() {
        Project project = getProject(projectCode);
        when(projectDao.queryByCode(projectCode)).thenReturn(project);

        WorkflowDefinition workflowDefinition = getWorkflowDefinition();

        // project check auth fail
        Mockito.doThrow(new ServiceException(Status.PROJECT_NOT_FOUND))
                .when(projectService).checkProjectAndAuthThrowException(user, project, WORKFLOW_DEFINITION);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> workflowDefinitionService.viewVariables(user, workflowDefinition.getProjectCode(),
                        workflowDefinition.getCode()));
        Assertions.assertEquals(Status.PROJECT_NOT_FOUND.getCode(), ex.getCode());
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

    private List<TaskNode> getTaskNodeList() {
        TaskNode firstTaskNode = new TaskNode();
        firstTaskNode.setCode(123456789L);
        firstTaskNode.setPreTasks(JSONUtils.toJsonString(Collections.emptyList()));

        TaskNode secondTaskNode = new TaskNode();
        secondTaskNode.setCode(123451234L);
        secondTaskNode.setPreTasks(JSONUtils.toJsonString(Collections.singletonList(123456789L)));

        return Arrays.asList(firstTaskNode, secondTaskNode);
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

    private MultipartFile createMultipartFile(String filePath) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader().getResource(filePath).toURI());
        byte[] content = Files.readAllBytes(path);

        // 2. 创建MockMultipartFile对象
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "",
                "application/json",
                content);
        return multipartFile;
    }

    @Test
    public void testSaveWorkflowLineageWithEmptyList() {
        // Test case: Empty lineage list should delete historical lineage
        long projectCode = 1L;
        long workflowDefinitionCode = 100L;
        int workflowDefinitionVersion = 1;
        List<TaskDefinitionLog> emptyTaskDefinitionLogList = new ArrayList<>();

        // Mock updateWorkflowLineage to return 0 for empty list
        when(workflowLineageService.updateWorkflowLineage(eq(workflowDefinitionCode), anyList()))
                .thenReturn(0);

        // Execute - should not throw exception
        Assertions.assertDoesNotThrow(() -> {
            workflowDefinitionService.saveWorkflowLineage(projectCode, workflowDefinitionCode,
                    workflowDefinitionVersion, emptyTaskDefinitionLogList);
        });

        // Verify that updateWorkflowLineage was called with empty list
        verify(workflowLineageService).updateWorkflowLineage(eq(workflowDefinitionCode), anyList());
    }

    @Test
    public void testSaveWorkflowLineageWithNonEmptyList() {
        // Test case: Normal save with non-empty lineage list
        long projectCode = 1L;
        long workflowDefinitionCode = 100L;
        int workflowDefinitionVersion = 1;

        // Create task definition logs with dependent tasks
        List<TaskDefinitionLog> taskDefinitionLogList = new ArrayList<>();
        TaskDefinitionLog taskLog = new TaskDefinitionLog();
        taskLog.setCode(200L);
        taskLog.setVersion(1);
        taskLog.setProjectCode(projectCode);
        taskLog.setTaskType("DEPENDENT");
        // Set taskParams with dependent parameters
        String taskParams =
                "{\"dependence\":{\"dependTaskList\":[{\"dependItemList\":[{\"definitionCode\":50,\"depTaskCode\":300}]}]}}";
        taskLog.setTaskParams(taskParams);
        taskDefinitionLogList.add(taskLog);

        // Mock updateWorkflowLineage to return success
        when(workflowLineageService.updateWorkflowLineage(eq(workflowDefinitionCode), anyList()))
                .thenReturn(1);

        // Execute - should not throw exception
        Assertions.assertDoesNotThrow(() -> {
            workflowDefinitionService.saveWorkflowLineage(projectCode, workflowDefinitionCode,
                    workflowDefinitionVersion, taskDefinitionLogList);
        });

        // Verify that updateWorkflowLineage was called
        verify(workflowLineageService).updateWorkflowLineage(eq(workflowDefinitionCode), anyList());
    }

    @Test
    public void testSaveWorkflowLineageWithInsertFailure() {
        // Test case: Should throw exception when insert fails
        long projectCode = 1L;
        long workflowDefinitionCode = 100L;
        int workflowDefinitionVersion = 1;

        // Create task definition logs
        List<TaskDefinitionLog> taskDefinitionLogList = new ArrayList<>();
        TaskDefinitionLog taskLog = new TaskDefinitionLog();
        taskLog.setCode(200L);
        taskLog.setVersion(1);
        taskLog.setProjectCode(projectCode);
        taskLog.setTaskType("DEPENDENT");
        String taskParams =
                "{\"dependence\":{\"dependTaskList\":[{\"dependItemList\":[{\"definitionCode\":50,\"depTaskCode\":300}]}]}}";
        taskLog.setTaskParams(taskParams);
        taskDefinitionLogList.add(taskLog);

        // Mock updateWorkflowLineage to throw exception (insert failure)
        when(workflowLineageService.updateWorkflowLineage(eq(workflowDefinitionCode), anyList()))
                .thenThrow(new ServiceException(Status.CREATE_WORKFLOW_LINEAGE_ERROR));

        // Execute and verify exception
        ServiceException exception = Assertions.assertThrows(ServiceException.class, () -> {
            workflowDefinitionService.saveWorkflowLineage(projectCode, workflowDefinitionCode,
                    workflowDefinitionVersion, taskDefinitionLogList);
        });

        Assertions.assertEquals(Status.CREATE_WORKFLOW_LINEAGE_ERROR.getCode(), exception.getCode());
        verify(workflowLineageService).updateWorkflowLineage(eq(workflowDefinitionCode), anyList());
    }

    @Test
    void testReplaceTaskCodeForSwitchTaskParams_replacesAllMappedCodes() throws Exception {
        long old1 = 100L, old2 = 200L, old3 = 300L;
        long new1 = 1000L, new2 = 2000L, new3 = 3000L;

        SwitchParameters.SwitchResult result = new SwitchParameters.SwitchResult();
        result.setNextNode(old2);
        result.setDependTaskList(Arrays.asList(
                createSwitchResultVo(old3),
                createSwitchResultVo(999L)));

        SwitchParameters params = new SwitchParameters();
        params.setNextBranch(old1);
        params.setSwitchResult(result);

        TaskDefinitionLog taskDef = new TaskDefinitionLog();
        taskDef.setTaskParams(JSONUtils.toJsonString(params));

        Map<Long, Long> codeMap = new HashMap<>();
        codeMap.put(old1, new1);
        codeMap.put(old2, new2);
        codeMap.put(old3, new3);

        invokePrivateMethod("replaceTaskCodeForSwitchTaskParams", taskDef, codeMap);

        SwitchParameters updated = JSONUtils.parseObject(taskDef.getTaskParams(), SwitchParameters.class);
        assert updated != null;
        assertThat(updated.getNextBranch()).isEqualTo(new1);
        assertThat(updated.getSwitchResult().getNextNode()).isEqualTo(new2);
        assertThat(updated.getSwitchResult().getDependTaskList().get(0).getNextNode()).isEqualTo(new3);
        assertThat(updated.getSwitchResult().getDependTaskList().get(1).getNextNode()).isEqualTo(999L);
    }

    @Test
    void testReplaceTaskCodeForSwitchTaskParams_handlesNullFields() throws Exception {
        SwitchParameters params = new SwitchParameters();
        params.setNextBranch(null);
        params.setSwitchResult(null);

        TaskDefinitionLog taskDef = new TaskDefinitionLog();
        taskDef.setTaskParams(JSONUtils.toJsonString(params));

        invokePrivateMethod("replaceTaskCodeForSwitchTaskParams", taskDef, Collections.emptyMap());

        SwitchParameters result = JSONUtils.parseObject(taskDef.getTaskParams(), SwitchParameters.class);
        assert result != null;
        assertThat(result.getNextBranch()).isNull();
        assertThat(result.getSwitchResult()).isNull();
    }

    @Test
    void testReplaceTaskCodeForSwitchTaskParams_throwsOnInvalidJson() {
        TaskDefinitionLog taskDef = new TaskDefinitionLog();
        taskDef.setTaskParams("{ broken json }");

        assertThatThrownBy(
                () -> invokePrivateMethod("replaceTaskCodeForSwitchTaskParams", taskDef, Collections.emptyMap()))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Failed to parse Switch task params");
    }

    @Test
    void testReplaceTaskCodeForConditionTaskParams_replacesDepAndResultNodes() throws Exception {
        long oldDep = 500L, oldSuc = 600L, oldFail = 700L;
        long newDep = 5000L, newSuc = 6000L, newFail = 7000L;

        ConditionDependentItem item = new ConditionDependentItem();
        item.setDepTaskCode(oldDep);

        ConditionDependentTaskModel model = new ConditionDependentTaskModel();
        model.setDependItemList(Collections.singletonList(item));

        ConditionsParameters.ConditionDependency dep = new ConditionsParameters.ConditionDependency();
        dep.setDependTaskList(Collections.singletonList(model));

        ConditionsParameters.ConditionResult result = new ConditionsParameters.ConditionResult();
        result.setSuccessNode(Arrays.asList(oldSuc, 888L));
        result.setFailedNode(Collections.singletonList(oldFail));

        ConditionsParameters params = new ConditionsParameters();
        params.setDependence(dep);
        params.setConditionResult(result);

        TaskDefinitionLog taskDef = new TaskDefinitionLog();
        taskDef.setTaskParams(JSONUtils.toJsonString(params));

        Map<Long, Long> codeMap = new HashMap<>();
        codeMap.put(oldDep, newDep);
        codeMap.put(oldSuc, newSuc);
        codeMap.put(oldFail, newFail);

        invokePrivateMethod("replaceTaskCodeForConditionTaskParams", taskDef, codeMap);

        ConditionsParameters updated = JSONUtils.parseObject(taskDef.getTaskParams(), ConditionsParameters.class);

        assert updated != null;
        long actualDepCode = updated.getDependence()
                .getDependTaskList().get(0).getDependItemList().get(0).getDepTaskCode();
        assertThat(actualDepCode).isEqualTo(newDep);

        assertThat(updated.getConditionResult().getSuccessNode())
                .containsExactly(newSuc, 888L);
        assertThat(updated.getConditionResult().getFailedNode())
                .containsExactly(newFail);
    }

    @Test
    void testReplaceTaskCodeForConditionTaskParams_handlesNulls() throws Exception {
        ConditionsParameters params = new ConditionsParameters();
        params.setDependence(null);
        params.setConditionResult(null);

        TaskDefinitionLog taskDef = new TaskDefinitionLog();
        taskDef.setTaskParams(JSONUtils.toJsonString(params));

        invokePrivateMethod("replaceTaskCodeForConditionTaskParams", taskDef, Collections.emptyMap());

        ConditionsParameters result = JSONUtils.parseObject(taskDef.getTaskParams(), ConditionsParameters.class);
        assert result != null;
        assertThat(result.getDependence()).isNull();
        assertThat(result.getConditionResult()).isNull();
    }

    @Test
    void testReplaceTaskCodeForConditionTaskParams_throwsOnInvalidJson() {
        TaskDefinitionLog taskDef = new TaskDefinitionLog();
        taskDef.setTaskParams("{ invalid: , }");

        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> invokePrivateMethod("replaceTaskCodeForConditionTaskParams", taskDef,
                        Collections.emptyMap()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to parse Condition task params");
    }

    @Test
    void testReplaceInNodeList_withNullList() throws Exception {
        AtomicReference<List<Long>> stateRef = new AtomicReference<>(null);
        Supplier<List<Long>> getter = stateRef::get;
        Consumer<List<Long>> setter = stateRef::set;

        invokeReplaceInNodeList(getter, setter, Collections.emptyMap());

        assertThat(stateRef.get()).isNull();
    }

    @Test
    void testReplaceInNodeList_withEmptyList() throws Exception {
        AtomicReference<List<Long>> stateRef = new AtomicReference<>(new ArrayList<>());
        Supplier<List<Long>> getter = stateRef::get;
        Consumer<List<Long>> setter = stateRef::set;

        invokeReplaceInNodeList(getter, setter, Collections.emptyMap());

        assertThat(stateRef.get()).isEmpty();
    }

    @Test
    void testReplaceInNodeList_replacesMappedCodes() throws Exception {
        AtomicReference<List<Long>> stateRef = new AtomicReference<>(new ArrayList<>(Arrays.asList(1L, 2L, 3L)));
        Supplier<List<Long>> getter = stateRef::get;
        Consumer<List<Long>> setter = stateRef::set;

        Map<Long, Long> codeMap = new HashMap<>();
        codeMap.put(1L, 10L);
        codeMap.put(3L, 30L);

        invokeReplaceInNodeList(getter, setter, codeMap);

        assertThat(stateRef.get()).containsExactly(10L, 2L, 30L);
    }

    @Test
    void testReplaceInNodeList_preservesUnmappedAndNullElements() throws Exception {
        AtomicReference<List<Long>> stateRef =
                new AtomicReference<>(new ArrayList<>(Arrays.asList(null, 4L, 5L, null)));
        Supplier<List<Long>> getter = stateRef::get;
        Consumer<List<Long>> setter = stateRef::set;

        Map<Long, Long> codeMap = new HashMap<>();
        codeMap.put(4L, 40L);

        invokeReplaceInNodeList(getter, setter, codeMap);

        assertThat(stateRef.get()).containsExactly((Long) null, 40L, 5L, (Long) null);
    }

    @Test
    void testReplaceInNodeList_noOpWhenCodeMapIsEmpty() throws Exception {
        AtomicReference<List<Long>> stateRef = new AtomicReference<>(new ArrayList<>(Arrays.asList(6L, 7L)));
        Supplier<List<Long>> getter = stateRef::get;
        Consumer<List<Long>> setter = stateRef::set;

        invokeReplaceInNodeList(getter, setter, Collections.emptyMap());

        assertThat(stateRef.get()).containsExactly(6L, 7L);
    }

    @Test
    void testReplaceInNodeList_createsNewListInstance() throws Exception {
        List<Long> original = Arrays.asList(8L, 9L);
        AtomicReference<List<Long>> stateRef = new AtomicReference<>(new ArrayList<>(original));
        Supplier<List<Long>> getter = stateRef::get;
        Consumer<List<Long>> setter = stateRef::set;

        invokeReplaceInNodeList(getter, setter, Collections.emptyMap());

        assertThat(stateRef.get()).isNotSameAs(original);
        assertThat(stateRef.get()).isEqualTo(original);
    }

    // Reflection Helper to call private replaceInNodeList
    private void invokeReplaceInNodeList(Supplier<List<Long>> getter,
                                         Consumer<List<Long>> setter,
                                         Map<Long, Long> codeMap) throws Exception {
        Method method = WorkflowDefinitionServiceImpl.class
                .getDeclaredMethod("replaceInNodeList", Supplier.class, Consumer.class, Map.class);
        method.setAccessible(true);

        try {
            method.invoke(workflowDefinitionService, getter, setter, codeMap);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("Exception in private method", cause);
        }
    }

    // Reflection Helper to call private replaceTaskCodeForSwitchTaskParams and replaceTaskCodeForConditionTaskParams
    private void invokePrivateMethod(String methodName, Object... args) throws Exception {
        Class<?>[] argTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }
        if ("replaceTaskCodeForSwitchTaskParams".equals(methodName) ||
                "replaceTaskCodeForConditionTaskParams".equals(methodName)) {
            argTypes[0] = TaskDefinitionLog.class;
            argTypes[1] = Map.class;
        }

        Method method = WorkflowDefinitionServiceImpl.class.getDeclaredMethod(methodName, argTypes);
        method.setAccessible(true);

        try {
            method.invoke(workflowDefinitionService, args);
        } catch (InvocationTargetException e) {
            // Unwrap the actual exception thrown by the private method
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                // Wrap checked exceptions in a RuntimeException or rethrow as Exception
                throw new RuntimeException("Checked exception thrown from private method", cause);
            }
        }
    }

    // Helper to create SwitchResultVo
    private SwitchResultVo createSwitchResultVo(Long nextNode) {
        SwitchResultVo vo = new SwitchResultVo();
        vo.setNextNode(nextNode);
        return vo;
    }
}
