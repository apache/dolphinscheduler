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

import org.apache.dolphinscheduler.data.quality.configuration.DataQualityConfiguration;
import org.apache.dolphinscheduler.data.quality.context.DataQualityContext;
import org.apache.dolphinscheduler.data.quality.flow.DataQualityTask;
import org.apache.dolphinscheduler.data.quality.flow.connector.ConnectorFactory;
import org.apache.dolphinscheduler.data.quality.flow.executor.SparkSqlExecuteTask;
import org.apache.dolphinscheduler.data.quality.flow.writer.WriterFactory;
import org.apache.dolphinscheduler.data.quality.utils.JsonUtil;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataQualityApplication
 */
public class DataQualityApplication {

    private static final Logger logger = LoggerFactory.getLogger(DataQualityApplication.class);

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            logger.error("can not find DataQualityConfiguration");
            System.exit(-1);
        }

        String dataQualityParameter = args[0];
        logger.info("DataQualityParameter is {}" + dataQualityParameter);

        DataQualityConfiguration dataQualityConfiguration = JsonUtil.fromJson(dataQualityParameter,DataQualityConfiguration.class);
        if (dataQualityConfiguration == null) {
            logger.info("DataQualityConfiguration is null");
            System.exit(-1);
        } else {
            dataQualityConfiguration.validate();
        }

        SparkConf conf = new SparkConf().setAppName(dataQualityConfiguration.getName());
        conf.set("spark.sql.crossJoin.enabled", "true");
        SparkSession sparkSession = SparkSession.builder().config(conf).enableHiveSupport().getOrCreate();

        DataQualityContext context = new DataQualityContext(
                sparkSession,
                dataQualityConfiguration.getConnectorParameters(),
                dataQualityConfiguration.getExecutorParameters(),
                dataQualityConfiguration.getWriterParams());

        execute(buildDataQualityFlow(context));
        sparkSession.stop();
    }

    private static List<DataQualityTask> buildDataQualityFlow(DataQualityContext context) throws Exception {
        List<DataQualityTask> taskList =
                new ArrayList<>(ConnectorFactory.getInstance().getConnectors(context));
        taskList.add(new SparkSqlExecuteTask(context.getSparkSession(),context.getExecutorParameterList()));
        taskList.addAll(WriterFactory.getInstance().getWriters(context));

        return taskList;
    }

    private static void execute(List<DataQualityTask> taskList) {
        for (DataQualityTask task: taskList) {
            task.execute();
        }
    }
}
