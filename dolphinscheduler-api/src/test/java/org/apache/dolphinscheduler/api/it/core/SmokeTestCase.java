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

package org.apache.dolphinscheduler.api.it.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class SmokeTestCase extends ApiIntegrationTestBase {

    /**
     * Smoke test does not depend on business base fixtures so it stays green
     * even when those fixtures are still being introduced (T9).
     */
    @Override
    protected List<String> baseFixtures() {
        return Collections.emptyList();
    }

    @Test
    void shouldStartContextAndHaveCleanDatabase() {
        // If we got here without exception, SpringBoot startup + embedded ZK + cleanAll all work.
        assertThat(databaseCleaner).isNotNull();
        assertThat(yamlDataLoader).isNotNull();
        assertThat(mockMvc).isNotNull();
    }
}
