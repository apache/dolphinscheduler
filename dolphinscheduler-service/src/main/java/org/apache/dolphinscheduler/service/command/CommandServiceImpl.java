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

package org.apache.dolphinscheduler.service.command;

import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ErrorCommand;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.service.utils.ParamUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.annotation.Counted;

/**
 * Command Service implementation
 */
@Component
@Slf4j
public class CommandServiceImpl implements CommandService {

    @Autowired
    private ErrorCommandMapper errorCommandMapper;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProcessDefinitionMapper processDefineMapper;

    @Override
    public void moveToErrorCommand(Command command, String message) {
        ErrorCommand errorCommand = new ErrorCommand(command, message);
        this.errorCommandMapper.insert(errorCommand);
        this.commandMapper.deleteById(command.getId());
    }

    @Override
    @Counted("ds.workflow.create.command.count")
    public int createCommand(Command command) {
        int result = 0;
        if (command == null) {
            return result;
        }
        // add command timezone
        Schedule schedule = scheduleMapper.queryByProcessDefinitionCode(command.getProcessDefinitionCode());
        if (schedule != null) {
            Map<String, String> commandParams =
                    StringUtils.isNotBlank(command.getCommandParam()) ? JSONUtils.toMap(command.getCommandParam())
                            : new HashMap<>();
            commandParams.put(Constants.SCHEDULE_TIMEZONE, schedule.getTimezoneId());
            command.setCommandParam(JSONUtils.toJsonString(commandParams));
        }
        command.setId(null);
        result = commandMapper.insert(command);
        return result;
    }

    @Override
    public boolean verifyIsNeedCreateCommand(Command command) {
        boolean isNeedCreate = true;
        EnumMap<CommandType, Integer> cmdTypeMap = new EnumMap<>(CommandType.class);
        cmdTypeMap.put(CommandType.REPEAT_RUNNING, 1);
        cmdTypeMap.put(CommandType.RECOVER_SUSPENDED_PROCESS, 1);
        cmdTypeMap.put(CommandType.START_FAILURE_TASK_PROCESS, 1);
        CommandType commandType = command.getCommandType();

        if (!cmdTypeMap.containsKey(commandType)) {
            return true;
        }

        ObjectNode cmdParamObj = JSONUtils.parseObject(command.getCommandParam());
        int processInstanceId = cmdParamObj.path(CMD_PARAM_RECOVER_PROCESS_ID_STRING).asInt();

        List<Command> commands = commandMapper.selectList(null);
        // for all commands
        for (Command tmpCommand : commands) {
            if (!cmdTypeMap.containsKey(tmpCommand.getCommandType())) {
                continue;
            }
            ObjectNode tempObj = JSONUtils.parseObject(tmpCommand.getCommandParam());
            if (tempObj != null
                    && processInstanceId == tempObj.path(CMD_PARAM_RECOVER_PROCESS_ID_STRING).asInt()) {
                isNeedCreate = false;
                break;
            }
        }
        return isNeedCreate;
    }

    @Override
    public Command createSubProcessCommand(ProcessInstance parentProcessInstance, ProcessInstance childInstance,
                                           ProcessInstanceMap instanceMap, TaskInstance task) {
        CommandType commandType = getSubCommandType(parentProcessInstance, childInstance);
        Map<String, Object> subProcessParam = JSONUtils.toMap(task.getTaskParams(), String.class, Object.class);
        long childDefineCode = 0L;
        if (subProcessParam.containsKey(CMD_PARAM_SUB_PROCESS_DEFINE_CODE)) {
            try {
                childDefineCode =
                        Long.parseLong(
                                String.valueOf(subProcessParam.get(CMD_PARAM_SUB_PROCESS_DEFINE_CODE)));
            } catch (NumberFormatException nfe) {
                log.error("processDefinitionCode is not a number", nfe);
                return null;
            }
        }
        ProcessDefinition subProcessDefinition = processDefineMapper.queryByCode(childDefineCode);

        Object localParams = subProcessParam.get(Constants.LOCAL_PARAMS);
        List<Property> allParam = JSONUtils.toList(JSONUtils.toJsonString(localParams), Property.class);
        Map<String, String> globalMap = ParamUtils.getGlobalParamMap(task.getVarPool());
        Map<String, String> fatherParams = new HashMap<>();
        if (CollectionUtils.isNotEmpty(allParam)) {
            for (Property info : allParam) {
                if (Direct.OUT == info.getDirect()) {
                    continue;
                }
                fatherParams.put(info.getProp(), globalMap.get(info.getProp()));
            }
        }
        String processParam = ParamUtils.getSubWorkFlowParam(instanceMap, parentProcessInstance, fatherParams);
        int subProcessInstanceId =
                childInstance == null ? 0 : (childInstance.getId() == null ? 0 : childInstance.getId());
        return new Command(
                commandType,
                TaskDependType.TASK_POST,
                parentProcessInstance.getFailureStrategy(),
                parentProcessInstance.getExecutorId(),
                subProcessDefinition.getCode(),
                processParam,
                parentProcessInstance.getWarningType(),
                parentProcessInstance.getWarningGroupId(),
                parentProcessInstance.getScheduleTime(),
                task.getWorkerGroup(),
                task.getEnvironmentCode(),
                parentProcessInstance.getProcessInstancePriority(),
                parentProcessInstance.getDryRun(),
                subProcessInstanceId,
                subProcessDefinition.getVersion(),
                parentProcessInstance.getTestFlag());
    }

    /**
     * get sub work flow command type
     * child instance exist: child command = fatherCommand
     * child instance not exists: child command = fatherCommand[0]
     */
    private CommandType getSubCommandType(ProcessInstance parentProcessInstance, ProcessInstance childInstance) {
        CommandType commandType = parentProcessInstance.getCommandType();
        if (childInstance == null) {
            String fatherHistoryCommand = parentProcessInstance.getHistoryCmd();
            commandType = CommandType.valueOf(fatherHistoryCommand.split(Constants.COMMA)[0]);
        }
        return commandType;
    }
}
