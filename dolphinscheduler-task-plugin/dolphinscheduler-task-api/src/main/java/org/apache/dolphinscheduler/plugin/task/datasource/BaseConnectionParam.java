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

package org.apache.dolphinscheduler.plugin.task.datasource;

import org.apache.dolphinscheduler.plugin.task.datasource.clickhouse.ClickhouseConnectionParam;
import org.apache.dolphinscheduler.plugin.task.datasource.db2.Db2ConnectionParam;
import org.apache.dolphinscheduler.plugin.task.datasource.hive.HiveConnectionParam;
import org.apache.dolphinscheduler.plugin.task.datasource.mysql.MysqlConnectionParam;
import org.apache.dolphinscheduler.plugin.task.datasource.oracle.OracleConnectionParam;
import org.apache.dolphinscheduler.plugin.task.datasource.postgresql.PostgreSqlConnectionParam;
import org.apache.dolphinscheduler.plugin.task.datasource.presto.PrestoConnectionParam;
import org.apache.dolphinscheduler.plugin.task.datasource.spark.SparkConnectionParam;
import org.apache.dolphinscheduler.plugin.task.datasource.sqlserver.SqlServerConnectionParam;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * The base model of connection param
 * <p>
 * {@link ClickhouseConnectionParam}
 * {@link Db2ConnectionParam}
 * {@link HiveConnectionParam}
 * {@link MysqlConnectionParam}
 * {@link OracleConnectionParam}
 * {@link PostgreSqlConnectionParam}
 * {@link PrestoConnectionParam}
 * {@link SparkConnectionParam}
 * {@link SqlServerConnectionParam}
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
