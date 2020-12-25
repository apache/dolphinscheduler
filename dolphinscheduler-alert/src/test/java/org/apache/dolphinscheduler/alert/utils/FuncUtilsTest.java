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

package org.apache.dolphinscheduler.alert.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FuncUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(FuncUtilsTest.class);

    /**
     * Test mkString
     */
    @Test
    public void testMKString() {

        //Define users list
        Iterable<String> users = Arrays.asList("user1", "user2", "user3");
        //Define split
        String split = "|";

        //Invoke mkString with correctParams
        String result = FuncUtils.mkString(users, split);
        logger.info(result);

        //Expected result string
        assertEquals("user1|user2|user3", result);

        //Null list expected return null
        result = FuncUtils.mkString(null, split);
        assertNull(result);

        //Null split expected return null
        result = FuncUtils.mkString(users, null);
        assertNull(result);

    }
}