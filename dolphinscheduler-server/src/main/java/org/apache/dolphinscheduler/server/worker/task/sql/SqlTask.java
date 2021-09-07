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

package org.apache.dolphinscheduler.server.worker.task.sql;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.datasource.DatasourceUtil;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.sql.SqlBinds;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.apache.dolphinscheduler.common.task.sql.SqlType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseCommand;
import org.apache.dolphinscheduler.server.entity.SQLTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.utils.UDFUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.apache.commons.collections.MapUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

/**
 * sql task
 */
public class SqlTask extends AbstractTask {

    /**
     * sql parameters
     */
    private SqlParameters sqlParameters;
    /**
     * alert dao
     */
    private AlertDao alertDao;
    /**
     * base datasource
     */
    private BaseConnectionParam baseConnectionParam;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    private AlertClientService alertClientService;

    public SqlTask(TaskExecutionContext taskExecutionContext, Logger logger, AlertClientService alertClientService) {
        super(taskExecutionContext, logger);

        this.taskExecutionContext = taskExecutionContext;

        logger.info("sql task params {}", taskExecutionContext.getTaskParams());
        this.sqlParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SqlParameters.class);

        if (!sqlParameters.checkParameters()) {
            throw new RuntimeException("sql task params is not valid");
        }

        this.alertClientService = alertClientService;
        this.alertDao = SpringApplicationContext.getBean(AlertDao.class);
    }

    @Override
    public void handle() throws Exception {
        // set the name of the current thread
        String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, taskExecutionContext.getTaskAppId());
        Thread.currentThread().setName(threadLoggerInfoName);

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
        try {
            SQLTaskExecutionContext sqlTaskExecutionContext = taskExecutionContext.getSqlTaskExecutionContext();

            // get datasource
            baseConnectionParam = (BaseConnectionParam) DatasourceUtil.buildConnectionParams(
                    DbType.valueOf(sqlParameters.getType()),
                    sqlTaskExecutionContext.getConnectionParams());

            // ready to execute SQL and parameter entity Map
            SqlBinds mainSqlBinds = getSqlAndSqlParamsMap(sqlParameters.getSql());
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

            List<String> createFuncs = UDFUtils.createFuncs(sqlTaskExecutionContext.getUdfFuncTenantCodeMap(),
                    logger);

            // execute sql task
            executeFuncAndSql(mainSqlBinds, preStatementSqlBinds, postStatementSqlBinds, createFuncs);

            setExitStatusCode(Constants.EXIT_CODE_SUCCESS);

        } catch (Exception e) {
            setExitStatusCode(Constants.EXIT_CODE_FAILURE);
            logger.error("sql task error: {}", e.toString());
            throw e;
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

        // combining local and global parameters
        Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext,getParameters());
        if (MapUtils.isEmpty(paramsMap)) {
            paramsMap = new HashMap<>();
        }
        if (MapUtils.isNotEmpty(taskExecutionContext.getParamsMap())) {
            paramsMap.putAll(taskExecutionContext.getParamsMap());
        }

        // spell SQL according to the final user-defined variable
        if (MapUtils.isEmpty(paramsMap)) {
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
        sql = ParameterUtils.replaceScheduleTime(sql, taskExecutionContext.getScheduleTime());
        // special characters need to be escaped, ${} needs to be escaped
        String rgex = "['\"]*\\$\\{(.*?)\\}['\"]*";
        setSqlParamsMap(sql, rgex, sqlParamsMap, paramsMap);
        //Replace the original value in sql ！{...} ，Does not participate in precompilation
        String rgexo = "['\"]*\\!\\{(.*?)\\}['\"]*";
        sql = replaceOriginalValue(sql, rgexo, paramsMap);
        // replace the ${} of the SQL statement with the Placeholder
        String formatSql = sql.replaceAll(rgex, "?");
        sqlBuilder.append(formatSql);

        // print repalce sql
        printReplacedSql(sql, formatSql, rgex, sqlParamsMap);
        return new SqlBinds(sqlBuilder.toString(), sqlParamsMap);
    }

    public String replaceOriginalValue(String content, String rgex, Map<String, Property> sqlParamsMap) {
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

    @Override
    public AbstractParameters getParameters() {
        return this.sqlParameters;
    }

    /**
     * execute function and sql
     *
     * @param mainSqlBinds main sql binds
     * @param preStatementsBinds pre statements binds
     * @param postStatementsBinds post statements binds
     * @param createFuncs create functions
     */
    public void executeFuncAndSql(SqlBinds mainSqlBinds,
                                  List<SqlBinds> preStatementsBinds,
                                  List<SqlBinds> postStatementsBinds,
                                  List<String> createFuncs) throws Exception {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {

            // create connection
            connection = DatasourceUtil.getConnection(DbType.valueOf(sqlParameters.getType()), baseConnectionParam);
            // create temp function
            if (CollectionUtils.isNotEmpty(createFuncs)) {
                createTempFunction(connection, createFuncs);
            }

            // pre sql
            preSql(connection, preStatementsBinds);
            stmt = prepareStatementAndBind(connection, mainSqlBinds);

            String result = null;
            // decide whether to executeQuery or executeUpdate based on sqlType
            if (sqlParameters.getSqlType() == SqlType.QUERY.ordinal()) {
                // query statements need to be convert to JsonArray and inserted into Alert to send
                resultSet = stmt.executeQuery();
                result = resultProcess(resultSet);

            } else if (sqlParameters.getSqlType() == SqlType.NON_QUERY.ordinal()) {
                // non query statement
                String updateResult = String.valueOf(stmt.executeUpdate());
                result = setNonQuerySqlReturn(updateResult, sqlParameters.getLocalParams());
            }
            //deal out params
            sqlParameters.dealOutParam(result);
            postSql(connection, postStatementsBinds);
        } catch (Exception e) {
            logger.error("execute sql error: {}", e.getMessage());
            throw e;
        } finally {
            close(resultSet, stmt, connection);
        }
    }

    public String setNonQuerySqlReturn(String updateResult, List<Property> properties) {
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

            while (rowCount < sqlParameters.getLimit() && resultSet.next()) {
                ObjectNode mapOfColValues = JSONUtils.createObjectNode();
                for (int i = 1; i <= num; i++) {
                    mapOfColValues.set(md.getColumnLabel(i), JSONUtils.toJsonNode(resultSet.getObject(i)));
                }
                resultJSONArray.add(mapOfColValues);
                rowCount++;
            }

            int displayRows = sqlParameters.getDisplayRows() > 0 ? sqlParameters.getDisplayRows() : Constants.DEFAULT_DISPLAY_ROWS;
            displayRows = Math.min(displayRows, resultJSONArray.size());
            logger.info("display sql result {} rows as follows:", displayRows);
            for (int i = 0; i < displayRows; i++) {
                String row = JSONUtils.toJsonString(resultJSONArray.get(i));
                logger.info("row {} : {}", i + 1, row);
            }
        }
        String result = JSONUtils.toJsonString(resultJSONArray);
        if (sqlParameters.getSendEmail() == null || sqlParameters.getSendEmail()) {
            sendAttachment(sqlParameters.getGroupId(), StringUtils.isNotEmpty(sqlParameters.getTitle())
                    ? sqlParameters.getTitle()
                    : taskExecutionContext.getTaskName() + " query result sets", result);
        }
        logger.debug("execute sql result : {}", result);
        return result;
    }

    /**
     * pre sql
     *
     * @param connection connection
     * @param preStatementsBinds preStatementsBinds
     */
    private void preSql(Connection connection,
                        List<SqlBinds> preStatementsBinds) throws Exception {
        for (SqlBinds sqlBind : preStatementsBinds) {
            try (PreparedStatement pstmt = prepareStatementAndBind(connection, sqlBind)) {
                int result = pstmt.executeUpdate();
                logger.info("pre statement execute result: {}, for sql: {}", result, sqlBind.getSql());

            }
        }
    }

    /**
     * post sql
     *
     * @param connection connection
     * @param postStatementsBinds postStatementsBinds
     */
    private void postSql(Connection connection,
                         List<SqlBinds> postStatementsBinds) throws Exception {
        for (SqlBinds sqlBind : postStatementsBinds) {
            try (PreparedStatement pstmt = prepareStatementAndBind(connection, sqlBind)) {
                int result = pstmt.executeUpdate();
                logger.info("post statement execute result: {},for sql: {}", result, sqlBind.getSql());
            }
        }
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
     * @param resultSet resultSet
     * @param pstmt pstmt
     * @param connection connection
     */
    private void close(ResultSet resultSet,
                       PreparedStatement pstmt,
                       Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.error("close result set error : {}", e.getMessage(), e);
            }
        }

        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                logger.error("close prepared statement error : {}", e.getMessage(), e);
            }
        }

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
    private PreparedStatement prepareStatementAndBind(Connection connection, SqlBinds sqlBinds) throws Exception {
        // is the timeout set
        boolean timeoutFlag = taskExecutionContext.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED
                || taskExecutionContext.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED;
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
        logger.info("prepare statement replace sql : {} ", stmt);
        return stmt;
    }

    /**
     * send mail as an attachment
     *
     * @param title title
     * @param content content
     */
    public void sendAttachment(int groupId, String title, String content) {
        AlertSendResponseCommand alertSendResponseCommand = alertClientService.sendAlert(groupId, title, content);
        if (!alertSendResponseCommand.getResStatus()) {
            throw new RuntimeException("send mail failed!");
        }
    }

    /**
     * regular expressions match the contents between two specified strings
     *
     * @param content content
     * @param rgex rgex
     * @param sqlParamsMap sql params map
     * @param paramsPropsMap params props map
     */
    public void setSqlParamsMap(String content, String rgex, Map<Integer, Property> sqlParamsMap, Map<String, Property> paramsPropsMap) {
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(content);
        int index = 1;
        while (m.find()) {

            String paramName = m.group(1);
            Property prop = paramsPropsMap.get(paramName);

            if (prop == null) {
                logger.error("setSqlParamsMap: No Property with paramName: {} is found in paramsPropsMap of task instance"
                        + " with id: {}. So couldn't put Property in sqlParamsMap.", paramName, taskExecutionContext.getTaskInstanceId());
            }
            else {
                sqlParamsMap.put(index, prop);
                index++;
                logger.info("setSqlParamsMap: Property with paramName: {} put in sqlParamsMap of content {} successfully.", paramName, content);
            }

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
    public void printReplacedSql(String content, String formatSql, String rgex, Map<Integer, Property> sqlParamsMap) {
        //parameter print style
        logger.info("after replace sql , preparing : {}", formatSql);
        if (MapUtils.isEmpty(sqlParamsMap)) {
            logger.info("sqlParamsMap should not be Empty");
            return;
        }
        StringBuilder logPrint = new StringBuilder("replaced sql , parameters:");
        if (sqlParamsMap == null) {
            logger.info("printReplacedSql: sqlParamsMap is null.");
        }
        else {
            for (int i = 1; i <= sqlParamsMap.size(); i++) {
                logPrint.append(sqlParamsMap.get(i).getValue() + "(" + sqlParamsMap.get(i).getType() + ")");
            }
        }
        logger.info("Sql Params are {}", logPrint);
    }
}
