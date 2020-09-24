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
package org.apache.dolphinscheduler.plugin.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class PropertyUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(PropertyUtilsTest.class);

    /**
     * Test getString
     */
    @Test
    public void testGetString() {

        String result = PropertyUtils.getString("test.string");
        logger.info(result);
        assertEquals("teststring", result);

        //If key is null, then return null
        result = PropertyUtils.getString(null);
        assertNull(result);
    }


    /**
     * Test getBoolean
     */
    @Test
    public void testGetBoolean() {

        //Expected true
        Boolean result = PropertyUtils.getBoolean("test.true");
        assertTrue(result);

        //Expected false
        result = PropertyUtils.getBoolean("test.false");
        assertFalse(result);
    }

    /**
     * Test getLong
     */
    @Test
    public void testGetLong() {
        long result = PropertyUtils.getLong("test.long");
        assertSame(100L, result);
    }

    /**
     * Test getDouble
     */
    @Test
    public void testGetDouble() {

        //If key is undefine in alert.properties, and there is a defaultval, then return defaultval
        double result = PropertyUtils.getDouble("abc", 5.0);
        assertEquals(5.0, result, 0);

        result = PropertyUtils.getDouble("cba", 5.0);
        assertEquals(3.1, result, 0.01);
    }

}