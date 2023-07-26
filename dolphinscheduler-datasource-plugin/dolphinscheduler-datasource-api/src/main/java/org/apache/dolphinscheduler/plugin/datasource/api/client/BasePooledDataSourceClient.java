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

package org.apache.dolphinscheduler.plugin.datasource.api.client;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.common.constants.DataSourceConstants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.PooledDataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.MapUtils;

import java.sql.Connection;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

import com.zaxxer.hikari.HikariDataSource;

@Slf4j
public abstract class BasePooledDataSourceClient implements PooledDataSourceClient {

    protected final BaseConnectionParam baseConnectionParam;
    protected HikariDataSource dataSource;

    public BasePooledDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {

        this.baseConnectionParam = checkNotNull(baseConnectionParam, "baseConnectionParam is null");
        this.dataSource = createDataSourcePool(baseConnectionParam, checkNotNull(dbType, "dbType is null"));
    }

    // todo: support multiple version databases
    @Override
    public HikariDataSource createDataSourcePool(BaseConnectionParam baseConnectionParam, DbType dbType) {

        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setDriverClassName(baseConnectionParam.getDriverClassName());
        dataSource.setJdbcUrl(DataSourceUtils.getJdbcUrl(dbType, baseConnectionParam));
        dataSource.setUsername(baseConnectionParam.getUser());
        dataSource.setPassword(PasswordUtils.decodePassword(baseConnectionParam.getPassword()));

        dataSource.setMinimumIdle(PropertyUtils.getInt(DataSourceConstants.SPRING_DATASOURCE_MIN_IDLE, 5));
        dataSource.setMaximumPoolSize(PropertyUtils.getInt(DataSourceConstants.SPRING_DATASOURCE_MAX_ACTIVE, 50));
        dataSource.setConnectionTestQuery(baseConnectionParam.getValidationQuery());

        if (MapUtils.isNotEmpty(baseConnectionParam.getOther())) {
            baseConnectionParam.getOther().forEach(dataSource::addDataSourceProperty);
        }

        log.info("Creating HikariDataSource for {} success.", dbType.name());
        return dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        log.info("do close dataSource {}.", baseConnectionParam.getDatabase());
        try (HikariDataSource closedDatasource = dataSource) {
            // only close the resource
        }
    }

}
