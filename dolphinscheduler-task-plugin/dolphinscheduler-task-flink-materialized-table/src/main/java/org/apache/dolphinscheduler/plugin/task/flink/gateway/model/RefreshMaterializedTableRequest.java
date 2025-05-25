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

    private Map<String, String> dynamicOptions;
    private Map<String, String> executionConfig;
    private Boolean isPeriodic;
    private @Nullable String scheduleTime;
    private Map<String, String> staticPartitions;

    /**
     * Default constructor initializing all maps.
     */
    public RefreshMaterializedTableRequest() {
        this.dynamicOptions = new HashMap<>();
        this.executionConfig = new HashMap<>();
        this.staticPartitions = new HashMap<>();
    }

    /**
     * All-args constructor.
     */
    public RefreshMaterializedTableRequest(Map<String, String> dynamicOptions,
                                           Map<String, String> executionConfig,
                                           Boolean isPeriodic,
                                           @Nullable String scheduleTime,
                                           Map<String, String> staticPartitions) {
        this.dynamicOptions = dynamicOptions != null ? dynamicOptions : new HashMap<>();
        this.executionConfig = executionConfig != null ? executionConfig : new HashMap<>();
        this.isPeriodic = isPeriodic;
        this.scheduleTime = scheduleTime;
        this.staticPartitions = staticPartitions != null ? staticPartitions : new HashMap<>();
    }

    public Map<String, String> getDynamicOptions() {
        return dynamicOptions;
    }

    public void setDynamicOptions(Map<String, String> dynamicOptions) {
        this.dynamicOptions = dynamicOptions;
    }

    public Map<String, String> getExecutionConfig() {
        return executionConfig;
    }

    public void setExecutionConfig(Map<String, String> executionConfig) {
        this.executionConfig = executionConfig;
    }

    public Boolean getIsPeriodic() {
        return isPeriodic;
    }

    public void setIsPeriodic(Boolean isPeriodic) {
        this.isPeriodic = isPeriodic;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Map<String, String> getStaticPartitions() {
        return staticPartitions;
    }

    public void setStaticPartitions(Map<String, String> staticPartitions) {
        this.staticPartitions = staticPartitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RefreshMaterializedTableRequest))
            return false;
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
        return "RefreshMaterializedTableRequest{" +
                "dynamicOptions=" + dynamicOptions +
                ", executionConfig=" + executionConfig +
                ", isPeriodic=" + isPeriodic +
                ", scheduleTime='" + scheduleTime + '\'' +
                ", staticPartitions=" + staticPartitions +
                '}';
    }
}
