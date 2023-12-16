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

package org.apache.dolphinscheduler.plugin.task.sqoop.generator.targets;

import static org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils.decodePassword;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.DOUBLE_QUOTES;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SINGLE_QUOTES;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.SPACE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.COLUMNS;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.DB_CONNECT;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.DB_PWD;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.DB_USERNAME;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.FIELDS_TERMINATED_BY;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.LINES_TERMINATED_BY;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.TABLE;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.UPDATE_KEY;
import static org.apache.dolphinscheduler.plugin.task.sqoop.SqoopConstants.UPDATE_MODE;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.sqoop.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.sqoop.generator.ITargetGenerator;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.SqoopParameters;
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.targets.TargetOracleParameter;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * oracle target generator
 */
public class OracleTargetGenerator implements ITargetGenerator {

    private static final Logger logger = LoggerFactory.getLogger(OracleTargetGenerator.class);

    @Override
    public String generate(SqoopParameters sqoopParameters, SqoopTaskExecutionContext sqoopTaskExecutionContext) {

        StringBuilder oracleTargetSb = new StringBuilder();

        try {
            TargetOracleParameter targetOracleParameter =
                    JSONUtils.parseObject(sqoopParameters.getTargetParams(), TargetOracleParameter.class);

            if (null == targetOracleParameter || targetOracleParameter.getTargetDatasource() == 0)
                return oracleTargetSb.toString();

            // get datasource
            BaseConnectionParam baseDataSource = (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                    sqoopTaskExecutionContext.getTargetType(),
                    sqoopTaskExecutionContext.getTargetConnectionParams());

            if (null == baseDataSource) {
                return oracleTargetSb.toString();
            }

            oracleTargetSb.append(SPACE).append(DB_CONNECT)
                    .append(SPACE).append(DOUBLE_QUOTES)
                    .append(DataSourceUtils.getJdbcUrl(DbType.ORACLE, baseDataSource)).append(DOUBLE_QUOTES)
                    .append(SPACE).append(DB_USERNAME)
                    .append(SPACE).append(baseDataSource.getUser())
                    .append(SPACE).append(DB_PWD)
                    .append(SPACE).append(DOUBLE_QUOTES)
                    .append(decodePassword(baseDataSource.getPassword())).append(DOUBLE_QUOTES)
                    .append(SPACE).append(TABLE)
                    .append(SPACE).append(targetOracleParameter.getTargetTable());

            if (StringUtils.isNotEmpty(targetOracleParameter.getTargetColumns())) {
                oracleTargetSb.append(SPACE).append(COLUMNS)
                        .append(SPACE).append(targetOracleParameter.getTargetColumns());
            }

            if (StringUtils.isNotEmpty(targetOracleParameter.getFieldsTerminated())) {
                oracleTargetSb.append(SPACE).append(FIELDS_TERMINATED_BY);
                if (targetOracleParameter.getFieldsTerminated().contains("'")) {
                    oracleTargetSb.append(SPACE).append(targetOracleParameter.getFieldsTerminated());

                } else {
                    oracleTargetSb.append(SPACE).append(SINGLE_QUOTES)
                            .append(targetOracleParameter.getFieldsTerminated()).append(SINGLE_QUOTES);
                }
            }

            if (StringUtils.isNotEmpty(targetOracleParameter.getLinesTerminated())) {
                oracleTargetSb.append(SPACE).append(LINES_TERMINATED_BY);
                if (targetOracleParameter.getLinesTerminated().contains(SINGLE_QUOTES)) {
                    oracleTargetSb.append(SPACE).append(targetOracleParameter.getLinesTerminated());
                } else {
                    oracleTargetSb.append(SPACE).append(SINGLE_QUOTES)
                            .append(targetOracleParameter.getLinesTerminated()).append(SINGLE_QUOTES);
                }
            }

            if (targetOracleParameter.getIsUpdate()
                    && StringUtils.isNotEmpty(targetOracleParameter.getTargetUpdateKey())
                    && StringUtils.isNotEmpty(targetOracleParameter.getTargetUpdateMode())) {
                oracleTargetSb.append(SPACE).append(UPDATE_KEY)
                        .append(SPACE).append(targetOracleParameter.getTargetUpdateKey())
                        .append(SPACE).append(UPDATE_MODE)
                        .append(SPACE).append(targetOracleParameter.getTargetUpdateMode());
            }

        } catch (Exception e) {
            logger.error(String.format("Sqoop oracle target params build failed: [%s]", e.getMessage()));
        }

        return oracleTargetSb.toString();
    }
}
