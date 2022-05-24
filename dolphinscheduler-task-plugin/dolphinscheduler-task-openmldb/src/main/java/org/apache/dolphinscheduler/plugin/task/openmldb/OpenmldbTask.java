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

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.python.PythonTask;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

/**
 * openmldb task
 */
public class OpenmldbTask extends PythonTask {

    /**
     * openmldb parameters
     */
    private OpenmldbParameters openmldbParameters;

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
    }

    @Override
    public void init() {
        logger.info("openmldb task params {}", taskRequest.getTaskParams());

        openmldbParameters = JSONUtils.parseObject(taskRequest.getTaskParams(), OpenmldbParameters.class);

        if (openmldbParameters == null || !openmldbParameters.checkParameters()) {
            throw new TaskException("openmldb task params is not valid");
        }
    }

    @Override
    @Deprecated
    public String getPreScript() {
        return "";
    }

    @Override
    public AbstractParameters getParameters() {
        return openmldbParameters;
    }

    /**
     * build python command file path
     *
     * @return python command file path
     */
    @Override
    protected String buildPythonCommandFilePath() {
        return String.format("%s/openmldb_%s.py", taskRequest.getExecutePath(), taskRequest.getTaskAppId());
    }

    /**
     * build python script content from sql
     *
     * @return raw python script
     */
    @Override
    protected String buildPythonScriptContent() {
        logger.info("raw sql script : {}", openmldbParameters.getSql());

        String rawSQLScript = openmldbParameters.getSql().replaceAll("[\\r]?\\n", "\n");
        Map<String, Property> paramsMap = mergeParamsWithContext(openmldbParameters);
        rawSQLScript = ParameterUtils.convertParameterPlaceholders(rawSQLScript, ParamUtils.convert(paramsMap));

        // convert sql to python script
        String pythonScript = buildPythonScriptsFromSql(rawSQLScript);
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
        builder.append("con.execute(\"set @@execute_mode='").append(executeMode).append("';\")\n");
        // offline job should be sync, and set job_timeout to 30min(==server.channel_keep_alive_time).
        // You can set it longer in sqls.
        if (executeMode.equals("offline")) {
            builder.append("con.execute(\"set @@sync_job=true\")\n");
            builder.append("con.execute(\"set @@job_timeout=1800000\")\n");
        }

        // split sql to list
        // skip the sql only has space characters
        Pattern pattern = Pattern.compile("\\S");
        for (String sql : rawSqlScript.split(";")) {
            if (pattern.matcher(sql).find()) {
                sql = sql.replaceAll("\\n", "\\\\n");
                builder.append("con.execute(\"").append(sql).append("\")\n");
            }
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
    @Override
    protected String buildPythonExecuteCommand(String pythonFile) {
        Preconditions.checkNotNull(pythonFile, "Python file cannot be null");
        return getPythonCommand() + " " + pythonFile;
    }

    private String getPythonCommand() {
        String pythonHome = System.getenv(PYTHON_HOME);
        return getPythonCommand(pythonHome);
    }

    private String getPythonCommand(String pythonHome) {
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
