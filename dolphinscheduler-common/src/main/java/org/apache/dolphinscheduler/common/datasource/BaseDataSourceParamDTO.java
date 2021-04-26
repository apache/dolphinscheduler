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

package org.apache.dolphinscheduler.common.datasource;

import org.apache.dolphinscheduler.common.datasource.clickhouse.ClickHouseDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.db2.Db2DatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.hive.HiveDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.mysql.MysqlDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.oracle.OracleDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.postgresql.PostgreSqlDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.presto.PrestoDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.spark.SparkDatasourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.sqlserver.SqlServerDatasourceParamDTO;
import org.apache.dolphinscheduler.common.enums.DbType;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Basic datasource params submitted to api.
 * <p>
 * see {@link MysqlDatasourceParamDTO}
 * see {@link PostgreSqlDatasourceParamDTO}
 * see {@link HiveDataSourceParamDTO}
 * see {@link SparkDatasourceParamDTO}
 * see {@link ClickHouseDatasourceParamDTO}
 * see {@link OracleDatasourceParamDTO}
 * see {@link SqlServerDatasourceParamDTO}
 * see {@link Db2DatasourceParamDTO}
 * see {@link PrestoDatasourceParamDTO}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = MysqlDatasourceParamDTO.class, name = "MYSQL"),
        @JsonSubTypes.Type(value = PostgreSqlDatasourceParamDTO.class, name = "POSTGRESQL"),
        @JsonSubTypes.Type(value = HiveDataSourceParamDTO.class, name = "HIVE"),
        @JsonSubTypes.Type(value = SparkDatasourceParamDTO.class, name = "SPARK"),
        @JsonSubTypes.Type(value = ClickHouseDatasourceParamDTO.class, name = "CLICKHOUSE"),
        @JsonSubTypes.Type(value = OracleDatasourceParamDTO.class, name = "ORACLE"),
        @JsonSubTypes.Type(value = SqlServerDatasourceParamDTO.class, name = "SQLSERVER"),
        @JsonSubTypes.Type(value = Db2DatasourceParamDTO.class, name = "DB2"),
        @JsonSubTypes.Type(value = PrestoDatasourceParamDTO.class, name = "PRESTO"),
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
