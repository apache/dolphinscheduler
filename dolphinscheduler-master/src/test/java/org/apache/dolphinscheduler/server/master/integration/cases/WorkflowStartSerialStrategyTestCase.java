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

import static com.google.common.truth.Truth.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import org.apache.commons.lang3.time.DateUtils;

import java.time.Duration;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for serial workflow start strategy scenarios.
 */
public class WorkflowStartSerialStrategyTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test start a workflow with one fake task(A) using serial wait strategy")
    public void testStartWorkflow_with_serialWaitStrategy() {
        final String yaml = "/it/start/workflow_with_serial_wait_strategy.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId1 = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);
        final Integer workflowInstanceId2 = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);
        final Integer workflowInstanceId3 = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId1).getState())
                            .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId2).getState())
                            .isEqualTo(WorkflowExecutionStatus.SERIAL_WAIT);
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId3).getState())
                            .isEqualTo(WorkflowExecutionStatus.SERIAL_WAIT);
                });

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    final WorkflowInstance workflowInstance1 = repository.queryWorkflowInstance(workflowInstanceId1);
                    final WorkflowInstance workflowInstance2 = repository.queryWorkflowInstance(workflowInstanceId2);
                    final WorkflowInstance workflowInstance3 = repository.queryWorkflowInstance(workflowInstanceId3);
                    assertThat(workflowInstance1.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                    assertThat(workflowInstance2.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                    assertThat(workflowInstance2.getEndTime())
                            .isAtLeast(DateUtils.addSeconds(workflowInstance1.getEndTime(), 5));
                    assertThat(workflowInstance3.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                    assertThat(workflowInstance3.getEndTime())
                            .isAtLeast(DateUtils.addSeconds(workflowInstance2.getEndTime(), 5));
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with one fake task(A) using serial discard strategy")
    public void testStartWorkflow_with_serialDiscardStrategy() {
        final String yaml = "/it/start/workflow_with_serial_discard_strategy.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId1 = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);
        final Integer workflowInstanceId2 = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);
        final Integer workflowInstanceId3 = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    final WorkflowInstance workflowInstance1 = repository.queryWorkflowInstance(workflowInstanceId1);
                    final WorkflowInstance workflowInstance2 = repository.queryWorkflowInstance(workflowInstanceId2);
                    final WorkflowInstance workflowInstance3 = repository.queryWorkflowInstance(workflowInstanceId3);
                    assertThat(workflowInstance1.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                    assertThat(workflowInstance2.getState()).isEqualTo(WorkflowExecutionStatus.STOP);
                    assertThat(workflowInstance2.getEndTime()).isNotNull();
                    assertThat(workflowInstance2.getEndTime()).isAtLeast(workflowInstance2.getStartTime());
                    assertThat(workflowInstance3.getState()).isEqualTo(WorkflowExecutionStatus.STOP);
                    assertThat(workflowInstance3.getEndTime()).isNotNull();
                    assertThat(workflowInstance3.getEndTime()).isAtLeast(workflowInstance3.getStartTime());
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with one fake task(A) using serial priority strategy")
    public void testStartWorkflow_with_serialPriorityStrategy() {
        final String yaml = "/it/start/workflow_with_serial_priority_strategy.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId1 = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);
        final Integer workflowInstanceId2 = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);
        final Integer workflowInstanceId3 = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    final WorkflowInstance workflowInstance1 = repository.queryWorkflowInstance(workflowInstanceId1);
                    final WorkflowInstance workflowInstance2 = repository.queryWorkflowInstance(workflowInstanceId2);
                    final WorkflowInstance workflowInstance3 = repository.queryWorkflowInstance(workflowInstanceId3);
                    assertThat(workflowInstance1.getState()).isEqualTo(WorkflowExecutionStatus.STOP);
                    assertThat(workflowInstance1.getEndTime()).isNotNull();
                    assertThat(workflowInstance1.getEndTime()).isAtLeast(workflowInstance1.getStartTime());
                    assertThat(workflowInstance2.getState()).isEqualTo(WorkflowExecutionStatus.STOP);
                    assertThat(workflowInstance2.getEndTime()).isNotNull();
                    assertThat(workflowInstance2.getEndTime()).isAtLeast(workflowInstance2.getStartTime());
                    assertThat(workflowInstance3.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with two serial fake tasks(A -> B) success")
    public void testStartWorkflow_with_twoSerialSuccessTask() {
        String yaml = "/it/start/workflow_with_two_serial_fake_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.SUCCESS))
                            .hasSize(1);

                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    Assertions
                            .assertThat(taskInstances)
                            .hasSize(2)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with two serial fake tasks(A(failed) -> B) success")
    public void testStartWorkflow_with_twoSerialFailedTask() {
        final String yaml = "/it/start/workflow_with_two_serial_fake_task_failed.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.FAILURE));
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

}
