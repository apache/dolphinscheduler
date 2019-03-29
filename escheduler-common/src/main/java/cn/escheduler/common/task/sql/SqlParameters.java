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
package cn.escheduler.common.task.sql;

import cn.escheduler.common.task.AbstractParameters;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Sql/Hql parameter
 */
public class SqlParameters extends AbstractParameters {
    /**
     * data source type，eg  MYSQL, POSTGRES, HIVE ...
     */
    private String type;

    /**
     * datasource id
     */
    private int datasource;

    /**
     * sql
     */
    private String sql;

    /**
     * sql type
     * 0 query
     * 1 NON_QUERY
     */
    private int sqlType;

    /**
     * udf list
     */
    private String udfs;
    /**
     * show type
     * 0 TABLE
     * 1 TEXT
     * 2 attachment
     * 3 TABLE+attachment
     */
    private String showType;
    /**
     * SQL connection parameters
     */
    private String connParams;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDatasource() {
        return datasource;
    }

    public void setDatasource(int datasource) {
        this.datasource = datasource;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getUdfs() {
        return udfs;
    }

    public void setUdfs(String udfs) {
        this.udfs = udfs;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public String getShowType() {
        return showType;
    }

    public void setShowType(String showType) {
        this.showType = showType;
    }

    public String getConnParams() {
        return connParams;
    }

    public void setConnParams(String connParams) {
        this.connParams = connParams;
    }


    @Override
    public boolean checkParameters() {
        return datasource != 0 && StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(sql);
    }

    @Override
    public List<String> getResourceFilesList() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "SqlParameters{" +
                "type='" + type + '\'' +
                ", datasource=" + datasource +
                ", sql='" + sql + '\'' +
                ", sqlType=" + sqlType +
                ", udfs='" + udfs + '\'' +
                ", showType='" + showType + '\'' +
                ", connParams='" + connParams + '\'' +
                '}';
    }
}
