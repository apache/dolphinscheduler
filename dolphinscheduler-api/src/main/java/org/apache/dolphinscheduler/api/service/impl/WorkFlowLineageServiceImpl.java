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
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkFlowLineageMapper;

import org.apache.commons.lang.StringUtils;

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
    private ProjectMapper projectMapper;

    @Override
    public Map<String, Object> queryWorkFlowLineageByName(long projectCode, String workFlowName) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);
            return result;
        }
        List<WorkFlowLineage> workFlowLineageList = workFlowLineageMapper.queryWorkFlowLineageByName(projectCode, workFlowName);
        result.put(Constants.DATA_LIST, workFlowLineageList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Map<String, Object> queryWorkFlowLineageByCode(long projectCode, long workFlowCode) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);
            return result;
        }
        WorkFlowLineage workFlowLineage = workFlowLineageMapper.queryWorkFlowLineageByCode(projectCode, workFlowCode);
        result.put(Constants.DATA_LIST, workFlowLineage);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Map<String, Object> queryWorkFlowLineage(long projectCode) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            putMsg(result, Status.PROJECT_NOT_FOUNT, projectCode);
            return result;
        }
        List<ProcessLineage> processLineages = workFlowLineageMapper.queryProcessLineage(projectCode);

        Map<Long, WorkFlowLineage> workFlowLineages = new HashMap<>();
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

    private void getRelation(Map<Long, WorkFlowLineage> workFlowLineageMap,
                             Set<WorkFlowRelation> workFlowRelations,
                             ProcessLineage processLineage) {
        List<ProcessLineage> relations = workFlowLineageMapper.queryCodeRelation(processLineage.getProjectCode(),
            processLineage.getProcessDefinitionCode(), processLineage.getPostTaskCode(), processLineage.getPostTaskVersion());
        if (!relations.isEmpty()) {
            Set<Long> preWorkFlowCodes = new HashSet<>();
            List<ProcessLineage> preRelations = workFlowLineageMapper.queryCodeRelation(processLineage.getProjectCode(),
                processLineage.getProcessDefinitionCode(), processLineage.getPreTaskCode(), processLineage.getPreTaskVersion());
            for (ProcessLineage preRelation : preRelations) {
                preWorkFlowCodes.add(preRelation.getProcessDefinitionCode());
            }
            ProcessLineage postRelation = relations.get(0);
            WorkFlowLineage post = workFlowLineageMapper.queryWorkFlowLineageByCode(postRelation.getProjectCode(), postRelation.getProcessDefinitionCode());
            preWorkFlowCodes.remove(post.getWorkFlowCode());
            if (!workFlowLineageMap.containsKey(post.getWorkFlowCode())) {
                post.setSourceWorkFlowCode(StringUtils.join(preWorkFlowCodes, ","));
                workFlowLineageMap.put(post.getWorkFlowCode(), post);
            } else {
                WorkFlowLineage workFlowLineage = workFlowLineageMap.get(post.getWorkFlowCode());
                String sourceWorkFlowCode = workFlowLineage.getSourceWorkFlowCode();
                if (StringUtils.isBlank(sourceWorkFlowCode)) {
                    post.setSourceWorkFlowCode(StringUtils.join(preWorkFlowCodes, ","));
                } else {
                    if (!preWorkFlowCodes.isEmpty()) {
                        workFlowLineage.setSourceWorkFlowCode(sourceWorkFlowCode + "," + StringUtils.join(preWorkFlowCodes, ","));
                    }
                }
            }
            if (preWorkFlowCodes.isEmpty()) {
                workFlowRelations.add(new WorkFlowRelation(0L, post.getWorkFlowCode()));
            } else {
                for (long workFlowCode : preWorkFlowCodes) {
                    workFlowRelations.add(new WorkFlowRelation(workFlowCode, post.getWorkFlowCode()));
                }
            }
        }
    }
}
