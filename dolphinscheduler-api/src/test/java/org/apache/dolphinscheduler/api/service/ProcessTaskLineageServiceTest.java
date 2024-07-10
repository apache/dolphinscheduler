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

import org.apache.dolphinscheduler.api.service.impl.ProcessLineageServiceImpl;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskLineage;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationDetail;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessTaskLineageDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

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
public class ProcessTaskLineageServiceTest {

    @InjectMocks
    private ProcessLineageServiceImpl processLineageService;

    @Mock
    private ProcessTaskLineageDao processTaskLineageDao;

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
        when(processTaskLineageDao.queryWorkFlowLineageByName(Mockito.anyLong(), Mockito.any()))
                .thenReturn(getWorkFlowLineages());
        List<WorkFlowRelationDetail> workFlowLineages = processLineageService.queryWorkFlowLineageByName(1L, name);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(workFlowLineages));
    }

    @Test
    public void testQueryWorkFlowLineage() {
        Project project = getProject("test");

        List<ProcessTaskLineage> processTaskLineages = new ArrayList<>();
        ProcessTaskLineage processTaskLineage = new ProcessTaskLineage();
        processTaskLineage.setProcessDefinitionCode(1);
        processTaskLineage.setProcessDefinitionVersion(1);
        processTaskLineage.setTaskDefinitionCode(2L);
        processTaskLineage.setTaskDefinitionVersion(1);
        processTaskLineage.setDeptProjectCode(1111L);
        processTaskLineage.setDeptProcessDefinitionCode(1);
        processTaskLineage.setDeptTaskDefinitionCode(1111L);
        processTaskLineages.add(processTaskLineage);

        List<WorkFlowRelationDetail> workFlowRelationDetailList = new ArrayList<>();
        WorkFlowRelationDetail workFlowRelationDetail = new WorkFlowRelationDetail();
        workFlowRelationDetail.setWorkFlowCode(processTaskLineage.getProcessDefinitionCode());
        workFlowRelationDetail.setWorkFlowName("testProcessDefinitionName");
        workFlowRelationDetailList.add(workFlowRelationDetail);

        when(projectMapper.queryByCode(1L)).thenReturn(project);
        when(processTaskLineageDao.queryByProjectCode(project.getCode())).thenReturn(processTaskLineages);
        when(processTaskLineageDao.queryWorkFlowLineageByCode(processTaskLineage.getProcessDefinitionCode()))
                .thenReturn(workFlowRelationDetailList);

        WorkFlowLineage workFlowLineage = processLineageService.queryWorkFlowLineage(1L);

        List<WorkFlowRelationDetail> workFlowLineageList =
                workFlowLineage.getWorkFlowRelationDetailList();
        List<WorkFlowRelation> workFlowRelations =
                workFlowLineage.getWorkFlowRelationList();
        Assertions.assertTrue(!workFlowLineageList.isEmpty());
        Assertions.assertTrue(!workFlowRelations.isEmpty());
    }

    private List<WorkFlowRelationDetail> getWorkFlowLineages() {
        List<WorkFlowRelationDetail> workFlowLineages = new ArrayList<>();
        WorkFlowRelationDetail workFlowRelationDetail = new WorkFlowRelationDetail();
        workFlowRelationDetail.setWorkFlowCode(1);
        workFlowRelationDetail.setWorkFlowName("testdag");
        workFlowLineages.add(workFlowRelationDetail);
        return workFlowLineages;
    }

}
