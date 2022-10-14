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

package org.apache.dolphinscheduler.spi.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {

    @Test
    public void testIsEmpty() {
        Assertions.assertTrue(StringUtils.isEmpty(""));
    }

    @Test
    public void testIsEmpty2() {
        Assertions.assertFalse(StringUtils.isEmpty("123"));
    }

    @Test
    public void testIsNotEmpty() {
        Assertions.assertTrue(StringUtils.isNotEmpty("cs"));
    }

    @Test
    public void testIsNotEmpty2() {
        Assertions.assertFalse(StringUtils.isNotEmpty(""));
    }

    @Test
    public void testIsBlank() {
        Assertions.assertTrue(StringUtils.isBlank(""));
    }

    @Test
    public void testIsBlank2() {
        Assertions.assertFalse(StringUtils.isBlank("123"));
    }

    @Test
    public void testIsNotBlank() {
        Assertions.assertTrue(StringUtils.isNotBlank("cs"));
    }

    @Test
    public void testIsNotBlank2() {
        Assertions.assertFalse(StringUtils.isNotBlank(""));
    }

    @Test
    public void testTrim() {
        Assertions.assertEquals("result", StringUtils.trim(" result "));
    }

    @Test
    public void testTrim2() {
        Assertions.assertEquals("", StringUtils.trim(""));
    }

    @Test
    public void testEqualsIgnoreCase() {
        Assertions.assertTrue(StringUtils.equalsIgnoreCase("Str1", "str1"));
    }

    @Test
    public void testEqualsIgnoreCase2() {
        Assertions.assertFalse(StringUtils.equalsIgnoreCase("str1", null));
    }

    @Test
    public void testJoin1() {
        // Setup
        List<String> collection = new ArrayList<>();
        collection.add("1");
        collection.add("2");

        // Run the test
        final String result = StringUtils.join(collection, "_");

        // Verify the results
        Assertions.assertEquals("1_2", result);
    }

    @Test
    public void testEscapeJava() {
        Assertions.assertEquals("str", StringUtils.escapeJava("str"));
    }

    @Test
    public void testReplaceDoubleBrackets() {
        Assertions.assertEquals("{ {a} }", StringUtils.replaceDoubleBrackets("{{a}}"));
    }
}
