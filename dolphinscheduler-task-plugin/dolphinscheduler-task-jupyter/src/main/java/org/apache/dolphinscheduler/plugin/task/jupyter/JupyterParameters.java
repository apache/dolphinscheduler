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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.List;

/**
 * jupyter parameters
 */
@Getter
@Setter
@ToString
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

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList;

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return resourceList;
    }

    @Override
    public boolean checkParameters() {
        return condaEnvName != null && inputNotePath != null && outputNotePath != null;
    }

}
