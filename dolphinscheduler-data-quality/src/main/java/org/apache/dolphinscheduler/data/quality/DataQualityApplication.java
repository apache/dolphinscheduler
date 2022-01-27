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

package org.apache.dolphinscheduler.data.quality;

import static org.apache.dolphinscheduler.data.quality.Constants.SPARK_APP_NAME;

import org.apache.dolphinscheduler.data.quality.config.Config;
import org.apache.dolphinscheduler.data.quality.config.DataQualityConfiguration;
import org.apache.dolphinscheduler.data.quality.config.EnvConfig;
import org.apache.dolphinscheduler.data.quality.context.DataQualityContext;
import org.apache.dolphinscheduler.data.quality.execution.SparkRuntimeEnvironment;
import org.apache.dolphinscheduler.data.quality.utils.JsonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * DataQualityApplication is spark application.
 * It mainly includes three components: reader, transformer and writer.
 * These three components realize the functions of connecting data, executing intermediate SQL
 * and writing execution results and error data to the specified storage engine
 */
public class DataQualityApplication {

    private static final Logger logger = LoggerFactory.getLogger(DataQualityApplication.class);

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            logger.error("Can not find DataQualityConfiguration");
            System.exit(-1);
        }

        String dataQualityParameter = args[0];

        DataQualityConfiguration dataQualityConfiguration = JsonUtils.fromJson(dataQualityParameter,DataQualityConfiguration.class);
        if (dataQualityConfiguration == null) {
            logger.info("DataQualityConfiguration is null");
            System.exit(-1);
        } else {
            dataQualityConfiguration.validate();
        }

        EnvConfig envConfig = dataQualityConfiguration.getEnvConfig();
        Config config = new Config(envConfig.getConfig());
        config.put("type",envConfig.getType());
        if (Strings.isNullOrEmpty(config.getString(SPARK_APP_NAME))) {
            config.put(SPARK_APP_NAME,dataQualityConfiguration.getName());
        }

        SparkRuntimeEnvironment sparkRuntimeEnvironment = new SparkRuntimeEnvironment(config);
        DataQualityContext dataQualityContext = new DataQualityContext(sparkRuntimeEnvironment,dataQualityConfiguration);
        dataQualityContext.execute();
    }
}
