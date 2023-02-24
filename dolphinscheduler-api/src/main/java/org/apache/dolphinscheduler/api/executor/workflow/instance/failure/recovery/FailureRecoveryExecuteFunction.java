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

package org.apache.dolphinscheduler.api.executor.workflow.instance.failure.recovery;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.executor.ExecuteFunction;
import org.apache.dolphinscheduler.api.executor.ExecuteRuntimeException;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.service.command.CommandService;

public class FailureRecoveryExecuteFunction implements ExecuteFunction<FailureRecoveryRequest, FailureRecoveryResult> {

    private final CommandService commandService;

    public FailureRecoveryExecuteFunction(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public FailureRecoveryResult execute(FailureRecoveryRequest request) throws ExecuteRuntimeException {
        ProcessInstance workflowInstance = request.getWorkflowInstance();
        if (!workflowInstance.getState().isFailure()) {
            throw new ExecuteRuntimeException(
                    String.format("The workflow instance: %s status is %s, can not be recovered",
                            workflowInstance.getName(), workflowInstance.getState()));
        }

        Command command = Command.builder()
                .commandType(CommandType.START_FAILURE_TASK_PROCESS)
                .processDefinitionCode(workflowInstance.getProcessDefinitionCode())
                .processDefinitionVersion(workflowInstance.getProcessDefinitionVersion())
                .processInstanceId(workflowInstance.getId())
                .executorId(request.getExecuteUser().getId())
                .testFlag(workflowInstance.getTestFlag())
                .build();
        if (commandService.createCommand(command) <= 0) {
            throw new ExecuteRuntimeException(
                    "Failure recovery workflow instance failed, due to insert command to db failed");
        }
        return new FailureRecoveryResult(command.getId());
    }

    @Override
    public ExecuteType getExecuteType() {
        return FailureRecoveryExecuteFunctionBuilder.EXECUTE_TYPE;
    }
}
