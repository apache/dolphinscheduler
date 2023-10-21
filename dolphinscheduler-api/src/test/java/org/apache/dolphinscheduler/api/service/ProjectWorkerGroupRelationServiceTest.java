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

import com.google.common.collect.Lists;
import com.sun.org.apache.regexp.internal.RE;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProjectWorkerGroupRelationServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectWorkerGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectWorkerGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectWorkerGroupRelationServiceTest {

    @InjectMocks
    private ProjectWorkerGroupRelationServiceImpl projectWorkerGroupRelationService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectWorkerGroupMapper projectWorkerGroupMapper;

    @Mock
    private WorkerGroupMapper workerGroupMapper;

    protected final static long projectCode = 1L;


    @Test
    public void testAssignWorkerGroupsToProject() {
        User loginUser = getAdminUser();

        Mockito.when(projectMapper.queryByCode(Mockito.any())).thenReturn(null);
        Result result = projectWorkerGroupRelationService.assignWorkerGroupsToProject(loginUser, projectCode, getWorkerGroups());
        Assertions.assertEquals(Status.PROJECT_NOT_EXIST.getCode(), result.getCode());

    }

    private List<String> getWorkerGroups() {
        return Lists.newArrayList("default");
    }


    private User getGeneralUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("userName");
        loginUser.setId(1);
        return loginUser;
    }

    private User getAdminUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setUserName("userName");
        loginUser.setId(1);
        return loginUser;
    }

    private Project getProject() {
        Project project = new Project();
        project.setCode(projectCode);
        project.setId(1);
        project.setName("test");
        project.setUserId(1);
        return project;
    }

    private ProjectWorkerGroup getProjectWorkerGroup() {
        ProjectWorkerGroup projectWorkerGroup = new ProjectWorkerGroup();
        projectWorkerGroup.setId(1);
        projectWorkerGroup.setProjectCode(projectCode);
        projectWorkerGroup.setWorkerGroup("default");
        return projectWorkerGroup;
    }
}
