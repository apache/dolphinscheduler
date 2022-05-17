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

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import java.util.HashMap;

public class MlflowParameters extends AbstractParameters {

    /**
     * common parameters
     */

    private String params = "";

    private String mlflowJobType = "BasicAlgorithm";

    /**
     * AutoML parameters
     */
    private String automlTool = "FLAML";


    /**
     * basic algorithm parameters
     */

    private String algorithm = "lightgbm";

    private String searchParams = "";

    private String dataPath;

    /**
     * mlflow parameters
     */

    private String experimentName;

    private String modelName = "";

    private String mlflowTrackingUri = "http://127.0.0.1:5000";


    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getParams() {
        return params;
    }

    public void setSearchParams(String searchParams) {
        this.searchParams = searchParams;
    }

    public String getSearchParams() {
        return searchParams;
    }

    public void setDataPaths(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getDataPath() {
        return dataPath;
    }


    public void setExperimentNames(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setModelNames(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setMlflowTrackingUris(String mlflowTrackingUri) {
        this.mlflowTrackingUri = mlflowTrackingUri;
    }

    public String getMlflowTrackingUri() {
        return mlflowTrackingUri;
    }

    public void setMlflowJobType(String mlflowJobType) {
        this.mlflowJobType = mlflowJobType;
    }

    public String getMlflowJobType() {
        return mlflowJobType;
    }

    public void setAutomlTool(String automlTool) {
        this.automlTool = automlTool;
    }

    public String getAutomlTool() {
        return automlTool;
    }

    @Override
    public boolean checkParameters() {

        Boolean checkResult = experimentName != null && mlflowTrackingUri != null;
        if (mlflowJobType.equals(MlflowConstants.JOB_TYPE_BASIC_ALGORITHM)) {
            checkResult &= dataPath != null;
        } else if (mlflowJobType.equals(MlflowConstants.JOB_TYPE_AUTOML)) {
            checkResult &= dataPath != null;
            checkResult &= automlTool != null;
        } else {
        }
        return checkResult;
    }

    public HashMap<String, String> getParamsMap() {

        HashMap<String, String> paramsMap = new HashMap<String, String>() {{
            put("params", params);
            put("data_path", dataPath);
            put("experiment_name", experimentName);
            put("model_name", modelName);
            put("MLFLOW_TRACKING_URI", mlflowTrackingUri);
        }};
        if (mlflowJobType.equals(MlflowConstants.JOB_TYPE_BASIC_ALGORITHM)) {
            addParamsMapForBasicAlgorithm(paramsMap);
        } else if (mlflowJobType.equals(MlflowConstants.JOB_TYPE_AUTOML)) {
            getParamsMapForAutoML(paramsMap);
        } else {
        }
        return paramsMap;
    }

    private void addParamsMapForBasicAlgorithm(HashMap<String, String> paramsMap) {
        paramsMap.put("algorithm", algorithm);
        paramsMap.put("search_params", searchParams);
        paramsMap.put("repo", MlflowConstants.PRESET_BASIC_ALGORITHM_PROJECT);
        paramsMap.put("repo_version", MlflowConstants.PRESET_REPOSITORY_VERSION);
    }

    private void getParamsMapForAutoML(HashMap<String, String> paramsMap) {
        paramsMap.put("automl_tool", automlTool);
        paramsMap.put("repo", MlflowConstants.PRESET_AUTOML_PROJECT);
        paramsMap.put("repo_version", MlflowConstants.PRESET_REPOSITORY_VERSION);
    }

    public String getScriptPath() {
        String projectScript;
        if (mlflowJobType.equals(MlflowConstants.JOB_TYPE_BASIC_ALGORITHM)) {
            projectScript = MlflowConstants.RUN_PROJECT_BASIC_ALGORITHM_SCRIPT;
        } else if (mlflowJobType.equals(MlflowConstants.JOB_TYPE_AUTOML)) {
            projectScript = MlflowConstants.RUN_PROJECT_AUTOML_SCRIPT;
        } else {
            throw new IllegalArgumentException();
        }
        String scriptPath = MlflowTask.class.getClassLoader().getResource(projectScript).getPath();
        return scriptPath;
    }

};
