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

package org.apache.dolphinscheduler.plugin.task.seatunnel.generator;

import org.apache.dolphinscheduler.plugin.task.seatunnel.Constants;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelTaskExecutionContext;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeatunnelConfigGenerator {

    private static final Pattern sourceTableNamePattern = Pattern.compile("source_table_name\\s*=\\s*\"(.*?)\"");
    private static final Pattern resultTableNamePattern = Pattern.compile("result_table_name\\s*=\\s*\"(.*?)\"");

    private static final String SOURCE_TABLE_NAME = "source_table_name";
    private static final String RESULT_TABLE_NAME = "result_table_name";

    public static String generate(SeatunnelParameters seatunnelParams,
                                  SeatunnelTaskExecutionContext seatunnelTaskExecutionContext) {

        ConfigTemplate configTemplate = seatunnelParams.getSourceConfig().getDbType().toConfigTemplate(seatunnelParams,
                seatunnelTaskExecutionContext);

        // build env config
        String envConfig = configTemplate.createEnv(seatunnelParams);

        // build transform config
        String transformConfig = "";
        if (seatunnelParams.isCustomDataFilter()) {
            transformConfig = checkAndGetTransform(seatunnelParams.getCustomTransform());
        }

        // build source config
        String sourceConfig = configTemplate.createSourceConfig();

        // build sink config
        ConfigTemplate sinkConfigTemplate = seatunnelParams.getSinkConfig().getDbType()
                .toConfigTemplate(seatunnelParams, seatunnelTaskExecutionContext);
        String sinkConfig = sinkConfigTemplate.createSinkConfig();

        // add 'source_table_name' and 'result_table_name' item in source and sink config
        // if transform config has set them
        Map<String, String> sourceAndResultTableFromTransform = getSourceAndResultTableFromTransform(transformConfig);

        if (sourceAndResultTableFromTransform != null) {
            if (sourceAndResultTableFromTransform.containsKey(SOURCE_TABLE_NAME)) {
                sourceConfig = addValues(sourceConfig,
                        RESULT_TABLE_NAME + " = \"" + sourceAndResultTableFromTransform.get(SOURCE_TABLE_NAME) + "\"");
            }

            if (sourceAndResultTableFromTransform.containsKey(RESULT_TABLE_NAME)) {
                sinkConfig = addValues(sinkConfig,
                        SOURCE_TABLE_NAME + " = \"" + sourceAndResultTableFromTransform.get(RESULT_TABLE_NAME) + "\"");
            }
        }

        String seatunnelConfig = envConfig + "\n" + sourceConfig + "\n" + transformConfig + "\n" + sinkConfig;

        log.info("Generate Seatunnel Config => \n{}", seatunnelConfig);

        return seatunnelConfig;
    }

    public static String checkAndGetTransform(String transform) {
        if (!transform.startsWith("transform")) {
            throw new IllegalArgumentException(
                    "Custom data filters must include the full transform configuration and need to start with 'transform'");
        }

        return transform;
    }

    public static Map<String, String> getSourceAndResultTableFromTransform(String transform) {
        if (StringUtils.isEmpty(transform)) {
            return null;
        }

        Map<String, String> sourceAndResultTableNameMap = new HashMap<>();

        Matcher sourceMatcher = sourceTableNamePattern.matcher(transform);
        Matcher resultMatcher = resultTableNamePattern.matcher(transform);

        if (sourceMatcher.find()) {
            sourceAndResultTableNameMap.put(SOURCE_TABLE_NAME, sourceMatcher.group(1));
        }

        if (resultMatcher.find()) {
            sourceAndResultTableNameMap.put(RESULT_TABLE_NAME, resultMatcher.group(1));
        }

        return sourceAndResultTableNameMap;
    }

    public static String addValues(String config, String value) {
        int lastIndexOfBracketsRight = config.lastIndexOf(Constants.SINGLE_BRACKETS_RIGHT);

        int penultimateOfBracketsRight =
                config.substring(0, lastIndexOfBracketsRight).lastIndexOf(Constants.SINGLE_BRACKETS_RIGHT);

        String configTmp = config.substring(0, penultimateOfBracketsRight);

        return configTmp + value + "\n" + "  }\n}";
    }

}
