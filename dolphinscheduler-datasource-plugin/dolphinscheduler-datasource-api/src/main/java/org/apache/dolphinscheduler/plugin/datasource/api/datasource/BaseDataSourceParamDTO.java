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

package org.apache.dolphinscheduler.plugin.datasource.api.datasource;

import org.apache.dolphinscheduler.plugin.datasource.api.datasource.clickhouse.ClickHouseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.db2.Db2DataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.hive.HiveDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.mysql.MySQLDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.oracle.OracleDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.postgresql.PostgreSQLDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.presto.PrestoDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.spark.SparkDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.sqlserver.SQLServerDataSourceParamDTO;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Basic datasource params submitted to api.
 * <p>
 * see {@link MySQLDataSourceParamDTO}
 * see {@link PostgreSQLDataSourceParamDTO}
 * see {@link HiveDataSourceParamDTO}
 * see {@link SparkDataSourceParamDTO}
 * see {@link ClickHouseDataSourceParamDTO}
 * see {@link OracleDataSourceParamDTO}
 * see {@link SQLServerDataSourceParamDTO}
 * see {@link Db2DataSourceParamDTO}
 * see {@link PrestoDataSourceParamDTO}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(value = {
    @JsonSubTypes.Type(value = MySQLDataSourceParamDTO.class, name = "MYSQL"),
    @JsonSubTypes.Type(value = PostgreSQLDataSourceParamDTO.class, name = "POSTGRESQL"),
    @JsonSubTypes.Type(value = HiveDataSourceParamDTO.class, name = "HIVE"),
    @JsonSubTypes.Type(value = SparkDataSourceParamDTO.class, name = "SPARK"),
    @JsonSubTypes.Type(value = ClickHouseDataSourceParamDTO.class, name = "CLICKHOUSE"),
    @JsonSubTypes.Type(value = OracleDataSourceParamDTO.class, name = "ORACLE"),
    @JsonSubTypes.Type(value = SQLServerDataSourceParamDTO.class, name = "SQLSERVER"),
    @JsonSubTypes.Type(value = Db2DataSourceParamDTO.class, name = "DB2"),
    @JsonSubTypes.Type(value = PrestoDataSourceParamDTO.class, name = "PRESTO"),
})
public abstract class BaseDataSourceParamDTO implements Serializable {

    protected Integer id;

    protected String name;

    protected String note;

    protected String host;

    protected Integer port;

    protected String database;

    protected String userName;

    protected String password;

    protected Map<String, String> other;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getOther() {
        return other;
    }

    public void setOther(Map<String, String> other) {
        this.other = other;
    }

    /**
     * Get the datasource type
     * see{@link DbType}
     *
     * @return datasource type code
     */
    public abstract DbType getType();
}
