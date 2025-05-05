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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Request object for refreshing Flink materialized tables.
 */
public class RefreshMaterializedTableRequest {

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
    private Boolean isPeriodic;

    /**
     * Scheduled time for the refresh operation.
     * This is only used for periodic refreshes.
     */
    private @Nullable String scheduleTime;

    /**
     * Static partition information for targeted refreshes.
     * Specifies which partitions should be refreshed.
     */
    private Map<String, String> staticPartitions;

    /**
     * Constructs a new RefreshMaterializedTableRequest with empty maps.
     */
    public RefreshMaterializedTableRequest() {
        this.executionConfig = new HashMap<>();
        this.dynamicOptions = new HashMap<>();
        this.staticPartitions = new HashMap<>();
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
    public Boolean getIsPeriodic() {
        return isPeriodic;
    }

    /**
     * Sets whether this is a periodic refresh operation.
     *
     * @param isPeriodic true for periodic refresh, false otherwise
     */
    public void setIsPeriodic(Boolean isPeriodic) {
        this.isPeriodic = isPeriodic;
    }

    /**
     * Gets the scheduled time for the refresh operation.
     *
     * @return The scheduled time as a string, or null if not set
     */
    public String getScheduleTime() {
        return scheduleTime;
    }

    /**
     * Sets the scheduled time for the refresh operation.
     *
     * @param scheduleTime The scheduled time to set
     */
    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    /**
     * Gets the static partition information.
     *
     * @return Map of static partition information
     */
    public Map<String, String> getStaticPartitions() {
        return staticPartitions;
    }

    /**
     * Sets the static partition information.
     *
     * @param staticPartitions Map of static partition information to set
     */
    public void setStaticPartitions(Map<String, String> staticPartitions) {
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
        RefreshMaterializedTableRequest that = (RefreshMaterializedTableRequest) o;
        return Objects.equals(dynamicOptions, that.dynamicOptions)
                && Objects.equals(executionConfig, that.executionConfig)
                && Objects.equals(isPeriodic, that.isPeriodic)
                && Objects.equals(scheduleTime, that.scheduleTime)
                && Objects.equals(staticPartitions, that.staticPartitions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dynamicOptions, executionConfig, isPeriodic, scheduleTime, staticPartitions);
    }

    @Override
    public String toString() {
        return "RefreshMaterializedTableRequest{"
                + "dynamicOptions=" + dynamicOptions
                + ", executionConfig=" + executionConfig
                + ", isPeriodic=" + isPeriodic
                + ", scheduleTime='" + scheduleTime + '\''
                + ", staticPartitions=" + staticPartitions
                + '}';
    }
}
