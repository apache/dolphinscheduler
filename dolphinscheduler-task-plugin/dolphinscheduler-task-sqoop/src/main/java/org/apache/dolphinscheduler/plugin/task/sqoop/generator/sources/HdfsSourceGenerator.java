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
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.HDFS_EXPORT_DIR;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.sqoop.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.sqoop.generator.ISourceGenerator;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.SqoopParameters;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.sources.SourceHdfsParameter;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/** hdfs source generator */
@Slf4j
public class HdfsSourceGenerator implements ISourceGenerator {

    @Override
    public String generate(
                           SqoopParameters sqoopParameters, SqoopTaskExecutionContext sqoopTaskExecutionContext) {

        StringBuilder hdfsSourceSb = new StringBuilder();

        try {
            SourceHdfsParameter sourceHdfsParameter =
                    JSONUtils.parseObject(
                            sqoopParameters.getSourceParams(), SourceHdfsParameter.class);

            if (null != sourceHdfsParameter) {
                if (StringUtils.isNotEmpty(sourceHdfsParameter.getExportDir())) {
                    hdfsSourceSb
                            .append(SPACE)
                            .append(HDFS_EXPORT_DIR)
                            .append(SPACE)
                            .append(sourceHdfsParameter.getExportDir());
                } else {
                    throw new IllegalArgumentException("Sqoop hdfs export dir is null");
                }
            }
        } catch (Exception e) {
            log.error(String.format("Sqoop hdfs source params build failed: [%s]", e.getMessage()));
        }

        return hdfsSourceSb.toString();
    }
}
