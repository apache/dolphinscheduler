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

package org.apache.dolphinscheduler.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

class LogUtilsTest {

    @Test
    void testRollViewLogLinesWithMultibyteCharacters() {
        String line = StringUtils.repeat("中", 22000);

        assertEquals(line + "\r\n", LogUtils.rollViewLogLines(Collections.singletonList(line)));
    }

    @Test
    void testRollViewLogLinesTruncatesLongLines() {
        for (String character : Arrays.asList("a", "中")) {
            String line = StringUtils.repeat(character, 65536);
            String expected = StringUtils.repeat(character, 65535)
                    + " [this line's size 65536 characters exceeds 65535 characters, so only "
                    + "65535 characters are reserved for performance reasons.]\r\n";

            assertEquals(expected, LogUtils.rollViewLogLines(Collections.singletonList(line)));
        }
    }

    @Test
    void testRollViewLogLinesStopsAtCharacterLimit() {
        String line = StringUtils.repeat("中", 32768);

        assertEquals(line + "\r\n" + line + "\r\n",
                LogUtils.rollViewLogLines(Arrays.asList(line, line, "omitted")));
    }
}
