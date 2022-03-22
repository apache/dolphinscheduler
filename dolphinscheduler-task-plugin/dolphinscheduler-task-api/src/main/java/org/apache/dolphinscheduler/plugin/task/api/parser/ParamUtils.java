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

package org.apache.dolphinscheduler.plugin.task.api.parser;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_EXECUTE_PATH;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.PARAMETER_TASK_INSTANCE_ID;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.enums.CommandType;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * param utils
 */
public class ParamUtils {

    /**
     * parameter conversion
     * Warning:
     *  When you first invoke the function of convert, the variables of localParams and varPool in the ShellParameters will be modified.
     *  But in the whole system the variables of localParams and varPool have been used in other functions. I'm not sure if this current
     *  situation is wrong. So I cannot modify the original logic.
     *
     * @param taskExecutionContext the context of this task instance
     * @param parameters the parameters
     * @return global params
     *
     */
    public static Map<String, Property> convert(TaskExecutionContext taskExecutionContext, AbstractParameters parameters) {
        Preconditions.checkNotNull(taskExecutionContext);
        Preconditions.checkNotNull(parameters);
        Map<String, Property> globalParams = getUserDefParamsMap(taskExecutionContext.getDefinedParams());
        Map<String,String> globalParamsMap = taskExecutionContext.getDefinedParams();
        CommandType commandType = CommandType.of(taskExecutionContext.getCmdTypeIfComplement());
        Date scheduleTime = taskExecutionContext.getScheduleTime();

        // combining local and global parameters
        Map<String, Property> localParams = parameters.getLocalParametersMap();

        Map<String, Property> varParams = parameters.getVarPoolMap();

        if (globalParams == null && localParams == null) {
            return null;
        }
        // if it is a complement,
        // you need to pass in the task instance id to locate the time
        // of the process instance complement
        Map<String,String> params = BusinessTimeUtils
                .getBusinessTime(commandType,
                        scheduleTime);

        if (globalParamsMap != null) {

            params.putAll(globalParamsMap);
        }

        if (StringUtils.isNotBlank(taskExecutionContext.getExecutePath())) {
            params.put(PARAMETER_TASK_EXECUTE_PATH, taskExecutionContext.getExecutePath());
        }
        params.put(PARAMETER_TASK_INSTANCE_ID, Integer.toString(taskExecutionContext.getTaskInstanceId()));

        if (globalParams != null && localParams != null) {
            globalParams.putAll(localParams);
        } else if (globalParams == null && localParams != null) {
            globalParams = localParams;
        }
        if (varParams != null) {
            varParams.putAll(globalParams);
            globalParams = varParams;
        }
        Iterator<Map.Entry<String, Property>> iter = globalParams.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Property> en = iter.next();
            Property property = en.getValue();

            if (StringUtils.isNotEmpty(property.getValue())
                    && property.getValue().startsWith("$")) {
                /**
                 *  local parameter refers to global parameter with the same name
                 *  note: the global parameters of the process instance here are solidified parameters,
                 *  and there are no variables in them.
                 */
                String val = property.getValue();

                val  = ParameterUtils.convertParameterPlaceholders(val, params);
                property.setValue(val);
            }
        }

        return globalParams;
    }

    /**
     * format convert
     *
     * @param paramsMap params map
     * @return Map of converted
     */
    public static Map<String,String> convert(Map<String,Property> paramsMap) {
        if (paramsMap == null) {
            return null;
        }

        Map<String, String> map = new HashMap<>();
        Iterator<Map.Entry<String, Property>> iter = paramsMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Property> en = iter.next();
            map.put(en.getKey(), en.getValue().getValue());
        }
        return map;
    }

    /**
     * get parameters map
     *
     * @param definedParams definedParams
     * @return parameters map
     */
    public static Map<String, Property> getUserDefParamsMap(Map<String, String> definedParams) {
        if (definedParams != null) {
            Map<String, Property> userDefParamsMaps = new HashMap<>();
            Iterator<Map.Entry<String, String>> iter = definedParams.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> en = iter.next();
                Property property = new Property(en.getKey(), Direct.IN, DataType.VARCHAR, en.getValue());
                userDefParamsMaps.put(property.getProp(),property);
            }
            return userDefParamsMaps;
        }
        return null;
    }
}