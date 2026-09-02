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
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for condition task with multiple predecessors in mixed states.
 * <p>
 * Unlike the single-predecessor cases, these cases verify that a condition task with
 * an AND dependency on several predecessors evaluates all of them and routes to the
 * failed branch as soon as any required predecessor does not reach the expected state.
 */
public class WorkflowStartConditionMixedPredecessorTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test start a workflow with one condition task(C) when predecessor A success and predecessor B failed")
    void testStartWorkflow_with_oneConditionTaskAndMixedPredecessorStates_shouldRouteToFailedBranch() {
        final String yaml = "/it/start/workflow_with_one_condition_task_with_mixed_predecessor_states.yaml";
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
                                    workflowInstance -> workflowInstance.getState() == WorkflowExecutionStatus.SUCCESS);

                    Assertions
                            .assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .hasSize(4)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("C");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            // B fails, so the AND condition is not met: only the failed
                            // branch D is executed, the success branch E must be skipped.
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("D");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .noneSatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("E");
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

}
