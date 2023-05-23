package org.apache.dolphinscheduler.server.master.runner.task.dynamic;

import org.apache.dolphinscheduler.common.constants.CommandKeyConstants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

public class DynamicCommandUtils {

    static public Command createCommand(ProcessInstance processInstance,
                                        Long subProcessDefinitionCode,
                                        Integer subProcessDefinitionVersion,
                                        Map<String, String> parameters) {
        Command command = new Command();
        if (processInstance.getCommandType().equals(CommandType.START_PROCESS)) {
            command.setCommandType(CommandType.DYNAMIC_GENERATION);
        } else {
            command.setCommandType(processInstance.getCommandType());
        }
        command.setProcessDefinitionCode(subProcessDefinitionCode);
        command.setProcessDefinitionVersion(subProcessDefinitionVersion);
        command.setTaskDependType(TaskDependType.TASK_POST);
        command.setFailureStrategy(processInstance.getFailureStrategy());
        command.setWarningType(processInstance.getWarningType());

        String globalParams = processInstance.getGlobalParams();
        if (StringUtils.isNotEmpty(globalParams)) {
            List<Property> parentParams = Lists.newArrayList(JSONUtils.toList(globalParams, Property.class));
            for (Property parentParam : parentParams) {
                parameters.put(parentParam.getProp(), parentParam.getValue());
            }
        }

        addDataToCommandParam(command, CommandKeyConstants.CMD_PARAM_START_PARAMS, JSONUtils.toJsonString(parameters));
        command.setExecutorId(processInstance.getExecutorId());
        command.setWarningGroupId(processInstance.getWarningGroupId());
        command.setProcessInstancePriority(processInstance.getProcessInstancePriority());
        command.setWorkerGroup(processInstance.getWorkerGroup());
        command.setDryRun(processInstance.getDryRun());
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
