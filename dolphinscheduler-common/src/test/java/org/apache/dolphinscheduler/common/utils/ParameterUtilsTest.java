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

import static org.apache.dolphinscheduler.common.utils.placeholder.TimePlaceholderUtils.replacePlaceholders;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DataType;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.utils.placeholder.PlaceholderUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterUtilsTest {
    public static final Logger logger = LoggerFactory.getLogger(ParameterUtilsTest.class);

    /**
     * Test convertParameterPlaceholders
     */
    @Test
    public void testConvertParameterPlaceholders() throws ParseException {
        // parameterString,parameterMap is null
        Assert.assertNull(ParameterUtils.convertParameterPlaceholders(null, null));

        // parameterString is null,parameterMap is not null
        Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("testParameter", "testParameter");
        Assert.assertNull(ParameterUtils.convertParameterPlaceholders(null, parameterMap));

        // parameterString、parameterMap is not null
        String parameterString = "test_parameter";
        Assert.assertEquals(parameterString, ParameterUtils.convertParameterPlaceholders(parameterString, parameterMap));

        //replace variable ${} form
        parameterMap.put("testParameter2", "${testParameter}");
        Assert.assertEquals(parameterString, PlaceholderUtils.replacePlaceholders(parameterString, parameterMap, true));

        // replace time $[...] form, eg. $[yyyyMMdd]
        Date cronTime = new Date();
        Assert.assertEquals(parameterString, replacePlaceholders(parameterString, cronTime, true));

        // replace time $[...] form, eg. $[yyyyMMdd]
        Date cronTimeStr = DateUtils.stringToDate("2019-02-02 00:00:00");
        Assert.assertEquals(parameterString, replacePlaceholders(parameterString, cronTimeStr, true));
    }

    @Test
    public void testConvertParameterPlaceholders2() {
        String parameterString =
            "${user} is userName, '$[1]' '$[add_months(yyyyMMdd,12*2)]' '$[add_months(yyyyMMdd,-12*2)]' '$[add_months(yyyyMMdd,3)]' '$[add_months(yyyyMMdd,-4)]' "
                + "'$[yyyyMMdd+7*2]' '$[yyyyMMdd-7*2]'  '$[yyyyMMdd+3]'  '$[0]' '$[yyyyMMdd-3]' '$[HHmmss+2/24]' '$[HHmmss-1/24]' '$[HHmmss+3/24/60]' '$[HHmmss-2/24/60]'  '$[3]'";
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("user", "Kris");
        parameterMap.put(Constants.PARAMETER_DATETIME, "20201201123000");
        parameterString = ParameterUtils.convertParameterPlaceholders(parameterString, parameterMap);
        Assert.assertEquals("Kris is userName, '$[1]' '20221201' '20181201' '20210301' '20200801' '20201215' '20201117'  '20201204'  '$[0]' '20201128' '143000' '113000' '123300' '122800'  '$[3]'",
            parameterString);
    }

    /**
     * Test curingGlobalParams
     */
    @Test
    public void testCuringGlobalParams() {
        //define globalMap
        Map<String, String> globalParamMap = new HashMap<>();
        globalParamMap.put("globalParams1", "Params1");

        //define globalParamList
        List<Property> globalParamList = new ArrayList<>();

        //define scheduleTime
        Date scheduleTime = DateUtils.stringToDate("2019-12-20 00:00:00");

        //test globalParamList is null
        String result = ParameterUtils.curingGlobalParams(globalParamMap, globalParamList, CommandType.START_CURRENT_TASK_PROCESS, scheduleTime);
        Assert.assertNull(result);
        Assert.assertNull(ParameterUtils.curingGlobalParams(null, null, CommandType.START_CURRENT_TASK_PROCESS, null));
        Assert.assertNull(ParameterUtils.curingGlobalParams(globalParamMap, null, CommandType.START_CURRENT_TASK_PROCESS, scheduleTime));

        //test globalParamList is not null
        Property property = new Property("testGlobalParam", Direct.IN, DataType.VARCHAR, "testGlobalParam");
        globalParamList.add(property);

        String result2 = ParameterUtils.curingGlobalParams(null, globalParamList, CommandType.START_CURRENT_TASK_PROCESS, scheduleTime);
        Assert.assertEquals(result2, JSONUtils.toJsonString(globalParamList));

        String result3 = ParameterUtils.curingGlobalParams(globalParamMap, globalParamList, CommandType.START_CURRENT_TASK_PROCESS, null);
        Assert.assertEquals(result3, JSONUtils.toJsonString(globalParamList));

        String result4 = ParameterUtils.curingGlobalParams(globalParamMap, globalParamList, CommandType.START_CURRENT_TASK_PROCESS, scheduleTime);
        Assert.assertEquals(result4, JSONUtils.toJsonString(globalParamList));

        //test var $ startsWith
        globalParamMap.put("bizDate", "${system.biz.date}");
        globalParamMap.put("b1zCurdate", "${system.biz.curdate}");

        Property property2 = new Property("testParamList1", Direct.IN, DataType.VARCHAR, "testParamList");
        Property property3 = new Property("testParamList2", Direct.IN, DataType.VARCHAR, "{testParamList1}");
        Property property4 = new Property("testParamList3", Direct.IN, DataType.VARCHAR, "${b1zCurdate}");

        globalParamList.add(property2);
        globalParamList.add(property3);
        globalParamList.add(property4);

        String result5 = ParameterUtils.curingGlobalParams(globalParamMap, globalParamList, CommandType.START_CURRENT_TASK_PROCESS, scheduleTime);
        Assert.assertEquals(result5, JSONUtils.toJsonString(globalParamList));
    }

    /**
     * Test handleEscapes
     */
    @Test
    public void testHandleEscapes() throws Exception {
        Assert.assertNull(ParameterUtils.handleEscapes(null));
        Assert.assertEquals("", ParameterUtils.handleEscapes(""));
        Assert.assertEquals("test Parameter", ParameterUtils.handleEscapes("test Parameter"));
        Assert.assertEquals("////%test////%Parameter", ParameterUtils.handleEscapes("%test%Parameter"));
    }

}
