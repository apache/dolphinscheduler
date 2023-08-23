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

package org.apache.dolphinscheduler.server.master.runner.task.dynamic;

import org.apache.dolphinscheduler.common.constants.CommandKeyConstants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.RelationSubWorkflow;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.master.ITaskInstanceExecutionEventListener;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstanceStateChangeEvent;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.DynamicInputParameter;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DynamicParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.execute.AsyncTaskExecuteFunction;
import org.apache.dolphinscheduler.server.master.runner.task.BaseAsyncLogicTask;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.subworkflow.SubWorkflowService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;

@Slf4j
public class DynamicLogicTask extends BaseAsyncLogicTask<DynamicParameters> {

    public static final String TASK_TYPE = "DYNAMIC";
    private final ProcessInstanceDao processInstanceDao;

    private final SubWorkflowService subWorkflowService;

    private final ProcessDefinitionMapper processDefineMapper;

    private final CommandMapper commandMapper;

    private final ProcessService processService;

    private ProcessInstance processInstance;

    private TaskInstance taskInstance;

    private boolean haveBeenCanceled = false;

    public DynamicLogicTask(TaskExecutionContext taskExecutionContext,
                            ProcessInstanceDao processInstanceDao,
                            TaskInstanceDao taskInstanceDao,
                            SubWorkflowService subWorkflowService,
                            ProcessService processService,
                            ProcessDefinitionMapper processDefineMapper,
                            CommandMapper commandMapper) {
        super(taskExecutionContext,
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), new TypeReference<DynamicParameters>() {
                }));
        this.processInstanceDao = processInstanceDao;
        this.subWorkflowService = subWorkflowService;
        this.processService = processService;
        this.processDefineMapper = processDefineMapper;
        this.commandMapper = commandMapper;

        this.processInstance = processInstanceDao.queryById(taskExecutionContext.getProcessInstanceId());
        this.taskInstance = taskInstanceDao.queryById(taskExecutionContext.getTaskInstanceId());
    }

    @Override
    public AsyncTaskExecuteFunction getAsyncTaskExecuteFunction() throws MasterTaskExecuteException {
        List<Map<String, String>> parameterGroup = generateParameterGroup();

        if (parameterGroup.size() > taskParameters.getMaxNumOfSubWorkflowInstances()) {
            log.warn("the number of sub process instances [{}] exceeds the maximum limit [{}]", parameterGroup.size(),
                    taskParameters.getMaxNumOfSubWorkflowInstances());
            parameterGroup = parameterGroup.subList(0, taskParameters.getMaxNumOfSubWorkflowInstances());
        }

        // if already exists sub process instance, do not generate again
        List<ProcessInstance> existsSubProcessInstanceList =
                subWorkflowService.getAllDynamicSubWorkflow(processInstance.getId(), taskInstance.getTaskCode());
        if (CollectionUtils.isEmpty(existsSubProcessInstanceList)) {
            generateSubWorkflowInstance(parameterGroup);
        } else {
            resetProcessInstanceStatus(existsSubProcessInstanceList);
        }
        return new DynamicAsyncTaskExecuteFunction(taskExecutionContext, processInstance, taskInstance, this,
                commandMapper,
                subWorkflowService, taskParameters.getDegreeOfParallelism());
    }

    public void resetProcessInstanceStatus(List<ProcessInstance> existsSubProcessInstanceList) {
        switch (processInstance.getCommandType()) {
            case REPEAT_RUNNING:
                existsSubProcessInstanceList.forEach(processInstance -> {
                    processInstance.setState(WorkflowExecutionStatus.WAIT_TO_RUN);
                    processInstanceDao.updateById(processInstance);
                });
                break;
            case START_FAILURE_TASK_PROCESS:
            case RECOVER_TOLERANCE_FAULT_PROCESS:
                List<ProcessInstance> failedProcessInstances =
                        subWorkflowService.filterFailedProcessInstances(existsSubProcessInstanceList);
                failedProcessInstances.forEach(processInstance -> {
                    processInstance.setState(WorkflowExecutionStatus.WAIT_TO_RUN);
                    processInstanceDao.updateById(processInstance);
                });
                break;
        }
    }

    public void generateSubWorkflowInstance(List<Map<String, String>> parameterGroup) throws MasterTaskExecuteException {
        List<ProcessInstance> processInstanceList = new ArrayList<>();
        ProcessDefinition subProcessDefinition =
                processDefineMapper.queryByCode(taskParameters.getProcessDefinitionCode());
        for (Map<String, String> parameters : parameterGroup) {
            String dynamicStartParams = JSONUtils.toJsonString(parameters);
            Command command = DynamicCommandUtils.createCommand(processInstance, subProcessDefinition.getCode(),
                    subProcessDefinition.getVersion(), parameters);
            // todo: set id to -1? we use command to generate sub process instance, but the generate method will use the
            // command id to do
            // somethings
            command.setId(-1);
            DynamicCommandUtils.addDataToCommandParam(command, CommandKeyConstants.CMD_DYNAMIC_START_PARAMS,
                    dynamicStartParams);
            ProcessInstance subProcessInstance = createSubProcessInstance(command);
            subProcessInstance.setState(WorkflowExecutionStatus.WAIT_TO_RUN);
            processInstanceDao.insert(subProcessInstance);
            command.setProcessInstanceId(subProcessInstance.getId());
            processInstanceList.add(subProcessInstance);
        }

        List<RelationSubWorkflow> relationSubWorkflowList = new ArrayList<>();
        for (ProcessInstance subProcessInstance : processInstanceList) {
            RelationSubWorkflow relationSubWorkflow = new RelationSubWorkflow();
            relationSubWorkflow.setParentWorkflowInstanceId(Long.valueOf(processInstance.getId()));
            relationSubWorkflow.setParentTaskCode(taskInstance.getTaskCode());
            relationSubWorkflow.setSubWorkflowInstanceId(Long.valueOf(subProcessInstance.getId()));
            relationSubWorkflowList.add(relationSubWorkflow);
        }

        log.info("Expected number of runs : {}, actual number of runs : {}", parameterGroup.size(),
                processInstanceList.size());

        int insertN = subWorkflowService.batchInsertRelationSubWorkflow(relationSubWorkflowList);
        log.info("insert {} relation sub workflow", insertN);
    }

    public ProcessInstance createSubProcessInstance(Command command) throws MasterTaskExecuteException {
        ProcessInstance subProcessInstance;
        try {
            subProcessInstance = processService.constructProcessInstance(command, processInstance.getHost());
            subProcessInstance.setIsSubProcess(Flag.YES);
            subProcessInstance.setVarPool(taskExecutionContext.getVarPool());
        } catch (Exception e) {
            log.error("create sub process instance error", e);
            throw new MasterTaskExecuteException(e.getMessage());
        }
        return subProcessInstance;
    }

    public List<Map<String, String>> generateParameterGroup() {
        List<DynamicInputParameter> dynamicInputParameters = getDynamicInputParameters();
        Set<String> filterStrings =
                Arrays.stream(StringUtils.split(taskParameters.getFilterCondition(), ",")).map(String::trim)
                        .collect(Collectors.toSet());

        List<List<DynamicInputParameter>> allParameters = new ArrayList<>();
        for (DynamicInputParameter dynamicInputParameter : dynamicInputParameters) {
            List<DynamicInputParameter> singleParameters = new ArrayList<>();
            String value = dynamicInputParameter.getValue();
            String separator = dynamicInputParameter.getSeparator();
            List<String> valueList =
                    Arrays.stream(StringUtils.split(value, separator)).map(String::trim).collect(Collectors.toList());

            valueList = valueList.stream().filter(v -> !filterStrings.contains(v)).collect(Collectors.toList());

            for (String v : valueList) {
                DynamicInputParameter singleParameter = new DynamicInputParameter();
                singleParameter.setName(dynamicInputParameter.getName());
                singleParameter.setValue(v);
                singleParameters.add(singleParameter);
            }
            allParameters.add(singleParameters);
        }

        // use Sets.cartesianProduct to get the cartesian product of all parameters
        List<List<DynamicInputParameter>> cartesianProduct = Lists.cartesianProduct(allParameters);

        // convert cartesian product to parameter group List<Map<name:value>>
        List<Map<String, String>> parameterGroup = cartesianProduct.stream().map(
                inputParameterList -> inputParameterList.stream().collect(
                        Collectors.toMap(DynamicInputParameter::getName, DynamicInputParameter::getValue)))
                .collect(Collectors.toList());

        log.info("parameter group size: {}", parameterGroup.size());
        // log every parameter group
        if (CollectionUtils.isNotEmpty(parameterGroup)) {
            for (Map<String, String> map : parameterGroup) {
                log.info("parameter group: {}", map);
            }
        }
        return parameterGroup;
    }

    private List<DynamicInputParameter> getDynamicInputParameters() {
        List<DynamicInputParameter> dynamicInputParameters = taskParameters.getListParameters();
        if (CollectionUtils.isNotEmpty(dynamicInputParameters)) {
            for (DynamicInputParameter dynamicInputParameter : dynamicInputParameters) {
                String value = dynamicInputParameter.getValue();
                Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
                value = ParameterUtils.convertParameterPlaceholders(value, ParameterUtils.convert(paramsMap));
                dynamicInputParameter.setValue(value);
            }
        }
        return dynamicInputParameters;
    }

    @Override
    public void kill() {
        try {
            changeRunningSubprocessInstancesToStop(WorkflowExecutionStatus.READY_STOP);
        } catch (MasterTaskExecuteException e) {
            log.error("kill {} error", taskInstance.getName(), e);
        }
    }

    private void changeRunningSubprocessInstancesToStop(WorkflowExecutionStatus stopStatus) throws MasterTaskExecuteException {
        this.haveBeenCanceled = true;
        List<ProcessInstance> existsSubProcessInstanceList =
                subWorkflowService.getAllDynamicSubWorkflow(processInstance.getId(), taskInstance.getTaskCode());
        List<ProcessInstance> runningSubProcessInstanceList =
                subWorkflowService.filterRunningProcessInstances(existsSubProcessInstanceList);
        for (ProcessInstance subProcessInstance : runningSubProcessInstanceList) {
            subProcessInstance.setState(stopStatus);
            processInstanceDao.updateById(subProcessInstance);
            if (subProcessInstance.getState().isFinished()) {
                log.info("The process instance [{}] is finished, no need to stop", subProcessInstance.getId());
                return;
            }
            try {
                sendToSubProcess(taskExecutionContext, subProcessInstance);
                log.info("Success send [{}] request to SubWorkflow's master: {}", stopStatus,
                        subProcessInstance.getHost());
            } catch (Exception e) {
                throw new MasterTaskExecuteException(
                        String.format("Send stop request to SubWorkflow's master: %s failed",
                                subProcessInstance.getHost()),
                        e);
            }
        }
    }

    private void sendToSubProcess(TaskExecutionContext taskExecutionContext, ProcessInstance subProcessInstance) {
        final ITaskInstanceExecutionEventListener iTaskInstanceExecutionEventListener =
                SingletonJdkDynamicRpcClientProxyFactory.getInstance()
                        .getProxyClient(subProcessInstance.getHost(), ITaskInstanceExecutionEventListener.class);
        final WorkflowInstanceStateChangeEvent workflowInstanceStateChangeEvent = new WorkflowInstanceStateChangeEvent(
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId(),
                subProcessInstance.getState(),
                subProcessInstance.getId(),
                0);
        iTaskInstanceExecutionEventListener.onWorkflowInstanceInstanceStateChange(workflowInstanceStateChangeEvent);
    }

    public boolean isCancel() {
        return haveBeenCanceled;
    }
}
