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
package org.apache.dolphinscheduler.service.permission;


import com.google.common.collect.Lists;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * permission service test
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourcePermissionCheckServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ResourcePermissionCheckServiceTest.class);

    @Mock
    private ProcessService processService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @Test
    public void testResourcePermissionCheck(){
        User user = new User();
        user.setId(1);
        Object[] obj = new Object[]{1,2};
        boolean result = this.resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.PROJECTS, obj, user.getId(), logger);
        Assert.assertFalse(result);
    }

    @Test
    public void testOperationPermissionCheck(){
        User user = new User();
        user.setId(1);
        Assert.assertFalse(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.PROJECTS, user.getId(), null, logger));
    }

    @Test
    public void testUserOwnedResourceIdsAcquisition(){
        User user = new User();
        user.setId(1);
        //ADMIN
        user.setUserType(UserType.ADMIN_USER);
        Object[] obj = new Object[]{1,2};
        List<Project> projectList = Lists.newArrayList(this.getEntity());
        User user1 = processService.getUserById(1);
        Set result = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.PROJECTS,
                user.getId(),
                logger);
        Assert.assertNotNull(result);
    }

    /**
     * create entity
     */
    private Project getEntity() {
        Project project = new Project();
        project.setId(1);
        project.setUserId(1);
        project.setName("permissionsTest");
        project.setUserName("permissionTest");
        return project;
    }

    /**
     * entity list
     */
    private List<Project> getList() {
        List<Project> list = new ArrayList<>();
        list.add(getEntity());
        return list;
    }
}
