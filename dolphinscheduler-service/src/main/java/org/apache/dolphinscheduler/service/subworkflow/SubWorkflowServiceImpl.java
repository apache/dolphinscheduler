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

package org.apache.dolphinscheduler.service.subworkflow;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.RelationSubWorkflow;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.RelationSubWorkflowMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubWorkflowServiceImpl implements SubWorkflowService {

    @Autowired
    private RelationSubWorkflowMapper relationSubWorkflowMapper;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

    @Override
    public List<WorkflowInstance> getAllDynamicSubWorkflow(long processInstanceId, long taskCode) {
        List<RelationSubWorkflow> relationSubWorkflows =
                relationSubWorkflowMapper.queryAllSubWorkflowInstance(processInstanceId, taskCode);
        List<Long> allSubProcessInstanceId = relationSubWorkflows.stream()
                .map(RelationSubWorkflow::getSubWorkflowInstanceId).collect(Collectors.toList());

        List<WorkflowInstance> allSubWorkflowInstance = workflowInstanceDao.queryByIds(allSubProcessInstanceId);
        allSubWorkflowInstance.sort(Comparator.comparing(WorkflowInstance::getId));
        return allSubWorkflowInstance;
    }

    @Override
    public int batchInsertRelationSubWorkflow(List<RelationSubWorkflow> relationSubWorkflowList) {
        int insertN = relationSubWorkflowMapper.batchInsert(relationSubWorkflowList);
        return insertN;
    }

    @Override
    public List<WorkflowInstance> filterFinishProcessInstances(List<WorkflowInstance> workflowInstanceList) {
        return workflowInstanceList.stream()
                .filter(subProcessInstance -> subProcessInstance.getState().isFinished()).collect(Collectors.toList());
    }

    @Override
    public List<WorkflowInstance> filterSuccessProcessInstances(List<WorkflowInstance> workflowInstanceList) {
        return workflowInstanceList.stream()
                .filter(subProcessInstance -> subProcessInstance.getState().isSuccess()).collect(Collectors.toList());
    }

    @Override
    public List<WorkflowInstance> filterRunningProcessInstances(List<WorkflowInstance> workflowInstanceList) {
        return workflowInstanceList.stream()
                .filter(subProcessInstance -> subProcessInstance.getState().isRunning()).collect(Collectors.toList());
    }

    @Override
    public List<WorkflowInstance> filterWaitToRunProcessInstances(List<WorkflowInstance> workflowInstanceList) {
        return workflowInstanceList.stream()
                .filter(subProcessInstance -> subProcessInstance.getState().equals(WorkflowExecutionStatus.WAIT_TO_RUN))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowInstance> filterFailedProcessInstances(List<WorkflowInstance> workflowInstanceList) {
        return workflowInstanceList.stream()
                .filter(subProcessInstance -> subProcessInstance.getState().isFailure()).collect(Collectors.toList());
    }

    @Override
    public List<Property> getWorkflowOutputParameters(WorkflowInstance workflowInstance) {
        List<Property> outputParamList =
                new ArrayList<>(JSONUtils.toList(workflowInstance.getVarPool(), Property.class));

        WorkflowDefinitionLog processDefinition = workflowDefinitionLogMapper
                .queryByDefinitionCodeAndVersion(workflowInstance.getWorkflowDefinitionCode(),
                        workflowInstance.getWorkflowDefinitionVersion());
        List<Property> globalParamList = JSONUtils.toList(processDefinition.getGlobalParams(), Property.class);

        Set<String> ouputParamSet = outputParamList.stream().map(Property::getProp).collect(Collectors.toSet());

        // add output global parameters which are not in output parameters list
        globalParamList.stream().filter(globalParam -> !ouputParamSet.contains(globalParam.getProp()))
                .forEach(outputParamList::add);

        return outputParamList;

    }
}
