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

import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.service.impl.WorkFlowLineageServiceImpl;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkFlowLineageMapper;

import java.util.ArrayList;
import java.util.Collection;
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

/**
 * work flow lineage service test
 */
@ExtendWith(MockitoExtension.class)
public class WorkFlowLineageServiceTest {

    @InjectMocks
    private WorkFlowLineageServiceImpl workFlowLineageService;

    @Mock
    private WorkFlowLineageMapper workFlowLineageMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    /**
     * get mock Project
     *
     * @param projectName projectName
     * @return Project
     */
    private Project getProject(String projectName) {
        Project project = new Project();
        project.setCode(1L);
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }

    @Test
    public void testQueryWorkFlowLineageByName() {
        Project project = getProject("test");
        String name = "test";
        when(projectMapper.queryByCode(1L)).thenReturn(project);
        when(workFlowLineageMapper.queryWorkFlowLineageByName(Mockito.anyLong(), Mockito.any()))
                .thenReturn(getWorkFlowLineages());
        Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByName(1L, name);
        List<WorkFlowLineage> workFlowLineageList = (List<WorkFlowLineage>) result.get(Constants.DATA_LIST);
        Assertions.assertTrue(workFlowLineageList.size() > 0);
    }

    @Test
    public void testQueryWorkFlowLineage() {
        Project project = getProject("test");

        List<ProcessLineage> processLineages = new ArrayList<>();
        ProcessLineage processLineage = new ProcessLineage();
        processLineage.setPreTaskVersion(1);
        processLineage.setPreTaskCode(1L);
        processLineage.setPostTaskCode(2L);
        processLineage.setPostTaskVersion(1);
        processLineage.setProcessDefinitionCode(1111L);
        processLineage.setProcessDefinitionVersion(1);
        processLineage.setProjectCode(1111L);
        processLineages.add(processLineage);
        WorkFlowLineage workFlowLineage = new WorkFlowLineage();
        workFlowLineage.setSourceWorkFlowCode("");
        workFlowLineage.setWorkFlowCode(1111L);
        List<WorkFlowLineage> workFlowLineages = new ArrayList<>();
        workFlowLineages.add(workFlowLineage);

        when(projectMapper.queryByCode(1L)).thenReturn(project);
        when(workFlowLineageMapper.queryProcessLineage(project.getCode())).thenReturn(processLineages);
        when(workFlowLineageMapper.queryWorkFlowLineageByLineage(processLineages)).thenReturn(workFlowLineages);

        Map<String, Object> result = workFlowLineageService.queryWorkFlowLineage(1L);

        Map<String, Object> workFlowLists = (Map<String, Object>) result.get(Constants.DATA_LIST);
        Collection<WorkFlowLineage> workFlowLineageList =
                (Collection<WorkFlowLineage>) workFlowLists.get(Constants.WORKFLOW_LIST);
        Set<WorkFlowRelation> workFlowRelations =
                (Set<WorkFlowRelation>) workFlowLists.get(Constants.WORKFLOW_RELATION_LIST);
        Assertions.assertTrue(workFlowLineageList.size() > 0);
        Assertions.assertTrue(workFlowRelations.size() > 0);
    }

    private List<WorkFlowLineage> getWorkFlowLineages() {
        List<WorkFlowLineage> workFlowLineages = new ArrayList<>();
        WorkFlowLineage workFlowLineage = new WorkFlowLineage();
        workFlowLineage.setWorkFlowCode(1);
        workFlowLineage.setWorkFlowName("testdag");
        workFlowLineages.add(workFlowLineage);
        return workFlowLineages;
    }

}
