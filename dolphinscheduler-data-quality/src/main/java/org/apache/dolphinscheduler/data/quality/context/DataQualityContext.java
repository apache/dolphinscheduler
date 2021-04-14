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

package org.apache.dolphinscheduler.data.quality.context;

import org.apache.dolphinscheduler.data.quality.configuration.ConnectorParameter;
import org.apache.dolphinscheduler.data.quality.configuration.ExecutorParameter;
import org.apache.dolphinscheduler.data.quality.configuration.WriterParameter;

import org.apache.spark.sql.SparkSession;

import java.util.List;

/**
 * DataQualityContext
 */
public class DataQualityContext {

    private SparkSession sparkSession;

    private List<ConnectorParameter> connectorParameterList;

    private List<ExecutorParameter> executorParameterList;

    private List<WriterParameter> writerParamList;

    public DataQualityContext() {
    }

    public DataQualityContext(SparkSession sparkSession,
                              List<ConnectorParameter> connectorParameterList,
                              List<ExecutorParameter> executorParameterList,
                              List<WriterParameter> writerParamList) {
        this.sparkSession = sparkSession;
        this.connectorParameterList = connectorParameterList;
        this.executorParameterList = executorParameterList;
        this.writerParamList = writerParamList;
    }

    public SparkSession getSparkSession() {
        return sparkSession;
    }

    public void setSparkSession(SparkSession sparkSession) {
        this.sparkSession = sparkSession;
    }

    public List<ConnectorParameter> getConnectorParameterList() {
        return connectorParameterList;
    }

    public void setConnectorParameterList(List<ConnectorParameter> connectorParameterList) {
        this.connectorParameterList = connectorParameterList;
    }

    public List<ExecutorParameter> getExecutorParameterList() {
        return executorParameterList;
    }

    public void setExecutorParameterList(List<ExecutorParameter> executorParameterList) {
        this.executorParameterList = executorParameterList;
    }

    public List<WriterParameter> getWriterParamList() {
        return writerParamList;
    }

    public void setWriterParamList(List<WriterParameter> writerParamList) {
        this.writerParamList = writerParamList;
    }
}
