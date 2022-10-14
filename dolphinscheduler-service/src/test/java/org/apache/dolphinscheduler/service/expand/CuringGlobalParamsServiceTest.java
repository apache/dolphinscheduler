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


package org.apache.dolphinscheduler.service.expand;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class CuringGlobalParamsServiceTest {

    private static final String placeHolderName = "$[yyyy-MM-dd-1]";

    @Mock
    private CuringParamsService curingGlobalParamsService;

    @InjectMocks
    private CuringGlobalParams dolphinSchedulerCuringGlobalParams;

    @Mock
    private TimePlaceholderResolverExpandService timePlaceholderResolverExpandService;

    @InjectMocks
    private TimePlaceholderResolverExpandServiceImpl timePlaceholderResolverExpandServiceImpl;

    private final Map<String, String> globalParamMap = new HashMap<>();

    @BeforeEach
    public void init() {
        globalParamMap.put("globalParams1", "Params1");
    }

    @Test
    public void testConvertParameterPlaceholders() {
        Mockito.when(curingGlobalParamsService.convertParameterPlaceholders(placeHolderName, globalParamMap)).thenReturn("2022-06-26");
        String result = curingGlobalParamsService.convertParameterPlaceholders(placeHolderName, globalParamMap);
        Assertions.assertNotNull(result);
    }

    @Test
    public void testTimeFunctionNeedExpand() {
        boolean result = curingGlobalParamsService.timeFunctionNeedExpand(placeHolderName);
        Assertions.assertFalse(result);
    }

    @Test
    public void testTimeFunctionExtension() {
        String result = curingGlobalParamsService.timeFunctionExtension(1, "", placeHolderName);
        Assertions.assertNull(result);
    }

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
        String result = dolphinSchedulerCuringGlobalParams.curingGlobalParams(1, globalParamMap, globalParamList, CommandType.START_CURRENT_TASK_PROCESS, scheduleTime, null);
        Assertions.assertNull(result);
        Assertions.assertNull(dolphinSchedulerCuringGlobalParams.curingGlobalParams(1, null, null, CommandType.START_CURRENT_TASK_PROCESS, null, null));
        Assertions.assertNull(dolphinSchedulerCuringGlobalParams.curingGlobalParams(1, globalParamMap, null, CommandType.START_CURRENT_TASK_PROCESS, scheduleTime, null));

        //test globalParamList is not null
        Property property = new Property("testGlobalParam", Direct.IN, DataType.VARCHAR, "testGlobalParam");
        globalParamList.add(property);

        String result2 = dolphinSchedulerCuringGlobalParams.curingGlobalParams(1, null, globalParamList, CommandType.START_CURRENT_TASK_PROCESS, scheduleTime, null);
        Assertions.assertEquals(result2, JSONUtils.toJsonString(globalParamList));

        String result3 = dolphinSchedulerCuringGlobalParams.curingGlobalParams(1, globalParamMap, globalParamList, CommandType.START_CURRENT_TASK_PROCESS, null, null);
        Assertions.assertEquals(result3, JSONUtils.toJsonString(globalParamList));

        String result4 = dolphinSchedulerCuringGlobalParams.curingGlobalParams(1, globalParamMap, globalParamList, CommandType.START_CURRENT_TASK_PROCESS, scheduleTime, null);
        Assertions.assertEquals(result4, JSONUtils.toJsonString(globalParamList));

        //test var $ startsWith
        globalParamMap.put("bizDate", "${system.biz.date}");
        globalParamMap.put("b1zCurdate", "${system.biz.curdate}");

        Property property2 = new Property("testParamList1", Direct.IN, DataType.VARCHAR, "testParamList");
        Property property3 = new Property("testParamList2", Direct.IN, DataType.VARCHAR, "{testParamList1}");
        Property property4 = new Property("testParamList3", Direct.IN, DataType.VARCHAR, "${b1zCurdate}");

        globalParamList.add(property2);
        globalParamList.add(property3);
        globalParamList.add(property4);

        String result5 = dolphinSchedulerCuringGlobalParams.curingGlobalParams(1, globalParamMap, globalParamList, CommandType.START_CURRENT_TASK_PROCESS, scheduleTime, null);
        Assertions.assertEquals(result5, JSONUtils.toJsonString(globalParamList));

        Property testStartParamProperty = new Property("testStartParam", Direct.IN, DataType.VARCHAR, "");
        globalParamList.add(testStartParamProperty);
        Property testStartParam2Property = new Property("testStartParam2", Direct.IN, DataType.VARCHAR, "$[yyyy-MM-dd+1]");
        globalParamList.add(testStartParam2Property);
        globalParamMap.put("testStartParam", "");
        globalParamMap.put("testStartParam2", "$[yyyy-MM-dd+1]");

        Map<String, String> startParamMap = new HashMap<>(2);
        startParamMap.put("testStartParam", "$[yyyyMMdd]");

        for (Map.Entry<String, String> param : globalParamMap.entrySet()) {
            String val = startParamMap.get(param.getKey());
            if (val != null) {
                param.setValue(val);
            }
        }

        String result6 = dolphinSchedulerCuringGlobalParams.curingGlobalParams(1, globalParamMap, globalParamList, CommandType.START_CURRENT_TASK_PROCESS, scheduleTime, null);
        Assertions.assertTrue(result6.contains("20191220"));
    }
}
