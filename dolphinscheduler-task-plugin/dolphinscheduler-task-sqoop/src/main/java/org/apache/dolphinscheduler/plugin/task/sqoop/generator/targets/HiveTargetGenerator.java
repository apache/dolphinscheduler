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

package org.apache.dolphinscheduler.plugin.task.sqoop.generator.targets;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SPACE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.CREATE_HIVE_TABLE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.DELETE_TARGET_DIR;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HIVE_DATABASE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HIVE_DELIMS_REPLACEMENT;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HIVE_DROP_IMPORT_DELIMS;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HIVE_IMPORT;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HIVE_OVERWRITE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HIVE_PARTITION_KEY;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HIVE_PARTITION_VALUE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HIVE_TABLE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.TARGET_DIR;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.sqoop.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.sqoop.generator.ITargetGenerator;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.SqoopParameters;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.targets.TargetHiveParameter;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * hive target generator
 */
@Slf4j
public class HiveTargetGenerator implements ITargetGenerator {

    @Override
    public String generate(SqoopParameters sqoopParameters, SqoopTaskExecutionContext sqoopTaskExecutionContext) {

        StringBuilder hiveTargetSb = new StringBuilder();

        try {
            TargetHiveParameter targetHiveParameter =
                    JSONUtils.parseObject(sqoopParameters.getTargetParams(), TargetHiveParameter.class);
            if (null != targetHiveParameter) {
                hiveTargetSb.append(SPACE).append(HIVE_IMPORT);

                if (StringUtils.isNotEmpty(targetHiveParameter.getHiveDatabase())
                        && StringUtils.isNotEmpty(targetHiveParameter.getHiveTable())) {
                    hiveTargetSb.append(SPACE).append(HIVE_DATABASE)
                            .append(SPACE).append(targetHiveParameter.getHiveDatabase())
                            .append(SPACE).append(HIVE_TABLE)
                            .append(SPACE).append(targetHiveParameter.getHiveTable());
                }

                if (targetHiveParameter.isCreateHiveTable()) {
                    hiveTargetSb.append(SPACE).append(CREATE_HIVE_TABLE);
                }

                if (targetHiveParameter.isDropDelimiter()) {
                    hiveTargetSb.append(SPACE).append(HIVE_DROP_IMPORT_DELIMS);
                }

                if (targetHiveParameter.isHiveOverWrite()) {
                    hiveTargetSb.append(SPACE).append(HIVE_OVERWRITE)
                            .append(SPACE).append(DELETE_TARGET_DIR);
                }

                if (StringUtils.isNotEmpty(targetHiveParameter.getReplaceDelimiter())) {
                    hiveTargetSb.append(SPACE).append(HIVE_DELIMS_REPLACEMENT)
                            .append(SPACE).append(targetHiveParameter.getReplaceDelimiter());
                }

                if (StringUtils.isNotEmpty(targetHiveParameter.getHivePartitionKey())
                        && StringUtils.isNotEmpty(targetHiveParameter.getHivePartitionValue())) {
                    hiveTargetSb.append(SPACE).append(HIVE_PARTITION_KEY)
                            .append(SPACE).append(targetHiveParameter.getHivePartitionKey())
                            .append(SPACE).append(HIVE_PARTITION_VALUE)
                            .append(SPACE).append(targetHiveParameter.getHivePartitionValue());
                }

                if (StringUtils.isNotEmpty(targetHiveParameter.getHiveTargetDir())) {
                    hiveTargetSb.append(SPACE).append(TARGET_DIR)
                            .append(SPACE).append(targetHiveParameter.getHiveTargetDir());
                }

            }
        } catch (Exception e) {
            log.error(String.format("Sqoop hive target params build failed: [%s]", e.getMessage()));
        }

        return hiveTargetSb.toString();
    }
}
