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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * The base model of connection param
 * <p>
 * {@link org.apache.dolphinscheduler.common.datasource.clickhouse.ClickhouseConnectionParam}
 * {@link org.apache.dolphinscheduler.common.datasource.db2.Db2ConnectionParam}
 * {@link org.apache.dolphinscheduler.common.datasource.hive.HiveConnectionParam}
 * {@link org.apache.dolphinscheduler.common.datasource.mysql.MysqlConnectionParam}
 * {@link org.apache.dolphinscheduler.common.datasource.oracle.OracleConnectionParam}
 * {@link org.apache.dolphinscheduler.common.datasource.postgresql.PostgreSqlConnectionParam}
 * {@link org.apache.dolphinscheduler.common.datasource.presto.PrestoConnectionParam}
 * {@link org.apache.dolphinscheduler.common.datasource.spark.SparkConnectionParam}
 * {@link org.apache.dolphinscheduler.common.datasource.sqlserver.SqlServerConnectionParam}
 */
@JsonInclude(Include.NON_NULL)
public abstract class BaseConnectionParam implements ConnectionParam {

    protected String user;

    protected String password;

    protected String address;

    protected String database;

    protected String jdbcUrl;

    protected String other;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}
