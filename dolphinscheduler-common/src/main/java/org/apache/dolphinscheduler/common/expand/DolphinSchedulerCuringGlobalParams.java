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

package org.apache.dolphinscheduler.common.expand;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DolphinSchedulerCuringGlobalParams implements CuringGlobalParamsService {

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
            if (val.startsWith("$")) {
                String str = "";
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
}
