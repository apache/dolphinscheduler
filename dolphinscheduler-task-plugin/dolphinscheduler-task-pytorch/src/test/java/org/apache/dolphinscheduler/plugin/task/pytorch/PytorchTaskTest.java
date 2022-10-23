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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RWXR_XR_X;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JSONUtils.class, PropertyUtils.class,})
@PowerMockIgnore({"javax.*"})
@SuppressStaticInitializationFor("org.apache.dolphinscheduler.spi.utils.PropertyUtils")
public class PytorchTaskTest {

    private final String pythonPath = ".";
    private final String requirementPath = "requirements.txt";

    @Before
    public void before() {
        PowerMockito.mockStatic(PropertyUtils.class);
    }

    @Test
    public void testPythonEnvManager() {
        PythonEnvManager envManager = new PythonEnvManager();

        envManager.setPythonEnvTool(PythonEnvManager.ENV_TOOL_CONDA);
        envManager.setCondaPythonVersion("3.9");
        String condaEnvCommand39 = envManager.getBuildEnvCommand(requirementPath);
        Assert.assertEquals(condaEnvCommand39, "conda create -y python=3.9 -p ./venv && source activate ./venv && ./venv/bin/python -m pip install -r " + requirementPath);

        envManager.setCondaPythonVersion("3.8");
        String condaEnvCommand38 = envManager.getBuildEnvCommand(requirementPath);
        Assert.assertEquals(condaEnvCommand38, "conda create -y python=3.8 -p ./venv && source activate ./venv && ./venv/bin/python -m pip install -r " + requirementPath);


        envManager.setPythonEnvTool(PythonEnvManager.ENV_TOOL_VENV);
        String venvEnvCommand = envManager.getBuildEnvCommand(requirementPath);
        Assert.assertEquals(venvEnvCommand, "virtualenv -p ${PYTHON_HOME} ./venv && source ./venv/bin/activate && ./venv/bin/python -m pip install -r " + requirementPath);

    }

    @Test
    public void testGitProject() {

        assertFalse(GitProjectManager.isGitPath("dolphinscheduler/test"));
        assertFalse(GitProjectManager.isGitPath("/dolphinscheduler/test"));
        assertTrue(GitProjectManager.isGitPath("https://github.com/apache/dolphinscheduler.git"));
        assertTrue(GitProjectManager.isGitPath("git@github.com:apache/dolphinscheduler.git"));
        assertTrue(GitProjectManager.isGitPath("git@github.com:apache/dolphinscheduler.git#doc"));

        GitProjectManager gpm1 = new GitProjectManager();
        gpm1.setPath("git@github.com:apache/dolphinscheduler.git#doc");
        Assert.assertEquals("git@github.com:apache/dolphinscheduler.git", gpm1.getGitUrl());
        Assert.assertEquals("./GIT_PROJECT/doc", gpm1.getGitLocalPath());

        GitProjectManager gpm2 = new GitProjectManager();
        gpm2.setPath("git@github.com:apache/dolphinscheduler.git");
        Assert.assertEquals("git@github.com:apache/dolphinscheduler.git", gpm2.getGitUrl());
        Assert.assertEquals("./GIT_PROJECT", gpm2.getGitLocalPath());

    }

    @Test
    public void testBuildPythonCommandWithoutCreateEnvironment() throws Exception {
        PytorchParameters parameters = new PytorchParameters();
        parameters.setScript("main.py");
        parameters.setScriptParams("--epochs=1 --dry-run");

        PytorchTask task1 = initTask(parameters);
        Assert.assertEquals(task1.buildPythonExecuteCommand(),
            "export PYTHONPATH=.\n" +
                "${PYTHON_HOME} main.py --epochs=1 --dry-run");

        parameters.setPythonCommand("");
        PytorchTask task2 = initTask(parameters);
        Assert.assertEquals(task2.buildPythonExecuteCommand(),
            "export PYTHONPATH=.\n" +
                "${PYTHON_HOME} main.py --epochs=1 --dry-run");

        parameters.setPythonCommand("/usr/bin/python");
        PytorchTask task3 = initTask(parameters);
        Assert.assertEquals(task3.buildPythonExecuteCommand(),
            "export PYTHONPATH=.\n" +
                "/usr/bin/python main.py --epochs=1 --dry-run");

    }


    @Test
    public void testBuildPythonCommandWithCreateCondeEnv() throws Exception {
        PytorchParameters parameters = new PytorchParameters();
        parameters.setPythonPath(pythonPath);
        parameters.setIsCreateEnvironment(true);
        parameters.setCondaPythonVersion("3.6");
        parameters.setPythonEnvTool(PythonEnvManager.ENV_TOOL_CONDA);
        parameters.setRequirements("requirements.txt");
        parameters.setScript("main.py");
        parameters.setScriptParams("--epochs=1 --dry-run");

        PytorchTask task = initTask(parameters);
        Assert.assertEquals(task.buildPythonExecuteCommand(),
            "export PYTHONPATH=.\n" +
                "conda create -y python=3.6 -p ./venv && source activate ./venv && ./venv/bin/python -m pip install -r requirements.txt\n" +
                "./venv/bin/python main.py --epochs=1 --dry-run");
    }

    @Test
    public void testBuildPythonCommandWithCreateVenvEnv() throws Exception {
        PytorchParameters parameters = new PytorchParameters();
        parameters.setPythonPath(pythonPath);
        parameters.setIsCreateEnvironment(true);
        parameters.setPythonEnvTool(PythonEnvManager.ENV_TOOL_VENV);
        parameters.setRequirements("requirements.txt");
        parameters.setScript("main.py");
        parameters.setScriptParams("--epochs=1 --dry-run");

        PytorchTask task = initTask(parameters);
        Assert.assertEquals(task.buildPythonExecuteCommand(),
            "export PYTHONPATH=.\n" +
                "virtualenv -p ${PYTHON_HOME} ./venv && source ./venv/bin/activate && ./venv/bin/python -m pip install -r requirements.txt\n" +
                "./venv/bin/python main.py --epochs=1 --dry-run");

    }

    @Test
    public void testGetPossiblePath() throws Exception {
        String requirements = "requirements.txt";
        String script = "train.py";
        String pythonPath = Paths.get("/tmp", UUID.randomUUID().toString()).toString();

        PytorchParameters parameters = new PytorchParameters();
        parameters.setRequirements(requirements);
        parameters.setScript(script);
        parameters.setPythonPath(pythonPath);
        parameters.setIsCreateEnvironment(true);
        parameters.setPythonEnvTool(PythonEnvManager.ENV_TOOL_VENV);

        PytorchTask task = initTask(parameters);

        String requirementFile = Paths.get(pythonPath, requirements).toString();
        String scriptFile = Paths.get(pythonPath, script).toString();
        createFile(requirementFile);
        createFile(scriptFile);

        String expected = "export PYTHONPATH=%s\n" +
            "virtualenv -p ${PYTHON_HOME} ./venv && source ./venv/bin/activate && ./venv/bin/python -m pip install -r %s\n" +
            "./venv/bin/python %s";
        System.out.println(task.buildPythonExecuteCommand());
        Assert.assertEquals(String.format(expected, pythonPath, requirementFile, scriptFile), task.buildPythonExecuteCommand());

    }


    private PytorchTask initTask(PytorchParameters pytorchParameters) {
        TaskExecutionContext taskExecutionContext = createContext(pytorchParameters);
        PytorchTask task = new PytorchTask(taskExecutionContext);
        task.init();
        return task;
    }

    public TaskExecutionContext createContext(PytorchParameters pytorchParameters) {
        String parameters = JSONUtils.toJsonString(pytorchParameters);
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskLogName()).thenReturn("PytorchTest");
        String APP_ID = UUID.randomUUID().toString();
        String folder = String.format("/tmp/dolphinscheduler_PytorchTest_%s", APP_ID);
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn(folder);
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn(APP_ID);
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getLogPath()).thenReturn(folder + "/log");
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        String envirementConfig = "export PATH=$HOME/anaconda3/bin:$PATH\n" + "export PYTHON_HOME=/bin/python";
        Mockito.when(taskExecutionContext.getEnvironmentConfig()).thenReturn(envirementConfig);

        String userName = System.getenv().get("USER");
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn(userName);

        TaskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);
        return taskExecutionContext;
    }

    private void createFile(String fileName) throws Exception {
        File file = new File(fileName);
        Path path = file.toPath();
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString(RWXR_XR_X);
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
        if (SystemUtils.IS_OS_WINDOWS) {
            Files.createFile(path);
        } else {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                Files.createFile(path, attr);
            } catch (FileAlreadyExistsException ex) {
                // this is expected
            }
        }

    }

}


