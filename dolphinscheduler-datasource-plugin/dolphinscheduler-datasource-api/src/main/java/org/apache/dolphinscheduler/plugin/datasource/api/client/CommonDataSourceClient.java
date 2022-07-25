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

import com.google.common.collect.Lists;

import org.apache.commons.lang3.tuple.MutableTriple;

import org.apache.dolphinscheduler.plugin.datasource.api.provider.ConnectionCallbackWithSqlProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.provider.JDBCDataSourceProvider;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DataSourceClient;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.base.Stopwatch;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedCaseInsensitiveMap;

public class CommonDataSourceClient implements DataSourceClient {

    private static final Logger logger = LoggerFactory.getLogger(CommonDataSourceClient.class);

    public static final String COMMON_USER = "root";
    public static final String COMMON_VALIDATION_QUERY = "select 1";

    protected final BaseConnectionParam baseConnectionParam;
    protected HikariDataSource dataSource;
    protected JdbcTemplate jdbcTemplate;

    public CommonDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        this.baseConnectionParam = baseConnectionParam;
        preInit();
        checkEnv(baseConnectionParam);
        initClient(baseConnectionParam, dbType);
        checkClient();
    }

    protected void preInit() {
        logger.info("preInit in CommonDataSourceClient");
    }

    protected void checkEnv(BaseConnectionParam baseConnectionParam) {
        checkValidationQuery(baseConnectionParam);
        checkUser(baseConnectionParam);
    }

    protected void initClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        this.dataSource = JDBCDataSourceProvider.createJdbcDataSource(baseConnectionParam, dbType);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    protected void checkUser(BaseConnectionParam baseConnectionParam) {
        if (StringUtils.isBlank(baseConnectionParam.getUser())) {
            setDefaultUsername(baseConnectionParam);
        }
    }

    protected void setDefaultUsername(BaseConnectionParam baseConnectionParam) {
        baseConnectionParam.setUser(COMMON_USER);
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
    public List<String> getDatabaseList(String databasePattern) {
        return this.jdbcTemplate.queryForList(getDatabaseListSql(databasePattern), String.class);
    }

    protected String getDatabaseListSql(String databasePattern) {
        throw new UnsupportedOperationException("NOT_SUPPORT");
    }

    @Override
    public List<String> getTableList(String dbName, String schemaName, String tablePattern) {
        return this.jdbcTemplate.queryForList(getTableListSql(dbName, schemaName, tablePattern), String.class);
    }

    protected String getTableListSql(String dbName, String schemaName, String tablePattern) {
        throw new UnsupportedOperationException("NOT_SUPPORT");
    }

    @Override
    public List<Map<String, Object>> getTableStruct(String dbName, String schemaName, String tableName) {
        throw new UnsupportedOperationException("NOT_SUPPORT");
    }

    @Override
    public MutableTriple<Map<String, String>, List<Map<String, Object>>, List<Map<String, String>>> executeSql(
        String dbName, String schemaName, Boolean oneSession, String querySql) {
        List<String> sqlList = assemblingSql(dbName, schemaName, querySql);
        JdbcTemplate jdbcTemplate = getJdbcTemplate(oneSession);
        return executeSqlListReturnLast(jdbcTemplate, sqlList);
    }

    protected JdbcTemplate getJdbcTemplate(Boolean oneSession) {
        return this.jdbcTemplate;
    }

    protected String switchEnvironment(String dbName, String schemaName) {
        return null;
    }

    private List<String> assemblingSql(String dbName, String schemaName, String querySql) {
        return Lists.asList(switchEnvironment(dbName, schemaName), querySql.split(";")).stream()
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    }

    private String getColumnName(String columnName) {
        return columnName.contains(".") ? columnName.split("\\.", 2)[1] : columnName;
    }

    protected MutableTriple<Map<String, String>, List<Map<String, Object>>, List<Map<String, String>>>
    executeSqlListReturnLast(JdbcTemplate jdbcTemplate, List<String> querySqlList) {
        Stopwatch stopwatchForAll = Stopwatch.createStarted();
        try {
            return jdbcTemplate.execute(
                new ConnectionCallbackWithSqlProvider<
                    MutableTriple<Map<String, String>, List<Map<String, Object>>, List<Map<String, String>>>>() {

                    private String currentSql;

                    @Override
                    public String getSql() {
                        return currentSql;
                    }

                    @Override
                    public MutableTriple<Map<String, String>, List<Map<String, Object>>, List<Map<String, String>>>
                    doInConnection(@NonNull Connection con) throws SQLException, DataAccessException {
                        MutableTriple<Map<String, String>, List<Map<String, Object>>, List<Map<String, String>>> mutableTriple =
                            new MutableTriple<>();
                        Map<String, String> rsMeta;
                        List<Map<String, Object>> rsData;
                        List<Map<String, String>> rsCols;
                        ColumnMapRowMapper columnMapRowMapper =
                            new ColumnMapRowMapper() {
                                @SuppressWarnings("NullableProblems")
                                @Override
                                protected String getColumnKey(String columnName) {
                                    return getColumnName(columnName);
                                }

                                @Override
                                protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
                                    Object object;
                                    if (rs.getMetaData().getColumnType(index) == Types.OTHER
                                        || rs.getMetaData().getColumnType(index) == Types.ARRAY
                                        || rs.getMetaData().getColumnType(index) == Types.BIT) {
                                        object = rs.getString(index);
                                    } else {
                                        object = super.getColumnValue(rs, index);
                                    }
                                    return object;
                                }
                            };
                        RowMapperResultSetExtractor<Map<String, Object>> rowMapperResultSetExtractor =
                            new RowMapperResultSetExtractor<>(columnMapRowMapper);
                        ResultSet rs = null;
                        for (int i = 0; i < querySqlList.size(); i++) {
                            currentSql = querySqlList.get(i);
                            Stopwatch stopwatchForOne = Stopwatch.createStarted();
                            try (Statement stmt = con.createStatement()) {
                                boolean retRs = stmt.execute(querySqlList.get(i));
                                if ((i == querySqlList.size() - 1) && retRs) {
                                    rs = stmt.getResultSet();
                                    ResultSetMetaData rsmd = rs.getMetaData();
                                    int columnCount = rsmd.getColumnCount();
                                    rsMeta = new LinkedCaseInsensitiveMap<>(columnCount);
                                    rsCols = new ArrayList<>(columnCount);
                                    for (int j = 1; j <= columnCount; j++) {
                                        String column = JdbcUtils.lookupColumnName(rsmd, j);
                                        rsMeta.putIfAbsent(getColumnName(column), rsmd.getColumnTypeName(j));
                                        rsCols.add(Collections.singletonMap(getColumnName(column), rsmd.getColumnTypeName(j)));
                                    }
                                    rsData = rowMapperResultSetExtractor.extractData(rs);
                                    mutableTriple.setLeft(rsMeta);
                                    mutableTriple.setMiddle(rsData);
                                    mutableTriple.setRight(rsCols);
                                }
                            } finally {
                                JdbcUtils.closeResultSet(rs);
                                logger.info(
                                    "Time to execute sql [{}] for {} ms ",
                                    querySqlList.get(i),
                                    stopwatchForOne.elapsed(TimeUnit.MILLISECONDS));
                            }
                        }
                        return mutableTriple;
                    }
                });
        } finally {
            logger.info(
                "Time to execute sql list\n {} \n for {} ms ", querySqlList, stopwatchForAll.elapsed(TimeUnit.MILLISECONDS));
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
        logger.info("do close dataSource {}.", baseConnectionParam.getDatabase());
        try (HikariDataSource closedDatasource = dataSource) {
            // only close the resource
        }
        this.jdbcTemplate = null;
    }

}
