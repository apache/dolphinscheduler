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

package org.apache.dolphinscheduler.server.master.it.cases;

import static com.google.common.truth.Truth.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTest;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.it.Repository;
import org.apache.dolphinscheduler.server.master.it.WorkflowITContext;
import org.apache.dolphinscheduler.server.master.it.WorkflowITContextFactory;
import org.apache.dolphinscheduler.server.master.it.WorkflowOperator;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The integration test for pausing a workflow instance.
 */
public class WorkflowInstancePauseIT extends AbstractMasterIntegrationTest {

    @Autowired
    private WorkflowITContextFactory workflowITContextFactory;

    @Autowired
    private WorkflowOperator workflowOperator;

    @Autowired
    private IWorkflowRepository workflowRepository;

    @Autowired
    private Repository repository;

    @Test
    @DisplayName("Test pause a workflow with one success task")
    public void testPauseWorkflow_with_oneSuccessTask() {
        final String yaml = "/it/pause/workflow_with_one_fake_task_success.yaml";
        final WorkflowITContext context = workflowITContextFactory.initializeContextFromYaml(yaml);
        final ProcessDefinition workflow = context.getWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .satisfies(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                            });
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
                            });
                });

        assertThat(workflowOperator.pauseWorkflowInstance(workflowInstanceId).isSuccess());

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .satisfies(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                            });
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });
                });

        assertThat(workflowRepository.getAll()).isEmpty();
    }

    @Test
    @DisplayName("Test pause a workflow with one failed task")
    public void testPauseWorkflow_with_oneFailedTask() {
        final String yaml = "/it/pause/workflow_with_one_fake_task_failed.yaml";
        final WorkflowITContext context = workflowITContextFactory.initializeContextFromYaml(yaml);
        final ProcessDefinition workflow = context.getWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .satisfies(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                            });
                    Assertions
                            .assertThat(
                                    repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
                            });
                });

        assertThat(workflowOperator.pauseWorkflowInstance(workflowInstanceId).isSuccess());

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .satisfies(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.FAILURE);
                            });
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            });
                });
        assertThat(workflowRepository.getAll()).isEmpty();
    }

    @Test
    @DisplayName("Test pause a workflow with three parallel three fake tasks(A1->A2->A3, B1->B2->B3, C1->C2->C3) success")
    public void testPauseWorkflow_with_threeParallelSuccessTask() {
        final String yaml = "/it/pause/workflow_with_three_parallel_three_fake_task_success.yaml";
        final WorkflowITContext context = workflowITContextFactory.initializeContextFromYaml(yaml);
        final ProcessDefinition workflow = context.getWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions.assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .satisfies(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                            });
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A1");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B1");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("C1");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
                            });
                });

        assertThat(workflowOperator.pauseWorkflowInstance(workflowInstanceId).isSuccess());

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .satisfies(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.PAUSE);
                            });
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .hasSize(6)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A1");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B1");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("C1");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A2");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.PAUSE);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B2");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.PAUSE);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("C2");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.PAUSE);
                            });
                });
        assertThat(workflowRepository.getAll()).isEmpty();
    }

}
