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

package org.apache.dolphinscheduler.server.master.it.cases;

import static com.google.common.truth.Truth.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTest;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.it.Repository;
import org.apache.dolphinscheduler.server.master.it.WorkflowITContext;
import org.apache.dolphinscheduler.server.master.it.WorkflowITContextFactory;
import org.apache.dolphinscheduler.server.master.it.WorkflowOperator;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The integration test for recover from failure tasks.
 */
public class WorkflowInstanceRecoverFailureTaskIT extends AbstractMasterIntegrationTest {

    @Autowired
    private WorkflowITContextFactory workflowITContextFactory;

    @Autowired
    private WorkflowOperator workflowOperator;

    @Autowired
    private IWorkflowRepository workflowRepository;

    @Autowired
    private Repository repository;

    @Test
    @DisplayName("Test recover from failure tasks")
    public void testRepeatRunningWorkflow_with_taskOnly() {
        final String yaml = "/it/recover_failure_tasks/failure_workflow_with_two_serial_fake_task.yaml";
        final WorkflowITContext context = workflowITContextFactory.initializeContextFromYaml(yaml);

        final Integer workflowInstanceId = context.getWorkflowInstance().getId();
        workflowOperator.recoverFailureTasks(workflowInstanceId);

        await()
                .pollInterval(Duration.ofMillis(100))
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId).getState())
                            .isEqualTo(WorkflowExecutionStatus.RUNNING_EXECUTION);
                });

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    final WorkflowInstance workflowInstance = repository.queryWorkflowInstance(workflowInstanceId);
                    Assertions
                            .assertThat(workflowInstance.getState())
                            .isEqualTo(WorkflowExecutionStatus.SUCCESS);
                    Assertions
                            .assertThat(workflowInstance.getRunTimes())
                            .isEqualTo(2);
                    final List<TaskInstance> taskInstances = repository.queryTaskInstance(workflowInstanceId);
                    Assertions
                            .assertThat(taskInstances)
                            .hasSize(3);

                    Assertions
                            .assertThat(taskInstances.get(0))
                            .matches(t -> "A".equals(t.getName()))
                            .matches(t -> t.getState() == TaskExecutionStatus.FAILURE)
                            .matches(t -> t.getFlag() == Flag.NO);

                    Assertions
                            .assertThat(taskInstances.get(1))
                            .matches(t -> "A".equals(t.getName()))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS)
                            .matches(t -> t.getFlag() == Flag.YES)
                            .matches(t -> StringUtils.isNotEmpty(t.getLogPath()));

                    Assertions
                            .assertThat(taskInstances.get(2))
                            .matches(t -> t.getState() == TaskExecutionStatus.SUCCESS)
                            .matches(t -> t.getFlag() == Flag.YES)
                            .matches(t -> StringUtils.isNotEmpty(t.getLogPath()));
                });
        assertThat(workflowRepository.getAll()).isEmpty();
    }

}
