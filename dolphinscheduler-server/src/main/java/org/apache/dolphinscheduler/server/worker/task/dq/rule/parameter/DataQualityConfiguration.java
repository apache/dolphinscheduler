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

package org.apache.dolphinscheduler.server.worker.task.dq.rule.parameter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DataQualityConfiguration
 */
public class DataQualityConfiguration {

    @JsonProperty("name")
    private String name;

    @JsonProperty("connectors")
    private List<ConnectorParameter> connectorParameters;

    @JsonProperty("writers")
    private List<WriterParameter> writerParams;

    @JsonProperty("executors")
    private List<ExecutorParameter> executorParameters;

    public DataQualityConfiguration(){}

    public DataQualityConfiguration(String name,
                                    List<ConnectorParameter> connectorParameters,
                                    List<WriterParameter> writerParams,
                                    List<ExecutorParameter> executorParameters) {
        this.name = name;
        this.connectorParameters = connectorParameters;
        this.writerParams = writerParams;
        this.executorParameters = executorParameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ConnectorParameter> getConnectorParameters() {
        return connectorParameters;
    }

    public void setConnectorParameters(List<ConnectorParameter> connectorParameters) {
        this.connectorParameters = connectorParameters;
    }

    public List<WriterParameter> getWriterParams() {
        return writerParams;
    }

    public void setWriterParams(List<WriterParameter> writerParams) {
        this.writerParams = writerParams;
    }

    public List<ExecutorParameter> getExecutorParameters() {
        return executorParameters;
    }

    public void setExecutorParameters(List<ExecutorParameter> executorParameters) {
        this.executorParameters = executorParameters;
    }

}
