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

import com.baomidou.mybatisplus.annotation.TableField;
import org.apache.dolphinscheduler.common.enums.UdfType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("t_ds_udfs")
public class UdfFunc {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    @TableField(value = "user_id")
    private int userId;
    @TableField(value = "func_name")
    private String funcName;
    @TableField(value = "class_name")
    private String className;
    @TableField(value = "arg_types")
    private String argTypes;
    @TableField(value = "database")
    private String database;
    @TableField(value = "description")
    private String description;
    @TableField(value = "resource_id")
    private int resourceId;
    @TableField(value = "resource_name")
    private String resourceName;
    @TableField(value = "type")
    private UdfType type;
    @TableField(value = "create_time")
    private Date createTime;
    @TableField(value = "update_time")
    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getArgTypes() {
        return argTypes;
    }

    public void setArgTypes(String argTypes) {
        this.argTypes = argTypes;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public UdfType getType() {
        return type;
    }

    public void setType(UdfType type) {
        this.type = type;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "UdfFunc{" +
                "id=" + id +
                ", userId=" + userId +
                ", funcName='" + funcName + '\'' +
                ", className='" + className + '\'' +
                ", argTypes='" + argTypes + '\'' +
                ", database='" + database + '\'' +
                ", description='" + description + '\'' +
                ", resourceId=" + resourceId +
                ", resourceName='" + resourceName + '\'' +
                ", type=" + type +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

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
}
