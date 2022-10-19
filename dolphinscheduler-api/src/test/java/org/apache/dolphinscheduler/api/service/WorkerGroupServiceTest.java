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

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.WorkerGroupServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ProfileType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(value = {ProfileType.H2})
@SpringBootTest(classes = ApiApplicationServer.class)
public class WorkerGroupServiceTest {

    @MockBean(name = "registryClient")
    private RegistryClient registryClient;

    @Autowired
    private WorkerGroupServiceImpl workerGroupService;

    @MockBean(name = "workerGroupMapper")
    private WorkerGroupMapper workerGroupMapper;

    @MockBean(name = "processInstanceMapper")
    private ProcessInstanceMapper processInstanceMapper;

    private String groupName = "groupName000001";

    private User loginUSer;

    @BeforeEach
    public void init() {
        loginUSer = new User();
        loginUSer.setUserType(UserType.ADMIN_USER);
    }

    @Test
    public void testQueryAllGroup() {
        Map<String, Object> result = workerGroupService.queryAllGroup(loginUSer);
        List<String> workerGroups = (List<String>) result.get(Constants.DATA_LIST);
        Assertions.assertEquals(workerGroups.size(), 1);
    }

    /**
     * delete group by id
     */
    @Test
    public void testDeleteWorkerGroupById() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        WorkerGroup wg2 = getWorkerGroup(2);
        Mockito.when(workerGroupMapper.selectById(2)).thenReturn(wg2);
        Mockito.when(processInstanceMapper.queryByWorkerGroupNameAndStatus(wg2.getName(), org.apache.dolphinscheduler.service.utils.Constants.NOT_TERMINATED_STATES)).thenReturn(getProcessInstanceList());
        Map<String, Object> result = workerGroupService.deleteWorkerGroupById(user, 1);
        Assertions.assertEquals(Status.DELETE_WORKER_GROUP_NOT_EXIST.getCode(),
                ((Status) result.get(Constants.STATUS)).getCode());
        result = workerGroupService.deleteWorkerGroupById(user, 2);
        Assertions.assertEquals(Status.DELETE_WORKER_GROUP_BY_ID_FAIL.getCode(),
                ((Status) result.get(Constants.STATUS)).getCode());
        // correct
        WorkerGroup wg3 = getWorkerGroup(3);
        Mockito.when(workerGroupMapper.selectById(3)).thenReturn(wg3);
        Mockito.when(processInstanceMapper.queryByWorkerGroupNameAndStatus(wg3.getName(), org.apache.dolphinscheduler.service.utils.Constants.NOT_TERMINATED_STATES)).thenReturn(new ArrayList<>());
        result = workerGroupService.deleteWorkerGroupById(user, 3);
        Assertions.assertEquals(Status.SUCCESS.getMsg(), result.get(Constants.MSG));
    }

    /**
     * get processInstances
     */
    private List<ProcessInstance> getProcessInstanceList() {
        List<ProcessInstance> processInstances = new ArrayList<>();
        processInstances.add(new ProcessInstance());
        return processInstances;
    }

    @Test
    public void testQueryAllGroupWithDefault() {
        Map<String, Object> result = workerGroupService.queryAllGroup(loginUSer);
        List<String> workerGroups = (List<String>) result.get(Constants.DATA_LIST);
        Assertions.assertEquals(1, workerGroups.size());
        Assertions.assertEquals("default", workerGroups.toArray()[0]);
    }

    /**
     * get Group
     */
    private WorkerGroup getWorkerGroup(int id) {
        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.setName(groupName);
        workerGroup.setId(id);
        return workerGroup;
    }

    private WorkerGroup getWorkerGroup() {
        return getWorkerGroup(1);
    }

    private List<WorkerGroup> getList() {
        List<WorkerGroup> list = new ArrayList<>();
        list.add(getWorkerGroup());
        return list;
    }

}
