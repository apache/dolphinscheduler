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

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.extract.master.command.RunWorkflowCommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils;
import org.apache.dolphinscheduler.server.master.AbstractMasterIntegrationTestCase;
import org.apache.dolphinscheduler.server.master.integration.WorkflowOperator;
import org.apache.dolphinscheduler.server.master.integration.WorkflowTestCaseContext;

import java.time.Duration;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

/**
 * Integration tests for workflow start parameter scenarios.
 */
public class WorkflowStartParameterTestCase extends AbstractMasterIntegrationTestCase {

    @Test
    @DisplayName("Test start a workflow which using workflow params")
    public void testStartWorkflow_usingWorkflowParam() {
        final String yaml = "/it/start/workflow_with_global_param.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.FAILURE));
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(2)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            });
                });

        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow which using command params")
    public void testStartWorkflow_usingCommandParam() {
        final String yaml = "/it/start/workflow_with_global_param.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final RunWorkflowCommandParam runWorkflowCommandParam = RunWorkflowCommandParam.builder()
                .commandParams(Lists.newArrayList(Property.builder()
                        .prop("name")
                        .direct(Direct.IN)
                        .type(DataType.VARCHAR)
                        .value("commandParam")
                        .build()))
                .build();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(runWorkflowCommandParam)
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.FAILURE));
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(2)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.FAILURE);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow contains fake task using local param will be overwrite by varpool")
    public void testStartWorkflow_fakeTask_usingLocalParamOverWriteByVarPool() {
        final String yaml = "/it/start/workflow_with_local_param_overwrite_by_varpool.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final RunWorkflowCommandParam runWorkflowCommandParam = RunWorkflowCommandParam.builder()
                .build();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(runWorkflowCommandParam)
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        List<Property> assertVarPools = Lists.newArrayList(
                Property.builder().prop("output").direct(Direct.OUT).type(DataType.VARCHAR).value("1").build());
        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> {
                                assertThat(workflowInstance.getState()).isEqualTo(WorkflowExecutionStatus.SUCCESS);
                                assertThat(VarPoolUtils.deserializeVarPool(workflowInstance.getVarPool()))
                                        .isEqualTo(assertVarPools);
                            });
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(3)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(VarPoolUtils.deserializeVarPool(taskInstance.getVarPool()))
                                        .isEqualTo(assertVarPools);
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B");
                                assertThat(VarPoolUtils.deserializeVarPool(taskInstance.getVarPool()))
                                        .isEqualTo(assertVarPools);
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("C");
                                assertThat(VarPoolUtils.deserializeVarPool(taskInstance.getVarPool()))
                                        .isEqualTo(assertVarPools);
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow which using null key params")
    public void testStartWorkflow_usingNullKeyParam() {
        final String yaml = "/it/start/workflow_with_null_key_param.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final RunWorkflowCommandParam runWorkflowCommandParam = RunWorkflowCommandParam.builder()
                .commandParams(Lists.newArrayList(Property.builder()
                        .prop(null)
                        .direct(Direct.IN)
                        .type(DataType.VARCHAR)
                        .value("commandParam")
                        .build()))
                .build();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(runWorkflowCommandParam)
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.SUCCESS));
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(2)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

    @Test
    @DisplayName("Test start a workflow which using workflow built in params")
    public void testStartWorkflow_usingWorkflowBuiltInParam() {
        final String yaml = "/it/start/workflow_with_built_in_param.yaml";
        final WorkflowTestCaseContext context = workflowTestCaseContextFactory.initializeContextFromYaml(yaml);
        final WorkflowDefinition workflow = context.getOneWorkflow();

        final WorkflowOperator.WorkflowTriggerDTO workflowTriggerDTO = WorkflowOperator.WorkflowTriggerDTO.builder()
                .workflowDefinition(workflow)
                .runWorkflowCommandParam(new RunWorkflowCommandParam())
                .build();
        workflowOperator.manualTriggerWorkflow(workflowTriggerDTO);

        await()
                .atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    Assertions
                            .assertThat(repository.queryWorkflowInstance(workflow))
                            .satisfiesExactly(workflowInstance -> assertThat(workflowInstance.getState())
                                    .isEqualTo(WorkflowExecutionStatus.SUCCESS));
                    Assertions
                            .assertThat(repository.queryTaskInstance(workflow))
                            .hasSize(2)
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("A");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            })
                            .anySatisfy(taskInstance -> {
                                assertThat(taskInstance.getName()).isEqualTo("B");
                                assertThat(taskInstance.getState()).isEqualTo(TaskExecutionStatus.SUCCESS);
                            });
                });
        masterContainer.assertAllResourceReleased();
    }

}
