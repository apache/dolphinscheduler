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

import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.command.BackfillWorkflowCommandParam;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import org.apache.commons.lang3.time.DateUtils;

import java.time.Duration;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The integration test for scheduling a workflow from workflow definition.
 */
public class WorkflowBackfillTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test backfill a workflow in asc order success")
    public void testSerialBackfillWorkflow_with_oneSuccessTask() {
        final String yaml = "/it/backfill/workflow_with_one_fake_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getWorkflows().get(0);

        final BackfillWorkflowCommandParam backfillWorkflowCommandParam = BackfillWorkflowCommandParam.builder()
                .backfillTimeList(
                        Lists.newArrayList(
                                "2024-08-11 00:00:00",
                                "2024-08-12 00:00:00",
                                "2024-08-13 00:00:00",
                                "2024-08-14 00:00:00"))
                .build();
        WorkflowOperator.WorkflowBackfillDTO workflowBackfillDTO = WorkflowOperator.WorkflowBackfillDTO.builder()
                .workflow(workflow)
                .backfillWorkflowCommandParam(backfillWorkflowCommandParam)
                .build();
        workflowOperator.backfillWorkflow(workflowBackfillDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    final List<WorkflowInstance> workflowInstances = repository.queryWorkflowInstance(workflow);
                    assertThat(workflowInstances).hasSize(4);
                    assertThat(workflowInstances.get(0).getScheduleTime())
                            .isEqualTo(DateUtils.parseDate("2024-08-11 00:00:00", "yyyy-MM-dd HH:mm:ss"));
                    assertThat(workflowInstances.get(1).getScheduleTime())
                            .isEqualTo(DateUtils.parseDate("2024-08-12 00:00:00", "yyyy-MM-dd HH:mm:ss"));
                    assertThat(workflowInstances.get(2).getScheduleTime())
                            .isEqualTo(DateUtils.parseDate("2024-08-13 00:00:00", "yyyy-MM-dd HH:mm:ss"));
                    assertThat(workflowInstances.get(3).getScheduleTime())
                            .isEqualTo(DateUtils.parseDate("2024-08-14 00:00:00", "yyyy-MM-dd HH:mm:ss"));
                });
        masterContainer.assertAllResourceReleased();

    }

}
