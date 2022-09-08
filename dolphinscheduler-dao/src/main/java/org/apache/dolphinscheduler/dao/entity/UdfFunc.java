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

package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.io.IOException;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.google.common.base.Strings;

@Data
@TableName("t_ds_udfs")
public class UdfFunc {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * user id
     */
    private int userId;

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = "UDF";
    }

    @TableField(exist = false)
    private String resourceType = "UDF";
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

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

    /**
     * user name
     */
    @TableField(exist = false)
    private String userName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UdfFunc udfFunc = (UdfFunc) o;

        if (id != udfFunc.id) {
            return false;
        }
        return !(funcName != null ? !funcName.equals(udfFunc.funcName) : udfFunc.funcName != null);

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

    public static class UdfFuncDeserializer extends KeyDeserializer {

        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            if (Strings.isNullOrEmpty(key)) {
                return null;
            }
            return JSONUtils.parseObject(key, UdfFunc.class);
        }
    }
}
