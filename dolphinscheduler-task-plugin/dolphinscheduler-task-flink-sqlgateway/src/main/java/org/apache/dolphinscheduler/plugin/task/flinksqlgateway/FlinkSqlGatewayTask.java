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

package org.apache.dolphinscheduler.plugin.task.flinksqlgateway;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.SqlSourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlinkSqlGatewayTask extends AbstractTask {

    private final TaskExecutionContext taskExecutionContext;
    private FlinkSqlGatewayParameters parameters;

    private volatile Connection connection;
    private volatile Statement statement;

    public FlinkSqlGatewayTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        parameters =
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), FlinkSqlGatewayParameters.class);

        log.info("Initialize flink sqlgateway task params {}", JSONUtils.toPrettyJsonString(parameters));

        if (parameters == null || !parameters.checkParameters()) {
            throw new TaskException("flink sqlgateway task params is not valid");
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return parameters;
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            String jdbcUrl = resolveFlinkJdbcUrl();
            Properties props = parameters.toJdbcProperties();
            log.info("Connecting flink sql gateway with jdbcUrl: {}", jdbcUrl);
            connection = DriverManager.getConnection(jdbcUrl, props);
            statement = connection.createStatement();

            executeScriptIfPresent(resolveScriptContent(parameters.getInitScriptType(),
                    parameters.getInitScript(), parameters.getInitScriptResourceList()), "init");
            executeScriptIfPresent(resolveScriptContent(parameters.getRawScriptType(),
                    parameters.getRawScript(), parameters.getResourceList()), "main");

            setExitStatusCode(TaskConstants.EXIT_CODE_SUCCESS);
        } catch (Exception e) {
            if (exitStatusCode == TaskConstants.EXIT_CODE_KILL) {
                log.info("flink sqlgateway task has been killed");
                return;
            }
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("Execute flink sqlgateway task failed", e);
        } finally {
            closeQuietly(statement);
            closeQuietly(connection);
        }
    }

    @Override
    public void cancel() throws TaskException {
        exitStatusCode = TaskConstants.EXIT_CODE_KILL;
        try {
            if (statement != null) {
                statement.cancel();
            }
        } catch (Exception e) {
            throw new TaskException("Cancel flink sqlgateway task failed", e);
        } finally {
            closeQuietly(statement);
            closeQuietly(connection);
        }
    }

    private String resolvePlaceholderParams(String content) {
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        Map<String, String> stringParams = ParameterUtils.convert(paramsMap);
        return ParameterUtils.convertParameterPlaceholders(content, stringParams);
    }

    private String resolveFlinkJdbcUrl() {
        return resolvePlaceholderParams(parameters.getFlinkJdbcUrl());
    }

    private String resolveScriptContent(SqlSourceType scriptType, String inlineScript,
                                        List<ResourceInfo> resourceList) throws Exception {
        if (scriptType == SqlSourceType.FILE && resourceList != null && !resourceList.isEmpty()) {
            String resourceName = resourceList.get(0).getResourceName();
            ResourceContext resourceContext = taskExecutionContext.getResourceContext();
            String localPath = resourceContext.getResourceItem(resourceName).getResourceAbsolutePathInLocal();
            String content = FileUtils.readFileToString(new File(localPath), StandardCharsets.UTF_8);
            log.info("Load {} sql script from resource file: {}, {} chars", scriptType, localPath, content.length());
            return resolvePlaceholderParams(content);
        }
        if (scriptType == SqlSourceType.SCRIPT && StringUtils.isNotBlank(inlineScript)) {
            return resolvePlaceholderParams(inlineScript);
        }
        return inlineScript;
    }

    private void executeScriptIfPresent(String script, String tag) throws Exception {
        if (StringUtils.isBlank(script)) {
            return;
        }
        List<String> sqlList = splitSql(script, parameters.getStatementSeparator());
        for (int i = 0; i < sqlList.size(); i++) {
            String sql = sqlList.get(i);
            String trimmed = stripLeadingCommentsAndBlanks(sql == null ? "" : sql);
            if (trimmed.isEmpty()) {
                continue;
            }

            log.info("[{}] execute #{} sql: {}", tag, i + 1, trimmed);

            boolean hasResultSet = statement.execute(trimmed);
            if (hasResultSet) {
                try (ResultSet rs = statement.getResultSet()) {
                    int row = 0;
                    int maxPrint = Math.max(parameters.getMaxPrintRows(), 0);
                    while (rs != null && rs.next() && row < maxPrint) {
                        row++;
                        // avoid log explosion, print row index only
                        log.info("[{}] query result row {}", tag, row);
                    }
                    if (maxPrint > 0) {
                        log.info("[{}] query printed rows: {}", tag, row);
                    }
                }
            } else {
                int updateCount = statement.getUpdateCount();
                log.info("[{}] updateCount={}", tag, updateCount);
            }
        }
    }

    static List<String> splitSql(String script, String separator) {
        String sep = StringUtils.isBlank(separator) ? ";" : separator;
        return Arrays.stream(script.split(Pattern.quote(sep)))
                .collect(Collectors.toList());
    }

    /**
     * Strip leading line comments (-- ...) and blank lines so that Flink parser
     * receives only the actual statement (e.g. SET pipeline.name='...').
     */
    static String stripLeadingCommentsAndBlanks(String sql) {
        if (sql == null) {
            return "";
        }
        String[] lines = sql.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                return String.join("\n", Arrays.copyOfRange(lines, i, lines.length)).trim();
            }
        }
        // all lines are comments or blank, nothing to execute
        return "";
    }

    private static void closeQuietly(AutoCloseable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (Exception ignored) {
            // ignored
        }
    }
}
