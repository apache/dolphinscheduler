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

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class SensitivePropertyCryptoUtilsTest {

    @Test
    void decodeSensitiveValuesDecryptsWhenEnabled() {
        try (MockedStatic<PropertyUtils> mocked = Mockito.mockStatic(PropertyUtils.class)) {
            mocked.when(() -> PropertyUtils.getBoolean(DataSourceConstants.DATASOURCE_ENCRYPTION_ENABLE, false))
                    .thenReturn(true);
            mocked.when(() -> PropertyUtils.getString(DataSourceConstants.DATASOURCE_ENCRYPTION_SALT,
                    DataSourceConstants.DATASOURCE_ENCRYPTION_SALT_DEFAULT))
                    .thenReturn(DataSourceConstants.DATASOURCE_ENCRYPTION_SALT_DEFAULT);

            String ciphertext = PasswordUtils.encodePassword("Secret123");
            List<Property> decoded = SensitivePropertyCryptoUtils.decodeSensitiveValues(
                    Collections.singletonList(Property.builder()
                            .prop("pwd")
                            .direct(Direct.IN)
                            .type(DataType.VARCHAR)
                            .value(ciphertext)
                            .sensitive(true)
                            .build()));
            Assertions.assertEquals("Secret123", decoded.get(0).getValue());
        }
    }

    @Test
    void decodeLocalParamsInTaskParams() {
        try (MockedStatic<PropertyUtils> mocked = Mockito.mockStatic(PropertyUtils.class)) {
            mocked.when(() -> PropertyUtils.getBoolean(DataSourceConstants.DATASOURCE_ENCRYPTION_ENABLE, false))
                    .thenReturn(true);
            mocked.when(() -> PropertyUtils.getString(DataSourceConstants.DATASOURCE_ENCRYPTION_SALT,
                    DataSourceConstants.DATASOURCE_ENCRYPTION_SALT_DEFAULT))
                    .thenReturn(DataSourceConstants.DATASOURCE_ENCRYPTION_SALT_DEFAULT);

            String ciphertext = PasswordUtils.encodePassword("token-value");
            String taskParams = "{\"localParams\":[{\"prop\":\"token\",\"direct\":\"IN\",\"type\":\"VARCHAR\","
                    + "\"value\":\"" + ciphertext + "\",\"sensitive\":true}]}";
            String decoded = SensitivePropertyCryptoUtils.decodeLocalParamsInTaskParams(taskParams);
            Assertions.assertTrue(decoded.contains("token-value"));
            Assertions.assertFalse(decoded.contains(ciphertext));
        }
    }
}
