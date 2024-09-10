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

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRepeatRunningRequest;
import org.apache.dolphinscheduler.extract.master.transportor.workflow.WorkflowInstanceRepeatRunningResponse;

import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class WorkflowInstanceRepeatTrigger
        extends
            AbstractWorkflowInstanceTrigger<WorkflowInstanceRepeatRunningRequest, WorkflowInstanceRepeatRunningResponse> {

    @Override
    protected WorkflowInstance constructWorkflowInstance(final WorkflowInstanceRepeatRunningRequest repeatRunningRequest) {
        return getWorkflowInstance(repeatRunningRequest.getWorkflowInstanceId());
    }

    @Override
    protected Command constructTriggerCommand(final WorkflowInstanceRepeatRunningRequest repeatRunningRequest,
                                              final WorkflowInstance workflowInstance) {
        return Command.builder()
                .commandType(CommandType.REPEAT_RUNNING)
                .workflowInstanceId(workflowInstance.getId())
                .workflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode())
                .workflowDefinitionVersion(workflowInstance.getWorkflowDefinitionVersion())
                .executorId(repeatRunningRequest.getUserId())
                .startTime(new Date())
                .updateTime(new Date())
                .build();
    }

    @Override
    protected WorkflowInstanceRepeatRunningResponse onTriggerSuccess(final WorkflowInstance workflowInstance) {
        return WorkflowInstanceRepeatRunningResponse.success();
    }
}
