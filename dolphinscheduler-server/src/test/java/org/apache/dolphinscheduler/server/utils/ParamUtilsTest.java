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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DataType;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.shell.ShellParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Test ParamUtils
 */
public class ParamUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(ParamUtilsTest.class);

    //Define global variables
    public Map<String, Property> globalParams = new HashMap<>();

    public Map<String, String> globalParamsMap = new HashMap<>();

    public Map<String, Property> localParams = new HashMap<>();

    public Map<String, Property> varPoolParams = new HashMap<>();

    /**
     * Init params
     *
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

        Property varProperty = new Property();
        varProperty.setProp("local_param");
        varProperty.setDirect(Direct.IN);
        varProperty.setType(DataType.VARCHAR);
        varProperty.setValue("${global_param}");
        varPoolParams.put("varPool", varProperty);
    }

    /**
     * Test convert
     */
    @Test
    public void testConvert() {
        //The expected value
        String expected = "{\"varPool\":{\"prop\":\"local_param\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"20191229\"},"
                + "\"global_param\":{\"prop\":\"global_param\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"20191229\"},"
                + "\"local_param\":{\"prop\":\"local_param\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"20191229\"}}";
        //The expected value when globalParams is null but localParams is not null
        String expected1 = "{\"varPool\":{\"prop\":\"local_param\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"20191229\"},"
                + "\"global_param\":{\"prop\":\"global_param\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"20191229\"},"
                + "\"local_param\":{\"prop\":\"local_param\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"20191229\"}}";
        //Define expected date , the month is 0-base
        Calendar calendar = Calendar.getInstance();
        calendar.set(2019, 11, 30);
        Date date = calendar.getTime();

        //Invoke convert
        Map<String, Property> paramsMap = ParamUtils.convert(globalParams, globalParamsMap, localParams, varPoolParams,CommandType.START_PROCESS, date);
        String result = JSONUtils.toJsonString(paramsMap);
        assertEquals(expected, result);

        for (Map.Entry<String, Property> entry : paramsMap.entrySet()) {

            String key = entry.getKey();
            Property prop = entry.getValue();
            logger.info(key + " : " + prop.getValue());
        }

        //Invoke convert with null globalParams
        Map<String, Property> paramsMap1 = ParamUtils.convert(null, globalParamsMap, localParams,varPoolParams, CommandType.START_PROCESS, date);
        String result1 = JSONUtils.toJsonString(paramsMap1);
        assertEquals(expected1, result1);

        //Null check, invoke convert with null globalParams and null localParams
        Map<String, Property> paramsMap2 = ParamUtils.convert(null, globalParamsMap, null, varPoolParams,CommandType.START_PROCESS, date);
        assertNull(paramsMap2);
    }

    /**
     * Test some new params related to task
     */
    @Test
    public void testConvertForParamsRelatedTask() throws Exception {
        // start to form some test data for new paramters
        Map<String,Property> globalParams = new HashMap<>();
        Map<String,String> globalParamsMap = new HashMap<>();

        Property taskInstanceIdProperty = new Property();
        String propName = "task_execution_id";
        String paramValue = String.format("${%s}", Constants.PARAMETER_TASK_INSTANCE_ID);
        taskInstanceIdProperty.setProp(propName);
        taskInstanceIdProperty.setDirect(Direct.IN);
        taskInstanceIdProperty.setType(DataType.VARCHAR);
        taskInstanceIdProperty.setValue(paramValue);
        globalParams.put(propName,taskInstanceIdProperty);
        globalParamsMap.put(propName,paramValue);

        Property taskExecutionPathProperty = new Property();
        propName = "task_execution_path";
        paramValue = String.format("${%s}", Constants.PARAMETER_TASK_EXECUTE_PATH);
        taskExecutionPathProperty.setProp(propName);
        taskExecutionPathProperty.setDirect(Direct.IN);
        taskExecutionPathProperty.setType(DataType.VARCHAR);
        taskExecutionPathProperty.setValue(paramValue);

        globalParams.put(propName,taskExecutionPathProperty);
        globalParamsMap.put(propName,paramValue);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2019,11,30);
        Date date = calendar.getTime();

        List<Property> globalParamList = globalParams.values().stream().collect(Collectors.toList());

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(1);
        taskExecutionContext.setTaskName("params test");
        taskExecutionContext.setTaskType(TaskType.SHELL.getDesc());
        taskExecutionContext.setHost("127.0.0.1:1234");
        taskExecutionContext.setExecutePath("/tmp/test");
        taskExecutionContext.setLogPath("/log");
        taskExecutionContext.setProcessInstanceId(1);
        taskExecutionContext.setExecutorId(1);
        taskExecutionContext.setCmdTypeIfComplement(0);
        taskExecutionContext.setScheduleTime(date);
        taskExecutionContext.setGlobalParams(JSONUtils.toJsonString(globalParamList));
        taskExecutionContext.setDefinedParams(globalParamsMap);
        taskExecutionContext.setTaskParams(
                "{\"rawScript\":\"#!/bin/sh\\necho $[yyyy-MM-dd HH:mm:ss]\\necho \\\" ${task_execution_id} \\\"\\necho \\\" ${task_execution_path}\\\"\\n\","
                        + "\"localParams\":"
                        + "[{\"prop\":\"task_execution_id\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"${system.task.instance.id}\"},"
                        + "{\"prop\":\"task_execution_path\",\"direct\":\"IN\",\"type\":\"VARCHAR"
                        + "\",\"value\":\"${system.task.execute.path}\"}],\"resourceList\":[]}");

        ShellParameters shellParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ShellParameters.class);

        //The expected value
        String expected = "{\"task_execution_id\":{\"prop\":\"task_execution_id\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"1\"},"
                + "\"task_execution_path\":{\"prop\":\"task_execution_path\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"/tmp/test\"}}";

        //The expected value when globalParams is null but localParams is not null
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext, shellParameters);

        String result = JSONUtils.toJsonString(paramsMap);

        Map<String,String> resultMap = JSONUtils.parseObject(result,Map.class);
        Map<String,String> expectedMap = JSONUtils.parseObject(expected,Map.class);

        result = JSONUtils.toJsonString(resultMap,SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        expected = JSONUtils.toJsonString(expectedMap,SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

        assertEquals(expected, result);

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
        String result = JSONUtils.toJsonString(paramsMap);
        assertEquals(expected, result);

        logger.info(result);

        //Null check
        Map<String, String> paramsMap1 = ParamUtils.convert(null);
        assertNull(paramsMap1);
    }
}