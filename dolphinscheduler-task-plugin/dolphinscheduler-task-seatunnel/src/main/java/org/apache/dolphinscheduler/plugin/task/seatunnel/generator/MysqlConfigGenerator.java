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

package org.apache.dolphinscheduler.plugin.task.seatunnel.generator;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.constants.DataSourceConstants;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.seatunnel.Constants;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.MysqlParameters;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.List;
import java.util.Objects;

public class MysqlConfigGenerator implements IConfigGenerator {

    private final SeatunnelParameters seatunnelParameters;

    private final SeatunnelTaskExecutionContext seatunnelTaskExecutionContext;

    public MysqlConfigGenerator(SeatunnelParameters seatunnelParameters,
                                SeatunnelTaskExecutionContext seatunnelTaskExecutionContext) {
        this.seatunnelParameters = seatunnelParameters;
        this.seatunnelTaskExecutionContext = seatunnelTaskExecutionContext;
    }

    private String initConfigTemplate() {
        return "  Jdbc {\n" +
                "    url = \"%s\"" + "\n" +
                "    driver = \"%s\"" + "\n" +
                "    user = \"%s\"" + "\n" +
                "    password = \"%s\"" + "\n";
    }

    @Override
    public String createSourceConfig() {
        MysqlParameters mysqlParameters =
                JSONUtils.parseObject(seatunnelParameters.getSourceConfig(), MysqlParameters.class);

        String connectionParams = seatunnelTaskExecutionContext.getSourceConnectionParams();
        BaseConnectionParam sourceConnParams =
                (BaseConnectionParam) DataSourceUtils.buildConnectionParams(DbType.MYSQL, connectionParams);
        Objects.requireNonNull(sourceConnParams);
        Objects.requireNonNull(mysqlParameters);

        StringBuilder mysqlSourceSb = new StringBuilder("source {\n");

        String configTemplate = initConfigTemplate();
        configTemplate = configTemplate + Constants.MYSQL_QUERY_PARAMS;

        String fillConfigTemplate = String.format(configTemplate,
                buildJdbcUrl(sourceConnParams.getJdbcUrl()),
                DataSourceConstants.COM_MYSQL_CJ_JDBC_DRIVER,
                sourceConnParams.getUser(),
                sourceConnParams.getPassword(),
                buildDefaultQuery(mysqlParameters.getTable()));

        mysqlSourceSb.append(fillConfigTemplate);

        List<Property> customParams = mysqlParameters.getCustomParams();
        if (null != customParams && !customParams.isEmpty()) {
            customParams.forEach(
                    param -> mysqlSourceSb
                            .append(Constants.INDENT_FOUR_SPACE)
                            .append(param.getProp())
                            .append(Constants.EQUAL_SIGN)
                            .append(Constants.DOUBLE_QUOTE)
                            .append(param.getValue())
                            .append(Constants.DOUBLE_QUOTE)
                            .append(Constants.LINE_BREAK));
        }

        mysqlSourceSb
                .append(Constants.INDENT_TWO_SPACE)
                .append(Constants.SINGLE_BRACKETS_RIGHT)
                .append(Constants.LINE_BREAK)
                .append(Constants.SINGLE_BRACKETS_RIGHT);

        return mysqlSourceSb.toString();
    }

    @Override
    public String createSinkConfig() {
        MysqlParameters mysqlParameters =
                JSONUtils.parseObject(seatunnelParameters.getTargetConfig(), MysqlParameters.class);

        String connectionParams = seatunnelTaskExecutionContext.getTargetConnectionParams();
        BaseConnectionParam targetConnParams =
                (BaseConnectionParam) DataSourceUtils.buildConnectionParams(DbType.MYSQL, connectionParams);
        Objects.requireNonNull(targetConnParams);
        Objects.requireNonNull(mysqlParameters);

        StringBuilder mysqlSinkSb = new StringBuilder("sink {\n");

        String configTemplate = initConfigTemplate();

        // lowercase jdbc is used in seatunnel's mysql sink
        configTemplate = configTemplate.replace("Jdbc", "jdbc");
        configTemplate += Constants.MYSQL_EXTRA_SINK_PARAMS;

        String fillConfigTemplate = String.format(configTemplate,
                buildJdbcUrl(targetConnParams.getJdbcUrl()),
                DataSourceConstants.COM_MYSQL_CJ_JDBC_DRIVER,
                targetConnParams.getUser(),
                targetConnParams.getPassword(),
                targetConnParams.getDatabase(),
                mysqlParameters.getTable(),
                true);

        mysqlSinkSb.append(fillConfigTemplate);

        // add custom config
        List<Property> customParams = mysqlParameters.getCustomParams();
        if (null != customParams && !customParams.isEmpty()) {
            customParams.forEach(
                    param -> mysqlSinkSb
                            .append(Constants.INDENT_FOUR_SPACE)
                            .append(param.getProp())
                            .append(Constants.EQUAL_SIGN)
                            .append(Constants.DOUBLE_QUOTE)
                            .append(param.getValue())
                            .append(Constants.DOUBLE_QUOTE)
                            .append(Constants.LINE_BREAK));
        }

        mysqlSinkSb
                .append(Constants.INDENT_TWO_SPACE)
                .append(Constants.SINGLE_BRACKETS_RIGHT)
                .append(Constants.LINE_BREAK)
                .append(Constants.SINGLE_BRACKETS_RIGHT);

        return mysqlSinkSb.toString();
    }

    private static String buildJdbcUrl(String jdbcUrl) {
        return jdbcUrl + "?" + "serverTimezone=GMT%2b8&useUnicode=true&characterEncoding=UTF-8";
    }

    private static String buildDefaultQuery(String table) {
        return Constants.MYSQL_DEFAULT_QUERY_PREFIX + table;
    }
}
