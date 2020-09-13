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

import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.VarPoolUtils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class VarPoolUtilsTest {
    
    private static final Logger logger = LoggerFactory.getLogger(VarPoolUtils.class);
    
    @Test
    public void testSetTaskNodeLocalParams() {
        String taskJson = "{\"conditionResult\":\"{\\\"successNode\\\":[\\\"\\\"],\\\"failedNode\\\":[\\\"\\\"]}\","
            + "\"conditionsTask\":false,\"depList\":[],\"dependence\":\"{}\",\"forbidden\":false,\"id\":\"tasks-75298\",\"maxRetryTimes\":0,\"name\":\"a1\","
            + "\"params\":\"{\\\"rawScript\\\":\\\"print(\\\\\\\"this is python task \\\\\\\",${p0})\\\","
            + "\\\"localParams\\\":[{\\\"prop\\\":\\\"p1\\\",\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"1\\\"}],"
            + "\\\"resourceList\\\":[]}\",\"preTasks\":\"[]\",\"retryInterval\":1,\"runFlag\":\"NORMAL\",\"taskInstancePriority\":\"MEDIUM\","
            + "\"taskTimeoutParameter\":{\"enable\":false,\"interval\":0},\"timeout\":\"{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"}\","
            + "\"type\":\"PYTHON\",\"workerGroup\":\"default\"}";
        TaskNode taskNode = JSONUtils.parseObject(taskJson, TaskNode.class);
        
        VarPoolUtils.setTaskNodeLocalParams(taskNode, "p1", "test1");
        logger.info(JSONUtils.toJsonString(taskNode));
        
        Map<String, Object> propToValue = new ConcurrentHashMap<String, Object>();
        propToValue.put("p1", "test2");
        
        VarPoolUtils.setTaskNodeLocalParams(taskNode, propToValue);
        logger.info(JSONUtils.toJsonString(taskNode));
    }
    
    @Test
    public void testConvertVarPoolToMap() throws Exception {
        String varPool = "p1,66$guyinyou$p2,69$guyinyou$";
        Map<String, Object> propToValue = new ConcurrentHashMap<String, Object>();
        VarPoolUtils.convertVarPoolToMap(propToValue, varPool);
        logger.info(propToValue.toString());
    }
    
    @Test
    public void testConvertPythonScriptPlaceholders() throws Exception {
        String rawScript = "print(${p1});\n${setShareVar(${p1},3)};\n${setShareVar(${p2},4)};";
        rawScript = VarPoolUtils.convertPythonScriptPlaceholders(rawScript);
        logger.info(rawScript);
    }
}
