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

import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import java.time.Duration;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for workflow start task group scenarios.
 */
public class WorkflowStartTaskGroupTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test start a workflow with two fake task(A) using task group")
    public void testStartWorkflow_with_successTaskUsingTaskGroup() {
        final String yaml = "/it/start/workflow_with_fake_tasks_using_task_group.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();

        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(2))
                .atLeast(Duration.ofSeconds(20))
                .untilAsserted(() -> {
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    Assertions
                            .assertThat(taskInstances)
                            .hasSize(2)
                            .allMatch(taskInstance -> TaskExecutionStatus.SUCCESS.equals(taskInstance.getState()) &&
                                    taskInstance.getTaskGroupId() == context.getTaskGroups().get(0).getId());

                    final TaskInstance taskA = taskInstances.stream()
                            .filter(t -> "A".equals(t.getName()))
                            .findFirst().get();
                    final TaskInstance taskB = taskInstances.stream()
                            .filter(t -> "B".equals(t.getName()))
                            .findFirst().get();
                    // TaskA's task group priority is smaller than B
                    Assertions.assertThat(taskA.getStartTime()).isAfter(taskB.getStartTime());
                    Assertions.assertThat(taskA.getEndTime()).isAfter(taskB.getEndTime());

                });

        masterContainer.assertAllResourceReleased();
    }

}
