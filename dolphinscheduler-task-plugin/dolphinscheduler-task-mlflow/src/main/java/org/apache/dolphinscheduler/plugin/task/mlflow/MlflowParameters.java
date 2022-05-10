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

package org.apache.dolphinscheduler.plugin.task.mlflow;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MlflowParameters extends AbstractParameters {

    /**
     * shell script
     */
    private String rawScript;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList;

    private String algorithm = "lightgbm";

    private String params = "";

    private String searchParams = "";

    private String dataPath;

    private String experimentName;

    private String modelName;

    private String mlflowTrackingUri = "http://127.0.0.1:5000";


    public String getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getParams() {
        return this.params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getSearchParams() {
        return this.searchParams;
    }

    public void setSearchParams(String searchParams) {
        this.searchParams = searchParams;
    }


    public String getDataPath() {
        return this.dataPath;
    }

    public void setDataPaths(String dataPath) {
        this.dataPath = dataPath;
    }


    public String getExperimentName() {
        return this.experimentName;
    }

    public void setExperimentNames(String experimentName) {
        this.experimentName = experimentName;
    }


    public String getModelName() {
        return this.modelName;
    }

    public void setModelNames(String modelName) {
        this.modelName = modelName;
    }


    public String getMlflowTrackingUri() {
        return this.mlflowTrackingUri;
    }

    public void setMlflowTrackingUris(String mlflowTrackingUri) {
        this.mlflowTrackingUri = mlflowTrackingUri;
    }


    @Override
    public boolean checkParameters() {
        return dataPath != null && experimentName != null && modelName != null;
    }

    public HashMap<String, String> getParamsMap() {

        HashMap<String, String> paramsMap = new HashMap<String, String>() {{
            put("algorithm", algorithm);
            put("params", params);
            put("search_params", searchParams);
            put("data_path", dataPath);
            put("experiment_name", experimentName);
            put("model_name", modelName);
            put("MLFLOW_TRACKING_URI", mlflowTrackingUri);
            put("repo", MlflowConstants.PRESET_SKLEARN_PROJECT);
        }};
        return paramsMap;

    }


}