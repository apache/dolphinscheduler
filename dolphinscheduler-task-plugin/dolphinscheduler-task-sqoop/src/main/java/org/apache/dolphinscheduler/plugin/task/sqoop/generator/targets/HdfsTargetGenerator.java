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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SINGLE_QUOTES;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SPACE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.COMPRESSION_CODEC;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.DELETE_TARGET_DIR;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.FIELDS_TERMINATED_BY;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.FIELD_NULL_PLACEHOLDER;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.LINES_TERMINATED_BY;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.TARGET_DIR;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.sqoop.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.sqoop.generator.ITargetGenerator;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.SqoopParameters;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.targets.TargetHdfsParameter;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * hdfs target generator
 */
@Slf4j
public class HdfsTargetGenerator implements ITargetGenerator {

    @Override
    public String generate(SqoopParameters sqoopParameters, SqoopTaskExecutionContext sqoopTaskExecutionContext) {

        StringBuilder hdfsTargetSb = new StringBuilder();

        try {
            TargetHdfsParameter targetHdfsParameter =
                    JSONUtils.parseObject(sqoopParameters.getTargetParams(), TargetHdfsParameter.class);

            if (null != targetHdfsParameter) {

                if (StringUtils.isNotEmpty(targetHdfsParameter.getTargetPath())) {
                    hdfsTargetSb.append(SPACE).append(TARGET_DIR)
                            .append(SPACE).append(targetHdfsParameter.getTargetPath());
                }

                if (StringUtils.isNotEmpty(targetHdfsParameter.getCompressionCodec())) {
                    hdfsTargetSb.append(SPACE).append(COMPRESSION_CODEC)
                            .append(SPACE).append(targetHdfsParameter.getCompressionCodec());
                }

                if (StringUtils.isNotEmpty(targetHdfsParameter.getFileType())) {
                    hdfsTargetSb.append(SPACE).append(targetHdfsParameter.getFileType());
                }

                if (targetHdfsParameter.isDeleteTargetDir()) {
                    hdfsTargetSb.append(SPACE).append(DELETE_TARGET_DIR);
                }

                if (StringUtils.isNotEmpty(targetHdfsParameter.getFieldsTerminated())) {
                    hdfsTargetSb.append(SPACE).append(FIELDS_TERMINATED_BY)
                            .append(SPACE).append(SINGLE_QUOTES).append(targetHdfsParameter.getFieldsTerminated())
                            .append(SINGLE_QUOTES);
                }

                if (StringUtils.isNotEmpty(targetHdfsParameter.getLinesTerminated())) {
                    hdfsTargetSb.append(SPACE).append(LINES_TERMINATED_BY)
                            .append(SPACE).append(SINGLE_QUOTES).append(targetHdfsParameter.getLinesTerminated())
                            .append(SINGLE_QUOTES);
                }

                hdfsTargetSb.append(SPACE).append(FIELD_NULL_PLACEHOLDER);
            }
        } catch (Exception e) {
            log.error(String.format("Sqoop hdfs target params build failed: [%s]", e.getMessage()));
        }

        return hdfsTargetSb.toString();
    }
}
