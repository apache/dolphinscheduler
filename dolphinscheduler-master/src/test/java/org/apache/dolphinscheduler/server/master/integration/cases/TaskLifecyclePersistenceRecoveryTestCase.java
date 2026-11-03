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
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DataAccessResourceFailureException;

public class TaskLifecyclePersistenceRecoveryTestCase extends AbstractMasterIntegrationTestCase {

    @SpyBean
    private TaskInstanceDao taskInstanceDao;

    @ParameterizedTest
    @EnumSource(value = TaskExecutionStatus.class, names = {"DISPATCH", "RUNNING_EXECUTION"})
    void shouldCompleteWorkflowAfterNonTerminalUpdateRecovers(final TaskExecutionStatus state) {
        final FailureProbe probe = failFirstUpdate(state);
        final Integer workflowId = start("/it/start/workflow_with_two_serial_fake_task_success.yaml");
        awaitWorkflow(workflowId, WorkflowExecutionStatus.SUCCESS, TaskExecutionStatus.SUCCESS, 2);
        assertThat(probe.attempts).hasValue(2);
        assertThat(probe.beforeFailure.get().getState()).isNotEqualTo(state);
    }

    @Test
    void shouldFinishStoppingWorkflowAfterKillUpdateRecovers() {
        final FailureProbe probe = failFirstUpdate(TaskExecutionStatus.KILL);
        final Integer workflowId = start("/it/stop/workflow_with_one_fake_task_failed.yaml");
        await().pollInterval(Duration.ofMillis(100)).atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertThat(repository.queryTaskInstance(workflowId)).satisfiesExactly(
                        task -> assertThat(task.getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION)));
        assertThat(workflowOperator.stopWorkflowInstance(workflowId).isSuccess()).isTrue();
        awaitWorkflow(workflowId, WorkflowExecutionStatus.STOP, TaskExecutionStatus.KILL, 1);
        assertThat(probe.attempts).hasValue(2);
        assertThat(probe.beforeFailure.get().getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
    }

    @Test
    void shouldFinishFailedWorkflowAfterFailureUpdateRecovers() {
        final FailureProbe probe = failFirstUpdate(TaskExecutionStatus.FAILURE);
        final Integer workflowId = start("/it/start/workflow_with_one_fake_task_failed.yaml");
        awaitWorkflow(workflowId, WorkflowExecutionStatus.FAILURE, TaskExecutionStatus.FAILURE, 1);
        assertThat(probe.attempts).hasValue(2);
        assertThat(probe.beforeFailure.get().getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
    }

    @Test
    void shouldFinishTasksSharingAGroupAfterSuccessUpdateRecovers() {
        final FailureProbe probe = failFirstUpdate(TaskExecutionStatus.SUCCESS);
        final Integer workflowId = start("/it/start/workflow_with_fake_tasks_using_task_group.yaml");
        // The task group coordinator waits one minute before its first scheduling round.
        awaitWorkflow(workflowId, WorkflowExecutionStatus.SUCCESS, TaskExecutionStatus.SUCCESS, 2,
                Duration.ofMinutes(2));
        assertThat(probe.attempts).hasValue(2);
        assertThat(probe.beforeFailure.get().getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
    }

    private Integer start(final String resource) {
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(resource);
        return workflowOperator.manualTriggerWorkflow(WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(context.getOneWorkflow())
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build());
    }

    private void awaitWorkflow(final Integer workflowId, final WorkflowExecutionStatus workflowState,
                               final TaskExecutionStatus taskState, final int taskCount) {
        awaitWorkflow(workflowId, workflowState, taskState, taskCount, Duration.ofSeconds(45));
    }

    private void awaitWorkflow(final Integer workflowId, final WorkflowExecutionStatus workflowState,
                               final TaskExecutionStatus taskState, final int taskCount, final Duration timeout) {
        await().atMost(timeout).untilAsserted(() -> {
            assertThat(repository.queryWorkflowInstance(workflowId).getState()).isEqualTo(workflowState);
            assertThat(repository.queryTaskInstance(workflowId)).hasSize(taskCount)
                    .allSatisfy(task -> assertThat(task.getState()).isEqualTo(taskState));
            masterContainer.assertAllResourceReleased();
        });
    }

    private FailureProbe failFirstUpdate(final TaskExecutionStatus state) {
        final FailureProbe probe = new FailureProbe();
        doAnswer(invocation -> {
            final TaskInstance candidate = invocation.getArgument(0);
            if ("A".equals(candidate.getName()) && candidate.getState() == state
                    && probe.attempts.incrementAndGet() == 1) {
                probe.beforeFailure.set(taskInstanceDao.queryById(candidate.getId()));
                throw new DataAccessResourceFailureException("synthetic first lifecycle update failure");
            }
            return invocation.callRealMethod();
        }).when(taskInstanceDao).updateById(any(TaskInstance.class));
        return probe;
    }

    private static final class FailureProbe {

        private final AtomicInteger attempts = new AtomicInteger();
        private final AtomicReference<TaskInstance> beforeFailure = new AtomicReference<>();
    }
}
