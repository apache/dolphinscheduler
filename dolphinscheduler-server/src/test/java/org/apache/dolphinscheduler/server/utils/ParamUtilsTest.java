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

package org.apache.dolphinscheduler.server.utils;

import com.alibaba.fastjson.JSON;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DataType;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.process.Property;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test ParamUtils
 */
public class ParamUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(ParamUtilsTest.class);

    //Define global variables
    public Map<String, Property> globalParams = new HashMap<>();

    public Map<String, String> globalParamsMap = new HashMap<>();

    public Map<String, Property> localParams = new HashMap<>();

    /**
     * Init params
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

        Property property = new Property();
        property.setProp("global_param");
        property.setDirect(Direct.IN);
        property.setType(DataType.VARCHAR);
        property.setValue("${system.biz.date}");
        globalParams.put("global_param", property);

        globalParamsMap.put("global_param", "${system.biz.date}");

        Property localProperty = new Property();
        localProperty.setProp("local_param");
        localProperty.setDirect(Direct.IN);
        localProperty.setType(DataType.VARCHAR);
        localProperty.setValue("${global_param}");
        localParams.put("local_param", localProperty);
    }

    /**
     * Test convert
     */
    @Test
    public void testConvert() {

        //The expected value
        String expected = "{\"global_param\":{\"direct\":\"IN\",\"prop\":\"global_param\",\"type\":\"VARCHAR\",\"value\":\"20191229\"},\"local_param\":{\"direct\":\"IN\",\"prop\":\"local_param\",\"type\":\"VARCHAR\",\"value\":\"20191229\"}}";

        //The expected value when globalParams is null but localParams is not null
        String expected1 = "{\"local_param\":{\"direct\":\"IN\",\"prop\":\"local_param\",\"type\":\"VARCHAR\",\"value\":\"20191229\"}}";

        //Define expected date , the month is 0-base
        Calendar calendar = Calendar.getInstance();
        calendar.set(2019,11,30);
        Date date = calendar.getTime();

        //Invoke convert
        Map<String, Property> paramsMap = ParamUtils.convert(globalParams, globalParamsMap, localParams, CommandType.START_PROCESS, date);
        String result = JSON.toJSONString(paramsMap);
        assertEquals(expected, result);

        for (Map.Entry<String, Property> entry : paramsMap.entrySet()) {

            String key = entry.getKey();
            Property prop = entry.getValue();
            logger.info(key + " : " + prop.getValue());
        }

        //Invoke convert with null globalParams
        Map<String, Property> paramsMap1 = ParamUtils.convert(null, globalParamsMap, localParams, CommandType.START_PROCESS, date);
        String result1 = JSON.toJSONString(paramsMap1);
        assertEquals(expected1, result1);

        //Null check, invoke convert with null globalParams and null localParams
        Map<String, Property> paramsMap2 = ParamUtils.convert(null, globalParamsMap, null, CommandType.START_PROCESS, date);
        assertNull(paramsMap2);
    }

    /**
     * Test the overload method of convert
     */
    @Test
    public void testConvert1() {

        //The expected value
        String expected = "{\"global_param\":\"${system.biz.date}\"}";

        //Invoke convert
        Map<String, String> paramsMap = ParamUtils.convert(globalParams);
        String result = JSON.toJSONString(paramsMap);
        assertEquals(expected, result);

        logger.info(result);

        //Null check
        Map<String, String> paramsMap1 = ParamUtils.convert(null);
        assertNull(paramsMap1);
    }
}