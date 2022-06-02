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

package org.apache.dolphinscheduler.plugin.task.jupyter;


import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JupyterTask extends AbstractTaskExecutor {

    /**
     * jupyter parameters
     */
    private JupyterParameters jupyterParameters;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    private ShellCommandExecutor shellCommandExecutor;

    public JupyterTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                logger);
    }

    @Override
    public void init() {
        logger.info("jupyter task params {}", taskExecutionContext.getTaskParams());

        jupyterParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), JupyterParameters.class);

        if (null == jupyterParameters) {
            logger.error("jupyter params is null");
            return;
        }

        if (!jupyterParameters.checkParameters()) {
            throw new RuntimeException("jupyter task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {
        try {
            // SHELL task exit code
            TaskResponse response = shellCommandExecutor.run(buildCommand());
            setExitStatusCode(response.getExitStatusCode());
            setAppIds(response.getAppIds());
            setProcessId(response.getProcessId());
        } catch (Exception e) {
            logger.error("jupyter task execution failure", e);
            exitStatusCode = -1;
            throw e;
        }
    }

    /**
     * create command
     *
     * @return command
     */
    protected String buildCommand() throws IOException {
        /**
         * papermill [OPTIONS] NOTEBOOK_PATH [OUTPUT_PATH]
         */
        List<String> args = new ArrayList<>();
        final String condaPath = PropertyUtils.getString(TaskConstants.CONDA_PATH);
        args.add(JupyterConstants.CONDA_INIT);
        args.add(condaPath);
        args.add(JupyterConstants.JOINTER);
        String condaEnvName = jupyterParameters.getCondaEnvName();
        if (condaEnvName.endsWith(JupyterConstants.TAR_SUFFIX)) {
            args.add(String.format(JupyterConstants.CREATE_ENV_FROM_TAR, condaEnvName));
        } else {
            args.add(JupyterConstants.CONDA_ACTIVATE);
            args.add(jupyterParameters.getCondaEnvName());
        }

        args.add(JupyterConstants.JOINTER);
        args.add(JupyterConstants.PAPERMILL);
        args.add(jupyterParameters.getInputNotePath());
        args.add(jupyterParameters.getOutputNotePath());

        // populate jupyter parameterization
        args.addAll(populateJupyterParameterization());

        // populate jupyter options
        args.addAll(populateJupyterOptions());

        // replace placeholder, and combining local and global parameters
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext, getParameters());
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
            paramsMap.putAll(taskExecutionContext.getParamsMap());
        }

        String command = ParameterUtils.convertParameterPlaceholders(String.join(" ", args), ParamUtils.convert(paramsMap));

        logger.info("jupyter task command: {}", command);

        return command;
    }


    /**
     * build jupyter parameterization
     *
     * @return argument list
     */
    private List<String> populateJupyterParameterization() throws IOException {
        List<String> args = new ArrayList<>();
        String parameters = jupyterParameters.getParameters();
        if (StringUtils.isNotEmpty(parameters)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                // convert JSON string to Map
                Map<String, String> jupyterParamsMap = mapper.readValue(parameters, Map.class);
                for (String key : jupyterParamsMap.keySet()) {
                    args.add(JupyterConstants.PARAMETERS);
                    args.add(key);
                    args.add(jupyterParamsMap.get(key));
                }

            } catch (IOException e) {
                logger.error("fail to parse jupyter parameterization", e);
                throw e;
            }
        }
        return args;
    }

    /**
     * build jupyter options
     *
     * @return argument list
     */
    private List<String> populateJupyterOptions() {
        List<String> args = new ArrayList<>();
        String kernel = jupyterParameters.getKernel();
        if (StringUtils.isNotEmpty(kernel)) {
            args.add(JupyterConstants.KERNEL);
            args.add(kernel);
        }

        String engine = jupyterParameters.getEngine();
        if (StringUtils.isNotEmpty(engine)) {
            args.add(JupyterConstants.ENGINE);
            args.add(engine);
        }

        String executionTimeout = jupyterParameters.getExecutionTimeout();
        if (StringUtils.isNotEmpty(executionTimeout)) {
            args.add(JupyterConstants.EXECUTION_TIMEOUT);
            args.add(executionTimeout);
        }

        String startTimeout = jupyterParameters.getStartTimeout();
        if (StringUtils.isNotEmpty(startTimeout)) {
            args.add(JupyterConstants.START_TIMEOUT);
            args.add(startTimeout);
        }

        String others = jupyterParameters.getOthers();
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        args.add(JupyterConstants.INJECT_PATHS);
        args.add(JupyterConstants.PROGRESS_BAR);
        return args;
    }

    @Override
    public void cancelApplication(boolean cancelApplication) throws Exception {
        // cancel process
        shellCommandExecutor.cancelApplication();
    }

    @Override
    public AbstractParameters getParameters() {
        return jupyterParameters;
    }

}
