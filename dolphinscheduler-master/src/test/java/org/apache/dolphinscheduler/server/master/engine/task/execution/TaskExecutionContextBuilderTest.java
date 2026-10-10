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

package org.apache.dolphinscheduler.server.master.engine.task.execution;

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TaskExecutionContextBuilderTest {

    private static final String WORKFLOW_INSTANCE_HOST = "127.0.0.1:5678";

    @Test
    @DisplayName("Test that VarPool from TaskInstance is propagated to TaskExecutionContext")
    void testBuildTaskInstanceRelatedInfo_copiesVarPool() {
        // Given: a TaskInstance with a predecessor-scoped VarPool containing
        // an OUT parameter from an upstream task
        final Property upstreamOutParam = Property.builder()
                .prop("output1")
                .direct(Direct.OUT)
                .type(DataType.VARCHAR)
                .value("upstream_value")
                .build();
        final List<Property> predecessorScopedVarPool = Collections.singletonList(upstreamOutParam);

        final TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setName("sub_workflow_task");
        taskInstance.setVarPool(VarPoolUtils.serializeVarPool(predecessorScopedVarPool));

        // When: building the TaskExecutionContext
        final TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildWorkflowInstanceHost(WORKFLOW_INSTANCE_HOST)
                .create();

        // Then: the VarPool is correctly propagated from TaskInstance to TaskExecutionContext
        assertThat(taskExecutionContext.getVarPool()).isNotNull();
        assertThat(taskExecutionContext.getVarPool()).hasSize(1);
        final Property varPoolEntry = taskExecutionContext.getVarPool().get(0);
        assertThat(varPoolEntry.getProp()).isEqualTo("output1");
        assertThat(varPoolEntry.getDirect()).isEqualTo(Direct.OUT);
        assertThat(varPoolEntry.getValue()).isEqualTo("upstream_value");
    }

    @Test
    @DisplayName("Test that null VarPool in TaskInstance results in empty list in TaskExecutionContext")
    void testBuildTaskInstanceRelatedInfo_nullVarPool() {
        // Given: a TaskInstance with null VarPool
        final TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setName("sub_workflow_task");
        taskInstance.setVarPool(null);

        // When: building the TaskExecutionContext
        final TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildWorkflowInstanceHost(WORKFLOW_INSTANCE_HOST)
                .create();

        // Then: the VarPool is an empty list (not null)
        assertThat(taskExecutionContext.getVarPool()).isNotNull();
        assertThat(taskExecutionContext.getVarPool()).isEmpty();
    }

    @Test
    @DisplayName("Test that multiple OUT parameters from predecessor are all propagated")
    void testBuildTaskInstanceRelatedInfo_multipleVarPoolEntries() {
        // Given: a TaskInstance with multiple predecessor-scoped VarPool entries
        final Property outParam1 = Property.builder()
                .prop("output1")
                .direct(Direct.OUT)
                .type(DataType.VARCHAR)
                .value("value1")
                .build();
        final Property outParam2 = Property.builder()
                .prop("output2")
                .direct(Direct.OUT)
                .type(DataType.INTEGER)
                .value("42")
                .build();
        final List<Property> predecessorScopedVarPool = Arrays.asList(outParam1, outParam2);

        final TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setName("sub_workflow_task");
        taskInstance.setVarPool(VarPoolUtils.serializeVarPool(predecessorScopedVarPool));

        // When: building the TaskExecutionContext
        final TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildWorkflowInstanceHost(WORKFLOW_INSTANCE_HOST)
                .create();

        // Then: all VarPool entries are propagated
        assertThat(taskExecutionContext.getVarPool()).hasSize(2);
        assertThat(taskExecutionContext.getVarPool().get(0).getProp()).isEqualTo("output1");
        assertThat(taskExecutionContext.getVarPool().get(1).getProp()).isEqualTo("output2");
    }
}
