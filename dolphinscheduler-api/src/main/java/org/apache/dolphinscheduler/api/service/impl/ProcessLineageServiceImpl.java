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
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProcessLineageService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.DependentLineageTask;
import org.apache.dolphinscheduler.dao.entity.DependentProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationDetail;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessLineageMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * work flow lineage service impl
 */
@Slf4j
@Service
public class ProcessLineageServiceImpl extends BaseServiceImpl implements ProcessLineageService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private ProcessLineageMapper processLineageMapper;
    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Override
    public List<WorkFlowRelationDetail> queryWorkFlowLineageByName(long projectCode, String processDefinitionName) {
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, projectCode);
        }
        return processLineageMapper.queryWorkFlowLineageByName(projectCode, processDefinitionName);
    }

    @Override
    public Map<String, Object> queryWorkFlowLineageByCode(long projectCode, long processDefinitionCode) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            log.error("Project does not exist, projectCode:{}.", projectCode);
            putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
            return result;
        }
        List<ProcessLineage> upstreamProcessLineageList =
                processLineageMapper.queryByProcessDefinitionCode(processDefinitionCode);
        List<ProcessLineage> downstreamProcessLineageList =
                processLineageMapper.queryWorkFlowLineageByDept(projectCode, processDefinitionCode, 0);
        List<ProcessLineage> totalProcessLineageList =
                Stream.of(upstreamProcessLineageList, downstreamProcessLineageList)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        List<WorkFlowRelation> workFlowRelationList = getWorkFlowRelations(totalProcessLineageList);
        List<WorkFlowRelationDetail> workFlowRelationDetailList =
                getWorkflowRelationDetails(totalProcessLineageList.stream()
                        .flatMap(pl -> {
                            List<Long> processDefinitionCodes = new ArrayList<>();
                            processDefinitionCodes.add(pl.getProcessDefinitionCode());
                            processDefinitionCodes.add(pl.getDeptProcessDefinitionCode());
                            return processDefinitionCodes.stream();
                        }).distinct().collect(Collectors.toList()));

        Map<String, Object> workFlowLists = new HashMap<>();
        workFlowLists.put(Constants.WORKFLOW_RELATION_DETAIL_LIST, workFlowRelationDetailList);
        workFlowLists.put(Constants.WORKFLOW_RELATION_LIST, workFlowRelationList);
        result.put(Constants.DATA_LIST, workFlowLists);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Map<String, Object> queryWorkFlowLineage(long projectCode) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            log.error("Project does not exist, projectCode:{}.", projectCode);
            putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
            return result;
        }
        List<ProcessLineage> processLineageList = processLineageMapper.queryByProjectCode(projectCode);
        List<WorkFlowRelation> workFlowRelationList = getWorkFlowRelations(processLineageList);
        List<WorkFlowRelationDetail> workFlowRelationDetailList = getWorkflowRelationDetails(processLineageList.stream()
                .flatMap(pl -> {
                    List<Long> processDefinitionCodes = new ArrayList<>();
                    processDefinitionCodes.add(pl.getProcessDefinitionCode());
                    processDefinitionCodes.add(pl.getDeptProcessDefinitionCode());
                    return processDefinitionCodes.stream();
                }).distinct().collect(Collectors.toList()));

        Map<String, Object> workFlowLists = new HashMap<>();
        workFlowLists.put(Constants.WORKFLOW_RELATION_DETAIL_LIST, workFlowRelationDetailList);
        workFlowLists.put(Constants.WORKFLOW_RELATION_LIST, workFlowRelationList);
        result.put(Constants.DATA_LIST, workFlowLists);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private List<WorkFlowRelation> getWorkFlowRelations(List<ProcessLineage> processLineageList) {
        List<WorkFlowRelation> workFlowRelations = new ArrayList<>();
        List<Long> processDefinitionCodes = processLineageList.stream()
                .map(ProcessLineage::getProcessDefinitionCode).distinct().collect(Collectors.toList());
        for (ProcessLineage processLineage : processLineageList) {
            workFlowRelations.add(new WorkFlowRelation(processLineage.getDeptProcessDefinitionCode(),
                    processLineage.getProcessDefinitionCode()));

            if (!processDefinitionCodes.contains(processLineage.getDeptProcessDefinitionCode())) {
                workFlowRelations.add(new WorkFlowRelation(0, processLineage.getProcessDefinitionCode()));
            }
        }
        return workFlowRelations;
    }

    private List<WorkFlowRelationDetail> getWorkflowRelationDetails(List<Long> processDefinitionCodes) {
        List<WorkFlowRelationDetail> workFlowRelationDetails = new ArrayList<>();
        for (Long processDefinitionCode : processDefinitionCodes) {
            List<WorkFlowRelationDetail> workFlowRelationDetailList =
                    processLineageMapper.queryWorkFlowLineageByCode(processDefinitionCode);
            workFlowRelationDetails.addAll(workFlowRelationDetailList);
        }
        return workFlowRelationDetails;
    }

    /**
     * Query tasks depend on process definition, include upstream or downstream
     * and return tasks dependence with string format.
     *
     * @param projectCode           Project code want to query tasks dependence
     * @param processDefinitionCode Process definition code want to query tasks dependence
     * @param taskCode              Task code want to query tasks dependence
     * @return Optional of formatter message
     */
    @Override
    public Optional<String> taskDependentMsg(long projectCode, long processDefinitionCode, long taskCode) {
        long queryTaskCode = 0;
        if (taskCode != 0) {
            queryTaskCode = taskCode;
        }
        List<ProcessLineage> dependentProcessList =
                processLineageMapper.queryWorkFlowLineageByDept(projectCode, processDefinitionCode, queryTaskCode);
        if (CollectionUtils.isEmpty(dependentProcessList)) {
            return Optional.empty();
        }

        List<String> taskDepStrList = new ArrayList<>();

        for (ProcessLineage processLineage : dependentProcessList) {
            ProcessDefinition processDefinition =
                    processDefinitionMapper.queryByCode(processLineage.getDeptProcessDefinitionCode());
            String taskName = "";
            if (processLineage.getTaskDefinitionCode() != 0) {
                TaskDefinition taskDefinition =
                        taskDefinitionMapper.queryByCode(processLineage.getTaskDefinitionCode());
                taskName = taskDefinition.getName();
            }
            taskDepStrList.add(String.format(Constants.FORMAT_S_S_COLON, processDefinition.getName(), taskName));
        }

        String taskDepStr = String.join(Constants.COMMA, taskDepStrList);
        if (taskCode != 0) {
            TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
            return Optional
                    .of(MessageFormat.format(Status.DELETE_TASK_USE_BY_OTHER_FAIL.getMsg(), taskDefinition.getName(),
                            taskDepStr));
        } else {
            return Optional.of(MessageFormat.format(Status.DELETE_TASK_USE_BY_OTHER_FAIL.getMsg(), "",
                    taskDepStr));
        }
    }

    /**
     * Query downstream tasks depend on a process definition or a task
     *
     * @param processDefinitionCode Process definition code want to query tasks dependence
     * @return downstream dependent process definition list
     */
    @Override
    public List<DependentProcessDefinition> queryDownstreamDependentProcessDefinitions(Long processDefinitionCode) {
        List<DependentProcessDefinition> dependentProcessDefinitionList = new ArrayList<>();
        List<ProcessLineage> processLineageList =
                processLineageMapper.queryWorkFlowLineageByDept(0, processDefinitionCode, 0);
        if (processLineageList.isEmpty()) {
            return dependentProcessDefinitionList;
        }

        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryByCodes(processLineageList.stream()
                .map(ProcessLineage::getDeptProcessDefinitionCode).distinct().collect(Collectors.toList()));
        List<TaskDefinition> taskDefinitionList = taskDefinitionMapper.queryByCodeList(processLineageList.stream()
                .map(ProcessLineage::getDeptTaskDefinitionCode).distinct().collect(Collectors.toList()));
        for (TaskDefinition taskDefinition : taskDefinitionList) {
            DependentProcessDefinition dependentProcessDefinition = new DependentProcessDefinition();
            processLineageList.stream()
                    .filter(processLineage -> processLineage.getDeptTaskDefinitionCode() == taskDefinition.getCode())
                    .findFirst()
                    .ifPresent(processLineage -> {
                        dependentProcessDefinition
                                .setProcessDefinitionCode(processLineage.getDeptProcessDefinitionCode());
                        dependentProcessDefinition.setTaskDefinitionCode(taskDefinition.getCode());
                        dependentProcessDefinition.setTaskParams(taskDefinition.getTaskParams());
                        dependentProcessDefinition.setWorkerGroup(taskDefinition.getWorkerGroup());
                    });
            processDefinitionList.stream()
                    .filter(processDefinition -> processDefinition.getCode() == dependentProcessDefinition
                            .getProcessDefinitionCode())
                    .findFirst()
                    .ifPresent(processDefinition -> {
                        dependentProcessDefinition.setProcessDefinitionVersion(processDefinition.getVersion());
                    });
        }

        return dependentProcessDefinitionList;
    }

    public Map<String, Object> queryDependentProcessDefinitions(long projectCode, long processDefinitionCode,
                                                                Long taskCode) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            log.error("Project does not exist, projectCode:{}.", projectCode);
            putMsg(result, Status.PROJECT_NOT_FOUND, projectCode);
            return result;
        }
        List<ProcessLineage> processLineageList = processLineageMapper.queryWorkFlowLineageByDept(projectCode,
                processDefinitionCode, taskCode == null ? 0 : taskCode);
        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryByCodes(processLineageList.stream()
                .map(ProcessLineage::getProcessDefinitionCode).distinct().collect(Collectors.toList()));
        List<TaskDefinition> taskDefinitionList = taskDefinitionMapper.queryByCodeList(processLineageList.stream()
                .map(ProcessLineage::getTaskDefinitionCode).filter(code -> code != 0).distinct()
                .collect(Collectors.toList()));
        List<DependentLineageTask> dependentLineageTaskList = new ArrayList<>();
        for (ProcessLineage processLineage : processLineageList) {
            DependentLineageTask dependentLineageTask = new DependentLineageTask();
            taskDefinitionList.stream()
                    .filter(taskDefinition -> taskDefinition.getCode() == processLineage.getTaskDefinitionCode())
                    .findFirst()
                    .ifPresent(taskDefinition -> {
                        dependentLineageTask.setTaskDefinitionCode(taskDefinition.getCode());
                        dependentLineageTask.setTaskDefinitionName(taskDefinition.getName());
                    });
            processDefinitionList.stream()
                    .filter(processDefinition -> processDefinition.getCode() == processLineage
                            .getProcessDefinitionCode())
                    .findFirst()
                    .ifPresent(processDefinition -> {
                        dependentLineageTask.setProcessDefinitionCode(processDefinition.getCode());
                        dependentLineageTask.setProcessDefinitionName(processDefinition.getName());
                        dependentLineageTask.setProjectCode(processDefinition.getProjectCode());
                    });
            dependentLineageTaskList.add(dependentLineageTask);
        }
        result.put(Constants.DATA_LIST, dependentLineageTaskList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public int createProcessLineage(List<ProcessLineage> processLineages) {
        return processLineageMapper.batchInsert(processLineages);
    }

    @Override
    public int updateProcessLineage(List<ProcessLineage> processLineages) {
        processLineageMapper.batchDeleteByProcessDefinitionCode(processLineages.stream()
                .map(ProcessLineage::getProcessDefinitionCode).distinct().collect(Collectors.toList()));

        return processLineageMapper.batchInsert(processLineages);
    }

    @Override
    public int deleteProcessLineage(List<Long> processDefinitionCodes) {
        return processLineageMapper.batchDeleteByProcessDefinitionCode(processDefinitionCodes);
    }
}
