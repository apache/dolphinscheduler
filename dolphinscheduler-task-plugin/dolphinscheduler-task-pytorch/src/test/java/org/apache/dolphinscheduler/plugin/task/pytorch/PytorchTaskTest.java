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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PytorchTaskTest {

    private final String pythonPath = ".";

    @Mock
    private ShellCommandExecutor shellCommandExecutor;

    @Test
    public void testPythonEnvManager() {
        PythonEnvManager envManager = new PythonEnvManager();

        envManager.setPythonEnvTool(PythonEnvManager.ENV_TOOL_CONDA);
        envManager.setCondaPythonVersion("3.9");
        String requirementPath = "requirements.txt";
        String condaEnvCommand39 = envManager.getBuildEnvCommand(requirementPath);
        assertEquals(condaEnvCommand39,
                "conda create -y python=3.9 -p ./venv && source activate ./venv && ./venv/bin/python -m pip install -r "
                        + requirementPath);

        envManager.setCondaPythonVersion("3.8");
        String condaEnvCommand38 = envManager.getBuildEnvCommand(requirementPath);
        assertEquals(condaEnvCommand38,
                "conda create -y python=3.8 -p ./venv && source activate ./venv && ./venv/bin/python -m pip install -r "
                        + requirementPath);

        envManager.setPythonEnvTool(PythonEnvManager.ENV_TOOL_VENV);
        String venvEnvCommand = envManager.getBuildEnvCommand(requirementPath);
        assertEquals(venvEnvCommand,
                "virtualenv -p ${PYTHON_LAUNCHER} ./venv && source ./venv/bin/activate && ./venv/bin/python -m pip install -r "
                        + requirementPath);

    }

    @Test
    public void testGitProjectUrlInjection() {
        assertFalse(GitProjectManager.isGitPath("git@& cat /etc/passwd >/poc.txt #"));
        assertFalse(GitProjectManager.isGitPath("git@| cat /etc/passwd >/poc.txt #"));
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
        assertEquals("git@github.com:apache/dolphinscheduler.git", gpm1.getGitUrl());
        assertEquals("./GIT_PROJECT/doc", gpm1.getGitLocalPath());

        GitProjectManager gpm2 = new GitProjectManager();
        gpm2.setPath("git@github.com:apache/dolphinscheduler.git");
        assertEquals("git@github.com:apache/dolphinscheduler.git", gpm2.getGitUrl());
        assertEquals("./GIT_PROJECT", gpm2.getGitLocalPath());

    }

    @Test
    public void testBuildPythonCommandWithoutCreateEnvironment() throws Exception {
        PytorchParameters parameters = new PytorchParameters();
        parameters.setScript("main.py");
        parameters.setScriptParams("--epochs=1 --dry-run");

        PytorchTask task1 = initTask(parameters);
        assertEquals(task1.buildPythonExecuteCommand(),
                "export PYTHONPATH=.\n" +
                        "${PYTHON_LAUNCHER} main.py --epochs=1 --dry-run");

        parameters.setPythonLauncher("");
        PytorchTask task2 = initTask(parameters);
        assertEquals(task2.buildPythonExecuteCommand(),
                "export PYTHONPATH=.\n" +
                        "${PYTHON_LAUNCHER} main.py --epochs=1 --dry-run");

        parameters.setPythonLauncher("/usr/bin/python");
        PytorchTask task3 = initTask(parameters);
        assertEquals(task3.buildPythonExecuteCommand(),
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
        assertEquals(task.buildPythonExecuteCommand(),
                "export PYTHONPATH=.\n" +
                        "conda create -y python=3.6 -p ./venv && source activate ./venv && ./venv/bin/python -m pip install -r requirements.txt\n"
                        +
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
        assertEquals(task.buildPythonExecuteCommand(),
                "export PYTHONPATH=.\n" +
                        "virtualenv -p ${PYTHON_LAUNCHER} ./venv && source ./venv/bin/activate && ./venv/bin/python -m pip install -r requirements.txt\n"
                        +
                        "./venv/bin/python main.py --epochs=1 --dry-run");

    }

    @Test
    public void testGetPossiblePath() throws Exception {
        String requirements = "requirements.txt";
        String script = "train.py";
        String pyPath = Paths.get("/tmp", UUID.randomUUID().toString()).toString();

        PytorchParameters parameters = new PytorchParameters();
        parameters.setRequirements(requirements);
        parameters.setScript(script);
        parameters.setPythonPath(pyPath);
        parameters.setIsCreateEnvironment(true);
        parameters.setPythonEnvTool(PythonEnvManager.ENV_TOOL_VENV);

        PytorchTask task = initTask(parameters);

        String requirementFile = Paths.get(pythonPath, requirements).toString();
        String scriptFile = Paths.get(pythonPath, script).toString();
        createFile(requirementFile);
        createFile(scriptFile);

        String expected = "export PYTHONPATH=%s\n" +
                "virtualenv -p ${PYTHON_LAUNCHER} ./venv && source ./venv/bin/activate && ./venv/bin/python -m pip install -r %s\n"
                +
                "./venv/bin/python %s";
        System.out.println(task.buildPythonExecuteCommand());
        assertEquals(String.format(expected, pyPath, requirementFile, scriptFile),
                task.buildPythonExecuteCommand());

    }

    @Test
    void testCancel_success() throws TaskException, InterruptedException {
        String requirements = "requirements.txt";
        String script = "train.py";
        String pyPath = Paths.get("/tmp", UUID.randomUUID().toString()).toString();

        PytorchParameters parameters = new PytorchParameters();
        parameters.setRequirements(requirements);
        parameters.setScript(script);
        parameters.setPythonPath(pyPath);
        parameters.setIsCreateEnvironment(true);
        parameters.setPythonEnvTool(PythonEnvManager.ENV_TOOL_VENV);

        PytorchTask task = initTask(parameters);
        // Inject mock via reflection
        setField(task, "shellCommandExecutor", shellCommandExecutor);

        // Act
        assertDoesNotThrow(task::cancel);

        // Assert: verify the mock was called
        verify(shellCommandExecutor).cancelApplication();
    }

    @Test
    void testCancel_cancelApplicationThrowsException_throwsTaskException() throws InterruptedException {
        String requirements = "requirements.txt";
        String script = "train.py";
        String pyPath = Paths.get("/tmp", UUID.randomUUID().toString()).toString();

        PytorchParameters parameters = new PytorchParameters();
        parameters.setRequirements(requirements);
        parameters.setScript(script);
        parameters.setPythonPath(pyPath);
        parameters.setIsCreateEnvironment(true);
        parameters.setPythonEnvTool(PythonEnvManager.ENV_TOOL_VENV);

        PytorchTask task = initTask(parameters);
        // Inject mock via reflection
        setField(task, "shellCommandExecutor", shellCommandExecutor);

        // Arrange
        doThrow(new RuntimeException("Failed to kill process"))
                .when(shellCommandExecutor).cancelApplication();

        // Act & Assert
        TaskException exception = assertThrows(TaskException.class, () -> {
            task.cancel();
        });

        assertEquals("cancel application error", exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertEquals("Failed to kill process", exception.getCause().getMessage());

        verify(shellCommandExecutor).cancelApplication();
    }

    // Helper: get private field via reflection
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
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
