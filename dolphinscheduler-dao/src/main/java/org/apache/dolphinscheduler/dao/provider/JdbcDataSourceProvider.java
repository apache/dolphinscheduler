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

package org.apache.dolphinscheduler.dao.provider;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.exception.BaseException;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.commons.collections.MapUtils;

import java.sql.Driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * Jdbc Data Source Provider
 */
public class JdbcDataSourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(JdbcDataSourceProvider.class);

    public JdbcDataSourceProvider() {
    }

    public static DruidDataSource createJdbcDataSource(BaseConnectionParam properties) {
        logger.info("Creating DruidDataSource pool for maxActive:{}", PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MAX_ACTIVE, 50));

        DruidDataSource druidDataSource = new DruidDataSource();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Driver driver = getDataSourceDriver(properties, classLoader);

        druidDataSource.setDriverClassLoader(classLoader);
        if (driver != null) {
            druidDataSource.setDriver(driver);
        }
        druidDataSource.setDriverClassName(properties.getDriverClassName());
        druidDataSource.setUrl(properties.getJdbcUrl());
        druidDataSource.setUsername(properties.getUser());
        druidDataSource.setPassword(CommonUtils.decodePassword(properties.getPassword()));

        druidDataSource.setMinIdle(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MIN_IDLE, 5));
        druidDataSource.setMaxActive(PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MAX_ACTIVE, 50));
        druidDataSource.setTestOnBorrow(PropertyUtils.getBoolean(Constants.SPRING_DATASOURCE_TEST_ON_BORROW, false));

        if (MapUtils.isNotEmpty(properties.getProps())) {
            properties.getProps().forEach(druidDataSource::addConnectionProperty);
        }
        logger.info("Creating DruidDataSource pool success.");
        return druidDataSource;
    }

    /**
     * @return One Session Jdbc DataSource
     */
    public static DruidDataSource createOneSessionJdbcDataSource(BaseConnectionParam properties) {
        logger.info("Creating OneSession DruidDataSource pool for maxActive:{}", PropertyUtils.getInt(Constants.SPRING_DATASOURCE_MAX_ACTIVE, 50));

        DruidDataSource druidDataSource = new DruidDataSource();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Driver driver = getDataSourceDriver(properties, classLoader);

        druidDataSource.setDriverClassLoader(classLoader);
        if (driver != null) {
            druidDataSource.setDriver(driver);
        }
        druidDataSource.setDriverClassName(properties.getDriverClassName());
        druidDataSource.setUrl(properties.getJdbcUrl());
        druidDataSource.setUsername(properties.getUser());
        druidDataSource.setPassword(CommonUtils.decodePassword(properties.getPassword()));

        druidDataSource.setMinIdle(1);
        druidDataSource.setMaxActive(1);
        druidDataSource.setTestOnBorrow(true);

        if (MapUtils.isNotEmpty(properties.getProps())) {
            properties.getProps().forEach(druidDataSource::addConnectionProperty);
        }
        logger.info("Creating OneSession DruidDataSource pool success.");
        return druidDataSource;
    }

    private static Driver getDataSourceDriver(BaseConnectionParam properties, ClassLoader classLoader) {
        Driver driver;
        try {
            final Class<?> clazz = Class.forName(properties.getDriverClassName(), true, classLoader);
            driver = (Driver) clazz.newInstance();
        } catch (Exception e) {
            throw BaseException.getInstance("Jdbc driver init error.");
        }
        return driver;
    }
}
