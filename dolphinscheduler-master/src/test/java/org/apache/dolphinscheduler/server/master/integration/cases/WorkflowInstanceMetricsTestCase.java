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

package org.apache.dolphinscheduler.server.master.integration.cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

public class WorkflowInstanceMetricsTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test workflow instance metrics for a successful workflow")
    public void testWorkflowInstanceMetrics_with_oneSuccessTask() {
        final String yaml = "/it/metrics/workflow_with_one_fake_task_success_for_metrics.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();
        final long workflowDefinitionCode = workflow.getCode();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(2))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                            .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                    assertThat(workflowInstanceCount(WorkflowExecutionStatus.SUBMITTED_SUCCESS.name(),
                            workflowDefinitionCode)).isEqualTo(1.0d);
                    assertThat(workflowInstanceCount(WorkflowExecutionStatus.SUCCESS.name(), workflowDefinitionCode))
                            .isEqualTo(1.0d);
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test workflow instance metrics for serial discard strategy")
    public void testWorkflowInstanceMetrics_with_serialDiscardStrategy() {
        final String yaml = "/it/metrics/workflow_with_serial_discard_strategy_for_metrics.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();
        final long workflowDefinitionCode = workflow.getCode();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId1 = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);
        final Integer workflowInstanceId2 = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);
        final Integer workflowInstanceId3 = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(2))
                .untilAsserted(() -> {
                    final WorkflowInstance workflowInstance1 = repository.queryWorkflowInstance(workflowInstanceId1);
                    final WorkflowInstance workflowInstance2 = repository.queryWorkflowInstance(workflowInstanceId2);
                    final WorkflowInstance workflowInstance3 = repository.queryWorkflowInstance(workflowInstanceId3);
                    assertThat(workflowInstance1.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                    assertThat(workflowInstance2.getState()).isEqualTo(WorkflowExecutionStatus.STOP);
                    assertThat(workflowInstance3.getState()).isEqualTo(WorkflowExecutionStatus.STOP);

                    assertThat(workflowInstanceCount(WorkflowExecutionStatus.SUBMITTED_SUCCESS.name(),
                            workflowDefinitionCode)).isEqualTo(1.0d);
                    assertThat(workflowInstanceCount(WorkflowExecutionStatus.SUCCESS.name(), workflowDefinitionCode))
                            .isEqualTo(1.0d);
                    assertThat(workflowInstanceCount(WorkflowExecutionStatus.STOP.name(), workflowDefinitionCode))
                            .isEqualTo(2.0d);
                });

        masterContainer.assertAllResourceReleased();
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
