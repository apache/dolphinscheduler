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

package org.apache.dolphinscheduler.server.master.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;

import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

class WorkflowInstanceMetricsTest {

    @Test
    void testIncWorkflowInstanceByStateAndWorkflowDefinitionCode_submitState() {
        String defCode = "test_submit_1";
        WorkflowInstanceMetrics.incWorkflowInstanceByStateAndWorkflowDefinitionCode(
                WorkflowExecutionStatus.SUBMITTED_SUCCESS, defCode);
        Counter counter = Metrics.globalRegistry.find("ds.workflow.instance.count")
                .tag("state", WorkflowExecutionStatus.SUBMITTED_SUCCESS.name())
                .tag("workflow.definition.code", defCode)
                .counter();
        assertNotNull(counter, "Counter should be registered for submit state");
        assertEquals(1, counter.count(), 0.001);
    }

    @Test
    void testIncWorkflowInstanceByStateAndWorkflowDefinitionCode_failureState() {
        String defCode = "test_failure_1";
        WorkflowInstanceMetrics.incWorkflowInstanceByStateAndWorkflowDefinitionCode(
                WorkflowExecutionStatus.FAILURE, defCode);
        Counter counter = Metrics.globalRegistry.find("ds.workflow.instance.count")
                .tag("state", WorkflowExecutionStatus.FAILURE.name())
                .tag("workflow.definition.code", defCode)
                .counter();
        assertNotNull(counter, "Counter should be registered for fail state");
        assertEquals(1, counter.count(), 0.001);
    }

    @Test
    void testIncWorkflowInstanceByStateAndWorkflowDefinitionCode_successState() {
        String defCode = "test_success_1";
        WorkflowInstanceMetrics.incWorkflowInstanceByStateAndWorkflowDefinitionCode(
                WorkflowExecutionStatus.SUCCESS, defCode);
        Counter counter = Metrics.globalRegistry.find("ds.workflow.instance.count")
                .tag("state", WorkflowExecutionStatus.SUCCESS.name())
                .tag("workflow.definition.code", defCode)
                .counter();
        assertNotNull(counter, "Counter should be registered for success state");
        assertEquals(1, counter.count(), 0.001);
    }

    @Test
    void testIncWorkflowInstanceByStateAndWorkflowDefinitionCode_stopState() {
        String defCode = "test_stop_1";
        WorkflowInstanceMetrics.incWorkflowInstanceByStateAndWorkflowDefinitionCode(
                WorkflowExecutionStatus.STOP, defCode);
        Counter counter = Metrics.globalRegistry.find("ds.workflow.instance.count")
                .tag("state", WorkflowExecutionStatus.STOP.name())
                .tag("workflow.definition.code", defCode)
                .counter();
        assertNotNull(counter, "Counter should be registered for stop state");
        assertEquals(1, counter.count(), 0.001);
    }

    @Test
    void testIncWorkflowInstanceByStateAndWorkflowDefinitionCode_pauseState() {
        String defCode = "test_pause_1";
        WorkflowInstanceMetrics.incWorkflowInstanceByStateAndWorkflowDefinitionCode(
                WorkflowExecutionStatus.PAUSE, defCode);
        Counter counter = Metrics.globalRegistry.find("ds.workflow.instance.count")
                .tag("state", WorkflowExecutionStatus.PAUSE.name())
                .tag("workflow.definition.code", defCode)
                .counter();
        assertNotNull(counter, "Counter should be registered for pause state");
        assertEquals(1, counter.count(), 0.001);
    }

    @Test
    void testIncWorkflowInstanceByStateAndWorkflowDefinitionCode_failoverState() {
        String defCode = "test_failover_1";
        WorkflowInstanceMetrics.incWorkflowInstanceByStateAndWorkflowDefinitionCode(
                WorkflowExecutionStatus.FAILOVER, defCode);
        Counter counter = Metrics.globalRegistry.find("ds.workflow.instance.count")
                .tag("state", WorkflowExecutionStatus.FAILOVER.name())
                .tag("workflow.definition.code", defCode)
                .counter();
        assertNotNull(counter, "Counter should be registered for failover state");
        assertEquals(1, counter.count(), 0.001);
    }

    @Test
    void testIncWorkflowInstanceByStateAndWorkflowDefinitionCode_runningState() {
        String defCode = "test_running_1";
        WorkflowInstanceMetrics.incWorkflowInstanceByStateAndWorkflowDefinitionCode(
                WorkflowExecutionStatus.RUNNING_EXECUTION, defCode);
        Counter counter = Metrics.globalRegistry.find("ds.workflow.instance.count")
                .tag("state", WorkflowExecutionStatus.RUNNING_EXECUTION.name())
                .tag("workflow.definition.code", defCode)
                .counter();
        assertNotNull(counter, "Counter should be registered for running state");
        assertEquals(1, counter.count(), 0.001);
    }

    @Test
    void testRecordCommandQueryTime() {
        WorkflowInstanceMetrics.recordCommandQueryTime(100L);
        Timer timer = Metrics.globalRegistry.find("ds.workflow.command.query.duration").timer();
        assertNotNull(timer, "Command query timer should be registered");
        assertEquals(1, timer.count(), "Timer should have recorded one event");
    }

    @Test
    void testRecordWorkflowInstanceGenerateTime() {
        WorkflowInstanceMetrics.recordWorkflowInstanceGenerateTime(200L);
        Timer timer = Metrics.globalRegistry.find("ds.workflow.instance.generate.duration").timer();
        assertNotNull(timer, "Workflow instance generate timer should be registered");
        assertEquals(1, timer.count(), "Timer should have recorded one event");
    }

    @Test
    void testRegisterWorkflowInstanceRunningGauge() {
        WorkflowInstanceMetrics.registerWorkflowInstanceRunningGauge(() -> 10);
        assertNotNull(Metrics.globalRegistry.find("ds.workflow.instance.running").gauge(),
                "Running gauge should be registered");
    }

    @Test
    void testRegisterWorkflowInstanceResubmitGauge() {
        WorkflowInstanceMetrics.registerWorkflowInstanceResubmitGauge(() -> 3);
        assertNotNull(Metrics.globalRegistry.find("ds.workflow.instance.resubmit").gauge(),
                "Resubmit gauge should be registered");
    }

}
