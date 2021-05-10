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

package org.apache.dolphinscheduler.api.controller;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.WorkFlowLineageServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

/**
 * work flow lineage controller test
 */
public class WorkFlowLineageControllerTest extends AbstractControllerTest {

    @InjectMocks
    private WorkFlowLineageController workFlowLineageController;

    @Mock
    private WorkFlowLineageServiceImpl workFlowLineageService;

    @Before
    public void init() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        ProjectMapper projectMapper = Mockito.mock(ProjectMapper.class);
        Mockito.when(applicationContext.getBean(ProjectMapper.class)).thenReturn(projectMapper);
        Project project = new Project();
        project.setId(1);
        project.setCode(1L);
        Mockito.when(projectMapper.selectById(1)).thenReturn(project);
    }

    @Test
    public void testQueryWorkFlowLineageByName() {
        int projectId = 1;
        String searchVal = "test";
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, 1);
        Mockito.when(workFlowLineageService.queryWorkFlowLineageByName(searchVal, projectId)).thenReturn(result);
        Result response = workFlowLineageController.queryWorkFlowLineageByName(user, projectId, searchVal);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    @Test
    public  void testQueryWorkFlowLineageByIds() {
        int projectId = 1;
        String ids = "1";
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, 1);
        Set<Integer> idSet = new HashSet<>();
        idSet.add(1);
        Mockito.when(workFlowLineageService.queryWorkFlowLineageByIds(idSet, projectId)).thenReturn(result);
        Result response = workFlowLineageController.queryWorkFlowLineageByIds(user, projectId, ids);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

}
