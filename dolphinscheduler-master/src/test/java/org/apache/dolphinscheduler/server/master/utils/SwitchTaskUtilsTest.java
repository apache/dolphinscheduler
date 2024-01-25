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

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SwitchTaskUtilsTest {

    @Test
    public void testGenerateContentWithTaskParams() {
        String content = "${test}==1";
        Map<String, Property> globalParams = new HashMap<>();
        Map<String, Property> varParams = new HashMap<>();
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            SwitchTaskUtils.generateContentWithTaskParams(content, globalParams, varParams);
        });

        globalParams.put("test", new Property("test", Direct.IN, DataType.INTEGER, "1"));
        String result = SwitchTaskUtils.generateContentWithTaskParams(content, globalParams, varParams);
        Assertions.assertEquals("1==1", result);
    }

    @Test
    public void testIllegalCondition() {
        String content = "java.lang.Runtime.getRuntime().exec(\"bash /tmp/shell\")";
        Map<String, Property> globalParams = new HashMap<>();
        Map<String, Property> varParams = new HashMap<>();
        globalParams.put("test", new Property("test", Direct.IN, DataType.INTEGER, "1"));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            SwitchTaskUtils.generateContentWithTaskParams(content, globalParams, varParams);
        });
    }
}
