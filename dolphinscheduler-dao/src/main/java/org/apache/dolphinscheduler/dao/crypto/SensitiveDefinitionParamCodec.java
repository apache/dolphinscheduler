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

package org.apache.dolphinscheduler.dao.crypto;

import static org.apache.dolphinscheduler.plugin.datasource.api.constants.DataSourceConstants.DATASOURCE_ENCRYPTION_ENABLE;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertySensitiveUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Encode / decode {@code sensitive=true} items in definition JSON.
 * Reuses {@link PasswordUtils} and honors {@code datasource.encryption.enable}.
 */
@UtilityClass
public class SensitiveDefinitionParamCodec {

    private static final String LOCAL_PARAMS = "localParams";

    public String encryptGlobalParams(String globalParams) {
        return rewriteGlobalParams(globalParams, SensitiveDefinitionParamCodec::encodeValue);
    }

    public String decryptGlobalParams(String globalParams) {
        return rewriteGlobalParams(globalParams, PasswordUtils::decodePassword);
    }

    public String encryptTaskParams(String taskParams) {
        return rewriteLocalParams(taskParams, SensitiveDefinitionParamCodec::encodeValue);
    }

    public String decryptTaskParams(String taskParams) {
        return rewriteLocalParams(taskParams, PasswordUtils::decodePassword);
    }

    public boolean isEncryptionEnabled() {
        return PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false);
    }

    private String rewriteGlobalParams(String globalParams, UnaryOperator<String> transformer) {
        if (StringUtils.isEmpty(globalParams) || !isEncryptionEnabled()) {
            return globalParams;
        }
        List<Property> properties = GlobalParameterUtils.deserializeGlobalParameter(globalParams);
        if (CollectionUtils.isEmpty(properties)) {
            return globalParams;
        }
        return GlobalParameterUtils.serializeGlobalParameter(transform(properties, transformer));
    }

    private String rewriteLocalParams(String taskParams, UnaryOperator<String> transformer) {
        if (StringUtils.isEmpty(taskParams) || !isEncryptionEnabled()) {
            return taskParams;
        }
        ObjectNode taskParamsNode = JSONUtils.parseObject(taskParams);
        if (taskParamsNode == null) {
            return taskParams;
        }
        JsonNode localParamsNode = taskParamsNode.get(LOCAL_PARAMS);
        if (localParamsNode == null || localParamsNode.isNull()) {
            return taskParams;
        }
        List<Property> localParams = JSONUtils.toList(localParamsNode.toString(), Property.class);
        taskParamsNode.set(LOCAL_PARAMS, JSONUtils.toJsonNode(transform(localParams, transformer)));
        return JSONUtils.toJsonString(taskParamsNode);
    }

    private List<Property> transform(List<Property> properties, UnaryOperator<String> transformer) {
        if (CollectionUtils.isEmpty(properties)) {
            return Collections.emptyList();
        }
        return properties.stream()
                .map(property -> transformOne(property, transformer))
                .collect(Collectors.toList());
    }

    private Property transformOne(Property property, UnaryOperator<String> transformer) {
        Property copied = PropertySensitiveUtils.copy(property);
        if (!PropertySensitiveUtils.isSensitive(copied) || StringUtils.isEmpty(copied.getValue())) {
            return copied;
        }
        if (PropertySensitiveUtils.isSensitiveValuePlaceholder(copied.getValue())) {
            return copied;
        }
        copied.setValue(transformer.apply(copied.getValue()));
        return copied;
    }

    /**
     * Encode plaintext only. {@link PasswordUtils#encodePassword(String)} is not idempotent.
     */
    private String encodeValue(String value) {
        if (isCiphertext(value)) {
            return value;
        }
        return PasswordUtils.encodePassword(value);
    }

    private boolean isCiphertext(String value) {
        return !value.equals(PasswordUtils.decodePassword(value));
    }
}
