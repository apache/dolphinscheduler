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

import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
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
 * Integration tests for workflow start switch task scenarios.
 */
public class WorkflowStartSwitchTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test start a workflow with one success switch task and two fake task")
    public void testStartWorkflow_with_oneSuccessSwitch_twoFakeTask() {
        final String yaml = "/it/start/workflow_with_one_success_switch_two_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition parentWorkflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(parentWorkflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .failureStrategy(FailureStrategy.CONTINUE)
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

                    Assertions
                            .assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .hasSize(2)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("switch_task");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("success_branch");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });

                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow with one failed switch task and two fake task")
    public void testStartWorkflow_with_oneFailedSwitch_twoFakeTask() {
        final String yaml = "/it/start/workflow_with_one_failed_switch_two_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition parentWorkflow = context.getOneWorkflow();

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

                    Assertions
                            .assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .hasSize(2)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("switch_task");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("default_branch");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });

                });

        masterContainer.assertAllResourceReleased();
    }

}
