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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.subworkflow;

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SubWorkflowLogicTaskMergeParamsTest {

    @Test
    @DisplayName("Test mergeParams: upstream VarPool (OUT) overrides global parameter (IN)")
    void testMergeParams_upstreamVarPoolOverridesGlobalParam() {
        // Simulates: globalParams=[param1=global_val], varPool=[param1=upstream_val]
        // The varPool (last in merge order) should win
        final Property globalParam = Property.builder()
                .prop("param1")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("global_val")
                .build();
        final Property upstreamOutParam = Property.builder()
                .prop("param1")
                .direct(Direct.OUT)
                .type(DataType.VARCHAR)
                .value("upstream_val")
                .build();

        final List<Property> result = SubWorkflowLogicTask.mergeParams(Arrays.asList(
                Collections.singletonList(globalParam),
                Collections.emptyList(),
                Collections.singletonList(upstreamOutParam)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProp()).isEqualTo("param1");
        assertThat(result.get(0).getValue()).isEqualTo("upstream_val");
    }

    @Test
    @DisplayName("Test mergeParams: command (start) parameter overrides global parameter")
    void testMergeParams_commandParamOverridesGlobalParam() {
        // Simulates: globalParams=[param1=global_val], commandParams=[param1=start_val]
        final Property globalParam = Property.builder()
                .prop("param1")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("global_val")
                .build();
        final Property commandParam = Property.builder()
                .prop("param1")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("start_val")
                .build();

        final List<Property> result = SubWorkflowLogicTask.mergeParams(Arrays.asList(
                Collections.singletonList(globalParam),
                Collections.singletonList(commandParam),
                Collections.emptyList()));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProp()).isEqualTo("param1");
        assertThat(result.get(0).getValue()).isEqualTo("start_val");
    }

    @Test
    @DisplayName("Test mergeParams: upstream VarPool overrides command (start) parameter")
    void testMergeParams_varPoolOverridesCommandParam() {
        // Simulates: commandParams=[param1=start_val], varPool=[param1=upstream_val]
        // The varPool (last in merge order) should win
        final Property commandParam = Property.builder()
                .prop("param1")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("start_val")
                .build();
        final Property upstreamOutParam = Property.builder()
                .prop("param1")
                .direct(Direct.OUT)
                .type(DataType.VARCHAR)
                .value("upstream_val")
                .build();

        final List<Property> result = SubWorkflowLogicTask.mergeParams(Arrays.asList(
                Collections.emptyList(),
                Collections.singletonList(commandParam),
                Collections.singletonList(upstreamOutParam)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProp()).isEqualTo("param1");
        assertThat(result.get(0).getValue()).isEqualTo("upstream_val");
    }

    @Test
    @DisplayName("Test mergeParams: full precedence — global < command < upstream VarPool")
    void testMergeParams_fullPrecedence() {
        // Simulates a conflict where all three sources provide the same key
        // globalParams=[param1=global_val], commandParams=[param1=start_val], varPool=[param1=upstream_val]
        // The varPool (last in merge order) should win
        final Property globalParam = Property.builder()
                .prop("param1")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("global_val")
                .build();
        final Property commandParam = Property.builder()
                .prop("param1")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("start_val")
                .build();
        final Property upstreamOutParam = Property.builder()
                .prop("param1")
                .direct(Direct.OUT)
                .type(DataType.VARCHAR)
                .value("upstream_val")
                .build();

        final List<Property> result = SubWorkflowLogicTask.mergeParams(Arrays.asList(
                Collections.singletonList(globalParam),
                Collections.singletonList(commandParam),
                Collections.singletonList(upstreamOutParam)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProp()).isEqualTo("param1");
        assertThat(result.get(0).getValue()).isEqualTo("upstream_val");
    }

    @Test
    @DisplayName("Test mergeParams: non-conflicting parameters from all sources are preserved")
    void testMergeParams_nonConflictingParamsAllPreserved() {
        // globalParams=[global_only=global], commandParams=[start_only=start], varPool=[upstream_only=upstream]
        final Property globalParam = Property.builder()
                .prop("global_only")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("global_val")
                .build();
        final Property commandParam = Property.builder()
                .prop("start_only")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("start_val")
                .build();
        final Property upstreamOutParam = Property.builder()
                .prop("upstream_only")
                .direct(Direct.OUT)
                .type(DataType.VARCHAR)
                .value("upstream_val")
                .build();

        final List<Property> result = SubWorkflowLogicTask.mergeParams(Arrays.asList(
                Collections.singletonList(globalParam),
                Collections.singletonList(commandParam),
                Collections.singletonList(upstreamOutParam)));

        assertThat(result).hasSize(3);
        // Verify each parameter is present with its expected value
        assertThat(
                result.stream().anyMatch(p -> "global_only".equals(p.getProp()) && "global_val".equals(p.getValue())))
                        .isTrue();
        assertThat(result.stream().anyMatch(p -> "start_only".equals(p.getProp()) && "start_val".equals(p.getValue())))
                .isTrue();
        assertThat(
                result.stream()
                        .anyMatch(p -> "upstream_only".equals(p.getProp()) && "upstream_val".equals(p.getValue())))
                                .isTrue();
    }
}
