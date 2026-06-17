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

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRepeatRunningRequest;

import org.junit.jupiter.api.Test;

class WorkflowInstanceRepeatTriggerTest {

    @Test
    void testRepeatRunningCommandShouldInheritWorkflowInstanceWorkerGroup() {
        final int workflowInstanceId = 1;
        final long workflowDefinitionCode = 1001L;
        final int workflowDefinitionVersion = 2;
        final String workerGroup = "worker-group-a";
        final WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(workflowInstanceId);
        workflowInstance.setWorkflowDefinitionCode(workflowDefinitionCode);
        workflowInstance.setWorkflowDefinitionVersion(workflowDefinitionVersion);
        workflowInstance.setWorkerGroup(workerGroup);

        final Command command = new WorkflowInstanceRepeatTrigger().constructTriggerCommand(
                WorkflowInstanceRepeatRunningRequest.builder()
                        .workflowInstanceId(workflowInstanceId)
                        .userId(1)
                        .build(),
                workflowInstance);

        assertThat(command.getCommandType()).isEqualTo(CommandType.REPEAT_RUNNING);
        assertThat(command.getWorkerGroup()).isEqualTo(workerGroup);
    }
}
