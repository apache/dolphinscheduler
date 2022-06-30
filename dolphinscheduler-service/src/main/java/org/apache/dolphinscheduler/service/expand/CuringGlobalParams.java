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

import com.google.common.base.Preconditions;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_EXECUTE_PATH;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_INSTANCE_ID;

@Component
public class CuringGlobalParams implements CuringParamsService {

    private static final Logger logger = LoggerFactory.getLogger(CuringGlobalParams.class);

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
    public String curingGlobalParams(Integer processInstanceId, Map<String, String> globalParamMap, List<Property> globalParamList, CommandType commandType, Date scheduleTime, String timezone) {
        if (globalParamList == null || globalParamList.isEmpty()) {
            return null;
        }
        Map<String, String> globalMap = new HashMap<>();
        if (globalParamMap != null) {
            globalMap.putAll(globalParamMap);
        }
        Map<String, String> allParamMap = new HashMap<>();
        //If it is a complement, a complement time needs to be passed in, according to the task type
        Map<String, String> timeParams = BusinessTimeUtils.
                getBusinessTime(commandType, scheduleTime, timezone);

        if (timeParams != null) {
            allParamMap.putAll(timeParams);
        }
        allParamMap.putAll(globalMap);
        Set<Map.Entry<String, String>> entries = allParamMap.entrySet();
        Map<String, String> resolveMap = new HashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            String val = entry.getValue();
            if (val.startsWith(Constants.FUNCTION_START_WITH)) {
                String str = "";
                // whether external scaling calculation is required
                if (timeFunctionNeedExpand(val)) {
                    str = timeFunctionExtension(processInstanceId, timezone, val);
                } else {
                    str = convertParameterPlaceholders(val, allParamMap);
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

    /**
     * the global parameters and local parameters used in the worker will be prepared here.
     *
     * @param taskExecutionContext
     * @param parameters
     * @param processInstance
     * @return
     */
    @Override
    public Map<String, Property> paramParsingPreparation(TaskExecutionContext taskExecutionContext, AbstractParameters parameters, ProcessInstance processInstance) {
        Preconditions.checkNotNull(taskExecutionContext);
        Preconditions.checkNotNull(parameters);
        setGlobalParamsMap(taskExecutionContext);
        Map<String, Property> globalParams = ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams());
        Map<String,String> globalParamsMap = taskExecutionContext.getDefinedParams();
        CommandType commandType = CommandType.of(taskExecutionContext.getCmdTypeIfComplement());
        Date scheduleTime = taskExecutionContext.getScheduleTime();

        // combining local and global parameters
        Map<String, Property> localParams = parameters.getInputLocalParametersMap();

        //stream pass params
        Map<String, Property> varParams = parameters.getVarPoolMap();

        if (globalParams.size() == 0 && localParams.size() == 0 && varParams.size() == 0) {
            return null;
        }
        // if it is a complement,
        // you need to pass in the task instance id to locate the time
        // of the process instance complement
        Map<String, String> cmdParam = JSONUtils.toMap(processInstance.getCommandParam());
        String timeZone = cmdParam.get(Constants.SCHEDULE_TIMEZONE);
        Map<String,String> params = BusinessTimeUtils.getBusinessTime(commandType, scheduleTime, timeZone);

        if (globalParamsMap != null) {
            params.putAll(globalParamsMap);
        }

        if (StringUtils.isNotBlank(taskExecutionContext.getExecutePath())) {
            params.put(PARAMETER_TASK_EXECUTE_PATH, taskExecutionContext.getExecutePath());
        }
        params.put(PARAMETER_TASK_INSTANCE_ID, Integer.toString(taskExecutionContext.getTaskInstanceId()));

        if (varParams.size() != 0) {
            globalParams.putAll(varParams);
        }
        if (localParams.size() != 0) {
            globalParams.putAll(localParams);
        }

        Iterator<Map.Entry<String, Property>> iter = globalParams.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Property> en = iter.next();
            Property property = en.getValue();

            if (StringUtils.isNotEmpty(property.getValue()) && property.getValue().startsWith(Constants.FUNCTION_START_WITH)) {
                /**
                 *  local parameter refers to global parameter with the same name
                 *  note: the global parameters of the process instance here are solidified parameters,
                 *  and there are no variables in them.
                 */
                String val = property.getValue();
                // whether external scaling calculation is required
                if (timeFunctionNeedExpand(val)) {
                    val = timeFunctionExtension(taskExecutionContext.getProcessInstanceId(), timeZone, val);
                } else {
                    val  = convertParameterPlaceholders(val, params);
                }
                property.setValue(val);
            }
        }
        if (MapUtils.isEmpty(globalParams)) {
            globalParams = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
            globalParams.putAll(taskExecutionContext.getParamsMap());
        }
        return globalParams;
    }

    private void setGlobalParamsMap(TaskExecutionContext taskExecutionContext) {
        Map<String, String> globalParamsMap = new HashMap<>(16);

        // global params string
        String globalParamsStr = taskExecutionContext.getGlobalParams();
        if (globalParamsStr != null) {
            List<Property> globalParamsList = JSONUtils.toList(globalParamsStr, Property.class);
            globalParamsMap.putAll(globalParamsList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue)));
        }
        taskExecutionContext.setDefinedParams(globalParamsMap);
    }
}
