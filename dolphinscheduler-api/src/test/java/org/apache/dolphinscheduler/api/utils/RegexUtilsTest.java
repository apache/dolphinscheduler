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

package org.apache.dolphinscheduler.api.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * RegexUtils test case
 */
public class RegexUtilsTest {

    @Test
    public void testIsValidLinuxUserName() {
        String name1 = "10000";
        Assertions.assertTrue(RegexUtils.isValidLinuxUserName(name1));

        String name2 = "00hayden";
        Assertions.assertTrue(RegexUtils.isValidLinuxUserName(name2));

        String name3 = "hayde123456789123456789123456789";
        Assertions.assertFalse(RegexUtils.isValidLinuxUserName(name3));

        String name4 = "hayd123456789123456789123456789";
        Assertions.assertTrue(RegexUtils.isValidLinuxUserName(name4));

        String name5 = "h";
        Assertions.assertTrue(RegexUtils.isValidLinuxUserName(name5));

        String name6 = "hayden";
        Assertions.assertTrue(RegexUtils.isValidLinuxUserName(name6));

        String name7 = "00hayden_0";
        Assertions.assertTrue(RegexUtils.isValidLinuxUserName(name2));

        String name8 = "00hayden.8";
        Assertions.assertTrue(RegexUtils.isValidLinuxUserName(name2));
    }

    @Test
    public void testEscapeNRT() {
        String result1 = RegexUtils.escapeNRT("abc\n");
        Assertions.assertEquals("abc_", result1);

        String result2 = RegexUtils.escapeNRT("abc\r");
        Assertions.assertEquals("abc_", result2);

        String result3 = RegexUtils.escapeNRT("abc\t");
        Assertions.assertEquals("abc_", result3);

        String result4 = RegexUtils.escapeNRT(null);
        Assertions.assertNull(result4);
    }

}
