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

package org.apache.dolphinscheduler.alert.plugin;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * alpacajs json and PluginParams java bean transfer
 */
public class PluginParamsTransfer {

    /**
     * transfe plugin params to form-create json
     * @param pluginParamsList
     *   pluginParamsList
     * @return
     */
    public static String getParamsJson(List<PluginParams> pluginParamsList) {
        return JSONUtils.toJsonString(pluginParamsList);
    }

    /**
     * transfe plugin params json string to PluginParams
     * @param paramsJson
     * @return
     */
    public static List<PluginParams> getPluginParams(String paramsJson) {
        return JSONUtils.toList(paramsJson, PluginParams.class);
    }

    /**
     * get the name and value of the plugin params
     * @param paramsJson
     * @return
     */
    public static Map<String, String> getPluginParamsMap(String paramsJson) {
        List<PluginParams> pluginParams = JSONUtils.toList(paramsJson, PluginParams.class);
        Map<String, String> paramsMap = new HashMap<>();
        for(PluginParams param : pluginParams) {
            paramsMap.put(param.getName(), param.getValue().toString());
        }

        return paramsMap;
    }

}
