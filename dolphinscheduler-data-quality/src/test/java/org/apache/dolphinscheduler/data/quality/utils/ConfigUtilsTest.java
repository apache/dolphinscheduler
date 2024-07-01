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

package org.apache.dolphinscheduler.data.quality.utils;

import org.apache.dolphinscheduler.data.quality.config.Config;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigUtilsTest {

    @Test
    public void testExtractSubConfig() {
        // Setup
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("aaa.www", "1");
        configMap.put("bbb.www", "1");

        final Config source = new Config(configMap);

        // Run the test
        final Config result = ConfigUtils.extractSubConfig(source, "aaa", false);
        int expect = 1;
        int actual = result.entrySet().size();

        Assertions.assertEquals(expect, actual);
    }
}
