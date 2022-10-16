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

package org.apache.dolphinler.plugin.task.mlflow;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.mlflow.MlflowConstants;
import org.apache.dolphinscheduler.plugin.task.mlflow.MlflowParameters;
import org.apache.dolphinscheduler.plugin.task.mlflow.MlflowTask;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class MlflowTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(MlflowTask.class);
    private MockedStatic<PropertyUtils> propertyUtilsMockedStatic;

    @BeforeEach
    public void init() {
        propertyUtilsMockedStatic = Mockito.mockStatic(PropertyUtils.class);
        propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(MlflowConstants.PRESET_REPOSITORY_VERSION_KEY))
                .thenReturn("main");
    }

    @AfterEach
    public void clean() {
        propertyUtilsMockedStatic.close();
    }

    public TaskExecutionContext createContext(MlflowParameters mlflowParameters) {
        String parameters = JSONUtils.toJsonString(mlflowParameters);
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        TaskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);
        return taskExecutionContext;
    }

    @Test
    public void testGetPresetRepositoryData() {
        Assertions.assertEquals("https://github.com/apache/dolphinscheduler-mlflow", MlflowTask.getPresetRepository());

        Assertions.assertEquals("main", MlflowTask.getPresetRepositoryVersion());

        String definedRepository = "https://github.com/<MY-ID>/dolphinscheduler-mlflow";
        Mockito.when(PropertyUtils.getString(MlflowConstants.PRESET_REPOSITORY_KEY))
                .thenAnswer(invocation -> definedRepository);
        Assertions.assertEquals(definedRepository, MlflowTask.getPresetRepository());

        String definedRepositoryVersion = "dev";
        Mockito.when(PropertyUtils.getString(MlflowConstants.PRESET_REPOSITORY_VERSION_KEY))
                .thenAnswer(invocation -> definedRepositoryVersion);
        Assertions.assertEquals(definedRepositoryVersion, MlflowTask.getPresetRepositoryVersion());
    }

    @Test
    public void testGetVersionString() {
        Assertions.assertEquals("--version=main",
                MlflowTask.getVersionString("main", "https://github.com/apache/dolphinscheduler-mlflow"));
        Assertions.assertEquals("--version=master",
                MlflowTask.getVersionString("master", "https://github.com/apache/dolphinscheduler-mlflow"));
        Assertions.assertEquals("--version=main",
                MlflowTask.getVersionString("main", "git@github.com:apache/dolphinscheduler-mlflow.git"));
        Assertions.assertEquals("--version=master",
                MlflowTask.getVersionString("master", "git@github.com:apache/dolphinscheduler-mlflow.git"));
        Assertions.assertEquals("", MlflowTask.getVersionString("main", "/tmp/dolphinscheduler-mlflow"));
        Assertions.assertEquals("", MlflowTask.getVersionString("master", "/tmp/dolphinscheduler-mlflow"));
    }

    @Test
    public void testInitBasicAlgorithmTask() {
        MlflowTask mlflowTask = initTask(createBasicAlgorithmParameters());
        Assertions.assertEquals(mlflowTask.buildCommand(),
                "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                        + "data_path=/data/iris.csv\n"
                        + "repo=https://github.com/apache/dolphinscheduler-mlflow#Project-BasicAlgorithm\n"
                        + "mlflow run $repo "
                        + "-P algorithm=xgboost "
                        + "-P data_path=$data_path "
                        + "-P params=\"n_estimators=100\" "
                        + "-P search_params=\"\" "
                        + "-P model_name=\"BasicAlgorithm\" "
                        + "--experiment-name=\"BasicAlgorithm\" "
                        + "--version=main");
    }

    @Test
    public void testInitAutoMLTask() {
        MlflowTask mlflowTask = initTask(createAutoMLParameters());
        Assertions.assertEquals(mlflowTask.buildCommand(),
                "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                        + "data_path=/data/iris.csv\n"
                        + "repo=https://github.com/apache/dolphinscheduler-mlflow#Project-AutoML\n"
                        + "mlflow run $repo "
                        + "-P tool=autosklearn "
                        + "-P data_path=$data_path "
                        + "-P params=\"time_left_for_this_task=30\" "
                        + "-P model_name=\"AutoML\" "
                        + "--experiment-name=\"AutoML\" "
                        + "--version=main");
    }

    @Test
    public void testInitCustomProjectTask() {
        MlflowTask mlflowTask = initTask(createCustomProjectParameters());

        // Version will be set if parameter.mlflowProjectVersion is empty
        Assertions.assertEquals(mlflowTask.buildCommand(),
                "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                        + "repo=https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native\n"
                        + "mlflow run $repo "
                        + "-P learning_rate=0.2 "
                        + "-P colsample_bytree=0.8 "
                        + "-P subsample=0.9 "
                        + "--experiment-name=\"custom_project\"");

        // Version will be set if repository is remote path
        mlflowTask.getParameters().setMlflowProjectVersion("dev");
        Assertions.assertEquals(mlflowTask.buildCommand(),
                "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                        + "repo=https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native\n"
                        + "mlflow run $repo "
                        + "-P learning_rate=0.2 "
                        + "-P colsample_bytree=0.8 "
                        + "-P subsample=0.9 "
                        + "--experiment-name=\"custom_project\" "
                        + "--version=dev");

        // Version will not be set if repository is local path
        mlflowTask.getParameters().setMlflowProjectRepository("/tmp/dolphinscheduler-mlflow");
        Assertions.assertEquals(mlflowTask.buildCommand(),
                "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                        + "repo=/tmp/dolphinscheduler-mlflow\n"
                        + "mlflow run $repo "
                        + "-P learning_rate=0.2 "
                        + "-P colsample_bytree=0.8 "
                        + "-P subsample=0.9 "
                        + "--experiment-name=\"custom_project\"");

    }

    @Test
    public void testModelsDeployMlflow() {
        MlflowTask mlflowTask = initTask(createModelDeplyMlflowParameters());
        Assertions.assertEquals(mlflowTask.buildCommand(),
                "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                        + "mlflow models serve -m models:/model/1 --port 7000 -h 0.0.0.0");
    }

    @Test
    public void testModelsDeployDocker() {
        MlflowTask mlflowTask = initTask(createModelDeplyDockerParameters());
        Assertions.assertEquals(mlflowTask.buildCommand(),
                "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                        + "mlflow models build-docker -m models:/model/1 -n mlflow/model:1 --enable-mlserver\n"
                        + "docker rm -f ds-mlflow-model-1\n"
                        + "docker run -d --name=ds-mlflow-model-1 -p=7000:8080 "
                        + "--health-cmd \"curl --fail http://127.0.0.1:8080/ping || exit 1\" --health-interval 5s --health-retries 20 "
                        + "mlflow/model:1");
    }

    private MlflowTask initTask(MlflowParameters mlflowParameters) {
        TaskExecutionContext taskExecutionContext = createContext(mlflowParameters);
        MlflowTask mlflowTask = new MlflowTask(taskExecutionContext);
        mlflowTask.init();
        mlflowTask.getParameters().setVarPool(taskExecutionContext.getVarPool());
        return mlflowTask;
    }

    private MlflowParameters createBasicAlgorithmParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_PROJECTS);
        mlflowParameters.setMlflowJobType(MlflowConstants.JOB_TYPE_BASIC_ALGORITHM);
        mlflowParameters.setAlgorithm("xgboost");
        mlflowParameters.setDataPath("/data/iris.csv");
        mlflowParameters.setParams("n_estimators=100");
        mlflowParameters.setExperimentName("BasicAlgorithm");
        mlflowParameters.setModelName("BasicAlgorithm");
        mlflowParameters.setMlflowTrackingUri("http://127.0.0.1:5000");
        return mlflowParameters;
    }

    private MlflowParameters createAutoMLParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_PROJECTS);
        mlflowParameters.setMlflowJobType(MlflowConstants.JOB_TYPE_AUTOML);
        mlflowParameters.setAutomlTool("autosklearn");
        mlflowParameters.setParams("time_left_for_this_task=30");
        mlflowParameters.setDataPath("/data/iris.csv");
        mlflowParameters.setExperimentName("AutoML");
        mlflowParameters.setModelName("AutoML");
        mlflowParameters.setMlflowTrackingUri("http://127.0.0.1:5000");
        return mlflowParameters;
    }

    private MlflowParameters createCustomProjectParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_PROJECTS);
        mlflowParameters.setMlflowJobType(MlflowConstants.JOB_TYPE_CUSTOM_PROJECT);
        mlflowParameters.setMlflowTrackingUri("http://127.0.0.1:5000");
        mlflowParameters.setExperimentName("custom_project");
        mlflowParameters.setParams("-P learning_rate=0.2 -P colsample_bytree=0.8 -P subsample=0.9");
        mlflowParameters.setMlflowProjectRepository("https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native");

        return mlflowParameters;
    }

    private MlflowParameters createModelDeplyMlflowParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_MODELS);
        mlflowParameters.setDeployType(MlflowConstants.MLFLOW_MODELS_DEPLOY_TYPE_MLFLOW);
        mlflowParameters.setMlflowTrackingUri("http://127.0.0.1:5000");
        mlflowParameters.setDeployModelKey("models:/model/1");
        mlflowParameters.setDeployPort("7000");
        return mlflowParameters;
    }

    private MlflowParameters createModelDeplyDockerParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_MODELS);
        mlflowParameters.setDeployType(MlflowConstants.MLFLOW_MODELS_DEPLOY_TYPE_DOCKER);
        mlflowParameters.setMlflowTrackingUri("http://127.0.0.1:5000");
        mlflowParameters.setDeployModelKey("models:/model/1");
        mlflowParameters.setDeployPort("7000");
        return mlflowParameters;
    }
}
