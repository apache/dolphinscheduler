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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKER_GROUP_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKER_GROUP_DELETE;
import static org.mockito.ArgumentMatchers.any;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.WorkerGroupServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentWorkerGroupRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WorkerGroupServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(WorkerGroupServiceTest.class);

    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);

    private static final Logger serviceLogger = LoggerFactory.getLogger(WorkerGroupService.class);

    @InjectMocks
    private WorkerGroupServiceImpl workerGroupService;

    @Mock
    private WorkerGroupMapper workerGroupMapper;

    @Mock
    private ProcessInstanceMapper processInstanceMapper;

    @Mock
    private ProcessService processService;

    @Mock
    private RegistryClient registryClient;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @Mock
    private EnvironmentWorkerGroupRelationMapper environmentWorkerGroupRelationMapper;

    private final String GROUP_NAME = "testWorkerGroup";

    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("workerGroupTestUser");
        loginUser.setId(1);
        return loginUser;
    }

    @Test
    public void giveNoPermission_whenSaveWorkerGroup_expectNoOperation() {
        User loginUser = getLoginUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                WORKER_GROUP_CREATE, baseServiceLogger)).thenReturn(false);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                baseServiceLogger)).thenReturn(false);
        Map<String, Object> result =
                workerGroupService.saveWorkerGroup(loginUser, 1, GROUP_NAME, "localhost:0000", "test group", "");
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(),
                ((Status) result.get(Constants.STATUS)).getCode());
    }

    @Test
    public void giveNullName_whenSaveWorkerGroup_expectNAME_NULL() {
        User loginUser = getLoginUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                WORKER_GROUP_CREATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                baseServiceLogger)).thenReturn(true);
        Map<String, Object> result =
                workerGroupService.saveWorkerGroup(loginUser, 1, "", "localhost:0000", "test group", "");
        Assertions.assertEquals(Status.NAME_NULL.getCode(),
                ((Status) result.get(Constants.STATUS)).getCode());
    }

    @Test
    public void giveSameUserName_whenSaveWorkerGroup_expectNAME_EXIST() {
        User loginUser = getLoginUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                WORKER_GROUP_CREATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                baseServiceLogger)).thenReturn(true);
        Mockito.when(workerGroupMapper.selectById(1)).thenReturn(null);
        List<WorkerGroup> workerGroupList = new ArrayList<WorkerGroup>();
        workerGroupList.add(getWorkerGroup(1));
        Mockito.when(workerGroupMapper.queryWorkerGroupByName(GROUP_NAME)).thenReturn(workerGroupList);

        Map<String, Object> result =
                workerGroupService.saveWorkerGroup(loginUser, 1, GROUP_NAME, "localhost:0000", "test group", "");
        Assertions.assertEquals(Status.NAME_EXIST.getCode(),
                ((Status) result.get(Constants.STATUS)).getCode());
    }

    @Test
    public void giveInvalidAddress_whenSaveWorkerGroup_expectADDRESS_INVALID() {
        User loginUser = getLoginUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                WORKER_GROUP_CREATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                baseServiceLogger)).thenReturn(true);
        Mockito.when(workerGroupMapper.selectById(1)).thenReturn(null);
        Mockito.when(workerGroupMapper.queryWorkerGroupByName(GROUP_NAME)).thenReturn(null);
        String workerGroupPath =
                Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS + Constants.SINGLE_SLASH + GROUP_NAME;
        Mockito.when(registryClient.exists(workerGroupPath)).thenReturn(false);
        Map<String, String> serverMaps = new HashMap<>();
        serverMaps.put("localhost1:0000", "");
        Mockito.when(registryClient.getServerMaps(NodeType.WORKER)).thenReturn(serverMaps);

        Map<String, Object> result =
                workerGroupService.saveWorkerGroup(loginUser, 1, GROUP_NAME, "localhost:0000", "test group", "");
        Assertions.assertEquals(Status.WORKER_ADDRESS_INVALID.getCode(),
                ((Status) result.get(Constants.STATUS)).getCode());
    }

    @Test
    public void giveValidWorkerGroup_whenSaveWorkerGroup_expectSuccess() {
        User loginUser = getLoginUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                WORKER_GROUP_CREATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                baseServiceLogger)).thenReturn(true);

        Mockito.when(workerGroupMapper.selectById(1)).thenReturn(null);
        Mockito.when(workerGroupMapper.queryWorkerGroupByName(GROUP_NAME)).thenReturn(null);
        String workerGroupPath =
                Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS + Constants.SINGLE_SLASH + GROUP_NAME;
        Mockito.when(registryClient.exists(workerGroupPath)).thenReturn(false);
        Map<String, String> serverMaps = new HashMap<>();
        serverMaps.put("localhost:0000", "");
        Mockito.when(registryClient.getServerMaps(NodeType.WORKER)).thenReturn(serverMaps);
        Mockito.when(workerGroupMapper.insert(any())).thenReturn(1);

        Map<String, Object> result =
                workerGroupService.saveWorkerGroup(loginUser, 1, GROUP_NAME, "localhost:0000", "test group", "");
        Assertions.assertEquals(Status.SUCCESS.getCode(),
                ((Status) result.get(Constants.STATUS)).getCode());
    }

    @Test
    public void giveValidParams_whenQueryAllGroupPaging_expectSuccess() {
        User loginUser = getLoginUser();
        Set<Integer> ids = new HashSet<>();
        ids.add(1);
        List<WorkerGroup> workerGroups = new ArrayList<>();
        workerGroups.add(getWorkerGroup(1));
        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.WORKER_GROUP,
                loginUser.getId(), serviceLogger)).thenReturn(ids);
        Mockito.when(workerGroupMapper.selectBatchIds(ids)).thenReturn(workerGroups);
        Set<String> activeWorkerNodes = new HashSet<>();
        activeWorkerNodes.add("localhost:12345");
        activeWorkerNodes.add("localhost:23456");
        Mockito.when(registryClient.getServerNodeSet(NodeType.WORKER)).thenReturn(activeWorkerNodes);

        Result result = workerGroupService.queryAllGroupPaging(loginUser, 1, 1, null);
        Assertions.assertEquals(result.getCode(), Status.SUCCESS.getCode());
    }

    @Test
    public void testQueryAllGroup() {
        Map<String, Object> result = workerGroupService.queryAllGroup(getLoginUser());
        List<String> workerGroups = (List<String>) result.get(Constants.DATA_LIST);
        Assertions.assertEquals(workerGroups.size(), 1);
    }

    @Test
    public void giveNotExistsWorkerGroup_whenDeleteWorkerGroupById_expectNotExists() {
        User loginUser = getLoginUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                WORKER_GROUP_DELETE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                baseServiceLogger)).thenReturn(true);
        Mockito.when(workerGroupMapper.selectById(1)).thenReturn(null);

        Map<String, Object> notExistResult = workerGroupService.deleteWorkerGroupById(loginUser, 1);
        Assertions.assertEquals(Status.DELETE_WORKER_GROUP_NOT_EXIST.getCode(),
                ((Status) notExistResult.get(Constants.STATUS)).getCode());
    }

    @Test
    public void giveRunningProcess_whenDeleteWorkerGroupById_expectFailed() {
        User loginUser = getLoginUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                WORKER_GROUP_DELETE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                baseServiceLogger)).thenReturn(true);
        WorkerGroup workerGroup = getWorkerGroup(1);
        Mockito.when(workerGroupMapper.selectById(1)).thenReturn(workerGroup);
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>();
        processInstances.add(processInstance);
        Mockito.when(processInstanceMapper.queryByWorkerGroupNameAndStatus(workerGroup.getName(),
                org.apache.dolphinscheduler.service.utils.Constants.NOT_TERMINATED_STATES))
                .thenReturn(processInstances);

        Map<String, Object> deleteFailed = workerGroupService.deleteWorkerGroupById(loginUser, 1);
        Assertions.assertEquals(Status.DELETE_WORKER_GROUP_BY_ID_FAIL.getCode(),
                ((Status) deleteFailed.get(Constants.STATUS)).getCode());
    }

    @Test
    public void giveValidParams_whenDeleteWorkerGroupById_expectSuccess() {
        User loginUser = getLoginUser();
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                WORKER_GROUP_DELETE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.WORKER_GROUP, null, 1,
                baseServiceLogger)).thenReturn(true);
        WorkerGroup workerGroup = getWorkerGroup(1);
        Mockito.when(workerGroupMapper.selectById(1)).thenReturn(workerGroup);
        Mockito.when(processInstanceMapper.queryByWorkerGroupNameAndStatus(workerGroup.getName(),
                org.apache.dolphinscheduler.service.utils.Constants.NOT_TERMINATED_STATES)).thenReturn(null);
        Mockito.when(workerGroupMapper.deleteById(1)).thenReturn(1);
        Mockito.when(processInstanceMapper.updateProcessInstanceByWorkerGroupName(workerGroup.getName(), ""))
                .thenReturn(1);
        Mockito.when(environmentWorkerGroupRelationMapper.queryByWorkerGroupName(workerGroup.getName()))
                .thenReturn(null);
        Map<String, Object> successResult = workerGroupService.deleteWorkerGroupById(loginUser, 1);
        Assertions.assertEquals(Status.SUCCESS.getCode(),
                ((Status) successResult.get(Constants.STATUS)).getCode());
    }

    @Test
    public void testQueryAllGroupWithDefault() {
        Map<String, Object> result = workerGroupService.queryAllGroup(getLoginUser());
        List<String> workerGroups = (List<String>) result.get(Constants.DATA_LIST);
        Assertions.assertEquals(1, workerGroups.size());
        Assertions.assertEquals("default", workerGroups.toArray()[0]);
    }

    @Test
    public void giveNull_whenGetTaskWorkerGroup_expectNull() {
        String nullWorkerGroup = workerGroupService.getTaskWorkerGroup(null);
        Assertions.assertNull(nullWorkerGroup);
    }

    @Test
    public void giveCorrectTaskInstance_whenGetTaskWorkerGroup_expectTaskWorkerGroup() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setWorkerGroup("cluster1");

        String workerGroup = workerGroupService.getTaskWorkerGroup(taskInstance);
        Assertions.assertEquals("cluster1", workerGroup);
    }

    @Test
    public void giveNullWorkerGroup_whenGetTaskWorkerGroup_expectProcessWorkerGroup() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setProcessInstanceId(1);
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setWorkerGroup("cluster1");
        Mockito.when(processService.findProcessInstanceById(1)).thenReturn(processInstance);

        String workerGroup = workerGroupService.getTaskWorkerGroup(taskInstance);
        Assertions.assertEquals("cluster1", workerGroup);
    }

    @Test
    public void giveNullTaskAndProcessWorkerGroup_whenGetTaskWorkerGroup_expectDefault() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setProcessInstanceId(1);
        Mockito.when(processService.findProcessInstanceById(1)).thenReturn(null);

        String defaultWorkerGroup = workerGroupService.getTaskWorkerGroup(taskInstance);
        Assertions.assertEquals(Constants.DEFAULT_WORKER_GROUP, defaultWorkerGroup);
    }

    /**
     * get Group
     */
    private WorkerGroup getWorkerGroup(int id) {
        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.setName(GROUP_NAME);
        workerGroup.setId(id);
        return workerGroup;
    }

}
