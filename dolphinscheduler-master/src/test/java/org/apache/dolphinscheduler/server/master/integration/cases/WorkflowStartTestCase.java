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

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import org.apache.commons.lang3.time.DateUtils;

import java.time.Duration;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

/**
 * The integration test for starting a workflow from workflow definition.
 * <p> In each test method, will create different workflow from yaml, and then trigger it, and do assertions.
 * <p> The method name should be clear to describe the test scenario.
 */
public class WorkflowStartTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test start a workflow with one fake task(A) success")
    public void testStartWorkflow_with_oneSuccessTask() {
        final String yaml = "/it/start/workflow_with_one_fake_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .matches(
                                    workflowInstance -> workflowInstance.getState() == WorkflowExecutionStatus.SUCCESS)
                            .matches(
                                    workflowInstance -> workflowInstance.getDryRun() == Flag.NO.getCode());
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                                assertThat(taskInstance.getDryRun()).isEqualTo(Flag.NO.getCode());
                            });
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with one fake task(A) dry run success")
    public void testStartWorkflow_with_oneSuccessTaskDryRun() {
        final String yaml = "/it/start/workflow_with_one_fake_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .dryRun(Flag.YES)
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .matches(
                                    workflowInstance -> workflowInstance.getState() == WorkflowExecutionStatus.SUCCESS)
                            .matches(
                                    workflowInstance -> workflowInstance.getDryRun() == Flag.YES.getCode());
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                                assertThat(taskInstance.getDryRun()).isEqualTo(Flag.YES.getCode());
                            });
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with one sub workflow task(A) success")
    public void testStartWorkflow_with_subWorkflowTask_success() {
        final String yaml = "/it/start/workflow_with_sub_workflow_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition parentWorkflow = context.getWorkflows().get(0);

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(parentWorkflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {

                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .matches(
                                    workflowInstance -> workflowInstance.getState() == WorkflowExecutionStatus.SUCCESS)
                            .matches(
                                    workflowInstance -> workflowInstance.getIsSubWorkflow() == Flag.NO)
                            .matches(
                                    workflowInstance -> workflowInstance.getDryRun() == Flag.NO.getCode());

                    final List<WorkflowInstance> subWorkflowInstance =
                            repository.queryWorkflowInstance(context.getWorkflows().get(1));
                    Assertions
                            .assertThat(subWorkflowInstance)
                            .hasSize(1)
                            .satisfiesExactly(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                                assertThat(workflowInstance.getIsSubWorkflow()).isEqualTo(Flag.YES);
                                assertThat(workflowInstance.getDryRun()).isEqualTo(Flag.NO.getCode());
                            });

                    Assertions
                            .assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("sub_logic_task");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });

                    Assertions
                            .assertThat(repository.queryTaskInstance(subWorkflowInstance.get(0).getId()))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("fake_task");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with one sub workflow task(A) dry run, will not execute")
    public void testStartWorkflow_with_subWorkflowTask_dryRunSuccess() {
        final String yaml = "/it/start/workflow_with_sub_workflow_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition parentWorkflow = context.getWorkflows().get(0);

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(parentWorkflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .dryRun(Flag.YES)
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {

                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .matches(
                                    workflowInstance -> workflowInstance.getState() == WorkflowExecutionStatus.SUCCESS)
                            .matches(
                                    workflowInstance -> workflowInstance.getIsSubWorkflow() == Flag.NO)
                            .matches(
                                    workflowInstance -> workflowInstance.getDryRun() == Flag.YES.getCode());

                    final List<WorkflowInstance> subWorkflowInstance =
                            repository.queryWorkflowInstance(context.getWorkflows().get(1));
                    Assertions
                            .assertThat(subWorkflowInstance)
                            .isEmpty();

                    Assertions
                            .assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("sub_logic_task");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                                assertThat(taskInstance.getDryRun()).isEqualTo(Flag.YES.getCode());
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with one sub workflow task(A) failed")
    public void testStartWorkflow_with_subWorkflowTask_failed() {
        final String yaml = "/it/start/workflow_with_sub_workflow_task_failed.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition parentWorkflow = context.getWorkflows().get(0);

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(parentWorkflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {

                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .matches(
                                    workflowInstance -> workflowInstance.getState() == WorkflowExecutionStatus.FAILURE)
                            .matches(
                                    workflowInstance -> workflowInstance.getIsSubWorkflow() == Flag.NO);

                    final List<WorkflowInstance> subWorkflowInstance =
                            repository.queryWorkflowInstance(context.getWorkflows().get(1));
                    Assertions
                            .assertThat(subWorkflowInstance)
                            .hasSize(1)
                            .satisfiesExactly(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.FAILURE);
                                assertThat(workflowInstance.getIsSubWorkflow()).isEqualTo(Flag.YES);
                            });

                    Assertions
                            .assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("sub_logic_task");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            });

                    Assertions
                            .assertThat(repository.queryTaskInstance(subWorkflowInstance.get(0).getId()))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("fake_task");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            });
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow which using workflow params")
    public void testStartWorkflow_usingWorkflowParam() {
        final String yaml = "/it/start/workflow_with_global_param.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

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
                            .hasSize(2)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            });
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow which using command params")
    public void testStartWorkflow_usingCommandParam() {
        final String yaml = "/it/start/workflow_with_global_param.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        final RunWorkflowCommandParam runWorkflowCommandParam = RunWorkflowCommandParam.builder()
                .commandParams(Lists.newArrayList(Property.builder()
                        .prop("name")
                        .direct(Direct.IN)
                        .type(DataType.VARCHAR)
                        .value("commandParam")
                        .build()))
                .build();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(runWorkflowCommandParam)
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
                            .hasSize(2)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with one fake task(A) failed")
    public void testStartWorkflow_with_oneFailedTask() {
        final String yaml = "/it/start/workflow_with_one_fake_task_failed.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

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

    @Test
    @DisplayName("Test start a workflow with one fake task(A) failed")
    public void testStartWorkflow_with_oneFailedTaskWithRetry() {
        final String yaml = "/it/start/workflow_with_one_fake_task_failed_with_retry.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(3))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.FAILURE));

                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    Assertions
                            .assertThat(taskInstances)
                            .allSatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            })
                            .hasSize(2);

                    final TaskInstance taskInstance = taskInstances.get(0);
                    Assertions
                            .assertThat(taskInstance)
                            .matches(task -> task.getRetryTimes() == 0)
                            .matches(task -> task.getFlag() == Flag.NO)
                            .isNotNull();

                    final TaskInstance latestTaskInstance = taskInstances.get(1);
                    Assertions
                            .assertThat(latestTaskInstance)
                            .matches(task -> task.getRetryTimes() == 1)
                            .matches(task -> task.getFlag() == Flag.YES)
                            .isNotNull();
                    assertThat(latestTaskInstance.getFirstSubmitTime()).isEqualTo(taskInstance.getFirstSubmitTime());
                    assertThat(latestTaskInstance.getSubmitTime())
                            .isAtLeast(DateUtils.addSeconds(taskInstance.getSubmitTime(), -65));
                    assertThat(latestTaskInstance.getSubmitTime())
                            .isAtMost(DateUtils.addMinutes(taskInstance.getSubmitTime(), 65));
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with two serial fake tasks(A -> B) success")
    public void testStartWorkflow_with_twoSerialSuccessTask() {
        String yaml = "/it/start/workflow_with_two_serial_fake_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

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
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

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

    @Test
    @DisplayName("Test start a workflow with two parallel fake tasks(A, B) success")
    public void testStartWorkflow_with_twoParallelSuccessTask() {
        final String yaml = "/it/start/workflow_with_two_parallel_fake_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

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
                                    .isEqualTo(WorkflowExecutionStatus.SUCCESS));

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
    @DisplayName("Test start a workflow with two parallel fake tasks(A(failed), B(failed)) success")
    public void testStartWorkflow_with_twoParallelFailedTask() {
        final String yaml = "/it/start/workflow_with_two_parallel_fake_task_failed.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

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
                            .filteredOn(
                                    workflowInstance -> workflowInstance.getState() == WorkflowExecutionStatus.FAILURE)
                            .hasSize(1);

                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    Assertions
                            .assertThat(taskInstances)
                            .hasSize(2)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with three parallel three fake tasks(A1->A2->A3, B1->B2->B3, C1->C2->C3) success")
    public void testStartWorkflow_with_threeParallelSuccessTask() {
        final String yaml = "/it/start/workflow_with_three_parallel_three_fake_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

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
                            .filteredOn(
                                    workflowInstance -> workflowInstance.getState() == WorkflowExecutionStatus.SUCCESS)
                            .hasSize(1);

                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    Assertions
                            .assertThat(taskInstances)
                            .hasSize(9)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A1");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A2");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A3");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B1");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B2");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B3");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("C1");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("C2");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("C3");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with three parallel three fake tasks(A1->A2->A3, B1->B2->B3, C1->C2->C3) success")
    public void testStartWorkflowFromStartNodes_with_threeParallelSuccessTask() {
        final String yaml = "/it/start/workflow_with_three_parallel_three_fake_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        final RunWorkflowCommandParam runWorkflowCommandParam = RunWorkflowCommandParam.builder()
                .startNodes(Lists.newArrayList(6L))
                .build();
        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(runWorkflowCommandParam)
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .filteredOn(
                                    workflowInstance -> workflowInstance.getState() == WorkflowExecutionStatus.SUCCESS)
                            .hasSize(1);

                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    Assertions
                            .assertThat(taskInstances)
                            .hasSize(2)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("C2");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("C3");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow which using workflow built in params")
    public void testStartWorkflow_usingWorkflowBuiltInParam() {
        final String yaml = "/it/start/workflow_with_built_in_param.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

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
                                    .isEqualTo(WorkflowExecutionStatus.SUCCESS));
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
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
}
