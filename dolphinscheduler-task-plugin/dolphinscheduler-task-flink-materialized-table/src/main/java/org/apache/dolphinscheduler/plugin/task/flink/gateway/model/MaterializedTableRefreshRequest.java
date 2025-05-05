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

package org.apache.dolphinscheduler.plugin.task.flink.gateway.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Request for refreshing a materialized table in Flink SQL Gateway.
 */
public class MaterializedTableRefreshRequest {

    /**
     * Dynamic options for the refresh operation.
     * These options can be modified between refresh operations.
     */
    private Map<String, String> dynamicOptions;

    /**
     * Execution configuration for the Flink job.
     * These parameters control how the refresh job is executed.
     */
    private Map<String, String> executionConfig;

    /**
     * Flag indicating whether this is a periodic refresh operation.
     */
    private String periodicity;

    /**
     * Scheduled time for the refresh operation.
     * This is only used for periodic refreshes.
     */
    private String scheduledTime;

    /**
     * Static partition information for targeted refreshes.
     * Specifies which partitions should be refreshed.
     */
    private List<String> staticPartitions;

    /**
     * Constructs a new RefreshMaterializedTableRequest with empty maps.
     */
    public MaterializedTableRefreshRequest() {
    }

    /**
     * Constructs a new RefreshMaterializedTableRequest with the specified parameters.
     *
     * @param dynamicOptions Dynamic options for the refresh operation
     * @param executionConfig Execution configuration for the Flink job
     * @param periodicity Flag indicating whether this is a periodic refresh operation
     * @param scheduledTime Scheduled time for the refresh operation
     * @param staticPartitions Static partition information for targeted refreshes
     */
    public MaterializedTableRefreshRequest(Map<String, String> dynamicOptions,
                                           Map<String, String> executionConfig,
                                           String periodicity,
                                           String scheduledTime,
                                           List<String> staticPartitions) {
        this.dynamicOptions = dynamicOptions;
        this.executionConfig = executionConfig;
        this.periodicity = periodicity;
        this.scheduledTime = scheduledTime;
        this.staticPartitions = staticPartitions;
    }

    /**
     * Gets the dynamic options for the refresh operation.
     *
     * @return Map of dynamic options
     */
    public Map<String, String> getDynamicOptions() {
        return dynamicOptions;
    }

    /**
     * Sets the dynamic options for the refresh operation.
     *
     * @param dynamicOptions Map of dynamic options to set
     */
    public void setDynamicOptions(Map<String, String> dynamicOptions) {
        this.dynamicOptions = dynamicOptions;
    }

    /**
     * Gets the execution configuration for the Flink job.
     *
     * @return Map of execution configuration parameters
     */
    public Map<String, String> getExecutionConfig() {
        return executionConfig;
    }

    /**
     * Sets the execution configuration for the Flink job.
     *
     * @param executionConfig Map of execution configuration parameters to set
     */
    public void setExecutionConfig(Map<String, String> executionConfig) {
        this.executionConfig = executionConfig;
    }

    /**
     * Gets whether this is a periodic refresh operation.
     *
     * @return true if this is a periodic refresh, false otherwise
     */
    public String getPeriodicity() {
        return periodicity;
    }

    /**
     * Sets whether this is a periodic refresh operation.
     *
     * @param periodicity true for periodic refresh, false otherwise
     */
    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    /**
     * Gets the scheduled time for the refresh operation.
     *
     * @return The scheduled time as a string, or null if not set
     */
    public String getScheduledTime() {
        return scheduledTime;
    }

    /**
     * Sets the scheduled time for the refresh operation.
     *
     * @param scheduledTime The scheduled time to set
     */
    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    /**
     * Gets the static partition information.
     *
     * @return List of static partition information
     */
    public List<String> getStaticPartitions() {
        return staticPartitions;
    }

    /**
     * Sets the static partition information.
     *
     * @param staticPartitions List of static partition information to set
     */
    public void setStaticPartitions(List<String> staticPartitions) {
        this.staticPartitions = staticPartitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaterializedTableRefreshRequest that = (MaterializedTableRefreshRequest) o;
        return Objects.equals(dynamicOptions, that.dynamicOptions)
                && Objects.equals(executionConfig, that.executionConfig)
                && Objects.equals(periodicity, that.periodicity)
                && Objects.equals(scheduledTime, that.scheduledTime)
                && Objects.equals(staticPartitions, that.staticPartitions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dynamicOptions, executionConfig, periodicity, scheduledTime, staticPartitions);
    }

    @Override
    public String toString() {
        return "MaterializedTableRefreshRequest{"
                + "dynamicOptions=" + dynamicOptions
                + ", executionConfig=" + executionConfig
                + ", periodicity='" + periodicity + '\''
                + ", scheduledTime='" + scheduledTime + '\''
                + ", staticPartitions=" + staticPartitions
                + '}';
    }
}
