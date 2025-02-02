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

import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.seatunnel.Constants;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.DorisParameters;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DorisConfigTemplate implements ConfigTemplate {

    private final SeatunnelParameters seatunnelParameters;

    private final SeatunnelTaskExecutionContext seatunnelTaskExecutionContext;

    public DorisConfigTemplate(SeatunnelParameters seatunnelSimplifyParameters,
                               SeatunnelTaskExecutionContext seatunnelTaskExecutionContext) {
        this.seatunnelParameters = seatunnelSimplifyParameters;
        this.seatunnelTaskExecutionContext = seatunnelTaskExecutionContext;
    }

    @Override
    public String initConfigTemplate() {
        return "  Doris {\n" +
                "    fenodes = \"%s\"" + "\n" +
                "    username = \"%s\"" + "\n" +
                "    password = \"%s\"" + "\n" +
                "    database = \"%s\"" + "\n" +
                "    table = \"%s\"" + "\n";
    }

    @Override
    public String createSourceConfig() {
        DorisParameters dorisConfigParameters = (DorisParameters) seatunnelParameters.getSourceConfig();
        String connectionParams = seatunnelTaskExecutionContext.getSourceConnectionParams();
        BaseConnectionParam sourceConnParams =
                (BaseConnectionParam) DataSourceUtils.buildConnectionParams(DbType.DORIS, connectionParams);
        Objects.requireNonNull(sourceConnParams);
        Objects.requireNonNull(dorisConfigParameters);

        List<String> hostsAndPort = getHostAndPortFromConnectionParams(sourceConnParams);

        String fenodes = hostsAndPort.get(0) + ":" + hostsAndPort.get(1);
        if (StringUtils.isEmpty(fenodes)) {
            throw new NullPointerException("get source fenodes is null. connection params => " + sourceConnParams);
        }

        StringBuilder dorisSourceSb = new StringBuilder("source {\n");

        String configTemplate = initConfigTemplate();
        String fillConfigTemplate = String.format(configTemplate, fenodes, sourceConnParams.getUser(),
                sourceConnParams.getPassword(), sourceConnParams.getDatabase(), dorisConfigParameters.getTable());

        dorisSourceSb.append(fillConfigTemplate);

        List<Property> customParams = dorisConfigParameters.getCustomParams();
        if (null != customParams && !customParams.isEmpty()) {
            customParams.forEach(
                    param -> dorisSourceSb.append(Constants.INDENT_FOUR_SPACE)
                            .append(param.getProp()).append(Constants.EQUAL_SIGN)
                            .append(Constants.DOUBLE_QUOTE).append(param.getValue())
                            .append(Constants.DOUBLE_QUOTE).append(Constants.LINE_BREAK));
        }

        dorisSourceSb.append(Constants.INDENT_TWO_SPACE).append(Constants.SINGLE_BRACKETS_RIGHT)
                .append(Constants.LINE_BREAK).append(Constants.SINGLE_BRACKETS_RIGHT);

        return dorisSourceSb.toString();
    }

    @Override
    public String createSinkConfig() {
        DorisParameters dorisConfigParameters = (DorisParameters) seatunnelParameters.getSinkConfig();
        String connectionParams = seatunnelTaskExecutionContext.getTargetConnectionParams();
        BaseConnectionParam targetConnParams =
                (BaseConnectionParam) DataSourceUtils.buildConnectionParams(DbType.DORIS, connectionParams);
        Objects.requireNonNull(targetConnParams);
        Objects.requireNonNull(dorisConfigParameters);

        List<String> hostsAndPort = getHostAndPortFromConnectionParams(targetConnParams);

        String fenodes = hostsAndPort.get(0) + ":" + hostsAndPort.get(1);

        if (StringUtils.isEmpty(fenodes)) {
            throw new NullPointerException("get target fenodes is null. connection params => " + targetConnParams);
        }

        String configTemplate = initConfigTemplate();
        String fillConfigTemplate =
                String.format(configTemplate, fenodes, targetConnParams.getUser(), targetConnParams.getPassword(),
                        targetConnParams.getDatabase(), dorisConfigParameters.getTable());

        StringBuilder dorisSinkSb = new StringBuilder("sink {\n");
        dorisSinkSb.append(fillConfigTemplate);

        // add custom config
        List<Property> customParams = dorisConfigParameters.getCustomParams();
        if (null != customParams && !customParams.isEmpty()) {
            customParams.forEach(
                    param -> dorisSinkSb.append(Constants.INDENT_FOUR_SPACE)
                            .append(param.getProp()).append(Constants.EQUAL_SIGN)
                            .append(Constants.DOUBLE_QUOTE).append(param.getValue())
                            .append(Constants.DOUBLE_QUOTE).append(Constants.LINE_BREAK));
        }

        // create doris.config in sink, default format=json
        if (!dorisSinkSb.toString().contains(Constants.DORIS_CONFIG)) {
            String dorisConfigTemplate = "    doris.config {\n" +
                    "      format = \"%s\"" + "\n" +
                    "      read_json_by_line = \"%s\"" + "\n" +
                    "    }\n";

            dorisSinkSb.append(String.format(dorisConfigTemplate, Constants.FORMAT_JSON, Constants.TRUE));
        }

        dorisSinkSb.append(Constants.INDENT_TWO_SPACE).append(Constants.SINGLE_BRACKETS_RIGHT)
                .append(Constants.LINE_BREAK).append(Constants.SINGLE_BRACKETS_RIGHT);

        return dorisSinkSb.toString();
    }

    private List<String> getHostAndPortFromConnectionParams(BaseConnectionParam baseConnectionParam) {
        String address = baseConnectionParam.getAddress();
        String[] hostSplits = address.split(org.apache.dolphinscheduler.common.constants.Constants.DOUBLE_SLASH);
        String[] hostPortArr =
                hostSplits[hostSplits.length - 1].split(org.apache.dolphinscheduler.common.constants.Constants.COMMA);

        List<String[]> hostPortSplits =
                Arrays.stream(hostPortArr)
                        .map(v -> v.split(org.apache.dolphinscheduler.common.constants.Constants.COLON))
                        .collect(Collectors.toList());

        String hosts = hostPortSplits.stream().map(v -> v[0]).collect(Collectors.joining(","));
        String port = hostPortSplits.get(0)[1];

        return Arrays.asList(hosts, port);

    }
}
