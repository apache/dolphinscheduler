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

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.regex.Pattern;

/**
 * Parameters for Flink Materialized Table Task.
 * 
 * This class defines the configuration parameters required for refreshing Flink materialized tables.
 * It includes parameters for table identification, gateway connection, and execution configuration.
 */
public class FlinkMaterializedTableParameters extends AbstractParameters {

    /**
     * The fully qualified identifier of the materialized table in the format: catalog.database.table.
     */
    private String identifier;

    /**
     * The endpoint URL of the Flink SQL Gateway.
     */
    private String gatewayEndpoint;

    /**
     * Initial configuration for the Flink SQL Gateway session.
     * These parameters are used when opening a new session.
     */
    private String initConfig;

    /**
     * Dynamic options for the materialized table refresh operation.
     * These parameters are passed to the refresh request.
     */
    private String dynamicOptions;

    /**
     * Execution configuration for the refresh operation.
     * These parameters control how the refresh job is executed.
     */
    private String executionConfig;

    /**
     * Description of the SQL statement (optional).
     */
    private String statementDescription;

    /**
     * Regular expression pattern for validating gateway endpoint URLs.
     */
    private static final Pattern URL_PATTERN = Pattern.compile("^(http|https)://[\\w\\-\\.]+(?::\\d+)?(?:/.*)?$");

    public FlinkMaterializedTableParameters() {
        // Default constructor
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getGatewayEndpoint() {
        return gatewayEndpoint;
    }

    public void setGatewayEndpoint(String gatewayEndpoint) {
        this.gatewayEndpoint = gatewayEndpoint;
    }

    public String getInitConfig() {
        return initConfig;
    }

    public void setInitConfig(String initConfig) {
        this.initConfig = initConfig;
    }

    public String getDynamicOptions() {
        return dynamicOptions;
    }

    public void setDynamicOptions(String dynamicOptions) {
        this.dynamicOptions = dynamicOptions;
    }

    public String getExecutionConfig() {
        return executionConfig;
    }

    public void setExecutionConfig(String executionConfig) {
        this.executionConfig = executionConfig;
    }

    /**
     * Validates the required parameters.
     * 
     * Checks if the identifier and gateway endpoint are properly set.
     *
     * @return true if all required parameters are valid, false otherwise
     */
    @Override
    public boolean checkParameters() {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }

        if (gatewayEndpoint == null || !URL_PATTERN.matcher(gatewayEndpoint).matches()) {
            return false;
        }

        return true;
    }
}
