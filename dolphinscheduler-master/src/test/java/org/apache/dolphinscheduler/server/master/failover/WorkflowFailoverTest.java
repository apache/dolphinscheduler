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

package org.apache.dolphinscheduler.server.master.failover;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

@ExtendWith(MockitoExtension.class)
class WorkflowFailoverTest {

    @InjectMocks
    private WorkflowFailover workflowFailover;

    @Mock
    private WorkflowInstanceDao workflowInstanceDao;

    @Mock
    private CommandDao commandDao;

    @Test
    void shouldRecordFailoverMetricWhenFailoverWorkflow() {
        final WorkflowInstance workflowInstance = WorkflowInstance.builder()
                .id(1)
                .name("workflow_instance")
                .workflowDefinitionCode(9527L)
                .workflowDefinitionVersion(1)
                .state(WorkflowExecutionStatus.RUNNING_EXECUTION)
                .build();
        final double failoverBefore = workflowInstanceCount("failover", workflowInstance.getWorkflowDefinitionCode());

        workflowFailover.failoverWorkflow(workflowInstance);

        verify(workflowInstanceDao).updateWorkflowInstanceState(
                workflowInstance.getId(),
                WorkflowExecutionStatus.RUNNING_EXECUTION,
                WorkflowExecutionStatus.FAILOVER);

        final ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(commandDao).insert(commandCaptor.capture());
        assertThat(commandCaptor.getValue().getCommandType()).isEqualTo(CommandType.RECOVER_TOLERANCE_FAULT_PROCESS);
        assertThat(commandCaptor.getValue().getWorkflowInstanceId()).isEqualTo(workflowInstance.getId());

        assertThat(workflowInstanceCount("failover", workflowInstance.getWorkflowDefinitionCode()))
                .isEqualTo(failoverBefore + 1.0d);
    }

    private double workflowInstanceCount(final String state, final long workflowDefinitionCode) {
        final Counter counter = Metrics.globalRegistry.find("ds.workflow.instance.count")
                .tags(
                        "state",
                        state,
                        "workflow.definition.code",
                        String.valueOf(workflowDefinitionCode))
                .counter();
        return counter == null ? 0.0d : counter.count();
    }
}
