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

import static org.apache.dolphinscheduler.api.utils.ServiceTestUtil.getGeneralUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.AssertionsHelper;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProjectParameterServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectParameter;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectParameterMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectParameterServiceTest {

    @InjectMocks
    private ProjectParameterServiceImpl projectParameterService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectParameterMapper projectParameterMapper;

    @Mock
    private ProjectServiceImpl projectService;

    protected final static long projectCode = 1L;

    @Test
    public void testCreateProjectParameter() {
        User loginUser = getGeneralUser();

        // PERMISSION DENIED
        when(projectService.hasProjectAndWritePerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class)))
                .thenReturn(false);
        Result result = projectParameterService.createProjectParameter(loginUser, projectCode, "key", "value",
                DataType.VARCHAR.name());
        assertNull(result.getData());
        assertNull(result.getCode());
        assertNull(result.getMsg());

        when(projectService.hasProjectAndWritePerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class)))
                .thenReturn(true);

        // CODE GENERATION ERROR
        try (MockedStatic<CodeGenerateUtils> ignored = Mockito.mockStatic(CodeGenerateUtils.class)) {
            when(CodeGenerateUtils.genCode()).thenThrow(CodeGenerateUtils.CodeGenerateException.class);

            result = projectParameterService.createProjectParameter(loginUser, projectCode, "key", "value",
                    DataType.VARCHAR.name());
            assertEquals(Status.CREATE_PROJECT_PARAMETER_ERROR.getCode(), result.getCode());
        }

        // PROJECT_PARAMETER_ALREADY_EXISTS
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        when(projectParameterMapper.selectOne(Mockito.any())).thenReturn(getProjectParameter());
        result = projectParameterService.createProjectParameter(loginUser, projectCode, "key", "value",
                DataType.VARCHAR.name());
        assertEquals(Status.PROJECT_PARAMETER_ALREADY_EXISTS.getCode(), result.getCode());

        // INSERT DATA ERROR
        when(projectParameterMapper.selectOne(Mockito.any())).thenReturn(null);
        when(projectParameterMapper.insert(Mockito.any())).thenReturn(-1);
        result = projectParameterService.createProjectParameter(loginUser, projectCode, "key1", "value",
                DataType.VARCHAR.name());
        assertEquals(Status.CREATE_PROJECT_PARAMETER_ERROR.getCode(), result.getCode());

        // SUCCESS
        when(projectParameterMapper.insert(Mockito.any())).thenReturn(1);
        result = projectParameterService.createProjectParameter(loginUser, projectCode, "key1", "value",
                DataType.VARCHAR.name());
        assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    @Test
    public void testUpdateProjectParameter() {
        User loginUser = getGeneralUser();

        // NO PERMISSION
        when(projectService.hasProjectAndWritePerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class)))
                .thenReturn(false);
        Result result = projectParameterService.updateProjectParameter(loginUser, projectCode, 1, "key", "value",
                DataType.VARCHAR.name());
        assertNull(result.getData());
        assertNull(result.getCode());
        assertNull(result.getMsg());

        // PROJECT_PARAMETER_NOT_EXISTS
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        when(projectService.hasProjectAndWritePerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class)))
                .thenReturn(true);
        when(projectParameterMapper.queryByCode(Mockito.anyLong())).thenReturn(null);
        result = projectParameterService.updateProjectParameter(loginUser, projectCode, 1, "key", "value",
                DataType.VARCHAR.name());
        assertEquals(Status.PROJECT_PARAMETER_NOT_EXISTS.getCode(), result.getCode());

        // PROJECT_PARAMETER_ALREADY_EXISTS
        when(projectParameterMapper.queryByCode(Mockito.anyLong())).thenReturn(getProjectParameter());
        when(projectParameterMapper.selectOne(Mockito.any())).thenReturn(getProjectParameter());
        result = projectParameterService.updateProjectParameter(loginUser, projectCode, 1, "key", "value",
                DataType.VARCHAR.name());
        assertEquals(Status.PROJECT_PARAMETER_ALREADY_EXISTS.getCode(), result.getCode());

        // PROJECT_UPDATE_ERROR
        when(projectParameterMapper.selectOne(Mockito.any())).thenReturn(null);
        when(projectParameterMapper.updateById(Mockito.any())).thenReturn(-1);
        result = projectParameterService.updateProjectParameter(loginUser, projectCode, 1, "key1", "value",
                DataType.VARCHAR.name());
        assertEquals(Status.UPDATE_PROJECT_PARAMETER_ERROR.getCode(), result.getCode());

        // SUCCESS
        when(projectParameterMapper.updateById(Mockito.any())).thenReturn(1);
        result = projectParameterService.updateProjectParameter(loginUser, projectCode, 1, "key1", "value",
                DataType.LONG.name());
        assertEquals(Status.SUCCESS.getCode(), result.getCode());
        ProjectParameter projectParameter = (ProjectParameter) result.getData();
        assertNotNull(projectParameter.getOperator());
        assertNotNull(projectParameter.getUpdateTime());
        assertEquals(DataType.LONG.name(), projectParameter.getParamDataType());
    }

    @Test
    public void testDeleteProjectParametersByCode() {
        User loginUser = getGeneralUser();

        // NO PERMISSION
        when(projectService.hasProjectAndWritePerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class)))
                .thenReturn(false);
        Result result = projectParameterService.deleteProjectParametersByCode(loginUser, projectCode, 1);
        assertNull(result.getData());
        assertNull(result.getCode());
        assertNull(result.getMsg());

        // PROJECT_PARAMETER_NOT_EXISTS
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        when(projectService.hasProjectAndWritePerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class)))
                .thenReturn(true);
        when(projectParameterMapper.queryByCode(Mockito.anyLong())).thenReturn(null);
        result = projectParameterService.deleteProjectParametersByCode(loginUser, projectCode, 1);
        assertEquals(Status.PROJECT_PARAMETER_NOT_EXISTS.getCode(), result.getCode());

        // DATABASE OPERATION ERROR
        when(projectParameterMapper.queryByCode(Mockito.anyLong())).thenReturn(getProjectParameter());
        when(projectParameterMapper.deleteById(Mockito.anyInt())).thenReturn(-1);
        result = projectParameterService.deleteProjectParametersByCode(loginUser, projectCode, 1);
        assertEquals(Status.DELETE_PROJECT_PARAMETER_ERROR.getCode(), result.getCode());

        // SUCCESS
        when(projectParameterMapper.deleteById(Mockito.anyInt())).thenReturn(1);
        result = projectParameterService.deleteProjectParametersByCode(loginUser, projectCode, 1);
        assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    @Test
    public void testQueryProjectParameterByCode() {
        User loginUser = getGeneralUser();

        // NO PERMISSION
        when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class),
                Mockito.any()))
                        .thenReturn(false);

        Result result = projectParameterService.queryProjectParameterByCode(loginUser, projectCode, 1);
        assertNull(result.getData());
        assertNull(result.getCode());
        assertNull(result.getMsg());

        // PROJECT_PARAMETER_NOT_EXISTS
        when(projectMapper.queryByCode(projectCode)).thenReturn(getProject(projectCode));
        when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class),
                Mockito.any())).thenReturn(true);
        when(projectParameterMapper.queryByCode(Mockito.anyLong())).thenReturn(null);
        result = projectParameterService.queryProjectParameterByCode(loginUser, projectCode, 1);
        assertEquals(Status.PROJECT_PARAMETER_NOT_EXISTS.getCode(), result.getCode());

        // SUCCESS
        when(projectParameterMapper.queryByCode(Mockito.anyLong())).thenReturn(getProjectParameter());
        result = projectParameterService.queryProjectParameterByCode(loginUser, projectCode, 1);
        assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    @Test
    public void testQueryProjectParameterListPaging() {
        User loginUser = getGeneralUser();
        Integer pageSize = 10;
        Integer pageNo = 1;

        // NO PERMISSION
        when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any(), Mockito.any(Result.class),
                Mockito.any()))
                        .thenReturn(false);

        Result result =
                projectParameterService.queryProjectParameterListPaging(loginUser, projectCode, pageSize, pageNo, null,
                        DataType.VARCHAR.name());
        assertNull(result.getData());
        assertNull(result.getCode());
        assertNull(result.getMsg());

        // SUCCESS
        when(projectService.hasProjectAndPerm(any(), any(), any(Result.class), any()))
                .thenReturn(true);

        Page<ProjectParameter> page = new Page<>(pageNo, pageSize);
        page.setRecords(Collections.singletonList(getProjectParameter()));

        when(projectParameterMapper.queryProjectParameterListPaging(any(), anyLong(), any(), any(), any()))
                .thenReturn(page);
        result = projectParameterService.queryProjectParameterListPaging(loginUser, projectCode, pageSize, pageNo,
                null, null);
        assertEquals(Status.SUCCESS.getCode(), result.getCode());
    }

    @Test
    public void testBatchDeleteProjectParametersByCodes() {
        User loginUser = getGeneralUser();

        Result result = projectParameterService.batchDeleteProjectParametersByCodes(loginUser, projectCode, "");
        assertEquals(Status.PROJECT_PARAMETER_CODE_EMPTY.getCode(), result.getCode());

        when(projectParameterMapper.queryByCodes(any())).thenReturn(Collections.singletonList(getProjectParameter()));

        AssertionsHelper.assertThrowsServiceException(Status.PROJECT_PARAMETER_NOT_EXISTS,
                () -> projectParameterService.batchDeleteProjectParametersByCodes(loginUser, projectCode, "1,2"));

        projectParameterService.batchDeleteProjectParametersByCodes(loginUser, projectCode, "1");
    }

    private Project getProject(long projectCode) {
        Project project = new Project();
        project.setCode(projectCode);
        project.setId(1);
        project.setName("test");
        project.setUserId(1);
        return project;
    }

    private ProjectParameter getProjectParameter() {
        ProjectParameter projectParameter = new ProjectParameter();
        projectParameter.setId(1);
        projectParameter.setCode(1);
        projectParameter.setProjectCode(1);
        projectParameter.setParamName("key");
        projectParameter.setParamValue("value");
        return projectParameter;
    }
}
