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

import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for workflow start timeout and alert scenarios.
 */
public class WorkflowStartTimeoutTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test start a workflow which contains a dep task with timeout kill strategy")
    public void testStartWorkflow_withTimeoutKillTask() {
        final String yaml = "/it/start/workflow_with_timeout_kill_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflow("workflow_with_timeout_kill_task");

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofSeconds(90))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.STOP));
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(1)
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("dep_task_with_timeout_killed");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.KILL);
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow which contains a dep task will be kill by system timeout")
    public void testStartWorkflow_withSystemTimeoutKillTask() {
        masterConfig.getServerLoadProtection().setMaxTaskInstanceRuntime(Duration.ofMinutes(1));

        final String yaml = "/it/start/workflow_with_system_timeout_kill_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflow("workflow_with_timeout_kill_task");

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofSeconds(90))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.STOP));
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(1)
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("dep_task_with_timeout_killed");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.KILL);
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow when timeout should trigger alert when warningGroupId is set")
    public void testWorkflowTimeout_WithAlertGroup_ShouldSendAlert() {
        final String yaml = "/it/start/workflow_with_workflow_timeout_alert.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .warningGroupId(workflow.getWarningGroupId())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await().atMost(Duration.ofMinutes(2))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .matches(
                                    workflowInstance -> workflowInstance.getState() == WorkflowExecutionStatus.SUCCESS);
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("long_running_task");
                                assertThat(taskInstance.getWorkerGroup()).isEqualTo("default");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });
                    Assertions
                            .assertThat(repository.queryAlert(workflowInstanceId))
                            .hasSize(1)
                            .anySatisfy(alert -> {
                                assertThat(alert.getTitle()).isEqualTo("Workflow Timeout Warn");
                                assertThat(alert.getProjectCode()).isEqualTo(1);
                                assertThat(alert.getWorkflowDefinitionCode()).isEqualTo(1);
                                assertThat(alert.getAlertType()).isEqualTo(AlertType.WORKFLOW_INSTANCE_TIMEOUT);
                            });
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow which contains a dep task with timeout warn strategy")
    public void testStartWorkflow_withTimeoutWarnTask() {
        masterConfig.getServerLoadProtection().setEnabled(false);

        final String yaml = "/it/start/workflow_with_timeout_warn_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflow("workflow_with_timeout_warn_task");

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO
                .builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .warningGroupId(workflow.getWarningGroupId())
                .build();

        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofSeconds(90))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(
                                    workflowInstance.getState())
                                            .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION));

                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(1)
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName())
                                        .isEqualTo("dep_task_with_timeout_warn");
                                assertThat(taskInstance.getState())
                                        .isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
                            });

                    Assertions
                            .assertThat(repository.queryAlert(workflowInstanceId))
                            .isNotEmpty()
                            .anySatisfy(alert -> {
                                assertThat(alert.getAlertType())
                                        .isEqualTo(AlertType.TASK_TIMEOUT);
                            });
                });

        workflowOperator.stopWorkflowInstance(workflowInstanceId);
        await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> Assertions.assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                        .matches(w -> w.getState() == WorkflowExecutionStatus.STOP));
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow which contains a dep task with timeout warn failed strategy")
    public void testStartWorkflow_withTimeoutWarnFailedTask() {
        masterConfig.getServerLoadProtection().setEnabled(false);

        final String yaml = "/it/start/workflow_with_timeout_warnfailed_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflow("workflow_with_timeout_warnfailed_task");

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO
                .builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .warningGroupId(workflow.getWarningGroupId())
                .build();

        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofSeconds(90))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(
                                    workflowInstance.getState())
                                            .isEqualTo(WorkflowExecutionStatus.STOP));

                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(1)
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName())
                                        .isEqualTo("dep_task_with_timeout_warnfailed");
                                assertThat(taskInstance.getState())
                                        .isEqualTo(TaskExecutionStatus.KILL);
                            });

                    Assertions
                            .assertThat(repository.queryAlert(workflowInstanceId))
                            .isNotEmpty()
                            .anySatisfy(alert -> {
                                assertThat(alert.getAlertType())
                                        .isEqualTo(AlertType.TASK_TIMEOUT);
                            });
                });

        masterContainer.assertAllResourceReleased();
    }

}
