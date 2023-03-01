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
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;

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
public class UdfFuncPermissionCheckTest {

    private static final Logger logger = LoggerFactory.getLogger(UdfFuncPermissionCheckTest.class);
    @InjectMocks
    private ResourcePermissionCheckServiceImpl.UdfFuncPermissionCheck udfFuncPermissionCheck;

    @Mock
    private UdfFuncMapper udfFuncMapper;

    @Test
    public void testPermissionCheck() {
        User user = getLoginUser();
        Assertions.assertTrue(udfFuncPermissionCheck.permissionCheck(user.getId(), null, logger));
    }

    @Test
    public void testAuthorizationTypes() {
        List<AuthorizationType> authorizationTypes = udfFuncPermissionCheck.authorizationTypes();
        Assertions.assertEquals(Collections.singletonList(AuthorizationType.UDF), authorizationTypes);
    }

    @Test
    public void testListAuthorizedResourceIds() {
        User user = getLoginUser();
        UdfFunc udfFunc = new UdfFunc();
        Set<Integer> ids = new HashSet();
        ids.add(udfFunc.getId());
        List<UdfFunc> udfFuncs = Arrays.asList(udfFunc);

        Mockito.when(udfFuncMapper.listAuthorizedUdfByUserId(user.getId())).thenReturn(udfFuncs);

        Assertions.assertEquals(ids, udfFuncPermissionCheck.listAuthorizedResourceIds(user.getId(), logger));
    }

    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("test");
        loginUser.setId(1);
        return loginUser;
    }

    private Project getProject() {
        Project project = new Project();
        project.setCode(1L);
        project.setId(1);
        project.setName("projectName");
        project.setUserId(1);
        return project;
    }
}
