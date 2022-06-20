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


import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.dolphinscheduler.spi.utils.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    JSONUtils.class,
    PropertyUtils.class,
})
@PowerMockIgnore({"javax.*"})
@SuppressStaticInitializationFor("org.apache.dolphinscheduler.spi.utils.PropertyUtils")
public class JupyterTaskTest {

    @Test
    public void testBuildJupyterCommandWithLocalEnv() throws Exception {
        String parameters = buildJupyterCommandWithLocalEnv();
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        PowerMockito.mockStatic(PropertyUtils.class);
        when(PropertyUtils.getString(any())).thenReturn("/opt/anaconda3/etc/profile.d/conda.sh");
        JupyterTask jupyterTask = spy(new JupyterTask(taskExecutionContext));
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

    @Test
    public void testBuildJupyterCommandWithPackedEnv() throws Exception {
        String parameters = buildJupyterCommandWithPackedEnv();
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        PowerMockito.mockStatic(PropertyUtils.class);
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

}
