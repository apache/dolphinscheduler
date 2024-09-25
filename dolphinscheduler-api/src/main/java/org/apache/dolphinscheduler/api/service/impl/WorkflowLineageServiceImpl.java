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
import org.apache.dolphinscheduler.dao.entity.DependentWorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelationDetail;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskLineage;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
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
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Override
    public List<WorkFlowRelationDetail> queryWorkFlowLineageByName(long projectCode, String workflowDefinitionName) {
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, projectCode);
        }
        return workflowTaskLineageDao.queryWorkFlowLineageByName(projectCode, workflowDefinitionName);
    }

    @Override
    public WorkFlowLineage queryWorkFlowLineageByCode(long projectCode, long workflowDefinitionCode) {
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, projectCode);
        }
        List<WorkflowTaskLineage> upstreamWorkflowTaskLineageList =
                workflowTaskLineageDao.queryByWorkflowDefinitionCode(workflowDefinitionCode);
        List<WorkflowTaskLineage> downstreamWorkflowTaskLineageList =
                workflowTaskLineageDao.queryWorkFlowLineageByDept(projectCode, workflowDefinitionCode,
                        Constants.DEPENDENT_ALL_TASK);
        List<WorkflowTaskLineage> totalWorkflowTaskLineageList =
                Stream.of(upstreamWorkflowTaskLineageList, downstreamWorkflowTaskLineageList)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        List<WorkFlowRelation> workFlowRelationList = getWorkFlowRelations(totalWorkflowTaskLineageList);
        List<WorkFlowRelationDetail> workFlowRelationDetailList =
                getWorkflowRelationDetails(totalWorkflowTaskLineageList.stream()
                        .flatMap(pl -> {
                            List<Long> workflowDefinitionCodes = new ArrayList<>();
                            workflowDefinitionCodes.add(pl.getWorkflowDefinitionCode());
                            workflowDefinitionCodes.add(pl.getDeptWorkflowDefinitionCode());
                            return workflowDefinitionCodes.stream();
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
                            List<Long> workflowDefinitionCodes = new ArrayList<>();
                            workflowDefinitionCodes.add(pl.getWorkflowDefinitionCode());
                            workflowDefinitionCodes.add(pl.getDeptWorkflowDefinitionCode());
                            return workflowDefinitionCodes.stream();
                        }).distinct().collect(Collectors.toList()));

        WorkFlowLineage workFlowLineage = new WorkFlowLineage();
        workFlowLineage.setWorkFlowRelationList(workFlowRelationList);
        workFlowLineage.setWorkFlowRelationDetailList(workFlowRelationDetailList);
        return workFlowLineage;
    }

    private List<WorkFlowRelation> getWorkFlowRelations(List<WorkflowTaskLineage> workflowTaskLineageList) {
        List<WorkFlowRelation> workFlowRelations = new ArrayList<>();
        List<Long> workflowDefinitionCodes = workflowTaskLineageList.stream()
                .map(WorkflowTaskLineage::getWorkflowDefinitionCode).distinct().collect(Collectors.toList());
        for (WorkflowTaskLineage workflowTaskLineage : workflowTaskLineageList) {
            workFlowRelations.add(new WorkFlowRelation(workflowTaskLineage.getDeptWorkflowDefinitionCode(),
                    workflowTaskLineage.getWorkflowDefinitionCode()));

            if (!workflowDefinitionCodes.contains(workflowTaskLineage.getDeptWorkflowDefinitionCode())) {
                workFlowRelations.add(new WorkFlowRelation(0, workflowTaskLineage.getWorkflowDefinitionCode()));
            }
        }
        return workFlowRelations;
    }

    private List<WorkFlowRelationDetail> getWorkflowRelationDetails(List<Long> workflowDefinitionCodes) {
        List<WorkFlowRelationDetail> workFlowRelationDetails = new ArrayList<>();
        for (Long workflowDefinitionCode : workflowDefinitionCodes) {
            List<WorkFlowRelationDetail> workFlowRelationDetailList =
                    workflowTaskLineageDao.queryWorkFlowLineageByCode(workflowDefinitionCode);
            workFlowRelationDetails.addAll(workFlowRelationDetailList);
        }
        return workFlowRelationDetails;
    }

    /**
     * Query tasks depend on workflow definition, include upstream or downstream
     * and return tasks dependence with string format.
     *
     * @param projectCode           Project code want to query tasks dependence
     * @param workflowDefinitionCode workflow definition code want to query tasks dependence
     * @param taskCode              Task code want to query tasks dependence
     * @return Optional of formatter message
     */
    @Override
    public Optional<String> taskDependentMsg(long projectCode, long workflowDefinitionCode, long taskCode) {
        long queryTaskCode = 0;
        if (taskCode != 0) {
            queryTaskCode = taskCode;
        }
        List<WorkflowTaskLineage> dependentWorkflowList =
                workflowTaskLineageDao.queryWorkFlowLineageByDept(projectCode, workflowDefinitionCode, queryTaskCode);
        if (CollectionUtils.isEmpty(dependentWorkflowList)) {
            return Optional.empty();
        }

        List<String> taskDepStrList = new ArrayList<>();

        for (WorkflowTaskLineage workflowTaskLineage : dependentWorkflowList) {
            WorkflowDefinition workflowDefinition =
                    workflowDefinitionMapper.queryByCode(workflowTaskLineage.getDeptWorkflowDefinitionCode());
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
     * Query downstream tasks depend on a workflow definition or a task
     *
     * @param workflowDefinitionCode workflow definition code want to query tasks dependence
     * @return downstream dependent workflow definition list
     */
    @Override
    public List<DependentWorkflowDefinition> queryDownstreamDependentWorkflowDefinitions(Long workflowDefinitionCode) {
        List<DependentWorkflowDefinition> dependentWorkflowDefinitionList = new ArrayList<>();
        List<WorkflowTaskLineage> workflowTaskLineageList =
                workflowTaskLineageDao.queryWorkFlowLineageByDept(Constants.DEFAULT_PROJECT_CODE,
                        workflowDefinitionCode,
                        Constants.DEPENDENT_ALL_TASK);
        if (workflowTaskLineageList.isEmpty()) {
            return dependentWorkflowDefinitionList;
        }

        List<WorkflowDefinition> workflowDefinitionList =
                workflowDefinitionMapper.queryByCodes(workflowTaskLineageList.stream()
                        .map(WorkflowTaskLineage::getDeptWorkflowDefinitionCode).distinct()
                        .collect(Collectors.toList()));
        List<TaskDefinition> taskDefinitionList = taskDefinitionMapper.queryByCodeList(workflowTaskLineageList.stream()
                .map(WorkflowTaskLineage::getDeptTaskDefinitionCode).distinct().collect(Collectors.toList()));
        for (TaskDefinition taskDefinition : taskDefinitionList) {
            DependentWorkflowDefinition dependentWorkflowDefinition = new DependentWorkflowDefinition();
            workflowTaskLineageList.stream()
                    .filter(workflowLineage -> workflowLineage.getDeptTaskDefinitionCode() == taskDefinition.getCode())
                    .findFirst()
                    .ifPresent(workflowLineage -> {
                        dependentWorkflowDefinition
                                .setWorkflowDefinitionCode(workflowLineage.getDeptWorkflowDefinitionCode());
                        dependentWorkflowDefinition.setTaskDefinitionCode(taskDefinition.getCode());
                        dependentWorkflowDefinition.setTaskParams(taskDefinition.getTaskParams());
                        dependentWorkflowDefinition.setWorkerGroup(taskDefinition.getWorkerGroup());
                    });
            workflowDefinitionList.stream()
                    .filter(workflowDefinition -> workflowDefinition.getCode() == dependentWorkflowDefinition
                            .getWorkflowDefinitionCode())
                    .findFirst()
                    .ifPresent(workflowDefinition -> {
                        dependentWorkflowDefinition.setWorkflowDefinitionVersion(workflowDefinition.getVersion());
                    });
        }

        return dependentWorkflowDefinitionList;
    }

    @Override
    public List<DependentLineageTask> queryDependentWorkflowDefinitions(long projectCode, long workflowDefinitionCode,
                                                                        Long taskCode) {
        Project project = projectMapper.queryByCode(projectCode);
        if (project == null) {
            throw new ServiceException(Status.PROJECT_NOT_FOUND, projectCode);
        }
        List<DependentLineageTask> dependentLineageTaskList = new ArrayList<>();
        List<WorkflowTaskLineage> workflowTaskLineageList =
                workflowTaskLineageDao.queryWorkFlowLineageByDept(projectCode,
                        workflowDefinitionCode, taskCode == null ? 0 : taskCode);
        if (workflowTaskLineageList.isEmpty()) {
            return dependentLineageTaskList;
        }
        List<WorkflowDefinition> workflowDefinitionList =
                workflowDefinitionMapper.queryByCodes(workflowTaskLineageList.stream()
                        .map(WorkflowTaskLineage::getWorkflowDefinitionCode).distinct().collect(Collectors.toList()));
        List<TaskDefinition> taskDefinitionList = taskDefinitionMapper.queryByCodeList(workflowTaskLineageList.stream()
                .map(WorkflowTaskLineage::getTaskDefinitionCode).filter(code -> code != 0).distinct()
                .collect(Collectors.toList()));
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
                    .filter(workflowDefinition -> workflowDefinition.getCode() == workflowTaskLineage
                            .getWorkflowDefinitionCode())
                    .findFirst()
                    .ifPresent(workflowDefinition -> {
                        dependentLineageTask.setWorkflowDefinitionCode(workflowDefinition.getCode());
                        dependentLineageTask.setWorkflowDefinitionName(workflowDefinition.getName());
                        dependentLineageTask.setProjectCode(workflowDefinition.getProjectCode());
                    });
            dependentLineageTaskList.add(dependentLineageTask);
        }
        return dependentLineageTaskList;
    }

    @Override
    public int createWorkflowLineage(List<WorkflowTaskLineage> workflowTaskLineages) {
        return workflowTaskLineageDao.batchInsert(workflowTaskLineages);
    }

    @Override
    public int updateWorkflowLineage(List<WorkflowTaskLineage> workflowTaskLineages) {
        return workflowTaskLineageDao.updateWorkflowTaskLineage(workflowTaskLineages);
    }

    @Override
    public int deleteWorkflowLineage(List<Long> workflowDefinitionCodes) {
        return workflowTaskLineageDao.batchDeleteByWorkflowDefinitionCode(workflowDefinitionCodes);
    }
}
