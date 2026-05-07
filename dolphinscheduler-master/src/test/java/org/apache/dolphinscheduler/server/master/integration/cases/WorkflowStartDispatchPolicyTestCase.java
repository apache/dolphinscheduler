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
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.config.TaskDispatchPolicy;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for workflow start dispatch policy scenarios.
 */
public class WorkflowStartDispatchPolicyTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test start a workflow whose task specifies a non-existent worker group when dispatch timeout is enabled")
    public void testTaskFail_with_workerGroupNotFoundAndTimeoutEnabled() {
        TaskDispatchPolicy taskDispatchPolicy = new TaskDispatchPolicy();
        taskDispatchPolicy.setDispatchTimeoutEnabled(true);
        taskDispatchPolicy.setMaxTaskDispatchDuration(Duration.ofSeconds(10));
        this.masterConfig.setTaskDispatchPolicy(taskDispatchPolicy);

        final String yaml = "/it/start/workflow_with_worker_group_not_found.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getWorkerGroup()).isEqualTo("workerGroupNotFound");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            });

                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.FAILURE));
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow whose task specifies a non-existent worker group when dispatch timeout is disabled")
    public void testTaskRemainsSubmittedSuccess_with_workerGroupNotFoundAndTimeoutDisabled() {
        TaskDispatchPolicy policy = new TaskDispatchPolicy();
        policy.setDispatchTimeoutEnabled(false);
        this.masterConfig.setTaskDispatchPolicy(policy);

        final String yaml = "/it/start/workflow_with_worker_group_not_found.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getWorkerGroup()).isEqualTo("workerGroupNotFound");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUBMITTED_SUCCESS);
                            });

                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION));

                });

        // This test intentionally leaves the workflow running, so we skip the resource cleanup check.
        // masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow when no available worker and dispatch timeout is enabled")
    public void testTaskFail_with_noAvailableWorkerAndTimeoutEnabled() {
        TaskDispatchPolicy taskDispatchPolicy = new TaskDispatchPolicy();
        taskDispatchPolicy.setDispatchTimeoutEnabled(true);
        taskDispatchPolicy.setMaxTaskDispatchDuration(Duration.ofSeconds(10));
        this.masterConfig.setTaskDispatchPolicy(taskDispatchPolicy);

        final String yaml = "/it/start/workflow_with_no_available_worker.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getWorkerGroup()).isEqualTo("default");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            });

                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.FAILURE));
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow when no available worker and dispatch timeout is disabled")
    public void testTaskRemainsSubmittedSuccess_with_noAvailableWorkerAndTimeoutDisabled() {
        TaskDispatchPolicy policy = new TaskDispatchPolicy();
        policy.setDispatchTimeoutEnabled(false);
        this.masterConfig.setTaskDispatchPolicy(policy);

        final String yaml = "/it/start/workflow_with_no_available_worker.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getWorkerGroup()).isEqualTo("default");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUBMITTED_SUCCESS);
                            });

                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION));
                });

        // This test intentionally leaves the workflow running, so we skip the resource cleanup check.
        // masterContainer.assertAllResourceReleased();
    }

}
