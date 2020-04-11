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
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ISourceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hdfs source generator
 */
public class HdfsSourceGenerator implements ISourceGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters,TaskExecutionContext taskExecutionContext) {
        StringBuilder result = new StringBuilder();
        try{
            SourceHdfsParameter sourceHdfsParameter
                    = JSONUtils.parseObject(sqoopParameters.getSourceParams(),SourceHdfsParameter.class);

            if(sourceHdfsParameter != null){
                if(StringUtils.isNotEmpty(sourceHdfsParameter.getExportDir())){
                    result.append(" --export-dir ")
                            .append(sourceHdfsParameter.getExportDir());
                }else{
                    throw new Exception("--export-dir is null");
                }

            }
        }catch (Exception e){
            logger.error("get hdfs source failed",e);
        }

        return result.toString();
    }
}
