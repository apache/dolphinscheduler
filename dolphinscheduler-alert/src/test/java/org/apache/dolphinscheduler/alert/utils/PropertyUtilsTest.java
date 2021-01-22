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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.dolphinscheduler.common.enums.ZKNodeType;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test PropertyUtils
 * and the resource path is src/test/resources/alert.properties.
 */
public class PropertyUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(PropertyUtilsTest.class);

    /**
     * Test getString
     */
    @Test
    public void testGetString() {

        //Expected "EMAIL"
        String result = PropertyUtils.getString("alert.type");
        logger.info(result);
        assertEquals("EMAIL", result);

        //Expected "xxx.xxx.test"
        result = PropertyUtils.getString("mail.server.host");
        assertEquals("xxx.xxx.test", result);

        //If key is undefine in alert.properties, then return null
        result = PropertyUtils.getString("abc");
        assertNull(result);

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
        Boolean result = PropertyUtils.getBoolean("mail.smtp.starttls.enable");
        assertTrue(result);

        //Expected false
        result = PropertyUtils.getBoolean("mail.smtp.ssl.enable");
        assertFalse(result);

        //If key is undefine in alert.properties, then return null
        result = PropertyUtils.getBoolean("abc");
        assertFalse(result);

        //If key is null, then return false
        result = PropertyUtils.getBoolean(null);
        assertFalse(result);
    }

    /**
     * Test getLong
     */
    @Test
    public void testGetLong() {

        //Expected 25
        long result = PropertyUtils.getLong("mail.server.port");
        assertSame(25L, result);

        //If key is null, then return -1
        result = PropertyUtils.getLong(null);
        assertSame(-1L, result);

        //If key is undefine in alert.properties, then return -1
        result = PropertyUtils.getLong("abc");
        assertSame(-1L, result);

        //If key is undefine in alert.properties, and there is a defaultval, then return defaultval
        result = PropertyUtils.getLong("abc", 200);
        assertEquals(200L, result);

        //If the value can not parse to long ,it will log the error and return -1L
        result = PropertyUtils.getLong("test.server.testnumber");
        assertSame(-1L, result);
    }

    /**
     * Test getDouble
     */
    @Test
    public void testGetDouble() {

        //Expected 3.0
        double result = PropertyUtils.getDouble("test.server.factor");
        assertEquals(3.0, result, 0);

        //If key is null, then return -1.0
        result = PropertyUtils.getDouble(null);
        assertEquals(-1.0, result, 0);

        //If key is undefine in alert.properties, then return -1
        result = PropertyUtils.getDouble("abc");
        assertEquals(-1.0, result, 0);

        //If key is undefine in alert.properties, and there is a defaultval, then return defaultval
        result = PropertyUtils.getDouble("abc", 5.0);
        assertEquals(5.0, result, 0);

        //If the value can not parse to double ,it will log the error and return -1.0
        result = PropertyUtils.getDouble("test.server.testnumber");
        assertEquals(-1.0, result, 0);
    }

    /**
     * Test getArray
     */
    @Test
    public void testGetArray() {

        //Expected length 3
        String[] result = PropertyUtils.getArray("test.server.list", ",");
        assertEquals(result.length, 3);

        //Equal array values
        assertEquals("xxx.xxx.test1", result[0]);
        assertEquals("xxx.xxx.test2", result[1]);
        assertEquals("xxx.xxx.test3", result[2]);

        //If key is null, then return -1
        result = PropertyUtils.getArray(null, ",");
        assertNull(result);

        //If key is undefine in alert.properties, then return null
        result = PropertyUtils.getArray("abc", ",");
        assertNull(result);

        //If splitStr is null, then return null
        result = PropertyUtils.getArray("test.server.list", null);
        assertNull(result);
    }

    /**
     * test getInt
     */
    @Test
    public void testGetInt() {

        //Expected 25
        int result = PropertyUtils.getInt("mail.server.port");
        assertSame(25, result);

        //If key is null, then return -1
        result = PropertyUtils.getInt(null);
        assertSame(-1, result);

        //If key is undefine in alert.properties, then return -1
        result = PropertyUtils.getInt("abc");
        assertSame(-1, result);

        //If key is undefine in alert.properties, and there is a defaultval, then return defaultval
        result = PropertyUtils.getInt("abc", 300);
        assertEquals(300, result);

        //If the value can not parse to int ,it will log the error and return -1
        result = PropertyUtils.getInt("test.server.testnumber");
        assertSame(-1, result);
    }

    /**
     * Test getEnum
     */
    @Test
    public void testGetEnum() {

        //Expected MASTER
        ZKNodeType zkNodeType = PropertyUtils.getEnum("test.server.enum1", ZKNodeType.class, ZKNodeType.WORKER);
        assertEquals(ZKNodeType.MASTER, zkNodeType);

        //Expected DEAD_SERVER
        zkNodeType = PropertyUtils.getEnum("test.server.enum2", ZKNodeType.class, ZKNodeType.WORKER);
        assertEquals(ZKNodeType.DEAD_SERVER, zkNodeType);

        //If key is null, then return defaultval
        zkNodeType = PropertyUtils.getEnum(null, ZKNodeType.class, ZKNodeType.WORKER);
        assertEquals(ZKNodeType.WORKER, zkNodeType);

        //If the value doesn't define in enum ,it will log the error and return -1
        zkNodeType = PropertyUtils.getEnum("test.server.enum3", ZKNodeType.class, ZKNodeType.WORKER);
        assertEquals(ZKNodeType.WORKER, zkNodeType);
    }

}
