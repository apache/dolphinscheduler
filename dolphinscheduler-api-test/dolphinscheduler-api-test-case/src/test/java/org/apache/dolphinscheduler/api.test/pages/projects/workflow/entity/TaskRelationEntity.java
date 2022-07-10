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

package org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;

import java.util.Map;

public class TaskRelationEntity extends AbstractBaseEntity {
    public String name;

    public int preTaskCode;

    public int preTaskVersion;

    public String postTaskCode;

    public int postTaskVersion;

    public String conditionType;

    public Map<String, String> conditionParams;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPreTaskCode() {
        return preTaskCode;
    }

    public void setPreTaskCode(int preTaskCode) {
        this.preTaskCode = preTaskCode;
    }

    public int getPreTaskVersion() {
        return preTaskVersion;
    }

    public void setPreTaskVersion(int preTaskVersion) {
        this.preTaskVersion = preTaskVersion;
    }

    public String getPostTaskCode() {
        return postTaskCode;
    }

    public void setPostTaskCode(String postTaskCode) {
        this.postTaskCode = postTaskCode;
    }

    public int getPostTaskVersion() {
        return postTaskVersion;
    }

    public void setPostTaskVersion(int postTaskVersion) {
        this.postTaskVersion = postTaskVersion;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public Map<String, String> getConditionParams() {
        return conditionParams;
    }

    public void setConditionParams(Map<String, String> conditionParams) {
        this.conditionParams = conditionParams;
    }
}
