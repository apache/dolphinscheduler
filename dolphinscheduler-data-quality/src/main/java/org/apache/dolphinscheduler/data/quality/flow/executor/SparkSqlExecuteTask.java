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

package org.apache.dolphinscheduler.data.quality.flow.executor;

import org.apache.dolphinscheduler.data.quality.configuration.ExecutorParameter;
import org.apache.dolphinscheduler.data.quality.flow.DataQualityTask;
import org.apache.dolphinscheduler.data.quality.utils.StringUtils;

import org.apache.spark.sql.SparkSession;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SparkSqlExecuteTask
 */
public class SparkSqlExecuteTask implements DataQualityTask {

    private static final Logger logger = LoggerFactory.getLogger(SparkSqlExecuteTask.class);

    private final SparkSession sparkSession;

    private final List<ExecutorParameter> executorParameterList;

    public SparkSqlExecuteTask(SparkSession sparkSession,List<ExecutorParameter> executorParameterList) {
        this.sparkSession = sparkSession;
        this.executorParameterList = executorParameterList;
    }

    @Override
    public void execute() {
        for (ExecutorParameter executorParameter : executorParameterList) {
            if (StringUtils.isNotEmpty(executorParameter.getTableAlias())) {
                sparkSession
                        .sql(executorParameter.getExecuteSql())
                        .createOrReplaceTempView(executorParameter.getTableAlias());
            } else {
                logger.error("lost table alias");
            }
        }
    }
}
