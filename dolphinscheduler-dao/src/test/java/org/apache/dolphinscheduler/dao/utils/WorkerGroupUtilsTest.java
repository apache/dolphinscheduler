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

class WorkerGroupUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "default"})
    void testIsWorkerGroupEmpty_emptyWorkerGroup(String workerGroup) {
        assertThat(WorkerGroupUtils.isWorkerGroupEmpty(workerGroup)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "default1"})
    void testIsWorkerGroupEmpty_nonEmptyWorkerGroup(String workerGroup) {
        assertThat(WorkerGroupUtils.isWorkerGroupEmpty(workerGroup)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "default"})
    void testGetWorkerGroupOrDefault_emptyWorkerGroup(String workerGroup) {
        assertThat(WorkerGroupUtils.getWorkerGroupOrDefault(workerGroup))
                .isEqualTo(WorkerGroupUtils.getDefaultWorkerGroup());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test"})
    void testGetWorkerGroupOrDefault_nonEmptyWorkerGroup(String workerGroup) {
        assertThat(WorkerGroupUtils.getWorkerGroupOrDefault(workerGroup)).isEqualTo(workerGroup);
    }

    @ParameterizedTest
    @CsvSource(value = {",test", "default,test"})
    void testGetWorkerGroupOrDefault_withDefaultValue_emptyWorkerGroup(String workerGroup, String defaultValue) {
        assertThat(WorkerGroupUtils.getWorkerGroupOrDefault(workerGroup, defaultValue)).isEqualTo(defaultValue);
    }

    @ParameterizedTest
    @CsvSource(value = {"test1,test"})
    void testGetWorkerGroupOrDefault_withDefaultValue_nonEmptyWorkerGroup(String workerGroup, String defaultValue) {
        assertThat(WorkerGroupUtils.getWorkerGroupOrDefault(workerGroup)).isEqualTo(workerGroup);
    }

    @Test
    void getDefaultWorkerGroup() {
        assertThat(WorkerGroupUtils.getDefaultWorkerGroup()).isEqualTo("default");
    }
}
