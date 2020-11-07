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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.QueryType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceMysqlParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.server.entity.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.SqoopConstants;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ISourceGenerator;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mysql source generator
 */
public class MysqlSourceGenerator implements ISourceGenerator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters, TaskExecutionContext taskExecutionContext) {

        LinkedList<String> mysqlSourceParamsList = new LinkedList<>();

        try {
            SourceMysqlParameter sourceMysqlParameter = JSONUtils.parseObject(sqoopParameters.getSourceParams(), SourceMysqlParameter.class);
            SqoopTaskExecutionContext sqoopTaskExecutionContext = taskExecutionContext.getSqoopTaskExecutionContext();

            if (null != sourceMysqlParameter) {
                BaseDataSource baseDataSource = DataSourceFactory.getDatasource(DbType.of(sqoopTaskExecutionContext.getSourcetype()),
                    sqoopTaskExecutionContext.getSourceConnectionParams());

                if (null != baseDataSource) {
                    mysqlSourceParamsList.add(SqoopConstants.DB_CONNECT);
                    mysqlSourceParamsList.add(baseDataSource.getJdbcUrl());
                    mysqlSourceParamsList.add(SqoopConstants.DB_USERNAME);
                    mysqlSourceParamsList.add(baseDataSource.getUser());
                    mysqlSourceParamsList.add(SqoopConstants.DB_PWD);
                    mysqlSourceParamsList.add(baseDataSource.getPassword());

                    //sqoop table & sql query
                    if (sourceMysqlParameter.getSrcQueryType() == QueryType.FORM.ordinal()) {
                        if (StringUtils.isNotEmpty(sourceMysqlParameter.getSrcTable())) {
                            mysqlSourceParamsList.add(SqoopConstants.TABLE);
                            mysqlSourceParamsList.add(sourceMysqlParameter.getSrcTable());
                        }

                        if (StringUtils.isNotEmpty(sourceMysqlParameter.getSrcColumns())) {
                            mysqlSourceParamsList.add(SqoopConstants.COLUMNS);
                            mysqlSourceParamsList.add(sourceMysqlParameter.getSrcColumns());
                        }
                    } else if (sourceMysqlParameter.getSrcQueryType() == QueryType.SQL.ordinal()
                        && StringUtils.isNotEmpty(sourceMysqlParameter.getSrcQuerySql())) {

                        mysqlSourceParamsList.add(SqoopConstants.QUERY);
                        String srcQuery = sourceMysqlParameter.getSrcQuerySql();
                        mysqlSourceParamsList.add(SqoopConstants.QUOTATION_MARKS + srcQuery);

                        if (srcQuery.toLowerCase().contains(SqoopConstants.QUERY_WHERE)) {
                            mysqlSourceParamsList.add(SqoopConstants.QUERY_CONDITION + SqoopConstants.QUOTATION_MARKS);
                        } else {
                            mysqlSourceParamsList.add(SqoopConstants.QUERY_WITHOUT_CONDITION + SqoopConstants.QUOTATION_MARKS);
                        }
                    }

                    //sqoop hive map column
                    List<Property> mapColumnHive = sourceMysqlParameter.getMapColumnHive();

                    if (null != mapColumnHive && !mapColumnHive.isEmpty()) {
                        StringBuilder columnMap = new StringBuilder();
                        for (Property item : mapColumnHive) {
                            columnMap.append(item.getProp()).append(Constants.EQUAL_SIGN).append(item.getValue()).append(Constants.COMMA);
                        }

                        if (StringUtils.isNotEmpty(columnMap.toString())) {
                            mysqlSourceParamsList.add(SqoopConstants.MAP_COLUMN_HIVE);
                            mysqlSourceParamsList.add(columnMap.substring(0, columnMap.length() - 1));
                        }
                    }

                    //sqoop map column java
                    List<Property> mapColumnJava = sourceMysqlParameter.getMapColumnJava();

                    if (null != mapColumnJava && !mapColumnJava.isEmpty()) {
                        StringBuilder columnMap = new StringBuilder();
                        for (Property item : mapColumnJava) {
                            columnMap.append(item.getProp()).append(Constants.EQUAL_SIGN).append(item.getValue()).append(Constants.COMMA);
                        }

                        if (StringUtils.isNotEmpty(columnMap.toString())) {
                            mysqlSourceParamsList.add(SqoopConstants.MAP_COLUMN_JAVA);
                            mysqlSourceParamsList.add(columnMap.substring(0, columnMap.length() - 1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop task mysql source params build failed: [%s]", e.getMessage()));
        }

        return String.join(" ", mysqlSourceParamsList);
    }
}
