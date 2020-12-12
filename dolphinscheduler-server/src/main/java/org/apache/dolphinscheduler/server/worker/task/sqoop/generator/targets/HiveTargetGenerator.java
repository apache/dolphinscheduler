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

package org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetHiveParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.SqoopConstants;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ITargetGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hive target generator
 */
public class HiveTargetGenerator implements ITargetGenerator {

    private static final Logger logger = LoggerFactory.getLogger(HiveTargetGenerator.class);

    @Override
    public String generate(SqoopParameters sqoopParameters, TaskExecutionContext taskExecutionContext) {

        StringBuilder hiveTargetSb = new StringBuilder();

        try {
            TargetHiveParameter targetHiveParameter =
                JSONUtils.parseObject(sqoopParameters.getTargetParams(), TargetHiveParameter.class);
            if (null != targetHiveParameter) {
                hiveTargetSb.append(Constants.SPACE).append(SqoopConstants.HIVE_IMPORT);

                if (StringUtils.isNotEmpty(targetHiveParameter.getHiveDatabase())
                    && StringUtils.isNotEmpty(targetHiveParameter.getHiveTable())) {
                    hiveTargetSb.append(Constants.SPACE).append(SqoopConstants.HIVE_DATABASE)
                        .append(Constants.SPACE).append(targetHiveParameter.getHiveDatabase())
                        .append(Constants.SPACE).append(SqoopConstants.HIVE_TABLE)
                        .append(Constants.SPACE).append(targetHiveParameter.getHiveTable());
                }

                if (targetHiveParameter.isCreateHiveTable()) {
                    hiveTargetSb.append(Constants.SPACE).append(SqoopConstants.CREATE_HIVE_TABLE);
                }

                if (targetHiveParameter.isDropDelimiter()) {
                    hiveTargetSb.append(Constants.SPACE).append(SqoopConstants.HIVE_DROP_IMPORT_DELIMS);
                }

                if (targetHiveParameter.isHiveOverWrite()) {
                    hiveTargetSb.append(Constants.SPACE).append(SqoopConstants.HIVE_OVERWRITE)
                        .append(Constants.SPACE).append(SqoopConstants.DELETE_TARGET_DIR);
                }

                if (StringUtils.isNotEmpty(targetHiveParameter.getHiveTargetDir())) {
                    hiveTargetSb.append(Constants.SPACE).append(SqoopConstants.TARGET_DIR)
                            .append(Constants.SPACE).append(targetHiveParameter.getHiveTargetDir());
                }

                if (StringUtils.isNotEmpty(targetHiveParameter.getReplaceDelimiter())) {
                    hiveTargetSb.append(Constants.SPACE).append(SqoopConstants.HIVE_DELIMS_REPLACEMENT)
                        .append(Constants.SPACE).append(targetHiveParameter.getReplaceDelimiter());
                }

                if (StringUtils.isNotEmpty(targetHiveParameter.getHivePartitionKey())
                    && StringUtils.isNotEmpty(targetHiveParameter.getHivePartitionValue())) {
                    hiveTargetSb.append(Constants.SPACE).append(SqoopConstants.HIVE_PARTITION_KEY)
                        .append(Constants.SPACE).append(targetHiveParameter.getHivePartitionKey())
                        .append(Constants.SPACE).append(SqoopConstants.HIVE_PARTITION_VALUE)
                        .append(Constants.SPACE).append(targetHiveParameter.getHivePartitionValue());
                }

            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop hive target params build failed: [%s]", e.getMessage()));
        }

        return hiveTargetSb.toString();
    }
}
