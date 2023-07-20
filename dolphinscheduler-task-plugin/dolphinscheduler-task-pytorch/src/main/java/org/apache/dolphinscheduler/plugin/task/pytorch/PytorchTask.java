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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.shell.IShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.shell.ShellInterceptorBuilderFactory;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PytorchTask extends AbstractTask {

    private final ShellCommandExecutor shellCommandExecutor;
    protected PytorchParameters pytorchParameters;
    protected TaskExecutionContext taskExecutionContext;
    private PythonEnvManager pythonEnvManager;

    public PytorchTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;

        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                log);
    }

    @Override
    public void init() {

        pytorchParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), PytorchParameters.class);
        log.info("Initialize pytorch task params {}", JSONUtils.toPrettyJsonString(taskExecutionContext));

        if (pytorchParameters == null || !pytorchParameters.checkParameters()) {
            throw new TaskException("python task params is not valid");
        }

        pythonEnvManager = new PythonEnvManager();
        pythonEnvManager.setPythonEnvTool(pytorchParameters.getPythonEnvTool());
        pythonEnvManager.setCondaPythonVersion(pytorchParameters.getCondaPythonVersion());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            IShellInterceptorBuilder<?, ?> shellActuatorBuilder = ShellInterceptorBuilderFactory.newBuilder()
                    .properties(ParameterUtils.convert(taskExecutionContext.getPrepareParamsMap()))
                    .appendScript(buildPythonExecuteCommand());

            TaskResponse taskResponse = shellCommandExecutor.run(shellActuatorBuilder, taskCallBack);
            setExitStatusCode(taskResponse.getExitStatusCode());
            setProcessId(taskResponse.getProcessId());
            setVarPool(shellCommandExecutor.getVarPool());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("The current Pytorch task has been interrupted", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("The current Pytorch task has been interrupted", e);
        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("Pytorch task execute failed", e);
        }
    }

    @Override
    public void cancel() throws TaskException {

    }

    public String buildPythonExecuteCommand() throws Exception {
        List<String> args = new ArrayList<>();

        String pythonPath = pytorchParameters.getPythonPath();

        if (GitProjectManager.isGitPath(pythonPath)) {
            GitProjectManager gpm = new GitProjectManager();
            gpm.setPath(pythonPath);
            gpm.setBaseDir(taskExecutionContext.getExecutePath());
            gpm.prepareProject();
            pytorchParameters.setPythonPath(gpm.getGitLocalPath());
        }

        args.add(String.format("export PYTHONPATH=%s", pytorchParameters.getPythonPath()));

        if (pytorchParameters.getIsCreateEnvironment()) {
            String buildEnvCommand = pythonEnvManager.getBuildEnvCommand(pytorchParameters.getRequirementPath());
            args.add(buildEnvCommand);
        }

        String scriptParams = pytorchParameters.getScriptParams();
        if (scriptParams != null && !scriptParams.isEmpty()) {
            args.add(String.format("%s %s %s", getPythonCommand(), pytorchParameters.getScriptPath(),
                    pytorchParameters.getScriptParams()));
        } else {
            args.add(String.format("%s %s", getPythonCommand(), pytorchParameters.getScriptPath()));

        }
        return args.stream().collect(Collectors.joining("\n"));
    }

    private String getPythonCommand() {
        String pythonCommand;
        if (pytorchParameters.getIsCreateEnvironment()) {
            pythonCommand = pythonEnvManager.getPythonCommand();
        } else {
            pythonCommand = pytorchParameters.getPythonLauncher();
        }
        return pythonCommand;

    }

    @Override
    public AbstractParameters getParameters() {
        return pytorchParameters;
    }
}
