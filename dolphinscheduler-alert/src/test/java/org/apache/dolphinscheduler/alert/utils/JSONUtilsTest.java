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

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.*;

public class JSONUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(JSONUtilsTest.class);

    public List<LinkedHashMap<String, Object>> list = new ArrayList<>();

    public String expected = null;

    @Before
    public void setUp() throws Exception {

        //Define expected json string
        expected = "[{\"mysql service name\":\"mysql200\",\"mysql address\":\"192.168.xx.xx\",\"port\":\"3306\",\"no index of number\":\"80\",\"database client connections\":\"190\"}]";

        //Initial map
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("mysql service name","mysql200");
        map.put("mysql address","192.168.xx.xx");
        map.put("port","3306");
        map.put("no index of number","80");
        map.put("database client connections","190");

        //Add map into list
        list.add(map);
    }


    /**
     * Test toJsonString
     */
    @Test
    public void testToJsonString() {

        //Invoke toJsonString
        String result = JSONUtils.toJsonString(list);
        logger.info(result);

        //Equal result with expected string
        assertEquals(result,expected);

        //If param is null, then return null string
        result = JSONUtils.toJsonString(null);
        logger.info(result);

        assertEquals("null", result);

    }

    /**
     * Test toList
     */
    @Test
    public void testToList() {

        //Invoke toList
        List<LinkedHashMap> result = JSONUtils.toList(expected ,LinkedHashMap.class);
        //Equal list size=1
        assertEquals(1,result.size());

        //Transform entity to LinkedHashMap<String, Object>
        LinkedHashMap<String, Object> entity = result.get(0);

        //Equal expected values
        assertEquals("mysql200",entity.get("mysql service name"));
        assertEquals("192.168.xx.xx", entity.get("mysql address"));
        assertEquals("3306", entity.get("port"));
        assertEquals("80", entity.get("no index of number"));
        assertEquals("190", entity.get("database client connections"));

        //If param is null, then return empty list
        result = JSONUtils.toList(null ,LinkedHashMap.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        //If param is incorrect, then return empty list and log error message
        result = JSONUtils.toList("}{" ,LinkedHashMap.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

}
