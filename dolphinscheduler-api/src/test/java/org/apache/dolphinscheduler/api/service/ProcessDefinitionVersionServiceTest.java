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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProcessDefinitionVersionServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionVersion;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionVersionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class ProcessDefinitionVersionServiceTest {

    @InjectMocks
    private ProcessDefinitionVersionServiceImpl processDefinitionVersionService;

    @Mock
    private ProcessDefinitionVersionMapper processDefinitionVersionMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Test
    public void testAddProcessDefinitionVersion() {
        long expectedVersion = 5L;
        ProcessDefinition processDefinition = getProcessDefinition();
        Mockito.when(processDefinitionVersionMapper
                .queryMaxVersionByProcessDefinitionId(processDefinition.getId()))
                .thenReturn(expectedVersion);

        long version = processDefinitionVersionService.addProcessDefinitionVersion(processDefinition);

        Assert.assertEquals(expectedVersion + 1, version);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testQueryProcessDefinitionVersions() {
        // pageNo <= 0
        int pageNo = -1;
        int pageSize = 10;
        int processDefinitionId = 66;

        String projectName = "project_test1";
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> resultMap1 = processDefinitionVersionService.queryProcessDefinitionVersions(
                loginUser
                , projectName
                , pageNo
                , pageSize
                , processDefinitionId);
        Assert.assertEquals(Status.QUERY_PROCESS_DEFINITION_VERSIONS_PAGE_NO_OR_PAGE_SIZE_LESS_THAN_1_ERROR
                , resultMap1.get(Constants.STATUS));

        // pageSize <= 0
        pageNo = 1;
        pageSize = -1;
        Map<String, Object> resultMap2 = processDefinitionVersionService.queryProcessDefinitionVersions(
                loginUser
                , projectName
                , pageNo
                , pageSize
                , processDefinitionId);
        Assert.assertEquals(Status.QUERY_PROCESS_DEFINITION_VERSIONS_PAGE_NO_OR_PAGE_SIZE_LESS_THAN_1_ERROR
                , resultMap2.get(Constants.STATUS));

        Map<String, Object> res = new HashMap<>();
        putMsg(res, Status.PROJECT_NOT_FOUNT);
        Project project = getProject(projectName);
        Mockito.when(projectMapper.queryByName(projectName))
                .thenReturn(project);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName))
                .thenReturn(res);

        // project auth fail
        pageNo = 1;
        pageSize = 10;
        Map<String, Object> resultMap3 = processDefinitionVersionService.queryProcessDefinitionVersions(
                loginUser
                , projectName
                , pageNo
                , pageSize
                , processDefinitionId);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, resultMap3.get(Constants.STATUS));

        putMsg(res, Status.SUCCESS);

        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName))
                .thenReturn(res);

        ProcessDefinitionVersion processDefinitionVersion = getProcessDefinitionVersion(getProcessDefinition());

        Mockito.when(processDefinitionVersionMapper
                .queryProcessDefinitionVersionsPaging(Mockito.any(Page.class), Mockito.eq(processDefinitionId)))
                .thenReturn(new Page<ProcessDefinitionVersion>()
                        .setRecords(Lists.newArrayList(processDefinitionVersion)));

        Map<String, Object> resultMap4 = processDefinitionVersionService.queryProcessDefinitionVersions(
                loginUser
                , projectName
                , pageNo
                , pageSize
                , processDefinitionId);
        Assert.assertEquals(Status.SUCCESS, resultMap4.get(Constants.STATUS));
        Assert.assertEquals(processDefinitionVersion
                , ((PageInfo<ProcessDefinitionVersion>) resultMap4.get(Constants.DATA_LIST))
                        .getLists().get(0));
    }

    @Test
    public void testQueryByProcessDefinitionIdAndVersion() {

        ProcessDefinitionVersion expectedProcessDefinitionVersion =
                getProcessDefinitionVersion(getProcessDefinition());

        int processDefinitionId = 66;
        long version = 10;
        Mockito.when(processDefinitionVersionMapper.queryByProcessDefinitionIdAndVersion(processDefinitionId, version))
                .thenReturn(expectedProcessDefinitionVersion);

        ProcessDefinitionVersion processDefinitionVersion = processDefinitionVersionService
                .queryByProcessDefinitionIdAndVersion(processDefinitionId, version);

        Assert.assertEquals(expectedProcessDefinitionVersion, processDefinitionVersion);
    }

    @Test
    public void testDeleteByProcessDefinitionIdAndVersion() {
        String projectName = "project_test1";
        int processDefinitionId = 66;
        long version = 10;
        Project project = getProject(projectName);
        Mockito.when(projectMapper.queryByName(projectName))
                .thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        // project auth fail
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName))
                .thenReturn(new HashMap<>());

        Map<String, Object> resultMap1 = processDefinitionVersionService.deleteByProcessDefinitionIdAndVersion(
                loginUser
                , projectName
                , processDefinitionId
                , version);

        Assert.assertEquals(0, resultMap1.size());

        Map<String, Object> res = new HashMap<>();
        putMsg(res, Status.SUCCESS);

        Mockito.when(processDefinitionVersionMapper.deleteByProcessDefinitionIdAndVersion(processDefinitionId, version))
                .thenReturn(1);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName))
                .thenReturn(res);

        Map<String, Object> resultMap2 = processDefinitionVersionService.deleteByProcessDefinitionIdAndVersion(
                loginUser
                , projectName
                , processDefinitionId
                , version);

        Assert.assertEquals(Status.SUCCESS, resultMap2.get(Constants.STATUS));

    }

    /**
     * get mock processDefinitionVersion by processDefinition
     *
     * @return processDefinitionVersion
     */
    private ProcessDefinitionVersion getProcessDefinitionVersion(ProcessDefinition processDefinition) {
        return ProcessDefinitionVersion
            .newBuilder()
            .processDefinitionId(processDefinition.getId())
            .version(1)
            .processDefinitionJson(processDefinition.getProcessDefinitionJson())
            .description(processDefinition.getDescription())
            .locations(processDefinition.getLocations())
            .connects(processDefinition.getConnects())
            .timeout(processDefinition.getTimeout())
            .globalParams(processDefinition.getGlobalParams())
            .createTime(processDefinition.getUpdateTime())
            .warningGroupId(processDefinition.getWarningGroupId())
                .resourceIds(processDefinition.getResourceIds())
                .build();
    }

    /**
     * get mock processDefinition
     *
     * @return ProcessDefinition
     */
    private ProcessDefinition getProcessDefinition() {

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(66);
        processDefinition.setName("test_pdf");
        processDefinition.setProjectId(2);
        processDefinition.setTenantId(1);
        processDefinition.setDescription("");

        return processDefinition;
    }

    /**
     * get mock Project
     *
     * @param projectName projectName
     * @return Project
     */
    private Project getProject(String projectName) {
        Project project = new Project();
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }
}