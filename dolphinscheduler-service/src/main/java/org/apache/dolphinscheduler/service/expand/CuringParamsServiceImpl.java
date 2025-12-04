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
import org.apache.dolphinscheduler.dao.entity.ProjectParameter;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.ProjectParameterMapper;
import org.apache.dolphinscheduler.extract.master.command.ICommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CuringParamsServiceImpl implements CuringParamsService {

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

    /**
     * here it is judged whether external expansion calculation is required and the calculation result is obtained
     *
     * @param workflowInstanceId
     * @param globalParamMap
     * @param globalParamList
     * @param commandType
     * @param scheduleTime
     * @param timezone
     * @return
     */
    @Override
    public String curingGlobalParams(Integer workflowInstanceId, Map<String, String> globalParamMap,
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
     * Prepares the final map of task execution parameters by merging parameters from multiple sources
     * in a well-defined priority order. The resulting map is guaranteed to contain only valid entries:
     * <ul>
     *   <li>Keys are non-null and non-blank strings</li>
     *   <li>Values are non-null {@link Property} objects</li>
     * </ul>
     *
     * <p> The priority of the parameters is as follows:
     * <p> varpool > command parameters > local parameters > global parameters > project parameters > built-in parameters
     * todo: Use TaskRuntimeParams to represent this.
     *
     * @param taskInstance
     * @param parameters
     * @param workflowInstance
     * @param projectName
     * @param workflowDefinitionName
     * @return
     */
    @Override
    public Map<String, Property> paramParsingPreparation(@NonNull TaskInstance taskInstance,
                                                         @NonNull AbstractParameters parameters,
                                                         @NonNull WorkflowInstance workflowInstance,
                                                         String projectName,
                                                         String workflowDefinitionName) {
        Map<String, Property> prepareParamsMap = new HashMap<>();

        // If it is a complement, you need to pass in the task instance id
        // to locate the time of the process instance complement.
        ICommandParam commandParam = JSONUtils.parseObject(workflowInstance.getCommandParam(), ICommandParam.class);
        if (commandParam == null) {
            throw new ServiceException(String.format("Failed to parse command parameter for workflow instance %s",
                    workflowInstance.getId()));
        }
        String timeZone = commandParam.getTimeZone();

        // 1. Built-in parameters (lowest precedence)
        Map<String, String> builtInParams = setBuiltInParamsMap(
                taskInstance, workflowInstance, timeZone, projectName, workflowDefinitionName);
        safePutAll(prepareParamsMap, ParameterUtils.getUserDefParamsMap(builtInParams));

        // 2. Project-level parameters
        Map<String, Property> projectParams = getProjectParameterMap(taskInstance.getProjectCode());
        safePutAll(prepareParamsMap, projectParams);

        // 3. Workflow global parameters
        Map<String, Property> globalParams = parseGlobalParamsMap(workflowInstance);
        safePutAll(prepareParamsMap, globalParams);

        // 4. Task-local parameters
        Map<String, Property> localParams = parameters.getInputLocalParametersMap();
        safePutAll(prepareParamsMap, localParams);

        // 5. Command-line / complement parameters
        if (CollectionUtils.isNotEmpty(commandParam.getCommandParams())) {
            Map<String, Property> commandParamsMap = commandParam.getCommandParams().stream()
                    .filter(prop -> StringUtils.isNotBlank(prop.getProp()))
                    .collect(Collectors.toMap(
                            Property::getProp,
                            Function.identity(),
                            (v1, v2) -> v2 // retain last on duplicate key
                    ));
            safePutAll(prepareParamsMap, commandParamsMap);
        }

        // 6. VarPool: override values only for existing IN-direction parameters
        List<Property> varPools = parseVarPool(taskInstance);
        if (CollectionUtils.isNotEmpty(varPools)) {
            for (Property varPool : varPools) {
                if (StringUtils.isBlank(varPool.getProp())) {
                    continue;
                }
                Property targetParam = prepareParamsMap.get(varPool.getProp());
                if (targetParam != null && Direct.IN.equals(targetParam.getDirect())) {
                    targetParam.setValue(varPool.getValue());
                }
            }
        }

        // 7. Inject business/scheduling parameters (e.g., ${datetime}), which may contain or reference placeholders
        Map<String, Property> businessParams = preBuildBusinessParams(workflowInstance);
        safePutAll(prepareParamsMap, businessParams);

        // 8. Resolve all placeholders (e.g., "${output_dir}") using the current parameter context
        resolvePlaceholders(prepareParamsMap);

        return prepareParamsMap;
    }

    /**
     * Safely merges entries from the {@code source} map into the {@code target} map,
     * skipping any entry with a {@code null}, empty, or blank key, or a {@code null} value.
     *
     * @param target the destination map to merge into (must not be null)
     * @param source the source map whose valid entries will be copied (may be null or empty)
     */
    private void safePutAll(Map<String, Property> target, Map<String, Property> source) {
        if (MapUtils.isEmpty(source)) {
            return;
        }
        source.forEach((key, value) -> {
            if (StringUtils.isNotBlank(key) && value != null) {
                target.put(key, value);
            } else {
                log.warn("Skipped invalid parameter entry: key='{}', value={}", key, value);
            }
        });
    }

    /**
     * Resolves placeholder expressions (e.g., "${var}") in parameter values by substituting them
     * with actual values from the current {@code paramsMap}.
     *
     * @param paramsMap the map of parameters (key: parameter name, value: {@link Property}) to resolve
     */
    private void resolvePlaceholders(Map<String, Property> paramsMap) {
        for (Property prop : paramsMap.values()) {
            String val = prop.getValue();
            if (StringUtils.isNotEmpty(val) && val.contains(Constants.FUNCTION_START_WITH)) {
                prop.setValue(convertParameterPlaceholders(val, paramsMap));
            }
        }
    }

    /**
     * build all built-in parameters
     *
     * @param taskInstance
     * @param timeZone
     * @param projectName
     * @param workflowDefinitionName
     */
    private Map<String, String> setBuiltInParamsMap(@NonNull TaskInstance taskInstance,
                                                    WorkflowInstance workflowInstance,
                                                    String timeZone,
                                                    String projectName,
                                                    String workflowDefinitionName) {
        CommandType commandType = workflowInstance.getCmdTypeIfComplement();
        Date scheduleTime = workflowInstance.getScheduleTime();

        Map<String, String> params = BusinessTimeUtils.getBusinessTime(commandType, scheduleTime, timeZone);

        if (StringUtils.isNotBlank(taskInstance.getExecutePath())) {
            params.put(PARAMETER_TASK_EXECUTE_PATH, taskInstance.getExecutePath());
        }
        params.put(PARAMETER_TASK_INSTANCE_ID, Integer.toString(taskInstance.getId()));
        params.put(PARAMETER_TASK_DEFINITION_NAME, taskInstance.getName());
        params.put(PARAMETER_TASK_DEFINITION_CODE, Long.toString(taskInstance.getTaskCode()));
        params.put(PARAMETER_WORKFLOW_INSTANCE_ID, Integer.toString(taskInstance.getWorkflowInstanceId()));
        params.put(PARAMETER_WORKFLOW_DEFINITION_NAME, workflowDefinitionName);
        params.put(PARAMETER_WORKFLOW_DEFINITION_CODE, Long.toString(workflowInstance.getWorkflowDefinitionCode()));
        params.put(PARAMETER_PROJECT_NAME, projectName);
        params.put(PARAMETER_PROJECT_CODE, Long.toString(workflowInstance.getProjectCode()));
        return params;
    }

    private Map<String, Property> parseGlobalParamsMap(WorkflowInstance workflowInstance) {
        final Map<String, Property> globalParametersMaps = new LinkedHashMap<>();
        if (StringUtils.isNotEmpty(workflowInstance.getGlobalParams())) {
            JSONUtils.toList(workflowInstance.getGlobalParams(), Property.class)
                    .forEach(property -> globalParametersMaps.put(property.getProp(), property));
        }
        return globalParametersMaps;
    }

    private List<Property> parseVarPool(TaskInstance taskInstance) {
        if (StringUtils.isNotEmpty(taskInstance.getVarPool())) {
            return VarPoolUtils.deserializeVarPool(taskInstance.getVarPool());
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, Property> preBuildBusinessParams(WorkflowInstance workflowInstance) {
        Map<String, Property> paramsMap = new HashMap<>();
        // replace variable TIME with $[YYYYmmddd...] in shell file when history run job and batch complement job
        if (workflowInstance.getScheduleTime() != null) {
            Date date = workflowInstance.getScheduleTime();
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
