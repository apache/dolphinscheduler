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
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.seatunnel.Constants;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.DorisParameters;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.List;
import java.util.Objects;

public class DorisConfigGenerator implements IConfigGenerator {

    private final SeatunnelParameters seatunnelParameters;

    private final SeatunnelTaskExecutionContext seatunnelTaskExecutionContext;

    public DorisConfigGenerator(SeatunnelParameters seatunnelSimplifyParameters,
                                SeatunnelTaskExecutionContext seatunnelTaskExecutionContext) {
        this.seatunnelParameters = seatunnelSimplifyParameters;
        this.seatunnelTaskExecutionContext = seatunnelTaskExecutionContext;
    }

    private String initConfigTemplate() {
        return "  Doris {\n" +
                "    fenodes = \"%s\"" + "\n" +
                "    username = \"%s\"" + "\n" +
                "    password = \"%s\"" + "\n" +
                "    database = \"%s\"" + "\n" +
                "    table = \"%s\"" + "\n";
    }

    @Override
    public String createSourceConfig() {
        DorisParameters dorisParameters =
                JSONUtils.parseObject(
                        seatunnelParameters.getSourceConfig(), DorisParameters.class);

        String connectionParams = seatunnelTaskExecutionContext.getSourceConnectionParams();
        BaseConnectionParam sourceConnParams =
                (BaseConnectionParam) DataSourceUtils.buildConnectionParams(DbType.DORIS, connectionParams);
        Objects.requireNonNull(sourceConnParams);
        Objects.requireNonNull(dorisParameters);

        String feNodes =
                getHosts(seatunnelTaskExecutionContext.getSourceConnectionParams()) + ":" + Constants.DORIS_HTTP_PORT;

        StringBuilder dorisSourceSb = new StringBuilder("source {\n");

        String configTemplate = initConfigTemplate();
        String fillConfigTemplate = String.format(
                configTemplate,
                feNodes,
                sourceConnParams.getUser(),
                sourceConnParams.getPassword(),
                sourceConnParams.getDatabase(),
                dorisParameters.getTable());

        dorisSourceSb.append(fillConfigTemplate);

        List<Property> customParams = dorisParameters.getCustomParams();
        if (null != customParams && !customParams.isEmpty()) {
            customParams.forEach(
                    param -> dorisSourceSb
                            .append(Constants.INDENT_FOUR_SPACE)
                            .append(param.getProp())
                            .append(Constants.EQUAL_SIGN)
                            .append(Constants.DOUBLE_QUOTE)
                            .append(param.getValue())
                            .append(Constants.DOUBLE_QUOTE)
                            .append(Constants.LINE_BREAK));
        }

        dorisSourceSb
                .append(Constants.INDENT_TWO_SPACE)
                .append(Constants.SINGLE_BRACKETS_RIGHT)
                .append(Constants.LINE_BREAK)
                .append(Constants.SINGLE_BRACKETS_RIGHT);

        return dorisSourceSb.toString();
    }

    @Override
    public String createSinkConfig() {
        DorisParameters dorisParameters =
                JSONUtils.parseObject(
                        seatunnelParameters.getTargetConfig(), DorisParameters.class);

        String connectionParams = seatunnelTaskExecutionContext.getTargetConnectionParams();
        BaseConnectionParam targetConnParams =
                (BaseConnectionParam) DataSourceUtils.buildConnectionParams(DbType.DORIS, connectionParams);
        Objects.requireNonNull(targetConnParams);
        Objects.requireNonNull(dorisParameters);

        String feNodes =
                getHosts(seatunnelTaskExecutionContext.getTargetConnectionParams()) + ":" + Constants.DORIS_HTTP_PORT;

        String configTemplate = initConfigTemplate();
        String fillConfigTemplate = String.format(
                configTemplate,
                feNodes,
                targetConnParams.getUser(),
                targetConnParams.getPassword(),
                targetConnParams.getDatabase(),
                dorisParameters.getTable());

        StringBuilder dorisSinkSb = new StringBuilder("sink {\n");
        dorisSinkSb.append(fillConfigTemplate);

        // add custom config
        List<Property> customParams = dorisParameters.getCustomParams();
        if (null != customParams && !customParams.isEmpty()) {
            customParams.forEach(
                    param -> dorisSinkSb
                            .append(Constants.INDENT_FOUR_SPACE)
                            .append(param.getProp())
                            .append(Constants.EQUAL_SIGN)
                            .append(Constants.DOUBLE_QUOTE)
                            .append(param.getValue())
                            .append(Constants.DOUBLE_QUOTE)
                            .append(Constants.LINE_BREAK));
        }

        // create doris.config in sink, default format=json
        if (!dorisSinkSb.toString().contains(Constants.DORIS_CONFIG)) {
            String dorisConfigTemplate = Constants.DORIS_CONFIG_SINK_TEMPLATE;

            dorisSinkSb
                    .append(String.format(dorisConfigTemplate,
                            Constants.FORMAT_JSON,
                            Constants.READ_JSON_BY_LINE_VALUE));
        }

        dorisSinkSb
                .append(Constants.INDENT_TWO_SPACE)
                .append(Constants.SINGLE_BRACKETS_RIGHT)
                .append(Constants.LINE_BREAK)
                .append(Constants.SINGLE_BRACKETS_RIGHT);

        return dorisSinkSb.toString();
    }

    private String getHosts(String connectionParams) {

        BaseDataSourceParamDTO datasourceParamDTO =
                DataSourceUtils.getDatasourceProcessor(DbType.DORIS).createDatasourceParamDTO(connectionParams);
        if (null == datasourceParamDTO) {
            throw new RuntimeException("Doris get hosts error. connection params => " + connectionParams);
        }

        return datasourceParamDTO.getHost();
    }
}
