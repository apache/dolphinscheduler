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

package org.apache.dolphinscheduler.api.executor.workflow.instance.pause.recover;

import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.executor.ExecuteFunction;
import org.apache.dolphinscheduler.api.executor.ExecuteRuntimeException;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.service.command.CommandService;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class RecoverExecuteFunction implements ExecuteFunction<RecoverExecuteRequest, RecoverExecuteResult> {

    private final CommandService commandService;

    public RecoverExecuteFunction(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public RecoverExecuteResult execute(RecoverExecuteRequest request) throws ExecuteRuntimeException {
        ProcessInstance workflowInstance = request.getWorkflowInstance();
        if (!workflowInstance.getState().isPause()) {
            throw new ExecuteRuntimeException(
                    String.format("The workflow instance: %s state is %s, cannot recovery", workflowInstance.getName(),
                            workflowInstance.getState()));
        }
        Command command = Command.builder()
                .commandType(CommandType.RECOVER_SUSPENDED_PROCESS)
                .processDefinitionCode(workflowInstance.getProcessDefinitionCode())
                .processDefinitionVersion(workflowInstance.getProcessDefinitionVersion())
                .processInstanceId(workflowInstance.getId())
                .commandParam(JSONUtils.toJsonString(createCommandParam(workflowInstance)))
                .executorId(request.getExecuteUser().getId())
                .testFlag(workflowInstance.getTestFlag())
                .build();
        if (commandService.createCommand(command) <= 0) {
            throw new ExecuteRuntimeException(
                    String.format("Recovery workflow instance: %s failed, due to insert command to db failed",
                            workflowInstance.getName()));
        }
        return new RecoverExecuteResult(command);
    }

    private Map<String, Object> createCommandParam(ProcessInstance workflowInstance) {
        return new ImmutableMap.Builder<String, Object>()
                .put(CMD_PARAM_RECOVER_PROCESS_ID_STRING, workflowInstance.getId())
                .build();
    }

    @Override
    public ExecuteType getExecuteType() {
        return RecoverExecuteFunctionBuilder.EXECUTE_TYPE;
    }
}
