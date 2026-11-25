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

package org.apache.dolphinscheduler.plugin.task.sql;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourcePluginManager;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.SQLTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.SqlType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Slf4j
public class SqlTask extends AbstractTask {

    private final TaskExecutionContext taskExecutionContext;

    private final SqlParameters sqlParameters;

    private BaseConnectionParam baseConnectionParam;

    /**
     * create function format
     * include replace here which can be compatible with more cases, for example a long-running Spark session in Kyuubi will keep its own temp functions instead of destroying them right away
     */
    private static final String CREATE_OR_REPLACE_FUNCTION_FORMAT =
            "create or replace temporary function {0} as ''{1}''";

    /**
     * default query sql limit
     */
    private static final int QUERY_LIMIT = 10000;

    /**
     * default max rows kept in the task-result alert content; rows beyond this are replaced by an omission marker,
     * overridable via the sql.task.alert.max.rows property in common.properties
     */
    private static final int DEFAULT_ALERT_MAX_ROWS = 30;

    /**
     * default max length of a single string field value kept in the alert content; longer values are truncated
     * with an ellipsis, overridable via the sql.task.alert.max.row.value.length property in common.properties
     */
    private static final int DEFAULT_ALERT_MAX_ROW_VALUE_LENGTH = 100;

    /**
     * default max serialized UTF-8 bytes of the alert content, kept below the 65535-byte capacity
     * of the MySQL TEXT column that stores t_ds_alert.content, overridable via the
     * sql.task.alert.max.content.bytes property in common.properties
     */
    private static final int DEFAULT_ALERT_MAX_CONTENT_BYTES = 60000;

    private final int alertMaxRows =
            PropertyUtils.getInt(Constants.SQL_TASK_ALERT_MAX_ROWS, DEFAULT_ALERT_MAX_ROWS);

    private final int alertMaxRowValueLength =
            PropertyUtils.getInt(Constants.SQL_TASK_ALERT_MAX_ROW_VALUE_LENGTH, DEFAULT_ALERT_MAX_ROW_VALUE_LENGTH);

    private final int alertMaxContentBytes =
            PropertyUtils.getInt(Constants.SQL_TASK_ALERT_MAX_CONTENT_BYTES, DEFAULT_ALERT_MAX_CONTENT_BYTES);

    private final SQLTaskExecutionContext sqlTaskExecutionContext;

    private final DbType dbType;

    private Connection sessionConnection;
    private Statement sessionStatement;

    public SqlTask(TaskExecutionContext taskRequest) {
        super(taskRequest);
        this.taskExecutionContext = taskRequest;
        this.sqlParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SqlParameters.class);
        log.info("Initialize sql task parameter {}", JSONUtils.toPrettyJsonString(sqlParameters));
        if (sqlParameters == null || !sqlParameters.checkParameters()) {
            throw new TaskException("sql task params is not valid");
        }
        if (this.sqlParameters.getDatasource() == 0) {
            throw new TaskException("unbound test data source");
        }

        sqlTaskExecutionContext =
                sqlParameters.generateExtendedContext(taskExecutionContext.getResourceParametersHelper());
        dbType = DbType.valueOf(sqlParameters.getType());
    }

    @Override
    public AbstractParameters getParameters() {
        return sqlParameters;
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        log.info("Full sql parameters: {}", sqlParameters);
        log.info(
                "sql type : {}, datasource : {}, sql : {} , localParams : {},showType : {},connParams : {},varPool : {} ,query max result limit  {}",
                sqlParameters.getType(),
                sqlParameters.getDatasource(),
                sqlParameters.getSql(),
                sqlParameters.getLocalParams(),
                sqlParameters.getShowType(),
                sqlParameters.getConnParams(),
                sqlParameters.getVarPool(),
                sqlParameters.getLimit());
        try {

            ensureSqlContent();

            // get datasource
            baseConnectionParam = (BaseConnectionParam) DataSourceUtils.buildConnectionParams(dbType,
                    sqlTaskExecutionContext.getConnectionParams());
            List<String> subSqls = DataSourcePluginManager.getDataSourceProcessor(dbType)
                    .splitAndRemoveComment(sqlParameters.getSql());

            // ready to execute SQL and parameter entity Map
            List<SqlBinds> mainStatementSqlBinds = subSqls
                    .stream()
                    .map(this::getSqlAndSqlParamsMap)
                    .collect(Collectors.toList());

            List<SqlBinds> preStatementSqlBinds = Optional.ofNullable(sqlParameters.getPreStatements())
                    .orElse(new ArrayList<>())
                    .stream()
                    .map(this::getSqlAndSqlParamsMap)
                    .collect(Collectors.toList());
            List<SqlBinds> postStatementSqlBinds = Optional.ofNullable(sqlParameters.getPostStatements())
                    .orElse(new ArrayList<>())
                    .stream()
                    .map(this::getSqlAndSqlParamsMap)
                    .collect(Collectors.toList());

            // execute sql task
            executeFuncAndSql(mainStatementSqlBinds, preStatementSqlBinds, postStatementSqlBinds);

            setExitStatusCode(TaskConstants.EXIT_CODE_SUCCESS);

        } catch (Exception e) {
            if (exitStatusCode == TaskConstants.EXIT_CODE_KILL) {
                log.info("sql task has been killed");
                return;
            }
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            log.error("sql task error", e);
            throw new TaskException("Execute sql task failed", e);
        }
    }

    @Override
    public void cancel() throws TaskException {
        try {
            if (sessionStatement != null) {
                sessionStatement.cancel();
            }
            exitStatusCode = TaskConstants.EXIT_CODE_KILL;
        } catch (Exception e) {
            throw new TaskException("Cancel sql task failed", e);
        }
    }

    /**
     * execute function and sql
     *
     * @param mainStatementsBinds main statements binds
     * @param preStatementsBinds  pre statements binds
     * @param postStatementsBinds post statements binds
     */
    public void executeFuncAndSql(List<SqlBinds> mainStatementsBinds,
                                  List<SqlBinds> preStatementsBinds,
                                  List<SqlBinds> postStatementsBinds) throws Exception {
        try (
                Connection connection =
                        DataSourceClientProvider.getAdHocConnection(DbType.valueOf(sqlParameters.getType()),
                                baseConnectionParam)) {
            sessionConnection = connection;
            // pre execute
            executeUpdate(connection, preStatementsBinds, "pre");

            // main execute
            String result = null;
            // decide whether to executeQuery or executeUpdate based on sqlType
            if (sqlParameters.getSqlType() == SqlType.QUERY.ordinal()) {
                // query statements need to be convert to JsonArray and inserted into Alert to send
                result = executeQuery(connection, mainStatementsBinds.get(0), "main");
            } else if (sqlParameters.getSqlType() == SqlType.NON_QUERY.ordinal()) {
                // non query statement
                String updateResult = executeUpdate(connection, mainStatementsBinds, "main");
                result = setNonQuerySqlReturn(updateResult, sqlParameters.getLocalParams());
            }
            // deal out params
            sqlParameters.dealOutParam(result);

            // post execute
            executeUpdate(connection, postStatementsBinds, "post");

            // set varPool
            taskExecutionContext.setVarPool(sqlParameters.getVarPool());
        } catch (Exception e) {
            log.error("execute sql error: {}", e.getMessage());
            throw e;
        }
    }

    private String setNonQuerySqlReturn(String updateResult, List<Property> properties) {
        String result = null;
        for (Property info : properties) {
            if (Direct.OUT == info.getDirect()) {
                List<Map<String, String>> updateRL = new ArrayList<>();
                Map<String, String> updateRM = new HashMap<>();
                updateRM.put(info.getProp(), updateResult);
                updateRL.add(updateRM);
                result = JSONUtils.toJsonString(updateRL);
                break;
            }
        }
        return result;
    }

    /**
     * result process
     *
     * @param resultSet resultSet
     * @throws Exception Exception
     */
    private String resultProcess(ResultSet resultSet) throws Exception {
        ArrayNode resultJSONArray = JSONUtils.createArrayNode();
        if (resultSet != null) {
            ResultSetMetaData md = resultSet.getMetaData();
            int num = md.getColumnCount();
            String[] columnLabels = new String[num];

            // Check for duplicates in column definitions (across all columns)
            Set<String> uniqueLabels = new HashSet<>(num);
            for (int i = 1; i <= num; i++) {
                String label = md.getColumnLabel(i);
                columnLabels[i - 1] = label;
                if (!uniqueLabels.add(label)) {
                    throw new TaskException("SQL column name conflict: duplicate column name '" + label
                            + "'. Please use aliases to ensure unique column names.");
                }
            }

            int displayRows = sqlParameters.getDisplayRows() > 0 ? sqlParameters.getDisplayRows()
                    : TaskConstants.DEFAULT_DISPLAY_ROWS;
            log.info("display the first {} rows of sql result as follows:", displayRows);
            while (resultSet.next()) {
                ObjectNode mapOfColValues = JSONUtils.createObjectNode();
                for (int i = 1; i <= num; i++) {
                    mapOfColValues.set(columnLabels[i - 1], JSONUtils.toJsonNode(resultSet.getObject(i)));
                }
                resultJSONArray.add(mapOfColValues);
                if (resultJSONArray.size() <= displayRows) {
                    log.info("row {} : {}", resultJSONArray.size(), JSONUtils.toJsonString(mapOfColValues));
                }
            }
            log.info("displayed {} of {} rows", Math.min(displayRows, resultJSONArray.size()),
                    resultJSONArray.size());
        }

        String result;
        ArrayNode alertArray = resultJSONArray;
        if (resultJSONArray.isEmpty()) {
            ArrayNode emptyRow = generateEmptyRow(resultSet);
            result = JSONUtils.toJsonString(emptyRow);
            alertArray = emptyRow;
        } else {
            result = JSONUtils.toJsonString(resultJSONArray);
        }

        if (Boolean.TRUE.equals(sqlParameters.getSendEmail())) {
            prepareTaskResultAlert(alertArray);
        }
        log.debug("execute sql result : {}", result);
        return result;
    }

    /**
     * generate empty Results as ArrayNode
     */
    private ArrayNode generateEmptyRow(ResultSet resultSet) throws SQLException {
        ArrayNode resultJSONArray = JSONUtils.createArrayNode();
        ObjectNode emptyOfColValues = JSONUtils.createObjectNode();
        if (resultSet != null) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnsNum = metaData.getColumnCount();
            log.info("sql query results is empty");
            for (int i = 1; i <= columnsNum; i++) {
                emptyOfColValues.set(metaData.getColumnLabel(i), JSONUtils.toJsonNode(""));
            }
        } else {
            emptyOfColValues.set("error", JSONUtils.toJsonNode("resultSet is null"));
        }
        resultJSONArray.add(emptyOfColValues);
        return resultJSONArray;
    }

    /**
     * Prepare task result alert info with the complete query result as content.
     */
    private void prepareTaskResultAlert(ArrayNode resultJSONArray) {
        TaskAlertInfo taskAlertInfo = new TaskAlertInfo();
        taskAlertInfo.setAlertGroupId(sqlParameters.getGroupId());
        taskAlertInfo.setTitle(StringUtils.isNotEmpty(sqlParameters.getTitle())
                ? sqlParameters.getTitle()
                : taskExecutionContext.getTaskName() + " query result sets");
        taskAlertInfo.setContent(limitAlertContent(resultJSONArray));
        taskAlertInfo.setAlertType(AlertType.TASK_RESULT);

        taskExecutionContext.setNeedAlert(true);
        taskExecutionContext.setTaskAlertInfo(taskAlertInfo);
        log.debug("Prepare task result alert: title={}, alertGroupId={}, alertType={}, totalRows={}",
                taskAlertInfo.getTitle(), taskAlertInfo.getAlertGroupId(), taskAlertInfo.getAlertType(),
                resultJSONArray.size());
    }

    /**
     * Build a size-limited alert content that stays a valid JSON array.
     * At most the configured max rows are kept; string field values longer than the configured
     * max value length are truncated with an ellipsis. The serialized content is bounded by
     * the configured max UTF-8 bytes: rows are dropped from the tail until it fits; a row that
     * alone exceeds the budget is replaced by the omission marker.
     */
    private String limitAlertContent(ArrayNode resultJSONArray) {
        int totalRows = resultJSONArray.size();

        // Serialize each kept row once and track the running UTF-8 byte size, so the budget
        // check on every dropped row is O(1) instead of rebuilding and re-encoding the whole
        // content (which would turn the drop loop into O(rows^2)).
        List<String> serializedRows = new ArrayList<>(Math.min(totalRows, alertMaxRows));
        int rowsBytes = 0;
        for (int i = 0; i < totalRows && i < alertMaxRows; i++) {
            String rowJson = JSONUtils.toJsonString(limitRowValues(resultJSONArray.get(i)));
            serializedRows.add(rowJson);
            rowsBytes += rowJson.getBytes(StandardCharsets.UTF_8).length;
        }

        int omittedRows = totalRows - serializedRows.size();
        while (!serializedRows.isEmpty()
                && contentBytes(rowsBytes, serializedRows.size(), omittedRows) > alertMaxContentBytes) {
            int last = serializedRows.size() - 1;
            rowsBytes -= serializedRows.remove(last).getBytes(StandardCharsets.UTF_8).length;
            omittedRows++;
        }
        return buildAlertContent(serializedRows, omittedRows);
    }

    /**
     * UTF-8 byte size of the alert content that buildAlertContent produces from the kept rows
     * and the omission marker, i.e. "[" + rows joined by "," + ("," + marker)? + "]".
     */
    private int contentBytes(int rowsBytes, int rowCount, int omittedRows) {
        int bytes = 2; // '[' and ']'
        if (rowCount > 0) {
            bytes += rowsBytes + rowCount - 1; // the rows plus their ',' separators
        }
        if (omittedRows > 0) {
            bytes += (rowCount > 0 ? 1 : 0)
                    + JSONUtils.toJsonString(createOmissionMarker(omittedRows)).getBytes(StandardCharsets.UTF_8).length;
        }
        return bytes;
    }

    /**
     * Assemble the alert content from the kept rows and the omission marker,
     * i.e. "[" + rows joined by "," + ("," + marker)? + "]".
     */
    private String buildAlertContent(List<String> serializedRows, int omittedRows) {
        StringBuilder content = new StringBuilder();
        content.append('[');
        for (int i = 0; i < serializedRows.size(); i++) {
            if (i > 0) {
                content.append(',');
            }
            content.append(serializedRows.get(i));
        }
        if (omittedRows > 0) {
            if (!serializedRows.isEmpty()) {
                content.append(',');
            }
            content.append(JSONUtils.toJsonString(createOmissionMarker(omittedRows)));
        }
        content.append(']');
        return content.toString();
    }

    private ObjectNode createOmissionMarker(int omittedRows) {
        ObjectNode omitted = JSONUtils.createObjectNode();
        omitted.put("...", omittedRows + " more rows omitted");
        return omitted;
    }

    private JsonNode limitRowValues(JsonNode row) {
        if (!row.isObject()) {
            return row;
        }
        ObjectNode limitedRow = JSONUtils.createObjectNode();
        Iterator<Map.Entry<String, JsonNode>> fields = row.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode value = entry.getValue();
            if (value.isTextual() && value.textValue().length() > alertMaxRowValueLength) {
                limitedRow.put(entry.getKey(),
                        truncateAtCodePointBoundary(value.textValue(), alertMaxRowValueLength) + "...");
            } else {
                limitedRow.set(entry.getKey(), value);
            }
        }
        return limitedRow;
    }

    /**
     * Cut a string to at most maxChars characters without splitting a surrogate pair.
     */
    private String truncateAtCodePointBoundary(String text, int maxChars) {
        if (text.codePointCount(0, text.length()) <= maxChars) {
            return text;
        }
        return text.substring(0, text.offsetByCodePoints(0, maxChars));
    }

    private String executeQuery(Connection connection, SqlBinds sqlBinds, String handlerType) throws Exception {
        try (PreparedStatement statement = prepareStatementAndBind(connection, sqlBinds)) {
            log.info("{} statement execute query, for sql: {}", handlerType, sqlBinds.getSql());
            ResultSet resultSet = statement.executeQuery();
            return resultProcess(resultSet);
        }
    }

    private String executeUpdate(Connection connection, List<SqlBinds> statementsBinds,
                                 String handlerType) throws Exception {
        int result = 0;
        for (SqlBinds sqlBind : statementsBinds) {
            try (PreparedStatement tmpStatement = prepareStatementAndBind(connection, sqlBind)) {
                result = tmpStatement.executeUpdate();
                log.info("{} statement execute update result: {}, for sql: {}", handlerType, result,
                        sqlBind.getSql());
            }
        }
        return String.valueOf(result);
    }

    /**
     * preparedStatement bind
     *
     * @param connection connection
     * @param sqlBinds   sqlBinds
     * @return PreparedStatement
     * @throws Exception Exception
     */
    private PreparedStatement prepareStatementAndBind(Connection connection, SqlBinds sqlBinds) {
        // is the timeout set
        // todo: we need control the timeout at master side.
        boolean timeoutFlag = taskExecutionContext.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED
                || taskExecutionContext.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED;
        try {
            PreparedStatement stmt = connection.prepareStatement(sqlBinds.getSql());
            if (timeoutFlag) {
                stmt.setQueryTimeout(taskExecutionContext.getTaskTimeout());
            }
            stmt.setMaxRows(sqlParameters.getLimit() <= 0 ? QUERY_LIMIT : sqlParameters.getLimit());
            Map<Integer, Property> params = sqlBinds.getParamsMap();
            if (params != null) {
                for (Map.Entry<Integer, Property> entry : params.entrySet()) {
                    Property prop = entry.getValue();
                    ParameterUtils.setInParameter(entry.getKey(), stmt, prop.getType(), prop.getValue());
                }
            }
            log.info("prepare statement replace sql : {}, sql parameters : {}", sqlBinds.getSql(),
                    sqlBinds.getParamsMap());
            sessionStatement = stmt;
            return stmt;
        } catch (Exception exception) {
            throw new TaskException("SQL task prepareStatementAndBind error", exception);
        }
    }

    /**
     * print replace sql
     *
     * @param content      content
     * @param formatSql    format sql
     * @param rgex         rgex
     * @param sqlParamsMap sql params map
     */
    private void printReplacedSql(String content, String formatSql, String rgex, Map<Integer, Property> sqlParamsMap) {
        // parameter print style
        log.info("after replace sql , preparing : {}", formatSql);
        StringBuilder logPrint = new StringBuilder("replaced sql , parameters:");
        if (sqlParamsMap == null) {
            log.info("printReplacedSql: sqlParamsMap is null.");
        } else {
            for (int i = 1; i <= sqlParamsMap.size(); i++) {
                logPrint.append(sqlParamsMap.get(i).getValue()).append("(").append(sqlParamsMap.get(i).getType())
                        .append(")");
            }
        }
        log.info("Sql Params are {}", logPrint);
    }

    private void ensureSqlContent() {
        if (StringUtils.isNotEmpty(sqlParameters.getSql())) {
            return;
        }
        if (StringUtils.isEmpty(sqlParameters.getSqlResource())) {
            return;
        }
        String resourcePathInStorage = sqlParameters.getSqlResource();
        try {
            ResourceContext resourceContext = taskExecutionContext.getResourceContext();
            ResourceContext.ResourceItem resourceItem =
                    resourceContext.getResourceItem(resourcePathInStorage);
            String localPath = resourceItem.getResourceAbsolutePathInLocal();
            log.info("Load sql content from resource file: {}", resourcePathInStorage);
            String sqlContent = new String(
                    Files.readAllBytes(Paths.get(localPath)),
                    StandardCharsets.UTF_8);
            sqlParameters.setSql(sqlContent);
        } catch (IOException e) {
            log.error("Read sql content from resource file {} error", resourcePathInStorage, e);
            throw new TaskException(
                    String.format("Read sql content from resource file %s error", resourcePathInStorage),
                    e);
        }
    }

    /**
     * ready to execute SQL and parameter entity Map
     *
     * @return SqlBinds
     */
    private SqlBinds getSqlAndSqlParamsMap(String sql) {
        Map<Integer, Property> sqlParamsMap = new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder();
        // new
        // replace variable TIME with $[YYYYmmddd...] in sql when history run job and batch complement job
        sql = ParameterUtils.replaceScheduleTime(sql,
                DateUtils.timeStampToDate(taskExecutionContext.getScheduleTime()));

        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();

        Map<String, String> placeholderParamsMap = paramsMap == null
                ? Collections.emptyMap()
                : ParameterUtils.convert(paramsMap);

        if (StringUtils.isNotEmpty(sqlParameters.getTitle())) {
            String title = ParameterUtils.convertParameterPlaceholders(sqlParameters.getTitle(), placeholderParamsMap);
            log.info("SQL title : {}", title);
            sqlParameters.setTitle(title);
        }

        // spell SQL according to the final user-defined variable
        if (paramsMap == null) {
            sqlBuilder.append(sql);
            return new SqlBinds(sqlBuilder.toString(), sqlParamsMap);
        }

        // special characters need to be escaped, ${} needs to be escaped
        setSqlParamsMap(sql, sqlParamsMap, paramsMap, taskExecutionContext.getTaskInstanceId());
        // Replace the original value in sql ！{...} ，Does not participate in precompilation
        String rgexo = "['\"]*\\!\\{(.*?)\\}['\"]*";
        sql = replaceOriginalValue(sql, rgexo, paramsMap);
        // replace the ${} of the SQL statement with the Placeholder
        // Convert the list parameter
        String formatSql = ParameterUtils.expandListParameter(sqlParamsMap, sql);
        sqlBuilder.append(formatSql);
        // print replace sql
        printReplacedSql(sql, formatSql, TaskConstants.SQL_PARAMS_REGEX, sqlParamsMap);
        return new SqlBinds(sqlBuilder.toString(), sqlParamsMap);
    }

    private String replaceOriginalValue(String content, String rgex, Map<String, Property> sqlParamsMap) {
        Pattern pattern = Pattern.compile(rgex);
        while (true) {
            Matcher m = pattern.matcher(content);
            if (!m.find()) {
                break;
            }
            String paramName = m.group(1);
            String paramValue = sqlParamsMap.get(paramName).getValue();
            content = m.replaceFirst(paramValue);
        }
        return content;
    }

}
