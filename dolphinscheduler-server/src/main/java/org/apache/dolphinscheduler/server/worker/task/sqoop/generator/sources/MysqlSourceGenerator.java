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
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.QueryType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceMysqlParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.server.entity.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ISourceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * mysql source generator
 */
public class MysqlSourceGenerator implements ISourceGenerator {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters,TaskExecutionContext taskExecutionContext) {
        StringBuilder result = new StringBuilder();
        try {
            SourceMysqlParameter sourceMysqlParameter
                    = JSONUtils.parseObject(sqoopParameters.getSourceParams(),SourceMysqlParameter.class);

            SqoopTaskExecutionContext sqoopTaskExecutionContext = taskExecutionContext.getSqoopTaskExecutionContext();

            if(sourceMysqlParameter != null){
                BaseDataSource baseDataSource = DataSourceFactory.getDatasource(DbType.of(sqoopTaskExecutionContext.getSourcetype()),
                        sqoopTaskExecutionContext.getSourceConnectionParams());
                if(baseDataSource != null){
                    result.append(" --connect ")
                            .append(baseDataSource.getJdbcUrl())
                            .append(" --username ")
                            .append(baseDataSource.getUser())
                            .append(" --password ")
                            .append(baseDataSource.getPassword());

                    if(sourceMysqlParameter.getSrcQueryType() == QueryType.FORM.ordinal()){
                        if(StringUtils.isNotEmpty(sourceMysqlParameter.getSrcTable())){
                            result.append(" --table ").append(sourceMysqlParameter.getSrcTable());
                        }

                        if(StringUtils.isNotEmpty(sourceMysqlParameter.getSrcColumns())){
                            result.append(" --columns ").append(sourceMysqlParameter.getSrcColumns());
                        }

                    }else if(sourceMysqlParameter.getSrcQueryType() == QueryType.SQL.ordinal()
                            && StringUtils.isNotEmpty(sourceMysqlParameter.getSrcQuerySql())){
                        String srcQuery = sourceMysqlParameter.getSrcQuerySql();
                        if(srcQuery.toLowerCase().contains("where")){
                            srcQuery += " AND "+"$CONDITIONS";
                        }else{
                            srcQuery += " WHERE $CONDITIONS";
                        }
                        result.append(" --query \'").append(srcQuery).append("\'");

                    }

                    List<Property>  mapColumnHive = sourceMysqlParameter.getMapColumnHive();

                    if(mapColumnHive != null && !mapColumnHive.isEmpty()){
                        StringBuilder columnMap = new StringBuilder();
                        for(Property item:mapColumnHive){
                            columnMap.append(item.getProp()).append("=").append(item.getValue()).append(",");
                        }

                        if(StringUtils.isNotEmpty(columnMap.toString())){
                            result.append(" --map-column-hive ")
                                    .append(columnMap.substring(0,columnMap.length() - 1));
                        }
                    }

                    List<Property>  mapColumnJava = sourceMysqlParameter.getMapColumnJava();

                    if(mapColumnJava != null && !mapColumnJava.isEmpty()){
                        StringBuilder columnMap = new StringBuilder();
                        for(Property item:mapColumnJava){
                            columnMap.append(item.getProp()).append("=").append(item.getValue()).append(",");
                        }

                        if(StringUtils.isNotEmpty(columnMap.toString())){
                            result.append(" --map-column-java ")
                                    .append(columnMap.substring(0,columnMap.length() - 1));
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        return result.toString();
    }
}
