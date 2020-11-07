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

import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetHiveParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.SqoopConstants;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ITargetGenerator;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hive target generator
 */
public class HiveTargetGenerator implements ITargetGenerator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters, TaskExecutionContext taskExecutionContext) {

        LinkedList<String> hiveTargetParamsList = new LinkedList<>();

        try {
            TargetHiveParameter targetHiveParameter =
                JSONUtils.parseObject(sqoopParameters.getTargetParams(), TargetHiveParameter.class);
            if (null != targetHiveParameter) {

                hiveTargetParamsList.add(SqoopConstants.HIVE_IMPORT);

                if (StringUtils.isNotEmpty(targetHiveParameter.getHiveDatabase())
                    && StringUtils.isNotEmpty(targetHiveParameter.getHiveTable())) {
                    hiveTargetParamsList.add(SqoopConstants.HIVE_TABLE);
                    hiveTargetParamsList.add(String.format("%s.%s", targetHiveParameter.getHiveDatabase(),
                        targetHiveParameter.getHiveTable()));
                }

                if (targetHiveParameter.isCreateHiveTable()) {
                    hiveTargetParamsList.add(SqoopConstants.CREATE_HIVE_TABLE);
                }

                if (targetHiveParameter.isDropDelimiter()) {
                    hiveTargetParamsList.add(SqoopConstants.HIVE_DROP_IMPORT_DELIMS);
                }

                if (targetHiveParameter.isHiveOverWrite()) {
                    hiveTargetParamsList.add(SqoopConstants.HIVE_OVERWRITE);
                    hiveTargetParamsList.add(SqoopConstants.DELETE_TARGET_DIR);
                }

                if (StringUtils.isNotEmpty(targetHiveParameter.getReplaceDelimiter())) {
                    hiveTargetParamsList.add(SqoopConstants.HIVE_DELIMS_REPLACEMENT);
                    hiveTargetParamsList.add(targetHiveParameter.getReplaceDelimiter());
                }

                if (StringUtils.isNotEmpty(targetHiveParameter.getHivePartitionKey())
                    && StringUtils.isNotEmpty(targetHiveParameter.getHivePartitionValue())) {
                    hiveTargetParamsList.add(SqoopConstants.HIVE_PARTITION_KEY);
                    hiveTargetParamsList.add(targetHiveParameter.getHivePartitionKey());
                    hiveTargetParamsList.add(SqoopConstants.HIVE_PARTITION_VALUE);
                    hiveTargetParamsList.add(targetHiveParameter.getHivePartitionValue());
                }

            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop hive target params build failed: [%s]", e.getMessage()));
        }

        return String.join(" ", hiveTargetParamsList);
    }
}
