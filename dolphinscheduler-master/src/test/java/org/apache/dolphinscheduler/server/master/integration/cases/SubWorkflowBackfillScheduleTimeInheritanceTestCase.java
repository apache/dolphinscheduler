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

class SubWorkflowBackfillScheduleTimeInheritanceTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test sub workflow inherits parent backfill schedule time")
    void testSubWorkflowInheritsParentBackfillScheduleTime() throws Exception {
        final String yaml = "/it/start/workflow_with_sub_workflow_task_success.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition parentWorkflow = context.getWorkflow("parent_workflow");
        final WorkflowDefinition childWorkflow = context.getWorkflow("child_workflow");
        final String backfillTime = "2026-04-08 00:00:00";

        final BackfillWorkflowCommandParam backfillWorkflowCommandParam = BackfillWorkflowCommandParam.builder()
                .backfillTimeList(Lists.newArrayList(backfillTime))
                .build();
        final WorkflowOperator.WorkflowBackfillDTO workflowBackfillDTO = WorkflowOperator.WorkflowBackfillDTO.builder()
                .workflow(parentWorkflow)
                .backfillWorkflowCommandParam(backfillWorkflowCommandParam)
                .build();

        workflowOperator.backfillWorkflow(workflowBackfillDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    final List<WorkflowInstance> parentWorkflowInstances =
                            repository.queryWorkflowInstance(parentWorkflow);
                    final List<WorkflowInstance> childWorkflowInstances =
                            repository.queryWorkflowInstance(childWorkflow);

                    assertThat(parentWorkflowInstances)
                            .singleElement()
                            .satisfies(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                                assertThat(workflowInstance.getScheduleTime())
                                        .isEqualTo(DateUtils.parseDate(backfillTime, "yyyy-MM-dd HH:mm:ss"));
                            });

                    assertThat(childWorkflowInstances)
                            .singleElement()
                            .satisfies(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                                assertThat(workflowInstance.getScheduleTime())
                                        .isEqualTo(DateUtils.parseDate(backfillTime, "yyyy-MM-dd HH:mm:ss"));
                            });
                });

        masterContainer.assertAllResourceReleased();
    }
}
