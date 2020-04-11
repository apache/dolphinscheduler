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

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetHdfsParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ITargetGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hdfs target generator
 */
public class HdfsTargetGenerator implements ITargetGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters,TaskExecutionContext taskExecutionContext) {
        StringBuilder result = new StringBuilder();
        try{
            TargetHdfsParameter targetHdfsParameter =
                    JSONUtils.parseObject(sqoopParameters.getTargetParams(),TargetHdfsParameter.class);

            if(targetHdfsParameter != null){

                if(StringUtils.isNotEmpty(targetHdfsParameter.getTargetPath())){
                    result.append(" --target-dir ").append(targetHdfsParameter.getTargetPath());
                }

                if(StringUtils.isNotEmpty(targetHdfsParameter.getCompressionCodec())){
                    result.append(" --compression-codec ").append(targetHdfsParameter.getCompressionCodec());
                }

                if(StringUtils.isNotEmpty(targetHdfsParameter.getFileType())){
                    result.append(" ").append(targetHdfsParameter.getFileType());
                }

                if(targetHdfsParameter.isDeleteTargetDir()){
                    result.append(" --delete-target-dir");
                }

                if(StringUtils.isNotEmpty(targetHdfsParameter.getFieldsTerminated())){
                    result.append(" --fields-terminated-by '").append(targetHdfsParameter.getFieldsTerminated()).append("'");
                }

                if(StringUtils.isNotEmpty(targetHdfsParameter.getLinesTerminated())){
                    result.append(" --lines-terminated-by '").append(targetHdfsParameter.getLinesTerminated()).append("'");
                }

                result.append(" --null-non-string 'NULL' --null-string 'NULL'");
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }

        return result.toString();
    }
}
