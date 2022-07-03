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

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskRelationRequestData {
    private String name;

    private Integer preTaskCode;

    private Integer preTaskVersion;

    private String postTaskCode;

    private Integer postTaskVersion;

    private String conditionType;

    private Map conditionParams;

    public String getName() {
        return name;
    }

    public Integer getPreTaskCode() {
        return preTaskCode;
    }

    public Integer getPreTaskVersion() {
        return preTaskVersion;
    }

    public String getPostTaskCode() {
        return postTaskCode;
    }

    public Integer getPostTaskVersion() {
        return postTaskVersion;
    }

    public String getConditionType() {
        return conditionType;
    }

    public Map getConditionParams() {
        return conditionParams;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPreTaskCode(Integer preTaskCode) {
        this.preTaskCode = preTaskCode;
    }

    public void setPreTaskVersion(Integer preTaskVersion) {
        this.preTaskVersion = preTaskVersion;
    }

    public void setPostTaskCode(String postTaskCode) {
        this.postTaskCode = postTaskCode;
    }

    public void setPostTaskVersion(Integer postTaskVersion) {
        this.postTaskVersion = postTaskVersion;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public void setConditionParams(Map conditionParams) {
        this.conditionParams = conditionParams;
    }
}
