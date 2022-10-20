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

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.spi.utils.DateUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JupyterTask extends AbstractRemoteTask {

    private JupyterParameters jupyterParameters;

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
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
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

    // todo split handle to submit and track
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            TaskResponse response = shellCommandExecutor.run(buildCommand());
            setExitStatusCode(response.getExitStatusCode());
            setAppIds(String.join(TaskConstants.COMMA, getApplicationIds()));
            setProcessId(response.getProcessId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("The current Jupyter task has been interrupted", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("The current Jupyter task has been interrupted", e);
        } catch (Exception e) {
            logger.error("jupyter task execution failure", e);
            exitStatusCode = -1;
            throw new TaskException("Execute jupyter task failed", e);
        }
    }

    @Override
    public void submitApplication() throws TaskException {

    }

    @Override
    public void trackApplicationStatus() throws TaskException {

    }

    /**
     * command will be like: papermill [OPTIONS] NOTEBOOK_PATH [OUTPUT_PATH]
     */
    protected String buildCommand() throws IOException {

        List<String> args = new ArrayList<>();
        final String condaPath = readCondaPath();
        final String timestamp = DateUtils.getTimestampString();
        String condaEnvName = jupyterParameters.getCondaEnvName();
        if (condaEnvName.endsWith(JupyterConstants.TXT_SUFFIX)) {
            args.add(JupyterConstants.EXECUTION_FLAG);
            args.add(JupyterConstants.NEW_LINE_SYMBOL);
        }

        args.add(JupyterConstants.CONDA_INIT);
        args.add(condaPath);
        args.add(JupyterConstants.JOINTER);
        if (condaEnvName.endsWith(JupyterConstants.TAR_SUFFIX)) {
            args.add(String.format(JupyterConstants.CREATE_ENV_FROM_TAR, condaEnvName));
        } else if (condaEnvName.endsWith(JupyterConstants.TXT_SUFFIX)) {
            args.add(String.format(JupyterConstants.CREATE_ENV_FROM_TXT, timestamp, timestamp, condaEnvName));
        } else {
            args.add(JupyterConstants.CONDA_ACTIVATE);
            args.add(jupyterParameters.getCondaEnvName());
        }

        args.add(JupyterConstants.JOINTER);
        args.add(JupyterConstants.PAPERMILL);
        args.add(jupyterParameters.getInputNotePath());
        args.add(jupyterParameters.getOutputNotePath());
        args.addAll(populateJupyterParameterization());
        args.addAll(populateJupyterOptions());

        // remove tmp conda env, if created from requirements.txt
        if (condaEnvName.endsWith(JupyterConstants.TXT_SUFFIX)) {
            args.add(JupyterConstants.NEW_LINE_SYMBOL);
            args.add(String.format(JupyterConstants.REMOVE_ENV, timestamp));
        }

        // replace placeholder, and combining local and global parameters
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        String command = ParameterUtils
                .convertParameterPlaceholders(String.join(" ", args), ParamUtils.convert(paramsMap));

        logger.info("jupyter task command: {}", command);

        return command;
    }

    protected String readCondaPath() {
        return PropertyUtils.getString(TaskConstants.CONDA_PATH);
    }

    protected List<String> populateJupyterParameterization() throws IOException {
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

    protected List<String> populateJupyterOptions() {
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
    public void cancelApplication() throws TaskException {
        // cancel process
        try {
            shellCommandExecutor.cancelApplication();
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return jupyterParameters;
    }

}
