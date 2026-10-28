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

package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.SqlSourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

/**
 * Submits Flink SQL to a remote SQL Gateway over JDBC.
 * Kept off {@link org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask#handle}
 * so the sql-client.sh path is not involved.
 */
@Slf4j
public class FlinkSqlGatewayExecutor {

    static final String DML_SYNC_SQL = "SET 'table.dml-sync'='true'";

    private final TaskExecutionContext taskExecutionContext;
    private final FlinkParameters parameters;
    private final FlinkTask task;

    private final Object lock = new Object();
    private volatile boolean killed;
    private volatile Connection connection;
    private volatile Statement statement;

    public FlinkSqlGatewayExecutor(TaskExecutionContext taskExecutionContext,
                                   FlinkParameters parameters,
                                   FlinkTask task) {
        this.taskExecutionContext = taskExecutionContext;
        this.parameters = parameters;
        this.task = task;
    }

    public void handle() throws TaskException {
        try {
            if (stopIfKilled()) {
                return;
            }
            String jdbcUrl = resolvePlaceholderParams(parameters.getFlinkJdbcUrl());
            Properties props = parameters.toJdbcProperties();
            log.info("Connecting flink sql gateway with jdbcUrl: {}", jdbcUrl);
            Connection opened = DriverManager.getConnection(jdbcUrl, props);
            synchronized (lock) {
                if (killed) {
                    closeQuietly(opened);
                    task.setExitStatusCode(TaskConstants.EXIT_CODE_KILL);
                    return;
                }
                connection = opened;
                statement = connection.createStatement();
            }

            executeAndInterpret(DML_SYNC_SQL, "sync");
            executeScriptIfPresent(resolveScriptContent(parameters.getInitScriptType(),
                    parameters.getInitScript(), parameters.getInitScriptResourceList()), "init");
            executeScriptIfPresent(resolveScriptContent(parameters.getRawScriptType(),
                    parameters.getRawScript(), parameters.getResourceList()), "main");

            if (stopIfKilled()) {
                return;
            }
            task.setExitStatusCode(TaskConstants.EXIT_CODE_SUCCESS);
        } catch (Exception e) {
            if (killed || task.getExitStatusCode() == TaskConstants.EXIT_CODE_KILL) {
                log.info("flink sql gateway task has been killed");
                task.setExitStatusCode(TaskConstants.EXIT_CODE_KILL);
                return;
            }
            task.setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("Execute flink sql gateway task failed", e);
        } finally {
            closeQuietly(statement);
            closeQuietly(connection);
        }
    }

    public void cancel() throws TaskException {
        Statement toCancel;
        Connection toClose;
        synchronized (lock) {
            killed = true;
            task.setExitStatusCode(TaskConstants.EXIT_CODE_KILL);
            toCancel = statement;
            toClose = connection;
        }
        try {
            if (toCancel != null) {
                toCancel.cancel();
            }
        } catch (Exception e) {
            throw new TaskException("Cancel flink sql gateway task failed", e);
        } finally {
            closeQuietly(toCancel);
            closeQuietly(toClose);
        }
    }

    /**
     * A submitted job is success only after a terminal FINISHED status.
     * FAILED / CANCELED after a successful submit is still a task failure.
     * A sequence that never reaches a terminal state is not success.
     */
    static SubmitOutcome interpretStatusSequence(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return SubmitOutcome.NOT_FINISHED;
        }
        SubmitOutcome outcome = SubmitOutcome.NOT_FINISHED;
        for (String status : statuses) {
            if (status == null) {
                continue;
            }
            String normalized = status.trim().toUpperCase(Locale.ROOT);
            if ("FAILED".equals(normalized) || "CANCELED".equals(normalized) || "CANCELLED".equals(normalized)) {
                return SubmitOutcome.FAILURE;
            }
            if ("FINISHED".equals(normalized)) {
                outcome = SubmitOutcome.SUCCESS;
            }
        }
        return outcome;
    }

    /**
     * A job-id column alone means the statement was accepted, not that the batch finished.
     * After {@code table.dml-sync=true} the driver returns only once the job is terminal and
     * throws on failure, so a job-id result in that mode is a finished success.
     */
    static SubmitOutcome interpretSubmitResult(boolean hasResultSet, boolean jobIdOnly, boolean statusPresent,
                                               List<String> statuses, boolean syncEnabled) {
        if (!hasResultSet) {
            return SubmitOutcome.SUCCESS;
        }
        if (statusPresent) {
            return interpretStatusSequence(statuses);
        }
        if (jobIdOnly) {
            return syncEnabled ? SubmitOutcome.SUCCESS : SubmitOutcome.NOT_FINISHED;
        }
        return SubmitOutcome.SUCCESS;
    }

    private boolean stopIfKilled() {
        if (!killed) {
            return false;
        }
        task.setExitStatusCode(TaskConstants.EXIT_CODE_KILL);
        return true;
    }

    private String resolvePlaceholderParams(String content) {
        if (content == null) {
            return null;
        }
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();
        Map<String, String> stringParams = ParameterUtils.convert(paramsMap);
        return ParameterUtils.convertParameterPlaceholders(content, stringParams);
    }

    private String resolveScriptContent(SqlSourceType scriptType, String inlineScript,
                                        List<ResourceInfo> resourceList) throws Exception {
        if (scriptType == SqlSourceType.FILE && resourceList != null && !resourceList.isEmpty()) {
            String resourceName = resourceList.get(0).getResourceName();
            ResourceContext resourceContext = taskExecutionContext.getResourceContext();
            String localPath = resourceContext.getResourceItem(resourceName).getResourceAbsolutePathInLocal();
            String content = FileUtils.readFileToString(new File(localPath), StandardCharsets.UTF_8);
            log.info("Load sql script from resource file: {}, {} chars", localPath, content.length());
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
            if (stopIfKilled()) {
                return;
            }
            String sql = sqlList.get(i);
            String trimmed = stripLeadingCommentsAndBlanks(sql == null ? "" : sql);
            if (trimmed.isEmpty()) {
                continue;
            }
            log.info("[{}] execute #{} sql: {}", tag, i + 1, trimmed);
            executeAndInterpret(trimmed, tag);
        }
    }

    private void executeAndInterpret(String sql, String tag) throws Exception {
        if (stopIfKilled()) {
            return;
        }
        boolean hasResultSet = statement.execute(sql);
        SubmitOutcome outcome = readOutcome(hasResultSet);
        if (outcome == SubmitOutcome.FAILURE) {
            throw new TaskException("Flink job failed after submission, tag=" + tag);
        }
        if (outcome == SubmitOutcome.NOT_FINISHED) {
            throw new TaskException("Flink job was submitted but did not reach a terminal success state, tag=" + tag);
        }
    }

    private SubmitOutcome readOutcome(boolean hasResultSet) throws SQLException {
        if (!hasResultSet) {
            int updateCount = statement.getUpdateCount();
            log.info("updateCount={}", updateCount);
            return SubmitOutcome.SUCCESS;
        }
        try (ResultSet rs = statement.getResultSet()) {
            if (rs == null) {
                return SubmitOutcome.SUCCESS;
            }
            ResultSetMetaData meta = rs.getMetaData();
            int statusIndex = columnIndex(meta, "status");
            int jobIdIndex = columnIndex(meta, "job id", "job_id", "jobid");
            List<String> statuses = new ArrayList<>();
            boolean sawJobId = false;
            int row = 0;
            int maxPrint = Math.max(parameters.getMaxPrintRows(), 0);
            while (rs.next()) {
                row++;
                if (jobIdIndex > 0 && StringUtils.isNotBlank(rs.getString(jobIdIndex))) {
                    sawJobId = true;
                    log.info("flink sql gateway job id: {}", rs.getString(jobIdIndex));
                }
                if (statusIndex > 0) {
                    statuses.add(rs.getString(statusIndex));
                }
                if (maxPrint > 0 && row <= maxPrint) {
                    log.info("query result row {}", row);
                }
            }
            boolean jobIdOnly = sawJobId && statusIndex <= 0 && meta.getColumnCount() == 1;
            return interpretSubmitResult(true, jobIdOnly, statusIndex > 0, statuses, true);
        }
    }

    private static int columnIndex(ResultSetMetaData meta, String... names) throws SQLException {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            String label = meta.getColumnLabel(i);
            if (label == null) {
                continue;
            }
            String normalized = label.trim().toLowerCase(Locale.ROOT);
            for (String name : names) {
                if (name.equals(normalized)) {
                    return i;
                }
            }
        }
        return -1;
    }

    static List<String> splitSql(String script, String separator) {
        String sep = StringUtils.isBlank(separator) ? ";" : separator;
        List<String> parts = new ArrayList<>();
        if (script == null) {
            return parts;
        }
        StringBuilder current = new StringBuilder();
        int i = 0;
        while (i < script.length()) {
            if (script.startsWith("--", i)) {
                int newline = script.indexOf('\n', i);
                if (newline < 0) {
                    current.append(script.substring(i));
                    break;
                }
                current.append(script, i, newline + 1);
                i = newline + 1;
                continue;
            }
            if (script.startsWith("/*", i)) {
                int end = script.indexOf("*/", i + 2);
                if (end < 0) {
                    current.append(script.substring(i));
                    break;
                }
                current.append(script, i, end + 2);
                i = end + 2;
                continue;
            }
            if (script.charAt(i) == '\'') {
                current.append('\'');
                i++;
                while (i < script.length()) {
                    char quoted = script.charAt(i);
                    current.append(quoted);
                    i++;
                    if (quoted == '\\' && i < script.length()) {
                        current.append(script.charAt(i));
                        i++;
                        continue;
                    }
                    if (quoted == '\'') {
                        if (i < script.length() && script.charAt(i) == '\'') {
                            current.append('\'');
                            i++;
                            continue;
                        }
                        break;
                    }
                }
                continue;
            }
            if (script.startsWith(sep, i)) {
                parts.add(current.toString());
                current.setLength(0);
                i += sep.length();
                continue;
            }
            current.append(script.charAt(i));
            i++;
        }
        parts.add(current.toString());
        return parts;
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

    enum SubmitOutcome {
        SUCCESS,
        FAILURE,
        NOT_FINISHED
    }
}
