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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.executor.LogicTaskExecutorContainerProvider;
import org.apache.dolphinscheduler.server.master.engine.executor.LogicTaskExecutorEventRemoteReporterClient;
import org.apache.dolphinscheduler.server.master.engine.executor.LogicTaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.server.master.engine.executor.LogicTaskExecutorRepository;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;
import org.apache.dolphinscheduler.server.master.rpc.LogicTaskExecutorOperatorImpl;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck;
import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "master.task-event-report-max-duration=10s")
class TaskExecutorFinalizationTestCase extends AbstractMasterIntegrationTestCase {

    @SpyBean
    private LogicTaskExecutorOperatorImpl taskExecutorOperator;

    @SpyBean
    private LogicTaskExecutorEventRemoteReporterClient reportClient;

    @Autowired
    private IWorkflowRepository workflowRepository;

    @Autowired
    private LogicTaskExecutorLifecycleEventReporter reporter;

    @Autowired
    private LogicTaskExecutorRepository executors;

    @Autowired
    private LogicTaskExecutorContainerProvider containers;

    @Test
    void shouldReleaseExecutorBeforeLostAckExpires() {
        final AtomicInteger lostAckTaskId = new AtomicInteger();
        doAnswer(invocation -> {
            final TaskExecutorLifecycleEventAck ack = invocation.getArgument(0);
            if (ack.getTaskExecutorLifecycleEventType() == TaskExecutorLifecycleEventType.SUCCESS) {
                lostAckTaskId.set(ack.getTaskExecutorId());
                throw new IllegalStateException("synthetic lost ACK");
            }
            return invocation.callRealMethod();
        }).when(taskExecutorOperator).ackTaskExecutorLifecycleEvent(any(TaskExecutorLifecycleEventAck.class));

        final int workflowId = triggerWorkflow();

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(lostAckTaskId.get()).isPositive();
            assertThat(repository.queryWorkflowInstance(workflowId).getState())
                    .isEqualTo(WorkflowExecutionStatus.SUCCESS);
            assertThat(workflowRepository.contains(workflowId)).isFalse();
            assertExecutorsReleased();
        });
        assertThat(reporter.getEventChannels()).containsKey(lostAckTaskId.get());
        // The workflow has left the cache and cannot ACK the retry. Only the small pending channel remains.
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> assertThat(reporter.getEventChannels()).isEmpty());
        masterContainer.assertAllResourceReleased();
    }

    @Test
    void shouldDeliverRetriedResultAfterExecutorIsAlreadyFinalized() {
        final AtomicBoolean loseFirstResult = new AtomicBoolean(true);
        final AtomicInteger pendingTaskId = new AtomicInteger();
        doAnswer(invocation -> {
            final IReportableTaskExecutorLifecycleEvent event = invocation.getArgument(1);
            if (event.getType() == TaskExecutorLifecycleEventType.SUCCESS && loseFirstResult.getAndSet(false)) {
                event.setLatestReportTime(System.currentTimeMillis());
                pendingTaskId.set(event.getTaskInstanceId());
                return null;
            }
            return invocation.callRealMethod();
        }).when(reportClient).reportTaskExecutionEventToMaster(anyString(),
                any(IReportableTaskExecutorLifecycleEvent.class));

        final int workflowId = triggerWorkflow();

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(pendingTaskId.get()).isPositive();
            assertExecutorsReleased();
            assertThat(repository.queryWorkflowInstance(workflowId).getState())
                    .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
            assertThat(reporter.getEventChannels()).containsKey(pendingTaskId.get());
        });
        // Wake the normal reporter without waiting for its three-minute retry interval.
        assertThat(reporter.onWorkflowInstanceHostChanged(pendingTaskId.get(), masterConfig.getMasterAddress()))
                .isTrue();
        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(repository.queryWorkflowInstance(workflowId).getState())
                        .isEqualTo(WorkflowExecutionStatus.SUCCESS));
        masterContainer.assertAllResourceReleased();
    }

    @Test
    void shouldPreserveOldDispatchedEventUntilNewResultIsDelivered() {
        final AtomicBoolean loseFirstDispatchedEvent = new AtomicBoolean(true);
        final AtomicInteger pendingTaskId = new AtomicInteger();
        doAnswer(invocation -> {
            final IReportableTaskExecutorLifecycleEvent event = invocation.getArgument(1);
            if (event.getType() == TaskExecutorLifecycleEventType.DISPATCHED
                    && loseFirstDispatchedEvent.getAndSet(false)) {
                // The prerequisite is older than the report deadline, but the terminal result will be fresh.
                ((TaskExecutorDispatchedLifecycleEvent) event)
                        .setEventCreateTime(System.currentTimeMillis() - Duration.ofHours(7).toMillis());
                event.setLatestReportTime(System.currentTimeMillis());
                pendingTaskId.set(event.getTaskInstanceId());
                return null;
            }
            return invocation.callRealMethod();
        }).when(reportClient).reportTaskExecutionEventToMaster(anyString(),
                any(IReportableTaskExecutorLifecycleEvent.class));

        final int workflowId = triggerWorkflow();

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(pendingTaskId.get()).isPositive();
            assertExecutorsReleased();
            assertThat(repository.queryTaskInstance(workflowId)).hasSize(1)
                    .allMatch(task -> task.getState() == TaskExecutionStatus.SUBMITTED_SUCCESS);
            assertThat(reporter.getEventChannels()).containsKey(pendingTaskId.get());
            assertThat(reporter.getEventChannels().get(pendingTaskId.get()).peek().getType())
                    .isEqualTo(TaskExecutorLifecycleEventType.DISPATCHED);
        });
        assertThat(reporter.onWorkflowInstanceHostChanged(pendingTaskId.get(), masterConfig.getMasterAddress()))
                .isTrue();

        // Keeping DISPATCHED lets the master accept the following SUCCESS instead of ACKing an ignored result.
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            assertThat(repository.queryTaskInstance(workflowId)).hasSize(1)
                    .allMatch(task -> task.getState() == TaskExecutionStatus.SUCCESS);
            assertThat(repository.queryWorkflowInstance(workflowId).getState())
                    .isEqualTo(WorkflowExecutionStatus.SUCCESS);
        });
        masterContainer.assertAllResourceReleased();
    }

    private int triggerWorkflow() {
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(
                "/it/pause/workflow_with_one_fake_task_success.yaml");
        return workflowOperator.manualTriggerWorkflow(WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(context.getOneWorkflow()).runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build());
    }

    private void assertExecutorsReleased() {
        assertThat(executors.getAll()).isEmpty();
        assertThat(containers.getExecutorContainer().getTaskExecutorAssignmentTable().isEmpty()).isTrue();
        assertThat(containers.getExecutorContainer().getTaskExecutorWorkers().getWorkers())
                .allMatch(worker -> worker.getRegisteredTaskExecutorSize() == 0
                        && worker.getFiredTaskExecutorSize() == 0);
    }
}
