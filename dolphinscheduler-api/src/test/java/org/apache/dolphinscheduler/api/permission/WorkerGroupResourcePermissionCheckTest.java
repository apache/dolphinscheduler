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

package org.apache.dolphinscheduler.api.permission;

import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class WorkerGroupResourcePermissionCheckTest {

    private static final Logger logger = LoggerFactory.getLogger(WorkerGroupResourcePermissionCheckTest.class);
    @InjectMocks
    private ResourcePermissionCheckServiceImpl.WorkerGroupResourcePermissionCheck workerGroupResourcePermissionCheck;

    @Mock
    private WorkerGroupMapper workerGroupMapper;

    @Test
    public void testPermissionCheck() {
        User user = getLoginUser();
        Assertions.assertFalse(workerGroupResourcePermissionCheck.permissionCheck(user.getId(), null, logger));
    }

    @Test
    public void testAuthorizationTypes() {
        List<AuthorizationType> authorizationTypes = workerGroupResourcePermissionCheck.authorizationTypes();
        Assertions.assertEquals(Collections.singletonList(AuthorizationType.WORKER_GROUP), authorizationTypes);
    }

    @Test
    public void testListAuthorizedResourceIds() {
        User user = getLoginUser();
        WorkerGroup workerGroup = new WorkerGroup();
        Set<Integer> ids = new HashSet();
        ids.add(workerGroup.getId());
        List<WorkerGroup> workerGroups = Arrays.asList(workerGroup);

        Mockito.when(workerGroupMapper.queryAllWorkerGroup()).thenReturn(workerGroups);

        Assertions.assertEquals(ids,
                workerGroupResourcePermissionCheck.listAuthorizedResourceIds(user.getId(), logger));
    }

    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("test");
        loginUser.setId(1);
        return loginUser;
    }
}
