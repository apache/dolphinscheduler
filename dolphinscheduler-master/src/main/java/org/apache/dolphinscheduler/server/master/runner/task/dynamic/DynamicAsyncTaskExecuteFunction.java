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

import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_DYNAMIC_START_PARAMS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.server.master.runner.execute.AsyncTaskExecuteFunction;
import org.apache.dolphinscheduler.service.subworkflow.SubWorkflowService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamicAsyncTaskExecuteFunction implements AsyncTaskExecuteFunction {

    private static final Duration TASK_EXECUTE_STATE_CHECK_INTERVAL = Duration.ofSeconds(10);

    private static final String OUTPUT_KEY = "dynamic.out";

    private final ProcessInstance processInstance;

    private final TaskInstance taskInstance;

    private final SubWorkflowService subWorkflowService;

    private final CommandMapper commandMapper;

    private final int degreeOfParallelism;

    private final DynamicLogicTask logicTask;

    public DynamicAsyncTaskExecuteFunction(TaskExecutionContext taskExecutionContext,
                                           ProcessInstance processInstance,
                                           TaskInstance taskInstance,
                                           DynamicLogicTask dynamicLogicTask,
                                           CommandMapper commandMapper,
                                           SubWorkflowService subWorkflowService,
                                           int degreeOfParallelism) {
        this.processInstance = processInstance;
        this.taskInstance = taskInstance;
        this.logicTask = dynamicLogicTask;
        this.degreeOfParallelism = degreeOfParallelism;

        this.commandMapper = commandMapper;
        this.subWorkflowService = subWorkflowService;
    }

    @Override
    public @NonNull AsyncTaskExecutionStatus getAsyncTaskExecutionStatus() {
        List<ProcessInstance> allSubProcessInstance = getAllSubProcessInstance();
        int totalSubProcessInstanceCount = allSubProcessInstance.size();

        List<ProcessInstance> finishedSubProcessInstance =
                subWorkflowService.filterFinishProcessInstances(allSubProcessInstance);

        if (finishedSubProcessInstance.size() == totalSubProcessInstanceCount) {
            log.info("all sub process instance finish");
            int successCount = subWorkflowService.filterSuccessProcessInstances(finishedSubProcessInstance).size();
            log.info("success sub process instance count: {}", successCount);
            if (successCount == totalSubProcessInstanceCount) {
                log.info("all sub process instance success");
                setOutputParameters();
                return AsyncTaskExecutionStatus.SUCCESS;
            } else {
                int failedCount = totalSubProcessInstanceCount - successCount;
                log.info("failed sub process instance count: {}", failedCount);
                return AsyncTaskExecutionStatus.FAILED;
            }
        }

        if (logicTask.isCancel()) {
            return AsyncTaskExecutionStatus.FAILED;
        }

        int runningCount = subWorkflowService.filterRunningProcessInstances(allSubProcessInstance).size();
        int startCount = degreeOfParallelism - runningCount;
        if (startCount > 0) {
            log.info("There are {} sub process instances that can be started", startCount);
            startSubProcessInstances(allSubProcessInstance, startCount);
        }
        // query the status of sub workflow instance
        return AsyncTaskExecutionStatus.RUNNING;
    }

    private void setOutputParameters() {
        log.info("set varPool");
        List<ProcessInstance> allSubProcessInstance = getAllSubProcessInstance();

        List<DynamicOutput> dynamicOutputs = new ArrayList<>();
        int index = 1;
        for (ProcessInstance processInstance : allSubProcessInstance) {
            DynamicOutput dynamicOutput = new DynamicOutput();
            Map<String, String> dynamicParams =
                    JSONUtils.toMap(JSONUtils.toMap(processInstance.getCommandParam()).get(CMD_DYNAMIC_START_PARAMS));
            dynamicOutput.setDynParams(dynamicParams);

            Map<String, String> outputValueMap = new HashMap<>();
            List<Property> propertyList = subWorkflowService.getWorkflowOutputParameters(processInstance);
            for (Property property : propertyList) {
                outputValueMap.put(property.getProp(), property.getValue());
            }

            dynamicOutput.setOutputValue(outputValueMap);
            dynamicOutput.setMappedTimes(index++);
            dynamicOutputs.add(dynamicOutput);
        }

        Property property = new Property();
        property.setProp(String.format("%s(%s)", OUTPUT_KEY, taskInstance.getName()));
        property.setDirect(Direct.OUT);
        property.setType(DataType.VARCHAR);
        property.setValue(JSONUtils.toJsonString(dynamicOutputs));

        List<Property> taskPropertyList = new ArrayList<>(JSONUtils.toList(taskInstance.getVarPool(), Property.class));
        taskPropertyList.add(property);
        logicTask.getTaskParameters().setVarPool(JSONUtils.toJsonString(taskPropertyList));

        log.info("set property: {}", property);
    }

    private void startSubProcessInstances(List<ProcessInstance> allSubProcessInstance, int startCount) {
        List<ProcessInstance> waitingProcessInstances =
                subWorkflowService.filterWaitToRunProcessInstances(allSubProcessInstance);

        for (int i = 0; i < Math.min(startCount, waitingProcessInstances.size()); i++) {
            ProcessInstance subProcessInstance = waitingProcessInstances.get(i);
            Map<String, String> parameters = JSONUtils.toMap(DynamicCommandUtils
                    .getDataFromCommandParam(subProcessInstance.getCommandParam(), CMD_DYNAMIC_START_PARAMS));
            Command command = DynamicCommandUtils.createCommand(this.processInstance,
                    subProcessInstance.getProcessDefinitionCode(), subProcessInstance.getProcessDefinitionVersion(),
                    parameters);
            command.setProcessInstanceId(subProcessInstance.getId());
            commandMapper.insert(command);
            log.info("start sub process instance, sub process instance id: {}, command: {}", subProcessInstance.getId(),
                    command);
        }
    }

    public List<ProcessInstance> getAllSubProcessInstance() {
        return subWorkflowService.getAllDynamicSubWorkflow(processInstance.getId(), taskInstance.getTaskCode());
    }

    @Override
    public @NonNull Duration getAsyncTaskStateCheckInterval() {
        return TASK_EXECUTE_STATE_CHECK_INTERVAL;
    }

}
