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
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The integration test for pausing a workflow instance.
 */
public class WorkflowInstanceRecoverPauseTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test recover a a workflow which is paused with one sub workflow task")
    public void testRecoverPausedWorkflow_with_subWorkflowTask_success() {
        final String yaml = "/it/recover_paused/workflow_with_sub_workflow_task_success.yaml";
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
                    assertThat(repository.queryWorkflowInstance(context.getWorkflows().get(1)))
                            .satisfiesExactly(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                            });
                });

        assertThat(workflowOperator.pauseWorkflowInstance(workflowInstanceId).isSuccess()).isTrue();

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .satisfies(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.PAUSE);
                                assertThat(workflowInstance.getIsSubWorkflow()).isEqualTo(Flag.NO);
                            });
                });

        assertThat(workflowOperator.recoverSuspendWorkflowInstance(workflowInstanceId).isSuccess());

        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .matches(
                                    workflowInstance -> workflowInstance.getState() == WorkflowExecutionStatus.SUCCESS);
                    assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });

                    List<WorkflowInstance> subWorkflowInstances =
                            repository.queryWorkflowInstance(context.getWorkflows().get(1));
                    assertThat(subWorkflowInstances)
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.SUCCESS));
                    assertThat(repository.queryTaskInstance(subWorkflowInstances.get(0).getId()))
                            .hasSize(2)
                            .satisfies(taskInstance -> {
                                assertThat(taskInstance.get(0).getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                                assertThat(taskInstance.get(0).getFlag()).isEqualTo(Flag.YES);

                                assertThat(taskInstance.get(1).getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                                assertThat(taskInstance.get(1).getFlag()).isEqualTo(Flag.YES);
                            });
                });

        masterContainer.assertAllResourceReleased();
    }

}
