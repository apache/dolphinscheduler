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

package org.apache.dolphinscheduler.data.quality.flow.connector;

import org.apache.dolphinscheduler.data.quality.configuration.ConnectorParameter;
import org.apache.dolphinscheduler.data.quality.flow.JdbcBaseConfig;
import org.apache.dolphinscheduler.data.quality.utils.JdbcUtil;
import org.apache.dolphinscheduler.data.quality.utils.Preconditions;

import org.apache.spark.sql.SparkSession;

import java.util.Map;

/**
 * JdbcConnector
 */
public class JdbcConnector implements IConnector {

    private final SparkSession sparkSession;

    private final ConnectorParameter connectorParameter;

    public JdbcConnector(SparkSession sparkSession, ConnectorParameter connectorParameter) {
        this.sparkSession = sparkSession;
        this.connectorParameter = connectorParameter;
    }

    @Override
    public void execute() {

        Map<String,Object> config = connectorParameter.getConfig();
        JdbcBaseConfig jdbcBaseConfig = new JdbcBaseConfig(config);

        Preconditions.checkArgument(JdbcUtil.isJdbcDriverLoaded(jdbcBaseConfig.getDriver()), "JDBC driver $driver not present in classpath");

        sparkSession
                .read()
                .format("jdbc")
                .option("driver",jdbcBaseConfig.getDriver())
                .option("url",jdbcBaseConfig.getUrl())
                .option("dbtable", jdbcBaseConfig.getDbTable())
                .option("user", jdbcBaseConfig.getUser())
                .option("password", jdbcBaseConfig.getPassword())
                .load().createOrReplaceTempView(jdbcBaseConfig.getTable());
    }
}
