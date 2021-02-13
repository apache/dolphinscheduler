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

package org.apache.dolphinscheduler.spi.params;

import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * plugin params pojo and json transfer tool
 */
public class PluginParamsTransfer {

    public static String transferParamsToJson(List<PluginParams> list) {
        return JSONUtils.toJsonString(list);
    }

    public static List<PluginParams> transferJsonToParamsList(String str) {
        return JSONUtils.toList(str, PluginParams.class);
    }

    /**
     * return the plugin params map
     */
    public static Map<String, Object> getPluginParamsMap(String paramsJsonStr) {
        List<PluginParams> pluginParams = transferJsonToParamsList(paramsJsonStr);
        Map<String, Object> paramsMap = new HashMap<>();
        for (PluginParams param : pluginParams) {
            paramsMap.put(param.getName(), param.getValue());
        }
        return paramsMap;
    }

    public static List<Map<String, Object>> generatePluginParams(String paramsJsonStr, String pluginParamsTemplate) {
        Map<String, Object> paramsMap = JSONUtils.toMap(paramsJsonStr);
        return generatePluginParams(paramsMap, pluginParamsTemplate);
    }

    public static List<Map<String, Object>> generatePluginParams(Map<String, Object> paramsMap, String pluginParamsTemplate) {
        if (paramsMap == null || paramsMap.isEmpty()) {
            return null;
        }
        List<Map<Object, Object>> pluginParamsList = JSONUtils.toMapList(pluginParamsTemplate);
        pluginParamsList.forEach(pluginParams -> pluginParams.put("value", paramsMap.get(pluginParams.get("field"))));
        return pluginParamsList;
    }
}
