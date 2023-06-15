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

package org.apache.dolphinscheduler.service.expand;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_PROJECT_CODE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_PROJECT_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_DEFINITION_CODE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_DEFINITION_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_EXECUTE_PATH;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_INSTANCE_ID;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_WORKFLOW_DEFINITION_CODE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_WORKFLOW_DEFINITION_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_WORKFLOW_INSTANCE_ID;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.DateConstants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.model.Parameter;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CuringGlobalParams implements CuringParamsService {

    @Autowired
    private TimePlaceholderResolverExpandService timePlaceholderResolverExpandService;

    @Override
    public String convertParameterPlaceholders(String val, Map<String, String> allParamMap) {
        return ParameterUtils.convertParameterPlaceholders(val, allParamMap);
    }

    @Override
    public boolean timeFunctionNeedExpand(String placeholderName) {
        return timePlaceholderResolverExpandService.timeFunctionNeedExpand(placeholderName);
    }

    @Override
    public String timeFunctionExtension(Integer processInstanceId, String timezone, String placeholderName) {
        return timePlaceholderResolverExpandService.timeFunctionExtension(processInstanceId, timezone, placeholderName);
    }

    /**
     * here it is judged whether external expansion calculation is required and the calculation result is obtained
     * @param processInstanceId
     * @param globalParamMap
     * @param globalParamList
     * @param commandType
     * @param scheduleTime
     * @param timezone
     * @return
     */
    @Override
    public String curingGlobalParams(Integer processInstanceId, Map<String, String> globalParamMap,
                                     List<Parameter> globalParamList, CommandType commandType, Date scheduleTime,
                                     String timezone) {
        if (globalParamList == null || globalParamList.isEmpty()) {
            return null;
        }
        Map<String, String> globalMap = new HashMap<>();
        if (globalParamMap != null) {
            globalMap.putAll(globalParamMap);
        }
        Map<String, String> allParamMap = new HashMap<>();
        // If it is a complement, a complement time needs to be passed in, according to the task type
        Map<String, String> timeParams = BusinessTimeUtils.getBusinessTime(commandType, scheduleTime, timezone);

        if (timeParams != null) {
            allParamMap.putAll(timeParams);
        }
        allParamMap.putAll(globalMap);
        Set<Map.Entry<String, String>> entries = allParamMap.entrySet();
        Map<String, String> resolveMap = new HashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            String val = entry.getValue();
            if (val.contains(Constants.FUNCTION_START_WITH)) {
                String str = val;
                // whether external scaling calculation is required
                if (timeFunctionNeedExpand(val)) {
                    str = timeFunctionExtension(processInstanceId, timezone, val);
                }
                resolveMap.put(entry.getKey(), str);
            }
        }
        globalMap.putAll(resolveMap);
        for (Parameter parameter : globalParamList) {
            String val = globalMap.get(parameter.getKey());
            if (val != null) {
                parameter.setValue(val);
            }
        }
        return JSONUtils.toJsonString(globalParamList);
    }

    /**
     * the global parameters and local parameters used in the worker will be prepared here, and built-in parameters.
     *
     * @param taskInstance
     * @param parameters
     * @param processInstance
     * @return
     */
    @Override
    public Map<String, Parameter> paramParsingPreparation(@NonNull TaskInstance taskInstance,
                                                          @NonNull AbstractParameters parameters,
                                                          @NonNull ProcessInstance processInstance) {
        // assign value to definedParams here
        Map<String, String> globalParamsMap = setGlobalParamsMap(processInstance);
        Map<String, Parameter> globalParams = ParameterUtils.getUserDefParamsMap(globalParamsMap);

        // combining local and global parameters
        Map<String, Parameter> localParams = parameters.getInputLocalParametersMap();

        // stream pass params
        parameters.setVarPool(taskInstance.getVarPool());
        Map<String, Parameter> varParams = parameters.getVarPoolMap();

        // if it is a complement,
        // you need to pass in the task instance id to locate the time
        // of the process instance complement
        Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
        String timeZone = cmdParam.get(Constants.SCHEDULE_TIMEZONE);

        // built-in params
        Map<String, String> params = setBuiltInParamsMap(taskInstance, timeZone);

        if (MapUtils.isNotEmpty(params)) {
            globalParams.putAll(ParameterUtils.getUserDefParamsMap(params));
        }

        if (MapUtils.isNotEmpty(varParams)) {
            globalParams.putAll(varParams);
        }
        if (MapUtils.isNotEmpty(localParams)) {
            globalParams.putAll(localParams);
        }
        if (MapUtils.isNotEmpty(cmdParam)) {
            globalParams.putAll(ParameterUtils.getUserDefParamsMap(cmdParam));
        }
        Iterator<Map.Entry<String, Parameter>> iter = globalParams.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Parameter> en = iter.next();
            Parameter parameter = en.getValue();

            if (StringUtils.isNotEmpty(parameter.getValue())
                    && parameter.getValue().contains(Constants.FUNCTION_START_WITH)) {
                /**
                 *  local parameter refers to global parameter with the same name
                 *  note: the global parameters of the process instance here are solidified parameters,
                 *  and there are no variables in them.
                 */
                String val = parameter.getValue();
                // whether external scaling calculation is required
                if (timeFunctionNeedExpand(val)) {
                    val = timeFunctionExtension(taskInstance.getProcessInstanceId(), timeZone, val);
                }
                parameter.setValue(val);
            }
        }
        if (MapUtils.isEmpty(globalParams)) {
            globalParams = new HashMap<>();
        }
        // put schedule time param to params map
        Map<String, Parameter> paramsMap = preBuildBusinessParams(processInstance);
        if (MapUtils.isNotEmpty(paramsMap)) {
            globalParams.putAll(paramsMap);
        }
        return globalParams;
    }

    /**
     * build all built-in parameters
     * @param taskInstance
     * @param timeZone
     */
    private Map<String, String> setBuiltInParamsMap(@NonNull TaskInstance taskInstance, String timeZone) {
        CommandType commandType = taskInstance.getProcessInstance().getCmdTypeIfComplement();
        Date scheduleTime = taskInstance.getProcessInstance().getScheduleTime();

        Map<String, String> params = BusinessTimeUtils.getBusinessTime(commandType, scheduleTime, timeZone);

        if (StringUtils.isNotBlank(taskInstance.getExecutePath())) {
            params.put(PARAMETER_TASK_EXECUTE_PATH, taskInstance.getExecutePath());
        }
        params.put(PARAMETER_TASK_INSTANCE_ID, Integer.toString(taskInstance.getId()));
        params.put(PARAMETER_TASK_DEFINITION_NAME, taskInstance.getTaskDefine().getName());
        params.put(PARAMETER_TASK_DEFINITION_CODE, Long.toString(taskInstance.getTaskDefine().getCode()));
        params.put(PARAMETER_WORKFLOW_INSTANCE_ID, Integer.toString(taskInstance.getProcessInstance().getId()));
        params.put(PARAMETER_WORKFLOW_DEFINITION_NAME,
                taskInstance.getProcessInstance().getProcessDefinition().getName());
        params.put(PARAMETER_WORKFLOW_DEFINITION_CODE,
                Long.toString(taskInstance.getProcessInstance().getProcessDefinition().getCode()));
        params.put(PARAMETER_PROJECT_NAME, taskInstance.getProcessInstance().getProcessDefinition().getProjectName());
        params.put(PARAMETER_PROJECT_CODE,
                Long.toString(taskInstance.getProcessInstance().getProcessDefinition().getProjectCode()));
        return params;
    }
    private Map<String, String> setGlobalParamsMap(ProcessInstance processInstance) {
        Map<String, String> globalParamsMap = new HashMap<>(16);

        // global params string
        String globalParamsStr = processInstance.getGlobalParams();
        if (globalParamsStr != null) {
            List<Parameter> globalParamsList = JSONUtils.toList(globalParamsStr, Parameter.class);
            globalParamsMap
                    .putAll(globalParamsList.stream()
                            .collect(Collectors.toMap(Parameter::getKey, Parameter::getValue)));
        }
        return globalParamsMap;
    }

    @Override
    public Map<String, Parameter> preBuildBusinessParams(ProcessInstance processInstance) {
        Map<String, Parameter> paramsMap = new HashMap<>();
        // replace variable TIME with $[YYYYmmddd...] in shell file when history run job and batch complement job
        if (processInstance.getScheduleTime() != null) {
            Date date = processInstance.getScheduleTime();
            String dateTime = DateUtils.format(date, DateConstants.PARAMETER_FORMAT_TIME, null);
            Parameter p = new Parameter();
            p.setValue(dateTime);
            p.setKey(DateConstants.PARAMETER_DATETIME);
            paramsMap.put(DateConstants.PARAMETER_DATETIME, p);
        }
        return paramsMap;
    }
}
