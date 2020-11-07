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

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mysql target generator
 */
public class MysqlTargetGenerator implements ITargetGenerator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String generate(SqoopParameters sqoopParameters, TaskExecutionContext taskExecutionContext) {

        LinkedList<String> mysqlTargetParamsList = new LinkedList<>();

        try {
            TargetMysqlParameter targetMysqlParameter =
                JSONUtils.parseObject(sqoopParameters.getTargetParams(), TargetMysqlParameter.class);

            SqoopTaskExecutionContext sqoopTaskExecutionContext = taskExecutionContext.getSqoopTaskExecutionContext();

            if (null != targetMysqlParameter && targetMysqlParameter.getTargetDatasource() != 0) {

                // get datasource
                BaseDataSource baseDataSource = DataSourceFactory.getDatasource(DbType.of(sqoopTaskExecutionContext.getTargetType()),
                    sqoopTaskExecutionContext.getTargetConnectionParams());

                if (null != baseDataSource) {
                    mysqlTargetParamsList.add(SqoopConstants.DB_CONNECT);
                    mysqlTargetParamsList.add(baseDataSource.getJdbcUrl());
                    mysqlTargetParamsList.add(SqoopConstants.DB_USERNAME);
                    mysqlTargetParamsList.add(baseDataSource.getUser());
                    mysqlTargetParamsList.add(SqoopConstants.DB_PWD);
                    mysqlTargetParamsList.add(baseDataSource.getPassword());
                    mysqlTargetParamsList.add(SqoopConstants.TABLE);
                    mysqlTargetParamsList.add(targetMysqlParameter.getTargetTable());

                    if (StringUtils.isNotEmpty(targetMysqlParameter.getTargetColumns())) {
                        mysqlTargetParamsList.add(SqoopConstants.COLUMNS);
                        mysqlTargetParamsList.add(targetMysqlParameter.getTargetColumns());
                    }

                    if (StringUtils.isNotEmpty(targetMysqlParameter.getFieldsTerminated())) {
                        mysqlTargetParamsList.add(SqoopConstants.FIELDS_TERMINATED_BY);
                        if (targetMysqlParameter.getFieldsTerminated().contains("'")) {
                            mysqlTargetParamsList.add(targetMysqlParameter.getFieldsTerminated());
                        } else {
                            mysqlTargetParamsList.add("'" + targetMysqlParameter.getFieldsTerminated() + "'");
                        }
                    }

                    if (StringUtils.isNotEmpty(targetMysqlParameter.getLinesTerminated())) {
                        mysqlTargetParamsList.add(SqoopConstants.LINES_TERMINATED_BY);
                        if (targetMysqlParameter.getLinesTerminated().contains("'")) {
                            mysqlTargetParamsList.add(targetMysqlParameter.getLinesTerminated());
                        } else {
                            mysqlTargetParamsList.add("'" + targetMysqlParameter.getLinesTerminated() + "'");
                        }
                    }

                    if (targetMysqlParameter.getIsUpdate()
                        && StringUtils.isNotEmpty(targetMysqlParameter.getTargetUpdateKey())
                        && StringUtils.isNotEmpty(targetMysqlParameter.getTargetUpdateMode())) {
                        mysqlTargetParamsList.add(SqoopConstants.UPDATE_KEY);
                        mysqlTargetParamsList.add(targetMysqlParameter.getTargetUpdateKey());
                        mysqlTargetParamsList.add(SqoopConstants.UPDATE_MODE);
                        mysqlTargetParamsList.add(targetMysqlParameter.getTargetUpdateMode());
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop mysql target params build failed: [%s]", e.getMessage()));
        }

        return String.join(" ", mysqlTargetParamsList);
    }
}
