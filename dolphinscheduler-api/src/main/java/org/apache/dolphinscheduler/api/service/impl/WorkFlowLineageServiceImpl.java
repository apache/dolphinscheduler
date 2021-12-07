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
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkFlowLineageMapper;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.curator.shaded.com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

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
        Map<Long, WorkFlowLineage> workFlowLineagesMap = new HashMap<>();
        Set<WorkFlowRelation> workFlowRelations = new HashSet<>();
        Set<Long> sourceWorkFlowCodes = Sets.newHashSet(workFlowCode);
        recursiveWorkFlow(projectCode, workFlowLineagesMap, workFlowRelations, sourceWorkFlowCodes);
        Map<String, Object> workFlowLists = new HashMap<>();
        workFlowLists.put(Constants.WORKFLOW_LIST, workFlowLineagesMap.values());
        workFlowLists.put(Constants.WORKFLOW_RELATION_LIST, workFlowRelations);
        result.put(Constants.DATA_LIST, workFlowLists);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private void recursiveWorkFlow(long projectCode,
                                   Map<Long, WorkFlowLineage> workFlowLineagesMap,
                                   Set<WorkFlowRelation> workFlowRelations,
                                   Set<Long> sourceWorkFlowCodes) {
        for (Long workFlowCode : sourceWorkFlowCodes) {
            WorkFlowLineage workFlowLineage = workFlowLineageMapper.queryWorkFlowLineageByCode(projectCode, workFlowCode);
            workFlowLineagesMap.put(workFlowCode, workFlowLineage);
            List<ProcessLineage> processLineages = workFlowLineageMapper.queryProcessLineageByCode(projectCode, workFlowCode);
            List<TaskDefinition> taskDefinitionList = new ArrayList<>();
            for (ProcessLineage processLineage : processLineages) {
                if (processLineage.getPreTaskCode() > 0) {
                    taskDefinitionList.add(new TaskDefinition(processLineage.getPreTaskCode(), processLineage.getPreTaskVersion()));
                }
                if (processLineage.getPostTaskCode() > 0) {
                    taskDefinitionList.add(new TaskDefinition(processLineage.getPostTaskCode(), processLineage.getPostTaskVersion()));
                }
            }
            sourceWorkFlowCodes = querySourceWorkFlowCodes(projectCode, workFlowCode, taskDefinitionList);
            if (sourceWorkFlowCodes.isEmpty()) {
                workFlowRelations.add(new WorkFlowRelation(0L, workFlowCode));
                return;
            } else {
                workFlowLineagesMap.get(workFlowCode).setSourceWorkFlowCode(StringUtils.join(sourceWorkFlowCodes, Constants.COMMA));
                sourceWorkFlowCodes.forEach(code -> workFlowRelations.add(new WorkFlowRelation(code, workFlowCode)));
                recursiveWorkFlow(projectCode, workFlowLineagesMap, workFlowRelations, sourceWorkFlowCodes);
            }
        }
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
        Map<Long, WorkFlowLineage> workFlowLineagesMap = new HashMap<>();
        Set<WorkFlowRelation> workFlowRelations = new HashSet<>();
        if (!processLineages.isEmpty()) {
            List<WorkFlowLineage> workFlowLineages = workFlowLineageMapper.queryWorkFlowLineageByLineage(processLineages);
            workFlowLineagesMap = workFlowLineages.stream().collect(Collectors.toMap(WorkFlowLineage::getWorkFlowCode, workFlowLineage -> workFlowLineage));
            Map<Long, List<TaskDefinition>> workFlowMap = new HashMap<>();
            for (ProcessLineage processLineage : processLineages) {
                workFlowMap.compute(processLineage.getProcessDefinitionCode(), (k, v) -> {
                    if (v == null) {
                        v = new ArrayList<>();
                    }
                    if (processLineage.getPreTaskCode() > 0) {
                        v.add(new TaskDefinition(processLineage.getPreTaskCode(), processLineage.getPreTaskVersion()));
                    }
                    if (processLineage.getPostTaskCode() > 0) {
                        v.add(new TaskDefinition(processLineage.getPostTaskCode(), processLineage.getPostTaskVersion()));
                    }
                    return v;
                });
            }
            for (Entry<Long, List<TaskDefinition>> workFlow : workFlowMap.entrySet()) {
                Set<Long> sourceWorkFlowCodes = querySourceWorkFlowCodes(projectCode, workFlow.getKey(), workFlow.getValue());
                if (sourceWorkFlowCodes.isEmpty()) {
                    workFlowRelations.add(new WorkFlowRelation(0L, workFlow.getKey()));
                } else {
                    workFlowLineagesMap.get(workFlow.getKey()).setSourceWorkFlowCode(StringUtils.join(sourceWorkFlowCodes, Constants.COMMA));
                    sourceWorkFlowCodes.forEach(code -> workFlowRelations.add(new WorkFlowRelation(code, workFlow.getKey())));
                }
            }
        }
        Map<String, Object> workFlowLists = new HashMap<>();
        workFlowLists.put(Constants.WORKFLOW_LIST, workFlowLineagesMap.values());
        workFlowLists.put(Constants.WORKFLOW_RELATION_LIST, workFlowRelations);
        result.put(Constants.DATA_LIST, workFlowLists);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private Set<Long> querySourceWorkFlowCodes(long projectCode, long workFlowCode, List<TaskDefinition> taskDefinitionList) {
        Set<Long> sourceWorkFlowCodes = new HashSet<>();
        if (taskDefinitionList == null || taskDefinitionList.isEmpty()) {
            return sourceWorkFlowCodes;
        }
        List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitionList);
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            if (taskDefinitionLog.getProjectCode() == projectCode) {
                if (taskDefinitionLog.getTaskType().equals(TaskType.DEPENDENT.getDesc())) {
                    DependentParameters dependentParameters = JSONUtils.parseObject(taskDefinitionLog.getDependence(), DependentParameters.class);
                    if (dependentParameters != null) {
                        List<DependentTaskModel> dependTaskList = dependentParameters.getDependTaskList();
                        for (DependentTaskModel taskModel : dependTaskList) {
                            List<DependentItem> dependItemList = taskModel.getDependItemList();
                            for (DependentItem dependentItem : dependItemList) {
                                if (dependentItem.getProjectCode() == projectCode && dependentItem.getDefinitionCode() != workFlowCode) {
                                    sourceWorkFlowCodes.add(dependentItem.getDefinitionCode());
                                }
                            }
                        }
                    }
                }
            }
        }
        return sourceWorkFlowCodes;
    }
}
