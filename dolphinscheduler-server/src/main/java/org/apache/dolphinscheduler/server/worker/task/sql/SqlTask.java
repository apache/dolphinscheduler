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
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.alert.utils.MailUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
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
import org.apache.dolphinscheduler.server.utils.ConnectionUtils;
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
        this.sqlParameters = JSONObject.parseObject(taskExecutionContext.getTaskParams(), SqlParameters.class);

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
            List<String> mainSqlList = Arrays.asList(sqlParameters.getSql().split(Constants.SEMICOLON));
            List<SqlBinds> mainSqlBinds = Optional.ofNullable(mainSqlList)
                    .orElse(new ArrayList<>())
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

            List<String> createFuncs = UDFUtils.createFuncs(sqlTaskExecutionContext.getUdfFuncList(),
                    taskExecutionContext.getTenantCode(),
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
     * @param sql sql
     * @return  SqlBinds
     */
    private SqlBinds getSqlAndSqlParamsMap(String sql) {
        Map<Integer,Property> sqlParamsMap =  new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder();
        
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
        //replace variable TIME with $[YYYYmmdd...] in sql when history run job and batch complement job
        sql = ParameterUtils.replaceScheduleTime(sql, taskExecutionContext.getScheduleTime());
        // special characters need to be escaped, ${} needs to be escaped
        String regex = "['\"]*\\$\\{(.*?)\\}['\"]*";
        setSqlParamsMap(sql, regex, sqlParamsMap, paramsMap);

        // replace the ${} of the SQL statement with the Placeholder
        String formatSql = sql.replaceAll(regex, "?");
        sqlBuilder.append(formatSql);

        // print replace sql
        printReplacedSql(formatSql,sqlParamsMap);
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
    private void executeFuncAndSql(List<SqlBinds> mainSqlBinds,
                                        List<SqlBinds> preStatementsBinds,
                                        List<SqlBinds> postStatementsBinds,
                                        List<String> createFuncs){
        Connection connection = null;
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
            executePreSql(connection,preStatementsBinds);
            //main sql
            executeMainSql(connection,mainSqlBinds);
            //post sql
            executePostSql(connection,postStatementsBinds);

        } catch (Exception e) {
            logger.error("execute sql error",e);
            throw new RuntimeException("execute sql error");
        } finally {
            if (connection != null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("close connection error",e);
                }
            }
        }
    }

    /**
     * result process
     *
     * @param resultSet resultSet
     * @throws Exception Exception
     */
    private void resultProcess(ResultSet resultSet) throws Exception{
        JSONArray resultJsonArray = new JSONArray();
        ResultSetMetaData md = resultSet.getMetaData();
        int num = md.getColumnCount();

        int rowCount = 0;

        while (rowCount < LIMIT && resultSet.next()) {
            JSONObject mapOfColValues = new JSONObject(true);
            for (int i = 1; i <= num; i++) {
                mapOfColValues.put(md.getColumnName(i), resultSet.getObject(i));
            }
            resultJsonArray.add(mapOfColValues);
            rowCount++;
        }

        // if there is a result set
        if (!resultJsonArray.isEmpty() ) {
            if(logger.isDebugEnabled()){
                logger.debug("execute sql : {}", JSONUtils.toJsonString(resultJsonArray, SerializerFeature.WriteMapNullValue));
            }

            if (StringUtils.isNotEmpty(sqlParameters.getTitle())) {
                sendAttachment(sqlParameters.getTitle(),
                        JSONUtils.toJsonString(resultJsonArray, SerializerFeature.WriteMapNullValue));
            }else{
                sendAttachment(taskExecutionContext.getTaskName() + " query result set ",
                        JSONUtils.toJsonString(resultJsonArray, SerializerFeature.WriteMapNullValue));
            }
        }
    }

    /**
     * main sql
     *
     * @param connection connection
     * @param preStatementsBinds preStatementsBinds
     */
    private void executeMainSql(Connection connection,
                        List<SqlBinds> preStatementsBinds) throws Exception{
        if (sqlParameters.getSqlType() == SqlType.QUERY.ordinal()) {
            // query statements need to be convert to JsonArray and inserted into Alert to send
            PreparedStatement preparedStatement = prepareStatementAndBind(connection, preStatementsBinds.get(0));
            ResultSet resultSet = preparedStatement.executeQuery();
            resultProcess(resultSet);

            close(preparedStatement, resultSet);

        }else if (sqlParameters.getSqlType() == SqlType.NON_QUERY.ordinal()) {
            for (SqlBinds sqlBind: preStatementsBinds) {
                try (PreparedStatement preparedStatement = prepareStatementAndBind(connection, sqlBind)){
                    // non query statement
                    preparedStatement.executeUpdate();
                }
            }
        }
    }

    private void close(PreparedStatement preparedStatement, ResultSet resultSet) {

        if(resultSet != null){
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.error("close result set error ",e);
            }
        }

        if(preparedStatement != null){
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                logger.error("close prepared statement error",e);
            }
        }

    }

    /**
     *  pre sql
     *
     * @param connection connection
     * @param preStatementsBinds preStatementsBinds
     */
    private void executePreSql(Connection connection,
                        List<SqlBinds> preStatementsBinds) throws Exception{
        for (SqlBinds sqlBind: preStatementsBinds) {
            try (PreparedStatement preparedStatement = prepareStatementAndBind(connection, sqlBind)){
                int result = preparedStatement.executeUpdate();
                logger.info("pre statement execute result: {}, for sql: {}",result,sqlBind.getSql());

            }
        }
    }

    /**
     * post sql
     *
     * @param connection connection
     * @param postStatementsBinds postStatementsBinds
     * @throws Exception Exception
     */
    private void executePostSql(Connection connection,
                         List<SqlBinds> postStatementsBinds) throws Exception{
        for (SqlBinds sqlBind: postStatementsBinds) {
            try (PreparedStatement preparedStatement = prepareStatementAndBind(connection, sqlBind)){
                int result = preparedStatement.executeUpdate();
                logger.info("post statement execute result: {},for sql: {}",result,sqlBind.getSql());
            }
        }
    }
    /**
     * create temp function
     *
     * @param connection connection
     * @param createFuncs createFuncs
     * @throws Exception Exception
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
     * @throws SQLException SQLException
     */
    private Connection createConnection() throws SQLException{
        // if hive , load connection params if exists
        Connection connection;
        if (HIVE == DbType.valueOf(sqlParameters.getType())) {
            Properties paramProp = new Properties();
            paramProp.setProperty(USER, baseDataSource.getUser());
            paramProp.setProperty(PASSWORD, baseDataSource.getPassword());
            Map<String, String> connParamMap = CollectionUtils.stringToMap(sqlParameters.getConnParams(),
                    SEMICOLON,
                    HIVE_CONF);
            paramProp.putAll(connParamMap);

            connection = ConnectionUtils.getConnection(paramProp,baseDataSource);
        }else{
            connection = ConnectionUtils.getConnection(baseDataSource);
        }
        return connection;
    }

    /**
     * preparedStatement bind
     * @param connection connection
     * @param sqlBinds sqlBinds
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
    private void sendAttachment(String title,String content){

        List<User> users = alertDao.queryUserByAlertGroupId(taskExecutionContext.getSqlTaskExecutionContext().getWarningGroupId());

        // receiving group list
        List<String> receiverList = new ArrayList<>();
        for(User user:users){
            receiverList.add(user.getEmail().trim());
        }
        // custom receiver
        String receivers = sqlParameters.getReceivers();
        if (StringUtils.isNotEmpty(receivers)){
            String[] splits = receivers.split(COMMA);
            for (String receiver : splits){
                receiverList.add(receiver.trim());
            }
        }

        // copy list
        List<String> receiverCcList = new ArrayList<>();
        // Custom Copier
        String receiversCc = sqlParameters.getReceiversCc();
        if (StringUtils.isNotEmpty(receiversCc)){
            String[] splits = receiversCc.split(COMMA);
            for (String receiverCc : splits){
                receiverCcList.add(receiverCc.trim());
            }
        }

        String showTypeName = sqlParameters.getShowType().replace(COMMA,"").trim();
        if(EnumUtils.isValidEnum(ShowType.class,showTypeName)){
            Map<String, Object> mailResult = MailUtils.sendMails(receiverList,
                    receiverCcList, title, content, ShowType.valueOf(showTypeName).getDescp());
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
     * @param regex              regex
     * @param sqlParamsMap      sql params map
     * @param paramsPropsMap    params props map
     */
    private void setSqlParamsMap(String content, String regex, Map<Integer,Property> sqlParamsMap, Map<String,Property> paramsPropsMap){
        Pattern pattern = Pattern.compile(regex);
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
     * @param formatSql     format sql
     * @param sqlParamsMap  sql params map
     */
    private void printReplacedSql(String formatSql,Map<Integer,Property> sqlParamsMap){
        //parameter print style
        logger.info("after replace sql , preparing : {}" , formatSql);
        StringBuilder logPrint = new StringBuilder("replaced sql , parameters:");
        for(int i=1;i<=sqlParamsMap.size();i++){
            logPrint.append(sqlParamsMap.get(i).getValue())
                    .append("(")
                    .append(sqlParamsMap.get(i).getType())
                    .append(")");
        }
        logger.info("Sql Params are {}", logPrint);
    }
}
