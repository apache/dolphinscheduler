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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * property utils
 */
public class PropertyUtils {

    private PropertyUtils() {
        throw new IllegalStateException("PropertyUtils class");
    }

    /**
     * startParams transform propertyList
     *
     * @param startParams startParams
     * @return startParamList
     */
    public static List<Property> startParamsTransformPropertyList(String startParams) {
        List<Property> startParamList = null;
        if (startParams != null) {
            JsonElement jsonElement = JsonParser.parseString(startParams);
            boolean isJson = jsonElement.isJsonObject();
            if (isJson) {
                Map<String, String> startParamMap = JSONUtils.toMap(startParams);
                startParamList = startParamMap.entrySet().stream()
                        .map(entry -> new Property(entry.getKey(), Direct.IN, DataType.VARCHAR, entry.getValue()))
                        .collect(Collectors.toList());
            } else {
                startParamList = JSONUtils.toList(startParams, Property.class);
            }
        }
        return startParamList;
    }

}
