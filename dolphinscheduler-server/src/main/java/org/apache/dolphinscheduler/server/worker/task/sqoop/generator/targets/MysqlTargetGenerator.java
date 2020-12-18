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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetMysqlParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.server.entity.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.SqoopConstants;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ITargetGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mysql target generator
 */
public class MysqlTargetGenerator implements ITargetGenerator {

    private static final Logger logger = LoggerFactory.getLogger(MysqlTargetGenerator.class);

    @Override
    public String generate(SqoopParameters sqoopParameters, TaskExecutionContext taskExecutionContext) {

        StringBuilder mysqlTargetSb = new StringBuilder();

        try {
            TargetMysqlParameter targetMysqlParameter =
                JSONUtils.parseObject(sqoopParameters.getTargetParams(), TargetMysqlParameter.class);

            SqoopTaskExecutionContext sqoopTaskExecutionContext = taskExecutionContext.getSqoopTaskExecutionContext();

            if (null != targetMysqlParameter && targetMysqlParameter.getTargetDatasource() != 0) {

                // get datasource
                BaseDataSource baseDataSource = DataSourceFactory.getDatasource(DbType.of(sqoopTaskExecutionContext.getTargetType()),
                    sqoopTaskExecutionContext.getTargetConnectionParams());

                if (null != baseDataSource) {

                    mysqlTargetSb.append(Constants.SPACE).append(SqoopConstants.DB_CONNECT)
                        .append(Constants.SPACE).append(Constants.DOUBLE_QUOTES).append(baseDataSource.getJdbcUrl()).append(Constants.DOUBLE_QUOTES)
                        .append(Constants.SPACE).append(SqoopConstants.DB_USERNAME)
                        .append(Constants.SPACE).append(baseDataSource.getUser())
                        .append(Constants.SPACE).append(SqoopConstants.DB_PWD)
                        .append(Constants.SPACE).append(Constants.DOUBLE_QUOTES).append(baseDataSource.getPassword()).append(Constants.DOUBLE_QUOTES)
                        .append(Constants.SPACE).append(SqoopConstants.TABLE)
                        .append(Constants.SPACE).append(targetMysqlParameter.getTargetTable());

                    if (StringUtils.isNotEmpty(targetMysqlParameter.getTargetColumns())) {
                        mysqlTargetSb.append(Constants.SPACE).append(SqoopConstants.COLUMNS)
                            .append(Constants.SPACE).append(targetMysqlParameter.getTargetColumns());
                    }

                    if (StringUtils.isNotEmpty(targetMysqlParameter.getFieldsTerminated())) {
                        mysqlTargetSb.append(Constants.SPACE).append(SqoopConstants.FIELDS_TERMINATED_BY);
                        if (targetMysqlParameter.getFieldsTerminated().contains("'")) {
                            mysqlTargetSb.append(Constants.SPACE).append(targetMysqlParameter.getFieldsTerminated());

                        } else {
                            mysqlTargetSb.append(Constants.SPACE).append(Constants.SINGLE_QUOTES).append(targetMysqlParameter.getFieldsTerminated()).append(Constants.SINGLE_QUOTES);
                        }
                    }

                    if (StringUtils.isNotEmpty(targetMysqlParameter.getLinesTerminated())) {
                        mysqlTargetSb.append(Constants.SPACE).append(SqoopConstants.LINES_TERMINATED_BY);
                        if (targetMysqlParameter.getLinesTerminated().contains(Constants.SINGLE_QUOTES)) {
                            mysqlTargetSb.append(Constants.SPACE).append(targetMysqlParameter.getLinesTerminated());
                        } else {
                            mysqlTargetSb.append(Constants.SPACE).append(Constants.SINGLE_QUOTES).append(targetMysqlParameter.getLinesTerminated()).append(Constants.SINGLE_QUOTES);
                        }
                    }

                    if (targetMysqlParameter.getIsUpdate()
                        && StringUtils.isNotEmpty(targetMysqlParameter.getTargetUpdateKey())
                        && StringUtils.isNotEmpty(targetMysqlParameter.getTargetUpdateMode())) {
                        mysqlTargetSb.append(Constants.SPACE).append(SqoopConstants.UPDATE_KEY)
                            .append(Constants.SPACE).append(targetMysqlParameter.getTargetUpdateKey())
                            .append(Constants.SPACE).append(SqoopConstants.UPDATE_MODE)
                            .append(Constants.SPACE).append(targetMysqlParameter.getTargetUpdateMode());
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop mysql target params build failed: [%s]", e.getMessage()));
        }

        return mysqlTargetSb.toString();
    }
}
