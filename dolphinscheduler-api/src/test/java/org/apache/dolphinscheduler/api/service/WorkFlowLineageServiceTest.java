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
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkFlowLineageMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * work flow lineage service test
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkFlowLineageServiceTest {

    @InjectMocks
    private WorkFlowLineageServiceImpl workFlowLineageService;

    @Mock
    private WorkFlowLineageMapper workFlowLineageMapper;

    @Mock
    private ProjectMapper projectMapper;

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
        String searchVal = "test";
        when(projectMapper.selectById(1)).thenReturn(project);
        when(workFlowLineageMapper.queryByName(Mockito.any(), Mockito.any())).thenReturn(getWorkFlowLineages());
        Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByName(searchVal, 1);
        List<WorkFlowLineage> workFlowLineageList = (List<WorkFlowLineage>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(workFlowLineageList.size() > 0);
    }

    @Test
    public void testQueryWorkFlowLineageByIds() {
        Set<Integer> ids = new HashSet<>();
        ids.add(1);
        ids.add(2);

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
        workFlowLineage.setSourceWorkFlowId("");

        when(projectMapper.selectById(1)).thenReturn(project);
        when(workFlowLineageMapper.queryRelationByIds(ids, project.getCode())).thenReturn(processLineages);
        when(workFlowLineageMapper.queryCodeRelation(processLineage.getPostTaskCode()
                , processLineage.getPreTaskVersion()
                , processLineage.getProcessDefinitionCode()
                , processLineage.getProjectCode()))
                .thenReturn(processLineages);
        when(workFlowLineageMapper
                .queryWorkFlowLineageByCode(processLineage.getProcessDefinitionCode(), processLineage.getProjectCode()))
                .thenReturn(workFlowLineage);

        Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByIds(ids, 1);

        Map<String, Object> workFlowLists = (Map<String, Object>) result.get(Constants.DATA_LIST);
        Collection<WorkFlowLineage> workFlowLineages = (Collection<WorkFlowLineage>) workFlowLists.get(Constants.WORKFLOW_LIST);
        Set<WorkFlowRelation> workFlowRelations = (Set<WorkFlowRelation>) workFlowLists.get(Constants.WORKFLOW_RELATION_LIST);
        Assert.assertTrue(workFlowLineages.size() > 0);
        Assert.assertTrue(workFlowRelations.size() > 0);
    }

    private List<WorkFlowLineage> getWorkFlowLineages() {
        List<WorkFlowLineage> workFlowLineages = new ArrayList<>();
        WorkFlowLineage workFlowLineage = new WorkFlowLineage();
        workFlowLineage.setWorkFlowId(1);
        workFlowLineage.setWorkFlowName("testdag");
        workFlowLineages.add(workFlowLineage);
        return workFlowLineages;
    }

}
