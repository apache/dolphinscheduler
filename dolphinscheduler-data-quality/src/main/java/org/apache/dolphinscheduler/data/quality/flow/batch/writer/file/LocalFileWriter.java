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

package org.apache.dolphinscheduler.data.quality.flow.batch.writer.file;

import org.apache.dolphinscheduler.data.quality.config.Config;
import org.apache.dolphinscheduler.data.quality.config.ValidateResult;
import org.apache.dolphinscheduler.data.quality.execution.SparkRuntimeEnvironment;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.Collections;

/**
 * LocalFileWriter
 */
public class LocalFileWriter extends BaseFileWriter {

    public LocalFileWriter(Config config) {
        super(config);
    }

    @Override
    public void write(Dataset<Row> data, SparkRuntimeEnvironment environment) {
        outputImpl(data, "file://");
    }

    @Override
    public ValidateResult validateConfig() {
        return checkConfigImpl(Collections.singletonList("file://"));
    }
}
