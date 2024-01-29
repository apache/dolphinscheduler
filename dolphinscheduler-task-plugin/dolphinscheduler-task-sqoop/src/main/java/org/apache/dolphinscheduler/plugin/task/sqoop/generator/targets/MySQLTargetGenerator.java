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
import org.apache.dolphinscheduler.plugin.task.sqoop.parameter.targets.TargetMysqlParameter;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * mysql target generator
 */
@Slf4j
public class MySQLTargetGenerator implements ITargetGenerator {

    @Override
    public String generate(SqoopParameters sqoopParameters, SqoopTaskExecutionContext sqoopTaskExecutionContext) {

        StringBuilder mysqlTargetSb = new StringBuilder();

        try {
            TargetMysqlParameter targetMysqlParameter =
                    JSONUtils.parseObject(sqoopParameters.getTargetParams(), TargetMysqlParameter.class);

            if (null != targetMysqlParameter && targetMysqlParameter.getTargetDatasource() != 0) {

                // get datasource
                BaseConnectionParam baseDataSource = (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                        sqoopTaskExecutionContext.getTargetType(),
                        sqoopTaskExecutionContext.getTargetConnectionParams());

                if (null != baseDataSource) {

                    mysqlTargetSb.append(SPACE).append(DB_CONNECT)
                            .append(SPACE).append(DOUBLE_QUOTES)
                            .append(DataSourceUtils.getJdbcUrl(DbType.MYSQL, baseDataSource)).append(DOUBLE_QUOTES)
                            .append(SPACE).append(DB_USERNAME)
                            .append(SPACE).append(baseDataSource.getUser())
                            .append(SPACE).append(DB_PWD)
                            .append(SPACE).append(DOUBLE_QUOTES)
                            .append(decodePassword(baseDataSource.getPassword())).append(DOUBLE_QUOTES)
                            .append(SPACE).append(TABLE)
                            .append(SPACE).append(targetMysqlParameter.getTargetTable());

                    if (StringUtils.isNotEmpty(targetMysqlParameter.getTargetColumns())) {
                        mysqlTargetSb.append(SPACE).append(COLUMNS)
                                .append(SPACE).append(targetMysqlParameter.getTargetColumns());
                    }

                    if (StringUtils.isNotEmpty(targetMysqlParameter.getFieldsTerminated())) {
                        mysqlTargetSb.append(SPACE).append(FIELDS_TERMINATED_BY);
                        if (targetMysqlParameter.getFieldsTerminated().contains("'")) {
                            mysqlTargetSb.append(SPACE).append(targetMysqlParameter.getFieldsTerminated());

                        } else {
                            mysqlTargetSb.append(SPACE).append(SINGLE_QUOTES)
                                    .append(targetMysqlParameter.getFieldsTerminated()).append(SINGLE_QUOTES);
                        }
                    }

                    if (StringUtils.isNotEmpty(targetMysqlParameter.getLinesTerminated())) {
                        mysqlTargetSb.append(SPACE).append(LINES_TERMINATED_BY);
                        if (targetMysqlParameter.getLinesTerminated().contains(SINGLE_QUOTES)) {
                            mysqlTargetSb.append(SPACE).append(targetMysqlParameter.getLinesTerminated());
                        } else {
                            mysqlTargetSb.append(SPACE).append(SINGLE_QUOTES)
                                    .append(targetMysqlParameter.getLinesTerminated()).append(SINGLE_QUOTES);
                        }
                    }

                    if (targetMysqlParameter.getIsUpdate()
                            && StringUtils.isNotEmpty(targetMysqlParameter.getTargetUpdateKey())
                            && StringUtils.isNotEmpty(targetMysqlParameter.getTargetUpdateMode())) {
                        mysqlTargetSb.append(SPACE).append(UPDATE_KEY)
                                .append(SPACE).append(targetMysqlParameter.getTargetUpdateKey())
                                .append(SPACE).append(UPDATE_MODE)
                                .append(SPACE).append(targetMysqlParameter.getTargetUpdateMode());
                    }
                }
            }
        } catch (Exception e) {
            log.error(String.format("Sqoop mysql target params build failed: [%s]", e.getMessage()));
        }

        return mysqlTargetSb.toString();
    }
}
