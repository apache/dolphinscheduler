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

package org.apache.dolphinscheduler.server.master.engine.workflow.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.master.command.BackfillWorkflowCommandParam;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerResponse;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowSucceedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.trigger.WorkflowBackfillTrigger;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WorkflowSuccessLifecycleListenerTest {

    @Mock
    private WorkflowBackfillTrigger workflowBackfillTrigger;

    @Mock
    private WorkflowInstanceDao workflowInstanceDao;

    @Mock
    private IWorkflowExecution workflowExecution;

    @Test
    void match_successAndFailureEvents() {
        final WorkflowSuccessLifecycleListener listener = createListener();

        assertThat(listener.match(WorkflowSucceedLifecycleEvent.of(null))).isTrue();
        assertThat(listener.match(WorkflowFailedLifecycleEvent.of(null))).isTrue();
    }

    @Test
    void notify_failureWithContinueStrategy_triggersNextBackfillDate() {
        final WorkflowSuccessLifecycleListener listener = createListener();
        final WorkflowInstance workflowInstance = createBackfillWorkflowInstance(FailureStrategy.CONTINUE);
        when(workflowExecution.getWorkflowInstance()).thenReturn(workflowInstance);
        when(workflowBackfillTrigger.triggerWorkflow(any()))
                .thenReturn(WorkflowBackfillTriggerResponse.success(123));

        listener.notifyWorkflowLifecycleEvent(workflowExecution, WorkflowFailedLifecycleEvent.of(workflowExecution));

        final ArgumentCaptor<WorkflowBackfillTriggerRequest> requestCaptor =
                ArgumentCaptor.forClass(WorkflowBackfillTriggerRequest.class);
        verify(workflowBackfillTrigger).triggerWorkflow(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getBackfillTimeList()).containsExactly("2024-08-12 00:00:00");
        assertThat(workflowInstance.getNextWorkflowInstanceId()).isEqualTo(123);
        verify(workflowInstanceDao).updateById(workflowInstance);
    }

    @Test
    void notify_failureWithEndStrategy_doesNotTriggerNextBackfillDate() {
        final WorkflowSuccessLifecycleListener listener = createListener();
        final WorkflowInstance workflowInstance = createBackfillWorkflowInstance(FailureStrategy.END);
        when(workflowExecution.getWorkflowInstance()).thenReturn(workflowInstance);

        listener.notifyWorkflowLifecycleEvent(workflowExecution, WorkflowFailedLifecycleEvent.of(workflowExecution));

        verify(workflowBackfillTrigger, never()).triggerWorkflow(any());
        verify(workflowInstanceDao, never()).updateById(any());
    }

    private WorkflowSuccessLifecycleListener createListener() {
        final WorkflowSuccessLifecycleListener listener = new WorkflowSuccessLifecycleListener();
        ReflectionTestUtils.setField(listener, "workflowBackfillTrigger", workflowBackfillTrigger);
        ReflectionTestUtils.setField(listener, "workflowInstanceDao", workflowInstanceDao);
        return listener;
    }

    private WorkflowInstance createBackfillWorkflowInstance(final FailureStrategy failureStrategy) {
        final WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setScheduleTime(DateUtils.stringToDate("2024-08-11 00:00:00"));
        workflowInstance.setFailureStrategy(failureStrategy);
        workflowInstance.setIsSubWorkflow(Flag.NO);
        workflowInstance.setCommandParam(JSONUtils.toJsonString(BackfillWorkflowCommandParam.builder()
                .backfillTimeList(Arrays.asList("2024-08-11 00:00:00", "2024-08-12 00:00:00"))
                .build()));
        return workflowInstance;
    }
}
