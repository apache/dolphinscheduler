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

import static com.google.common.truth.Truth.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import java.time.Duration;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The integration test for validating sub workflow instances inherit global params from all parent flows.
 * Global params are asserted in each sub workflow instance and the fake_task in the sub-sub-workflow is used to verify the global params.
 */
class SubWorkflowInstanceGlobalParamsInheritanceTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test subflows inherit global params from all parent flows")
    void testSubflowInheritsGlobalParamsFromParentFlows_with_oneSuccessTask() {
        final String yaml = "/it/start/workflow_with_sub_workflows_with_global_params.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition parentWorkflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(parentWorkflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        final Integer workflowInstanceId = workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflowInstanceId))
                            .matches(workflowInstance -> workflowInstance.getState() == WorkflowExecutionStatus.SUCCESS)
                            .matches(workflowInstance -> workflowInstance.getIsSubWorkflow() == Flag.NO);

                    final List<WorkflowInstance> subWorkflowInstance =
                            repository.queryWorkflowInstance(context.getWorkflows().get(1));
                    Assertions
                            .assertThat(subWorkflowInstance)
                            .hasSize(1)
                            .satisfiesExactly(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                                assertThat(workflowInstance.getIsSubWorkflow()).isEqualTo(Flag.YES);

                                Assertions
                                        .assertThat(
                                                JSONUtils.toList(workflowInstance.getGlobalParams(), Property.class))
                                        .hasSize(1)
                                        .anySatisfy(property -> {
                                            assertThat(property.getProp()).isEqualTo("parentWorkflowParam");
                                            assertThat(property.getValue()).isEqualTo("parentWorkflowParamValue");
                                        });
                            });

                    final List<WorkflowInstance> subSubWorkflowInstance =
                            repository.queryWorkflowInstance(context.getWorkflows().get(2));
                    Assertions
                            .assertThat(subSubWorkflowInstance)
                            .hasSize(1)
                            .satisfiesExactly(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                                assertThat(workflowInstance.getIsSubWorkflow()).isEqualTo(Flag.YES);

                                Assertions
                                        .assertThat(
                                                JSONUtils.toList(workflowInstance.getGlobalParams(), Property.class))
                                        .hasSize(2)
                                        .anySatisfy(property -> {
                                            assertThat(property.getProp()).isEqualTo("parentWorkflowParam");
                                            assertThat(property.getValue()).isEqualTo("parentWorkflowParamValue");
                                        })
                                        .anySatisfy(property -> {
                                            assertThat(property.getProp()).isEqualTo("subWorkflowParam");
                                            assertThat(property.getValue()).isEqualTo("subWorkflowParamValue");
                                        });
                            });

                    Assertions
                            .assertThat(repository.queryTaskInstance(subSubWorkflowInstance.get(0).getId()))
                            .satisfiesExactly(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("fake_task");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });
                });

        masterContainer.assertAllResourceReleased();
    }

}
