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

package org.apache.dolphinscheduler.api.executor.workflow.instance.rerun;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_PARAMS;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.executor.ExecuteFunction;
import org.apache.dolphinscheduler.api.executor.ExecuteRuntimeException;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.service.command.CommandService;

import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;

public class RepeatRunningExecuteFunction implements ExecuteFunction<RepeatRunningRequest, RepeatRunningResult> {

    private final CommandService commandService;

    public RepeatRunningExecuteFunction(CommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public RepeatRunningResult execute(RepeatRunningRequest request) throws ExecuteRuntimeException {
        checkNotNull(request, "request cannot be null");
        // todo: check workflow definition valid? or we don't need to do this check, since we will check in master
        // again.
        // todo: check tenant valid? or we don't need to do this check, since we need to check in master again.
        ProcessInstance workflowInstance = request.getWorkflowInstance();
        if (workflowInstance.getState() == null || !workflowInstance.getState().isFinished()) {
            throw new ExecuteRuntimeException(
                    String.format("The workflow instance: %s status is %s, cannot repeat running",
                            workflowInstance.getName(), workflowInstance.getState()));
        }
        Command command = Command.builder()
                .commandType(CommandType.REPEAT_RUNNING)
                .commandParam(JSONUtils.toJsonString(createCommandParams(workflowInstance)))
                .processDefinitionCode(workflowInstance.getProcessDefinitionCode())
                .processDefinitionVersion(workflowInstance.getProcessDefinitionVersion())
                .processInstanceId(workflowInstance.getId())
                .processInstancePriority(workflowInstance.getProcessInstancePriority())
                .testFlag(workflowInstance.getTestFlag())
                .build();
        if (commandService.createCommand(command) <= 0) {
            throw new ExecuteRuntimeException(
                    String.format("Repeat running workflow instance: %s failed, due to insert command to db failed",
                            workflowInstance.getName()));
        }
        return new RepeatRunningResult(command.getId());
    }

    @Override
    public ExecuteType getExecuteType() {
        return RepeatRunningExecuteFunctionBuilder.EXECUTE_TYPE;
    }

    private Map<String, Object> createCommandParams(ProcessInstance workflowInstance) {
        Map<String, Object> commandMap =
                JSONUtils.parseObject(workflowInstance.getCommandParam(), new TypeReference<Map<String, Object>>() {
                });
        Map<String, Object> repeatRunningCommandParams = new HashMap<>();
        Optional.ofNullable(MapUtils.getObject(commandMap, CMD_PARAM_START_PARAMS))
                .ifPresent(startParams -> repeatRunningCommandParams.put(CMD_PARAM_START_PARAMS, startParams));
        repeatRunningCommandParams.put(CMD_PARAM_RECOVER_PROCESS_ID_STRING, workflowInstance.getId());
        return repeatRunningCommandParams;
    }
}
