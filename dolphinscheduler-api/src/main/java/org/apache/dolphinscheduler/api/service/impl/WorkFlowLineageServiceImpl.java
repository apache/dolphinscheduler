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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DEPENDENT;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.WorkFlowLineageService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DependentProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskMainInfo;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkFlowLineageMapper;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * work flow lineage service impl
 */
@Service
public class WorkFlowLineageServiceImpl extends BaseServiceImpl implements WorkFlowLineageService {

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowLineageServiceImpl.class);

    @Autowired
    private WorkFlowLineageMapper workFlowLineageMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Override
    public Map<String, Object> queryWorkFlowLineageByName(long projectCode, String workFlowName) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            logger.error("Project does not exist, projectCode:{}.", projectCode);
            putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
            return result;
        }
        List<WorkFlowLineage> workFlowLineageList = workFlowLineageMapper.queryWorkFlowLineageByName(projectCode, workFlowName);
        result.put(Constants.DATA_LIST, workFlowLineageList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Map<String, Object> queryWorkFlowLineageByCode(long projectCode, long sourceWorkFlowCode) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            logger.error("Project does not exist, projectCode:{}.", projectCode);
            putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
            return result;
        }
        List<WorkFlowLineage> workFlowLineages = new ArrayList<>();
        Set<WorkFlowRelation> workFlowRelations = new HashSet<>();
        recursiveWorkFlow(projectCode, sourceWorkFlowCode, workFlowLineages, workFlowRelations);
        Map<String, Object> workFlowLists = new HashMap<>();
        workFlowLists.put(Constants.WORKFLOW_LIST, workFlowLineages);
        workFlowLists.put(Constants.WORKFLOW_RELATION_LIST, workFlowRelations);
        result.put(Constants.DATA_LIST, workFlowLists);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private void recursiveWorkFlow(long projectCode,
                                   long sourceWorkFlowCode,
                                   List<WorkFlowLineage> workFlowLineages,
                                   Set<WorkFlowRelation> workFlowRelations) {
        workFlowLineages.add(workFlowLineageMapper.queryWorkFlowLineageByCode(projectCode,sourceWorkFlowCode));

        List<WorkFlowLineage> downStreamWorkFlowLineages =
                workFlowLineageMapper.queryDownstreamLineageByProcessDefinitionCode(sourceWorkFlowCode, "DEPENDENT");
        workFlowLineages.addAll(downStreamWorkFlowLineages);
        downStreamWorkFlowLineages.forEach(workFlowLineage -> workFlowRelations.add(new WorkFlowRelation(sourceWorkFlowCode, workFlowLineage.getWorkFlowCode())));

        List<WorkFlowLineage> upstreamWorkFlowLineages = new ArrayList<>();
        getUpstreamLineages(sourceWorkFlowCode, upstreamWorkFlowLineages);
        workFlowLineages.addAll(upstreamWorkFlowLineages);
        upstreamWorkFlowLineages.forEach(workFlowLineage -> workFlowRelations.add(new WorkFlowRelation(workFlowLineage.getWorkFlowCode(), sourceWorkFlowCode)));
    }

    private void getUpstreamLineages(long sourceWorkFlowCode,
                                     List<WorkFlowLineage> upstreamWorkFlowLineages) {
        List<DependentProcessDefinition> workFlowDependentDefinitionList =
                workFlowLineageMapper.queryUpstreamDependentParamsByProcessDefinitionCode(sourceWorkFlowCode, "DEPENDENT");

        List<Long> upstreamProcessDefinitionCodes = new ArrayList<>();

        getProcessDefinitionCodeByDependentDefinitionList(workFlowDependentDefinitionList,
                upstreamProcessDefinitionCodes);

        if (!upstreamProcessDefinitionCodes.isEmpty()) {
            upstreamWorkFlowLineages.addAll(
                    workFlowLineageMapper.queryWorkFlowLineageByProcessDefinitionCodes(upstreamProcessDefinitionCodes));
        }
    }

    /**
     * get dependent process definition code by dependent process definition list
     */
    private void getProcessDefinitionCodeByDependentDefinitionList(List<DependentProcessDefinition> dependentDefinitionList,
                                                                   List<Long> processDefinitionCodes) {
        for (DependentProcessDefinition dependentProcessDefinition : dependentDefinitionList) {
            for (DependentTaskModel dependentTaskModel : dependentProcessDefinition.getDependentParameters().getDependTaskList()) {
                for (DependentItem dependentItem : dependentTaskModel.getDependItemList()) {
                    if (!processDefinitionCodes.contains(dependentItem.getDefinitionCode())) {
                        processDefinitionCodes.add(dependentItem.getDefinitionCode());
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Object> queryWorkFlowLineage(long projectCode) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            logger.error("Project does not exist, projectCode:{}.", projectCode);
            putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
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
                if (taskDefinitionLog.getTaskType().equals(TASK_TYPE_DEPENDENT)) {
                    DependentParameters dependentParameters = JSONUtils.parseObject(taskDefinitionLog.getDependence(), DependentParameters.class);
                    if (dependentParameters != null) {
                        List<DependentTaskModel> dependTaskList = dependentParameters.getDependTaskList();
                        if (!CollectionUtils.isEmpty(dependTaskList)) {
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
        }
        return sourceWorkFlowCodes;
    }

    /**
     * Query and return tasks dependence with string format, is a wrapper of queryTaskDepOnTask and task query method.
     *
     * @param projectCode Project code want to query tasks dependence
     * @param processDefinitionCode Process definition code want to query tasks dependence
     * @param taskCode Task code want to query tasks dependence
     * @return Optional of formatter message
     */
    @Override
    public Optional<String> taskDepOnTaskMsg(long projectCode, long processDefinitionCode, long taskCode) {
        List<TaskMainInfo> tasksDep = workFlowLineageMapper.queryTaskDepOnTask(projectCode, processDefinitionCode, taskCode);
        if (CollectionUtils.isEmpty(tasksDep)) {
            return Optional.empty();
        }

        String taskDepStr = tasksDep.stream().map(task -> String.format(Constants.FORMAT_S_S_COLON, task.getProcessDefinitionName(), task.getTaskName())).collect(Collectors.joining(Constants.COMMA));
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        return Optional.of(MessageFormat.format(Status.DELETE_TASK_USE_BY_OTHER_FAIL.getMsg(), taskDefinition.getName(), taskDepStr));
    }

    /**
     * Query tasks depend on process definition, include upstream or downstream
     *
     * @param projectCode Project code want to query tasks dependence
     * @param processDefinitionCode Process definition code want to query tasks dependence
     * @return Set of TaskMainInfo
     */
    @Override
    public Set<TaskMainInfo> queryTaskDepOnProcess(long projectCode, long processDefinitionCode) {
        Set<TaskMainInfo> taskMainInfos = new HashSet<>();
        List<TaskMainInfo> taskDependents = workFlowLineageMapper.queryTaskDependentDepOnProcess(projectCode, processDefinitionCode);
        List<TaskMainInfo> taskSubProcess = workFlowLineageMapper.queryTaskSubProcessDepOnProcess(projectCode, processDefinitionCode);
        taskMainInfos.addAll(taskDependents);
        taskMainInfos.addAll(taskSubProcess);
        return taskMainInfos;
    }
}
