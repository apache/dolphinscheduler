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

package org.apache.dolphinscheduler.dao.utils;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class EnvironmentUtilsTest {

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void testIsEnvironmentCodeEmpty_emptyEnvironmentCode(Long environmentCode) {
        assertThat(EnvironmentUtils.isEnvironmentCodeEmpty(environmentCode)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(longs = {123})
    void testIsEnvironmentCodeEmpty_nonEmptyEnvironmentCode(Long environmentCode) {
        assertThat(EnvironmentUtils.isEnvironmentCodeEmpty(environmentCode)).isFalse();
    }

    @Test
    void testGetDefaultEnvironmentCode() {
        assertThat(EnvironmentUtils.getDefaultEnvironmentCode()).isEqualTo(-1L);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void testGetEnvironmentCodeOrDefault_emptyEnvironmentCode(Long environmentCode) {
        assertThat(EnvironmentUtils.getEnvironmentCodeOrDefault(environmentCode)).isEqualTo(-1L);
    }

    @ParameterizedTest
    @ValueSource(longs = {123})
    void testGetEnvironmentCodeOrDefault_nonEmptyEnvironmentCode(Long environmentCode) {
        assertThat(EnvironmentUtils.getEnvironmentCodeOrDefault(environmentCode)).isEqualTo(environmentCode);
    }

    @ParameterizedTest
    @CsvSource(value = {",123", "-1,123"})
    void testGetEnvironmentCodeOrDefault_withDefaultValue_emptyEnvironmentCode(Long environmentCode,
                                                                               Long defaultValue) {
        assertThat(EnvironmentUtils.getEnvironmentCodeOrDefault(environmentCode, defaultValue)).isEqualTo(defaultValue);
    }

    @ParameterizedTest
    @CsvSource(value = {"1,123"})
    void testGetEnvironmentCodeOrDefault_withDefaultValue_nonEmptyEnvironmentCode(Long environmentCode,
                                                                                  Long defaultValue) {
        assertThat(EnvironmentUtils.getEnvironmentCodeOrDefault(environmentCode, defaultValue))
                .isEqualTo(environmentCode);
    }
}
