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


import org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JupyterTask extends AbstractYarnTask {

    /**
     * jupyter parameters
     */
    private JupyterParameters jupyterParameters;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    public JupyterTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
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

    /**
     * create command
     *
     * @return command
     */
    @Override
    protected String buildCommand() {
        /**
         * papermill [OPTIONS] NOTEBOOK_PATH [OUTPUT_PATH]
         */
        List<String> args = new ArrayList<>();

        args.add(JupyterConstants.CONDA_ACTIVATE);
        args.add(jupyterParameters.getCondaEnvName());
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
    private List<String> populateJupyterParameterization() {
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
    protected void setMainJarName() {
        return;
    }

    @Override
    public AbstractParameters getParameters() {
        return jupyterParameters;
    }
}
