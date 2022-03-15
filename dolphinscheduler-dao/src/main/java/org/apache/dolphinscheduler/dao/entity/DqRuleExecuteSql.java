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

import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ExecuteSqlType;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * RuleExecuteSql
 */
@TableName("t_ds_dq_rule_execute_sql")
public class DqRuleExecuteSql implements Serializable {
    /**
     * primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    /**
     * index，ensure the execution order of sql
     */
    @TableField(value = "index")
    private int index;
    /**
     * SQL Statement
     */
    @TableField(value = "sql")
    private String sql;
    /**
     * table alias name
     */
    @TableField(value = "table_alias")
    private String tableAlias;
    /**
     * input entry type: default,statistics,comparison,check
     */
    @TableField(value = "type")
    private int type = ExecuteSqlType.MIDDLE.getCode();
    /**
     * is error output sql
     */
    @TableField(value = "is_error_output_sql")
    private boolean isErrorOutputSql;
    /**
     * create_time
     */
    @TableField(value = "create_time")
    private Date createTime;
    /**
     * update_time
     */
    @TableField(value = "update_time")
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