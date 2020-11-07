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

package org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources;

import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceHiveParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.SqoopConstants;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ISourceGenerator;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hive source generator
 */
public class HiveSourceGenerator implements ISourceGenerator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters, TaskExecutionContext taskExecutionContext) {

        LinkedList<String> hiveSourceParamsList = new LinkedList<>();

        try {
            SourceHiveParameter sourceHiveParameter
                = JSONUtils.parseObject(sqoopParameters.getSourceParams(), SourceHiveParameter.class);

            if (null != sourceHiveParameter) {
                if (StringUtils.isNotEmpty(sourceHiveParameter.getHiveDatabase())) {
                    hiveSourceParamsList.add(SqoopConstants.HCATALOG_DATABASE);
                    hiveSourceParamsList.add(sourceHiveParameter.getHiveDatabase());
                }

                if (StringUtils.isNotEmpty(sourceHiveParameter.getHiveTable())) {
                    hiveSourceParamsList.add(SqoopConstants.HCATALOG_TABLE);
                    hiveSourceParamsList.add(sourceHiveParameter.getHiveTable());
                }

                if (StringUtils.isNotEmpty(sourceHiveParameter.getHivePartitionKey())
                    && StringUtils.isNotEmpty(sourceHiveParameter.getHivePartitionValue())) {
                    hiveSourceParamsList.add(SqoopConstants.HCATALOG_PARTITION_KEYS);
                    hiveSourceParamsList.add(sourceHiveParameter.getHivePartitionKey());
                    hiveSourceParamsList.add(SqoopConstants.HCATALOG_PARTITION_VALUES);
                    hiveSourceParamsList.add(sourceHiveParameter.getHivePartitionValue());
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop hive source params build failed: [%s]", e.getMessage()));
        }

        return String.join(" ", hiveSourceParamsList);
    }
}
