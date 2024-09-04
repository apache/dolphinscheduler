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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.dynamic;

import org.apache.dolphinscheduler.common.constants.CommandKeyConstants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

public class DynamicCommandUtils {

    static public Command createCommand(WorkflowInstance workflowInstance,
                                        Long subProcessDefinitionCode,
                                        Integer subProcessDefinitionVersion,
                                        Map<String, String> parameters) {
        Command command = new Command();
        if (workflowInstance.getCommandType().equals(CommandType.START_PROCESS)) {
            command.setCommandType(CommandType.DYNAMIC_GENERATION);
        } else {
            command.setCommandType(workflowInstance.getCommandType());
        }
        command.setWorkflowDefinitionCode(subProcessDefinitionCode);
        command.setWorkflowDefinitionVersion(subProcessDefinitionVersion);
        command.setTaskDependType(TaskDependType.TASK_POST);
        command.setFailureStrategy(workflowInstance.getFailureStrategy());
        command.setWarningType(workflowInstance.getWarningType());

        String globalParams = workflowInstance.getGlobalParams();
        if (StringUtils.isNotEmpty(globalParams)) {
            List<Property> parentParams = Lists.newArrayList(JSONUtils.toList(globalParams, Property.class));
            for (Property parentParam : parentParams) {
                parameters.put(parentParam.getProp(), parentParam.getValue());
            }
        }

        addDataToCommandParam(command, CommandKeyConstants.CMD_PARAM_START_PARAMS, JSONUtils.toJsonString(parameters));
        command.setExecutorId(workflowInstance.getExecutorId());
        command.setWarningGroupId(workflowInstance.getWarningGroupId());
        command.setWorkflowInstancePriority(workflowInstance.getWorkflowInstancePriority());
        command.setWorkerGroup(workflowInstance.getWorkerGroup());
        command.setDryRun(workflowInstance.getDryRun());
        command.setTenantCode(workflowInstance.getTenantCode());
        return command;
    }

    static public String getDataFromCommandParam(String commandParam, String key) {
        Map<String, String> cmdParam = JSONUtils.toMap(commandParam);
        return cmdParam.get(key);
    }

    static void addDataToCommandParam(Command command, String key, String data) {
        Map<String, String> cmdParam = JSONUtils.toMap(command.getCommandParam());
        if (cmdParam == null) {
            cmdParam = new HashMap<>();
        }
        cmdParam.put(key, data);
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
    }

}
