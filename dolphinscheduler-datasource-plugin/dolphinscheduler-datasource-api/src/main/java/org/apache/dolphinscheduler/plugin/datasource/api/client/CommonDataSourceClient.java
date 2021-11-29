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
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
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

    protected final BaseConnectionParam baseConnectionParam;
    protected DataSource dataSource;
    protected JdbcTemplate jdbcTemplate;

    public CommonDataSourceClient(BaseConnectionParam baseConnectionParam) {
        this.baseConnectionParam = baseConnectionParam;
        preInit();
        checkEnv(baseConnectionParam);
        initClient(baseConnectionParam);
        checkClient();
    }

    protected void preInit() {
        logger.info("preInit in CommonDataSourceClient");
    }

    protected void checkEnv(BaseConnectionParam baseConnectionParam) {
        checkValidationQuery(baseConnectionParam);
        checkUser(baseConnectionParam);
    }

    protected void initClient(BaseConnectionParam baseConnectionParam) {
        this.dataSource = JdbcDataSourceProvider.createJdbcDataSource(baseConnectionParam);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    protected void checkUser(BaseConnectionParam baseConnectionParam) {
        if (StringUtils.isBlank(baseConnectionParam.getUser())) {
            setDefaultUsername(baseConnectionParam);
        }
        if (StringUtils.isBlank(baseConnectionParam.getPassword())) {
            setDefaultPassword(baseConnectionParam);
        }
    }

    protected void setDefaultUsername(BaseConnectionParam baseConnectionParam) {
        baseConnectionParam.setUser(COMMON_USER);
    }

    protected void setDefaultPassword(BaseConnectionParam baseConnectionParam) {
        baseConnectionParam.setPassword(COMMON_PASSWORD);
    }

    protected void checkValidationQuery(BaseConnectionParam baseConnectionParam) {
        if (StringUtils.isBlank(baseConnectionParam.getValidationQuery())) {
            setDefaultValidationQuery(baseConnectionParam);
        }
    }

    protected void setDefaultValidationQuery(BaseConnectionParam baseConnectionParam) {
        baseConnectionParam.setValidationQuery(COMMON_VALIDATION_QUERY);
    }

    @Override
    public void checkClient() {
        //Checking data source client
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            this.jdbcTemplate.execute(this.baseConnectionParam.getValidationQuery());
        } catch (Exception e) {
            throw new RuntimeException("JDBC connect failed", e);
        } finally {
            logger.info("Time to execute check jdbc client with sql {} for {} ms ", this.baseConnectionParam.getValidationQuery(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("get druidDataSource Connection fail SQLException: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void close() {
        logger.info("do close dataSource.");
        this.dataSource = null;
        this.jdbcTemplate = null;
    }

}
