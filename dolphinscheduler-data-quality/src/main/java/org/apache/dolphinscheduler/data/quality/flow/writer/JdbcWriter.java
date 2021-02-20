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

package org.apache.dolphinscheduler.data.quality.flow.writer;

import static org.apache.dolphinscheduler.data.quality.Constants.EMPTY;
import static org.apache.dolphinscheduler.data.quality.Constants.SQL;

import org.apache.dolphinscheduler.data.quality.configuration.WriterParameter;
import org.apache.dolphinscheduler.data.quality.flow.JdbcBaseConfig;
import org.apache.dolphinscheduler.data.quality.utils.JdbcUtil;
import org.apache.dolphinscheduler.data.quality.utils.Preconditions;

import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.util.Map;

/**
 * JdbcWriter
 */
public class JdbcWriter implements IWriter {

    private final SparkSession sparkSession;

    private final WriterParameter writerParam;

    public JdbcWriter(SparkSession sparkSession, WriterParameter writerParam) {
        this.sparkSession = sparkSession;
        this.writerParam = writerParam;
    }

    @Override
    public void execute() {

        Map<String,Object> config = writerParam.getConfig();
        JdbcBaseConfig jdbcBaseConfig = new JdbcBaseConfig(config);
        String sql = String.valueOf(config.getOrDefault(SQL,EMPTY));

        Preconditions.checkArgument(JdbcUtil.isJdbcDriverLoaded(jdbcBaseConfig.getDriver()), "JDBC driver $driver not present in classpath");

        sparkSession.sql(sql)
                .write()
                .format("jdbc")
                .option("driver",jdbcBaseConfig.getDriver())
                .option("url",jdbcBaseConfig.getUrl())
                .option("dbtable", jdbcBaseConfig.getDbTable())
                .option("user", jdbcBaseConfig.getUser())
                .option("password", jdbcBaseConfig.getPassword())
                .mode(SaveMode.Append)
                .save();
    }

}
