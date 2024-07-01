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

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import lombok.Data;

@Data
public class PytorchParameters extends AbstractParameters {

    private Boolean isCreateEnvironment = false;
    private String pythonPath = ".";
    private String script;
    private String scriptParams;
    private String pythonLauncher = "${PYTHON_LAUNCHER}";
    private String pythonEnvTool = PythonEnvManager.ENV_TOOL_VENV;
    private String requirements = "requirements.txt";
    private String condaPythonVersion = "3.9";
    /**
     * resource list
     */
    private List<ResourceInfo> resourceList;

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return resourceList;
    }

    @Override
    public boolean checkParameters() {
        return true;
    }

    public String getRequirementPath() {
        return getPossiblePath(requirements);
    }

    public String getPythonLauncher() {
        String command;
        if (pythonLauncher.isEmpty()) {
            command = "${PYTHON_LAUNCHER}";
        } else {
            command = pythonLauncher;
        }
        return command;
    }

    public String getScriptPath() {
        return getPossiblePath(script);
    }

    private String getPossiblePath(String filePath) {
        String possiblePath = filePath;
        File sourceFile = new File(possiblePath);
        String newPath = Paths.get(pythonPath, possiblePath).toString();
        File newFile = new File(newPath);
        if (newFile.exists() && !sourceFile.exists()) {
            possiblePath = newPath;
        } else if (resourceList != null) {
            String newPathResource = StringUtils.removeStart(newPath, "./");
            for (ResourceInfo resourceInfo : resourceList) {
                if (resourceInfo.getResourceName().equals("/" + newPathResource)) {
                    possiblePath = newPath;
                    break;
                }
            }
        }
        return possiblePath;

    }
}
