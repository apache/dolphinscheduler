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

package org.apache.dolphinscheduler.server.master.engine.workflow.trigger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionTypeEnum;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.master.command.BackfillWorkflowCommandParam;
import org.apache.dolphinscheduler.extract.master.command.ICommandParam;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowBackfillTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowManualTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowScheduleTriggerRequest;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

class WorkflowTriggerParamsTest {

    @ParameterizedTest
    @EnumSource(WorkflowExecutionTypeEnum.class)
    void testManualTriggerPersistsParamsBeforeQueueing(WorkflowExecutionTypeEnum executionType) {
        final WorkflowManualTrigger trigger = prepareTrigger(new WorkflowManualTrigger(), executionType);
        trigger.triggerWorkflow(WorkflowManualTriggerRequest.builder()
                .workflowDefinitionCode(1L).workflowDefinitionVersion(1).userId(1)
                .startNodes(Collections.singletonList(10L))
                .startParamList(JSONUtils.toList("[{\"prop\":\"shared\",\"value\":\"override\"}]", Property.class))
                .timeZone("Asia/Shanghai").build());
    }

    @ParameterizedTest
    @EnumSource(WorkflowExecutionTypeEnum.class)
    void testScheduleTriggerPersistsParamsBeforeQueueing(WorkflowExecutionTypeEnum executionType) {
        final WorkflowScheduleTrigger trigger = prepareTrigger(new WorkflowScheduleTrigger(), executionType);
        trigger.triggerWorkflow(WorkflowScheduleTriggerRequest.builder()
                .workflowCode(1L).workflowVersion(1).userId(1).timezoneId("Asia/Shanghai").build());
    }

    @ParameterizedTest
    @EnumSource(WorkflowExecutionTypeEnum.class)
    void testBackfillTriggerPersistsParamsBeforeQueueing(WorkflowExecutionTypeEnum executionType) {
        final WorkflowBackfillTrigger trigger = prepareTrigger(new WorkflowBackfillTrigger(), executionType);
        trigger.triggerWorkflow(WorkflowBackfillTriggerRequest.builder()
                .workflowCode(1L).workflowVersion(1).userId(1)
                .startNodes(Collections.singletonList(10L))
                .startParamList(JSONUtils.toList("[{\"prop\":\"shared\",\"value\":\"override\"}]", Property.class))
                .backfillTimeList(Arrays.asList("2026-11-17 00:00:00", "2026-11-18 00:00:00")).build());
    }

    private <T extends AbstractWorkflowTrigger<?, ?>> T prepareTrigger(T original,
                                                                       WorkflowExecutionTypeEnum executionType) {
        final T trigger = spy(original);
        final WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setCode(1L);
        workflow.setVersion(1);
        workflow.setName("params");
        workflow.setExecutionType(executionType);
        workflow.setGlobalParams(
                "[{\"prop\":\"shared\",\"value\":\"default\"},{\"prop\":\"inherited\",\"value\":\"kept\"}]");
        doReturn(workflow).when(trigger).getProcessDefinition(1L, 1);
        final User user = new User();
        user.setUserName("admin");
        doReturn(user).when(trigger).getExecutorUser(1);
        final WorkflowInstanceDao instanceDao = mock(WorkflowInstanceDao.class);
        ReflectionTestUtils.setField(trigger, "workflowInstanceDao", instanceDao);
        ReflectionTestUtils.setField(trigger, "commandDao", mock(CommandDao.class));
        ReflectionTestUtils.setField(trigger, "serialCommandDao", mock(SerialCommandDao.class));
        doAnswer(invocation -> {
            final WorkflowInstance instance = invocation.getArgument(0);
            final ICommandParam params = JSONUtils.parseObject(instance.getCommandParam(), ICommandParam.class);
            assertThat(params).isNotNull();
            assertThat(params.getTimeZone()).isNotEmpty();
            assertThat(JSONUtils.toList(instance.getGlobalParams(), Property.class))
                    .extracting(Property::getProp).containsExactlyInAnyOrder("shared", "inherited");
            assertThat(JSONUtils.toList(instance.getGlobalParams(), Property.class))
                    .filteredOn(p -> p.getProp().equals("shared"))
                    .extracting(Property::getValue)
                    .containsExactly(original instanceof WorkflowScheduleTrigger ? "default" : "override");
            if (!(original instanceof WorkflowScheduleTrigger)) {
                assertThat(params.getStartNodes()).containsExactly(10L);
                assertThat(params.getCommandParams()).extracting(Property::getValue).containsExactly("override");
            }
            if (params instanceof BackfillWorkflowCommandParam) {
                assertThat(((BackfillWorkflowCommandParam) params).getBackfillTimeList())
                        .containsExactly("2026-11-17 00:00:00", "2026-11-18 00:00:00");
            }
            instance.setId(1);
            return 1;
        }).when(instanceDao).insert(any(WorkflowInstance.class));
        return trigger;
    }
}
