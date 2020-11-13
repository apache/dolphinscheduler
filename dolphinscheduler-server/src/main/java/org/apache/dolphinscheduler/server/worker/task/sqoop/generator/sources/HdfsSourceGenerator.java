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
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceHdfsParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.SqoopConstants;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ISourceGenerator;

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hdfs source generator
 */
public class HdfsSourceGenerator implements ISourceGenerator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters, TaskExecutionContext taskExecutionContext) {

        LinkedList<String> hdfsSourceParamsList = new LinkedList<>();

        try {
            SourceHdfsParameter sourceHdfsParameter
                = JSONUtils.parseObject(sqoopParameters.getSourceParams(), SourceHdfsParameter.class);

            if (null != sourceHdfsParameter) {
                if (StringUtils.isNotEmpty(sourceHdfsParameter.getExportDir())) {
                    hdfsSourceParamsList.add(SqoopConstants.HDFS_EXPORT_DIR);
                    hdfsSourceParamsList.add(sourceHdfsParameter.getExportDir());
                } else {
                    throw new IllegalArgumentException("Sqoop hdfs export dir is null");
                }

            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop hdfs source parmas build failed: [%s]", e.getMessage()));
        }

        return String.join(" ", hdfsSourceParamsList);
    }
}
