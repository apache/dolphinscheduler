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

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.engine.workflow.serial.WorkflowSerialCoordinator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import java.time.Duration;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

public class WorkflowSerialWaitRecoverParamsTestCase extends AbstractMasterIntegrationTestCase {

    @Autowired
    private WorkflowSerialCoordinator serialCoordinator;
    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;
    @Autowired
    private SerialCommandDao serialCommandDao;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void testRecoverNeverStartedSerialInstancePreservesParams() throws InterruptedException {
        final Thread coordinatorThread = (Thread) ReflectionTestUtils.getField(serialCoordinator, "internalThread");
        serialCoordinator.close();
        coordinatorThread.join(10000);
        assertThat(coordinatorThread.isAlive()).isFalse();

        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(
                "/it/start/workflow_with_serial_wait_params.yaml");
        final Integer instanceId = workflowOperator.manualTriggerWorkflow(WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(context.getOneWorkflow())
                .runWorkflowCommandParam(RunWorkflowCommandParam.builder()
                        .startNodes(Collections.singletonList(1L))
                        .commandParams(
                                JSONUtils.toList("[{\"prop\":\"param\",\"value\":\"original\"}]", Property.class))
                        .build())
                .build());
        final WorkflowInstance instance = repository.queryWorkflowInstance(instanceId);
        assertThat(instance.getState()).isEqualTo(WorkflowExecutionStatus.SERIAL_WAIT);
        assertThat(instance.getCommandParam()).isNotBlank();
        assertThat(JSONUtils.toList(instance.getGlobalParams(), Property.class))
                .extracting(Property::getValue).containsExactlyInAnyOrder("original", "kept");
        assertThat(repository.queryTaskInstance(instanceId)).isEmpty();

        for (int i = 0; i < 2; i++) {
            // Apply the same DB-only pause transaction as PauseWorkflowInstanceExecutorDelegate in the API module.
            transactionTemplate.execute(status -> {
                workflowInstanceDao.updateWorkflowInstanceState(instanceId,
                        WorkflowExecutionStatus.SERIAL_WAIT, WorkflowExecutionStatus.PAUSE);
                assertThat(serialCommandDao.deleteByWorkflowInstanceId(instanceId)).isEqualTo(1);
                return null;
            });
            assertThat(repository.queryWorkflowInstance(instanceId).getState())
                    .isEqualTo(WorkflowExecutionStatus.PAUSE);
            assertThat(workflowOperator.recoverSuspendWorkflowInstance(instanceId).isSuccess()).isTrue();
            assertThat(repository.queryWorkflowInstance(instanceId).getCommandParam())
                    .isEqualTo(instance.getCommandParam());
            assertThat(repository.queryWorkflowInstance(instanceId).getGlobalParams())
                    .isEqualTo(instance.getGlobalParams());
        }

        serialCoordinator.start();
        await().atMost(Duration.ofMinutes(1)).untilAsserted(() -> {
            final WorkflowInstance recovered = repository.queryWorkflowInstance(instanceId);
            assertThat(recovered.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
            assertThat(recovered.getCommandParam()).isEqualTo(instance.getCommandParam());
            assertThat(recovered.getGlobalParams()).isEqualTo(instance.getGlobalParams());
            assertThat(repository.queryTaskInstance(instanceId)).singleElement()
                    .satisfies(task -> assertThat(task.getState()).isEqualTo(TaskExecutionStatus.SUCCESS));
        });
        masterContainer.assertAllResourceReleased();
    }
}
