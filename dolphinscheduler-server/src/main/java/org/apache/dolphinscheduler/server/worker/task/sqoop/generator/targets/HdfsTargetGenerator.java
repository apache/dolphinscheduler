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
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetHdfsParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.SqoopConstants;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ITargetGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hdfs target generator
 */
public class HdfsTargetGenerator implements ITargetGenerator {

    private static final Logger logger = LoggerFactory.getLogger(HdfsTargetGenerator.class);

    @Override
    public String generate(SqoopParameters sqoopParameters, TaskExecutionContext taskExecutionContext) {

        StringBuilder hdfsTargetSb = new StringBuilder();

        try {
            TargetHdfsParameter targetHdfsParameter =
                JSONUtils.parseObject(sqoopParameters.getTargetParams(), TargetHdfsParameter.class);

            if (null != targetHdfsParameter) {

                if (StringUtils.isNotEmpty(targetHdfsParameter.getTargetPath())) {
                    hdfsTargetSb.append(Constants.SPACE).append(SqoopConstants.TARGET_DIR)
                        .append(Constants.SPACE).append(targetHdfsParameter.getTargetPath());
                }

                if (StringUtils.isNotEmpty(targetHdfsParameter.getCompressionCodec())) {
                    hdfsTargetSb.append(Constants.SPACE).append(SqoopConstants.COMPRESSION_CODEC)
                        .append(Constants.SPACE).append(targetHdfsParameter.getCompressionCodec());
                }

                if (StringUtils.isNotEmpty(targetHdfsParameter.getFileType())) {
                    hdfsTargetSb.append(Constants.SPACE).append(targetHdfsParameter.getFileType());
                }

                if (targetHdfsParameter.isDeleteTargetDir()) {
                    hdfsTargetSb.append(Constants.SPACE).append(SqoopConstants.DELETE_TARGET_DIR);
                }

                if (StringUtils.isNotEmpty(targetHdfsParameter.getFieldsTerminated())) {
                    hdfsTargetSb.append(Constants.SPACE).append(SqoopConstants.FIELDS_TERMINATED_BY)
                        .append(Constants.SPACE).append(Constants.SINGLE_QUOTES).append(targetHdfsParameter.getFieldsTerminated()).append(Constants.SINGLE_QUOTES);
                }

                if (StringUtils.isNotEmpty(targetHdfsParameter.getLinesTerminated())) {
                    hdfsTargetSb.append(Constants.SPACE).append(SqoopConstants.LINES_TERMINATED_BY)
                        .append(Constants.SPACE).append(Constants.SINGLE_QUOTES).append(targetHdfsParameter.getLinesTerminated()).append(Constants.SINGLE_QUOTES);
                }

                hdfsTargetSb.append(Constants.SPACE).append(SqoopConstants.FIELD_NULL_PLACEHOLDER);
            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop hdfs target params build failed: [%s]", e.getMessage()));
        }

        return hdfsTargetSb.toString();
    }
}
