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

    public static final String PRESET_AUTOML_PROJECT = "https://github.com/jieguangzhou/MLflow-AutoML";

    public static final String PRESET_BASIC_ALGORITHM_PROJECT = "https://github.com/jieguangzhou/mlflow_sklearn_gallery";

    public static final String RUN_PROJECT_BASIC_ALGORITHM_SCRIPT = "run_mlflow_basic_algorithm_project.sh";

    public static final String RUN_PROJECT_AUTOML_SCRIPT = "run_mlflow_automl_project.sh";


}