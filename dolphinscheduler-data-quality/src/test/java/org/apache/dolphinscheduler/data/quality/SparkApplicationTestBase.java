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

package org.apache.dolphinscheduler.data.quality;

import org.apache.dolphinscheduler.data.quality.config.Config;
import org.apache.dolphinscheduler.data.quality.execution.SparkRuntimeEnvironment;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;

/**
 * SparkApplicationTestBase
 */
public class SparkApplicationTestBase {

    protected SparkRuntimeEnvironment sparkRuntimeEnvironment;

    @BeforeEach
    public void init() {
        Map<String, Object> config = new HashMap<>();
        config.put("spark.app.name", "data quality test");
        config.put("spark.sql.crossJoin.enabled", "true");
        config.put("spark.driver.bindAddress", "127.0.0.1");
        config.put("spark.ui.port", 13000);
        config.put("spark.master", "local[4]");

        sparkRuntimeEnvironment = new SparkRuntimeEnvironment(new Config(config));
    }
}
