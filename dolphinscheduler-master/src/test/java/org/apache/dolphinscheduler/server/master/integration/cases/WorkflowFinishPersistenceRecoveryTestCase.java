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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.SerialCommand;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowFinalizeLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowStoppedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowSucceedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.handler.WorkflowFailedLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.handler.WorkflowFinalizeLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.handler.WorkflowPausedLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.handler.WorkflowStoppedLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.handler.WorkflowSucceedLifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.serial.WorkflowSerialCoordinator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;
import org.apache.dolphinscheduler.server.master.metrics.WorkflowInstanceMetrics;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

public class WorkflowFinishPersistenceRecoveryTestCase extends AbstractMasterIntegrationTestCase {

    private enum Fault {
        UPDATE, SERIAL_DELETE, BEFORE_COMMIT
    }

    @SpyBean
    private WorkflowInstanceDao workflowDao;
    @SpyBean
    private SerialCommandDao serialDao;
    @SpyBean
    private WorkflowFinalizeLifecycleEventHandler finalizeHandler;
    @Autowired
    private IWorkflowRepository workflowRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private WorkflowSerialCoordinator serialCoordinator;
    @Autowired
    private WorkflowSucceedLifecycleEventHandler succeedHandler;
    @Autowired
    private WorkflowFailedLifecycleEventHandler failedHandler;
    @Autowired
    private WorkflowStoppedLifecycleEventHandler stoppedHandler;
    @Autowired
    private WorkflowPausedLifecycleEventHandler pausedHandler;

    private static Stream<Arguments> finishFailures() {
        return Stream.of(WorkflowExecutionStatus.SUCCESS, WorkflowExecutionStatus.FAILURE,
                WorkflowExecutionStatus.STOP, WorkflowExecutionStatus.PAUSE)
                .flatMap(state -> Stream.of(Fault.values()).map(fault -> Arguments.of(state, fault)));
    }

    @ParameterizedTest
    @MethodSource("finishFailures")
    void shouldRetryFinishingAfterTransactionFailure(final WorkflowExecutionStatus target, final Fault fault) {
        final Fixture fixture = new Fixture(target, true);
        fixture.failFirst(fault);

        assertThatThrownBy(fixture::deliver).isInstanceOf(DataAccessResourceFailureException.class);
        fixture.assertNotFinished();

        fixture.deliver();
        fixture.assertFinished();
        assertThat(fixture.updateAttempts).hasValue(2);

        // The real handler selects the terminal-state action for a repeated report.
        fixture.deliver();
        fixture.assertFinished();
        assertThat(fixture.updateAttempts).hasValue(2);
    }

    private static Stream<Arguments> pendingFinishFailures() {
        return Stream.of(
                Arguments.of(WorkflowExecutionStatus.READY_PAUSE, WorkflowExecutionStatus.SUCCESS),
                Arguments.of(WorkflowExecutionStatus.READY_PAUSE, WorkflowExecutionStatus.FAILURE),
                Arguments.of(WorkflowExecutionStatus.READY_STOP, WorkflowExecutionStatus.STOP),
                Arguments.of(WorkflowExecutionStatus.READY_STOP, WorkflowExecutionStatus.SUCCESS),
                Arguments.of(WorkflowExecutionStatus.READY_STOP, WorkflowExecutionStatus.FAILURE))
                .flatMap(transition -> Stream.of(Fault.values())
                        .map(fault -> Arguments.of(transition.get()[0], transition.get()[1], fault)));
    }

    @ParameterizedTest
    @MethodSource("pendingFinishFailures")
    void shouldRetryFinishingFromPendingState(final WorkflowExecutionStatus originalState,
                                              final WorkflowExecutionStatus target,
                                              final Fault fault) {
        final Fixture fixture = new Fixture(originalState, target, true);
        fixture.failFirst(fault);

        assertThatThrownBy(fixture::deliver).isInstanceOf(DataAccessResourceFailureException.class);
        fixture.assertNotFinished();

        fixture.deliver();
        fixture.assertFinished();
        assertThat(fixture.updateAttempts).hasValue(2);

        fixture.deliver();
        fixture.assertFinished();
        assertThat(fixture.updateAttempts).hasValue(2);
    }

    private static Stream<Arguments> nonSuccessRecoveryFailures() {
        // Parallel workflows have no serial-command cleanup; that failure is covered by the transition matrix.
        return Stream.of(WorkflowExecutionStatus.FAILURE, WorkflowExecutionStatus.STOP, WorkflowExecutionStatus.PAUSE)
                .flatMap(target -> Stream.of(Fault.UPDATE, Fault.BEFORE_COMMIT)
                        .map(fault -> Arguments.of(target, fault)));
    }

    @ParameterizedTest
    @MethodSource("nonSuccessRecoveryFailures")
    void shouldRecoverNonSuccessFinishThroughTheEventBus(final WorkflowExecutionStatus target, final Fault fault) {
        final String yaml;
        final WorkflowExecutionStatus originalState;
        switch (target) {
            case FAILURE:
                yaml = "/it/pause/workflow_with_one_fake_task_failed.yaml";
                originalState = WorkflowExecutionStatus.RUNNING_EXECUTION;
                break;
            case STOP:
                yaml = "/it/stop/workflow_with_one_fake_task_success.yaml";
                originalState = WorkflowExecutionStatus.READY_STOP;
                break;
            case PAUSE:
                // A running fake task cannot pause itself. Its successors pause after it completes.
                yaml = "/it/pause/workflow_with_three_parallel_three_fake_task_success.yaml";
                originalState = WorkflowExecutionStatus.READY_PAUSE;
                break;
            default:
                throw new IllegalArgumentException(target.name());
        }
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final AtomicInteger finishAttempts = new AtomicInteger();
        final AtomicReference<WorkflowInstance> sharedInstance = new AtomicReference<>();
        doAnswer(call -> {
            final WorkflowInstance update = call.getArgument(0);
            if (update.getState() != target) {
                return call.callRealMethod();
            }
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            final WorkflowInstance shared = workflowRepository.get(update.getId()).getWorkflowInstance();
            sharedInstance.set(shared);
            // Also checked on the automatic retry: the first attempt must have rolled back in H2 and memory.
            assertThat(shared.getState()).isEqualTo(originalState);
            assertThat(shared.getEndTime()).isNull();
            final WorkflowInstance persisted = workflowDao.queryById(update.getId());
            assertThat(persisted.getState()).isEqualTo(originalState);
            assertThat(persisted.getEndTime()).isNull();
            if (finishAttempts.incrementAndGet() == 1) {
                if (fault == Fault.UPDATE) {
                    throw new DataAccessResourceFailureException("synthetic workflow update failure");
                }
                final Object result = call.callRealMethod();
                registerCommitFailure();
                return result;
            }
            return call.callRealMethod();
        }).when(workflowDao).updateById(any(WorkflowInstance.class));

        final Integer workflowId = workflowOperator.manualTriggerWorkflow(WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(context.getOneWorkflow())
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build());
        if (target == WorkflowExecutionStatus.STOP || target == WorkflowExecutionStatus.PAUSE) {
            await().pollInterval(Duration.ofMillis(50)).atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
                assertThat(repository.queryWorkflowInstance(workflowId).getState())
                        .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                assertThat(repository.queryTaskInstance(workflowId))
                        .hasSize(target == WorkflowExecutionStatus.PAUSE ? 3 : 1)
                        .extracting(TaskInstance::getState).containsOnly(TaskExecutionStatus.RUNNING_EXECUTION);
            });
            if (target == WorkflowExecutionStatus.STOP) {
                assertThat(workflowOperator.stopWorkflowInstance(workflowId).isSuccess()).isTrue();
            } else {
                assertThat(workflowOperator.pauseWorkflowInstance(workflowId).isSuccess()).isTrue();
            }
        }
        await().atMost(Duration.ofSeconds(40)).untilAsserted(() -> {
            assertThat(repository.queryWorkflowInstance(workflowId).getState()).isEqualTo(target);
            assertThat(finishAttempts).hasValue(2);
            assertThat(sharedInstance.get()).isNotNull();
            assertThat(sharedInstance.get().getState()).isEqualTo(target);
            assertThat(sharedInstance.get().getEndTime()).isNotNull()
                    .isEqualTo(repository.queryWorkflowInstance(workflowId).getEndTime());
        });
        masterContainer.assertAllResourceReleased();
        verify(finalizeHandler, times(1)).handle(any(IWorkflowExecution.class),
                any(WorkflowFinalizeLifecycleEvent.class));
        verify(serialDao, never()).deleteByWorkflowInstanceId(anyInt());
        if (target == WorkflowExecutionStatus.PAUSE) {
            assertThat(repository.queryTaskInstance(workflowId)).hasSize(6)
                    .extracting(TaskInstance::getName).doesNotHaveDuplicates();
            assertThat(repository.queryTaskInstance(workflowId)).extracting(TaskInstance::getState)
                    .containsExactlyInAnyOrder(TaskExecutionStatus.SUCCESS, TaskExecutionStatus.SUCCESS,
                            TaskExecutionStatus.SUCCESS, TaskExecutionStatus.PAUSE,
                            TaskExecutionStatus.PAUSE, TaskExecutionStatus.PAUSE);
        } else {
            assertThat(repository.queryTaskInstance(workflowId)).hasSize(1)
                    .extracting(TaskInstance::getState)
                    .containsExactly(target == WorkflowExecutionStatus.STOP
                            ? TaskExecutionStatus.KILL
                            : TaskExecutionStatus.FAILURE);
        }
    }

    @ParameterizedTest
    @EnumSource(value = WorkflowExecutionStatus.class, names = {"SUCCESS", "FAILURE", "STOP", "PAUSE"})
    void shouldPublishCompletionOnlyAfterCommit(final WorkflowExecutionStatus target) {
        final Fixture fixture = new Fixture(target, true);
        transactionTemplate.execute(status -> {
            fixture.deliver();
            // SQL has executed, but the enclosing transaction has not committed.
            assertThat(workflowDao.queryById(fixture.shared.getId()).getState()).isEqualTo(target);
            assertThat(serialDao.queryById(fixture.commandId)).isNull();
            fixture.assertSharedUnchanged();
            return null;
        });
        fixture.assertFinished();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldRemainRetryableAfterEnclosingTransactionRollsBack(final boolean failBeforeCommit) {
        final Fixture fixture = new Fixture(WorkflowExecutionStatus.SUCCESS, true);
        if (failBeforeCommit) {
            assertThatThrownBy(() -> transactionTemplate.execute(status -> {
                fixture.deliver();
                registerCommitFailure();
                return null;
            })).isInstanceOf(DataAccessResourceFailureException.class);
        } else {
            transactionTemplate.execute(status -> {
                fixture.deliver();
                status.setRollbackOnly();
                return null;
            });
        }
        fixture.assertNotFinished();
        fixture.deliver();
        fixture.assertFinished();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldFinishNormallyForParallelAndSerialWorkflows(final boolean serial) {
        final Fixture fixture = new Fixture(WorkflowExecutionStatus.SUCCESS, serial);
        fixture.deliver();
        fixture.assertFinished();
        if (!serial) {
            verify(serialDao, never()).deleteByWorkflowInstanceId(anyInt());
        }
    }

    @ParameterizedTest
    @EnumSource(Fault.class)
    void shouldRecoverThroughTheEventBusAndReleaseTheSerialQueue(final Fault fault) {
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(
                "/it/start/workflow_with_serial_wait_strategy.yaml");
        final AtomicInteger firstWorkflowId = new AtomicInteger();
        final AtomicInteger finishAttempts = new AtomicInteger();
        doAnswer(call -> {
            final WorkflowInstance instance = call.getArgument(0);
            if (instance.getState() == WorkflowExecutionStatus.SUCCESS) {
                firstWorkflowId.compareAndSet(0, instance.getId());
                if (instance.getId() == firstWorkflowId.get()) {
                    assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
                    if (finishAttempts.incrementAndGet() == 1 && fault == Fault.UPDATE) {
                        throw new DataAccessResourceFailureException("synthetic workflow update failure");
                    }
                }
            }
            return call.callRealMethod();
        }).when(workflowDao).updateById(any(WorkflowInstance.class));
        doAnswer(call -> {
            final Integer workflowId = call.getArgument(0);
            if (workflowId == firstWorkflowId.get() && finishAttempts.get() == 1) {
                if (fault == Fault.SERIAL_DELETE) {
                    throw new DataAccessResourceFailureException("synthetic serial cleanup failure");
                }
                if (fault == Fault.BEFORE_COMMIT) {
                    registerCommitFailure();
                }
            }
            return call.callRealMethod();
        }).when(serialDao).deleteByWorkflowInstanceId(anyInt());

        final WorkflowOperator.WorkflowTriggerDTO trigger = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(context.getOneWorkflow())
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer firstId = workflowOperator.manualTriggerWorkflow(trigger);
        final Integer secondId = workflowOperator.manualTriggerWorkflow(trigger);
        await().atMost(Duration.ofSeconds(40)).untilAsserted(() -> {
            assertThat(repository.queryWorkflowInstance(firstId).getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
            assertThat(repository.queryWorkflowInstance(secondId).getState())
                    .isEqualTo(WorkflowExecutionStatus.SUCCESS);
            assertThat(repository.queryTaskInstance(context.getOneWorkflow()))
                    .hasSize(2)
                    .extracting(TaskInstance::getState)
                    .containsOnly(TaskExecutionStatus.SUCCESS);
            assertThat(serialDao.fetchSerialCommands(10)).isEmpty();
        });
        assertThat(finishAttempts).hasValue(2);
        masterContainer.assertAllResourceReleased();
    }

    @ParameterizedTest
    @EnumSource(value = WorkflowExecutionStatus.class, names = {"SUCCESS", "FAILURE", "STOP", "PAUSE"})
    void shouldFinishOnlyOnceWithinEnclosingTransaction(final WorkflowExecutionStatus target) {
        final Fixture fixture = new Fixture(target, true);
        transactionTemplate.execute(status -> {
            fixture.deliver();
            fixture.deliver();
            return null;
        });
        fixture.assertFinished();
        verify(workflowDao, times(1)).updateById(any(WorkflowInstance.class));
    }

    @Test
    void shouldKeepFirstFinishWhenConflictingEventArrivesBeforeCommit() {
        final Fixture fixture = new Fixture(WorkflowExecutionStatus.SUCCESS, true);
        transactionTemplate.execute(status -> {
            fixture.deliver();
            failedHandler.handle(fixture.workflow, WorkflowFailedLifecycleEvent.of(fixture.workflow));
            fixture.assertSharedUnchanged();
            return null;
        });
        fixture.assertFinished();
        verify(workflowDao, times(1)).updateById(any(WorkflowInstance.class));
    }

    @Test
    void shouldFinishDifferentWorkflowsInTheSameTransaction() {
        final Fixture fixture = new Fixture(WorkflowExecutionStatus.SUCCESS, false);
        final WorkflowInstance secondInstance = new WorkflowInstance();
        BeanUtils.copyProperties(fixture.shared, secondInstance);
        secondInstance.setId(null);
        workflowDao.insert(secondInstance);
        final IWorkflowExecution secondWorkflow = mock(IWorkflowExecution.class);
        final WorkflowEventBus secondBus = mock(WorkflowEventBus.class);
        when(secondWorkflow.getWorkflowInstance()).thenReturn(secondInstance);
        when(secondWorkflow.getState()).thenAnswer(call -> secondInstance.getState());
        final IWorkflowExecuteContext secondContext = fixture.workflow.getWorkflowExecuteContext();
        final IWorkflowExecutionGraph secondGraph = fixture.workflow.getWorkflowExecutionGraph();
        when(secondWorkflow.getWorkflowExecuteContext()).thenReturn(secondContext);
        when(secondWorkflow.getWorkflowExecutionGraph()).thenReturn(secondGraph);
        when(secondWorkflow.getWorkflowEventBus()).thenReturn(secondBus);
        transactionTemplate.execute(status -> {
            fixture.deliver();
            succeedHandler.handle(secondWorkflow, WorkflowSucceedLifecycleEvent.of(secondWorkflow));
            fixture.assertSharedUnchanged();
            assertThat(secondInstance.getState()).isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
            return null;
        });
        assertThat(workflowDao.queryById(fixture.shared.getId()).getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
        assertThat(workflowDao.queryById(secondInstance.getId()).getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
        assertThat(fixture.shared.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
        assertThat(secondInstance.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
        verify(fixture.bus, times(1)).publish(any(WorkflowFinalizeLifecycleEvent.class));
        verify(secondBus, times(1)).publish(any(WorkflowFinalizeLifecycleEvent.class));
        assertThat(fixture.metric()).isEqualTo(fixture.originalMetric + 2);
    }

    @Test
    void shouldRetryRepeatedFinishAfterEnclosingRollback() {
        final Fixture fixture = new Fixture(WorkflowExecutionStatus.SUCCESS, true);
        transactionTemplate.execute(status -> {
            fixture.deliver();
            fixture.deliver();
            status.setRollbackOnly();
            return null;
        });
        fixture.assertNotFinished();
        fixture.deliver();
        fixture.assertFinished();
    }

    @Test
    void shouldPublishFinalizeEvenWhenCompletionMetricThrows() {
        final Fixture fixture = new Fixture(WorkflowExecutionStatus.SUCCESS, true);
        final IllegalStateException failure = new IllegalStateException("synthetic metric failure");
        try (MockedStatic<WorkflowInstanceMetrics> metrics = mockStatic(WorkflowInstanceMetrics.class)) {
            metrics.when(() -> WorkflowInstanceMetrics.recordWorkflowInstanceFinish(
                    WorkflowExecutionStatus.SUCCESS, fixture.shared.getWorkflowDefinitionCode()))
                    .thenThrow(failure);
            fixture.deliver();
            fixture.deliver();
        }
        assertThat(workflowDao.queryById(fixture.shared.getId()).getState())
                .isEqualTo(WorkflowExecutionStatus.SUCCESS);
        assertThat(fixture.shared.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
        verify(fixture.bus, times(1)).publish(any(WorkflowFinalizeLifecycleEvent.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldKeepCommittedStateWhenFinalizePublicationThrows(final boolean enqueueBeforeThrowing) {
        final Fixture fixture = new Fixture(WorkflowExecutionStatus.SUCCESS, true);
        final WorkflowEventBus bus = spy(new WorkflowEventBus());
        when(fixture.workflow.getWorkflowEventBus()).thenReturn(bus);
        final IllegalStateException failure = new IllegalStateException("synthetic publish failure");
        doAnswer(call -> {
            if (enqueueBeforeThrowing) {
                call.callRealMethod();
            }
            throw failure;
        }).when(bus).publish(any(WorkflowFinalizeLifecycleEvent.class));
        assertThatThrownBy(fixture::deliver).isSameAs(failure);
        assertThat(workflowDao.queryById(fixture.shared.getId()).getState())
                .isEqualTo(WorkflowExecutionStatus.SUCCESS);
        assertThat(fixture.shared.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
        assertThat(serialDao.queryById(fixture.commandId)).isNull();
        // This documents the in-memory publication boundary, not guaranteed delivery after commit.
        fixture.deliver();
        verify(bus, times(1)).publish(any(WorkflowFinalizeLifecycleEvent.class));
        assertThat(bus.poll().isPresent()).isEqualTo(enqueueBeforeThrowing);
        assertThat(bus.isEmpty()).isTrue();
    }

    @Test
    void shouldAllowNextSerialWorkflowWhilePreviousFinalizeIsDelayed() throws Exception {
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(
                "/it/start/workflow_with_serial_wait_strategy.yaml");
        final AtomicInteger delayedWorkflowId = new AtomicInteger();
        final CountDownLatch finalizeEntered = new CountDownLatch(1);
        final CountDownLatch releaseFinalize = new CountDownLatch(1);
        doAnswer(call -> {
            final IWorkflowExecution workflow = call.getArgument(0);
            if (delayedWorkflowId.compareAndSet(0, workflow.getId())) {
                finalizeEntered.countDown();
                if (!releaseFinalize.await(50, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timed out waiting to release finalize");
                }
            }
            return call.callRealMethod();
        }).when(finalizeHandler).handle(any(IWorkflowExecution.class), any(WorkflowFinalizeLifecycleEvent.class));
        final WorkflowOperator.WorkflowTriggerDTO trigger = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(context.getOneWorkflow())
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        try {
            final Integer firstId = workflowOperator.manualTriggerWorkflow(trigger);
            final Integer secondId = workflowOperator.manualTriggerWorkflow(trigger);
            assertThat(finalizeEntered.await(30, TimeUnit.SECONDS)).isTrue();
            assertThat(delayedWorkflowId).hasValue(firstId);
            await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
                assertThat(repository.queryWorkflowInstance(firstId).getState())
                        .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                assertThat(repository.queryWorkflowInstance(secondId).getState())
                        .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                assertThat(workflowRepository.contains(firstId)).isTrue();
                assertThat(workflowRepository.contains(secondId)).isFalse();
                assertThat(serialDao.fetchSerialCommands(10)).isEmpty();
                assertThat(repository.queryTaskInstance(context.getOneWorkflow())).hasSize(2)
                        .extracting(TaskInstance::getState).containsOnly(TaskExecutionStatus.SUCCESS);
            });
        } finally {
            releaseFinalize.countDown();
        }
        masterContainer.assertAllResourceReleased();
    }

    @ParameterizedTest
    @EnumSource(value = WorkflowExecutionStatus.class, names = {"SUCCESS", "FAILURE", "STOP", "PAUSE"})
    void shouldFinishAfterEarlierAfterCommitThrows(final WorkflowExecutionStatus target) {
        final Fixture fixture = new Fixture(target, true);
        final IllegalStateException failure = new IllegalStateException("synthetic earlier callback failure");
        assertThatThrownBy(() -> transactionTemplate.execute(status -> {
            registerAfterCommitFailure(failure);
            fixture.deliver();
            fixture.assertSharedUnchanged();
            return null;
        })).isSameAs(failure);
        fixture.assertFinished();
        fixture.deliver();
        fixture.assertFinished();
        verify(workflowDao, times(1)).updateById(any(WorkflowInstance.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldFinishOtherWorkflowWhenEarlierFinalizeThrows(final boolean enqueueBeforeThrowing) {
        final Fixture fixture = new Fixture(WorkflowExecutionStatus.SUCCESS, false);
        final WorkflowEventBus firstBus = spy(new WorkflowEventBus());
        when(fixture.workflow.getWorkflowEventBus()).thenReturn(firstBus);
        final IllegalStateException failure = new IllegalStateException("synthetic earlier finalize failure");
        doAnswer(call -> {
            if (enqueueBeforeThrowing) {
                call.callRealMethod();
            }
            throw failure;
        }).when(firstBus).publish(any(WorkflowFinalizeLifecycleEvent.class));

        final WorkflowInstance secondInstance = new WorkflowInstance();
        BeanUtils.copyProperties(fixture.shared, secondInstance);
        secondInstance.setId(null);
        workflowDao.insert(secondInstance);
        final IWorkflowExecution secondWorkflow = mock(IWorkflowExecution.class);
        final WorkflowEventBus secondBus = mock(WorkflowEventBus.class);
        final IWorkflowExecuteContext secondContext = fixture.workflow.getWorkflowExecuteContext();
        final IWorkflowExecutionGraph secondGraph = fixture.workflow.getWorkflowExecutionGraph();
        when(secondWorkflow.getWorkflowInstance()).thenReturn(secondInstance);
        when(secondWorkflow.getState()).thenAnswer(call -> secondInstance.getState());
        when(secondWorkflow.getWorkflowExecuteContext()).thenReturn(secondContext);
        when(secondWorkflow.getWorkflowExecutionGraph()).thenReturn(secondGraph);
        when(secondWorkflow.getWorkflowEventBus()).thenReturn(secondBus);

        assertThatThrownBy(() -> transactionTemplate.execute(status -> {
            fixture.deliver();
            succeedHandler.handle(secondWorkflow, WorkflowSucceedLifecycleEvent.of(secondWorkflow));
            return null;
        })).isSameAs(failure);
        for (WorkflowInstance instance : new WorkflowInstance[]{fixture.shared, secondInstance}) {
            final WorkflowInstance persisted = workflowDao.queryById(instance.getId());
            assertThat(persisted.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
            assertThat(instance.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
            assertThat(persisted.getEndTime()).isNotNull().isEqualTo(instance.getEndTime());
        }
        fixture.deliver();
        succeedHandler.handle(secondWorkflow, WorkflowSucceedLifecycleEvent.of(secondWorkflow));
        verify(workflowDao, times(2)).updateById(any(WorkflowInstance.class));
        verify(firstBus, times(1)).publish(any(WorkflowFinalizeLifecycleEvent.class));
        verify(secondBus, times(1)).publish(any(WorkflowFinalizeLifecycleEvent.class));
        assertThat(firstBus.poll().isPresent()).isEqualTo(enqueueBeforeThrowing);
        assertThat(firstBus.isEmpty()).isTrue();
        assertThat(fixture.metric()).isEqualTo(fixture.originalMetric + 2);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldNotRepeatPublicationWhenCompletionFallbackThrows(final boolean enqueueBeforeThrowing) {
        final Fixture fixture = new Fixture(WorkflowExecutionStatus.SUCCESS, true);
        final WorkflowEventBus bus = spy(new WorkflowEventBus());
        when(fixture.workflow.getWorkflowEventBus()).thenReturn(bus);
        final IllegalStateException earlierFailure = new IllegalStateException("synthetic earlier callback failure");
        doAnswer(call -> {
            if (enqueueBeforeThrowing) {
                call.callRealMethod();
            }
            throw new IllegalStateException("synthetic fallback publication failure");
        }).when(bus).publish(any(WorkflowFinalizeLifecycleEvent.class));
        // Spring logs an afterCompletion failure; it does not replace the earlier afterCommit exception.
        assertThatThrownBy(() -> transactionTemplate.execute(status -> {
            registerAfterCommitFailure(earlierFailure);
            fixture.deliver();
            return null;
        })).isSameAs(earlierFailure);
        assertThat(workflowDao.queryById(fixture.shared.getId()).getState())
                .isEqualTo(WorkflowExecutionStatus.SUCCESS);
        assertThat(fixture.shared.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
        assertThat(serialDao.queryById(fixture.commandId)).isNull();
        fixture.deliver();
        verify(workflowDao, times(1)).updateById(any(WorkflowInstance.class));
        verify(bus, times(1)).publish(any(WorkflowFinalizeLifecycleEvent.class));
        assertThat(bus.poll().isPresent()).isEqualTo(enqueueBeforeThrowing);
        assertThat(bus.isEmpty()).isTrue();
        assertThat(fixture.metric()).isEqualTo(fixture.originalMetric + 1);
    }

    private static void registerAfterCommitFailure(final RuntimeException failure) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                throw failure;
            }
        });
    }

    private static void registerCommitFailure() {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void beforeCommit(final boolean readOnly) {
                throw new DataAccessResourceFailureException("synthetic failure before commit");
            }
        });
    }

    private final class Fixture {

        private final WorkflowExecutionStatus target;
        private final WorkflowExecutionStatus originalState;
        private final WorkflowInstance shared;
        private final IWorkflowExecution workflow = mock(IWorkflowExecution.class);
        private final WorkflowEventBus bus = mock(WorkflowEventBus.class);
        private final AtomicInteger updateAttempts = new AtomicInteger();
        private final Integer commandId;
        private final double originalMetric;

        private Fixture(final WorkflowExecutionStatus target, final boolean serial) {
            this(target == WorkflowExecutionStatus.PAUSE ? WorkflowExecutionStatus.READY_PAUSE
                    : WorkflowExecutionStatus.RUNNING_EXECUTION, target, serial);
        }

        private Fixture(final WorkflowExecutionStatus originalState, final WorkflowExecutionStatus target,
                        final boolean serial) {
            this.target = target;
            this.originalState = originalState;
            // These fixtures drive the handlers directly; the separate workflow test exercises the coordinator.
            serialCoordinator.close();
            final WorkflowTestCaseContext seeded = workflowTestCaseContextFactory.initializeContextFromYaml(
                    "/it/failover/running_workflowInstance_with_one_dispatched_fake_task.yaml");
            shared = workflowDao.queryById(1);
            shared.setState(originalState);
            workflowDao.updateById(shared);
            seeded.getOneWorkflow().setExecutionType(serial
                    ? WorkflowExecutionTypeEnum.SERIAL_WAIT
                    : WorkflowExecutionTypeEnum.PARALLEL);
            final IWorkflowExecuteContext context = mock(IWorkflowExecuteContext.class);
            final IWorkflowExecutionGraph graph = mock(IWorkflowExecutionGraph.class);
            when(workflow.getId()).thenReturn(shared.getId());
            when(workflow.getName()).thenReturn("workflow-finish-recovery");
            when(workflow.getWorkflowInstance()).thenReturn(shared);
            when(workflow.getState()).thenAnswer(call -> shared.getState());
            when(workflow.getWorkflowExecuteContext()).thenReturn(context);
            when(context.getWorkflowDefinition()).thenReturn(seeded.getOneWorkflow());
            when(workflow.getWorkflowExecutionGraph()).thenReturn(graph);
            when(graph.isAllTaskExecutionChainSuccess()).thenReturn(true);
            when(graph.isExistFailureTaskExecutionChain()).thenReturn(true);
            when(graph.isExistKilledTaskExecutionChain()).thenReturn(true);
            when(workflow.getWorkflowEventBus()).thenReturn(bus);
            if (serial) {
                final SerialCommand command = new SerialCommand();
                command.setWorkflowInstanceId(shared.getId());
                command.setWorkflowDefinitionCode(shared.getWorkflowDefinitionCode());
                command.setWorkflowDefinitionVersion(shared.getWorkflowDefinitionVersion());
                command.setCommand("{}");
                command.setCreateTime(new Date());
                command.setUpdateTime(new Date());
                serialDao.insert(command);
                commandId = command.getId();
            } else {
                commandId = null;
            }
            originalMetric = metric();
            clearInvocations(workflowDao);
        }

        private void failFirst(final Fault fault) {
            doAnswer(call -> {
                assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
                if (updateAttempts.incrementAndGet() == 1 && fault == Fault.UPDATE) {
                    throw new DataAccessResourceFailureException("synthetic workflow update failure");
                }
                return call.callRealMethod();
            }).when(workflowDao).updateById(any(WorkflowInstance.class));
            doAnswer(call -> {
                if (updateAttempts.get() == 1) {
                    if (fault == Fault.SERIAL_DELETE) {
                        throw new DataAccessResourceFailureException("synthetic serial cleanup failure");
                    }
                    if (fault == Fault.BEFORE_COMMIT) {
                        registerCommitFailure();
                    }
                }
                return call.callRealMethod();
            }).when(serialDao).deleteByWorkflowInstanceId(anyInt());
        }

        private void deliver() {
            switch (target) {
                case SUCCESS:
                    succeedHandler.handle(workflow, WorkflowSucceedLifecycleEvent.of(workflow));
                    break;
                case FAILURE:
                    failedHandler.handle(workflow, WorkflowFailedLifecycleEvent.of(workflow));
                    break;
                case STOP:
                    stoppedHandler.handle(workflow, WorkflowStoppedLifecycleEvent.of(workflow));
                    break;
                case PAUSE:
                    pausedHandler.handle(workflow, WorkflowPausedLifecycleEvent.of(workflow));
                    break;
                default:
                    throw new IllegalArgumentException(target.name());
            }
        }

        private void assertSharedUnchanged() {
            assertThat(shared.getState()).isEqualTo(originalState);
            assertThat(shared.getEndTime()).isNull();
            verify(bus, never()).publish(any(WorkflowFinalizeLifecycleEvent.class));
            assertThat(metric()).isEqualTo(originalMetric);
        }

        private void assertNotFinished() {
            final WorkflowInstance persisted = workflowDao.queryById(shared.getId());
            assertThat(persisted.getState()).isEqualTo(originalState);
            assertThat(persisted.getEndTime()).isNull();
            if (commandId != null) {
                assertThat(serialDao.queryById(commandId)).isNotNull();
            }
            assertSharedUnchanged();
        }

        private void assertFinished() {
            final WorkflowInstance persisted = workflowDao.queryById(shared.getId());
            assertThat(persisted.getState()).isEqualTo(target);
            assertThat(persisted.getEndTime()).isNotNull().isEqualTo(shared.getEndTime());
            assertThat(shared.getState()).isEqualTo(target);
            assertThat(workflow.getWorkflowInstance()).isSameAs(shared);
            if (commandId != null) {
                assertThat(serialDao.queryById(commandId)).isNull();
            }
            verify(bus, times(1)).publish(any(WorkflowFinalizeLifecycleEvent.class));
            assertThat(metric()).isEqualTo(originalMetric + 1);
        }

        private double metric() {
            final Counter counter = Metrics.globalRegistry.find("ds.workflow.instance.count")
                    .tags("state", target.name(), "workflow.definition.code",
                            String.valueOf(shared.getWorkflowDefinitionCode()))
                    .counter();
            return counter == null ? 0 : counter.count();
        }
    }
}
