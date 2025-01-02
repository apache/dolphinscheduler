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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The integration test for repeat running a workflow instance.
 */
public class WorkflowInstanceRepeatRunningTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test repeat running a workflow instance with one success task")
    public void testRepeatRunningWorkflow_with_oneSuccessTask() {
        final String yaml = "/it/repeat_running/success_workflow_with_one_fake_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);

        final Integer workflowInstanceId = context.getWorkflowInstance().getId();
        workflowOperator.repeatRunningWorkflowInstance(workflowInstanceId);

        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                            .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                });

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    final WorkflowInstance workflowInstance = repository.queryWorkflowInstance(workflowInstanceId);
                    assertThat(workflowInstance)
                            .matches(w -> w.getState() == WorkflowExecutionStatus.SUCCESS)
                            .matches(w -> w.getRunTimes() == 2);

                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflowInstanceId);
                    assertThat(taskInstances)
                            .hasSize(2);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS)
                            .matches(t -> t.getFlag() == Flag.NO);

                    assertThat(taskInstances.get(1))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS)
                            .matches(t -> t.getFlag() == Flag.YES)
                            .matches(t -> StringUtils.isNotEmpty(t.getLogPath()));
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test repeat running a workflow instance with one failed task")
    public void testRepeatRunningWorkflow_with_oneFailedTask() {
        final String yaml = "/it/repeat_running/failed_workflow_with_one_fake_task_failed.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);

        final Integer workflowInstanceId = context.getWorkflowInstance().getId();
        workflowOperator.repeatRunningWorkflowInstance(workflowInstanceId);

        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                            .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                });

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    final WorkflowInstance workflowInstance = repository.queryWorkflowInstance(workflowInstanceId);
                    assertThat(workflowInstance.getState())
                            .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                    assertThat(workflowInstance.getRunTimes())
                            .isEqualTo(2);
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflowInstanceId);
                    assertThat(taskInstances)
                            .hasSize(2);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.FAILURE)
                            .matches(t -> t.getFlag() == Flag.NO);

                    assertThat(taskInstances.get(1))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS)
                            .matches(t -> t.getFlag() == Flag.YES)
                            .matches(t -> StringUtils.isNotEmpty(t.getLogPath()));
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test repeat running a workflow instance with task only")
    public void testRepeatRunningWorkflow_with_taskOnly() {
        final String yaml = "/it/repeat_running/success_workflow_with_task_only.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);

        final Integer workflowInstanceId = context.getWorkflowInstance().getId();
        workflowOperator.repeatRunningWorkflowInstance(workflowInstanceId);

        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                            .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                });

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    final WorkflowInstance workflowInstance = repository.queryWorkflowInstance(workflowInstanceId);
                    assertThat(workflowInstance.getState())
                            .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                    assertThat(workflowInstance.getRunTimes())
                            .isEqualTo(2);
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflowInstanceId);
                    assertThat(taskInstances)
                            .hasSize(2);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS)
                            .matches(t -> t.getFlag() == Flag.NO);

                    assertThat(taskInstances.get(1))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS)
                            .matches(t -> t.getFlag() == Flag.YES)
                            .matches(t -> StringUtils.isNotEmpty(t.getLogPath()));
                });
        masterContainer.assertAllResourceReleased();
    }

}
