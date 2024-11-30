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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.dynamic;

import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_DYNAMIC_START_PARAMS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.service.subworkflow.SubWorkflowService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DynamicAsyncTaskExecuteFunction {

    private static final Duration TASK_EXECUTE_STATE_CHECK_INTERVAL = Duration.ofSeconds(10);

    private static final String OUTPUT_KEY = "dynamic.out";

    private final WorkflowInstance workflowInstance;

    private final TaskInstance taskInstance;

    private final SubWorkflowService subWorkflowService;

    private final CommandMapper commandMapper;

    private final int degreeOfParallelism;

    private final DynamicLogicTask logicTask;

    public DynamicAsyncTaskExecuteFunction(TaskExecutionContext taskExecutionContext,
                                           WorkflowInstance workflowInstance,
                                           TaskInstance taskInstance,
                                           DynamicLogicTask dynamicLogicTask,
                                           CommandMapper commandMapper,
                                           SubWorkflowService subWorkflowService,
                                           int degreeOfParallelism) {
        this.workflowInstance = workflowInstance;
        this.taskInstance = taskInstance;
        this.logicTask = dynamicLogicTask;
        this.degreeOfParallelism = degreeOfParallelism;

        this.commandMapper = commandMapper;
        this.subWorkflowService = subWorkflowService;
    }

    public @NonNull TaskExecutionStatus getAsyncTaskExecutionStatus() {
        List<WorkflowInstance> allSubWorkflowInstance = getAllSubProcessInstance();
        int totalSubProcessInstanceCount = allSubWorkflowInstance.size();

        List<WorkflowInstance> finishedSubWorkflowInstance =
                subWorkflowService.filterFinishProcessInstances(allSubWorkflowInstance);

        if (finishedSubWorkflowInstance.size() == totalSubProcessInstanceCount) {
            log.info("all sub process instance finish");
            int successCount = subWorkflowService.filterSuccessProcessInstances(finishedSubWorkflowInstance).size();
            log.info("success sub process instance count: {}", successCount);
            if (successCount == totalSubProcessInstanceCount) {
                log.info("all sub process instance success");
                setOutputParameters();
                return TaskExecutionStatus.SUCCESS;
            } else {
                int failedCount = totalSubProcessInstanceCount - successCount;
                log.info("failed sub process instance count: {}", failedCount);
                return TaskExecutionStatus.FAILURE;
            }
        }

        if (logicTask.isCancel()) {
            return TaskExecutionStatus.FAILURE;
        }

        int runningCount = subWorkflowService.filterRunningProcessInstances(allSubWorkflowInstance).size();
        int startCount = degreeOfParallelism - runningCount;
        if (startCount > 0) {
            log.info("There are {} sub process instances that can be started", startCount);
            startSubProcessInstances(allSubWorkflowInstance, startCount);
        }
        // query the status of sub workflow instance
        return TaskExecutionStatus.RUNNING_EXECUTION;
    }

    private void setOutputParameters() {
        log.info("set varPool");
        List<WorkflowInstance> allSubWorkflowInstance = getAllSubProcessInstance();

        List<DynamicOutput> dynamicOutputs = new ArrayList<>();
        int index = 1;
        for (WorkflowInstance workflowInstance : allSubWorkflowInstance) {
            DynamicOutput dynamicOutput = new DynamicOutput();
            Map<String, String> dynamicParams =
                    JSONUtils.toMap(JSONUtils.toMap(workflowInstance.getCommandParam()).get(CMD_DYNAMIC_START_PARAMS));
            dynamicOutput.setDynParams(dynamicParams);

            Map<String, String> outputValueMap = new HashMap<>();
            List<Property> propertyList = subWorkflowService.getWorkflowOutputParameters(workflowInstance);
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
        // logicTask.getTaskParameters().setVarPool(JSONUtils.toJsonString(taskPropertyList));

        log.info("set property: {}", property);
    }

    private void startSubProcessInstances(List<WorkflowInstance> allSubWorkflowInstance, int startCount) {
        List<WorkflowInstance> waitingWorkflowInstances =
                subWorkflowService.filterWaitToRunProcessInstances(allSubWorkflowInstance);

        for (int i = 0; i < Math.min(startCount, waitingWorkflowInstances.size()); i++) {
            WorkflowInstance subWorkflowInstance = waitingWorkflowInstances.get(i);
            Map<String, String> parameters = JSONUtils.toMap(DynamicCommandUtils
                    .getDataFromCommandParam(subWorkflowInstance.getCommandParam(), CMD_DYNAMIC_START_PARAMS));
            Command command = DynamicCommandUtils.createCommand(this.workflowInstance,
                    subWorkflowInstance.getWorkflowDefinitionCode(), subWorkflowInstance.getWorkflowDefinitionVersion(),
                    parameters);
            command.setWorkflowInstanceId(subWorkflowInstance.getId());
            commandMapper.insert(command);
            log.info("start sub process instance, sub process instance id: {}, command: {}",
                    subWorkflowInstance.getId(),
                    command);
        }
    }

    public List<WorkflowInstance> getAllSubProcessInstance() {
        return subWorkflowService.getAllDynamicSubWorkflow(workflowInstance.getId(), taskInstance.getTaskCode());
    }

}
