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

package org.apache.dolphinscheduler.plugin.datasource.redshift;

import org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient;
import org.apache.dolphinscheduler.plugin.datasource.redshift.param.RedshiftAuthMode;
import org.apache.dolphinscheduler.plugin.datasource.redshift.param.RedshiftConnectionParam;
import org.apache.dolphinscheduler.plugin.datasource.redshift.param.RedshiftDataSourceProcessor;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Stopwatch;

@Slf4j
public class RedshiftDataSourceClient extends CommonDataSourceClient {

    public RedshiftDataSourceClient(BaseConnectionParam baseConnectionParam, DbType dbType) {
        super(baseConnectionParam, dbType);
    }

    @Override
    public Connection getConnection() {
        RedshiftConnectionParam connectionParam = (RedshiftConnectionParam) this.baseConnectionParam;
        if (connectionParam.getMode().equals(RedshiftAuthMode.PASSWORD)) {
            return super.getConnection();
        }
        return RedshiftDataSourceProcessor.getConnectionByIAM(connectionParam);
    }

    @Override
    public void checkClient() {
        RedshiftConnectionParam connectionParam = (RedshiftConnectionParam) this.baseConnectionParam;
        Stopwatch stopwatch = Stopwatch.createStarted();
        String validationQuery = this.baseConnectionParam.getValidationQuery();
        if (connectionParam.getMode().equals(RedshiftAuthMode.PASSWORD)) {
            // Checking data source client
            try {
                this.jdbcTemplate.execute(validationQuery);
            } catch (Exception e) {
                throw new RuntimeException("JDBC connect failed", e);
            } finally {
                log.info("Time to execute check jdbc client with sql {} for {} ms ",
                        this.baseConnectionParam.getValidationQuery(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        } else {
            try (Statement statement = getConnection().createStatement()) {
                if (!statement.execute(validationQuery)) {
                    throw new SQLException("execute check redshift access key failed : " + validationQuery);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                log.info("Time to execute check redshift access key with sql {} for {} ms ",
                        this.baseConnectionParam.getValidationQuery(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }
    }
}
