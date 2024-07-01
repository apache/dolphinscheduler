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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JupyterTaskTest {

    private static final String EXPECTED_JUPYTER_TASK_COMMAND_USE_LOCAL_CONDA_ENV =
            "source /opt/anaconda3/etc/profile.d/conda.sh && " +
                    "conda activate jupyter-lab && " +
                    "papermill " +
                    "/test/input_note.ipynb " +
                    "/test/output_note.ipynb " +
                    "--parameters city Shanghai " +
                    "--parameters factor 0.01 " +
                    "--kernel python3 " +
                    "--engine default_engine " +
                    "--execution-timeout 10 " +
                    "--start-timeout 3 " +
                    "--version " +
                    "--inject-paths " +
                    "--progress-bar";

    private static final String EXPECTED_JUPYTER_TASK_COMMAND_USE_PACKED_CONDA_ENV =
            "source /opt/anaconda3/etc/profile.d/conda.sh && " +
                    "mkdir jupyter_env && " +
                    "tar -xzf jupyter.tar.gz -C jupyter_env && " +
                    "source jupyter_env/bin/activate && " +
                    "papermill " +
                    "/test/input_note.ipynb " +
                    "/test/output_note.ipynb " +
                    "--parameters city Shanghai " +
                    "--parameters factor 0.01 " +
                    "--kernel python3 " +
                    "--engine default_engine " +
                    "--execution-timeout 10 " +
                    "--start-timeout 3 " +
                    "--version " +
                    "--inject-paths " +
                    "--progress-bar";

    private static final String EXPECTED_JUPYTER_TASK_COMMAND_USE_PIP_REQUIREMENTS =
            "set +e \n " +
                    "source /opt/anaconda3/etc/profile.d/conda.sh && " +
                    "conda create -n jupyter-tmp-env-123456789 -y && " +
                    "conda activate jupyter-tmp-env-123456789 && " +
                    "pip install -r requirements.txt && " +
                    "papermill " +
                    "/test/input_note.ipynb " +
                    "/test/output_note.ipynb " +
                    "--parameters city Shanghai " +
                    "--parameters factor 0.01 " +
                    "--kernel python3 " +
                    "--engine default_engine " +
                    "--execution-timeout 10 " +
                    "--start-timeout 3 " +
                    "--version " +
                    "--inject-paths " +
                    "--progress-bar \n " +
                    "conda deactivate && conda remove --name jupyter-tmp-env-123456789 --all -y";

    @Test
    public void jupyterTaskUseLocalCondaEnv() throws Exception {
        String jupyterTaskParameters = buildJupyterTaskUseLocalCondaEnvCommand();
        JupyterTask jupyterTask = prepareJupyterTaskForTest(jupyterTaskParameters);
        jupyterTask.init();
        Assertions.assertEquals(jupyterTask.buildCommand(), EXPECTED_JUPYTER_TASK_COMMAND_USE_LOCAL_CONDA_ENV);
    }

    @Test
    public void jupyterTaskUsePackedCondaEnv() throws Exception {
        String jupyterTaskParameters = buildJupyterTaskUsePackedCondaEnvCommand();
        JupyterTask jupyterTask = prepareJupyterTaskForTest(jupyterTaskParameters);
        jupyterTask.init();
        Assertions.assertEquals(jupyterTask.buildCommand(), EXPECTED_JUPYTER_TASK_COMMAND_USE_PACKED_CONDA_ENV);
    }

    @Test
    public void jupyterTaskUsePipRequirements() throws Exception {
        String jupyterTaskParameters = buildJupyterTaskUsePipRequirementsCommand();
        JupyterTask jupyterTask = prepareJupyterTaskForTest(jupyterTaskParameters);

        try (MockedStatic<DateUtils> mockedStaticDateUtils = Mockito.mockStatic(DateUtils.class)) {
            mockedStaticDateUtils.when(DateUtils::getTimestampString).thenReturn("123456789");
            jupyterTask.init();
            Assertions.assertEquals(jupyterTask.buildCommand(), EXPECTED_JUPYTER_TASK_COMMAND_USE_PIP_REQUIREMENTS);
        }
    }

    private JupyterTask prepareJupyterTaskForTest(final String jupyterTaskParameters) {
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(jupyterTaskParameters);
        JupyterTask jupyterTask = spy(new JupyterTask(taskExecutionContext));
        doReturn("/opt/anaconda3/etc/profile.d/conda.sh").when(jupyterTask).readCondaPath();
        return jupyterTask;
    }

    private String buildJupyterTaskUseLocalCondaEnvCommand() {
        JupyterParameters jupyterParameters = new JupyterParameters();
        jupyterParameters.setCondaEnvName("jupyter-lab");
        jupyterParameters.setInputNotePath("/test/input_note.ipynb");
        jupyterParameters.setOutputNotePath("/test/output_note.ipynb");
        jupyterParameters.setParameters("{\"city\": \"Shanghai\", \"factor\": \"0.01\"}");
        jupyterParameters.setKernel("python3");
        jupyterParameters.setEngine("default_engine");
        jupyterParameters.setExecutionTimeout("10");
        jupyterParameters.setStartTimeout("3");
        jupyterParameters.setOthers("--version");
        return JSONUtils.toJsonString(jupyterParameters);
    }

    private String buildJupyterTaskUsePackedCondaEnvCommand() {
        JupyterParameters jupyterParameters = new JupyterParameters();
        jupyterParameters.setCondaEnvName("jupyter.tar.gz");
        jupyterParameters.setInputNotePath("/test/input_note.ipynb");
        jupyterParameters.setOutputNotePath("/test/output_note.ipynb");
        jupyterParameters.setParameters("{\"city\": \"Shanghai\", \"factor\": \"0.01\"}");
        jupyterParameters.setKernel("python3");
        jupyterParameters.setEngine("default_engine");
        jupyterParameters.setExecutionTimeout("10");
        jupyterParameters.setStartTimeout("3");
        jupyterParameters.setOthers("--version");
        return JSONUtils.toJsonString(jupyterParameters);
    }

    private String buildJupyterTaskUsePipRequirementsCommand() {
        JupyterParameters jupyterParameters = new JupyterParameters();
        jupyterParameters.setCondaEnvName("requirements.txt");
        jupyterParameters.setInputNotePath("/test/input_note.ipynb");
        jupyterParameters.setOutputNotePath("/test/output_note.ipynb");
        jupyterParameters.setParameters("{\"city\": \"Shanghai\", \"factor\": \"0.01\"}");
        jupyterParameters.setKernel("python3");
        jupyterParameters.setEngine("default_engine");
        jupyterParameters.setExecutionTimeout("10");
        jupyterParameters.setStartTimeout("3");
        jupyterParameters.setOthers("--version");
        return JSONUtils.toJsonString(jupyterParameters);
    }

}
