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
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetMysqlParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.server.entity.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ITargetGenerator;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mysql target generator
 */
public class MysqlTargetGenerator implements ITargetGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters,TaskExecutionContext taskExecutionContext) {

        StringBuilder result = new StringBuilder();
        try{

            TargetMysqlParameter targetMysqlParameter =
                    JSONUtils.parseObject(sqoopParameters.getTargetParams(),TargetMysqlParameter.class);

            SqoopTaskExecutionContext sqoopTaskExecutionContext = taskExecutionContext.getSqoopTaskExecutionContext();

            if(targetMysqlParameter != null && targetMysqlParameter.getTargetDatasource() != 0){

                // get datasource
                BaseDataSource baseDataSource = DataSourceFactory.getDatasource(DbType.of(sqoopTaskExecutionContext.getTargetType()),
                        sqoopTaskExecutionContext.getTargetConnectionParams());

                if(baseDataSource != null){
                    result.append(" --connect ")
                            .append(baseDataSource.getJdbcUrl())
                            .append(" --username ")
                            .append(baseDataSource.getUser())
                            .append(" --password ")
                            .append(baseDataSource.getPassword())
                            .append(" --table ")
                            .append(targetMysqlParameter.getTargetTable());

                    if(StringUtils.isNotEmpty(targetMysqlParameter.getTargetColumns())){
                        result.append(" --columns ").append(targetMysqlParameter.getTargetColumns());
                    }

                    if(StringUtils.isNotEmpty(targetMysqlParameter.getFieldsTerminated())){
                        result.append(" --fields-terminated-by '").append(targetMysqlParameter.getFieldsTerminated()).append("'");
                    }

                    if(StringUtils.isNotEmpty(targetMysqlParameter.getLinesTerminated())){
                        result.append(" --lines-terminated-by '").append(targetMysqlParameter.getLinesTerminated()).append("'");
                    }

                    if(targetMysqlParameter.isUpdate()
                            && StringUtils.isNotEmpty(targetMysqlParameter.getTargetUpdateKey())
                            && StringUtils.isNotEmpty(targetMysqlParameter.getTargetUpdateMode())){
                        result.append(" --update-key ").append(targetMysqlParameter.getTargetUpdateKey())
                              .append(" --update-mode ").append(targetMysqlParameter.getTargetUpdateMode());
                    }
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        return result.toString();
    }
}
