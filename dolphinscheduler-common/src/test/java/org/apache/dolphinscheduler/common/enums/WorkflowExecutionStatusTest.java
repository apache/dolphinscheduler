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

package org.apache.dolphinscheduler.common.enums;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

class WorkflowExecutionStatusTest {

    @Test
    void testIsSuccess() {
        assertThat(WorkflowExecutionStatus.SUCCESS.isSuccess()).isTrue();
    }

    @Test
    void testIsFailure() {
        assertThat(WorkflowExecutionStatus.FAILURE.isFailure()).isTrue();
    }

    @Test
    void testIsPaused() {
        assertThat(WorkflowExecutionStatus.PAUSE.isPaused()).isTrue();
    }

    @Test
    void testIsStopped() {
        assertThat(WorkflowExecutionStatus.STOP.isStopped()).isTrue();
    }

    @Test
    void testNonTerminalStates() {
        assertThat(WorkflowExecutionStatus.NOT_TERMINAL_STATES).asList().containsExactly(
                WorkflowExecutionStatus.SUBMITTED_SUCCESS.getCode(),
                WorkflowExecutionStatus.RUNNING_EXECUTION.getCode(),
                WorkflowExecutionStatus.READY_PAUSE.getCode(),
                WorkflowExecutionStatus.READY_STOP.getCode(),
                WorkflowExecutionStatus.SERIAL_WAIT.getCode(),
                WorkflowExecutionStatus.FAILOVER.getCode());
    }

    @Test
    void testNeedFailoverStates() {
        assertThat(WorkflowExecutionStatus.NEED_FAILOVER_STATES).asList().containsExactly(
                WorkflowExecutionStatus.RUNNING_EXECUTION.getCode(),
                WorkflowExecutionStatus.READY_PAUSE.getCode(),
                WorkflowExecutionStatus.READY_STOP.getCode());
    }

    @Test
    void testCanStop() {
        assertThat(WorkflowExecutionStatus.SUBMITTED_SUCCESS.isCanStop()).isFalse();
        assertThat(WorkflowExecutionStatus.RUNNING_EXECUTION.isCanStop()).isTrue();
        assertThat(WorkflowExecutionStatus.READY_PAUSE.isCanStop()).isTrue();
        assertThat(WorkflowExecutionStatus.PAUSE.isCanStop()).isFalse();
        assertThat(WorkflowExecutionStatus.READY_STOP.isCanStop()).isTrue();
        assertThat(WorkflowExecutionStatus.STOP.isCanStop()).isFalse();
        assertThat(WorkflowExecutionStatus.FAILURE.isCanStop()).isFalse();
        assertThat(WorkflowExecutionStatus.SUCCESS.isCanStop()).isFalse();
        assertThat(WorkflowExecutionStatus.SERIAL_WAIT.isCanStop()).isTrue();
        assertThat(WorkflowExecutionStatus.FAILOVER.isCanStop()).isFalse();
    }

    @Test
    void testCanPause() {
        assertThat(WorkflowExecutionStatus.SUBMITTED_SUCCESS.isCanPause()).isFalse();
        assertThat(WorkflowExecutionStatus.RUNNING_EXECUTION.isCanPause()).isTrue();
        assertThat(WorkflowExecutionStatus.READY_PAUSE.isCanPause()).isTrue();
        assertThat(WorkflowExecutionStatus.PAUSE.isCanPause()).isFalse();
        assertThat(WorkflowExecutionStatus.READY_STOP.isCanPause()).isFalse();
        assertThat(WorkflowExecutionStatus.STOP.isCanPause()).isFalse();
        assertThat(WorkflowExecutionStatus.FAILURE.isCanPause()).isFalse();
        assertThat(WorkflowExecutionStatus.SUCCESS.isCanPause()).isFalse();
        assertThat(WorkflowExecutionStatus.SERIAL_WAIT.isCanPause()).isTrue();
        assertThat(WorkflowExecutionStatus.FAILOVER.isCanPause()).isFalse();
    }

}
