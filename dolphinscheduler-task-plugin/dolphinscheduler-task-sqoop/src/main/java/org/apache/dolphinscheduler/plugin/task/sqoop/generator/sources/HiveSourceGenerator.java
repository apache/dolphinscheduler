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

package org.apache.dolphinscheduler.plugin.task.sqoop.generator.sources;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SPACE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HCATALOG_DATABASE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HCATALOG_PARTITION_KEYS;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HCATALOG_PARTITION_VALUES;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HCATALOG_TABLE;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.sqoop.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.sqoop.generator.ISourceGenerator;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.SqoopParameters;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.sources.SourceHiveParameter;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * hive source generator
 */
@Slf4j
public class HiveSourceGenerator implements ISourceGenerator {

    @Override
    public String generate(SqoopParameters sqoopParameters, SqoopTaskExecutionContext sqoopTaskExecutionContext) {

        StringBuilder hiveSourceSb = new StringBuilder();

        try {
            SourceHiveParameter sourceHiveParameter =
                    JSONUtils.parseObject(sqoopParameters.getSourceParams(), SourceHiveParameter.class);

            if (null != sourceHiveParameter) {
                if (StringUtils.isNotEmpty(sourceHiveParameter.getHiveDatabase())) {
                    hiveSourceSb.append(SPACE).append(HCATALOG_DATABASE)
                            .append(SPACE).append(sourceHiveParameter.getHiveDatabase());
                }

                if (StringUtils.isNotEmpty(sourceHiveParameter.getHiveTable())) {
                    hiveSourceSb.append(SPACE).append(HCATALOG_TABLE)
                            .append(SPACE).append(sourceHiveParameter.getHiveTable());
                }

                if (StringUtils.isNotEmpty(sourceHiveParameter.getHivePartitionKey())
                        && StringUtils.isNotEmpty(sourceHiveParameter.getHivePartitionValue())) {
                    hiveSourceSb.append(SPACE).append(HCATALOG_PARTITION_KEYS)
                            .append(SPACE).append(sourceHiveParameter.getHivePartitionKey())
                            .append(SPACE).append(HCATALOG_PARTITION_VALUES)
                            .append(SPACE).append(sourceHiveParameter.getHivePartitionValue());
                }
            }
        } catch (Exception e) {
            log.error(String.format("Sqoop hive source params build failed: [%s]", e.getMessage()));
        }

        return hiveSourceSb.toString();
    }
}
