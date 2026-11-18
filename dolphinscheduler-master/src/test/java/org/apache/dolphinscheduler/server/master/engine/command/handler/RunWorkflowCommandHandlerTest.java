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

package org.apache.dolphinscheduler.server.master.engine.command.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteContext;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteContext.WorkflowExecuteContextBuilder;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RunWorkflowCommandHandlerTest {

    @Test
    void testStartCommandPreservesPersistedInstanceParams() {
        final WorkflowInstance instance = new WorkflowInstance();
        instance.setId(1);
        instance.setCommandParam("{\"commandType\":\"START_PROCESS\"}");
        instance.setGlobalParams("[{\"prop\":\"param\",\"value\":\"original\"}]");
        final WorkflowInstanceDao instanceDao = mock(WorkflowInstanceDao.class);
        when(instanceDao.queryById(1)).thenReturn(instance);
        final RunWorkflowCommandHandler handler = new RunWorkflowCommandHandler();
        ReflectionTestUtils.setField(handler, "workflowInstanceDao", instanceDao);
        ReflectionTestUtils.setField(handler, "masterConfig", new MasterConfig());
        final WorkflowExecuteContextBuilder context = WorkflowExecuteContext.builder().withCommand(Command.builder()
                .workflowInstanceId(1).commandType(CommandType.START_PROCESS).build());

        context.setWorkflowDefinition(new WorkflowDefinition());
        handler.assembleWorkflowInstance(context);

        assertThat(context.getWorkflowInstance().getCommandParam()).isEqualTo("{\"commandType\":\"START_PROCESS\"}");
        assertThat(context.getWorkflowInstance().getGlobalParams())
                .isEqualTo("[{\"prop\":\"param\",\"value\":\"original\"}]");
    }
}
