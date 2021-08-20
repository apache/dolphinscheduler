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
import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.datasource.DatasourceUtil;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.SqoopQueryType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceJDBCParameter;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.entity.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.SqoopConstants;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ISourceGenerator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC source generator
 */
public class JDBCSourceGenerator implements ISourceGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JDBCSourceGenerator.class);

    @Override
    public String generate(SqoopParameters sqoopParameters, TaskExecutionContext taskExecutionContext) {

        StringBuilder jdbcSourceSb = new StringBuilder();

        try {
            SourceJDBCParameter sourceJDBCParameter = JSONUtils.parseObject(sqoopParameters.getSourceParams(), SourceJDBCParameter.class);
            SqoopTaskExecutionContext sqoopTaskExecutionContext = taskExecutionContext.getSqoopTaskExecutionContext();

            if (null != sourceJDBCParameter) {
                BaseConnectionParam baseDataSource = (BaseConnectionParam) DatasourceUtil.buildConnectionParams(
                        DbType.of(sqoopTaskExecutionContext.getSourcetype()),
                    sqoopTaskExecutionContext.getSourceConnectionParams());

                if (null != baseDataSource) {

                    jdbcSourceSb.append(Constants.SPACE).append(SqoopConstants.DB_CONNECT)
                        .append(Constants.SPACE).append(Constants.DOUBLE_QUOTES).append(baseDataSource.getJdbcUrl()).append(Constants.DOUBLE_QUOTES)
                        .append(Constants.SPACE).append(SqoopConstants.DB_USERNAME)
                        .append(Constants.SPACE).append(baseDataSource.getUser())
                        .append(Constants.SPACE).append(SqoopConstants.DB_PWD)
                        .append(Constants.SPACE).append(Constants.DOUBLE_QUOTES).append(baseDataSource.getPassword()).append(Constants.DOUBLE_QUOTES);

                    //sqoop table & sql query
                    if (sourceJDBCParameter.getSrcQueryType() == SqoopQueryType.FORM.getCode()) {
                        if (StringUtils.isNotEmpty(sourceJDBCParameter.getSrcTable())) {
                            jdbcSourceSb.append(Constants.SPACE).append(SqoopConstants.TABLE)
                                .append(Constants.SPACE).append(sourceJDBCParameter.getSrcTable());
                        }

                        if (StringUtils.isNotEmpty(sourceJDBCParameter.getSrcColumns())) {
                            jdbcSourceSb.append(Constants.SPACE).append(SqoopConstants.COLUMNS)
                                .append(Constants.SPACE).append(sourceJDBCParameter.getSrcColumns());
                        }
                    } else if (sourceJDBCParameter.getSrcQueryType() == SqoopQueryType.SQL.getCode()
                        && StringUtils.isNotEmpty(sourceJDBCParameter.getSrcQuerySql())) {

                        String srcQuery = sourceJDBCParameter.getSrcQuerySql();
                        jdbcSourceSb.append(Constants.SPACE).append(SqoopConstants.QUERY)
                            .append(Constants.SPACE).append(Constants.DOUBLE_QUOTES).append(srcQuery);

                        if (srcQuery.toLowerCase().contains(SqoopConstants.QUERY_WHERE)) {
                            jdbcSourceSb.append(Constants.SPACE).append(SqoopConstants.QUERY_CONDITION).append(Constants.DOUBLE_QUOTES);
                        } else {
                            jdbcSourceSb.append(Constants.SPACE).append(SqoopConstants.QUERY_WITHOUT_CONDITION).append(Constants.DOUBLE_QUOTES);
                        }
                    }

                    //sqoop hive map column
                    List<Property> mapColumnHive = sourceJDBCParameter.getMapColumnHive();

                    if (null != mapColumnHive && !mapColumnHive.isEmpty()) {
                        StringBuilder columnMap = new StringBuilder();
                        for (Property item : mapColumnHive) {
                            columnMap.append(item.getProp()).append(Constants.EQUAL_SIGN).append(item.getValue()).append(Constants.COMMA);
                        }

                        if (StringUtils.isNotEmpty(columnMap.toString())) {
                            jdbcSourceSb.append(Constants.SPACE).append(SqoopConstants.MAP_COLUMN_HIVE)
                                .append(Constants.SPACE).append(columnMap.substring(0, columnMap.length() - 1));
                        }
                    }

                    //sqoop map column java
                    List<Property> mapColumnJava = sourceJDBCParameter.getMapColumnJava();

                    if (null != mapColumnJava && !mapColumnJava.isEmpty()) {
                        StringBuilder columnMap = new StringBuilder();
                        for (Property item : mapColumnJava) {
                            columnMap.append(item.getProp()).append(Constants.EQUAL_SIGN).append(item.getValue()).append(Constants.COMMA);
                        }

                        if (StringUtils.isNotEmpty(columnMap.toString())) {
                            jdbcSourceSb.append(Constants.SPACE).append(SqoopConstants.MAP_COLUMN_JAVA)
                                .append(Constants.SPACE).append(columnMap.substring(0, columnMap.length() - 1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop task JDBC source params build failed: [%s]", e.getMessage()));
        }

        return jdbcSourceSb.toString();
    }
}