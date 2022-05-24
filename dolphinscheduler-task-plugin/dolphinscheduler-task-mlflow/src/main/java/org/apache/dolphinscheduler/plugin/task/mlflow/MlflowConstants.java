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

public class MlflowConstants {
    private MlflowConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String JOB_TYPE_AUTOML = "AutoML";

    public static final String JOB_TYPE_BASIC_ALGORITHM = "BasicAlgorithm";

    public static final String JOB_TYPE_CUSTOM_PROJECT = "CustomProject";

    public static final String PRESET_REPOSITORY = "https://github.com/apache/dolphinscheduler-mlflow";

    public static final String PRESET_REPOSITORY_VERSION = "main";

    public static final String PRESET_AUTOML_PROJECT = PRESET_REPOSITORY + "#Project-AutoML";

    public static final String PRESET_BASIC_ALGORITHM_PROJECT = PRESET_REPOSITORY + "#Project-BasicAlgorithm";

    public static final String RUN_PROJECT_BASIC_ALGORITHM_SCRIPT = "run_mlflow_basic_algorithm_project.sh";

    public static final String RUN_PROJECT_AUTOML_SCRIPT = "run_mlflow_automl_project.sh";

    public static final String MLFLOW_TASK_TYPE_PROJECTS = "MLflow Projects";

    public static final String MLFLOW_TASK_TYPE_MODELS = "MLflow Models";


    public static final String MLFLOW_MODELS_DEPLOY_TYPE_MLFLOW = "MLFLOW";

    public static final String MLFLOW_MODELS_DEPLOY_TYPE_DOCKER = "DOCKER";


    /**
     * mlflow command
     */

    public static final String EXPORT_MLFLOW_TRACKING_URI_ENV = "export MLFLOW_TRACKING_URI=%s";

    public static final String SET_DATA_PATH = "data_path=%s";

    public static final String SET_REPOSITORY = "repo=%s";

    public static final String MLFLOW_RUN_BASIC_ALGORITHM = "mlflow run $repo " +
            "-P algorithm=%s " +
            "-P data_path=$data_path " +
            "-P params=\"%s\" " +
            "-P search_params=\"%s\" " +
            "-P model_name=\"%s\" " +
            "--experiment-name=\"%s\" " +
            "--version=main ";

    public static final String MLFLOW_RUN_AUTOML_PROJECT = "mlflow run $repo " +
            "-P tool=%s " +
            "-P data_path=$data_path " +
            "-P params=\"%s\" " +
            "-P model_name=\"%s\" " +
            "--experiment-name=\"%s\" " +
            "--version=main ";

    public static final String MLFLOW_RUN_CUSTOM_PROJECT = "mlflow run $repo " +
            "%s " +
            "--experiment-name=\"%s\" " +
            "--version=\"%s\" ";

    public static final String MLFLOW_MODELS_SERVE = "mlflow models serve -m %s --port %s -h 0.0.0.0";

    public static final String MLFLOW_BUILD_DOCKER = "mlflow models build-docker -m %s -n %s --enable-mlserver";


    public static final String DOCKER_RREMOVE_CONTAINER = "docker rm -f %s";

    public static final String DOCKER_RUN = "docker run --name=%s -p=%s:8080 %s";

}
