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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.alert.utils.MailUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.ShowType;
import org.apache.dolphinscheduler.common.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.sql.SqlBinds;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.apache.dolphinscheduler.common.task.sql.SqlType;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.server.entity.SQLTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.utils.UDFUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.slf4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.dolphinscheduler.common.Constants.*;
import static org.apache.dolphinscheduler.common.enums.DbType.HIVE;
/**
 * sql task
 */
public class SqlTask extends AbstractTask {

    /**
     *  sql parameters
     */
    private SqlParameters sqlParameters;
    /**
     *  alert dao
     */
    private AlertDao alertDao;
    /**
     * base datasource
     */
    private BaseDataSource baseDataSource;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * default query sql limit
     */
    private static final int LIMIT = 10000;

    public SqlTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);

        this.taskExecutionContext = taskExecutionContext;

        logger.info("sql task params {}", taskExecutionContext.getTaskParams());
        this.sqlParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SqlParameters.class);

        if (!sqlParameters.checkParameters()) {
            throw new RuntimeException("sql task params is not valid");
        }

        this.alertDao = SpringApplicationContext.getBean(AlertDao.class);
    }

    @Override
    public void handle() throws Exception {
        // set the name of the current thread
        String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, taskExecutionContext.getTaskAppId());
        Thread.currentThread().setName(threadLoggerInfoName);

        logger.info("Full sql parameters: {}", sqlParameters);
        logger.info("sql type : {}, datasource : {}, sql : {} , localParams : {},udfs : {},showType : {},connParams : {}",
                sqlParameters.getType(),
                sqlParameters.getDatasource(),
                sqlParameters.getSql(),
                sqlParameters.getLocalParams(),
                sqlParameters.getUdfs(),
                sqlParameters.getShowType(),
                sqlParameters.getConnParams());
        try {
            SQLTaskExecutionContext sqlTaskExecutionContext = taskExecutionContext.getSqlTaskExecutionContext();
            // load class
            DataSourceFactory.loadClass(DbType.valueOf(sqlParameters.getType()));

            // get datasource
            baseDataSource = DataSourceFactory.getDatasource(DbType.valueOf(sqlParameters.getType()),
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
            logger.error("sql task error", e);
            throw e;
        }
    }

    /**
     * ready to execute SQL and parameter entity Map
     * @return SqlBinds
     */
    private SqlBinds getSqlAndSqlParamsMap(String sql) {
        Map<Integer,Property> sqlParamsMap =  new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder();

        // find process instance by task id


        Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
                taskExecutionContext.getDefinedParams(),
                sqlParameters.getLocalParametersMap(),
                CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
                taskExecutionContext.getScheduleTime());

        // spell SQL according to the final user-defined variable
        if(paramsMap == null){
            sqlBuilder.append(sql);
            return new SqlBinds(sqlBuilder.toString(), sqlParamsMap);
        }

        if (StringUtils.isNotEmpty(sqlParameters.getTitle())){
            String title = ParameterUtils.convertParameterPlaceholders(sqlParameters.getTitle(),
                    ParamUtils.convert(paramsMap));
            logger.info("SQL title : {}",title);
            sqlParameters.setTitle(title);
        }
        
        //new
        //replace variable TIME with $[YYYYmmddd...] in sql when history run job and batch complement job
        sql = ParameterUtils.replaceScheduleTime(sql, taskExecutionContext.getScheduleTime());
        // special characters need to be escaped, ${} needs to be escaped
        String rgex = "['\"]*\\$\\{(.*?)\\}['\"]*";
        setSqlParamsMap(sql, rgex, sqlParamsMap, paramsMap);

        // replace the ${} of the SQL statement with the Placeholder
        String formatSql = sql.replaceAll(rgex, "?");
        sqlBuilder.append(formatSql);

        // print repalce sql
        printReplacedSql(sql, formatSql, rgex, sqlParamsMap);
        return new SqlBinds(sqlBuilder.toString(), sqlParamsMap);
    }

    @Override
    public AbstractParameters getParameters() {
        return this.sqlParameters;
    }

    /**
     * execute function and sql
     * @param mainSqlBinds          main sql binds
     * @param preStatementsBinds    pre statements binds
     * @param postStatementsBinds   post statements binds
     * @param createFuncs           create functions
     */
    public void executeFuncAndSql(SqlBinds mainSqlBinds,
                                        List<SqlBinds> preStatementsBinds,
                                        List<SqlBinds> postStatementsBinds,
                                        List<String> createFuncs){
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            // if upload resource is HDFS and kerberos startup
            CommonUtils.loadKerberosConf();
            // create connection
            connection = createConnection();
            // create temp function
            if (CollectionUtils.isNotEmpty(createFuncs)) {
                createTempFunction(connection,createFuncs);
            }

            // pre sql
            preSql(connection,preStatementsBinds);
            stmt = prepareStatementAndBind(connection, mainSqlBinds);

            // decide whether to executeQuery or executeUpdate based on sqlType
            if (sqlParameters.getSqlType() == SqlType.QUERY.ordinal()) {
                // query statements need to be convert to JsonArray and inserted into Alert to send
                resultSet = stmt.executeQuery();
                resultProcess(resultSet);

            } else if (sqlParameters.getSqlType() == SqlType.NON_QUERY.ordinal()) {
                // non query statement
                stmt.executeUpdate();
            }

            postSql(connection,postStatementsBinds);

        } catch (Exception e) {
            logger.error("execute sql error",e);
            throw new RuntimeException("execute sql error");
        } finally {
            close(resultSet,stmt,connection);
        }
    }

    /**
     * result process
     *
     * @param resultSet resultSet
     * @throws Exception
     */
    private void resultProcess(ResultSet resultSet) throws Exception{
        JSONArray resultJSONArray = new JSONArray();
        ResultSetMetaData md = resultSet.getMetaData();
        int num = md.getColumnCount();

        int rowCount = 0;

        while (rowCount < LIMIT && resultSet.next()) {
            JSONObject mapOfColValues = new JSONObject(true);
            for (int i = 1; i <= num; i++) {
                mapOfColValues.put(md.getColumnLabel(i), resultSet.getObject(i));
            }
            resultJSONArray.add(mapOfColValues);
            rowCount++;
        }
        String result = JSONUtils.toJsonString(resultJSONArray);
        logger.debug("execute sql : {}", result);

        sendAttachment(StringUtils.isNotEmpty(sqlParameters.getTitle()) ?
                        sqlParameters.getTitle(): taskExecutionContext.getTaskName() + " query result sets",
                JSONUtils.toJsonString(resultJSONArray));
    }

    /**
     *  pre sql
     *
     * @param connection connection
     * @param preStatementsBinds preStatementsBinds
     */
    private void preSql(Connection connection,
                        List<SqlBinds> preStatementsBinds) throws Exception{
        for (SqlBinds sqlBind: preStatementsBinds) {
            try (PreparedStatement pstmt = prepareStatementAndBind(connection, sqlBind)){
                int result = pstmt.executeUpdate();
                logger.info("pre statement execute result: {}, for sql: {}",result,sqlBind.getSql());

            }
        }
    }

    /**
     * post sql
     *
     * @param connection connection
     * @param postStatementsBinds postStatementsBinds
     * @throws Exception
     */
    private void postSql(Connection connection,
                         List<SqlBinds> postStatementsBinds) throws Exception{
        for (SqlBinds sqlBind: postStatementsBinds) {
            try (PreparedStatement pstmt = prepareStatementAndBind(connection, sqlBind)){
                int result = pstmt.executeUpdate();
                logger.info("post statement execute result: {},for sql: {}",result,sqlBind.getSql());
            }
        }
    }
    /**
     * create temp function
     *
     * @param connection connection
     * @param createFuncs createFuncs
     * @throws Exception
     */
    private void createTempFunction(Connection connection,
                                    List<String> createFuncs) throws Exception{
        try (Statement funcStmt = connection.createStatement()) {
            for (String createFunc : createFuncs) {
                logger.info("hive create function sql: {}", createFunc);
                funcStmt.execute(createFunc);
            }
        }
    }
    
    /**
     * create connection
     *
     * @return connection
     * @throws Exception Exception
     */
    private Connection createConnection() throws Exception{
        // if hive , load connection params if exists
        Connection connection = null;
        if (HIVE == DbType.valueOf(sqlParameters.getType())) {
            Properties paramProp = new Properties();
            paramProp.setProperty(USER, baseDataSource.getUser());
            paramProp.setProperty(PASSWORD, baseDataSource.getPassword());
            Map<String, String> connParamMap = CollectionUtils.stringToMap(sqlParameters.getConnParams(),
                    SEMICOLON,
                    HIVE_CONF);
            paramProp.putAll(connParamMap);

            connection = DriverManager.getConnection(baseDataSource.getJdbcUrl(),
                    paramProp);
        }else{
            connection = DriverManager.getConnection(baseDataSource.getJdbcUrl(),
                    baseDataSource.getUser(),
                    baseDataSource.getPassword());
        }
        return connection;
    }

    /**
     *  close jdbc resource
     *
     * @param resultSet resultSet
     * @param pstmt pstmt
     * @param connection connection
     */
    private void close(ResultSet resultSet,
                       PreparedStatement pstmt,
                       Connection connection){
        if (resultSet != null){
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.error("close result set error : {}",e.getMessage(),e);
            }
        }

        if (pstmt != null){
            try {
                pstmt.close();
            } catch (SQLException e) {
                logger.error("close prepared statement error : {}",e.getMessage(),e);
            }
        }

        if (connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("close connection error : {}",e.getMessage(),e);
            }
        }
    }

    /**
     * preparedStatement bind
     * @param connection connection
     * @param sqlBinds  sqlBinds
     * @return PreparedStatement
     * @throws Exception Exception
     */
    private PreparedStatement prepareStatementAndBind(Connection connection, SqlBinds sqlBinds) throws Exception {
        // is the timeout set
        boolean timeoutFlag = TaskTimeoutStrategy.of(taskExecutionContext.getTaskTimeoutStrategy()) == TaskTimeoutStrategy.FAILED ||
                TaskTimeoutStrategy.of(taskExecutionContext.getTaskTimeoutStrategy()) == TaskTimeoutStrategy.WARNFAILED;
        PreparedStatement stmt = connection.prepareStatement(sqlBinds.getSql());
        if(timeoutFlag){
            stmt.setQueryTimeout(taskExecutionContext.getTaskTimeout());
        }
        Map<Integer, Property> params = sqlBinds.getParamsMap();
        if(params != null) {
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
     * @param title     title
     * @param content   content
     */
    public void sendAttachment(String title,String content){

        List<User> users = alertDao.queryUserByAlertGroupId(taskExecutionContext.getSqlTaskExecutionContext().getWarningGroupId());

        // receiving group list
        List<String> receiversList = new ArrayList<>();
        for(User user:users){
            receiversList.add(user.getEmail().trim());
        }
        // custom receiver
        String receivers = sqlParameters.getReceivers();
        if (StringUtils.isNotEmpty(receivers)){
            String[] splits = receivers.split(COMMA);
            for (String receiver : splits){
                receiversList.add(receiver.trim());
            }
        }

        // copy list
        List<String> receiversCcList = new ArrayList<>();
        // Custom Copier
        String receiversCc = sqlParameters.getReceiversCc();
        if (StringUtils.isNotEmpty(receiversCc)){
            String[] splits = receiversCc.split(COMMA);
            for (String receiverCc : splits){
                receiversCcList.add(receiverCc.trim());
            }
        }

        String showTypeName = sqlParameters.getShowType().replace(COMMA,"").trim();
        if(EnumUtils.isValidEnum(ShowType.class,showTypeName)){
            Map<String, Object> mailResult = MailUtils.sendMails(receiversList,
                    receiversCcList, title, content, ShowType.valueOf(showTypeName).getDescp());
            if(!(boolean) mailResult.get(STATUS)){
                throw new RuntimeException("send mail failed!");
            }
        }else{
            logger.error("showType: {} is not valid "  ,showTypeName);
            throw new RuntimeException(String.format("showType: %s is not valid ",showTypeName));
        }
    }

    /**
     * regular expressions match the contents between two specified strings
     * @param content           content
     * @param rgex              rgex
     * @param sqlParamsMap      sql params map
     * @param paramsPropsMap    params props map
     */
    public void setSqlParamsMap(String content, String rgex, Map<Integer,Property> sqlParamsMap, Map<String,Property> paramsPropsMap){
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(content);
        int index = 1;
        while (m.find()) {

            String paramName = m.group(1);
            Property prop =  paramsPropsMap.get(paramName);

            sqlParamsMap.put(index,prop);
            index ++;
        }
    }

    /**
     * print replace sql
     * @param content       content
     * @param formatSql     format sql
     * @param rgex          rgex
     * @param sqlParamsMap  sql params map
     */
    public void printReplacedSql(String content, String formatSql,String rgex, Map<Integer,Property> sqlParamsMap){
        //parameter print style
        logger.info("after replace sql , preparing : {}" , formatSql);
        StringBuilder logPrint = new StringBuilder("replaced sql , parameters:");
        for(int i=1;i<=sqlParamsMap.size();i++){
            logPrint.append(sqlParamsMap.get(i).getValue()+"("+sqlParamsMap.get(i).getType()+")");
        }
        logger.info("Sql Params are {}", logPrint);
    }
}
