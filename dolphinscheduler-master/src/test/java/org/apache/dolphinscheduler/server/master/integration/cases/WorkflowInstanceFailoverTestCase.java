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
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.engine.system.SystemEventBus;
import org.apache.dolphinscheduler.server.master.engine.system.event.GlobalMasterFailoverEvent;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkflowInstanceFailoverTestCase extends AbstractMasterIntegrationTestCase {

    @Autowired
    private SystemEventBus systemEventBus;

    @Test
    public void testGlobalFailover_runningWorkflow_withSubmittedTasks() {
        final String yaml = "/it/failover/running_workflowInstance_with_one_submitted_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(1);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS, "state should success")
                            .matches(t -> t.getFlag() == Flag.YES)
                            .matches(t -> StringUtils.isNotEmpty(t.getLogPath()));
                });

        masterContainer.assertAllResourceReleased();

    }

    @Test
    public void testGlobalFailover_runningWorkflow_withDispatchTasks() {
        final String yaml = "/it/failover/running_workflowInstance_with_one_dispatched_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(2);
                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.NEED_FAULT_TOLERANCE)
                            .matches(t -> t.getFlag() == Flag.NO);

                    assertThat(taskInstances.get(1))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS)
                            .matches(t -> t.getFlag() == Flag.YES)
                            .matches(t -> StringUtils.isNotEmpty(t.getLogPath()));
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testGlobalFailover_runningWorkflow_withRunningTasks() {
        final String yaml = "/it/failover/running_workflowInstance_with_one_running_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(2);
                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.NEED_FAULT_TOLERANCE)
                            .matches(t -> t.getFlag() == Flag.NO);

                    assertThat(taskInstances.get(1))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS)
                            .matches(t -> t.getFlag() == Flag.YES)
                            .matches(t -> StringUtils.isNotEmpty(t.getLogPath()));
                });
        masterContainer.assertAllResourceReleased();

    }

    @Test
    public void testGlobalFailover_runningWorkflow_withSuccessTasks() {
        final String yaml = "/it/failover/running_workflowInstance_with_one_success_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(1);
                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS)
                            .matches(t -> t.getFlag() == Flag.YES);
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testGlobalFailover_runningWorkflow_withFailedTasks() {
        final String yaml = "/it/failover/running_workflowInstance_with_one_failed_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.FAILURE);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(1);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.FAILURE)
                            .matches(t -> t.getFlag() == Flag.YES);
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testGlobalFailover_readyPauseWorkflow_withSubmittedTasks() {
        final String yaml = "/it/failover/readyPause_workflowInstance_with_one_submitted_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.PAUSE);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(1);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.PAUSE)
                            .matches(t -> t.getFlag() == Flag.YES);
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testGlobalFailover_readyPauseWorkflow_withDispatchedTasks() {
        final String yaml = "/it/failover/readyPause_workflowInstance_with_one_dispatched_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        // Since the task take over failed
        // So will create a new task instance and trigger it, but the workflow instance is ready pause
        // The task will be paused.
        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.PAUSE);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(2);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.NEED_FAULT_TOLERANCE)
                            .matches(t -> t.getFlag() == Flag.NO);

                    assertThat(taskInstances.get(1))
                            .matches(t -> t.getState() == TaskExecutionStatus.PAUSE)
                            .matches(t -> t.getFlag() == Flag.YES);
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testGlobalFailover_readyPauseWorkflow_withSuccessTasks() {
        final String yaml = "/it/failover/readyPause_workflowInstance_with_one_success_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(1);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS)
                            .matches(t -> t.getFlag() == Flag.YES);
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testGlobalFailover_readyPauseWorkflow_withFailedTasks() {
        final String yaml = "/it/failover/readyPause_workflowInstance_with_one_failed_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.FAILURE);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(1);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.FAILURE)
                            .matches(t -> t.getFlag() == Flag.YES);
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testGlobalFailover_readyPauseWorkflow_withPausedTasks() {
        final String yaml = "/it/failover/readyPause_workflowInstance_with_one_paused_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.PAUSE);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(1);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.PAUSE)
                            .matches(t -> t.getFlag() == Flag.YES);
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testGlobalFailover_readyStopWorkflow_withSubmittedTasks() {
        final String yaml = "/it/failover/readyStop_workflowInstance_with_one_submitted_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.STOP);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(1);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.KILL)
                            .matches(t -> t.getFlag() == Flag.YES);
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testGlobalFailover_readyStopWorkflow_withDispatchedTasks() {
        final String yaml = "/it/failover/readyStop_workflowInstance_with_one_dispatched_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        // Since the task take over failed
        // So will create a new task instance and trigger it, but the workflow instance is ready stop
        // The task will be killed.
        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.STOP);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(2);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.NEED_FAULT_TOLERANCE)
                            .matches(t -> t.getFlag() == Flag.NO);

                    assertThat(taskInstances.get(1))
                            .matches(t -> t.getState() == TaskExecutionStatus.KILL)
                            .matches(t -> t.getFlag() == Flag.YES);
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testGlobalFailover_readyStopWorkflow_withSuccessTasks() {
        final String yaml = "/it/failover/readyStop_workflowInstance_with_one_success_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(1);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS)
                            .matches(t -> t.getFlag() == Flag.YES);
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testGlobalFailover_readyStopWorkflow_withFailedTasks() {
        final String yaml = "/it/failover/readyStop_workflowInstance_with_one_failed_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.FAILURE);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(1);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.FAILURE)
                            .matches(t -> t.getFlag() == Flag.YES);
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testGlobalFailover_readyStopWorkflow_withKilledTasks() {
        final String yaml = "/it/failover/readyStop_workflowInstance_with_one_killed_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.STOP);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_success-20240816071251690");
                            });
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflow);
                    assertThat(taskInstances)
                            .hasSize(1);

                    assertThat(taskInstances.get(0))
                            .matches(t -> t.getState() == TaskExecutionStatus.KILL)
                            .matches(t -> t.getFlag() == Flag.YES);
                });
        masterContainer.assertAllResourceReleased();
    }

}
