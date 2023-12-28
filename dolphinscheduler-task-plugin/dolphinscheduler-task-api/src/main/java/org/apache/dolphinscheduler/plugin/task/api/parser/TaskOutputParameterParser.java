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
package org.apache.dolphinscheduler.plugin.task.api.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import lombok.extern.slf4j.Slf4j;

/**
 * Used to parse ${setValue()} and #{setValue()} from given lines.
 */
@Slf4j
@NotThreadSafe
public class TaskOutputParameterParser {

    // Used to avoid '${setValue(' which loss the end of ')}'
    private final int maxOneParameterRows;

    // Used to avoid '${setValue(' which length is too long, this may case OOM
    private final int maxOneParameterLength;

    private final Map<String, String> taskOutputParams;

    private List<String> currentTaskOutputParam;

    private long currentTaskOutputParamLength;

    public TaskOutputParameterParser() {
        // the default max rows of one parameter is 1024, this should be enough
        this(1024, Integer.MAX_VALUE);
    }

    public TaskOutputParameterParser(int maxOneParameterRows, int maxOneParameterLength) {
        this.maxOneParameterRows = maxOneParameterRows;
        this.maxOneParameterLength = maxOneParameterLength;
        this.taskOutputParams = new HashMap<>();
        this.currentTaskOutputParam = null;
        this.currentTaskOutputParamLength = 0;
    }

    public void appendParseLog(String logLine) {
        if (logLine == null) {
            return;
        }

        if (currentTaskOutputParam != null) {
            if (currentTaskOutputParam.size() > maxOneParameterRows
                    || currentTaskOutputParamLength > maxOneParameterLength) {
                log.warn(
                        "The output param expression '{}' is too long, the max rows is {}, max length is {}, will skip this param",
                        String.join("\n", currentTaskOutputParam), maxOneParameterLength, maxOneParameterRows);
                currentTaskOutputParam = null;
                currentTaskOutputParamLength = 0;
                return;
            }
            // continue to parse the rest of line
            int i = logLine.indexOf(")}");
            if (i == -1) {
                // the end of var pool not found
                currentTaskOutputParam.add(logLine);
                currentTaskOutputParamLength += logLine.length();
            } else {
                // the end of var pool found
                currentTaskOutputParam.add(logLine.substring(0, i + 2));
                Pair<String, String> keyValue = parseOutputParam(String.join("\n", currentTaskOutputParam));
                if (keyValue.getKey() != null && keyValue.getValue() != null) {
                    taskOutputParams.put(keyValue.getKey(), keyValue.getValue());
                }
                currentTaskOutputParam = null;
                currentTaskOutputParamLength = 0;
                // continue to parse the rest of line
                if (i + 2 != logLine.length()) {
                    appendParseLog(logLine.substring(i + 2));
                }
            }
            return;
        }

        int indexOfVarPoolBegin = logLine.indexOf("${setValue(");
        if (indexOfVarPoolBegin == -1) {
            indexOfVarPoolBegin = logLine.indexOf("#{setValue(");
        }
        if (indexOfVarPoolBegin == -1) {
            return;
        }
        currentTaskOutputParam = new ArrayList<>();
        appendParseLog(logLine.substring(indexOfVarPoolBegin));
    }

    public Map<String, String> getTaskOutputParams() {
        return taskOutputParams;
    }

    // #{setValue(xx=xx)}
    protected Pair<String, String> parseOutputParam(String outputParam) {
        if (StringUtils.isEmpty(outputParam)) {
            log.info("The task output param is empty");
            return ImmutablePair.nullPair();
        }
        if ((!outputParam.startsWith("${setValue(") && !outputParam.startsWith("#{setValue("))
                || !outputParam.endsWith(")}")) {
            log.info("The task output param {} should start with '${setValue(' or '#{setValue(' and end with ')}'",
                    outputParam);
            return ImmutablePair.nullPair();
        }
        String keyValueExpression = outputParam.substring(11, outputParam.length() - 2);
        if (!keyValueExpression.contains("=")) {
            log.warn("The task output param {} should composite with key=value", outputParam);
            return ImmutablePair.nullPair();
        }

        String[] keyValue = keyValueExpression.split("=", 2);
        return ImmutablePair.of(keyValue[0], keyValue[1]);
    }

}
