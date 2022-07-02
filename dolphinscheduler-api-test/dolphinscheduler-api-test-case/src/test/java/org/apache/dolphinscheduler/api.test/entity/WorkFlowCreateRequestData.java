/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.api.test.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WorkFlowCreateRequestData {

    String taskDefinitionJson;

    String taskRelationJson;

    String locations;

    String name;

    String tenantCode;

    String executionType;

    String description;

    String globalParams;

    Integer timeout;

    public void setTaskDefinitionJson(String taskDefinitionJson) {
        this.taskDefinitionJson = taskDefinitionJson;
    }

    public void setTaskRelationJson(String taskRelationJson) {
        this.taskRelationJson = taskRelationJson;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGlobalParams(String globalParams) {
        this.globalParams = globalParams;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getTaskDefinitionJson() {
        return taskDefinitionJson;
    }

    public String getTaskRelationJson() {
        return taskRelationJson;
    }

    public String getLocations() {
        return locations;
    }

    public String getName() {
        return name;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public String getExecutionType() {
        return executionType;
    }

    public String getDescription() {
        return description;
    }

    public String getGlobalParams() {
        return globalParams;
    }

    public Integer getTimeout() {
        return timeout;
    }
}
