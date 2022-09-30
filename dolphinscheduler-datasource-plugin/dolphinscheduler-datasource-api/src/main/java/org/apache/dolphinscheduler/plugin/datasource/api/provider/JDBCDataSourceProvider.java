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

package org.apache.dolphinscheduler.plugin.datasource.api.provider;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.sql.Driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Jdbc Data Source Provider
 */
public class JDBCDataSourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(JDBCDataSourceProvider.class);

    public static HikariDataSource createJdbcDataSource(BaseConnectionParam properties, DbType dbType) {
        logger.info("Creating HikariDataSource pool for maxActive:{}",
                PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MAX_ACTIVE, 50));
        HikariDataSource dataSource = new HikariDataSource();

        // TODO Support multiple versions of data sources
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        loaderJdbcDriver(classLoader, properties, dbType);

        dataSource.setDriverClassName(properties.getDriverClassName());
        dataSource.setJdbcUrl(DataSourceUtils.getJdbcUrl(dbType, properties));
        dataSource.setUsername(properties.getUser());
        dataSource.setPassword(PasswordUtils.decodePassword(properties.getPassword()));

        dataSource.setMinimumIdle(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MIN_IDLE, 5));
        dataSource.setMaximumPoolSize(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MAX_ACTIVE, 50));
        dataSource.setConnectionTestQuery(properties.getValidationQuery());

        if (properties.getProps() != null) {
            properties.getProps().forEach(dataSource::addDataSourceProperty);
        }

        logger.info("Creating HikariDataSource pool success.");
        return dataSource;
    }

    /**
     * @return One Session Jdbc DataSource
     */
    public static HikariDataSource createOneSessionJdbcDataSource(BaseConnectionParam properties, DbType dbType) {
        logger.info("Creating OneSession HikariDataSource pool for maxActive:{}",
                PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MAX_ACTIVE, 50));

        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setDriverClassName(properties.getDriverClassName());
        dataSource.setJdbcUrl(DataSourceUtils.getJdbcUrl(dbType, properties));
        dataSource.setUsername(properties.getUser());
        dataSource.setPassword(PasswordUtils.decodePassword(properties.getPassword()));

        Boolean isOneSession = PropertyUtils.getBoolean(Constants.SUPPORT_HIVE_ONE_SESSION, false);
        dataSource.setMinimumIdle(isOneSession ? 1 : PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MIN_IDLE, 5));
        dataSource.setMaximumPoolSize(
                isOneSession ? 1 : PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MAX_ACTIVE, 50));
        dataSource.setConnectionTestQuery(properties.getValidationQuery());

        if (properties.getProps() != null) {
            properties.getProps().forEach(dataSource::addDataSourceProperty);
        }

        logger.info("Creating OneSession HikariDataSource pool success.");
        return dataSource;
    }

    protected static void loaderJdbcDriver(ClassLoader classLoader, BaseConnectionParam properties, DbType dbType) {
        String drv = StringUtils.isBlank(properties.getDriverClassName())
                ? DataSourceUtils.getDatasourceProcessor(dbType).getDatasourceDriver()
                : properties.getDriverClassName();
        try {
            final Class<?> clazz = Class.forName(drv, true, classLoader);
            final Driver driver = (Driver) clazz.newInstance();
            if (!driver.acceptsURL(properties.getJdbcUrl())) {
                logger.warn("Jdbc driver loading error. Driver {} cannot accept url.", drv);
                throw new RuntimeException("Jdbc driver loading error.");
            }
            if (dbType.equals(DbType.MYSQL)) {
                if (driver.getMajorVersion() >= 8) {
                    properties.setDriverClassName(drv);
                } else {
                    properties.setDriverClassName(Constants.COM_MYSQL_JDBC_DRIVER);
                }
            }
        } catch (final Exception e) {
            logger.warn("The specified driver not suitable.");
        }
    }

}
