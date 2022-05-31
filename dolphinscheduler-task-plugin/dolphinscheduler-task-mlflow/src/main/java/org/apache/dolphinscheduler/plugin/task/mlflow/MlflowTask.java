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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * shell task
 */
public class MlflowTask extends AbstractTaskExecutor {

    /**
     * shell parameters
     */
    private MlflowParameters mlflowParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public MlflowTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle, taskExecutionContext, logger);
    }

    @Override
    public void init() {
        logger.info("shell task params {}", taskExecutionContext.getTaskParams());

        mlflowParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), MlflowParameters.class);

        if (!mlflowParameters.checkParameters()) {
            throw new RuntimeException("shell task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {
        try {
            // construct process
            String command = buildCommand();
            TaskResponse commandExecuteResult = shellCommandExecutor.run(command);
            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setAppIds(commandExecuteResult.getAppIds());
            setProcessId(commandExecuteResult.getProcessId());
            mlflowParameters.dealOutParam(shellCommandExecutor.getVarPool());
        } catch (Exception e) {
            logger.error("shell task error", e);
            setExitStatusCode(EXIT_CODE_FAILURE);
            throw e;
        }
    }

    @Override
    public void cancelApplication(boolean cancelApplication) throws Exception {
        // cancel process
        shellCommandExecutor.cancelApplication();
    }

    public String buildCommand(){
        String command = "";
        if (mlflowParameters.getMlflowTaskType().equals(MlflowConstants.MLFLOW_TASK_TYPE_PROJECTS)) {
            command = buildCommandForMlflowProjects();
        } else if (mlflowParameters.getMlflowTaskType().equals(MlflowConstants.MLFLOW_TASK_TYPE_MODELS)) {
            command = buildCommandForMlflowModels();
        }
        logger.info("mlflow task command: \n{}", command);
        return command;
    }

    /**
     * create command
     *
     * @return file name
     */
    private String buildCommandForMlflowProjects() {

        Map<String, Property> paramsMap = getParamsMap();
        List<String> args = new ArrayList<>();
        args.add(String.format(MlflowConstants.EXPORT_MLFLOW_TRACKING_URI_ENV, mlflowParameters.getMlflowTrackingUri()));

        String runCommand;

        if (mlflowParameters.getMlflowJobType().equals(MlflowConstants.JOB_TYPE_BASIC_ALGORITHM)) {
            args.add(String.format(MlflowConstants.SET_DATA_PATH, mlflowParameters.getDataPath()));
            args.add(String.format(MlflowConstants.SET_REPOSITORY, MlflowConstants.PRESET_BASIC_ALGORITHM_PROJECT));


            runCommand = MlflowConstants.MLFLOW_RUN_BASIC_ALGORITHM;
            runCommand = String.format(runCommand, mlflowParameters.getAlgorithm(), mlflowParameters.getParams(), mlflowParameters.getSearchParams(), mlflowParameters.getModelName(), mlflowParameters.getExperimentName());

        } else if (mlflowParameters.getMlflowJobType().equals(MlflowConstants.JOB_TYPE_AUTOML)) {
            args.add(String.format(MlflowConstants.SET_DATA_PATH, mlflowParameters.getDataPath()));
            args.add(String.format(MlflowConstants.SET_REPOSITORY, MlflowConstants.PRESET_AUTOML_PROJECT));

            runCommand = MlflowConstants.MLFLOW_RUN_AUTOML_PROJECT;
            runCommand = String.format(runCommand, mlflowParameters.getAutomlTool(), mlflowParameters.getParams(), mlflowParameters.getModelName(), mlflowParameters.getExperimentName());


        } else if (mlflowParameters.getMlflowJobType().equals(MlflowConstants.JOB_TYPE_CUSTOM_PROJECT)) {
            args.add(String.format(MlflowConstants.SET_REPOSITORY, mlflowParameters.getMlflowProjectRepository()));

            runCommand = MlflowConstants.MLFLOW_RUN_CUSTOM_PROJECT;
            runCommand = String.format(runCommand, mlflowParameters.getParams(), mlflowParameters.getExperimentName(), mlflowParameters.getMlflowProjectVersion());
        }
        else {
            runCommand = String.format("Cant not Support %s", mlflowParameters.getMlflowJobType());

        }
        args.add(runCommand);

        String command = ParameterUtils.convertParameterPlaceholders(String.join("\n", args), ParamUtils.convert(paramsMap));
        return command;
    }

    protected String buildCommandForMlflowModels() {
        /**
         * papermill [OPTIONS] NOTEBOOK_PATH [OUTPUT_PATH]
         */


        Map<String, Property> paramsMap = getParamsMap();
        List<String> args = new ArrayList<>();
        args.add(String.format(MlflowConstants.EXPORT_MLFLOW_TRACKING_URI_ENV, mlflowParameters.getMlflowTrackingUri()));
        String deployModelKey = mlflowParameters.getDeployModelKey();

        if (mlflowParameters.getDeployType().equals(MlflowConstants.MLFLOW_MODELS_DEPLOY_TYPE_MLFLOW)) {
            args.add(String.format(MlflowConstants.MLFLOW_MODELS_SERVE, deployModelKey, mlflowParameters.getDeployPort()));

        } else if (mlflowParameters.getDeployType().equals(MlflowConstants.MLFLOW_MODELS_DEPLOY_TYPE_DOCKER)) {
            String imageName = "mlflow/" + mlflowParameters.getModelKeyName(":");
            String containerName = "mlflow-" + mlflowParameters.getModelKeyName("-");

            args.add(String.format(MlflowConstants.MLFLOW_BUILD_DOCKER, deployModelKey, imageName));
            args.add(String.format(MlflowConstants.DOCKER_RREMOVE_CONTAINER, containerName));
            args.add(String.format(MlflowConstants.DOCKER_RUN, containerName, mlflowParameters.getDeployPort(), imageName));
        }

        String command = ParameterUtils.convertParameterPlaceholders(String.join("\n", args), ParamUtils.convert(paramsMap));
        return command;
    }

    private Map<String, Property> getParamsMap() {
        // replace placeholder, and combining local and global parameters
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext, getParameters());
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
            paramsMap.putAll(taskExecutionContext.getParamsMap());
        }
        return paramsMap;

    }

    @Override
    public AbstractParameters getParameters() {
        return mlflowParameters;
    }

}
