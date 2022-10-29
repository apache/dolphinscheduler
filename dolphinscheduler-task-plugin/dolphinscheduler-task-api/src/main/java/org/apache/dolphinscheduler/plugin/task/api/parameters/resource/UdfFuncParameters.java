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

package org.apache.dolphinscheduler.plugin.task.api.parameters.resource;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.UdfType;

import java.util.Date;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class UdfFuncParameters extends AbstractResourceParameters {

    /**
     * id
     */
    private int id;

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @JsonProperty(value = "UDF")
    private String resourceType;

    /**
     * user id
     */
    private int userId;

    /**
     * udf function name
     */
    private String funcName;

    /**
     * udf class name
     */
    private String className;

    /**
     * udf argument types
     */
    private String argTypes;

    /**
     * udf data base
     */
    private String database;

    /**
     * udf description
     */
    private String description;

    /**
     * resource id
     */
    private int resourceId;

    /**
     * resource name
     */
    private String resourceName;

    /**
     * udf function type: hive / spark
     */
    private UdfType type;

    private String tenantCode;

    private String defaultFS;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UdfFuncParameters udfFuncRequest = (UdfFuncParameters) o;

        if (id != udfFuncRequest.id) {
            return false;
        }
        return !(funcName != null ? !funcName.equals(udfFuncRequest.funcName) : udfFuncRequest.funcName != null);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (funcName != null ? funcName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return JSONUtils.toJsonString(this);
    }

}
