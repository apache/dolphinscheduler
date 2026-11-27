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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SensitivePlaceholderMaskTest {

    @Test
    void masksSensitivePlaceholderOfAnyLengthWithoutTouchingSameText() {
        Map<String, Property> params = new HashMap<>();
        params.put("pwd", sensitive("pwd", "1"));
        params.put("name", plain("name", "task"));

        String template = "echo ${pwd}\necho 1\necho ${name}\nexit 1";

        assertEquals("echo ******\necho 1\necho task\nexit 1", SensitivePlaceholderMask.forLog(template, params));
    }

    @Test
    void keepsPlaceholderWhenValueIsTheMaskItself() {
        Map<String, Property> params = new HashMap<>();
        params.put("pwd", sensitive("pwd", "******"));

        assertEquals("echo ******", SensitivePlaceholderMask.forLog("echo ${pwd}", params));
    }

    private static Property sensitive(String prop, String value) {
        return Property.builder().prop(prop).value(value).sensitive(true).build();
    }

    private static Property plain(String prop, String value) {
        return Property.builder().prop(prop).value(value).sensitive(false).build();
    }
}
