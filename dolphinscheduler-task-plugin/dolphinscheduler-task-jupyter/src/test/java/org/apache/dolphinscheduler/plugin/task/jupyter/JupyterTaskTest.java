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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.DateUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JupyterTaskTest {

    @Test
    public void testBuildJupyterCommandWithLocalEnv() throws Exception {
        String parameters = buildJupyterCommandWithLocalEnv();
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        JupyterPropertyReader mockedJupyterPropertyReader = Mockito.mock(JupyterPropertyReader.class);
        JupyterTask jupyterTask = spy(new JupyterTask(taskExecutionContext));
        doReturn(mockedJupyterPropertyReader).when(jupyterTask).proxyJupyterPropertyReaderCreator();
        doReturn("/opt/anaconda3/etc/profile.d/conda.sh").when(mockedJupyterPropertyReader).readProperty(any());
        jupyterTask.init();
        Assert.assertEquals(jupyterTask.buildCommand(),
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
                        "--progress-bar");
    }

    @Ignore
    @Test
    public void testBuildJupyterCommandWithPackedEnv() throws Exception {
        String parameters = buildJupyterCommandWithPackedEnv();
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.mockStatic(PropertyUtils.class);
        when(PropertyUtils.getString(any())).thenReturn("/opt/anaconda3/etc/profile.d/conda.sh");
        JupyterTask jupyterTask = spy(new JupyterTask(taskExecutionContext));
        jupyterTask.init();
        Assert.assertEquals(jupyterTask.buildCommand(),
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
                        "--progress-bar");
    }

    @Ignore
    @Test
    public void testBuildJupyterCommandWithRequirements() throws Exception {
        String parameters = buildJupyterCommandWithRequirements();
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.mockStatic(PropertyUtils.class);
        when(PropertyUtils.getString(any())).thenReturn("/opt/anaconda3/etc/profile.d/conda.sh");
        Mockito.mockStatic(DateUtils.class);
        when(DateUtils.getTimestampString()).thenReturn("123456789");
        JupyterTask jupyterTask = spy(new JupyterTask(taskExecutionContext));
        jupyterTask.init();
        Assert.assertEquals(jupyterTask.buildCommand(),
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
                        "conda deactivate && conda remove --name jupyter-tmp-env-123456789 --all -y");
    }

    private String buildJupyterCommandWithLocalEnv() {
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

    private String buildJupyterCommandWithPackedEnv() {
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

    private String buildJupyterCommandWithRequirements() {
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
