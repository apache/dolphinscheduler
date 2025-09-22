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

package org.apache.dolphinscheduler.task.executor.utils;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class CommonUtilsTest {

    @BeforeEach
    public void setUp() {
        // Setup code if needed
    }

    @Test
    public void isDevelopMode_DevelopmentStateTrue_ReturnsTrue() {
        try (MockedStatic<PropertyUtils> mockedPropertyUtils = Mockito.mockStatic(PropertyUtils.class)) {
            mockedPropertyUtils.when(() -> PropertyUtils.getBoolean(Constants.DEVELOPMENT_STATE, true))
                    .thenReturn(true);
            Assertions.assertTrue(CommonUtils.isDevelopMode());
        }
    }

    @Test
    public void isDevelopMode_DevelopmentStateFalse_ReturnsFalse() {
        try (MockedStatic<PropertyUtils> mockedPropertyUtils = Mockito.mockStatic(PropertyUtils.class)) {
            mockedPropertyUtils.when(() -> PropertyUtils.getBoolean(Constants.DEVELOPMENT_STATE, true))
                    .thenReturn(false);
            Assertions.assertFalse(CommonUtils.isDevelopMode());
        }
    }
}
