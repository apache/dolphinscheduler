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

import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

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
        assertEquals(result, "EMAIL");

        //Expected "xxx.xxx.test"
        result = PropertyUtils.getString("mail.server.host");
        assertEquals(result, "xxx.xxx.test");

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
        assertSame(result, 25L);

        //If key is null, then return -1
        result = PropertyUtils.getLong(null);
        assertSame(result, -1L);

        //If key is undefine in alert.properties, then return -1
        result = PropertyUtils.getLong("abc");
        assertSame(result, -1L);

        //If key is undefine in alert.properties, and there is a defaultval, then return defaultval
        result = PropertyUtils.getLong("abc", 200);
        assertEquals(result, 200L);

        //If the value can not parse to long ,it will log the error and return -1L
        result = PropertyUtils.getLong("test.server.testnumber");
        assertSame(result, -1L);
    }

    /**
     * Test getDouble
     */
    @Test
    public void testGetDouble() {

        //Expected 3.0
        double result = PropertyUtils.getDouble("test.server.factor");
        assertEquals(result, 3.0, 0);

        //If key is null, then return -1.0
        result = PropertyUtils.getDouble(null);
        assertEquals(result, -1.0, 0);

        //If key is undefine in alert.properties, then return -1
        result = PropertyUtils.getDouble("abc");
        assertEquals(result, -1.0, 0);

        //If key is undefine in alert.properties, and there is a defaultval, then return defaultval
        result = PropertyUtils.getDouble("abc", 5.0);
        assertEquals(result, 5.0, 0);

        //If the value can not parse to double ,it will log the error and return -1.0
        result = PropertyUtils.getDouble("test.server.testnumber");
        assertEquals(result, -1.0, 0);
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
        assertEquals(result[0], "xxx.xxx.test1");
        assertEquals(result[1], "xxx.xxx.test2");
        assertEquals(result[2], "xxx.xxx.test3");

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
        assertSame(result, 25);

        //If key is null, then return -1
        result = PropertyUtils.getInt(null);
        assertSame(result, -1);

        //If key is undefine in alert.properties, then return -1
        result = PropertyUtils.getInt("abc");
        assertSame(result, -1);

        //If key is undefine in alert.properties, and there is a defaultval, then return defaultval
        result = PropertyUtils.getInt("abc", 300);
        assertEquals(result, 300);

        //If the value can not parse to int ,it will log the error and return -1
        result = PropertyUtils.getInt("test.server.testnumber");
        assertSame(result, -1);
    }

    /**
     * Test getEnum
     */
    @Test
    public void testGetEnum() {

        //Expected MASTER
        ZKNodeType zkNodeType = PropertyUtils.getEnum("test.server.enum1", ZKNodeType.class,ZKNodeType.WORKER);
        assertEquals(zkNodeType, ZKNodeType.MASTER);

        //Expected DEAD_SERVER
        zkNodeType = PropertyUtils.getEnum("test.server.enum2", ZKNodeType.class,ZKNodeType.WORKER);
        assertEquals(zkNodeType, ZKNodeType.DEAD_SERVER);

        //If key is null, then return defaultval
        zkNodeType = PropertyUtils.getEnum(null, ZKNodeType.class,ZKNodeType.WORKER);
        assertEquals(zkNodeType, ZKNodeType.WORKER);

        //If the value doesn't define in enum ,it will log the error and return -1
        zkNodeType = PropertyUtils.getEnum("test.server.enum3", ZKNodeType.class,ZKNodeType.WORKER);
        assertEquals(zkNodeType, ZKNodeType.WORKER);
    }

}