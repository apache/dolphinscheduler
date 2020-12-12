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
import org.apache.dolphinscheduler.common.enums.SqoopQueryType;
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mysql source generator
 */
public class MysqlSourceGenerator implements ISourceGenerator {

    private static final Logger logger = LoggerFactory.getLogger(MysqlSourceGenerator.class);

    @Override
    public String generate(SqoopParameters sqoopParameters, TaskExecutionContext taskExecutionContext) {

        StringBuilder mysqlSourceSb = new StringBuilder();

        try {
            SourceMysqlParameter sourceMysqlParameter = JSONUtils.parseObject(sqoopParameters.getSourceParams(), SourceMysqlParameter.class);
            SqoopTaskExecutionContext sqoopTaskExecutionContext = taskExecutionContext.getSqoopTaskExecutionContext();

            if (null != sourceMysqlParameter) {
                BaseDataSource baseDataSource = DataSourceFactory.getDatasource(DbType.of(sqoopTaskExecutionContext.getSourcetype()),
                    sqoopTaskExecutionContext.getSourceConnectionParams());

                if (null != baseDataSource) {

                    mysqlSourceSb.append(Constants.SPACE).append(SqoopConstants.DB_CONNECT)
                        .append(Constants.SPACE).append(Constants.DOUBLE_QUOTES).append(baseDataSource.getJdbcUrl()).append(Constants.DOUBLE_QUOTES)
                        .append(Constants.SPACE).append(SqoopConstants.DB_USERNAME)
                        .append(Constants.SPACE).append(baseDataSource.getUser())
                        .append(Constants.SPACE).append(SqoopConstants.DB_PWD)
                        .append(Constants.SPACE).append(Constants.DOUBLE_QUOTES).append(baseDataSource.getPassword()).append(Constants.DOUBLE_QUOTES);

                    //sqoop table & sql query
                    if (sourceMysqlParameter.getSrcQueryType() == SqoopQueryType.FORM.getCode()) {
                        if (StringUtils.isNotEmpty(sourceMysqlParameter.getSrcTable())) {
                            mysqlSourceSb.append(Constants.SPACE).append(SqoopConstants.TABLE)
                                .append(Constants.SPACE).append(sourceMysqlParameter.getSrcTable());
                        }

                        if (StringUtils.isNotEmpty(sourceMysqlParameter.getSrcColumns())) {
                            mysqlSourceSb.append(Constants.SPACE).append(SqoopConstants.COLUMNS)
                                .append(Constants.SPACE).append(sourceMysqlParameter.getSrcColumns());
                        }
                    } else if (sourceMysqlParameter.getSrcQueryType() == SqoopQueryType.SQL.getCode()
                        && StringUtils.isNotEmpty(sourceMysqlParameter.getSrcQuerySql())) {

                        String srcQuery = sourceMysqlParameter.getSrcQuerySql();
                        mysqlSourceSb.append(Constants.SPACE).append(SqoopConstants.QUERY)
                            .append(Constants.SPACE).append(Constants.DOUBLE_QUOTES).append(srcQuery);

                        if (srcQuery.toLowerCase().contains(SqoopConstants.QUERY_WHERE)) {
                            mysqlSourceSb.append(Constants.SPACE).append(SqoopConstants.QUERY_CONDITION).append(Constants.DOUBLE_QUOTES);
                        } else {
                            mysqlSourceSb.append(Constants.SPACE).append(SqoopConstants.QUERY_WITHOUT_CONDITION).append(Constants.DOUBLE_QUOTES);
                        }
                    }

                    //sqoop hive map column
                    List<Property> mapColumnHive = sourceMysqlParameter.getMapColumnHive();

                    if (null != mapColumnHive && !mapColumnHive.isEmpty()) {
                        StringBuilder columnMap = new StringBuilder();
                        for (Property item : mapColumnHive) {
                            if (!item.getProp().isEmpty()) {
                                columnMap.append(item.getProp()).append(Constants.EQUAL_SIGN)
                                        .append(item.getValue()).append(Constants.COMMA);
                            }
                        }

                        if (StringUtils.isNotEmpty(columnMap.toString())) {
                            mysqlSourceSb.append(Constants.SPACE).append(SqoopConstants.MAP_COLUMN_HIVE)
                                .append(Constants.SPACE).append(columnMap.substring(0, columnMap.length() - 1));
                        }
                    }

                    //sqoop map column java
                    List<Property> mapColumnJava = sourceMysqlParameter.getMapColumnJava();

                    if (null != mapColumnJava && !mapColumnJava.isEmpty()) {
                        StringBuilder columnJavaMap = new StringBuilder();
                        for (Property item : mapColumnJava) {
                            if (!item.getProp().isEmpty()) {
                                columnJavaMap.append(item.getProp()).append(Constants.EQUAL_SIGN)
                                        .append(item.getValue()).append(Constants.COMMA);
                            }
                        }

                        if (StringUtils.isNotEmpty(columnJavaMap.toString())) {
                            mysqlSourceSb.append(Constants.SPACE).append(SqoopConstants.MAP_COLUMN_JAVA)
                                .append(Constants.SPACE).append(columnJavaMap.substring(0, columnJavaMap.length() - 1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop task mysql source params build failed: [%s]", e.getMessage()));
        }

        return mysqlSourceSb.toString();
    }
}
