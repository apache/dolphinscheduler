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
import org.apache.dolphinscheduler.dao.entity.RelationSubWorkflow;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopResponse;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
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
    private final WorkflowInstanceDao workflowInstanceDao;

    private final SubWorkflowService subWorkflowService;

    private final WorkflowDefinitionMapper processDefineMapper;

    private final CommandMapper commandMapper;

    private final ProcessService processService;

    private WorkflowInstance workflowInstance;

    private TaskInstance taskInstance;

    private boolean haveBeenCanceled = false;

    public DynamicLogicTask(TaskExecutionContext taskExecutionContext,
                            WorkflowInstanceDao workflowInstanceDao,
                            TaskInstanceDao taskInstanceDao,
                            SubWorkflowService subWorkflowService,
                            ProcessService processService,
                            WorkflowDefinitionMapper processDefineMapper,
                            CommandMapper commandMapper) {
        super(taskExecutionContext,
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), new TypeReference<DynamicParameters>() {
                }));
        this.workflowInstanceDao = workflowInstanceDao;
        this.subWorkflowService = subWorkflowService;
        this.processService = processService;
        this.processDefineMapper = processDefineMapper;
        this.commandMapper = commandMapper;

        this.workflowInstance = workflowInstanceDao.queryById(taskExecutionContext.getProcessInstanceId());
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
        List<WorkflowInstance> existsSubWorkflowInstanceList =
                subWorkflowService.getAllDynamicSubWorkflow(workflowInstance.getId(), taskInstance.getTaskCode());
        if (CollectionUtils.isEmpty(existsSubWorkflowInstanceList)) {
            generateSubWorkflowInstance(parameterGroup);
        } else {
            resetProcessInstanceStatus(existsSubWorkflowInstanceList);
        }
        return new DynamicAsyncTaskExecuteFunction(taskExecutionContext, workflowInstance, taskInstance, this,
                commandMapper,
                subWorkflowService, taskParameters.getDegreeOfParallelism());
    }

    public void resetProcessInstanceStatus(List<WorkflowInstance> existsSubWorkflowInstanceList) {
        switch (workflowInstance.getCommandType()) {
            case REPEAT_RUNNING:
                existsSubWorkflowInstanceList.forEach(processInstance -> {
                    processInstance.setState(WorkflowExecutionStatus.WAIT_TO_RUN);
                    workflowInstanceDao.updateById(processInstance);
                });
                break;
            case START_FAILURE_TASK_WORKFLOW:
            case RECOVER_TOLERANCE_FAULT_WORKFLOW:
                List<WorkflowInstance> failedWorkflowInstances =
                        subWorkflowService.filterFailedProcessInstances(existsSubWorkflowInstanceList);
                failedWorkflowInstances.forEach(processInstance -> {
                    processInstance.setState(WorkflowExecutionStatus.WAIT_TO_RUN);
                    workflowInstanceDao.updateById(processInstance);
                });
                break;
        }
    }

    public void generateSubWorkflowInstance(List<Map<String, String>> parameterGroup) throws MasterTaskExecuteException {
        List<WorkflowInstance> workflowInstanceList = new ArrayList<>();
        WorkflowDefinition subWorkflowDefinition =
                processDefineMapper.queryByCode(taskParameters.getProcessDefinitionCode());
        for (Map<String, String> parameters : parameterGroup) {
            String dynamicStartParams = JSONUtils.toJsonString(parameters);
            Command command = DynamicCommandUtils.createCommand(workflowInstance, subWorkflowDefinition.getCode(),
                    subWorkflowDefinition.getVersion(), parameters);
            // todo: set id to -1? we use command to generate sub process instance, but the generate method will use the
            // command id to do
            // somethings
            command.setId(-1);
            DynamicCommandUtils.addDataToCommandParam(command, CommandKeyConstants.CMD_DYNAMIC_START_PARAMS,
                    dynamicStartParams);
            WorkflowInstance subWorkflowInstance = createSubProcessInstance(command);
            subWorkflowInstance.setState(WorkflowExecutionStatus.WAIT_TO_RUN);
            workflowInstanceDao.insert(subWorkflowInstance);
            command.setProcessInstanceId(subWorkflowInstance.getId());
            workflowInstanceList.add(subWorkflowInstance);
        }

        List<RelationSubWorkflow> relationSubWorkflowList = new ArrayList<>();
        for (WorkflowInstance subWorkflowInstance : workflowInstanceList) {
            RelationSubWorkflow relationSubWorkflow = new RelationSubWorkflow();
            relationSubWorkflow.setParentWorkflowInstanceId(Long.valueOf(workflowInstance.getId()));
            relationSubWorkflow.setParentTaskCode(taskInstance.getTaskCode());
            relationSubWorkflow.setSubWorkflowInstanceId(Long.valueOf(subWorkflowInstance.getId()));
            relationSubWorkflowList.add(relationSubWorkflow);
        }

        log.info("Expected number of runs : {}, actual number of runs : {}", parameterGroup.size(),
                workflowInstanceList.size());

        int insertN = subWorkflowService.batchInsertRelationSubWorkflow(relationSubWorkflowList);
        log.info("insert {} relation sub workflow", insertN);
    }

    public WorkflowInstance createSubProcessInstance(Command command) throws MasterTaskExecuteException {
        WorkflowInstance subWorkflowInstance;
        try {
            subWorkflowInstance = processService.constructProcessInstance(command, workflowInstance.getHost());
            subWorkflowInstance.setIsSubProcess(Flag.YES);
            subWorkflowInstance.setVarPool(taskExecutionContext.getVarPool());
        } catch (Exception e) {
            log.error("create sub process instance error", e);
            throw new MasterTaskExecuteException(e.getMessage());
        }
        return subWorkflowInstance;
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
            doKillSubWorkflowInstances();
        } catch (MasterTaskExecuteException e) {
            log.error("kill {} error", taskInstance.getName(), e);
        }
    }

    private void doKillSubWorkflowInstances() throws MasterTaskExecuteException {
        List<WorkflowInstance> existsSubWorkflowInstanceList =
                subWorkflowService.getAllDynamicSubWorkflow(workflowInstance.getId(), taskInstance.getTaskCode());
        if (CollectionUtils.isEmpty(existsSubWorkflowInstanceList)) {
            return;
        }

        commandMapper.deleteByWorkflowInstanceIds(
                existsSubWorkflowInstanceList.stream().map(WorkflowInstance::getId).collect(Collectors.toList()));

        List<WorkflowInstance> runningSubWorkflowInstanceList =
                subWorkflowService.filterRunningProcessInstances(existsSubWorkflowInstanceList);
        doKillRunningSubWorkflowInstances(runningSubWorkflowInstanceList);

        List<WorkflowInstance> waitToRunWorkflowInstances =
                subWorkflowService.filterWaitToRunProcessInstances(existsSubWorkflowInstanceList);
        doKillWaitToRunSubWorkflowInstances(waitToRunWorkflowInstances);

        this.haveBeenCanceled = true;
    }

    private void doKillRunningSubWorkflowInstances(List<WorkflowInstance> runningSubWorkflowInstanceList) throws MasterTaskExecuteException {
        for (WorkflowInstance subWorkflowInstance : runningSubWorkflowInstanceList) {
            try {
                WorkflowInstanceStopResponse workflowInstanceStopResponse = Clients
                        .withService(IWorkflowControlClient.class)
                        .withHost(subWorkflowInstance.getHost())
                        .stopWorkflowInstance(new WorkflowInstanceStopRequest(subWorkflowInstance.getId()));
                if (workflowInstanceStopResponse.isSuccess()) {
                    log.info("Stop SubWorkflow: {} successfully", subWorkflowInstance.getName());
                } else {
                    throw new MasterTaskExecuteException(
                            "Stop subWorkflow: " + subWorkflowInstance.getName() + " failed");
                }
            } catch (MasterTaskExecuteException me) {
                throw me;
            } catch (Exception e) {
                throw new MasterTaskExecuteException(
                        String.format("Send stop request to SubWorkflow's master: %s failed",
                                subWorkflowInstance.getHost()),
                        e);
            }
        }
    }

    private void doKillWaitToRunSubWorkflowInstances(List<WorkflowInstance> waitToRunWorkflowInstances) {
        for (WorkflowInstance subWorkflowInstance : waitToRunWorkflowInstances) {
            subWorkflowInstance.setState(WorkflowExecutionStatus.STOP);
            workflowInstanceDao.updateById(subWorkflowInstance);
        }
    }

    public boolean isCancel() {
        return haveBeenCanceled;
    }
}
