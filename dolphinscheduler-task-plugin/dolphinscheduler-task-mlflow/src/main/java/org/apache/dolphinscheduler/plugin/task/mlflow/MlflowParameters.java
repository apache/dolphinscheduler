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

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import lombok.Data;

@Data
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

    private String mlflowProjectVersion = "";

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

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(mlflowTrackingUri);
    }

    public HashMap<String, String> getParamsMap() {

        HashMap<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("params", params);
        paramsMap.put("data_path", dataPath);
        paramsMap.put("experiment_name", experimentName);
        paramsMap.put("model_name", modelName);
        paramsMap.put("MLFLOW_TRACKING_URI", mlflowTrackingUri);
        switch (mlflowJobType) {
            case MlflowConstants.JOB_TYPE_BASIC_ALGORITHM:
                addParamsMapForBasicAlgorithm(paramsMap);
                break;
            case MlflowConstants.JOB_TYPE_AUTOML:
                getParamsMapForAutoML(paramsMap);
                break;
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

    public Boolean isCustomProject() {
        return mlflowJobType.equals(MlflowConstants.JOB_TYPE_CUSTOM_PROJECT);
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
        imageName = imageName.replace("/", tag).toLowerCase();
        return imageName;
    }

    public String getContainerName() {
        return "ds-mlflow-" + getModelKeyName("-");
    }

    public boolean getIsDeployDocker() {
        if (StringUtils.isEmpty(deployType)) {
            return false;
        }
        return deployType.equals(MlflowConstants.MLFLOW_MODELS_DEPLOY_TYPE_DOCKER);
    }
}
