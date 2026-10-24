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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.executor.LogicTaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.server.master.engine.executor.LogicTaskExecutorRepository;
import org.apache.dolphinscheduler.server.master.engine.task.client.LogicTaskExecutorClientDelegator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

class TaskTerminalAckRecoveryTestCase extends AbstractMasterIntegrationTestCase {

    @SpyBean
    private LogicTaskExecutorClientDelegator taskExecutorClientDelegator;

    @Autowired
    private IWorkflowRepository workflowRepository;

    @Autowired
    private LogicTaskExecutorLifecycleEventReporter taskExecutorEventReporter;

    @Autowired
    private LogicTaskExecutorRepository taskExecutorRepository;

    @Test
    void shouldReleaseExecutorWhenTerminalAckIsRetriedAfterWorkflowFinalization() {
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(
                "/it/start/workflow_with_two_serial_fake_task_success.yaml");
        final AtomicInteger lostAckTaskId = new AtomicInteger();
        final AtomicBoolean allowTerminalAck = new AtomicBoolean();
        doAnswer(invocation -> {
            final TaskInstance taskInstance = invocation.getArgument(0);
            final TaskExecutorLifecycleEventAck ack = invocation.getArgument(1);
            if ("B".equals(taskInstance.getName())
                    && ack.getTaskExecutorLifecycleEventType() == TaskExecutorLifecycleEventType.SUCCESS
                    && !allowTerminalAck.get()) {
                lostAckTaskId.set(taskInstance.getId());
                throw new IllegalStateException("synthetic terminal ACK send failure");
            }
            return invocation.callRealMethod();
        }).when(taskExecutorClientDelegator).ackTaskExecutorLifecycleEvent(
                any(TaskInstance.class), any(TaskExecutorLifecycleEventAck.class));

        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(
                WorkflowOperator.WorkflowTriggerDTO.builder()
                        .workflowDefinition(context.getOneWorkflow())
                        .runWorkflowCommandParam(new RunWorkflowCommandParam())
                        .build());

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                    .isEqualTo(WorkflowExecutionStatus.SUCCESS);
            assertThat(workflowRepository.contains(workflowInstanceId)).isFalse();
        });
        assertThat(lostAckTaskId.get()).isPositive();
        assertThat(taskExecutorEventReporter.getEventChannels()).containsKey(lostAckTaskId.get());
        assertThat(taskExecutorEventReporter.getEventChannels().get(lostAckTaskId.get()).peek().getType())
                .isEqualTo(TaskExecutorLifecycleEventType.SUCCESS);
        assertThat(taskExecutorRepository.get(lostAckTaskId.get())).isPresent();

        // Reset the reporter's retry clock without waiting three minutes. The real reporter thread,
        // RPC listener, database fallback and executor finalization still process the pending report.
        allowTerminalAck.set(true);
        taskExecutorEventReporter.onWorkflowInstanceHostChanged(lostAckTaskId.get());

        masterContainer.assertAllResourceReleased();
    }

    @ParameterizedTest
    @EnumSource(value = WorkflowExecutionStatus.class, names = {"PAUSE", "STOP"})
    void shouldReleaseExecutorWhenFailedRetryIsPausedOrStoppedAfterAckLoss(final WorkflowExecutionStatus finalState) {
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(
                "/it/pause/workflow_with_fake_task_failed_retrying.yaml");
        final AtomicInteger lostAckTaskId = new AtomicInteger();
        final AtomicBoolean allowTerminalAck = new AtomicBoolean();
        doAnswer(invocation -> {
            final TaskInstance taskInstance = invocation.getArgument(0);
            final TaskExecutorLifecycleEventAck ack = invocation.getArgument(1);
            if (ack.getTaskExecutorLifecycleEventType() == TaskExecutorLifecycleEventType.FAILED
                    && !allowTerminalAck.get()) {
                lostAckTaskId.set(taskInstance.getId());
                throw new IllegalStateException("synthetic terminal ACK send failure");
            }
            return invocation.callRealMethod();
        }).when(taskExecutorClientDelegator).ackTaskExecutorLifecycleEvent(
                any(TaskInstance.class), any(TaskExecutorLifecycleEventAck.class));

        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(
                WorkflowOperator.WorkflowTriggerDTO.builder()
                        .workflowDefinition(context.getOneWorkflow())
                        .runWorkflowCommandParam(new RunWorkflowCommandParam())
                        .build());

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(lostAckTaskId.get()).isPositive();
            assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                    .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
            assertThat(repository.queryTaskInstance(workflowInstanceId)).satisfiesExactly(taskInstance -> {
                assertThat(taskInstance.getId()).isEqualTo(lostAckTaskId.get());
                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
            });
        });

        if (finalState == WorkflowExecutionStatus.PAUSE) {
            assertThat(workflowOperator.pauseWorkflowInstance(workflowInstanceId).isSuccess()).isTrue();
        } else {
            assertThat(workflowOperator.stopWorkflowInstance(workflowInstanceId).isSuccess()).isTrue();
        }

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState()).isEqualTo(finalState);
            assertThat(workflowRepository.contains(workflowInstanceId)).isFalse();
            assertThat(repository.queryTaskInstance(workflowInstanceId)).satisfiesExactly(taskInstance -> {
                assertThat(taskInstance.getId()).isEqualTo(lostAckTaskId.get());
                assertThat(taskInstance.getState()).isEqualTo(finalState == WorkflowExecutionStatus.PAUSE
                        ? TaskExecutionStatus.PAUSE
                        : TaskExecutionStatus.KILL);
            });
        });
        assertThat(taskExecutorEventReporter.getEventChannels()).containsKey(lostAckTaskId.get());
        assertThat(taskExecutorEventReporter.getEventChannels().get(lostAckTaskId.get()).peek().getType())
                .isEqualTo(TaskExecutorLifecycleEventType.FAILED);
        assertThat(taskExecutorRepository.get(lostAckTaskId.get())).isPresent();

        allowTerminalAck.set(true);
        taskExecutorEventReporter.onWorkflowInstanceHostChanged(lostAckTaskId.get());

        masterContainer.assertAllResourceReleased();
    }
}
