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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DataAccessResourceFailureException;

public class TaskSuccessPersistenceRecoveryTestCase extends AbstractMasterIntegrationTestCase {

    @SpyBean
    private TaskInstanceDao taskInstanceDao;

    @Test
    public void testSuccessPersistenceFailureRetriesAndCompletesWorkflow() {
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(
                "/it/start/workflow_with_two_serial_fake_task_success.yaml");
        final AtomicInteger successUpdateAttempts = new AtomicInteger();
        final AtomicReference<TaskInstance> persistedTaskBeforeFailure = new AtomicReference<>();

        // Fail only A's first success update. All other calls use the real DAO and the isolated H2 database.
        doAnswer(invocation -> {
            final TaskInstance taskInstance = invocation.getArgument(0);
            if ("A".equals(taskInstance.getName()) && taskInstance.getState() == TaskExecutionStatus.SUCCESS
                    && successUpdateAttempts.incrementAndGet() == 1) {
                persistedTaskBeforeFailure.set(taskInstanceDao.queryById(taskInstance.getId()));
                throw new DataAccessResourceFailureException("synthetic first success update failure");
            }
            return invocation.callRealMethod();
        }).when(taskInstanceDao).updateById(any(TaskInstance.class));

        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(
                WorkflowOperator.WorkflowTriggerDTO.builder()
                        .workflowDefinition(context.getOneWorkflow())
                        .runWorkflowCommandParam(new RunWorkflowCommandParam())
                        .build());

        await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                            .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                    assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .hasSize(2)
                            .extracting(TaskInstance::getName)
                            .containsExactly("A", "B");
                    assertThat(repository.queryTaskInstance(workflowInstanceId))
                            .allSatisfy(taskInstance -> {
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                                assertThat(taskInstance.getEndTime()).isNotNull();
                            });
                });

        assertThat(successUpdateAttempts).hasValue(2);
        assertThat(persistedTaskBeforeFailure.get()).isNotNull();
        assertThat(persistedTaskBeforeFailure.get().getState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        assertThat(persistedTaskBeforeFailure.get().getEndTime()).isNull();
        masterContainer.assertAllResourceReleased();
    }
}
