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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SensitivePropertyJsonTest {

    @Test
    void masksOnlyTheSensitiveValueField() {
        Property secret = Property.builder().prop("pwd").value("1").sensitive(true).build();
        Property plain = Property.builder().prop("name").value("1").sensitive(false).build();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("localParams", Arrays.asList(secret, plain));
        payload.put("note", "exit 1");

        String masked = SensitivePropertyJson.forLog(payload);

        assertTrue(masked.contains("\"prop\" : \"pwd\""));
        assertTrue(masked.contains("\"value\" : \"******\""));
        assertTrue(masked.contains("\"prop\" : \"name\""));
        assertTrue(masked.contains("\"value\" : \"1\""));
        assertTrue(masked.contains("\"note\" : \"exit 1\""));
        assertFalse(masked.contains("\"prop\" : \"pwd\",\n    \"value\" : \"1\""));
    }
}
