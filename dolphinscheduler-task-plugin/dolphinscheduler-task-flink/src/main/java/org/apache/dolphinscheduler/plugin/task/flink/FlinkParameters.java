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

package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.dolphinscheduler.plugin.task.api.enums.SqlSourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.Data;

@Data
public class FlinkParameters extends AbstractParameters {

    /**
     * major jar
     */
    private ResourceInfo mainJar;

    /**
     * major class
     */
    private String mainClass;

    /**
     * deploy mode  yarn-cluster yarn-local yarn-application
     */
    private FlinkDeployMode deployMode;

    /**
     * arguments
     */
    private String mainArgs;

    /**
     * slot count
     */
    private int slot;

    private int parallelism;

    /**
     * yarn application name
     */
    private String appName;

    /**
     * taskManager count
     */
    private int taskManager;

    private String jobManagerMemory;

    private String taskManagerMemory;

    private List<ResourceInfo> resourceList = new ArrayList<>();

    /**
     * The YARN queue to submit to
     */
    private String yarnQueue;

    /**
     * other arguments
     */
    private String others;

    private String flinkVersion;

    /**
     * program type
     * 0 JAVA,1 SCALA,2 PYTHON,3 SQL
     */
    private ProgramType programType;

    /**
     * flink sql initialization file
     */
    private String initScript;

    /**
     * flink sql script file
     */
    private String rawScript;

    /**
     * Absent or CLIENT keeps sql-client.sh. SQL_GATEWAY submits over JDBC.
     */
    private FlinkSqlSubmitType sqlSubmitType;

    /**
     * example: jdbc:flink://host:port
     */
    private String flinkJdbcUrl;

    /**
     * optional: username/password or other jdbc properties
     */
    private Map<String, String> jdbcProperties;

    /**
     * init script source, used only when {@link #sqlSubmitType} is SQL_GATEWAY
     */
    private SqlSourceType initScriptType = SqlSourceType.FILE;

    /**
     * resource list for init script file (when initScriptType=FILE), size 1
     */
    private List<ResourceInfo> initScriptResourceList;

    /**
     * main script source, used only when {@link #sqlSubmitType} is SQL_GATEWAY
     */
    private SqlSourceType rawScriptType = SqlSourceType.FILE;

    /**
     * default: ;
     */
    private String statementSeparator = ";";

    /**
     * print query result rows count in log, default 0 means do not print rows
     */
    private int maxPrintRows;

    public boolean isSqlGateway() {
        return sqlSubmitType == FlinkSqlSubmitType.SQL_GATEWAY;
    }

    public Properties toJdbcProperties() {
        Properties props = new Properties();
        if (jdbcProperties != null) {
            jdbcProperties.forEach((k, v) -> {
                if (k != null && v != null) {
                    props.put(k, v);
                }
            });
        }
        return props;
    }

    @Override
    public boolean checkParameters() {
        if (isSqlGateway()) {
            return checkSqlGatewayParameters();
        }
        /**
         * When saving a task, the parameter cannot be empty. There are two judgments:
         * (1) When ProgramType is SQL, rawScript cannot be empty.
         * (2) When ProgramType is Java/Scala/Python, mainJar cannot be empty.
         */
        return programType != null && (rawScript != null || mainJar != null);
    }

    private boolean checkSqlGatewayParameters() {
        if (StringUtils.isBlank(flinkJdbcUrl)) {
            return false;
        }
        if (initScriptType == SqlSourceType.FILE && isEmpty(initScriptResourceList)) {
            return false;
        }
        if (rawScriptType == SqlSourceType.FILE) {
            return !isEmpty(resourceList);
        }
        return rawScriptType == SqlSourceType.SCRIPT && StringUtils.isNotBlank(rawScript);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        if (isSqlGateway()) {
            List<ResourceInfo> list = new ArrayList<>();
            if (initScriptType == SqlSourceType.FILE && !isEmpty(initScriptResourceList)) {
                list.addAll(initScriptResourceList);
            }
            if (rawScriptType == SqlSourceType.FILE && !isEmpty(resourceList)) {
                list.addAll(resourceList);
            }
            return list;
        }
        if (mainJar != null && !resourceList.contains(mainJar)) {
            resourceList.add(mainJar);
        }
        return resourceList;
    }

    private static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
}
