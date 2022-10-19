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

package org.apache.dolphinscheduler.plugin.task.dq.rule.parameter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DataQualityConfiguration
 * The reader is responsible for connecting to the data source,
 * and the transformer is responsible for transformer the data that from reader,
 * and the writer is responsible for writing data to the target datasource
 */
public class DataQualityConfiguration {

    @JsonProperty("name")
    private String name;

    @JsonProperty("env")
    private EnvConfig envConfig;

    @JsonProperty("readers")
    private List<BaseConfig> readerConfigs;

    @JsonProperty("transformers")
    private List<BaseConfig> transformerConfigs;

    @JsonProperty("writers")
    private List<BaseConfig> writerConfigs;

    public DataQualityConfiguration() {
    }

    public DataQualityConfiguration(String name,
                                    List<BaseConfig> readerConfigs,
                                    List<BaseConfig> writerConfigs,
                                    List<BaseConfig> transformerConfigs) {
        this.name = name;
        this.readerConfigs = readerConfigs;
        this.writerConfigs = writerConfigs;
        this.transformerConfigs = transformerConfigs;
    }

    public DataQualityConfiguration(String name,
                                    EnvConfig envConfig,
                                    List<BaseConfig> readerConfigs,
                                    List<BaseConfig> writerConfigs,
                                    List<BaseConfig> transformerConfigs) {
        this.name = name;
        this.envConfig = envConfig;
        this.readerConfigs = readerConfigs;
        this.writerConfigs = writerConfigs;
        this.transformerConfigs = transformerConfigs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnvConfig getEnvConfig() {
        return envConfig;
    }

    public void setEnvConfig(EnvConfig envConfig) {
        this.envConfig = envConfig;
    }

    public List<BaseConfig> getReaderConfigs() {
        return readerConfigs;
    }

    public void setReaderConfigs(List<BaseConfig> readerConfigs) {
        this.readerConfigs = readerConfigs;
    }

    public List<BaseConfig> getTransformerConfigs() {
        return transformerConfigs;
    }

    public void setTransformerConfigs(List<BaseConfig> transformerConfigs) {
        this.transformerConfigs = transformerConfigs;
    }

    public List<BaseConfig> getWriterConfigs() {
        return writerConfigs;
    }

    public void setWriterConfigs(List<BaseConfig> writerConfigs) {
        this.writerConfigs = writerConfigs;
    }

    @Override
    public String toString() {
        return "DataQualityConfiguration{"
                + "name='" + name + '\''
                + ", envConfig=" + envConfig
                + ", readerConfigs=" + readerConfigs
                + ", transformerConfigs=" + transformerConfigs
                + ", writerConfigs=" + writerConfigs
                + '}';
    }
}
