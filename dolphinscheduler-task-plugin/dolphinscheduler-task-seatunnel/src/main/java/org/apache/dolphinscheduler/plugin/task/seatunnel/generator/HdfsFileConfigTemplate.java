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

import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.seatunnel.Constants;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.SeatunnelTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.HdfsFileParameters;

import java.util.List;
import java.util.Objects;

public class HdfsFileConfigTemplate implements ConfigTemplate {

    private final SeatunnelParameters seatunnelParameters;

    private final SeatunnelTaskExecutionContext seatunnelTaskExecutionContext;

    public HdfsFileConfigTemplate(SeatunnelParameters seatunnelParameters,
                                  SeatunnelTaskExecutionContext seatunnelTaskExecutionContext) {
        Objects.requireNonNull(seatunnelParameters);
        this.seatunnelParameters = seatunnelParameters;
        this.seatunnelTaskExecutionContext = seatunnelTaskExecutionContext;
    }

    @Override
    public String initConfigTemplate() {
        return "  HdfsFile {\n" +
                "    path = \"%s\"" + "\n" +
                "    file_format_type = \"%s\"" + "\n" +
                "    fs.defaultFS = \"%s\"" + "\n";
    }

    @Override
    public String createSourceConfig() {
        HdfsFileParameters fileParameters = (HdfsFileParameters) seatunnelParameters.getSourceConfig();
        Objects.requireNonNull(fileParameters);

        String configTemplate = initConfigTemplate();
        String fillConfigTemplate = String.format(configTemplate, fileParameters.getFilePath(),
                fileParameters.getFileFormat(), fileParameters.getDefaultFs());

        StringBuilder hdfsSourceSb = new StringBuilder("source {\n");
        hdfsSourceSb.append(fillConfigTemplate);

        List<Property> customParams = fileParameters.getCustomParams();
        if (null != customParams && !customParams.isEmpty()) {
            customParams.forEach(
                    param -> hdfsSourceSb.append(Constants.INDENT_FOUR_SPACE)
                            .append(param.getProp()).append(Constants.EQUAL_SIGN)
                            .append(Constants.DOUBLE_QUOTE).append(param.getValue())
                            .append(Constants.DOUBLE_QUOTE).append(Constants.LINE_BREAK));
        }

        hdfsSourceSb.append(Constants.INDENT_TWO_SPACE).append(Constants.SINGLE_BRACKETS_RIGHT)
                .append(Constants.LINE_BREAK).append(Constants.SINGLE_BRACKETS_RIGHT);

        return hdfsSourceSb.toString();
    }

    @Override
    public String createSinkConfig() {
        HdfsFileParameters fileParameters = (HdfsFileParameters) seatunnelParameters.getSinkConfig();
        Objects.requireNonNull(fileParameters);

        String configTemplate = initConfigTemplate();
        String fillConfigTemplate = String.format(configTemplate, fileParameters.getFilePath(),
                fileParameters.getFileFormat(), fileParameters.getDefaultFs());

        StringBuilder hdfsSinkSb = new StringBuilder("sink {\n");
        hdfsSinkSb.append(fillConfigTemplate);

        List<Property> customParams = fileParameters.getCustomParams();
        if (null != customParams && !customParams.isEmpty()) {
            customParams.forEach(
                    param -> hdfsSinkSb.append(Constants.INDENT_FOUR_SPACE)
                            .append(param.getProp()).append(Constants.EQUAL_SIGN)
                            .append(Constants.DOUBLE_QUOTE).append(param.getValue())
                            .append(Constants.DOUBLE_QUOTE).append(Constants.LINE_BREAK));
        }

        hdfsSinkSb.append(Constants.INDENT_TWO_SPACE).append(Constants.SINGLE_BRACKETS_RIGHT)
                .append(Constants.LINE_BREAK).append(Constants.SINGLE_BRACKETS_RIGHT);

        return hdfsSinkSb.toString();
    }
}
