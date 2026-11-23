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

package org.apache.dolphinscheduler.service.process;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.service.model.TaskNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessServiceTest {

    @InjectMocks
    private ProcessServiceImpl processService;

    @Mock
    private WorkflowTaskRelationLogMapper workflowTaskRelationLogMapper;

    @Mock
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;

    @Mock
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Mock
    private TaskDefinitionDao taskDefinitionDao;

    @Mock
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Test
    public void testGetUserById() {
        User user = new User();
        user.setId(123);
        when(userMapper.selectById(123)).thenReturn(user);
        Assertions.assertEquals(user, processService.getUserById(123));
    }

    @Test
    public void testFindAllSubWorkflowDefinitionCode() {
        int parentProcessDefineId = 1;
        long parentProcessDefineCode = 1L;
        int parentProcessDefineVersion = 1;

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(parentProcessDefineCode);
        workflowDefinition.setVersion(parentProcessDefineVersion);
        when(workflowDefinitionMapper.selectById(parentProcessDefineId)).thenReturn(workflowDefinition);

        long postTaskCode = 2L;
        int postTaskVersion = 2;

        List<WorkflowTaskRelationLog> relationLogList = new ArrayList<>();
        WorkflowTaskRelationLog processTaskRelationLog = new WorkflowTaskRelationLog();
        processTaskRelationLog.setPostTaskCode(postTaskCode);
        processTaskRelationLog.setPostTaskVersion(postTaskVersion);
        relationLogList.add(processTaskRelationLog);
        when(workflowTaskRelationLogMapper.queryByWorkflowCodeAndVersion(parentProcessDefineCode,
                parentProcessDefineVersion)).thenReturn(relationLogList);

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        TaskDefinitionLog taskDefinitionLog1 = new TaskDefinitionLog();
        taskDefinitionLog1.setTaskParams("{\"workflowDefinitionCode\": 123L}");
        taskDefinitionLogs.add(taskDefinitionLog1);
        when(taskDefinitionLogMapper.queryByTaskDefinitions(Mockito.anySet())).thenReturn(taskDefinitionLogs);

        Assertions.assertTrue(processService.findAllSubWorkflowDefinitionCode(parentProcessDefineCode).isEmpty());
    }

    @Test
    public void testSwitchVersion() {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(1L);
        workflowDefinition.setProjectCode(1L);
        workflowDefinition.setId(123);
        workflowDefinition.setName("test");
        workflowDefinition.setVersion(1);

        WorkflowDefinitionLog processDefinitionLog = new WorkflowDefinitionLog();
        processDefinitionLog.setCode(1L);
        processDefinitionLog.setVersion(2);
        Assertions.assertEquals(0, processService.switchVersion(workflowDefinition, processDefinitionLog));
    }

    @Test
    public void testSwitchTaskDefinitionVersionRestoresHistoryOnlyTask() {
        TaskDefinitionLog version = new TaskDefinitionLog();
        version.setId(42);
        version.setCode(101L);
        version.setVersion(2);
        version.setProjectCode(1L);
        when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(101L, 2)).thenReturn(version);
        when(taskDefinitionMapper.insert(any(TaskDefinition.class))).thenReturn(1);

        Assertions.assertEquals(1, processService.switchTaskDefinitionVersion(101L, 2));

        ArgumentCaptor<TaskDefinition> restored = ArgumentCaptor.forClass(TaskDefinition.class);
        Mockito.verify(taskDefinitionMapper).insert(restored.capture());
        Assertions.assertNull(restored.getValue().getId());
        Assertions.assertEquals(101L, restored.getValue().getCode());
        Assertions.assertEquals(2, restored.getValue().getVersion());
        Assertions.assertEquals(1L, restored.getValue().getProjectCode());
    }

    @Test
    public void testSwitchTaskDefinitionVersionRejectsFailedUpdate() {
        TaskDefinition current = new TaskDefinition(101L, 1);
        current.setId(7);
        TaskDefinitionLog version = new TaskDefinitionLog();
        version.setCode(101L);
        version.setVersion(2);
        when(taskDefinitionMapper.queryByCode(101L)).thenReturn(current);
        when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(101L, 2)).thenReturn(version);
        when(taskDefinitionMapper.updateById(version)).thenReturn(0);

        Assertions.assertEquals(Constants.EXIT_CODE_FAILURE, processService.switchTaskDefinitionVersion(101L, 2));
    }

    @Test
    public void testSwitchWorkflowTaskRelationVersionRejectsPartialTaskRestore() {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setCode(1L);
        definition.setProjectCode(1L);
        definition.setVersion(2);
        WorkflowTaskRelationLog first = new WorkflowTaskRelationLog();
        first.setPostTaskCode(101L);
        first.setPostTaskVersion(1);
        WorkflowTaskRelationLog second = new WorkflowTaskRelationLog();
        second.setPreTaskCode(101L);
        second.setPreTaskVersion(1);
        second.setPostTaskCode(102L);
        second.setPostTaskVersion(1);
        when(workflowTaskRelationLogMapper.queryByWorkflowCodeAndVersion(1L, 2))
                .thenReturn(Arrays.asList(first, second));
        when(workflowTaskRelationMapper.batchInsert(Mockito.anyList())).thenReturn(2);
        when(taskDefinitionMapper.queryByCode(101L)).thenReturn(new TaskDefinition(101L, 1));

        Assertions.assertEquals(Constants.EXIT_CODE_FAILURE,
                processService.switchWorkflowTaskRelationVersion(definition));
    }

    @Test
    public void testSaveTaskDefine() {
        User operator = new User();
        operator.setId(-1);
        operator.setUserType(UserType.GENERAL_USER);
        long projectCode = 751485690568704L;
        String taskJson =
                "[{\"code\":751500437479424,\"name\":\"aa\",\"version\":1,\"description\":\"\",\"delayTime\":0,"
                        + "\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"sleep 1s\\necho 11\","
                        + "\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"waitStartTimeout\":{}},"
                        + "\"flag\":\"YES\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"yarn\",\"failRetryTimes\":0,\"failRetryInterval\":1,"
                        + "\"timeoutFlag\":\"OPEN\",\"timeoutNotifyStrategy\":\"FAILED\",\"timeout\":1,\"environmentCode\":751496815697920},"
                        + "{\"code\":751516889636864,\"name\":\"bb\",\"description\":\"\",\"taskType\":\"SHELL\",\"taskParams\":{\"resourceList\":[],"
                        + "\"localParams\":[],\"rawScript\":\"echo 22\",\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},"
                        + "\"waitStartTimeout\":{}},\"flag\":\"YES\",\"taskPriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"failRetryTimes\":\"0\","
                        + "\"failRetryInterval\":\"1\",\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"timeout\":0,\"delayTime\":\"0\",\"environmentCode\":-1}]";
        List<TaskDefinitionLog> taskDefinitionLogs = JSONUtils.toList(taskJson, TaskDefinitionLog.class);
        TaskDefinitionLog taskDefinition = new TaskDefinitionLog();
        taskDefinition.setCode(751500437479424L);
        taskDefinition.setName("aa");
        taskDefinition.setProjectCode(751485690568704L);
        taskDefinition.setTaskType("SHELL");
        taskDefinition.setUserId(-1);
        taskDefinition.setVersion(1);
        taskDefinition.setCreateTime(new Date());
        taskDefinition.setUpdateTime(new Date());
        when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskDefinition.getCode(),
                taskDefinition.getVersion())).thenReturn(taskDefinition);
        when(taskDefinitionLogMapper.queryMaxVersionForDefinition(taskDefinition.getCode())).thenReturn(1);
        when(taskDefinitionMapper.queryByCodeList(Collections.singletonList(taskDefinition.getCode())))
                .thenReturn(Collections.singletonList(taskDefinition));
        when(taskDefinitionMapper.queryByCode(Mockito.anyLong())).thenReturn(taskDefinition);
        int result = processService.saveTaskDefine(operator, projectCode, taskDefinitionLogs, Boolean.TRUE);
        Assertions.assertEquals(0, result);
    }

    @Test
    public void testGenDagGraph() {
        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setCode(1L);
        workflowDefinition.setId(123);
        workflowDefinition.setName("test");
        workflowDefinition.setVersion(1);
        workflowDefinition.setCode(11L);

        WorkflowTaskRelationLog processTaskRelation = new WorkflowTaskRelationLog();
        processTaskRelation.setName("def 1");
        processTaskRelation.setWorkflowDefinitionVersion(1);
        processTaskRelation.setProjectCode(1L);
        processTaskRelation.setWorkflowDefinitionCode(1L);
        processTaskRelation.setPostTaskCode(3L);
        processTaskRelation.setPreTaskCode(2L);
        processTaskRelation.setUpdateTime(new Date());
        processTaskRelation.setCreateTime(new Date());
        List<WorkflowTaskRelationLog> list = new ArrayList<>();
        list.add(processTaskRelation);

        TaskDefinitionLog taskDefinition = new TaskDefinitionLog();
        taskDefinition.setCode(3L);
        taskDefinition.setName("1-test");
        taskDefinition.setProjectCode(1L);
        taskDefinition.setTaskType("SHELL");
        taskDefinition.setUserId(1);
        taskDefinition.setVersion(2);
        taskDefinition.setCreateTime(new Date());
        taskDefinition.setUpdateTime(new Date());

        TaskDefinitionLog td2 = new TaskDefinitionLog();
        td2.setCode(2L);
        td2.setName("unit-test");
        td2.setProjectCode(1L);
        td2.setTaskType("SHELL");
        td2.setUserId(1);
        td2.setVersion(1);
        td2.setCreateTime(new Date());
        td2.setUpdateTime(new Date());

        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        taskDefinitionLogs.add(taskDefinition);
        taskDefinitionLogs.add(td2);

        when(taskDefinitionLogDao.queryTaskDefineLogList(any())).thenReturn(taskDefinitionLogs);
        when(workflowTaskRelationLogMapper.queryByWorkflowCodeAndVersion(Mockito.anyLong(), Mockito.anyInt()))
                .thenReturn(list);

        DAG<Long, TaskNode, TaskNodeRelation> stringTaskNodeTaskNodeRelationDAG =
                processService.genDagGraph(workflowDefinition);
        Assertions.assertEquals(1, stringTaskNodeTaskNodeRelationDAG.getNodesCount());
    }

}
