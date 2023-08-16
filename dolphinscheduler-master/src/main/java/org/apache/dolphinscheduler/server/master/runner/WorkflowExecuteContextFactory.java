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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.enums.SlotCheckState;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.graph.IWorkflowGraph;
import org.apache.dolphinscheduler.server.master.graph.WorkflowGraphFactory;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowExecuteContextFactory {

    @Autowired
    private ServerNodeManager serverNodeManager;

    @Autowired
    private ProcessService processService;

    @Autowired
    private WorkflowGraphFactory workflowGraphFactory;

    @Autowired
    private MasterConfig masterConfig;

    public Optional<IWorkflowExecuteContext> createWorkflowExecuteRunnableContext(Command command) throws Exception {
        Optional<ProcessInstance> workflowInstanceOptional = createWorkflowInstance(command);
        if (!workflowInstanceOptional.isPresent()) {
            return Optional.empty();
        }
        ProcessInstance workflowInstance = workflowInstanceOptional.get();
        ProcessDefinition workflowDefinition = processService.findProcessDefinition(
                workflowInstance.getProcessDefinitionCode(), workflowInstance.getProcessDefinitionVersion());
        workflowInstance.setProcessDefinition(workflowDefinition);

        IWorkflowGraph workflowGraph = workflowGraphFactory.createWorkflowGraph(workflowInstance);

        return Optional.of(new WorkflowExecuteContext(workflowDefinition, workflowInstance, workflowGraph));
    }

    private Optional<ProcessInstance> createWorkflowInstance(Command command) throws CronParseException {
        long commandTransformStartTime = System.currentTimeMillis();
        // Note: this check is not safe, the slot may change after command transform.
        // We use the database transaction in `handleCommand` so that we can guarantee the command will
        // always be executed
        // by only one master
        SlotCheckState slotCheckState = slotCheck(command);
        if (slotCheckState.equals(SlotCheckState.CHANGE) || slotCheckState.equals(SlotCheckState.INJECT)) {
            log.info("Master handle command {} skip, slot check state: {}", command.getId(), slotCheckState);
            throw new RuntimeException("Slot check failed the current state: " + slotCheckState);
        }
        ProcessInstance processInstance = processService.handleCommand(masterConfig.getMasterAddress(), command);
        ProcessInstanceMetrics
                .recordProcessInstanceGenerateTime(System.currentTimeMillis() - commandTransformStartTime);
        return Optional.ofNullable(processInstance);
    }

    private SlotCheckState slotCheck(Command command) {
        int slot = serverNodeManager.getSlot();
        int masterSize = serverNodeManager.getMasterSize();
        SlotCheckState state;
        if (masterSize <= 0) {
            state = SlotCheckState.CHANGE;
        } else if (command.getId() % masterSize == slot) {
            state = SlotCheckState.PASS;
        } else {
            state = SlotCheckState.INJECT;
        }
        return state;
    }

}
