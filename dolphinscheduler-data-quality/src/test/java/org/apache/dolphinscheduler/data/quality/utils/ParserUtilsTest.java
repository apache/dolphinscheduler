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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserUtilsTest {

    @Test
    public void testParserUtils() {
        String testStr = "aaa$bbb$ccc%ddd^eee#fff";
        String encode = ParserUtils.encode(testStr);
        String decode = ParserUtils.decode(encode);
        Assertions.assertEquals(testStr, decode);

        String blank = "";
        Assertions.assertEquals(ParserUtils.encode(blank), blank);
        Assertions.assertEquals(ParserUtils.decode(blank), blank);

        Assertions.assertNull(ParserUtils.encode(null));
        Assertions.assertNull(ParserUtils.decode(null));
    }
}
