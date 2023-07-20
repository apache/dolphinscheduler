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
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.RelationSubWorkflow;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.RelationSubWorkflowMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
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
    private ProcessInstanceDao processInstanceDao;

    @Autowired
    private ProcessDefinitionLogMapper processDefinitionLogMapper;

    @Override
    public List<ProcessInstance> getAllDynamicSubWorkflow(long processInstanceId, long taskCode) {
        List<RelationSubWorkflow> relationSubWorkflows =
                relationSubWorkflowMapper.queryAllSubProcessInstance(processInstanceId, taskCode);
        List<Long> allSubProcessInstanceId = relationSubWorkflows.stream()
                .map(RelationSubWorkflow::getSubWorkflowInstanceId).collect(Collectors.toList());

        List<ProcessInstance> allSubProcessInstance = processInstanceDao.queryByIds(allSubProcessInstanceId);
        allSubProcessInstance.sort(Comparator.comparing(ProcessInstance::getId));
        return allSubProcessInstance;
    }

    @Override
    public int batchInsertRelationSubWorkflow(List<RelationSubWorkflow> relationSubWorkflowList) {
        int insertN = relationSubWorkflowMapper.batchInsert(relationSubWorkflowList);
        return insertN;
    }

    @Override
    public List<ProcessInstance> filterFinishProcessInstances(List<ProcessInstance> processInstanceList) {
        return processInstanceList.stream()
                .filter(subProcessInstance -> subProcessInstance.getState().isFinished()).collect(Collectors.toList());
    }

    @Override
    public List<ProcessInstance> filterSuccessProcessInstances(List<ProcessInstance> processInstanceList) {
        return processInstanceList.stream()
                .filter(subProcessInstance -> subProcessInstance.getState().isSuccess()).collect(Collectors.toList());
    }

    @Override
    public List<ProcessInstance> filterRunningProcessInstances(List<ProcessInstance> processInstanceList) {
        return processInstanceList.stream()
                .filter(subProcessInstance -> subProcessInstance.getState().isRunning()).collect(Collectors.toList());
    }

    @Override
    public List<ProcessInstance> filterWaitToRunProcessInstances(List<ProcessInstance> processInstanceList) {
        return processInstanceList.stream()
                .filter(subProcessInstance -> subProcessInstance.getState().equals(WorkflowExecutionStatus.WAIT_TO_RUN))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessInstance> filterFailedProcessInstances(List<ProcessInstance> processInstanceList) {
        return processInstanceList.stream()
                .filter(subProcessInstance -> subProcessInstance.getState().isFailure()).collect(Collectors.toList());
    }

    @Override
    public List<Property> getWorkflowOutputParameters(ProcessInstance processInstance) {
        List<Property> outputParamList =
                new ArrayList<>(JSONUtils.toList(processInstance.getVarPool(), Property.class));

        ProcessDefinitionLog processDefinition = processDefinitionLogMapper
                .queryByDefinitionCodeAndVersion(processInstance.getProcessDefinitionCode(),
                        processInstance.getProcessDefinitionVersion());
        List<Property> globalParamList = JSONUtils.toList(processDefinition.getGlobalParams(), Property.class);

        Set<String> ouputParamSet = outputParamList.stream().map(Property::getProp).collect(Collectors.toSet());

        // add output global parameters which are not in output parameters list
        globalParamList.stream().filter(globalParam -> !ouputParamSet.contains(globalParam.getProp()))
                .forEach(outputParamList::add);

        return outputParamList;

    }
}
