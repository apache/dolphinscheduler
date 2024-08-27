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
import org.apache.dolphinscheduler.api.service.WorkflowLineageService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.DependentLineageTask;
import org.apache.dolphinscheduler.dao.entity.DependentProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskLineage;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationDetail;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.repository.WorkflowTaskLineageDao;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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
public class WorkflowLineageServiceImpl extends BaseServiceImpl implements WorkflowLineageService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private WorkflowTaskLineageDao workflowTaskLineageDao;
    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Override
    public List<WorkFlowRelationDetail> queryWorkFlowLineageByName(long projectCode, String processDefinitionName) {
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, projectCode);
        }
        return workflowTaskLineageDao.queryWorkFlowLineageByName(projectCode, processDefinitionName);
    }

    @Override
    public WorkFlowLineage queryWorkFlowLineageByCode(long projectCode, long processDefinitionCode) {
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, projectCode);
        }
        List<WorkflowTaskLineage> upstreamWorkflowTaskLineageList =
                workflowTaskLineageDao.queryByWorkflowDefinitionCode(processDefinitionCode);
        List<WorkflowTaskLineage> downstreamWorkflowTaskLineageList =
                workflowTaskLineageDao.queryWorkFlowLineageByDept(projectCode, processDefinitionCode,
                        Constants.DEPENDENT_ALL_TASK);
        List<WorkflowTaskLineage> totalWorkflowTaskLineageList =
                Stream.of(upstreamWorkflowTaskLineageList, downstreamWorkflowTaskLineageList)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        List<WorkFlowRelation> workFlowRelationList = getWorkFlowRelations(totalWorkflowTaskLineageList);
        List<WorkFlowRelationDetail> workFlowRelationDetailList =
                getWorkflowRelationDetails(totalWorkflowTaskLineageList.stream()
                        .flatMap(pl -> {
                            List<Long> processDefinitionCodes = new ArrayList<>();
                            processDefinitionCodes.add(pl.getWorkflowDefinitionCode());
                            processDefinitionCodes.add(pl.getDeptWorkflowDefinitionCode());
                            return processDefinitionCodes.stream();
                        }).distinct().collect(Collectors.toList()));

        WorkFlowLineage workFlowLineage = new WorkFlowLineage();
        workFlowLineage.setWorkFlowRelationDetailList(workFlowRelationDetailList);
        workFlowLineage.setWorkFlowRelationList(workFlowRelationList);
        return workFlowLineage;
    }

    @Override
    public WorkFlowLineage queryWorkFlowLineage(long projectCode) {
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, projectCode);
        }
        List<WorkflowTaskLineage> workflowTaskLineageList = workflowTaskLineageDao.queryByProjectCode(projectCode);
        List<WorkFlowRelation> workFlowRelationList = getWorkFlowRelations(workflowTaskLineageList);
        List<WorkFlowRelationDetail> workFlowRelationDetailList =
                getWorkflowRelationDetails(workflowTaskLineageList.stream()
                        .flatMap(pl -> {
                            List<Long> processDefinitionCodes = new ArrayList<>();
                            processDefinitionCodes.add(pl.getWorkflowDefinitionCode());
                            processDefinitionCodes.add(pl.getDeptWorkflowDefinitionCode());
                            return processDefinitionCodes.stream();
                        }).distinct().collect(Collectors.toList()));

        WorkFlowLineage workFlowLineage = new WorkFlowLineage();
        workFlowLineage.setWorkFlowRelationList(workFlowRelationList);
        workFlowLineage.setWorkFlowRelationDetailList(workFlowRelationDetailList);
        return workFlowLineage;
    }

    private List<WorkFlowRelation> getWorkFlowRelations(List<WorkflowTaskLineage> workflowTaskLineageList) {
        List<WorkFlowRelation> workFlowRelations = new ArrayList<>();
        List<Long> processDefinitionCodes = workflowTaskLineageList.stream()
                .map(WorkflowTaskLineage::getWorkflowDefinitionCode).distinct().collect(Collectors.toList());
        for (WorkflowTaskLineage workflowTaskLineage : workflowTaskLineageList) {
            workFlowRelations.add(new WorkFlowRelation(workflowTaskLineage.getDeptWorkflowDefinitionCode(),
                    workflowTaskLineage.getWorkflowDefinitionCode()));

            if (!processDefinitionCodes.contains(workflowTaskLineage.getDeptWorkflowDefinitionCode())) {
                workFlowRelations.add(new WorkFlowRelation(0, workflowTaskLineage.getWorkflowDefinitionCode()));
            }
        }
        return workFlowRelations;
    }

    private List<WorkFlowRelationDetail> getWorkflowRelationDetails(List<Long> processDefinitionCodes) {
        List<WorkFlowRelationDetail> workFlowRelationDetails = new ArrayList<>();
        for (Long processDefinitionCode : processDefinitionCodes) {
            List<WorkFlowRelationDetail> workFlowRelationDetailList =
                    workflowTaskLineageDao.queryWorkFlowLineageByCode(processDefinitionCode);
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
        List<WorkflowTaskLineage> dependentProcessList =
                workflowTaskLineageDao.queryWorkFlowLineageByDept(projectCode, processDefinitionCode, queryTaskCode);
        if (CollectionUtils.isEmpty(dependentProcessList)) {
            return Optional.empty();
        }

        List<String> taskDepStrList = new ArrayList<>();

        for (WorkflowTaskLineage workflowTaskLineage : dependentProcessList) {
            WorkflowDefinition workflowDefinition =
                    processDefinitionMapper.queryByCode(workflowTaskLineage.getDeptWorkflowDefinitionCode());
            String taskName = "";
            if (workflowTaskLineage.getTaskDefinitionCode() != 0) {
                TaskDefinition taskDefinition =
                        taskDefinitionMapper.queryByCode(workflowTaskLineage.getTaskDefinitionCode());
                taskName = taskDefinition.getName();
            }
            taskDepStrList.add(String.format(Constants.FORMAT_S_S_COLON, workflowDefinition.getName(), taskName));
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
        List<WorkflowTaskLineage> workflowTaskLineageList =
                workflowTaskLineageDao.queryWorkFlowLineageByDept(Constants.DEFAULT_PROJECT_CODE, processDefinitionCode,
                        Constants.DEPENDENT_ALL_TASK);
        if (workflowTaskLineageList.isEmpty()) {
            return dependentProcessDefinitionList;
        }

        List<WorkflowDefinition> workflowDefinitionList =
                processDefinitionMapper.queryByCodes(workflowTaskLineageList.stream()
                        .map(WorkflowTaskLineage::getDeptWorkflowDefinitionCode).distinct().collect(Collectors.toList()));
        List<TaskDefinition> taskDefinitionList = taskDefinitionMapper.queryByCodeList(workflowTaskLineageList.stream()
                .map(WorkflowTaskLineage::getDeptTaskDefinitionCode).distinct().collect(Collectors.toList()));
        for (TaskDefinition taskDefinition : taskDefinitionList) {
            DependentProcessDefinition dependentProcessDefinition = new DependentProcessDefinition();
            workflowTaskLineageList.stream()
                    .filter(processLineage -> processLineage.getDeptTaskDefinitionCode() == taskDefinition.getCode())
                    .findFirst()
                    .ifPresent(processLineage -> {
                        dependentProcessDefinition
                                .setProcessDefinitionCode(processLineage.getDeptWorkflowDefinitionCode());
                        dependentProcessDefinition.setTaskDefinitionCode(taskDefinition.getCode());
                        dependentProcessDefinition.setTaskParams(taskDefinition.getTaskParams());
                        dependentProcessDefinition.setWorkerGroup(taskDefinition.getWorkerGroup());
                    });
            workflowDefinitionList.stream()
                    .filter(processDefinition -> processDefinition.getCode() == dependentProcessDefinition
                            .getProcessDefinitionCode())
                    .findFirst()
                    .ifPresent(processDefinition -> {
                        dependentProcessDefinition.setProcessDefinitionVersion(processDefinition.getVersion());
                    });
        }

        return dependentProcessDefinitionList;
    }

    @Override
    public List<DependentLineageTask> queryDependentProcessDefinitions(long projectCode, long processDefinitionCode,
                                                                       Long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, projectCode);
        }
        List<WorkflowTaskLineage> workflowTaskLineageList = workflowTaskLineageDao.queryWorkFlowLineageByDept(projectCode,
                processDefinitionCode, taskCode == null ? 0 : taskCode);
        List<WorkflowDefinition> workflowDefinitionList =
                processDefinitionMapper.queryByCodes(workflowTaskLineageList.stream()
                        .map(WorkflowTaskLineage::getWorkflowDefinitionCode).distinct().collect(Collectors.toList()));
        List<TaskDefinition> taskDefinitionList = taskDefinitionMapper.queryByCodeList(workflowTaskLineageList.stream()
                .map(WorkflowTaskLineage::getTaskDefinitionCode).filter(code -> code != 0).distinct()
                .collect(Collectors.toList()));
        List<DependentLineageTask> dependentLineageTaskList = new ArrayList<>();
        for (WorkflowTaskLineage workflowTaskLineage : workflowTaskLineageList) {
            DependentLineageTask dependentLineageTask = new DependentLineageTask();
            taskDefinitionList.stream()
                    .filter(taskDefinition -> taskDefinition.getCode() == workflowTaskLineage.getTaskDefinitionCode())
                    .findFirst()
                    .ifPresent(taskDefinition -> {
                        dependentLineageTask.setTaskDefinitionCode(taskDefinition.getCode());
                        dependentLineageTask.setTaskDefinitionName(taskDefinition.getName());
                    });
            workflowDefinitionList.stream()
                    .filter(processDefinition -> processDefinition.getCode() == workflowTaskLineage
                            .getWorkflowDefinitionCode())
                    .findFirst()
                    .ifPresent(processDefinition -> {
                        dependentLineageTask.setProcessDefinitionCode(processDefinition.getCode());
                        dependentLineageTask.setProcessDefinitionName(processDefinition.getName());
                        dependentLineageTask.setProjectCode(processDefinition.getProjectCode());
                    });
            dependentLineageTaskList.add(dependentLineageTask);
        }
        return dependentLineageTaskList;
    }

    @Override
    public int createProcessLineage(List<WorkflowTaskLineage> workflowTaskLineages) {
        return workflowTaskLineageDao.batchInsert(workflowTaskLineages);
    }

    @Override
    public int updateProcessLineage(List<WorkflowTaskLineage> workflowTaskLineages) {
        return workflowTaskLineageDao.updateWorkflowTaskLineage(workflowTaskLineages);
    }

    @Override
    public int deleteProcessLineage(List<Long> processDefinitionCodes) {
        return workflowTaskLineageDao.batchDeleteByWorkflowDefinitionCode(processDefinitionCodes);
    }
}
