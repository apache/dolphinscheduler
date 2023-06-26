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

package org.apache.dolphinscheduler.data.quality.flow.batch.writer;

import static org.apache.dolphinscheduler.data.quality.Constants.APPEND;
import static org.apache.dolphinscheduler.data.quality.Constants.DATABASE;
import static org.apache.dolphinscheduler.data.quality.Constants.DB_TABLE;
import static org.apache.dolphinscheduler.data.quality.Constants.DRIVER;
import static org.apache.dolphinscheduler.data.quality.Constants.JDBC;
import static org.apache.dolphinscheduler.data.quality.Constants.PASSWORD;
import static org.apache.dolphinscheduler.data.quality.Constants.SAVE_MODE;
import static org.apache.dolphinscheduler.data.quality.Constants.SQL;
import static org.apache.dolphinscheduler.data.quality.Constants.TABLE;
import static org.apache.dolphinscheduler.data.quality.Constants.URL;
import static org.apache.dolphinscheduler.data.quality.Constants.USER;

import org.apache.dolphinscheduler.data.quality.config.Config;
import org.apache.dolphinscheduler.data.quality.config.ValidateResult;
import org.apache.dolphinscheduler.data.quality.execution.SparkRuntimeEnvironment;
import org.apache.dolphinscheduler.data.quality.flow.batch.BatchWriter;
import org.apache.dolphinscheduler.data.quality.utils.ParserUtils;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.Arrays;

import com.google.common.base.Strings;

/**
 * JdbcWriter
 */
public class JdbcWriter implements BatchWriter {

    private final Config config;

    public JdbcWriter(Config config) {
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
        if (Strings.isNullOrEmpty(config.getString(SAVE_MODE))) {
            config.put(SAVE_MODE, APPEND);
        }
    }

    @Override
    public void write(Dataset<Row> data, SparkRuntimeEnvironment env) {
        if (!Strings.isNullOrEmpty(config.getString(SQL))) {
            data = env.sparkSession().sql(config.getString(SQL));
        }

        data.write()
                .format(JDBC)
                .option(DRIVER, config.getString(DRIVER))
                .option(URL, config.getString(URL))
                .option(DB_TABLE, config.getString(DATABASE) + "." + config.getString(TABLE))
                .option(USER, config.getString(USER))
                .option(PASSWORD, ParserUtils.decode(config.getString(PASSWORD)))
                .mode(config.getString(SAVE_MODE))
                .save();
    }
}
