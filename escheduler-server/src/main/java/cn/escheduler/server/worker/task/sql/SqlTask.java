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
import cn.escheduler.common.enums.DbType;
import cn.escheduler.common.enums.ShowType;
import cn.escheduler.common.enums.TaskTimeoutStrategy;
import cn.escheduler.common.enums.UdfType;
import cn.escheduler.common.job.db.*;
import cn.escheduler.common.process.Property;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.sql.SqlParameters;
import cn.escheduler.common.task.sql.SqlType;
import cn.escheduler.common.utils.CollectionUtils;
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


    public SqlTask(TaskProps props, Logger logger) {
        super(props, logger);

        logger.info("sql task params {}", taskProps.getTaskParams());
        this.sqlParameters = JSONObject.parseObject(props.getTaskParams(), SqlParameters.class);

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
                sqlParameters.getType(), sqlParameters.getDatasource(), sqlParameters.getSql(),
                sqlParameters.getLocalParams(), sqlParameters.getUdfs(), sqlParameters.getShowType(), sqlParameters.getConnParams());

        // determine whether there is a data source
        if (sqlParameters.getDatasource() == 0){
            logger.error("datasource is null");
            exitStatusCode = -1;
        }else {
            List<String> createFuncs = null;
            DataSource dataSource = processDao.findDataSourceById(sqlParameters.getDatasource());
            logger.info("datasource name : {} , type : {} , desc : {}  , user_id : {} , parameter : {}",
                    dataSource.getName(),dataSource.getType(),dataSource.getNote(),
                    dataSource.getUserId(),dataSource.getConnectionParams());

            if (dataSource != null){
                Connection con = null;
                try {
                    BaseDataSource baseDataSource = null;
                    if (DbType.MYSQL.name().equals(dataSource.getType().name())){
                        baseDataSource = JSONObject.parseObject(dataSource.getConnectionParams(),MySQLDataSource.class);
                        Class.forName(Constants.JDBC_MYSQL_CLASS_NAME);
                    }else if (DbType.POSTGRESQL.name().equals(dataSource.getType().name())){
                        baseDataSource = JSONObject.parseObject(dataSource.getConnectionParams(),PostgreDataSource.class);
                        Class.forName(Constants.JDBC_POSTGRESQL_CLASS_NAME);
                    }else if (DbType.HIVE.name().equals(dataSource.getType().name())){
                        baseDataSource = JSONObject.parseObject(dataSource.getConnectionParams(),HiveDataSource.class);
                        Class.forName(Constants.JDBC_HIVE_CLASS_NAME);
                    }else if (DbType.SPARK.name().equals(dataSource.getType().name())){
                        baseDataSource = JSONObject.parseObject(dataSource.getConnectionParams(),SparkDataSource.class);
                        Class.forName(Constants.JDBC_SPARK_CLASS_NAME);
                    }else if (DbType.CLICKHOUSE.name().equals(dataSource.getType().name())){
                        baseDataSource = JSONObject.parseObject(dataSource.getConnectionParams(),ClickHouseDataSource.class);
                        Class.forName(Constants.JDBC_CLICKHOUSE_CLASS_NAME);
                    }else if (DbType.ORACLE.name().equals(dataSource.getType().name())){
                        baseDataSource = JSONObject.parseObject(dataSource.getConnectionParams(),OracleDataSource.class);
                        Class.forName(Constants.JDBC_ORACLE_CLASS_NAME);
                    }else if (DbType.SQLSERVER.name().equals(dataSource.getType().name())){
                        baseDataSource = JSONObject.parseObject(dataSource.getConnectionParams(),SQLServerDataSource.class);
                        Class.forName(Constants.JDBC_SQLSERVER_CLASS_NAME);
                    }

                    Map<Integer,Property> sqlParamMap =  new HashMap<Integer,Property>();
                    StringBuilder sqlBuilder = new StringBuilder();

                    // ready to execute SQL and parameter entity Map
                    setSqlAndSqlParamsMap(sqlBuilder,sqlParamMap);

                    if(EnumUtils.isValidEnum(UdfType.class, sqlParameters.getType()) && StringUtils.isNotEmpty(sqlParameters.getUdfs())){
                        List<UdfFunc> udfFuncList = processDao.queryUdfFunListByids(sqlParameters.getUdfs());
                        createFuncs = UDFUtils.createFuncs(udfFuncList, taskProps.getTenantCode(), logger);
                    }

                    // execute sql task
                    con = executeFuncAndSql(baseDataSource,sqlBuilder.toString(),sqlParamMap,createFuncs);

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
        }
    }

    /**
     *  ready to execute SQL and parameter entity Map
     * @return
     */
    private void setSqlAndSqlParamsMap(StringBuilder sqlBuilder,Map<Integer,Property> sqlParamsMap) {

        String sql =  sqlParameters.getSql();

        // find process instance by task id
        ProcessInstance processInstance = processDao.findProcessInstanceByTaskId(taskProps.getTaskInstId());

        Map<String, Property> paramsMap = ParamUtils.convert(taskProps.getUserDefParamsMap(),
                taskProps.getDefinedParams(),
                sqlParameters.getLocalParametersMap(),
                processInstance.getCmdTypeIfComplement(),
                processInstance.getScheduleTime());

        // spell SQL according to the final user-defined variable
        if(paramsMap == null){
            sqlBuilder.append(sql);
            return;
        }

        // special characters need to be escaped, ${} needs to be escaped
        String rgex = "'?\\$\\{(.*?)\\}'?";
        setSqlParamsMap(sql,rgex,sqlParamsMap,paramsMap);

        // replace the ${} of the SQL statement with the Placeholder
        String formatSql = sql.replaceAll(rgex,"?");
        sqlBuilder.append(formatSql);

        // print repalce sql
        printReplacedSql(sql,formatSql,rgex,sqlParamsMap);
    }

    @Override
    public AbstractParameters getParameters() {
        return this.sqlParameters;
    }

    /**
     *  execute sql
     * @param baseDataSource
     * @param sql
     * @param params
     */
    public Connection executeFuncAndSql(BaseDataSource baseDataSource, String sql, Map<Integer,Property> params, List<String> createFuncs){
        Connection connection = null;
        try {

            if (DbType.HIVE.name().equals(sqlParameters.getType())) {
                Properties paramProp = new Properties();
                paramProp.setProperty("user", baseDataSource.getUser());
                paramProp.setProperty("password", baseDataSource.getPassword());
                Map<String, String> connParamMap = CollectionUtils.stringToMap(sqlParameters.getConnParams(), Constants.SEMICOLON,"hiveconf:");
                if(connParamMap != null){
                    paramProp.putAll(connParamMap);
                }

                connection = DriverManager.getConnection(baseDataSource.getJdbcUrl(),paramProp);
            }else{
                connection = DriverManager.getConnection(baseDataSource.getJdbcUrl(),
                        baseDataSource.getUser(), baseDataSource.getPassword());
            }

            Statement  funcStmt = connection.createStatement();
            // create temp function
            if (createFuncs != null) {
                for (String createFunc : createFuncs) {
                    logger.info("hive create function sql: {}", createFunc);
                    funcStmt.execute(createFunc);
                }
            }

            PreparedStatement  stmt = connection.prepareStatement(sql);
            if(taskProps.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED || taskProps.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED){
                stmt.setQueryTimeout(taskProps.getTaskTimeout());
            }
            if(params != null){
                for(Integer key : params.keySet()){
                    Property prop = params.get(key);
                    ParameterUtils.setInParameter(key,stmt,prop.getType(),prop.getValue());
                }
            }
            // decide whether to executeQuery or executeUpdate based on sqlType
            if(sqlParameters.getSqlType() == SqlType.QUERY.ordinal()){
                // query statements need to be convert to JsonArray and inserted into Alert to send
                JSONArray array=new JSONArray();
                ResultSet resultSet = stmt.executeQuery();
                ResultSetMetaData md=resultSet.getMetaData();
                int num=md.getColumnCount();

                while(resultSet.next()){
                    JSONObject mapOfColValues=new JSONObject(true);
                    for(int i=1;i<=num;i++){
                        mapOfColValues.put(md.getColumnName(i), resultSet.getObject(i));
                    }
                    array.add(mapOfColValues);
                }

                logger.info("execute sql : {}",JSONObject.toJSONString(array, SerializerFeature.WriteMapNullValue));

                // send as an attachment
                if(StringUtils.isEmpty(sqlParameters.getShowType())){
                    logger.info("showType is empty,don't need send email");
                }else{
                    if(array.size() > 0 ){
                        sendAttachment(taskProps.getNodeName() + " query resultsets ",JSONObject.toJSONString(array, SerializerFeature.WriteMapNullValue));
                    }
                }

                exitStatusCode = 0;

            }else if(sqlParameters.getSqlType() == SqlType.NON_QUERY.ordinal()){
                // non query statement
                int result = stmt.executeUpdate();
                exitStatusCode = 0;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return connection;
    }


    /**
     *  send mail as an attachment
     * @param title
     * @param content
     */
    public void sendAttachment(String title,String content){

        //  process instance
        ProcessInstance instance = processDao.findProcessInstanceByTaskId(taskProps.getTaskInstId());

        // process define
        ProcessDefinition processDefine = processDao.findProcessDefineById(instance.getProcessDefinitionId());

        List<User> users = alertDao.queryUserByAlertGroupId(instance.getWarningGroupId());

        // receiving group list
        List<String> receviersList = new ArrayList<String>();
        for(User user:users){
            receviersList.add(user.getEmail());
        }
        // custom receiver
        String receivers = processDefine.getReceivers();
        if (StringUtils.isNotEmpty(receivers)){
            String[] splits = receivers.split(Constants.COMMA);
            for (String receiver : splits){
                receviersList.add(receiver);
            }
        }

        // copy list
        List<String> receviersCcList = new ArrayList<String>();


        // Custom Copier
        String receiversCc = processDefine.getReceiversCc();

        if (StringUtils.isNotEmpty(receiversCc)){
            String[] splits = receiversCc.split(Constants.COMMA);
            for (String receiverCc : splits){
                receviersCcList.add(receiverCc);
            }
        }

        String showTypeName = sqlParameters.getShowType().replace(Constants.COMMA,"").trim();
        if(EnumUtils.isValidEnum(ShowType.class,showTypeName)){
            MailUtils.sendMails(receviersList,receviersCcList,title, content, ShowType.valueOf(showTypeName));
        }else{
            logger.error("showType: {} is not valid "  ,showTypeName);
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
        StringBuffer logPrint = new StringBuffer("replaced sql , parameters:");
        for(int i=1;i<=sqlParamsMap.size();i++){
            logPrint.append(sqlParamsMap.get(i).getValue()+"("+sqlParamsMap.get(i).getType()+")");
        }
        logger.info(logPrint.toString());

        //direct print style
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(content);
        int index = 1;
        StringBuffer sb = new StringBuffer("replaced sql , direct:");
        while (m.find()) {

            m.appendReplacement(sb, sqlParamsMap.get(index).getValue());

            index ++;
        }
        m.appendTail(sb);
        logger.info(sb.toString());
    }
}
