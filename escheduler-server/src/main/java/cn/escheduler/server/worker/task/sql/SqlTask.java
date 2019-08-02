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
package cn.escheduler.server.worker.task.sql;

import cn.escheduler.alert.utils.MailUtils;
import cn.escheduler.common.Constants;
import cn.escheduler.common.enums.ShowType;
import cn.escheduler.common.enums.TaskTimeoutStrategy;
import cn.escheduler.common.enums.UdfType;
import cn.escheduler.common.job.db.*;
import cn.escheduler.common.process.Property;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.sql.SqlBinds;
import cn.escheduler.common.task.sql.SqlParameters;
import cn.escheduler.common.task.sql.SqlType;
import cn.escheduler.common.utils.CollectionUtils;
import cn.escheduler.common.utils.CommonUtils;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.AlertDao;
import cn.escheduler.dao.DaoFactory;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.model.*;
import cn.escheduler.server.utils.ParamUtils;
import cn.escheduler.server.utils.UDFUtils;
import cn.escheduler.server.worker.task.AbstractTask;
import cn.escheduler.server.worker.task.TaskProps;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.escheduler.common.Constants.*;
import static cn.escheduler.common.enums.DbType.*;

/**
 *  sql task
 */
public class SqlTask extends AbstractTask {

    /**
     *  sql parameters
     */
    private SqlParameters sqlParameters;

    /**
     *  process database access
     */
    private ProcessDao processDao;

    /**
     *  alert dao
     */
    private AlertDao alertDao;

    /**
     * datasource
     */
    private DataSource dataSource;

    /**
     * base datasource
     */
    private BaseDataSource baseDataSource;


    public SqlTask(TaskProps taskProps, Logger logger) {
        super(taskProps, logger);

        logger.info("sql task params {}", taskProps.getTaskParams());
        this.sqlParameters = JSONObject.parseObject(taskProps.getTaskParams(), SqlParameters.class);

        if (!sqlParameters.checkParameters()) {
            throw new RuntimeException("sql task params is not valid");
        }
        this.processDao = DaoFactory.getDaoInstance(ProcessDao.class);
        this.alertDao = DaoFactory.getDaoInstance(AlertDao.class);
    }

    @Override
    public void handle() throws Exception {
        // set the name of the current thread
        String threadLoggerInfoName = String.format("TaskLogInfo-%s", taskProps.getTaskAppId());
        Thread.currentThread().setName(threadLoggerInfoName);
        logger.info(sqlParameters.toString());
        logger.info("sql type : {}, datasource : {}, sql : {} , localParams : {},udfs : {},showType : {},connParams : {}",
                sqlParameters.getType(),
                sqlParameters.getDatasource(),
                sqlParameters.getSql(),
                sqlParameters.getLocalParams(),
                sqlParameters.getUdfs(),
                sqlParameters.getShowType(),
                sqlParameters.getConnParams());

        // not set data source
        if (sqlParameters.getDatasource() == 0){
            logger.error("datasource id not exists");
            exitStatusCode = -1;
            return;
        }

        dataSource= processDao.findDataSourceById(sqlParameters.getDatasource());
        logger.info("datasource name : {} , type : {} , desc : {}  , user_id : {} , parameter : {}",
                dataSource.getName(),
                dataSource.getType(),
                dataSource.getNote(),
                dataSource.getUserId(),
                dataSource.getConnectionParams());

        if (dataSource == null){
            logger.error("datasource not exists");
            exitStatusCode = -1;
            return;
        }

        Connection con = null;
        List<String> createFuncs = null;
        try {
            // load class
            DataSourceFactory.loadClass(dataSource.getType());
            // get datasource
            baseDataSource = DataSourceFactory.getDatasource(dataSource.getType(),
                    dataSource.getConnectionParams());

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

            // determine if it is UDF
            boolean udfTypeFlag = EnumUtils.isValidEnum(UdfType.class, sqlParameters.getType())
                    && StringUtils.isNotEmpty(sqlParameters.getUdfs());
            if(udfTypeFlag){
                List<UdfFunc> udfFuncList = processDao.queryUdfFunListByids(sqlParameters.getUdfs());
                createFuncs = UDFUtils.createFuncs(udfFuncList, taskProps.getTenantCode(), logger);
            }

            // execute sql task
            con = executeFuncAndSql(mainSqlBinds, preStatementSqlBinds, postStatementSqlBinds, createFuncs);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    throw e;
                }
            }
        }
    }

    /**
     *  ready to execute SQL and parameter entity Map
     * @return
     */
    private SqlBinds getSqlAndSqlParamsMap(String sql) {
        Map<Integer,Property> sqlParamsMap =  new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder();

        // find process instance by task id


        Map<String, Property> paramsMap = ParamUtils.convert(taskProps.getUserDefParamsMap(),
                taskProps.getDefinedParams(),
                sqlParameters.getLocalParametersMap(),
                taskProps.getCmdTypeIfComplement(),
                taskProps.getScheduleTime());

        // spell SQL according to the final user-defined variable
        if(paramsMap == null){
            sqlBuilder.append(sql);
            return new SqlBinds(sqlBuilder.toString(), sqlParamsMap);
        }

        if (StringUtils.isNotEmpty(sqlParameters.getTitle())){
            String title = ParameterUtils.convertParameterPlaceholders(sqlParameters.getTitle(),
                    ParamUtils.convert(paramsMap));
            logger.info("SQL tile : {}",title);
            sqlParameters.setTitle(title);
        }

        // special characters need to be escaped, ${} needs to be escaped
        String rgex = "['\"]*\\$\\{(.*?)\\}['\"]*";
        setSqlParamsMap(sql, rgex, sqlParamsMap, paramsMap);

        // replace the ${} of the SQL statement with the Placeholder
        String formatSql = sql.replaceAll(rgex,"?");
        sqlBuilder.append(formatSql);

        // print repalce sql
        printReplacedSql(sql,formatSql,rgex,sqlParamsMap);
        return new SqlBinds(sqlBuilder.toString(), sqlParamsMap);
    }

    @Override
    public AbstractParameters getParameters() {
        return this.sqlParameters;
    }

    /**
     * execute sql
     * @param mainSqlBinds
     * @param preStatementsBinds
     * @param postStatementsBinds
     * @param createFuncs
     * @return
     */
    public Connection executeFuncAndSql(SqlBinds mainSqlBinds,
                                        List<SqlBinds> preStatementsBinds,
                                        List<SqlBinds> postStatementsBinds,
                                        List<String> createFuncs){
        Connection connection = null;
        try {
            // if upload resource is HDFS and kerberos startup
            CommonUtils.loadKerberosConf();

            // if hive , load connection params if exists
            if (HIVE == dataSource.getType()) {
                Properties paramProp = new Properties();
                paramProp.setProperty(USER, baseDataSource.getUser());
                paramProp.setProperty(PASSWORD, baseDataSource.getPassword());
                Map<String, String> connParamMap = CollectionUtils.stringToMap(sqlParameters.getConnParams(),
                        SEMICOLON,
                        HIVE_CONF);
                if(connParamMap != null){
                    paramProp.putAll(connParamMap);
                }

                connection = DriverManager.getConnection(baseDataSource.getJdbcUrl(),
                        paramProp);
            }else{
                connection = DriverManager.getConnection(baseDataSource.getJdbcUrl(),
                        baseDataSource.getUser(),
                        baseDataSource.getPassword());
            }

            // create temp function
            if (CollectionUtils.isNotEmpty(createFuncs)) {
                try (Statement funcStmt = connection.createStatement()) {
                    for (String createFunc : createFuncs) {
                        logger.info("hive create function sql: {}", createFunc);
                        funcStmt.execute(createFunc);
                    }
                }
            }

            for (SqlBinds sqlBind: preStatementsBinds) {
                try (PreparedStatement stmt = prepareStatementAndBind(connection, sqlBind)) {
                    int result = stmt.executeUpdate();
                    logger.info("pre statement execute result: {}, for sql: {}",result,sqlBind.getSql());
                }
            }

            try (PreparedStatement  stmt = prepareStatementAndBind(connection, mainSqlBinds)) {
                // decide whether to executeQuery or executeUpdate based on sqlType
                if (sqlParameters.getSqlType() == SqlType.QUERY.ordinal()) {
                    // query statements need to be convert to JsonArray and inserted into Alert to send
                    JSONArray resultJSONArray = new JSONArray();
                    ResultSet resultSet = stmt.executeQuery();
                    ResultSetMetaData md = resultSet.getMetaData();
                    int num = md.getColumnCount();

                    while (resultSet.next()) {
                        JSONObject mapOfColValues = new JSONObject(true);
                        for (int i = 1; i <= num; i++) {
                            mapOfColValues.put(md.getColumnName(i), resultSet.getObject(i));
                        }
                        resultJSONArray.add(mapOfColValues);
                    }

                    logger.debug("execute sql : {}", JSONObject.toJSONString(resultJSONArray, SerializerFeature.WriteMapNullValue));

                    // if there is a result set
                    if (resultJSONArray.size() > 0) {
                        if (StringUtils.isNotEmpty(sqlParameters.getTitle())) {
                            sendAttachment(sqlParameters.getTitle(),
                                    JSONObject.toJSONString(resultJSONArray, SerializerFeature.WriteMapNullValue));
                        }else{
                            sendAttachment(taskProps.getNodeName() + " query resultsets ",
                                    JSONObject.toJSONString(resultJSONArray, SerializerFeature.WriteMapNullValue));
                        }
                    }

                    exitStatusCode = 0;

                } else if (sqlParameters.getSqlType() == SqlType.NON_QUERY.ordinal()) {
                    // non query statement
                    stmt.executeUpdate();
                    exitStatusCode = 0;
                }
            }

            for (SqlBinds sqlBind: postStatementsBinds) {
                try (PreparedStatement stmt = prepareStatementAndBind(connection, sqlBind)) {
                    int result = stmt.executeUpdate();
                    logger.info("post statement execute result: {},for sql: {}",result,sqlBind.getSql());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e.getMessage());
        }
        return connection;
    }

    /**
     * preparedStatement bind
     * @param connection
     * @param sqlBinds
     * @return
     * @throws Exception
     */
    private PreparedStatement prepareStatementAndBind(Connection connection, SqlBinds sqlBinds) throws Exception {
        PreparedStatement  stmt = connection.prepareStatement(sqlBinds.getSql());
        // is the timeout set
        boolean timeoutFlag = taskProps.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED ||
                taskProps.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED;
        if(timeoutFlag){
            stmt.setQueryTimeout(taskProps.getTaskTimeout());
        }
        Map<Integer, Property> params = sqlBinds.getParamsMap();
        if(params != null){
            for(Integer key : params.keySet()){
                Property prop = params.get(key);
                ParameterUtils.setInParameter(key,stmt,prop.getType(),prop.getValue());
            }
        }
        logger.info("prepare statement replace sql : {} ",stmt.toString());
        return stmt;
    }

    /**
     *  send mail as an attachment
     * @param title
     * @param content
     */
    public void sendAttachment(String title,String content){

        //  process instance
        ProcessInstance instance = processDao.findProcessInstanceByTaskId(taskProps.getTaskInstId());

        List<User> users = alertDao.queryUserByAlertGroupId(instance.getWarningGroupId());

        // receiving group list
        List<String> receviersList = new ArrayList<String>();
        for(User user:users){
            receviersList.add(user.getEmail().trim());
        }
        // custom receiver
        String receivers = sqlParameters.getReceivers();
        if (StringUtils.isNotEmpty(receivers)){
            String[] splits = receivers.split(COMMA);
            for (String receiver : splits){
                receviersList.add(receiver.trim());
            }
        }

        // copy list
        List<String> receviersCcList = new ArrayList<String>();
        // Custom Copier
        String receiversCc = sqlParameters.getReceiversCc();
        if (StringUtils.isNotEmpty(receiversCc)){
            String[] splits = receiversCc.split(COMMA);
            for (String receiverCc : splits){
                receviersCcList.add(receiverCc.trim());
            }
        }

        String showTypeName = sqlParameters.getShowType().replace(COMMA,"").trim();
        if(EnumUtils.isValidEnum(ShowType.class,showTypeName)){
            Map<String, Object> mailResult = MailUtils.sendMails(receviersList,
                    receviersCcList, title, content, ShowType.valueOf(showTypeName));
            if(!(Boolean) mailResult.get(STATUS)){
                throw new RuntimeException("send mail failed!");
            }
        }else{
            logger.error("showType: {} is not valid "  ,showTypeName);
            throw new RuntimeException(String.format("showType: %s is not valid ",showTypeName));
        }
    }

    /**
     *  regular expressions match the contents between two specified strings
     * @param content
     * @return
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
     *  print replace sql
     * @param content
     * @param formatSql
     * @param rgex
     * @param sqlParamsMap
     */
    public void printReplacedSql(String content, String formatSql,String rgex, Map<Integer,Property> sqlParamsMap){
        //parameter print style
        logger.info("after replace sql , preparing : {}" , formatSql);
        StringBuilder logPrint = new StringBuilder("replaced sql , parameters:");
        for(int i=1;i<=sqlParamsMap.size();i++){
            logPrint.append(sqlParamsMap.get(i).getValue()+"("+sqlParamsMap.get(i).getType()+")");
        }
        logger.info(logPrint.toString());
    }
}
