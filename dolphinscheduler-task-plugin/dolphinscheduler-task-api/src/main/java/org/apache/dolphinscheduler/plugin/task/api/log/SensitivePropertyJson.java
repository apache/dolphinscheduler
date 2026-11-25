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

package org.apache.dolphinscheduler.plugin.task.api.log;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Pretty-prints an object for a log line and masks {@code value} only on objects whose
 * {@code sensitive} field is true. Other text, including the same characters, is unchanged.
 */
public final class SensitivePropertyJson {

    private SensitivePropertyJson() {
    }

    public static String forLog(final Object value) {
        String json = JSONUtils.toJsonString(value);
        if (StringUtils.isEmpty(json)) {
            return json;
        }
        JsonNode root = json.trim().startsWith("[") ? JSONUtils.parseArray(json) : JSONUtils.parseObject(json);
        if (root == null) {
            return json;
        }
        maskSensitiveValues(root);
        return JSONUtils.toPrettyJsonString(root);
    }

    private static void maskSensitiveValues(final JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            JsonNode sensitive = objectNode.get("sensitive");
            JsonNode propertyValue = objectNode.get("value");
            if (sensitive != null && sensitive.isBoolean() && sensitive.booleanValue()
                    && propertyValue != null && propertyValue.isTextual()
                    && StringUtils.isNotEmpty(propertyValue.asText())
                    && !TaskConstants.SENSITIVE_DATA_MASK.equals(propertyValue.asText())) {
                objectNode.set("value", TextNode.valueOf(TaskConstants.SENSITIVE_DATA_MASK));
            }
            objectNode.fields().forEachRemaining(entry -> maskSensitiveValues(entry.getValue()));
        } else if (node.isArray()) {
            node.forEach(SensitivePropertyJson::maskSensitiveValues);
        }
    }
}
