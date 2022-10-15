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

package org.apache.dolphinscheduler.plugin.task.python;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
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
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * python task
 */
public class PythonTask extends AbstractTask {

    /**
     * python parameters
     */
    protected PythonParameters pythonParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    protected TaskExecutionContext taskRequest;

    protected static final String PYTHON_HOME = "PYTHON_HOME";

    private static final String DEFAULT_PYTHON_VERSION = "python";

    /**
     * constructor
     *
     * @param taskRequest taskRequest
     */
    public PythonTask(TaskExecutionContext taskRequest) {
        super(taskRequest);
        this.taskRequest = taskRequest;

        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskRequest,
                logger);
    }

    @Override
    public void init() {
        logger.info("python task params {}", taskRequest.getTaskParams());

        pythonParameters = JSONUtils.parseObject(taskRequest.getTaskParams(), PythonParameters.class);

        if (!pythonParameters.checkParameters()) {
            throw new TaskException("python task params is not valid");
        }
    }

    @Override
    public String getPreScript() {
        String rawPythonScript = pythonParameters.getRawScript().replaceAll("\\r\\n", "\n");
        try {
            rawPythonScript = convertPythonScriptPlaceholders(rawPythonScript);
        } catch (StringIndexOutOfBoundsException e) {
            logger.error("setShareVar field format error, raw python script : {}", rawPythonScript);
        }
        return rawPythonScript;
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            // generate the content of this python script
            String pythonScriptContent = buildPythonScriptContent();
            // generate the file path of this python script
            String pythonScriptFile = buildPythonCommandFilePath();

            // create this file
            createPythonCommandFileIfNotExists(pythonScriptContent, pythonScriptFile);
            String command = buildPythonExecuteCommand(pythonScriptFile);

            TaskResponse taskResponse = shellCommandExecutor.run(command);
            setExitStatusCode(taskResponse.getExitStatusCode());
            setProcessId(taskResponse.getProcessId());
            setVarPool(shellCommandExecutor.getVarPool());
        } catch (Exception e) {
            logger.error("python task failure", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("run python task error", e);
        }
    }

    @Override
    public void cancel() throws TaskException {
        // cancel process
        try {
            shellCommandExecutor.cancelApplication();
        } catch (Exception e) {
            throw new TaskException("cancel application error", e);
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return pythonParameters;
    }

    /**
     * convertPythonScriptPlaceholders
     *
     * @param rawScript rawScript
     * @return String
     * @throws StringIndexOutOfBoundsException if substring index is out of bounds
     */
    private static String convertPythonScriptPlaceholders(String rawScript) throws StringIndexOutOfBoundsException {
        int len = "${setShareVar(${".length();
        int scriptStart = 0;
        while ((scriptStart = rawScript.indexOf("${setShareVar(${", scriptStart)) != -1) {
            int start = -1;
            int end = rawScript.indexOf('}', scriptStart + len);
            String prop = rawScript.substring(scriptStart + len, end);

            start = rawScript.indexOf(',', end);
            end = rawScript.indexOf(')', start);

            String value = rawScript.substring(start + 1, end);

            start = rawScript.indexOf('}', start) + 1;
            end = rawScript.length();

            String replaceScript = String.format("print(\"${{setValue({},{})}}\".format(\"%s\",%s))", prop, value);

            rawScript = rawScript.substring(0, scriptStart) + replaceScript + rawScript.substring(start, end);

            scriptStart += replaceScript.length();
        }
        return rawScript;
    }

    /**
     * create python command file if not exists
     *
     * @param pythonScript     exec python script
     * @param pythonScriptFile python script file
     * @throws IOException io exception
     */
    protected void createPythonCommandFileIfNotExists(String pythonScript, String pythonScriptFile) throws IOException {
        logger.info("tenantCode :{}, task dir:{}", taskRequest.getTenantCode(), taskRequest.getExecutePath());

        if (!Files.exists(Paths.get(pythonScriptFile))) {
            logger.info("generate python script file:{}", pythonScriptFile);

            StringBuilder sb = new StringBuilder();
            sb.append("#-*- encoding=utf8 -*-\n");

            sb.append("\n\n");
            sb.append(pythonScript);
            logger.info(sb.toString());

            // write data to file
            FileUtils.writeStringToFile(new File(pythonScriptFile),
                    sb.toString(),
                    StandardCharsets.UTF_8);
        }
    }

    /**
     * build python command file path
     *
     * @return python command file path
     */
    protected String buildPythonCommandFilePath() {
        return String.format("%s/py_%s.py", taskRequest.getExecutePath(), taskRequest.getTaskAppId());
    }

    /**
     * build python script content
     *
     * @return raw python script
     * @throws Exception exception
     */
    protected String buildPythonScriptContent() throws Exception {
        logger.info("raw python script : {}", pythonParameters.getRawScript());
        String rawPythonScript = pythonParameters.getRawScript().replaceAll("\\r\\n", "\n");
        Map<String, Property> paramsMap = mergeParamsWithContext(pythonParameters);
        return ParameterUtils.convertParameterPlaceholders(rawPythonScript, ParamUtils.convert(paramsMap));
    }

    protected Map<String, Property> mergeParamsWithContext(AbstractParameters parameters) {
        // replace placeholder
        return taskRequest.getPrepareParamsMap();
    }

    /**
     * Build the python task command.
     * If user have set the 'PYTHON_HOME' environment, we will use the 'PYTHON_HOME',
     * if not, we will default use python.
     *
     * @param pythonFile Python file, cannot be empty.
     * @return Python execute command, e.g. 'python test.py'.
     */
    protected String buildPythonExecuteCommand(String pythonFile) {
        Preconditions.checkNotNull(pythonFile, "Python file cannot be null");

        String pythonHome = String.format("${%s}", PYTHON_HOME);

        return pythonHome + " " + pythonFile;
    }

}
