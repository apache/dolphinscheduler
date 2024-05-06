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

import static org.apache.dolphinscheduler.api.utils.ServiceTestUtil.getAdminUser;
import static org.apache.dolphinscheduler.api.utils.ServiceTestUtil.getGeneralUser;

import org.apache.dolphinscheduler.api.AssertionsHelper;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProjectWorkerGroupRelationServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectWorkerGroup;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectWorkerGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.common.collect.Lists;

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

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private ScheduleMapper scheduleMapper;

    protected final static long projectCode = 1L;

    @Test
    public void testAssignWorkerGroupsToProject() {
        User generalUser = getGeneralUser();
        User loginUser = getAdminUser();

        // no permission
        Result result = projectWorkerGroupRelationService.assignWorkerGroupsToProject(generalUser, projectCode,
                getWorkerGroups());
        Assertions.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), result.getCode());

        // project code is null
        result = projectWorkerGroupRelationService.assignWorkerGroupsToProject(loginUser, null,
                getWorkerGroups());
        Assertions.assertEquals(Status.PROJECT_NOT_EXIST.getCode(), result.getCode());

        // worker group is empty
        result = projectWorkerGroupRelationService.assignWorkerGroupsToProject(loginUser, projectCode,
                Collections.emptyList());
        Assertions.assertEquals(Status.WORKER_GROUP_TO_PROJECT_IS_EMPTY.getCode(), result.getCode());

        // project not exists
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(null);
        result = projectWorkerGroupRelationService.assignWorkerGroupsToProject(loginUser, projectCode,
                getWorkerGroups());
        Assertions.assertEquals(Status.PROJECT_NOT_EXIST.getCode(), result.getCode());

        // worker group not exists
        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.setName("test");
        Mockito.when(projectMapper.queryByCode(Mockito.anyLong())).thenReturn(getProject());
        Mockito.when(workerGroupMapper.queryAllWorkerGroup()).thenReturn(Collections.singletonList(workerGroup));
        result = projectWorkerGroupRelationService.assignWorkerGroupsToProject(loginUser, projectCode,
                getDiffWorkerGroups());
        Assertions.assertEquals(Status.WORKER_GROUP_NOT_EXIST.getCode(), result.getCode());

        // db insertion fail
        Mockito.when(workerGroupMapper.queryAllWorkerGroup()).thenReturn(Collections.singletonList(workerGroup));
        Mockito.when(projectWorkerGroupMapper.insert(Mockito.any())).thenReturn(-1);
        AssertionsHelper.assertThrowsServiceException(Status.ASSIGN_WORKER_GROUP_TO_PROJECT_ERROR,
                () -> projectWorkerGroupRelationService.assignWorkerGroupsToProject(loginUser, projectCode,
                        getWorkerGroups()));

        // success
        Mockito.when(projectWorkerGroupMapper.insert(Mockito.any())).thenReturn(1);

        result = projectWorkerGroupRelationService.assignWorkerGroupsToProject(loginUser, projectCode,
                getWorkerGroups());
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());

        // success when there is diff between current wg and assigned wg
        Mockito.when(projectWorkerGroupMapper.selectList(Mockito.any()))
                .thenReturn(Collections.singletonList(getDiffProjectWorkerGroup()));
        Mockito.when(projectWorkerGroupMapper.delete(Mockito.any())).thenReturn(1);
        result = projectWorkerGroupRelationService.assignWorkerGroupsToProject(loginUser, projectCode,
                getWorkerGroups());
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode());

        // db deletion fail
        Mockito.when(projectWorkerGroupMapper.delete(Mockito.any())).thenReturn(-1);
        AssertionsHelper.assertThrowsServiceException(Status.ASSIGN_WORKER_GROUP_TO_PROJECT_ERROR,
                () -> projectWorkerGroupRelationService.assignWorkerGroupsToProject(loginUser, projectCode,
                        getWorkerGroups()));

        // fail when wg is referenced by task definition
        Mockito.when(taskDefinitionMapper.queryAllDefinitionList(Mockito.anyLong()))
                .thenReturn(Collections.singletonList(getTaskDefinitionWithDiffWorkerGroup()));
        AssertionsHelper.assertThrowsServiceException(Status.USED_WORKER_GROUP_EXISTS,
                () -> projectWorkerGroupRelationService.assignWorkerGroupsToProject(loginUser, projectCode,
                        getWorkerGroups()));
    }

    @Test
    public void testQueryWorkerGroupsByProject() {
        // no permission
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any(), Mockito.anyMap(), Mockito.any()))
                .thenReturn(false);

        Map<String, Object> result =
                projectWorkerGroupRelationService.queryWorkerGroupsByProject(getGeneralUser(), projectCode);

        Assertions.assertTrue(result.isEmpty());

        // success
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any(), Mockito.anyMap(), Mockito.any()))
                .thenReturn(true);

        Mockito.when(projectMapper.queryByCode(projectCode))
                .thenReturn(getProject());

        Mockito.when(projectWorkerGroupMapper.selectList(Mockito.any()))
                .thenReturn(Lists.newArrayList(getProjectWorkerGroup()));

        Mockito.when(taskDefinitionMapper.queryAllDefinitionList(Mockito.anyLong()))
                .thenReturn(new ArrayList<>());

        Mockito.when(scheduleMapper.querySchedulerListByProjectName(Mockito.any()))
                .thenReturn(Lists.newArrayList());

        result = projectWorkerGroupRelationService.queryWorkerGroupsByProject(getGeneralUser(), projectCode);

        ProjectWorkerGroup[] actualValue =
                ((List<ProjectWorkerGroup>) result.get(Constants.DATA_LIST)).toArray(new ProjectWorkerGroup[0]);

        Assertions.assertEquals(actualValue[0].getWorkerGroup(), getProjectWorkerGroup().getWorkerGroup());
    }

    private List<String> getWorkerGroups() {
        return Lists.newArrayList("default");
    }

    private List<String> getDiffWorkerGroups() {
        return Lists.newArrayList("default", "new");
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

    private ProjectWorkerGroup getDiffProjectWorkerGroup() {
        ProjectWorkerGroup projectWorkerGroup = new ProjectWorkerGroup();
        projectWorkerGroup.setId(2);
        projectWorkerGroup.setProjectCode(projectCode);
        projectWorkerGroup.setWorkerGroup("new");
        return projectWorkerGroup;
    }

    private TaskDefinition getTaskDefinitionWithDiffWorkerGroup() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setProjectCode(projectCode);
        taskDefinition.setId(1);
        taskDefinition.setWorkerGroup("new");
        return taskDefinition;
    }
}
