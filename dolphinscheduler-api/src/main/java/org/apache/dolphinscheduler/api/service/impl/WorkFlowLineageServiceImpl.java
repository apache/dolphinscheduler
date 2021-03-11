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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.WorkFlowLineageService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkFlowLineageMapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * work flow lineage service impl
 */
@Service
public class WorkFlowLineageServiceImpl extends BaseServiceImpl implements WorkFlowLineageService {

    @Autowired
    private WorkFlowLineageMapper workFlowLineageMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public Map<String, Object> queryWorkFlowLineageByName(String workFlowName, int projectId) {
        Project project = projectMapper.selectById(projectId);
        Map<String, Object> result = new HashMap<>();
        List<WorkFlowLineage> workFlowLineageList = workFlowLineageMapper.queryByName(workFlowName, project.getCode());
        result.put(Constants.DATA_LIST, workFlowLineageList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private void getRelation(Map<Integer, WorkFlowLineage> workFlowLineageMap,
                             Set<WorkFlowRelation> workFlowRelations,
                             ProcessLineage processLineage) {
        List<ProcessLineage> relations = workFlowLineageMapper.queryCodeRelation(
                processLineage.getPostTaskCode(), processLineage.getPostTaskVersion()
                , processLineage.getProcessDefinitionCode());

        for (ProcessLineage relation : relations) {
            if (relation.getProcessDefinitionCode() != null) {

                relation.setPreTaskCode(processLineage.getPostTaskCode());
                relation.setPreTaskVersion(processLineage.getPostTaskVersion());

                WorkFlowLineage pre = workFlowLineageMapper
                        .queryWorkFlowLineageByCode(processLineage.getProcessDefinitionCode(), processLineage.getProjectCode());
                // sourceWorkFlowId = ""
                if (!workFlowLineageMap.containsKey(pre.getWorkFlowId())) {
                    workFlowLineageMap.put(pre.getWorkFlowId(), pre);
                }

                WorkFlowLineage post = workFlowLineageMapper
                        .queryWorkFlowLineageByCode(relation.getProcessDefinitionCode(), relation.getProjectCode());

                if (workFlowLineageMap.containsKey(post.getWorkFlowId())) {
                    WorkFlowLineage workFlowLineage = workFlowLineageMap.get(post.getWorkFlowId());
                    String sourceWorkFlowId = workFlowLineage.getSourceWorkFlowId();
                    if (sourceWorkFlowId.equals("")) {
                        workFlowLineage.setSourceWorkFlowId(String.valueOf(pre.getWorkFlowId()));
                    } else {
                        workFlowLineage.setSourceWorkFlowId(sourceWorkFlowId + "," + pre.getWorkFlowId());
                    }

                } else {
                    post.setSourceWorkFlowId(String.valueOf(pre.getWorkFlowId()));
                    workFlowLineageMap.put(post.getWorkFlowId(), post);
                }

                WorkFlowRelation workFlowRelation = new WorkFlowRelation();
                workFlowRelation.setSourceWorkFlowId(pre.getWorkFlowId());
                workFlowRelation.setTargetWorkFlowId(post.getWorkFlowId());
                if (workFlowRelations.contains(workFlowRelation)) {
                    continue;
                }
                workFlowRelations.add(workFlowRelation);
                getRelation(workFlowLineageMap, workFlowRelations, relation);
            }
        }

    }

    @Override
    public Map<String, Object> queryWorkFlowLineageByIds(Set<Integer> ids, int projectId) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.selectById(projectId);
        List<ProcessLineage> processLineages = workFlowLineageMapper.queryRelationByIds(ids, project.getCode());

        Map<Integer, WorkFlowLineage> workFlowLineages = new HashMap<>();
        Set<WorkFlowRelation> workFlowRelations = new HashSet<>();

        for (ProcessLineage processLineage : processLineages) {
            getRelation(workFlowLineages, workFlowRelations, processLineage);
        }

        Map<String, Object> workFlowLists = new HashMap<>();
        workFlowLists.put(Constants.WORKFLOW_LIST, workFlowLineages.values());
        workFlowLists.put(Constants.WORKFLOW_RELATION_LIST, workFlowRelations);
        result.put(Constants.DATA_LIST, workFlowLists);
        putMsg(result, Status.SUCCESS);
        return result;
    }

}
