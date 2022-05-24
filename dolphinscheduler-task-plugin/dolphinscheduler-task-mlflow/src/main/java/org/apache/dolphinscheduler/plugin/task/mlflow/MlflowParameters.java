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

    private String mlflowJobType = "";

    /**
     * CustomProject parameters
     */
    private String mlflowProjectRepository;

    private String mlflowProjectVersion = "master";

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

    private String mlflowTaskType = "";

    private String experimentName = "Default";

    private String modelName = "";

    private String mlflowTrackingUri = "http://127.0.0.1:5000";

    /**
     * mlflow models deploy parameters
     */

    private String deployType;

    private String deployModelKey;

    private String deployPort;

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

    public void setMlflowTaskType(String mlflowTaskType) {
        this.mlflowTaskType = mlflowTaskType;
    }

    public String getMlflowTaskType() {
        return mlflowTaskType;
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

    public String getMlflowProjectRepository() {
        return mlflowProjectRepository;
    }

    public void setMlflowProjectRepository(String mlflowProjectRepository) {
        this.mlflowProjectRepository = mlflowProjectRepository;
    }

    public String getMlflowProjectVersion() {
        return mlflowProjectVersion;
    }

    public void setMlflowProjectVersion(String mlflowProjectVersion) {
        this.mlflowProjectVersion = mlflowProjectVersion;
    }

    public String getAutomlTool() {
        return automlTool;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public String getDeployType() {
        return deployType;
    }

    public void setDeployModelKey(String deployModelKey) {
        this.deployModelKey = deployModelKey;
    }

    public String getDeployModelKey() {
        return deployModelKey;
    }

    public void setDeployPort(String deployPort) {
        this.deployPort = deployPort;
    }

    public String getDeployPort() {
        return deployPort;
    }

    @Override
    public boolean checkParameters() {
        Boolean checkResult = true;
//        Boolean checkResult = mlflowTrackingUri != null;
//        if (mlflowJobType.equals(MlflowConstants.JOB_TYPE_BASIC_ALGORITHM)) {
//            checkResult &= dataPath != null;
//            checkResult &= experimentName != null;
//        } else if (mlflowJobType.equals(MlflowConstants.JOB_TYPE_AUTOML)) {
//            checkResult &= dataPath != null;
//            checkResult &= automlTool != null;
//            checkResult &= experimentName != null;
//        } else {
//        }
        return checkResult;
    }

    public HashMap<String, String> getParamsMap() {

        HashMap<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("params", params);
        paramsMap.put("data_path", dataPath);
        paramsMap.put("experiment_name", experimentName);
        paramsMap.put("model_name", modelName);
        paramsMap.put("MLFLOW_TRACKING_URI", mlflowTrackingUri);
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

    public String getModelKeyName(String tag) throws IllegalArgumentException {
        String imageName;
        if (deployModelKey.startsWith("runs:")) {
            imageName = deployModelKey.replace("runs:/", "");
        } else if (deployModelKey.startsWith("models:")) {
            imageName = deployModelKey.replace("models:/", "");
        } else {
            throw new IllegalArgumentException("model key must start with runs:/ or models:/ ");
        }
        imageName = imageName.replace("/", tag);
        return imageName;
    }

};
