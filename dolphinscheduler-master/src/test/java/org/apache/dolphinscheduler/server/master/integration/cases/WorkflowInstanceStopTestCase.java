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

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopResponse;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The integration test for stopping a workflow instance.
 */
public class WorkflowInstanceStopTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test stop a workflow with one success task")
    public void testStopWorkflow_with_oneSuccessTask() {
        final String yaml = "/it/stop/workflow_with_one_fake_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                            .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                    assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(
                                    taskInstance -> {
                                        assertThat(taskInstance.getState())
                                                .isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
                                    });
                });

        assertThat(workflowOperator.stopWorkflowInstance(workflowInstanceId))
                .matches(WorkflowInstanceStopResponse::isSuccess);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .matches(workflowInstance -> workflowInstance.getState()
                                    .equals(WorkflowExecutionStatus.STOP));
                    assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(
                                    taskInstance -> {
                                        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.KILL);
                                    });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test stop a workflow with one failed task")
    public void testStopWorkflow_with_oneFailedTask() {
        final String yaml = "/it/stop/workflow_with_one_fake_task_failed.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                            .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                    assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(
                                    taskInstance -> {
                                        assertThat(taskInstance.getState())
                                                .isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
                                    });
                });

        assertThat(workflowOperator.stopWorkflowInstance(workflowInstanceId).isSuccess());
        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .satisfies(
                                    workflowInstance -> {
                                        assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.STOP);
                                    });
                    assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(
                                    taskInstance -> {
                                        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.KILL);
                                    });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test stop a workflow with three parallel three fake tasks(A1->A2->A3, B1->B2->B3, C1->C2->C3) success")
    public void testStopWorkflow_with_threeParallelSuccessTask() {
        final String yaml = "/it/stop/workflow_with_three_parallel_three_fake_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                        .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION)

                );

        // make sure the task has been dispatched to the executor
        ThreadUtils.sleep(2_000);
        assertThat(workflowOperator.stopWorkflowInstance(workflowInstanceId).isSuccess());

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                            .isEqualTo(WorkflowExecutionStatus.STOP);

                    assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .hasSize(3)
                            .anySatisfy(
                                    taskInstance -> {
                                        assertThat(taskInstance.getName()).isEqualTo("A1");
                                        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.KILL);
                                    })
                            .anySatisfy(
                                    taskInstance -> {
                                        assertThat(taskInstance.getName()).isEqualTo("B1");
                                        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.KILL);
                                    })
                            .anySatisfy(
                                    taskInstance -> {
                                        assertThat(taskInstance.getName()).isEqualTo("C1");
                                        assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.KILL);
                                    });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test stop a workflow with one sub workflow task")
    public void testStopWorkflow_with_subWorkflowTask_success() {
        final String yaml = "/it/stop/workflow_with_sub_workflow_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .satisfies(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                            });
                    assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
                            });
                    assertThat(repository.queryWorkflowInstance(context.getWorkflows().get(1)))
                            .satisfiesExactly(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                            });
                });

        assertThat(workflowOperator.stopWorkflowInstance(workflowInstanceId).isSuccess());

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .satisfies(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.STOP);
                                assertThat(workflowInstance.getIsSubWorkflow()).isEqualTo(Flag.NO);
                            });
                    assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("sub_logic_task");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.KILL);
                            });

                    final WorkflowDefinition subWorkflowDefinition = context.getWorkflows().get(1);
                    assertThat(repository.queryWorkflowInstance(subWorkflowDefinition))
                            .satisfiesExactly(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.STOP);
                                assertThat(workflowInstance.getIsSubWorkflow()).isEqualTo(Flag.YES);
                            });

                    assertThat(repository.queryTaskInstance(subWorkflowDefinition))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.KILL);
                            });
                });

        masterContainer.assertAllResourceReleased();
    }
}
