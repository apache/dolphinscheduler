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

import static java.util.Objects.nonNull;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_PROJECT_CODE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_PROJECT_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_DEFINITION_CODE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_DEFINITION_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_EXECUTE_PATH;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_INSTANCE_ID;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_WORKFLOW_DEFINITION_CODE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_WORKFLOW_DEFINITION_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_WORKFLOW_INSTANCE_ID;

import org.apache.dolphinscheduler.common.constants.CommandKeyConstants;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.constants.DateConstants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectParameter;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.mapper.ProjectParameterMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertyUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CuringParamsServiceImpl implements CuringParamsService {

    @Autowired
    private TimePlaceholderResolverExpandService timePlaceholderResolverExpandService;

    @Autowired
    private ProjectParameterMapper projectParameterMapper;

    @Override
    public String convertParameterPlaceholders(String val, Map<String, Property> allParamMap) {
        Map<String, String> paramMap = allParamMap
                .entrySet()
                .stream()
                .filter(entry -> nonNull(entry.getValue().getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue()));
        return ParameterUtils.convertParameterPlaceholders(val, paramMap);
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
                                     List<Property> globalParamList, CommandType commandType, Date scheduleTime,
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
        for (Property property : globalParamList) {
            String val = globalMap.get(property.getProp());
            if (val != null) {
                property.setValue(val);
            }
        }
        return JSONUtils.toJsonString(globalParamList);
    }

    @Override
    public Map<String, Property> parseWorkflowStartParam(@Nullable Map<String, String> cmdParam) {
        if (cmdParam == null || !cmdParam.containsKey(CommandKeyConstants.CMD_PARAM_START_PARAMS)) {
            return new HashMap<>();
        }
        String startParamJson = cmdParam.get(CommandKeyConstants.CMD_PARAM_START_PARAMS);
        List<Property> propertyList = PropertyUtils.startParamsTransformPropertyList(startParamJson);
        if (CollectionUtils.isEmpty(propertyList)) {
            return new HashMap<>();
        }
        return propertyList.stream().collect(Collectors.toMap(Property::getProp, Function.identity()));
    }

    @Override
    public Map<String, Property> parseWorkflowFatherParam(@Nullable Map<String, String> cmdParam) {
        if (cmdParam == null || !cmdParam.containsKey(CommandKeyConstants.CMD_PARAM_FATHER_PARAMS)) {
            return new HashMap<>();
        }
        String startParamJson = cmdParam.get(CommandKeyConstants.CMD_PARAM_FATHER_PARAMS);
        Map<String, String> startParamMap = JSONUtils.toMap(startParamJson);
        return startParamMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> new Property(entry.getKey(), Direct.IN, DataType.VARCHAR, entry.getValue())));
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
    public Map<String, Property> paramParsingPreparation(@NonNull TaskInstance taskInstance,
                                                         @NonNull AbstractParameters parameters,
                                                         @NonNull ProcessInstance processInstance) {
        Map<String, Property> prepareParamsMap = new HashMap<>();

        // assign value to definedParams here
        Map<String, Property> globalParams = setGlobalParamsMap(processInstance);

        // combining local and global parameters
        Map<String, Property> localParams = parameters.getInputLocalParametersMap();

        // stream pass params
        parameters.setVarPool(taskInstance.getVarPool());
        Map<String, Property> varParams = parameters.getVarPoolMap();

        // if it is a complement,
        // you need to pass in the task instance id to locate the time
        // of the process instance complement
        Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
        String timeZone = cmdParam.get(Constants.SCHEDULE_TIMEZONE);

        // built-in params
        Map<String, String> builtInParams = setBuiltInParamsMap(taskInstance, timeZone);

        // project-level params
        Map<String, Property> projectParams = getProjectParameterMap(taskInstance.getProjectCode());

        if (MapUtils.isNotEmpty(builtInParams)) {
            prepareParamsMap.putAll(ParameterUtils.getUserDefParamsMap(builtInParams));
        }

        if (MapUtils.isNotEmpty(projectParams)) {
            prepareParamsMap.putAll(projectParams);
        }

        if (MapUtils.isNotEmpty(globalParams)) {
            prepareParamsMap.putAll(globalParams);
        }

        if (MapUtils.isNotEmpty(varParams)) {
            prepareParamsMap.putAll(varParams);
        }

        if (MapUtils.isNotEmpty(localParams)) {
            prepareParamsMap.putAll(localParams);
        }

        if (MapUtils.isNotEmpty(cmdParam)) {
            prepareParamsMap.putAll(parseWorkflowStartParam(cmdParam));
        }

        Iterator<Map.Entry<String, Property>> iter = prepareParamsMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Property> en = iter.next();
            Property property = en.getValue();

            if (StringUtils.isNotEmpty(property.getValue())
                    && property.getValue().contains(Constants.FUNCTION_START_WITH)) {
                /**
                 *  local parameter refers to global parameter with the same name
                 *  note: the global parameters of the process instance here are solidified parameters,
                 *  and there are no variables in them.
                 */
                String val = property.getValue();
                // whether external scaling calculation is required
                if (timeFunctionNeedExpand(val)) {
                    val = timeFunctionExtension(taskInstance.getProcessInstanceId(), timeZone, val);
                } else {
                    // handle some chain parameter assign, such as `{"var1": "${var2}", "var2": 1}` should be convert to
                    // `{"var1": 1, "var2": 1}`
                    val = convertParameterPlaceholders(val, prepareParamsMap);
                }
                property.setValue(val);
            }
        }

        // put schedule time param to params map
        Map<String, Property> paramsMap = preBuildBusinessParams(processInstance);
        if (MapUtils.isNotEmpty(paramsMap)) {
            prepareParamsMap.putAll(paramsMap);
        }
        return prepareParamsMap;
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
    private Map<String, Property> setGlobalParamsMap(ProcessInstance processInstance) {
        Map<String, Property> globalParamsMap = new HashMap<>(16);

        // global params string
        String globalParamsStr = processInstance.getGlobalParams();
        if (globalParamsStr != null) {
            List<Property> globalParamsList = JSONUtils.toList(globalParamsStr, Property.class);
            globalParamsMap
                    .putAll(globalParamsList.stream()
                            .collect(Collectors.toMap(Property::getProp, Function.identity())));
        }
        return globalParamsMap;
    }

    @Override
    public Map<String, Property> preBuildBusinessParams(ProcessInstance processInstance) {
        Map<String, Property> paramsMap = new HashMap<>();
        // replace variable TIME with $[YYYYmmddd...] in shell file when history run job and batch complement job
        if (processInstance.getScheduleTime() != null) {
            Date date = processInstance.getScheduleTime();
            String dateTime = DateUtils.format(date, DateConstants.PARAMETER_FORMAT_TIME, null);
            Property p = new Property();
            p.setValue(dateTime);
            p.setProp(DateConstants.PARAMETER_DATETIME);
            paramsMap.put(DateConstants.PARAMETER_DATETIME, p);
        }
        return paramsMap;
    }

    @Override
    public Map<String, Property> getProjectParameterMap(long projectCode) {
        Map<String, Property> result = new HashMap<>(16);
        List<ProjectParameter> projectParameterList = projectParameterMapper.queryByProjectCode(projectCode);

        projectParameterList.forEach(projectParameter -> {
            Property property = new Property(projectParameter.getParamName(),
                    Direct.IN,
                    Enum.valueOf(DataType.class, projectParameter.getParamDataType()),
                    projectParameter.getParamValue());
            result.put(projectParameter.getParamName(), property);
        });

        return result;
    }
}
