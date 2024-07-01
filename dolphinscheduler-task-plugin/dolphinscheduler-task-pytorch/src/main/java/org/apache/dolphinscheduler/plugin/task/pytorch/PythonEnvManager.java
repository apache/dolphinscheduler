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

package org.apache.dolphinscheduler.plugin.task.pytorch;

import lombok.Data;

@Data
public class PythonEnvManager {

    public static final String ENV_TOOL_VENV = "virtualenv";

    public static final String ENV_TOOL_CONDA = "conda";

    private static final String PATTERN_ENVIRONMENT_PYTHON = "python[\\d\\.]*$";

    private static final String PATTERN_ENVIRONMENT_REQUIREMENT = "\\.txt$";

    private static final String CREATE_ENV_NAME = "./venv";

    private static final String CONDA_SOURCE = "source activate %s";

    private static final String CONDA_BUILD = "conda create -y python=%s -p %s";

    private static final String VIRTUALENV_SOURCE = "source %s/bin/activate";

    private static final String VIRTUALENV_BUILD = "virtualenv -p ${PYTHON_LAUNCHER} %s";

    private static final String INSTALL_COMMAND = "%s -m pip install -r %s";

    private String pythonEnvTool = ENV_TOOL_VENV;

    private String condaPythonVersion = "3.9";

    public String getBuildEnvCommand(String requirementPath) {
        String buildCommand = "";
        String sourceCommand = getSourceEnvCommand(CREATE_ENV_NAME);
        if (pythonEnvTool.equals(ENV_TOOL_VENV)) {
            buildCommand = String.format(VIRTUALENV_BUILD, CREATE_ENV_NAME);
        } else if (pythonEnvTool.equals(ENV_TOOL_CONDA)) {
            buildCommand = String.format(CONDA_BUILD, condaPythonVersion, CREATE_ENV_NAME);
        }
        String installCommand = String.format(INSTALL_COMMAND, getPythonCommand(), requirementPath);

        return buildCommand + " && " + sourceCommand + " && " + installCommand;
    }

    private String getSourceEnvCommand(String envName) {
        String command = "";
        if (pythonEnvTool.equals(ENV_TOOL_VENV)) {
            command = String.format(VIRTUALENV_SOURCE, envName);
        } else if (pythonEnvTool.equals(ENV_TOOL_CONDA)) {
            command = String.format(CONDA_SOURCE, envName);
        }

        return command;
    }

    public String getPythonCommand() {
        return String.format("%s/bin/python", CREATE_ENV_NAME);
    }

}
