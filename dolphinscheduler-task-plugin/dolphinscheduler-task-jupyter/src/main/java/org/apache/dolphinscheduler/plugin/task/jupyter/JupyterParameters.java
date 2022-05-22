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

package org.apache.dolphinscheduler.plugin.task.jupyter;

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

/**
 * jupyter parameters
 */
public class JupyterParameters extends AbstractParameters {

    /**
     * conda env name
     */
    private String condaEnvName;

    /**
     * input note path
     */
    private String inputNotePath;

    /**
     * output note path
     */
    private String outputNotePath;

    /**
     * parameters to pass into jupyter note cells
     */
    private String parameters;

    /**
     * jupyter kernel
     */
    private String kernel;

    /**
     * the execution engine name to use in evaluating the notebook
     */
    private String engine;

    /**
     * time in seconds to wait for each cell before failing execution (default: forever)
     */
    private String executionTimeout;

    /**
     * time in seconds to wait for kernel to start
     */
    private String startTimeout;

    /**
     * other arguments
     */
    private String others;


    public String getCondaEnvName() {
        return condaEnvName;
    }

    public void setCondaEnvName(String condaEnvName) {
        this.condaEnvName = condaEnvName;
    }

    public String getInputNotePath() {
        return inputNotePath;
    }

    public void setInputNotePath(String inputNotePath) {
        this.inputNotePath = inputNotePath;
    }

    public String getOutputNotePath() {
        return outputNotePath;
    }

    public void setOutputNotePath(String outputNotePath) {
        this.outputNotePath = outputNotePath;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getKernel() {
        return kernel;
    }

    public void setKernel(String kernel) {
        this.kernel = kernel;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getExecutionTimeout() {
        return executionTimeout;
    }

    public void setExecutionTimeout(String executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    public String getStartTimeout() {
        return startTimeout;
    }

    public void setStartTimeout(String startTimeout) {
        this.startTimeout = startTimeout;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    @Override
    public boolean checkParameters() {
        return condaEnvName != null && inputNotePath != null && outputNotePath != null;
    }

    @Override
    public String toString() {
        return "JupyterParameters{" +
                "condaEnvName='" + condaEnvName + '\'' +
                ", inputNotePath='" + inputNotePath + '\'' +
                ", outputNotePath='" + outputNotePath + '\'' +
                ", parameters='" + parameters + '\'' +
                ", kernel='" + kernel + '\'' +
                ", engine='" + engine + '\'' +
                ", executionTimeout=" + executionTimeout +
                ", startTimeout=" + startTimeout +
                ", others='" + others + '\'' +
                '}';
    }
}
