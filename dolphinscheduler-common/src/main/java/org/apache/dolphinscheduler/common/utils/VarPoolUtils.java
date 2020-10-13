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

import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.TaskParams;

import java.text.ParseException;
import java.util.Map;

public class VarPoolUtils {
    /**
     * getTaskNodeLocalParam
     * @param taskNode taskNode
     * @param prop prop
     * @return localParamForProp
     */
    public static Object getTaskNodeLocalParam(TaskNode taskNode, String prop) {
        String taskParamsJson = taskNode.getParams();
        TaskParams taskParams = JSONUtils.parseObject(taskParamsJson, TaskParams.class);
        if (taskParams == null) {
            return null;
        }
        return taskParams.getLocalParamValue(prop);
    }
    
    /**
     * setTaskNodeLocalParams
     * @param taskNode taskNode
     * @param prop LocalParamName
     * @param value LocalParamValue
     */
    public static void setTaskNodeLocalParams(TaskNode taskNode, String prop, Object value) {
        String taskParamsJson = taskNode.getParams();
        TaskParams taskParams = JSONUtils.parseObject(taskParamsJson, TaskParams.class);
        if (taskParams == null) {
            return;
        }
        taskParams.setLocalParamValue(prop, value);
        taskNode.setParams(JSONUtils.toJsonString(taskParams));
    }

    /**
     * setTaskNodeLocalParams
     * @param taskNode taskNode
     * @param propToValue propToValue
     */
    public static void setTaskNodeLocalParams(TaskNode taskNode, Map<String, Object> propToValue) {
        String taskParamsJson = taskNode.getParams();
        TaskParams taskParams = JSONUtils.parseObject(taskParamsJson, TaskParams.class);
        if (taskParams == null) {
            return;
        }
        taskParams.setLocalParamValue(propToValue);
        taskNode.setParams(JSONUtils.toJsonString(taskParams));
    }

    /**
     * convertVarPoolToMap
     * @param propToValue propToValue
     * @param varPool varPool
     * @throws ParseException ParseException
     */
    public static void convertVarPoolToMap(Map<String, Object> propToValue, String varPool) throws ParseException {
        if (varPool == null || propToValue == null) {
            return;
        }
        String[] splits = varPool.split("\\$VarPool\\$");
        for (String kv : splits) {
            String[] kvs = kv.split(",");
            if (kvs.length == 2) {
                propToValue.put(kvs[0], kvs[1]);
            } else {
                throw new ParseException(kv, 2);
            }
        }
    }

    /**
     * convertPythonScriptPlaceholders
     * @param rawScript rawScript
     * @return String
     * @throws StringIndexOutOfBoundsException StringIndexOutOfBoundsException
     */
    public static String convertPythonScriptPlaceholders(String rawScript) throws StringIndexOutOfBoundsException {
        int len = "${setShareVar(${".length();
        int scriptStart = 0;
        while ((scriptStart = rawScript.indexOf("${setShareVar(${", scriptStart)) != -1) {
            int start = -1;
            int end = rawScript.indexOf('}', scriptStart + len);
            String prop = rawScript.substring(scriptStart + len, end);

            start = rawScript.indexOf(',', end);
            end = rawScript.indexOf(')', start);

            String value = rawScript.substring(start + 1, end);

            start = rawScript.indexOf('}', start) + 1;
            end = rawScript.length();

            String replaceScript = String.format("print(\"${{setValue({},{})}}\".format(\"%s\",%s))", prop, value);

            rawScript = rawScript.substring(0, scriptStart) + replaceScript + rawScript.substring(start, end);

            scriptStart += replaceScript.length();
        }
        return rawScript;
    }
} 