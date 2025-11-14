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
import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceStopResponse;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.registry.api.utils.RegistryUtils;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.cluster.MasterServerMetadata;
import org.apache.dolphinscheduler.server.master.engine.system.SystemEventBus;
import org.apache.dolphinscheduler.server.master.engine.system.event.GlobalMasterFailoverEvent;
import org.apache.dolphinscheduler.server.master.engine.system.event.MasterFailoverEvent;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkflowInstanceFailoverTestCase extends AbstractMasterIntegrationTestCase {

    @Autowired
    private SystemEventBus systemEventBus;

    @Test
    public void testGlobalFailover_runningWorkflow_withSubmittedTasks() {
        final String yaml = "/it/failover/running_workflowInstance_with_one_submitted_fake_task.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
    public void testGlobalFailover_runningWorkflow_withRunningTasksUsingEnvironment() {
        final String yaml = "/it/failover/running_workflowInstance_with_one_running_fake_task_using_environment.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
                                        .isEqualTo(
                                                "running_workflowInstance_with_one_running_fake_task_using_environment-20240816071251690");
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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
        final WorkflowDefinition workflow = context.getOneWorkflow();

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
    public void testGlobalFailover_runningWorkflow_fromAnotherMaster() {
        final String yaml = "/it/failover/running_workflowInstance_from_another_master.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        final String masterFailoverNodePath = RegistryUtils.getGlobalMasterFailoverNodePath(
                "127.0.0.1:15678");
        // wait failover process
        await()
                .atMost(Duration.ofMinutes(3))
                .untilAsserted(() -> {
                    assertThat(registryClient.exists(masterFailoverNodePath)).isTrue();
                });

        // check workflow's status and can stop it
        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_running-20250322201900000");

                                final WorkflowInstanceStopResponse stopResponse = Clients
                                        .withService(IWorkflowControlClient.class)
                                        .withHost(workflowInstance.getHost())
                                        .stopWorkflowInstance(
                                                new WorkflowInstanceStopRequest(workflowInstance.getId()));

                                assertThat((stopResponse != null && stopResponse.isSuccess())).isTrue();
                            });
                });

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflow))
                            .hasSize(1)
                            .anySatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.STOP);
                                assertThat(workflowInstance.getName())
                                        .isEqualTo("workflow_with_one_fake_task_running-20250322201900000");
                            });
                });

        masterContainer.assertAllResourceReleased();

    }

    @Test
    public void testGlobalFailover_runningWorkflow_takeOverSubWorkflow() {
        final String yaml = "/it/failover/running_workflowInstance_with_sub_workflow_task_running.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition mainWorkflow = context.getWorkflows().stream()
                .filter(workflow -> workflow.getName().equals("workflow_with_one_sub_workflow_running")).findFirst()
                .orElse(null);
        final WorkflowDefinition subWorkflow = context.getWorkflows().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow_running")).findFirst().orElse(null);

        assertThat(mainWorkflow).isNotNull();
        assertThat(subWorkflow).isNotNull();

        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(repository.queryAllWorkflowInstance())
                            .hasSize(2)
                            .allSatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                            });
                });

        assertThat(repository.queryTaskInstance(mainWorkflow))
                .hasSize(2)
                .allSatisfy(taskInstance -> {
                    assertThat(taskInstance.getState())
                            .isEqualTo(taskInstance.getId() == 1 ? TaskExecutionStatus.NEED_FAULT_TOLERANCE
                                    : TaskExecutionStatus.SUCCESS);
                    assertThat(taskInstance.getName())
                            .isEqualTo("sub_workflow_task");
                });

        assertThat(repository.queryTaskInstance(subWorkflow))
                .hasSize(2)
                .allSatisfy(taskInstance -> {
                    assertThat(taskInstance.getState())
                            .isEqualTo(taskInstance.getId() == 2 ? TaskExecutionStatus.NEED_FAULT_TOLERANCE
                                    : TaskExecutionStatus.SUCCESS);
                    assertThat(taskInstance.getName())
                            .isEqualTo("fake_task_A");
                });

        assertThat(repository.queryAllTaskInstance()).hasSize(4);

        masterContainer.assertAllResourceReleased();

    }

    @Test
    public void testMasterFailover_runningWorkflow_takeOverSubWorkflowOnParentHealthy() {
        final String yaml = "/it/failover/running_workflowInstance_with_sub_workflow_task_running_in_diff_master.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition mainWorkflow = context.getWorkflows().stream()
                .filter(workflow -> workflow.getName().equals("workflow_with_one_sub_workflow_running")).findFirst()
                .orElse(null);
        final WorkflowDefinition subWorkflow = context.getWorkflows().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow_running")).findFirst().orElse(null);

        final WorkflowInstance mainWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName()
                        .equals("workflow_with_one_sub_workflow_running-20250424180000000"))
                .findFirst()
                .orElse(null);
        final WorkflowInstance subWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow_running-20250424180000000")).findFirst()
                .orElse(null);

        assertThat(mainWorkflow).isNotNull();
        assertThat(subWorkflow).isNotNull();
        assertThat(mainWorkflowInstance).isNotNull();
        assertThat(subWorkflowInstance).isNotNull();

        MasterServerMetadata masterServerMain = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .address(mainWorkflowInstance.getHost())
                .build();
        MasterServerMetadata masterServerSub = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .address(subWorkflowInstance.getHost())
                .build();

        // first start workflow to simulate the normal parent workflow
        systemEventBus.publish(MasterFailoverEvent.of(masterServerMain, new Date(), 0));

        final String mainMasterFailoverNodePath = RegistryUtils.getFailoveredNodePath(
                masterServerMain.getAddress(),
                masterServerMain.getServerStartupTime(),
                masterServerMain.getProcessId());
        // wait failover main-workflow
        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(registryClient.exists(mainMasterFailoverNodePath)).isTrue();
                });
        // wait main-workflow started
        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(mainWorkflow))
                            .hasSize(1)
                            .allSatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                            });
                });

        // wait sub-workflow-task started
        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(repository.queryTaskInstance(mainWorkflow))
                            .hasSize(2)
                            .allSatisfy(taskInstance -> {
                                assertThat(taskInstance.getState())
                                        .isEqualTo(taskInstance.getId() == 1 ? TaskExecutionStatus.NEED_FAULT_TOLERANCE
                                                : TaskExecutionStatus.RUNNING_EXECUTION);
                                assertThat(taskInstance.getName())
                                        .isEqualTo("sub_workflow_task");
                            });
                });

        // failover sub-workflow
        systemEventBus.publish(MasterFailoverEvent.of(masterServerSub, new Date(), 0));

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(repository.queryAllWorkflowInstance())
                            .hasSize(2)
                            .allSatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                            });
                });

        assertThat(repository.queryTaskInstance(mainWorkflow))
                .hasSize(2)
                .allSatisfy(taskInstance -> {
                    assertThat(taskInstance.getState())
                            .isEqualTo(taskInstance.getId() == 1 ? TaskExecutionStatus.NEED_FAULT_TOLERANCE
                                    : TaskExecutionStatus.SUCCESS);
                    assertThat(taskInstance.getName())
                            .isEqualTo("sub_workflow_task");
                });

        assertThat(repository.queryTaskInstance(subWorkflow))
                .hasSize(2)
                .allSatisfy(taskInstance -> {
                    assertThat(taskInstance.getState())
                            .isEqualTo(taskInstance.getId() == 2 ? TaskExecutionStatus.NEED_FAULT_TOLERANCE
                                    : TaskExecutionStatus.SUCCESS);
                    assertThat(taskInstance.getName())
                            .isEqualTo("fake_task_A");
                });

        assertThat(repository.queryAllTaskInstance()).hasSize(4);

        masterContainer.assertAllResourceReleased();

    }

    @Test
    public void testMasterFailover_runningWorkflow_takeOverSubWorkflowOnChildHealthy() {
        final String yaml = "/it/failover/running_workflowInstance_with_sub_workflow_task_running_in_diff_master.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition mainWorkflow = context.getWorkflows().stream()
                .filter(workflow -> workflow.getName().equals("workflow_with_one_sub_workflow_running")).findFirst()
                .orElse(null);
        final WorkflowDefinition subWorkflow = context.getWorkflows().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow_running")).findFirst().orElse(null);

        final WorkflowInstance mainWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName()
                        .equals("workflow_with_one_sub_workflow_running-20250424180000000"))
                .findFirst()
                .orElse(null);
        final WorkflowInstance subWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow_running-20250424180000000")).findFirst()
                .orElse(null);

        assertThat(mainWorkflow).isNotNull();
        assertThat(subWorkflow).isNotNull();
        assertThat(mainWorkflowInstance).isNotNull();
        assertThat(subWorkflowInstance).isNotNull();

        MasterServerMetadata masterServerMain = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .address(mainWorkflowInstance.getHost())
                .build();
        MasterServerMetadata masterServerSub = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .address(subWorkflowInstance.getHost())
                .build();

        // first start sub-workflow to simulate the normal child workflow
        systemEventBus.publish(MasterFailoverEvent.of(masterServerSub, new Date(), 0));

        final String subMasterFailoverNodePath = RegistryUtils.getFailoveredNodePath(
                masterServerSub.getAddress(),
                masterServerSub.getServerStartupTime(),
                masterServerSub.getProcessId());
        // wait failover sub-workflow
        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(registryClient.exists(subMasterFailoverNodePath)).isTrue();
                });

        // failover main-workflow
        systemEventBus.publish(MasterFailoverEvent.of(masterServerMain, new Date(), 0));

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(repository.queryAllWorkflowInstance())
                            .hasSize(2)
                            .allSatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                            });
                });

        assertThat(repository.queryAllTaskInstance()).filteredOn(
                taskInstance -> taskInstance.getId() > 2 && taskInstance.getState() == TaskExecutionStatus.SUCCESS)
                .hasSize(2);

        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testMasterFailover_runningWorkflow_takeOverSubWorkflowOnChildNotHealthy() {
        final String yaml = "/it/failover/running_workflowInstance_with_sub_workflow_not_running_in_diff_master.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition mainWorkflow = context.getWorkflows().stream()
                .filter(workflow -> workflow.getName().equals("workflow_with_one_sub_workflows")).findFirst()
                .orElse(null);
        final WorkflowDefinition subWorkflow = context.getWorkflows().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow")).findFirst().orElse(null);

        final WorkflowInstance mainWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName().equals("workflow_with_sub_workflow_running-20250424180000000"))
                .findFirst()
                .orElse(null);
        final WorkflowInstance submittedSubWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow_submitted-20250424180000000")).findFirst()
                .orElse(null);

        assertThat(mainWorkflow).isNotNull();
        assertThat(subWorkflow).isNotNull();
        assertThat(mainWorkflowInstance).isNotNull();
        assertThat(submittedSubWorkflowInstance).isNotNull();

        // Failover the main and sub workflow master
        systemEventBus.publish(GlobalMasterFailoverEvent.of(new Date()));
        // wait failover finished
        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(registryClient
                            .exists(RegistryUtils.getGlobalMasterFailoverNodePath(mainWorkflowInstance.getHost())))
                                    .isTrue();
                    assertThat(registryClient.exists(
                            RegistryUtils.getGlobalMasterFailoverNodePath(submittedSubWorkflowInstance.getHost())))
                                    .isTrue();
                });

        // Two workflow instance is success
        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(repository.queryAllWorkflowInstance())
                            .hasSize(2)
                            .satisfies(workflowInstances -> {
                                // Main workflow instance should be success
                                // And the sub-workflow task instance should be success too.
                                assertThat(workflowInstances)
                                        .filteredOn(workflowInstance -> Objects.equals(workflowInstance.getId(),
                                                mainWorkflowInstance.getId()))
                                        .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                                .isEqualTo(WorkflowExecutionStatus.SUCCESS))
                                        .satisfiesExactly(workflowInstance -> {
                                            assertThat(repository.queryTaskInstance(mainWorkflowInstance.getId()))
                                                    .hasSize(2)
                                                    .anySatisfy(taskInstance -> assertThat(taskInstance.getState())
                                                            .isEqualTo(TaskExecutionStatus.NEED_FAULT_TOLERANCE))
                                                    .anySatisfy(taskInstance -> assertThat(taskInstance.getState())
                                                            .isEqualTo(TaskExecutionStatus.SUCCESS));
                                        });
                                // Sub-workflow instance should be success.
                                // The task instance in sub-workflow should be failover.
                                assertThat(workflowInstances)
                                        .filteredOn(workflowInstance -> Objects.equals(workflowInstance.getId(),
                                                submittedSubWorkflowInstance.getId()))
                                        .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                                .isEqualTo(WorkflowExecutionStatus.SUCCESS))
                                        .satisfiesExactly(workflowInstance -> {
                                            assertThat(
                                                    repository.queryTaskInstance(submittedSubWorkflowInstance.getId()))
                                                            .hasSize(1)
                                                            .satisfiesExactly(
                                                                    taskInstance -> assertThat(taskInstance.getState())
                                                                            .isEqualTo(TaskExecutionStatus.SUCCESS));
                                        });
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    public void testMasterFailover_readyStopWorkflow_takeOverSubWorkflowOnChildNotHealthy() {
        final String yaml = "/it/failover/readyStop_workflowInstance_with_sub_workflow_not_running_in_diff_master.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition mainWorkflow = context.getWorkflows().stream()
                .filter(workflow -> workflow.getName().equals("workflow_with_one_sub_workflows")).findFirst()
                .orElse(null);
        final WorkflowDefinition subWorkflow = context.getWorkflows().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow")).findFirst().orElse(null);

        final WorkflowInstance mainWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName().equals("workflow_with_sub_workflow_running-20250424180000000"))
                .findFirst()
                .orElse(null);
        final WorkflowInstance submittedSubWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow_submitted-20250424180000000")).findFirst()
                .orElse(null);

        final WorkflowInstance stopppedSubWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow_stopped-20250424180000000")).findFirst()
                .orElse(null);

        final WorkflowInstance pausedSubWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow_paused-20250424180000000")).findFirst()
                .orElse(null);

        assertThat(mainWorkflow).isNotNull();
        assertThat(subWorkflow).isNotNull();
        assertThat(mainWorkflowInstance).isNotNull();
        assertThat(submittedSubWorkflowInstance).isNotNull();
        assertThat(stopppedSubWorkflowInstance).isNotNull();
        assertThat(pausedSubWorkflowInstance).isNotNull();

        MasterServerMetadata masterServerMain = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .address(mainWorkflowInstance.getHost())
                .build();
        MasterServerMetadata masterServerSub = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .address(submittedSubWorkflowInstance.getHost())
                .build();

        systemEventBus.publish(MasterFailoverEvent.of(masterServerMain, new Date(), 0));
        systemEventBus.publish(MasterFailoverEvent.of(masterServerSub, new Date(), 0));

        final String mainMasterFailoverNodePath = RegistryUtils.getFailoveredNodePath(
                masterServerMain.getAddress(),
                masterServerMain.getServerStartupTime(),
                masterServerMain.getProcessId());
        // wait failover main-workflow
        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(registryClient.exists(mainMasterFailoverNodePath)).isTrue();
                });
        // wait main-workflow stop
        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(mainWorkflow))
                            .hasSize(1)
                            .allSatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.STOP);
                            });
                });

        assertThat(repository.queryAllWorkflowInstance().size()).isEqualTo(4);
        assertThat(repository.queryAllTaskInstance())
                .hasSize(6)
                .filteredOn(taskInstance -> taskInstance.getId() > 4)
                .anySatisfy(taskInstance -> {
                    assertThat(taskInstance.getState())
                            .isEqualTo(TaskExecutionStatus.KILL);
                });

        masterContainer.assertAllResourceReleased();

    }

    @Test
    public void testMasterFailover_readyPauseWorkflow_takeOverSubWorkflowOnChildNotHealthy() {
        final String yaml =
                "/it/failover/readyPause_workflowInstance_with_sub_workflow_not_running_in_diff_master.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition mainWorkflow = context.getWorkflows().stream()
                .filter(workflow -> workflow.getName().equals("workflow_with_one_sub_workflows")).findFirst()
                .orElse(null);
        final WorkflowDefinition subWorkflow = context.getWorkflows().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow")).findFirst().orElse(null);

        final WorkflowInstance mainWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName().equals("workflow_with_sub_workflow_running-20250424180000000"))
                .findFirst()
                .orElse(null);
        final WorkflowInstance submittedSubWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow_submitted-20250424180000000")).findFirst()
                .orElse(null);

        final WorkflowInstance stopppedSubWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow_stopped-20250424180000000")).findFirst()
                .orElse(null);

        final WorkflowInstance pausedSubWorkflowInstance = context.getWorkflowInstances().stream()
                .filter(workflow -> workflow.getName().equals("sub_workflow_paused-20250424180000000")).findFirst()
                .orElse(null);

        assertThat(mainWorkflow).isNotNull();
        assertThat(subWorkflow).isNotNull();
        assertThat(mainWorkflowInstance).isNotNull();
        assertThat(submittedSubWorkflowInstance).isNotNull();
        assertThat(stopppedSubWorkflowInstance).isNotNull();
        assertThat(pausedSubWorkflowInstance).isNotNull();

        MasterServerMetadata masterServerMain = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .address(mainWorkflowInstance.getHost())
                .build();
        MasterServerMetadata masterServerSub = MasterServerMetadata.builder()
                .cpuUsage(0.2)
                .memoryUsage(0.4)
                .serverStatus(ServerStatus.NORMAL)
                .address(submittedSubWorkflowInstance.getHost())
                .build();

        systemEventBus.publish(MasterFailoverEvent.of(masterServerMain, new Date(), 0));
        systemEventBus.publish(MasterFailoverEvent.of(masterServerSub, new Date(), 0));

        final String mainMasterFailoverNodePath = RegistryUtils.getFailoveredNodePath(
                masterServerMain.getAddress(),
                masterServerMain.getServerStartupTime(),
                masterServerMain.getProcessId());
        // wait failover main-workflow
        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(registryClient.exists(mainMasterFailoverNodePath)).isTrue();
                });
        // wait main-workflow stop
        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(mainWorkflow))
                            .hasSize(1)
                            .allSatisfy(workflowInstance -> {
                                assertThat(workflowInstance.getState())
                                        .isEqualTo(WorkflowExecutionStatus.PAUSE);
                            });
                });

        assertThat(repository.queryAllWorkflowInstance().size()).isEqualTo(4);
        assertThat(repository.queryAllTaskInstance())
                .hasSize(6)
                .filteredOn(taskInstance -> taskInstance.getId() > 4)
                .allSatisfy(taskInstance -> {
                    assertThat(taskInstance.getState())
                            .isEqualTo(TaskExecutionStatus.PAUSE);
                });

        masterContainer.assertAllResourceReleased();

    }
}
