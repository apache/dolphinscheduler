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

import org.apache.dolphinscheduler.plugin.datasource.api.provider.JdbcDataSourceProvider;
import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
import org.apache.dolphinscheduler.spi.datasource.JdbcConnectionParam;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.base.Stopwatch;

public class CommonDataSourceClient implements DataSourceClient {

    private static final Logger logger = LoggerFactory.getLogger(CommonDataSourceClient.class);

    public static final String COMMON_USER = "root";
    public static final String COMMON_PASSWORD = "123456";
    public static final String COMMON_VALIDATION_QUERY = "select 1";

    protected final JdbcConnectionParam connectionParam;
    protected DataSource dataSource;
    protected JdbcTemplate jdbcTemplate;

    public CommonDataSourceClient(JdbcConnectionParam connectionParam) {
        this.connectionParam = connectionParam;
        preInit();
        checkEnv(connectionParam);
        initClient(connectionParam);
        checkClient();
    }

    protected void preInit() {
        logger.info("preInit in CommonDataSourceClient");
    }

    protected void checkEnv(JdbcConnectionParam connectionParam) {
        checkValidationQuery(connectionParam);
        checkUser(connectionParam);
    }

    protected void initClient(JdbcConnectionParam connectionParam) {
        this.dataSource = JdbcDataSourceProvider.getDataSourceFactory().createDataSource(connectionParam);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    protected void checkUser(JdbcConnectionParam connectionParam) {
        if (StringUtils.isBlank(connectionParam.getUser())) {
            setDefaultUsername(connectionParam);
        }
        if (StringUtils.isBlank(connectionParam.getPassword())) {
            setDefaultPassword(connectionParam);
        }
    }

    protected void setDefaultUsername(JdbcConnectionParam baseConnectionParam) {
        baseConnectionParam.setUser(COMMON_USER);
    }

    protected void setDefaultPassword(JdbcConnectionParam baseConnectionParam) {
        baseConnectionParam.setPassword(COMMON_PASSWORD);
    }

    protected void checkValidationQuery(JdbcConnectionParam connectionParam) {
        if (StringUtils.isBlank(connectionParam.getValidationQuery())) {
            setDefaultValidationQuery(connectionParam);
        }
    }

    protected void setDefaultValidationQuery(JdbcConnectionParam connectionParam) {
        connectionParam.setValidationQuery(COMMON_VALIDATION_QUERY);
    }

    @Override
    public void checkClient() {
        //Checking data source client
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            this.jdbcTemplate.execute(COMMON_VALIDATION_QUERY);
        } catch (Exception e) {
            throw new RuntimeException("JDBC connect failed", e);
        } finally {
            logger.info("Time to execute check jdbc client with sql {} for {} ms ", COMMON_VALIDATION_QUERY, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("get dataSource Connection fail SQLException: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void close() {
        logger.info("do close dataSource.");
        JdbcDataSourceProvider.getDataSourceFactory().destroy(this.dataSource);
        this.dataSource = null;
        this.jdbcTemplate = null;
    }

}
