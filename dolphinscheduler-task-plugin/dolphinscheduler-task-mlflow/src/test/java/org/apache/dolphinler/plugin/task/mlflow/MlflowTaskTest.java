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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.mlflow.MlflowConstants;
import org.apache.dolphinscheduler.plugin.task.mlflow.MlflowParameters;
import org.apache.dolphinscheduler.plugin.task.mlflow.MlflowTask;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    JSONUtils.class,
    PropertyUtils.class,
})
@PowerMockIgnore({"javax.*"})
@SuppressStaticInitializationFor("org.apache.dolphinscheduler.spi.utils.PropertyUtils")
public class MlflowTaskTest {
    private static final Logger logger = LoggerFactory.getLogger(MlflowTask.class);

    @Before
    public void before() throws Exception {
        PowerMockito.mockStatic(PropertyUtils.class);
    }

    public TaskExecutionContext createContext(MlflowParameters mlflowParameters) {
        String parameters = JSONUtils.toJsonString(mlflowParameters);
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getTaskLogName()).thenReturn("MLflowTest");
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp/dolphinscheduler_test");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getLogPath()).thenReturn("/tmp/dolphinscheduler_test/log");
        Mockito.when(taskExecutionContext.getEnvironmentConfig()).thenReturn("export PATH=$HOME/anaconda3/bin:$PATH");

        String userName = System.getenv().get("USER");
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn(userName);

        TaskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);
        return taskExecutionContext;
    }

    @Test
    public void testInitBasicAlgorithmTask() {
        MlflowTask mlflowTask = initTask(createBasicAlgorithmParameters());
        Assert.assertEquals(mlflowTask.buildCommand(),
            "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                + "data_path=/data/iris.csv\n"
                + "repo=dolphinscheduler-mlflow#Project-BasicAlgorithm\n"
                + "git clone https://github.com/apache/dolphinscheduler-mlflow dolphinscheduler-mlflow\n"
                + "mlflow run $repo "
                + "-P algorithm=xgboost "
                + "-P data_path=$data_path "
                + "-P params=\"n_estimators=100\" "
                + "-P search_params=\"\" "
                + "-P model_name=\"BasicAlgorithm\" "
                + "--experiment-name=\"BasicAlgorithm\"");
    }

    @Test
    public void testInitAutoMLTask() {
        MlflowTask mlflowTask = initTask(createAutoMLParameters());
        Assert.assertEquals(mlflowTask.buildCommand(),
            "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                + "data_path=/data/iris.csv\n"
                + "repo=dolphinscheduler-mlflow#Project-AutoML\n"
                + "git clone https://github.com/apache/dolphinscheduler-mlflow dolphinscheduler-mlflow\n"
                + "mlflow run $repo "
                + "-P tool=autosklearn "
                + "-P data_path=$data_path "
                + "-P params=\"time_left_for_this_task=30\" "
                + "-P model_name=\"AutoML\" "
                + "--experiment-name=\"AutoML\"");
    }

    @Test
    public void testInitCustomProjectTask() {
        MlflowTask mlflowTask = initTask(createCustomProjectParameters());
        Assert.assertEquals(mlflowTask.buildCommand(),
            "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                + "repo=https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native\n"
                + "mlflow run $repo "
                + "-P learning_rate=0.2 "
                + "-P colsample_bytree=0.8 "
                + "-P subsample=0.9 "
                + "--experiment-name=\"custom_project\" "
                + "--version=\"master\" ");
    }

    @Test
    public void testModelsDeployMlflow() {
        MlflowTask mlflowTask = initTask(createModelDeplyMlflowParameters());
        Assert.assertEquals(mlflowTask.buildCommand(),
            "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                + "mlflow models serve -m models:/model/1 --port 7000 -h 0.0.0.0");
    }

    @Test
    public void testModelsDeployDocker() {
        MlflowTask mlflowTask = initTask(createModelDeplyDockerParameters());
        Assert.assertEquals(mlflowTask.buildCommand(),
            "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                + "mlflow models build-docker -m models:/model/1 -n mlflow/model:1 --enable-mlserver\n"
                + "docker rm -f ds-mlflow-model-1\n"
                + "docker run -d --name=ds-mlflow-model-1 -p=7000:8080 "
                + "--health-cmd \"curl --fail http://127.0.0.1:8080/ping || exit 1\" --health-interval 5s --health-retries 20 "
                + "mlflow/model:1");
    }

    @Test
    public void testModelsDeployDockerCompose() throws Exception {
        MlflowTask mlflowTask = initTask(createModelDeplyDockerComposeParameters());
        Assert.assertEquals(mlflowTask.buildCommand(),
            "export MLFLOW_TRACKING_URI=http://127.0.0.1:5000\n"
                + "cp "
                + mlflowTask.getTemplatePath(MlflowConstants.TEMPLATE_DOCKER_COMPOSE)
                + " /tmp/dolphinscheduler_test\n"
                + "mlflow models build-docker -m models:/model/1 -n mlflow/model:1 --enable-mlserver\n"
                + "docker rm -f ds-mlflow-model-1\n"
                + "export DS_TASK_MLFLOW_IMAGE_NAME=mlflow/model:1\n"
                + "export DS_TASK_MLFLOW_CONTAINER_NAME=ds-mlflow-model-1\n"
                + "export DS_TASK_MLFLOW_DEPLOY_PORT=7000\n"
                + "export DS_TASK_MLFLOW_CPU_LIMIT=0.5\n"
                + "export DS_TASK_MLFLOW_MEMORY_LIMIT=200m\n"
                + "docker-compose up -d");
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
        mlflowParameters.setDataPaths("/data/iris.csv");
        mlflowParameters.setParams("n_estimators=100");
        mlflowParameters.setExperimentNames("BasicAlgorithm");
        mlflowParameters.setModelNames("BasicAlgorithm");
        mlflowParameters.setMlflowTrackingUris("http://127.0.0.1:5000");
        return mlflowParameters;
    }

    private MlflowParameters createAutoMLParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_PROJECTS);
        mlflowParameters.setMlflowJobType(MlflowConstants.JOB_TYPE_AUTOML);
        mlflowParameters.setAutomlTool("autosklearn");
        mlflowParameters.setParams("time_left_for_this_task=30");
        mlflowParameters.setDataPaths("/data/iris.csv");
        mlflowParameters.setExperimentNames("AutoML");
        mlflowParameters.setModelNames("AutoML");
        mlflowParameters.setMlflowTrackingUris("http://127.0.0.1:5000");
        return mlflowParameters;
    }

    private MlflowParameters createCustomProjectParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_PROJECTS);
        mlflowParameters.setMlflowJobType(MlflowConstants.JOB_TYPE_CUSTOM_PROJECT);
        mlflowParameters.setMlflowTrackingUris("http://127.0.0.1:5000");
        mlflowParameters.setExperimentNames("custom_project");
        mlflowParameters.setParams("-P learning_rate=0.2 -P colsample_bytree=0.8 -P subsample=0.9");
        mlflowParameters.setMlflowProjectRepository("https://github.com/mlflow/mlflow#examples/xgboost/xgboost_native");

        return mlflowParameters;
    }

    private MlflowParameters createModelDeplyMlflowParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_MODELS);
        mlflowParameters.setDeployType(MlflowConstants.MLFLOW_MODELS_DEPLOY_TYPE_MLFLOW);
        mlflowParameters.setMlflowTrackingUris("http://127.0.0.1:5000");
        mlflowParameters.setDeployModelKey("models:/model/1");
        mlflowParameters.setDeployPort("7000");
        return mlflowParameters;
    }

    private MlflowParameters createModelDeplyDockerParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_MODELS);
        mlflowParameters.setDeployType(MlflowConstants.MLFLOW_MODELS_DEPLOY_TYPE_DOCKER);
        mlflowParameters.setMlflowTrackingUris("http://127.0.0.1:5000");
        mlflowParameters.setDeployModelKey("models:/model/1");
        mlflowParameters.setDeployPort("7000");
        return mlflowParameters;
    }

    private MlflowParameters createModelDeplyDockerComposeParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_MODELS);
        mlflowParameters.setDeployType(MlflowConstants.MLFLOW_MODELS_DEPLOY_TYPE_DOCKER_COMPOSE);
        mlflowParameters.setMlflowTrackingUris("http://127.0.0.1:5000");
        mlflowParameters.setDeployModelKey("models:/model/1");
        mlflowParameters.setDeployPort("7000");
        mlflowParameters.setCpuLimit("0.5");
        mlflowParameters.setMemoryLimit("200m");
        return mlflowParameters;
    }
}
