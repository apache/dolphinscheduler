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

package org.apache.dolphinscheduler.server.master.engine.workflow.strategy;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.service.command.CommandService;

import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Workflow execution strategy service for 3.3.x version.
 * <p> This service handles different execution strategies for workflow instances:
 * - PARALLEL: Execute workflow instances in parallel
 * - SERIAL_WAIT: Execute workflow instances in serial order, waiting for previous to complete
 * - SERIAL_DISCARD: Discard new instances and kill running ones
 * - SERIAL_PRIORITY: Execute workflow instances in serial order based on priority
 */
@Slf4j
@Service
public class WorkflowExecutionStrategyServiceImpl implements WorkflowExecutionStrategyService {

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private CommandService commandService;

    @Autowired
    private WorkflowDefinitionLogDao workflowDefinitionLogDao;

    /**
     * Check and apply execution strategy for a new workflow instance.
     *
     * @param workflowDefinition the workflow definition
     * @param workflowInstance   the workflow instance to check
     * @return true if the workflow instance should be executed, false otherwise
     */
    @Override
    public boolean checkAndApplyExecutionStrategy(WorkflowDefinition workflowDefinition,
                                                  WorkflowInstance workflowInstance) {
        WorkflowExecutionTypeEnum executionType = workflowDefinition.getExecutionType();

        switch (executionType) {
            case PARALLEL:
                return handleParallelStrategy(workflowDefinition, workflowInstance);
            case SERIAL_WAIT:
                return handleSerialWaitStrategy(workflowDefinition, workflowInstance);
            case SERIAL_DISCARD:
                return handleSerialDiscardStrategy(workflowDefinition, workflowInstance);
            case SERIAL_PRIORITY:
                return handleSerialPriorityStrategy(workflowDefinition, workflowInstance);
            default:
                log.warn("Unknown execution type: {}, defaulting to parallel", executionType);
                return true;
        }
    }

    /**
     * Handle parallel execution strategy.
     * All workflow instances can run in parallel.
     */
    @Override
    public boolean handleParallelStrategy(WorkflowDefinition workflowDefinition,
                                          WorkflowInstance workflowInstance) {
        log.debug("Applying parallel strategy for workflow: {}", workflowInstance.getName());
        return true;
    }

    /**
     * Handle serial wait execution strategy.
     * New workflow instances will wait for the previous one to complete.
     */
    @Override
    public boolean handleSerialWaitStrategy(WorkflowDefinition workflowDefinition,
                                            WorkflowInstance workflowInstance) {
        log.debug("Applying serial wait strategy for workflow: {}", workflowInstance.getName());

        // Check if there are any running instances of the same workflow definition
        List<WorkflowInstance> runningInstances = workflowInstanceDao.queryByWorkflowDefinitionCodeAndStatus(
                workflowDefinition.getCode(),
                new int[]{WorkflowExecutionStatus.RUNNING_EXECUTION.getCode()});

        if (CollectionUtils.isNotEmpty(runningInstances)) {
            // Set the new instance to SERIAL_WAIT state
            workflowInstance.setStateWithDesc(WorkflowExecutionStatus.SERIAL_WAIT,
                    "wait by serial_wait strategy");
            workflowInstanceDao.upsertWorkflowInstance(workflowInstance);
            log.info("Workflow instance {} set to SERIAL_WAIT state", workflowInstance.getName());
            return false;
        }

        return true;
    }

    /**
     * Handle serial discard execution strategy.
     * New workflow instances will be discarded and running ones will be killed.
     */
    @Override
    public boolean handleSerialDiscardStrategy(WorkflowDefinition workflowDefinition,
                                               WorkflowInstance workflowInstance) {
        log.debug("Applying serial discard strategy for workflow: {}", workflowInstance.getName());

        // Check if there are any running instances of the same workflow definition
        List<WorkflowInstance> runningInstances = workflowInstanceDao.queryByWorkflowDefinitionCodeAndStatus(
                workflowDefinition.getCode(),
                new int[]{WorkflowExecutionStatus.RUNNING_EXECUTION.getCode()});

        if (CollectionUtils.isNotEmpty(runningInstances)) {
            // Kill all running instances
            for (WorkflowInstance runningInstance : runningInstances) {
                killWorkflowInstance(runningInstance);
            }

            // Set the new instance to RUNNING state
            workflowInstance.setStateWithDesc(WorkflowExecutionStatus.RUNNING_EXECUTION,
                    "submit from serial_discard strategy");
            workflowInstanceDao.upsertWorkflowInstance(workflowInstance);
            log.info("Killed {} running instances and started new instance: {}",
                    runningInstances.size(), workflowInstance.getName());
            return true;
        }

        return true;
    }

    /**
     * Handle serial priority execution strategy.
     * <p> Workflow instances will be executed in serial order based on priority.
     */
    @Override
    public boolean handleSerialPriorityStrategy(WorkflowDefinition workflowDefinition,
                                                WorkflowInstance workflowInstance) {
        log.debug("Applying serial priority strategy for workflow: {}", workflowInstance.getName());

        // Check if there are any running instances of the same workflow definition
        List<WorkflowInstance> runningInstances = workflowInstanceDao.queryByWorkflowDefinitionCodeAndStatus(
                workflowDefinition.getCode(),
                new int[]{WorkflowExecutionStatus.RUNNING_EXECUTION.getCode()});

        if (CollectionUtils.isNotEmpty(runningInstances)) {
            // Check if the new instance has higher priority than running instances
            boolean hasHigherPriority = runningInstances.stream()
                    .allMatch(runningInstance -> workflowInstance.getWorkflowInstancePriority()
                            .getCode() > runningInstance.getWorkflowInstancePriority().getCode());

            if (hasHigherPriority) {
                // Kill all running instances and start the new one
                for (WorkflowInstance runningInstance : runningInstances) {
                    killWorkflowInstance(runningInstance);
                }

                workflowInstance.setStateWithDesc(WorkflowExecutionStatus.RUNNING_EXECUTION,
                        "submit from serial_priority strategy with higher priority");
                workflowInstanceDao.upsertWorkflowInstance(workflowInstance);
                log.info("Killed {} running instances and started higher priority instance: {}",
                        runningInstances.size(), workflowInstance.getName());
                return true;
            } else {
                // Set the new instance to SERIAL_WAIT state
                workflowInstance.setStateWithDesc(WorkflowExecutionStatus.SERIAL_WAIT,
                        "wait by serial_priority strategy");
                workflowInstanceDao.upsertWorkflowInstance(workflowInstance);
                log.info("Workflow instance {} set to SERIAL_WAIT state due to lower priority",
                        workflowInstance.getName());
                return false;
            }
        }

        return true;
    }

    /**
     * Kill a workflow instance by creating a stop command.
     */

    @Override
    public void killWorkflowInstance(WorkflowInstance workflowInstance) {
        Command command = new Command();
        command.setCommandType(CommandType.STOP);
        command.setWorkflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode());
        command.setWorkflowDefinitionVersion(workflowInstance.getWorkflowDefinitionVersion());
        command.setWorkflowInstanceId(workflowInstance.getId());
        command.setExecutorId(workflowInstance.getExecutorId());

        Map<String, String> commandParam = new HashMap<>();
        commandParam.put("workflowInstanceId", String.valueOf(workflowInstance.getId()));
        command.setCommandParam(JSONUtils.toJsonString(commandParam));

        commandService.createCommand(command);
        log.info("Created stop command for workflow instance: {}", workflowInstance.getName());
    }

    /**
     * Check and wake up the next serial waiting workflow instance.
     * This method should be called when a workflow instance completes.
     */
    @Override
    public void checkAndWakeUpNextSerialInstance(WorkflowInstance completedInstance) {
        log.info("Checking for next serial waiting instance after workflow instance {} completes",
                completedInstance.getName());

        // Get workflow definition by code and version
        WorkflowDefinition workflowDefinition = workflowDefinitionLogDao.queryByDefinitionCodeAndVersion(
                completedInstance.getWorkflowDefinitionCode(),
                completedInstance.getWorkflowDefinitionVersion());

        if (workflowDefinition == null) {
            log.warn("Cannot find workflow definition for instance: {} (code: {}, version: {})",
                    completedInstance.getName(),
                    completedInstance.getWorkflowDefinitionCode(),
                    completedInstance.getWorkflowDefinitionVersion());
            return;
        }

        WorkflowExecutionTypeEnum executionType = workflowDefinition.getExecutionType();
        log.info("Workflow definition {} has execution type: {}", workflowDefinition.getName(), executionType);

        if (executionType == WorkflowExecutionTypeEnum.SERIAL_WAIT ||
                executionType == WorkflowExecutionTypeEnum.SERIAL_PRIORITY) {

            // Find the next waiting instance
            List<WorkflowInstance> waitingInstances = workflowInstanceDao.queryByWorkflowDefinitionCodeAndStatus(
                    workflowDefinition.getCode(),
                    new int[]{WorkflowExecutionStatus.SERIAL_WAIT.getCode()});

            log.info("Found {} waiting instances for workflow definition {}",
                    waitingInstances.size(), workflowDefinition.getName());

            if (CollectionUtils.isNotEmpty(waitingInstances)) {
                // Sort by priority if it's SERIAL_PRIORITY strategy
                if (executionType == WorkflowExecutionTypeEnum.SERIAL_PRIORITY) {
                    waitingInstances.sort((a, b) -> Integer.compare(b.getWorkflowInstancePriority().getCode(),
                            a.getWorkflowInstancePriority().getCode()));
                }

                // Wake up the first waiting instance
                WorkflowInstance nextInstance = waitingInstances.get(0);
                log.info("Waking up next waiting instance: {}", nextInstance.getName());
                wakeUpSerialWaitingInstance(nextInstance);
            } else {
                log.info("No waiting instances found for workflow definition {}", workflowDefinition.getName());
            }
        } else {
            log.info("Workflow definition {} is not using serial strategy, no need to wake up waiting instances",
                    workflowDefinition.getName());
        }
    }

    /**
     * Wake up a serial waiting workflow instance.
     */
    @Override
    public void wakeUpSerialWaitingInstance(WorkflowInstance waitingInstance) {
        log.info("Creating RECOVER_SERIAL_WAIT command for workflow instance: {} (id: {})",
                waitingInstance.getName(), waitingInstance.getId());

        Command command = new Command();
        command.setCommandType(CommandType.RECOVER_SERIAL_WAIT);
        command.setWorkflowDefinitionCode(waitingInstance.getWorkflowDefinitionCode());
        command.setWorkflowDefinitionVersion(waitingInstance.getWorkflowDefinitionVersion());
        command.setWorkflowInstanceId(waitingInstance.getId());
        command.setExecutorId(waitingInstance.getExecutorId());

        Map<String, String> commandParam = new HashMap<>();
        commandParam.put("workflowInstanceId", String.valueOf(waitingInstance.getId()));
        command.setCommandParam(JSONUtils.toJsonString(commandParam));

        try {
            commandService.createCommand(command);
            log.info("Successfully created RECOVER_SERIAL_WAIT command for workflow instance: {}",
                    waitingInstance.getName());
        } catch (Exception e) {
            log.error("Failed to create RECOVER_SERIAL_WAIT command for workflow instance: {}",
                    waitingInstance.getName(), e);
        }
    }
}
