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

import static org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils.decodePassword;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.COMMA;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.DOUBLE_QUOTES;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EQUAL_SIGN;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SPACE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.COLUMNS;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.DB_CONNECT;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.DB_PWD;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.DB_USERNAME;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.DRIVER;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.MAP_COLUMN_HIVE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.MAP_COLUMN_JAVA;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.QUERY;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.QUERY_CONDITION;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.QUERY_WHERE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.QUERY_WITHOUT_CONDITION;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.TABLE;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.sqoop.SqoopQueryType;
import org.apache.dolphinscheduler.plugin.task.sqoop.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.sqoop.generator.ISourceGenerator;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.SqoopParameters;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.sources.SourceHanaParameter;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hana source generator
 */
public class HanaSourceGenerator implements ISourceGenerator {

    private static final Logger logger = LoggerFactory.getLogger(HanaSourceGenerator.class);

    @Override
    public String generate(SqoopParameters sqoopParameters, SqoopTaskExecutionContext sqoopTaskExecutionContext) {

        StringBuilder hanaSourceSb = new StringBuilder();

        try {
            SourceHanaParameter sourceHanaParameter =
                    JSONUtils.parseObject(sqoopParameters.getSourceParams(), SourceHanaParameter.class);

            if (null == sourceHanaParameter)
                return hanaSourceSb.toString();
            BaseConnectionParam baseDataSource = (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                    sqoopTaskExecutionContext.getSourcetype(),
                    sqoopTaskExecutionContext.getSourceConnectionParams());

            if (null == baseDataSource)
                return hanaSourceSb.toString();

            hanaSourceSb.append(SPACE).append(DB_CONNECT)
                    .append(SPACE).append(DOUBLE_QUOTES)
                    .append(DataSourceUtils.getJdbcUrl(DbType.HANA, baseDataSource)).append(DOUBLE_QUOTES)
                    .append(SPACE).append(DRIVER)
                    .append(SPACE).append(DataSourceUtils.getDatasourceDriver(DbType.HANA))
                    .append(SPACE).append(DB_USERNAME)
                    .append(SPACE).append(baseDataSource.getUser())
                    .append(SPACE).append(DB_PWD)
                    .append(SPACE).append(DOUBLE_QUOTES)
                    .append(decodePassword(baseDataSource.getPassword())).append(DOUBLE_QUOTES);

            // sqoop table & sql query
            if (sourceHanaParameter.getSrcQueryType() == SqoopQueryType.FORM.getCode()) {
                if (StringUtils.isNotEmpty(sourceHanaParameter.getSrcTable())) {
                    hanaSourceSb.append(SPACE).append(TABLE)
                            .append(SPACE).append(sourceHanaParameter.getSrcTable());
                }

                if (StringUtils.isNotEmpty(sourceHanaParameter.getSrcColumns())) {
                    hanaSourceSb.append(SPACE).append(COLUMNS)
                            .append(SPACE).append(sourceHanaParameter.getSrcColumns());
                }
            } else if (sourceHanaParameter.getSrcQueryType() == SqoopQueryType.SQL.getCode()
                    && StringUtils.isNotEmpty(sourceHanaParameter.getSrcQuerySql())) {

                String srcQuery = sourceHanaParameter.getSrcQuerySql();
                hanaSourceSb.append(SPACE).append(QUERY)
                        .append(SPACE).append(DOUBLE_QUOTES).append(srcQuery);

                if (srcQuery.toLowerCase().contains(QUERY_WHERE)) {
                    hanaSourceSb.append(SPACE).append(QUERY_CONDITION).append(DOUBLE_QUOTES);
                } else {
                    hanaSourceSb.append(SPACE).append(QUERY_WITHOUT_CONDITION).append(DOUBLE_QUOTES);
                }
            }

            // sqoop hive map column
            buildColumnMapToHIve(hanaSourceSb, sourceHanaParameter);
            // sqoop map column java
            buildColumnMapToJava(hanaSourceSb, sourceHanaParameter);
        } catch (Exception e) {
            logger.error(String.format("Sqoop task hana source params build failed: [%s]", e.getMessage()));
        }

        return hanaSourceSb.toString();
    }

    private static void buildColumnMapToJava(StringBuilder hanaSourceSb, SourceHanaParameter sourceHanaParameter) {
        List<Property> mapColumnJava = sourceHanaParameter.getMapColumnJava();

        if (null != mapColumnJava && !mapColumnJava.isEmpty()) {
            StringBuilder columnMap = new StringBuilder();
            for (Property item : mapColumnJava) {
                columnMap.append(item.getProp()).append(EQUAL_SIGN).append(item.getValue()).append(COMMA);
            }

            if (StringUtils.isNotEmpty(columnMap.toString())) {
                hanaSourceSb.append(SPACE).append(MAP_COLUMN_JAVA)
                        .append(SPACE).append(columnMap.substring(0, columnMap.length() - 1));
            }
        }
    }

    private static void buildColumnMapToHIve(StringBuilder hanaSourceSb, SourceHanaParameter sourceHanaParameter) {
        List<Property> mapColumnHive = sourceHanaParameter.getMapColumnHive();

        if (null != mapColumnHive && !mapColumnHive.isEmpty()) {
            StringBuilder columnMap = new StringBuilder();
            for (Property item : mapColumnHive) {
                columnMap.append(item.getProp()).append(EQUAL_SIGN).append(item.getValue()).append(COMMA);
            }

            if (StringUtils.isNotEmpty(columnMap.toString())) {
                hanaSourceSb.append(SPACE).append(MAP_COLUMN_HIVE)
                        .append(SPACE).append(columnMap.substring(0, columnMap.length() - 1));
            }
        }
    }
}
