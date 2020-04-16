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

import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceHiveParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ISourceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hive source generator
 */
public class HiveSourceGenerator implements ISourceGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters,TaskExecutionContext taskExecutionContext) {
        StringBuilder sb = new StringBuilder();
        try{
            SourceHiveParameter sourceHiveParameter
                    = JSONUtils.parseObject(sqoopParameters.getSourceParams(),SourceHiveParameter.class);
            if(sourceHiveParameter != null){
                if(StringUtils.isNotEmpty(sourceHiveParameter.getHiveDatabase())){
                    sb.append(" --hcatalog-database ").append(sourceHiveParameter.getHiveDatabase());
                }

                if(StringUtils.isNotEmpty(sourceHiveParameter.getHiveTable())){
                    sb.append(" --hcatalog-table ").append(sourceHiveParameter.getHiveTable());
                }

                if(StringUtils.isNotEmpty(sourceHiveParameter.getHivePartitionKey())&&
                        StringUtils.isNotEmpty(sourceHiveParameter.getHivePartitionValue())){
                    sb.append(" --hcatalog-partition-keys ").append(sourceHiveParameter.getHivePartitionKey())
                            .append(" --hcatalog-partition-values ").append(sourceHiveParameter.getHivePartitionValue());
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        return sb.toString();
    }
}
