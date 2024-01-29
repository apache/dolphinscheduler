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

package org.apache.dolphinscheduler.data.quality.flow.batch.reader;

import static org.apache.dolphinscheduler.data.quality.Constants.DATABASE;
import static org.apache.dolphinscheduler.data.quality.Constants.DB_TABLE;
import static org.apache.dolphinscheduler.data.quality.Constants.DOTS;
import static org.apache.dolphinscheduler.data.quality.Constants.DRIVER;
import static org.apache.dolphinscheduler.data.quality.Constants.JDBC;
import static org.apache.dolphinscheduler.data.quality.Constants.PASSWORD;
import static org.apache.dolphinscheduler.data.quality.Constants.TABLE;
import static org.apache.dolphinscheduler.data.quality.Constants.URL;
import static org.apache.dolphinscheduler.data.quality.Constants.USER;

import org.apache.dolphinscheduler.data.quality.config.Config;
import org.apache.dolphinscheduler.data.quality.config.ValidateResult;
import org.apache.dolphinscheduler.data.quality.execution.SparkRuntimeEnvironment;
import org.apache.dolphinscheduler.data.quality.flow.batch.BatchReader;
import org.apache.dolphinscheduler.data.quality.utils.ConfigUtils;
import org.apache.dolphinscheduler.data.quality.utils.ParserUtils;

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * AbstractJdbcSource
 */
public class JdbcReader implements BatchReader {

    private final Config config;

    public JdbcReader(Config config) {
        this.config = config;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public ValidateResult validateConfig() {
        return validate(Arrays.asList(URL, TABLE, USER, PASSWORD));
    }

    @Override
    public void prepare(SparkRuntimeEnvironment prepareEnv) {
        // Do nothing
    }

    @Override
    public Dataset<Row> read(SparkRuntimeEnvironment env) {
        return jdbcReader(env.sparkSession()).load();
    }

    private DataFrameReader jdbcReader(SparkSession sparkSession) {

        DataFrameReader reader = sparkSession.read()
                .format(JDBC)
                .option(URL, config.getString(URL))
                .option(DB_TABLE, config.getString(DATABASE) + "." + config.getString(TABLE))
                .option(USER, config.getString(USER))
                .option(PASSWORD, ParserUtils.decode(config.getString(PASSWORD)))
                .option(DRIVER, config.getString(DRIVER));

        Config jdbcConfig = ConfigUtils.extractSubConfig(config, JDBC + DOTS, false);

        if (!config.isEmpty()) {
            Map<String, String> optionMap = new HashMap<>(16);
            jdbcConfig.entrySet().forEach(x -> optionMap.put(x.getKey(), String.valueOf(x.getValue())));
            reader.options(optionMap);
        }

        return reader;
    }
}
