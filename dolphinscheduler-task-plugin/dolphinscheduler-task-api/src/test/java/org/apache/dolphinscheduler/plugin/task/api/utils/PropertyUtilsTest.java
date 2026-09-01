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
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PropertyUtilsTest {

    @Test
    void mapFormatLosesSensitiveFlag() {
        List<Property> startParams =
                PropertyUtils.startParamsTransformPropertyList("{\"pwd\":\"" + TaskConstants.SENSITIVE_DATA_MASK
                        + "\"}");

        Assertions.assertEquals(1, startParams.size());
        Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK, startParams.get(0).getValue());
        Assertions.assertFalse(startParams.get(0).isSensitive());
    }

    @Test
    void listFormatKeepsSensitiveFlag() {
        Property submitted = Property.builder()
                .prop("pwd")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value(TaskConstants.SENSITIVE_DATA_MASK)
                .sensitive(true)
                .build();
        String json = JSONUtils.toJsonString(Collections.singletonList(submitted));

        List<Property> startParams = PropertyUtils.startParamsTransformPropertyList(json);

        Assertions.assertEquals(1, startParams.size());
        Assertions.assertTrue(startParams.get(0).isSensitive());
        Assertions.assertEquals(TaskConstants.SENSITIVE_DATA_MASK, startParams.get(0).getValue());
    }

    @Test
    void mapFormatMaskDoesNotOverwriteSensitiveGlobalParam() {
        List<Property> startParams =
                PropertyUtils.startParamsTransformPropertyList("{\"pwd\":\"" + TaskConstants.SENSITIVE_DATA_MASK
                        + "\"}");
        Property global = Property.builder()
                .prop("pwd")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("Secret123")
                .sensitive(true)
                .build();

        List<Property> merged = PropertySensitiveUtils.mergeStartParamsWithGlobalParams(
                startParams, Collections.singletonList(global));

        Assertions.assertEquals("Secret123", merged.get(0).getValue());
        Assertions.assertTrue(merged.get(0).isSensitive());
    }

    @Test
    void listFormatMaskDoesNotOverwriteSensitiveGlobalParam() {
        Property submitted = Property.builder()
                .prop("pwd")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value(TaskConstants.SENSITIVE_DATA_MASK)
                .sensitive(true)
                .build();
        Property global = Property.builder()
                .prop("pwd")
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value("Secret123")
                .sensitive(true)
                .build();

        List<Property> startParams =
                PropertyUtils.startParamsTransformPropertyList(JSONUtils.toJsonString(Collections.singletonList(
                        submitted)));
        List<Property> merged = PropertySensitiveUtils.mergeStartParamsWithGlobalParams(
                startParams, Collections.singletonList(global));

        Assertions.assertEquals("Secret123", merged.get(0).getValue());
        Assertions.assertTrue(merged.get(0).isSensitive());
    }
}
