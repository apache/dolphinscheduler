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

import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * param utils
 */
public class ParamUtils {

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
        Map<String, Property> userDefParamsMaps = new HashMap<>();
        if (definedParams != null) {
            Iterator<Map.Entry<String, String>> iter = definedParams.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> en = iter.next();
                Property property = new Property(en.getKey(), Direct.IN, DataType.VARCHAR, en.getValue());
                userDefParamsMaps.put(property.getProp(),property);
            }
        }
        return userDefParamsMaps;
    }
}