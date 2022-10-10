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

import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.*;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.SqlType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.UdfFuncParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.DateUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SqlTask extends AbstractTask {

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * sql parameters
     */
    private SqlParameters sqlParameters;

    /**
     * base datasource
     */
    private BaseConnectionParam baseConnectionParam;

    /**
     * create function format
     * include replace here which can be compatible with more cases, for example a long-running Spark session in Kyuubi will keep its own temp functions instead of destroying them right away
     */
    private static final String CREATE_OR_REPLACE_FUNCTION_FORMAT = "create or replace temporary function {0} as ''{1}''";

    /**
     * default query sql limit
     */
    private static final int QUERY_LIMIT = 10000;

    private SQLTaskExecutionContext sqlTaskExecutionContext;

    public static final int TEST_FLAG_YES = 1;

    private static final String SQL_SEPARATOR = ";\n";

    /**
     * Abstract Yarn Task
     *
     * @param taskRequest taskRequest
     */
    public SqlTask(TaskExecutionContext taskRequest) {
        super(taskRequest);
        this.taskExecutionContext = taskRequest;
        this.sqlParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SqlParameters.class);

        assert sqlParameters != null;
        if (taskExecutionContext.getTestFlag() == TEST_FLAG_YES && this.sqlParameters.getDatasource() == 0) {
            throw new RuntimeException("unbound test data source");
        }
        if (!sqlParameters.checkParameters()) {
            throw new RuntimeException("sql task params is not valid");
        }

        sqlTaskExecutionContext = sqlParameters.generateExtendedContext(taskExecutionContext.getResourceParametersHelper());
    }

    @Override
    public AbstractParameters getParameters() {
        return sqlParameters;
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        logger.info("Full sql parameters: {}", sqlParameters);
        logger.info("sql type : {}, datasource : {}, sql : {} , localParams : {},udfs : {},showType : {},connParams : {},varPool : {} ,query max result limit  {}",
                sqlParameters.getType(),
                sqlParameters.getDatasource(),
                sqlParameters.getSql(),
                sqlParameters.getLocalParams(),
                sqlParameters.getUdfs(),
                sqlParameters.getShowType(),
                sqlParameters.getConnParams(),
                sqlParameters.getVarPool(),
                sqlParameters.getLimit());
        String separator = SQL_SEPARATOR;
        try {

            // get datasource
            baseConnectionParam = (BaseConnectionParam) DataSourceUtils.buildConnectionParams(
                    DbType.valueOf(sqlParameters.getType()),
                    sqlTaskExecutionContext.getConnectionParams());
            if (DbType.valueOf(sqlParameters.getType()).isSupportMultipleStatement()) {
                separator = "";
            }
            // ready to execute SQL and parameter entity Map
            List<SqlBinds> mainStatementSqlBinds = split(sqlParameters.getSql(), separator)
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

            List<String> createFuncs = createFuncs(sqlTaskExecutionContext.getUdfFuncParametersList(), logger);

            // execute sql task
            executeFuncAndSql(mainStatementSqlBinds, preStatementSqlBinds, postStatementSqlBinds, createFuncs);

            setExitStatusCode(TaskConstants.EXIT_CODE_SUCCESS);

        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            logger.error("sql task error", e);
            throw new TaskException("Execute sql task failed", e);
        }
    }

    @Override
    public void cancel() throws TaskException {

    }

    /**
     * split sql by segment separator
     * <p>The segment separator is used
     * when the data source does not support multi-segment SQL execution,
     * and the client needs to split the SQL and execute it multiple times.</p>
     * @param sql
     * @param segmentSeparator
     * @return
     */
    public static List<String> split(String sql, String segmentSeparator) {
        if (StringUtils.isEmpty(segmentSeparator)) {
            return Collections.singletonList(sql);
        }

        String[] lines = sql.split(segmentSeparator);
        List<String> segments = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("--")) {
                continue;
            }
            segments.add(line);
        }
        return segments;
    }

    /**
     * execute function and sql
     *
     * @param mainStatementsBinds main statements binds
     * @param preStatementsBinds pre statements binds
     * @param postStatementsBinds post statements binds
     * @param createFuncs create functions
     */
    public void executeFuncAndSql(List<SqlBinds> mainStatementsBinds,
                                  List<SqlBinds> preStatementsBinds,
                                  List<SqlBinds> postStatementsBinds,
                                  List<String> createFuncs) throws Exception {
        Connection connection = null;
        try {

            // create connection
            connection = DataSourceClientProvider.getInstance().getConnection(DbType.valueOf(sqlParameters.getType()), baseConnectionParam);
            // create temp function
            if (CollectionUtils.isNotEmpty(createFuncs)) {
                createTempFunction(connection, createFuncs);
            }

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
            //deal out params
            sqlParameters.dealOutParam(result);

            // post execute
            executeUpdate(connection, postStatementsBinds, "post");
        } catch (Exception e) {
            logger.error("execute sql error: {}", e.getMessage());
            throw e;
        } finally {
            close(connection);
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

            int rowCount = 0;
            int limit = sqlParameters.getLimit() == 0 ? QUERY_LIMIT : sqlParameters.getLimit();

            while (resultSet.next()) {
                if (rowCount == limit) {
                    logger.info("sql result limit : {} exceeding results are filtered", limit);
                    break;
                }
                ObjectNode mapOfColValues = JSONUtils.createObjectNode();
                for (int i = 1; i <= num; i++) {
                    mapOfColValues.set(md.getColumnLabel(i), JSONUtils.toJsonNode(resultSet.getObject(i)));
                }
                resultJSONArray.add(mapOfColValues);
                rowCount++;
            }

            int displayRows = sqlParameters.getDisplayRows() > 0 ? sqlParameters.getDisplayRows() : TaskConstants.DEFAULT_DISPLAY_ROWS;
            displayRows = Math.min(displayRows, rowCount);
            logger.info("display sql result {} rows as follows:", displayRows);
            for (int i = 0; i < displayRows; i++) {
                String row = JSONUtils.toJsonString(resultJSONArray.get(i));
                logger.info("row {} : {}", i + 1, row);
            }
        }

        String result = resultJSONArray.isEmpty() ?
            JSONUtils.toJsonString(generateEmptyRow(resultSet)) : JSONUtils.toJsonString(resultJSONArray);

        if (sqlParameters.getSendEmail() == null || sqlParameters.getSendEmail()) {
            sendAttachment(sqlParameters.getGroupId(), StringUtils.isNotEmpty(sqlParameters.getTitle())
                    ? sqlParameters.getTitle()
                    : taskExecutionContext.getTaskName() + " query result sets", result);
        }
        logger.debug("execute sql result : {}", result);
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
            logger.info("sql query results is empty");
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
     * send alert as an attachment
     *
     * @param title title
     * @param content content
     */
    private void sendAttachment(int groupId, String title, String content) {
        setNeedAlert(Boolean.TRUE);
        TaskAlertInfo taskAlertInfo = new TaskAlertInfo();
        taskAlertInfo.setAlertGroupId(groupId);
        taskAlertInfo.setContent(content);
        taskAlertInfo.setTitle(title);
        setTaskAlertInfo(taskAlertInfo);
    }

    private String executeQuery(Connection connection, SqlBinds sqlBinds, String handlerType) throws Exception {
        try (PreparedStatement statement = prepareStatementAndBind(connection, sqlBinds)) {
            logger.info("{} statement execute query, for sql: {}", handlerType, sqlBinds.getSql());
            ResultSet resultSet = statement.executeQuery();
            return resultProcess(resultSet);
        }
    }

    private String executeUpdate(Connection connection, List<SqlBinds> statementsBinds, String handlerType) throws Exception {
        int result = 0;
        for (SqlBinds sqlBind : statementsBinds) {
            try (PreparedStatement statement = prepareStatementAndBind(connection, sqlBind)) {
                result = statement.executeUpdate();
                logger.info("{} statement execute update result: {}, for sql: {}", handlerType, result, sqlBind.getSql());
            }
        }
        return String.valueOf(result);
    }

    /**
     * create temp function
     *
     * @param connection connection
     * @param createFuncs createFuncs
     */
    private void createTempFunction(Connection connection,
                                    List<String> createFuncs) throws Exception {
        try (Statement funcStmt = connection.createStatement()) {
            for (String createFunc : createFuncs) {
                logger.info("hive create function sql: {}", createFunc);
                funcStmt.execute(createFunc);
            }
        }
    }

    /**
     * close jdbc resource
     *
     * @param connection connection
     */
    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("close connection error : {}", e.getMessage(), e);
            }
        }
    }

    /**
     * preparedStatement bind
     *
     * @param connection connection
     * @param sqlBinds sqlBinds
     * @return PreparedStatement
     * @throws Exception Exception
     */
    private PreparedStatement prepareStatementAndBind(Connection connection, SqlBinds sqlBinds) {
        // is the timeout set
        boolean timeoutFlag = taskExecutionContext.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED
                || taskExecutionContext.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED;
        try {
            PreparedStatement stmt = connection.prepareStatement(sqlBinds.getSql());
            if (timeoutFlag) {
                stmt.setQueryTimeout(taskExecutionContext.getTaskTimeout());
            }
            Map<Integer, Property> params = sqlBinds.getParamsMap();
            if (params != null) {
                for (Map.Entry<Integer, Property> entry : params.entrySet()) {
                    Property prop = entry.getValue();
                    ParameterUtils.setInParameter(entry.getKey(), stmt, prop.getType(), prop.getValue());
                }
            }
            logger.info("prepare statement replace sql : {}, sql parameters : {}", sqlBinds.getSql(), sqlBinds.getParamsMap());
            return stmt;
        } catch (Exception exception) {
            throw new TaskException("SQL task prepareStatementAndBind error", exception);
        }
    }

    /**
     * print replace sql
     *
     * @param content content
     * @param formatSql format sql
     * @param rgex rgex
     * @param sqlParamsMap sql params map
     */
    private void printReplacedSql(String content, String formatSql, String rgex, Map<Integer, Property> sqlParamsMap) {
        //parameter print style
        logger.info("after replace sql , preparing : {}", formatSql);
        StringBuilder logPrint = new StringBuilder("replaced sql , parameters:");
        if (sqlParamsMap == null) {
            logger.info("printReplacedSql: sqlParamsMap is null.");
        } else {
            for (int i = 1; i <= sqlParamsMap.size(); i++) {
                logPrint.append(sqlParamsMap.get(i).getValue()).append("(").append(sqlParamsMap.get(i).getType()).append(")");
            }
        }
        logger.info("Sql Params are {}", logPrint);
    }

    /**
     * ready to execute SQL and parameter entity Map
     *
     * @return SqlBinds
     */
    private SqlBinds getSqlAndSqlParamsMap(String sql) {
        Map<Integer, Property> sqlParamsMap = new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder();

        // combining local and global parameters
        Map<String, Property> paramsMap = taskExecutionContext.getPrepareParamsMap();

        // spell SQL according to the final user-defined variable
        if (paramsMap == null) {
            sqlBuilder.append(sql);
            return new SqlBinds(sqlBuilder.toString(), sqlParamsMap);
        }

        if (StringUtils.isNotEmpty(sqlParameters.getTitle())) {
            String title = ParameterUtils.convertParameterPlaceholders(sqlParameters.getTitle(),
                    ParamUtils.convert(paramsMap));
            logger.info("SQL title : {}", title);
            sqlParameters.setTitle(title);
        }

        //new
        //replace variable TIME with $[YYYYmmddd...] in sql when history run job and batch complement job
        sql = ParameterUtils.replaceScheduleTime(sql, DateUtils.timeStampToDate(taskExecutionContext.getScheduleTime()));
        // special characters need to be escaped, ${} needs to be escaped
        setSqlParamsMap(sql, rgex, sqlParamsMap, paramsMap,taskExecutionContext.getTaskInstanceId());
        //Replace the original value in sql ！{...} ，Does not participate in precompilation
        String rgexo = "['\"]*\\!\\{(.*?)\\}['\"]*";
        sql = replaceOriginalValue(sql, rgexo, paramsMap);
        // replace the ${} of the SQL statement with the Placeholder
        String formatSql = sql.replaceAll(rgex, "?");
        // Convert the list parameter
        formatSql = ParameterUtils.expandListParameter(sqlParamsMap, formatSql);
        sqlBuilder.append(formatSql);
        // print replace sql
        printReplacedSql(sql, formatSql, rgex, sqlParamsMap);
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

    /**
     * create function list
     *
     * @param udfFuncParameters udfFuncParameters
     * @param logger logger
     * @return
     */
    private List<String> createFuncs(List<UdfFuncParameters> udfFuncParameters, Logger logger) {

        if (CollectionUtils.isEmpty(udfFuncParameters)) {
            logger.info("can't find udf function resource");
            return null;
        }
        // build jar sql
        List<String> funcList = buildJarSql(udfFuncParameters);

        // build temp function sql
        List<String> tempFuncList = buildTempFuncSql(udfFuncParameters);
        funcList.addAll(tempFuncList);
        return funcList;
    }

    /**
     * build temp function sql
     * @param udfFuncParameters udfFuncParameters
     * @return
     */
    private List<String> buildTempFuncSql(List<UdfFuncParameters> udfFuncParameters) {
        return udfFuncParameters.stream().map(value -> MessageFormat
                .format(CREATE_OR_REPLACE_FUNCTION_FORMAT, value.getFuncName(), value.getClassName())).collect(Collectors.toList());
    }

    /**
     * build jar sql
     * @param udfFuncParameters udfFuncParameters
     * @return
     */
    private List<String> buildJarSql(List<UdfFuncParameters> udfFuncParameters) {
        return udfFuncParameters.stream().map(value -> {
            String defaultFS = value.getDefaultFS();
            String prefixPath = defaultFS.startsWith("file://") ? "file://" : defaultFS;
            String uploadPath = CommonUtils.getHdfsUdfDir(value.getTenantCode());
            String resourceFullName = value.getResourceName();
            resourceFullName = resourceFullName.startsWith("/") ? resourceFullName : String.format("/%s", resourceFullName);
            return String.format("add jar %s%s%s", prefixPath, uploadPath, resourceFullName);
        }).collect(Collectors.toList());
    }

}
