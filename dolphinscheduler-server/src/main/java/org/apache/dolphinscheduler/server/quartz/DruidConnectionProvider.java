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
package org.apache.dolphinscheduler.server.quartz;

import com.alibaba.druid.pool.DruidDataSource;
import org.quartz.SchedulerException;
import org.quartz.utils.ConnectionProvider;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * druid connection provider
 */
public class DruidConnectionProvider implements ConnectionProvider {

    /**
     * JDBC driver
     */
    public String driver;

    /**
     * JDBC URL
     */
    public String URL;

    /**
     * Database user name
     */
    public String user;

    /**
     * Database password
     */
    public String password;

    /**
     * Maximum number of database connections
     */
    public int maxConnections;

    /**
     * The query that validates the database connection
     */
    public String validationQuery;

    /**
     * Whether the database sql query to validate connections should be executed every time
     * a connection is retrieved from the pool to ensure that it is still valid.  If false,
     * then validation will occur on check-in.  Default is false.
     */
    private boolean validateOnCheckout;

    /**
     * The number of seconds between tests of idle connections - only enabled
     * if the validation query property is set.  Default is 50 seconds.
     */
    private int idleConnectionValidationSeconds;

    /**
     * The maximum number of prepared statements that will be cached per connection in the pool.
     * Depending upon your JDBC Driver this may significantly help performance, or may slightly
     * hinder performance.
     * Default is 120, as Quartz uses over 100 unique statements. 0 disables the feature.
     */
    public String maxCachedStatementsPerConnection;

    /**
     * Discard connections after they have been idle this many seconds.  0 disables the feature. Default is 0.
     */
    private String discardIdleConnectionsSeconds;

    /**
     * Default maximum number of database connections in the pool.
     */
    public static final int DEFAULT_DB_MAX_CONNECTIONS = 10;

    /**
     * The maximum number of prepared statements that will be cached per connection in the pool.
     */
    public static final int DEFAULT_DB_MAX_CACHED_STATEMENTS_PER_CONNECTION = 120;

    /**
     * Druid connection pool
     */
    private DruidDataSource datasource;

    /**
     * get connection
     * @return Connection
     * @throws SQLException sql exception
     */
    @Override
    public Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }

    /**
     * shutdown data source
     * @throws SQLException sql exception
     */
    @Override
    public void shutdown() throws SQLException {
        datasource.close();
    }

    /**
     * data source initialize
     * @throws SQLException sql exception
     */
    @Override
    public void initialize() throws SQLException{
        if (this.URL == null) {
            throw new SQLException("DBPool could not be created: DB URL cannot be null");
        }
        if (this.driver == null) {
            throw new SQLException("DBPool driver could not be created: DB driver class name cannot be null!");
        }
        if (this.maxConnections < 0) {
            throw new SQLException("DBPool maxConnectins could not be created: Max connections must be greater than zero!");
        }
        datasource = new DruidDataSource();
        try{
            datasource.setDriverClassName(this.driver);
        } catch (Exception e) {
            try {
                throw new SchedulerException("Problem setting driver class name on datasource", e);
            } catch (SchedulerException e1) {
            }
        }
        datasource.setUrl(this.URL);
        datasource.setUsername(this.user);
        datasource.setPassword(this.password);
        datasource.setMaxActive(this.maxConnections);
        datasource.setMinIdle(1);
        datasource.setMaxWait(0);
        datasource.setMaxPoolPreparedStatementPerConnectionSize(DEFAULT_DB_MAX_CONNECTIONS);
        if (this.validationQuery != null) {
            datasource.setValidationQuery(this.validationQuery);
            if(!this.validateOnCheckout){
                datasource.setTestOnReturn(true);
            } else {
                datasource.setTestOnBorrow(true);
            }
            datasource.setValidationQueryTimeout(this.idleConnectionValidationSeconds);
        }
    }

    public String getDriver() {
        return driver;
    }
    public void setDriver(String driver) {
        this.driver = driver;
    }
    public String getURL() {
        return URL;
    }
    public void setURL(String URL) {
        this.URL = URL;
    }
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
    public int getMaxConnections() {
        return maxConnections;
    }
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    public String getValidationQuery() {
        return validationQuery;
    }
    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }
    public boolean isValidateOnCheckout() {
        return validateOnCheckout;
    }
    public void setValidateOnCheckout(boolean validateOnCheckout) {
        this.validateOnCheckout = validateOnCheckout;
    }
    public int getIdleConnectionValidationSeconds() {
        return idleConnectionValidationSeconds;
    }
    public void setIdleConnectionValidationSeconds(int idleConnectionValidationSeconds) {
        this.idleConnectionValidationSeconds = idleConnectionValidationSeconds;
    }
    public DruidDataSource getDatasource() {
        return datasource;
    }
    public void setDatasource(DruidDataSource datasource) {
        this.datasource = datasource;
    }
    public String getDiscardIdleConnectionsSeconds() {
        return discardIdleConnectionsSeconds;
    }
    public void setDiscardIdleConnectionsSeconds(String discardIdleConnectionsSeconds) {
        this.discardIdleConnectionsSeconds = discardIdleConnectionsSeconds;
    }
}
