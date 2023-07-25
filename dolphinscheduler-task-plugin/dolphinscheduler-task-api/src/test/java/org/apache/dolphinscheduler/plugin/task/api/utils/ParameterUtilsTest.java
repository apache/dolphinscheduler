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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import static org.apache.dolphinscheduler.plugin.task.api.parser.TimePlaceholderUtils.replacePlaceholders;

import org.apache.dolphinscheduler.common.constants.DateConstants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parser.PlaceholderUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

public class ParameterUtilsTest {

    @Test
    public void expandListParameter() {
        Map<Integer, Property> params = new HashMap<>();
        params.put(1,
                new Property(null, null, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList("c1", "c2", "c3"))));
        params.put(2, new Property(null, null, DataType.DATE, "2020-06-30"));
        params.put(3, new Property(null, null, DataType.LIST,
                JSONUtils.toJsonString(Lists.newArrayList(3.1415, 2.44, 3.44))));
        String sql = ParameterUtils.expandListParameter(params,
                "select * from test where col1 in (?) and date=? and col2 in (?)");
        Assertions.assertEquals("select * from test where col1 in (?,?,?) and date=? and col2 in (?,?,?)", sql);
        Assertions.assertEquals(7, params.size());

        Map<Integer, Property> params2 = new HashMap<>();
        params2.put(1, new Property(null, null, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList("c1"))));
        params2.put(2, new Property(null, null, DataType.DATE, "2020-06-30"));
        String sql2 = ParameterUtils.expandListParameter(params2, "select * from test where col1 in (?) and date=?");
        Assertions.assertEquals("select * from test where col1 in (?) and date=?", sql2);
        Assertions.assertEquals(2, params2.size());

    }

    @Test
    public void testConvertParameterPlaceholders() throws ParseException {
        // parameterString,parameterMap is null
        Assertions.assertNull(ParameterUtils.convertParameterPlaceholders(null, null));

        // parameterString is null,parameterMap is not null
        Map<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("testParameter", "testParameter");
        Assertions.assertNull(ParameterUtils.convertParameterPlaceholders(null, parameterMap));

        // parameterString、parameterMap is not null
        String parameterString = "test_parameter";
        Assertions.assertEquals(parameterString,
                ParameterUtils.convertParameterPlaceholders(parameterString, parameterMap));

        // replace variable ${} form
        parameterMap.put("testParameter2", "${testParameter}");
        Assertions.assertEquals(parameterString,
                PlaceholderUtils.replacePlaceholders(parameterString, parameterMap, true));

        // replace time $[...] form, eg. $[yyyyMMdd]
        Date cronTime = new Date();
        Assertions.assertEquals(parameterString, replacePlaceholders(parameterString, cronTime, true));

        // replace time $[...] form, eg. $[yyyyMMdd]
        Date cronTimeStr = DateUtils.stringToDate("2019-02-02 00:00:00");
        Assertions.assertEquals(parameterString, replacePlaceholders(parameterString, cronTimeStr, true));
    }

    @Test
    public void testConvertParameterPlaceholders2() {
        String parameterString =
                "${user} is userName, '$[1]' '$[add_months(yyyyMMdd,12*2)]' '$[add_months(yyyyMMdd,-12*2)]' '$[add_months(yyyyMMdd,3)]' '$[add_months(yyyyMMdd,-4)]' "
                        + "'$[yyyyMMdd+7*2]' '$[yyyyMMdd-7*2]'  '$[yyyyMMdd+3]'  '$[0]' '$[yyyyMMdd-3]' '$[HHmmss+2/24]' '$[HHmmss-1/24]' '$[HHmmss+3/24/60]' '$[HHmmss-2/24/60]'  '$[3]'";
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("user", "Kris");
        parameterMap.put(DateConstants.PARAMETER_DATETIME, "20201201123000");
        parameterString = ParameterUtils.convertParameterPlaceholders(parameterString, parameterMap);
        Assertions.assertEquals(
                "Kris is userName, '$[1]' '20221201' '20181201' '20210301' '20200801' '20201215' '20201117'  '20201204'  '$[0]' '20201128' '143000' '113000' '123300' '122800'  '$[3]'",
                parameterString);
    }

    /**
     * Test handleEscapes
     */
    @Test
    public void testHandleEscapes() throws Exception {
        Assertions.assertNull(ParameterUtils.handleEscapes(null));
        Assertions.assertEquals("", ParameterUtils.handleEscapes(""));
        Assertions.assertEquals("test Parameter", ParameterUtils.handleEscapes("test Parameter"));
        Assertions.assertEquals("////%test////%Parameter", ParameterUtils.handleEscapes("%test%Parameter"));
    }
}
