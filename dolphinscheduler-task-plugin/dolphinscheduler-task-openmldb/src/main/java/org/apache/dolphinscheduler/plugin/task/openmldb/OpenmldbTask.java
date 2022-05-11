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

package org.apache.dolphinscheduler.plugin.task.openmldb;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

/**
 * openmldb task
 */
public class OpenmldbTask extends AbstractTaskExecutor {

    /**
     * openmldb parameters
     */
    private OpenmldbParameters openmldbParameters;

    /**
     * shell command executor
     */
    private final ShellCommandExecutor shellCommandExecutor;

    private final TaskExecutionContext taskRequest;

    private static final String PYTHON_HOME = "PYTHON_HOME";

    /**
     * python process(openmldb only supports version 3 by default)
     */
    private static final String OPENMLDB_PYTHON = "python3";
    private static final Pattern PYTHON_PATH_PATTERN = Pattern.compile("/bin/python[\\d.]*$");

    /**
     * constructor
     *
     * @param taskRequest taskRequest
     */
    public OpenmldbTask(TaskExecutionContext taskRequest) {
        super(taskRequest);
        this.taskRequest = taskRequest;

        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskRequest,
                logger);
    }

    @Override
    public void init() {
        logger.info("openmldb task params {}", taskRequest.getTaskParams());

        openmldbParameters = JSONUtils.parseObject(taskRequest.getTaskParams(), OpenmldbParameters.class);

        assert openmldbParameters != null;
        if (!openmldbParameters.checkParameters()) {
            throw new TaskException("openmldb task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {
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
            setAppIds(taskResponse.getAppIds());
            setProcessId(taskResponse.getProcessId());
            setVarPool(shellCommandExecutor.getVarPool());
        } catch (Exception e) {
            logger.error("openmldb task failure", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("run openmldb task error", e);
        }
    }

    @Override
    public void cancelApplication(boolean cancelApplication) throws Exception {
        // cancel process
        shellCommandExecutor.cancelApplication();
    }

    @Override
    public AbstractParameters getParameters() {
        return openmldbParameters;
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
        return String.format("%s/openmldb_%s.py", taskRequest.getExecutePath(), taskRequest.getTaskAppId());
    }

    /**
     * build python script content
     *
     * @return raw python script
     */
    private String buildPythonScriptContent() {
        // sqls doesn't need \n, use ; to split
        String rawSqlScript = openmldbParameters.getSql().replaceAll("[\\r]?\\n", " ");

        // replace placeholder
        Map<String, Property> paramsMap = ParamUtils.convert(taskRequest, openmldbParameters);
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(taskRequest.getParamsMap())) {
            paramsMap.putAll(taskRequest.getParamsMap());
        }
        rawSqlScript = ParameterUtils.convertParameterPlaceholders(rawSqlScript, ParamUtils.convert(paramsMap));
        logger.info("raw sql script : {}", rawSqlScript);

        // convert sql to python script
        String pythonScript = buildPythonScriptsFromSql(rawSqlScript);
        logger.info("rendered python script : {}", pythonScript);

        return pythonScript;
    }

    private String buildPythonScriptsFromSql(String rawSqlScript) {
        // imports
        StringBuilder builder = new StringBuilder("import openmldb\nimport sqlalchemy as db\n");

        // connect to openmldb
        builder.append(String.format("engine = db.create_engine('openmldb:///?zk=%s&zkPath=%s')\n",
                openmldbParameters.getZk(), openmldbParameters.getZkPath()));
        builder.append("con = engine.connect()\n");

        // execute mode
        String executeMode = openmldbParameters.getExecuteMode().toLowerCase(Locale.ROOT);
        builder.append("con.execute(\"set @@execute_mode='").append(executeMode).append("';" +
                "\")\n");
        // offline job should be sync, and set job_timeout to 30min(==server.channel_keep_alive_time).
        // You can set it longer in sqls.
        if (executeMode.equals("offline")) {
            builder.append("con.execute(\"set @@sync_job=true\")\n");
            builder.append("con.execute(\"set @@job_timeout=1800000\")\n");
        }

        // split sqls to list
        for (String sql : rawSqlScript.split(";")) {
            sql = sql.trim();
            if (sql.isEmpty()) {
                continue;
            }
            builder.append("con.execute(\"").append(sql).append("\")\n");
        }
        return builder.toString();
    }

    /**
     * Build the python task command.
     * If user have set the 'PYTHON_HOME' environment, we will use the 'PYTHON_HOME',
     * if not, we will default use python.
     *
     * @param pythonFile Python file, cannot be empty.
     * @return Python execute command, e.g. 'python test.py'.
     */
    private String buildPythonExecuteCommand(String pythonFile) {
        Preconditions.checkNotNull(pythonFile, "Python file cannot be null");
        return getPythonCommand() + " " + pythonFile;
    }

    public String getPythonCommand() {
        String pythonHome = System.getenv(PYTHON_HOME);
        return getPythonCommand(pythonHome);
    }

    public String getPythonCommand(String pythonHome) {
        if (StringUtils.isEmpty(pythonHome)) {
            return OPENMLDB_PYTHON;
        }
        // If your python home is "xx/bin/python[xx]", you are forced to use python3
        String pythonBinPath = "/bin/" + OPENMLDB_PYTHON;
        Matcher matcher = PYTHON_PATH_PATTERN.matcher(pythonHome);
        if (matcher.find()) {
            return matcher.replaceAll(pythonBinPath);
        }
        return Paths.get(pythonHome, pythonBinPath).toString();
    }
}
