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

import java.util.Date;
import java.util.UUID;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.mlflow.MlflowConstants;
import org.apache.dolphinscheduler.plugin.task.mlflow.MlflowParameters;
import org.apache.dolphinscheduler.plugin.task.mlflow.MlflowTask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;


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
    public void testInitBasicAlgorithmTask() throws Exception {
        MlflowTask mlflowTask = initTask(createBasicAlgorithmParameters());
        String command = mlflowTask.buildCommand();

    }

    @Test
    public void testInitAutoMLTask() throws Exception {
        MlflowTask mlflowTask = initTask(createAutoMLParameters());
        String command = mlflowTask.buildCommand();
    }

    @Test
    public void testModelsDeployMlflow() throws Exception {
        MlflowTask mlflowTask = initTask(createModelDeplyMlflowParameters());
        String command = mlflowTask.buildCommand();
    }

    @Test
    public void testModelsDeployDocker() throws Exception {
        MlflowTask mlflowTask = initTask(createModelDeplyDockerParameters());
        String command = mlflowTask.buildCommand();
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
        mlflowParameters.setDataPaths("xxxxxxxxxx");
        mlflowParameters.setExperimentNames("asbbb");
        mlflowParameters.setMlflowTrackingUris("http://127.0.0.1:5000");
        return mlflowParameters;
    }

    private MlflowParameters createAutoMLParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_PROJECTS);
        mlflowParameters.setMlflowJobType(MlflowConstants.JOB_TYPE_AUTOML);
        mlflowParameters.setAutomlTool("autosklearn");
        mlflowParameters.setParams("time_left_for_this_task=30");
        mlflowParameters.setDataPaths("xxxxxxxxxxx");
        mlflowParameters.setExperimentNames("asbbb");
        mlflowParameters.setModelNames("asbbb");
        mlflowParameters.setMlflowTrackingUris("http://127.0.0.1:5000");
        return mlflowParameters;
    }

    private MlflowParameters createModelDeplyMlflowParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_MODELS);
        mlflowParameters.setDeployType(MlflowConstants.MLFLOW_MODELS_DEPLOY_TYPE_MLFLOW);
        mlflowParameters.setMlflowTrackingUris("http://127.0.0.1:5000");
        mlflowParameters.setDeployModelKey("runs:/a272ec279fc34a8995121ae04281585f/model");
        mlflowParameters.setDeployPort("7000");
        return mlflowParameters;
    }

    private MlflowParameters createModelDeplyDockerParameters() {
        MlflowParameters mlflowParameters = new MlflowParameters();
        mlflowParameters.setMlflowTaskType(MlflowConstants.MLFLOW_TASK_TYPE_MODELS);
        mlflowParameters.setDeployType(MlflowConstants.MLFLOW_MODELS_DEPLOY_TYPE_DOCKER);
        mlflowParameters.setMlflowTrackingUris("http://127.0.0.1:5000");
        mlflowParameters.setDeployModelKey("runs:/a272ec279fc34a8995121ae04281585f/model");
        mlflowParameters.setDeployPort("7000");
        return mlflowParameters;
    }
}
