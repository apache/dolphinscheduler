 
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.datasource.DatasourceUtil;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetJDBCParameter;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.entity.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.SqoopConstants;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.ITargetGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC target generator
 */
public class JDBCTargetGenerator implements ITargetGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JDBCTargetGenerator.class);

    @Override
    public String generate(SqoopParameters sqoopParameters, TaskExecutionContext taskExecutionContext) {

        StringBuilder JDBCTargetSb = new StringBuilder();

        try {
            TargetJDBCParameter targetJDBCParameter =
                JSONUtils.parseObject(sqoopParameters.getTargetParams(), TargetJDBCParameter.class);

            SqoopTaskExecutionContext sqoopTaskExecutionContext = taskExecutionContext.getSqoopTaskExecutionContext();

            if (null != targetJDBCParameter && targetJDBCParameter.getTargetDatasource() != 0) {

                // get datasource
                BaseConnectionParam baseDataSource = (BaseConnectionParam) DatasourceUtil.buildConnectionParams(
                        DbType.of(sqoopTaskExecutionContext.getTargetType()),
                    sqoopTaskExecutionContext.getTargetConnectionParams());

                if (null != baseDataSource) {



 JDBCTargetSb.append(Constants.SPACE).append(SqoopConstants.DB_CONNECT)
                        .append(Constants.SPACE).append(Constants.DOUBLE_QUOTES).append(baseDataSource.getJdbcUrl()).append(Constants.DOUBLE_QUOTES)
                        .append(Constants.SPACE).append(SqoopConstants.DB_USERNAME)
                        .append(Constants.SPACE).append(baseDataSource.getUser())
                        .append(Constants.SPACE).append(SqoopConstants.DB_PWD)
                        .append(Constants.SPACE).append(Constants.DOUBLE_QUOTES).append(baseDataSource.getPassword()).append(Constants.DOUBLE_QUOTES)
                        .append(Constants.SPACE).append(SqoopConstants.TABLE)
                        .append(Constants.SPACE).append(targetJDBCParameter.getTargetTable());
                        
                    if (StringUtils.isNotEmpty(targetJDBCParameter.getTargetColumns())) {
                        JDBCTargetSb.append(Constants.SPACE).append(SqoopConstants.COLUMNS)
                            .append(Constants.SPACE).append(targetJDBCParameter.getTargetColumns());
                    }

                    if (StringUtils.isNotEmpty(targetJDBCParameter.getFieldsTerminated())) {
                        JDBCTargetSb.append(Constants.SPACE).append(SqoopConstants.FIELDS_TERMINATED_BY);
                        if (targetJDBCParameter.getFieldsTerminated().contains("'")) {
                            JDBCTargetSb.append(Constants.SPACE).append(targetJDBCParameter.getFieldsTerminated());

                        } else {
                            JDBCTargetSb.append(Constants.SPACE).append(Constants.SINGLE_QUOTES).append(targetJDBCParameter.getFieldsTerminated()).append(Constants.SINGLE_QUOTES);
                        }
                    }

                    if (StringUtils.isNotEmpty(targetJDBCParameter.getLinesTerminated())) {
                        JDBCTargetSb.append(Constants.SPACE).append(SqoopConstants.LINES_TERMINATED_BY);
                        if (targetJDBCParameter.getLinesTerminated().contains(Constants.SINGLE_QUOTES)) {
                            JDBCTargetSb.append(Constants.SPACE).append(targetJDBCParameter.getLinesTerminated());
                        } else {
                            JDBCTargetSb.append(Constants.SPACE).append(Constants.SINGLE_QUOTES).append(targetJDBCParameter.getLinesTerminated()).append(Constants.SINGLE_QUOTES);
                        }
                    }

                    if (targetJDBCParameter.getIsUpdate()
                        && StringUtils.isNotEmpty(targetJDBCParameter.getTargetUpdateKey())
                        && StringUtils.isNotEmpty(targetJDBCParameter.getTargetUpdateMode())) {
                        JDBCTargetSb.append(Constants.SPACE).append(SqoopConstants.UPDATE_KEY)
                            .append(Constants.SPACE).append(targetJDBCParameter.getTargetUpdateKey())
                            .append(Constants.SPACE).append(SqoopConstants.UPDATE_MODE)
                            .append(Constants.SPACE).append(targetJDBCParameter.getTargetUpdateMode());
                    }
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Sqoop JDBC target params build failed: [%s]", e.getMessage()));
        }

        return JDBCTargetSb.toString();
    }
}