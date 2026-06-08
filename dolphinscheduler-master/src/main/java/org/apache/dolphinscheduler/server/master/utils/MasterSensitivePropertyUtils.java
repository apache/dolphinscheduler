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

package org.apache.dolphinscheduler.server.master.utils;

import static org.apache.dolphinscheduler.common.constants.Constants.LOCAL_PARAMS;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertySensitiveUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@UtilityClass
public class MasterSensitivePropertyUtils {

    public String decryptGlobalParams(String globalParams) {
        List<Property> properties = GlobalParameterUtils.deserializeGlobalParameter(globalParams);
        if (CollectionUtils.isEmpty(properties)) {
            return globalParams;
        }
        return GlobalParameterUtils.serializeGlobalParameter(decryptSensitiveValues(properties));
    }

    public String decryptLocalParamsInTaskParams(String taskParams) {
        if (StringUtils.isEmpty(taskParams)) {
            return taskParams;
        }
        ObjectNode taskParamsNode = JSONUtils.parseObject(taskParams);
        JsonNode localParamsNode = taskParamsNode.findValue(LOCAL_PARAMS);
        if (localParamsNode == null || localParamsNode.isNull()) {
            return taskParams;
        }
        List<Property> localParams = JSONUtils.toList(localParamsNode.toString(), Property.class);
        taskParamsNode.set(LOCAL_PARAMS, JSONUtils.toJsonNode(decryptSensitiveValues(localParams)));
        return JSONUtils.toJsonString(taskParamsNode);
    }

    public Map<String, Property> decryptPrepareParams(Map<String, Property> prepareParams) {
        if (prepareParams == null || prepareParams.isEmpty()) {
            return prepareParams;
        }
        prepareParams.replaceAll((key, property) -> decryptSensitiveValue(property));
        return prepareParams;
    }

    public List<Property> decryptSensitiveValues(Collection<Property> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return Collections.emptyList();
        }
        return properties.stream()
                .map(MasterSensitivePropertyUtils::decryptSensitiveValue)
                .collect(Collectors.toList());
    }

    private Property decryptSensitiveValue(Property property) {
        Property decryptedProperty = PropertySensitiveUtils.copy(property);
        if (!PropertySensitiveUtils.isSensitive(decryptedProperty)
                || StringUtils.isEmpty(decryptedProperty.getValue())
                || PropertySensitiveUtils.isSensitiveValuePlaceholder(decryptedProperty.getValue())) {
            return decryptedProperty;
        }
        decryptedProperty.setValue(PasswordUtils.decodePassword(decryptedProperty.getValue()));
        return decryptedProperty;
    }
}
