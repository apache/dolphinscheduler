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

package org.apache.dolphinscheduler.plugin.task.dq.rule.entity;

import org.apache.dolphinscheduler.spi.task.dq.enums.ExecuteSqlType;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * RuleExecuteSql
 */
public class DqRuleExecuteSql implements Serializable {
    /**
     * primary key
     */
    private int id;
    /**
     * index，ensure the execution order of sql
     */
    private int index;
    /**
     * SQL Statement
     */
    private String sql;
    /**
     * table alias name
     */
    private String tableAlias;
    /**
     * input entry type: default,statistics,comparison,check
     */
    private int type = ExecuteSqlType.MIDDLE.getCode();
    /**
     * is error output sql
     */
    private boolean isErrorOutputSql;
    /**
     * create_time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * update_time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isErrorOutputSql() {
        return isErrorOutputSql;
    }

    public void setErrorOutputSql(boolean errorOutputSql) {
        isErrorOutputSql = errorOutputSql;
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
        return "DqRuleExecuteSql{"
                + "id=" + id
                + ", index=" + index
                + ", sql='" + sql + '\''
                + ", tableAlias='" + tableAlias + '\''
                + ", type=" + type
                + ", isErrorOutputSql=" + isErrorOutputSql
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + '}';
    }
}