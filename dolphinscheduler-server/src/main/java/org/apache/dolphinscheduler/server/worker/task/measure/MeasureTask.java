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
package org.apache.dolphinscheduler.server.worker.task.measure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.dolphinscheduler.alert.utils.MailUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.job.db.BaseDataSource;
import org.apache.dolphinscheduler.common.job.db.DataSourceFactory;
import org.apache.dolphinscheduler.common.process.MeasureProperty;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.measure.MeasureParameters;
import org.apache.dolphinscheduler.common.task.sql.SqlBinds;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.utils.SpringApplicationContext;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.dolphinscheduler.common.Constants.*;
import static org.apache.dolphinscheduler.common.enums.DbType.HIVE;

/**
 * measure task
 */
public class MeasureTask extends AbstractTask {

    /**
     *  measure parameters
     */
    private MeasureParameters measureParameters;

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


    public MeasureTask(TaskProps taskProps, Logger logger) {
        super(taskProps, logger);

        logger.info("sql task params {}", taskProps.getTaskParams());
        this.measureParameters = JSONObject.parseObject(taskProps.getTaskParams(), MeasureParameters.class);

        if (!measureParameters.checkParameters()) {
            throw new RuntimeException("sql task params is not valid");
        }
        this.processDao = SpringApplicationContext.getBean(ProcessDao.class);
        this.alertDao = SpringApplicationContext.getBean(AlertDao.class);
    }

    @Override
    public void handle() throws Exception {
        // set the name of the current thread
        String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, taskProps.getTaskAppId());
        Thread.currentThread().setName(threadLoggerInfoName);
        logger.info(measureParameters.toString());

        // not set data source
        if (measureParameters.getDatasource() == 0){
            logger.error("datasource id not exists");
            exitStatusCode = -1;
            return;
        }

        dataSource= processDao.findDataSourceById(measureParameters.getDatasource());
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
        try {
            // load class
            DataSourceFactory.loadClass(dataSource.getType());
            // get datasource
            baseDataSource = DataSourceFactory.getDatasource(dataSource.getType(),
                    dataSource.getConnectionParams());

            // ready to execute SQL and parameter entity Map
            SqlBinds mainSqlBinds = getSqlAndSqlParamsMap(measureParameters.getSql());


            // execute measure task
            con = executeMeasure(mainSqlBinds);
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
                measureParameters.getLocalParametersMap(),
                taskProps.getCmdTypeIfComplement(),
                taskProps.getScheduleTime());

        // spell SQL according to the final user-defined variable
        if(paramsMap == null){
            sqlBuilder.append(sql);
            return new SqlBinds(sqlBuilder.toString(), sqlParamsMap);
        }

        if (StringUtils.isNotEmpty(measureParameters.getTitle())){
            String title = ParameterUtils.convertParameterPlaceholders(measureParameters.getTitle(),
                    ParamUtils.convert(paramsMap));
            logger.info("SQL tile : {}",title);
            measureParameters.setTitle(title);
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
        return this.measureParameters;
    }

    /**
     * execute measure
     * @param mainSqlBinds          main sql binds
     * @return Connection
     */
    public Connection executeMeasure(SqlBinds mainSqlBinds){
        Connection connection = null;
        try {
            // if upload resource is HDFS and kerberos startup
            CommonUtils.loadKerberosConf();

            // if hive , load connection params if exists
            if (HIVE == dataSource.getType()) {
                Properties paramProp = new Properties();
                paramProp.setProperty(USER, baseDataSource.getUser());
                paramProp.setProperty(PASSWORD, baseDataSource.getPassword());
                Map<String, String> connParamMap = CollectionUtils.stringToMap(measureParameters.getConnParams(),
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


            try (PreparedStatement  stmt = prepareStatementAndBind(connection, mainSqlBinds)) {
                // decide whether to executeQuery or executeUpdate based on sqlType
                    // query statements need to be convert to JsonArray and inserted into Alert to send
                JSONArray resultJSONArray = new JSONArray();
                ResultSet resultSet = stmt.executeQuery();
                ResultSetMetaData md = resultSet.getMetaData();
                int num = md.getColumnCount();
                List<MeasureProperty> measureParams = measureParameters.getMeasureParams();
                JSONObject measureResult = new JSONObject();
                JSONObject measureFailStateJson = new JSONObject();
                while (resultSet.next()) {
                    for (int i = 1; i <= num; i++) {
                        String columnTypeName = md.getColumnTypeName(i);
                        String columnName = md.getColumnName(i);
                        Object result = resultSet.getObject(i);

                        for (int j = 0; j < measureParams.size(); j++) {
                            MeasureProperty measureProperty = measureParams.get(j);
                            BasicType measureBasicType = measureProperty.getBasicType();
                            String measureProp = measureProperty.getProp();
                            String measureValue = measureProperty.getValue();
                            MeasureOperation measureOperation = measureProperty.getOperation();
                            BigDecimal measureMatchRate = measureProperty.getMatchRate();
                            if(columnName.equals(measureProp)){
                                String columnTypeError = columnName+"【TypeNotMatch】";
                                String columnDataIsNull = columnName+"【DataIsNull】";
                                String columnDataIsEmpty = columnName+"【DataIsEmpty】";
                                String columnDataMatch = columnName+"【DataMatch】";
                                String columnDataNotMatch = columnName+"【DataNotMatch】";
                                if(measureResult.get(columnTypeError) == null){
                                    measureResult.put(columnTypeError,0);
                                }
                                if(measureResult.get(columnDataIsNull) == null){
                                    measureResult.put(columnDataIsNull,0);
                                }
                                if(measureResult.get(columnDataIsEmpty) == null){
                                    measureResult.put(columnDataIsEmpty,0);
                                }
                                if(measureResult.get(columnDataMatch) == null){
                                    measureResult.put(columnDataMatch,0);
                                }
                                if(measureResult.get(columnDataNotMatch) == null){
                                    measureResult.put(columnDataNotMatch,0);
                                }
                                if(!measureBasicType.getDescp().equals(columnTypeName)){
                                    //logger.info(columnName+" type error");
                                    measureResult.put(columnTypeError,measureResult.getInteger(columnTypeError).intValue() + 1);
                                    continue;
                                }
                                if(result == null){
                                    //logger.info(columnName+" data is null");
                                    measureResult.put(columnDataIsNull,measureResult.getInteger(columnDataIsNull).intValue() + 1);
                                    continue;
                                }
                                if("".equals(result)){
                                    //logger.info(columnName+" data is empty");
                                    measureResult.put(columnDataIsEmpty,measureResult.getInteger(columnDataIsEmpty).intValue() + 1);
                                    continue;
                                }
                                int intColumnDataMatch = measureResult.getInteger(columnDataMatch).intValue();
                                int intColumnDataNotMatch = measureResult.getInteger(columnDataNotMatch).intValue();

                                if(BasicType.VARCHAR.equals(measureProperty.getBasicType()) ||
                                        BasicType.CHAR.equals(measureProperty.getBasicType())){
                                    measureResult = this.measureStringColumnDataMatch(result.toString(),measureValue,measureOperation,columnName,columnDataMatch,columnDataNotMatch,intColumnDataMatch,intColumnDataNotMatch,measureResult);
                                }

                                if(BasicType.INT.equals(measureProperty.getBasicType()) ||
                                        BasicType.INTEGER.equals(measureProperty.getBasicType()) ||
                                        BasicType.BIGINT.equals(measureProperty.getBasicType())){
                                    measureResult = this.measureIntColumnDataMatch(result.toString(),measureValue,measureOperation,columnName,columnDataMatch,columnDataNotMatch,intColumnDataMatch,intColumnDataNotMatch,measureResult);
                                }

                                if(BasicType.LONG.equals(measureProperty.getBasicType())){
                                    measureResult = this.measureLongColumnDataMatch(result.toString(),measureValue,measureOperation,columnName,columnDataMatch,columnDataNotMatch,intColumnDataMatch,intColumnDataNotMatch,measureResult);
                                }
                                if(BasicType.DOUBLE.equals(measureProperty.getBasicType())){
                                    measureResult = this.measureDoubleColumnDataMatch(result.toString(),measureValue,measureOperation,columnName,columnDataMatch,columnDataNotMatch,intColumnDataMatch,intColumnDataNotMatch,measureResult);
                                }
                                if(BasicType.FLOAT.equals(measureProperty.getBasicType())) {
                                    measureResult = this.measureFloatColumnDataMatch(result.toString(), measureValue, measureOperation, columnName, columnDataMatch, columnDataNotMatch, intColumnDataMatch, intColumnDataNotMatch, measureResult);
                                }
                                if(BasicType.DECIMAL.equals(measureProperty.getBasicType())) {
                                    measureResult = this.measureDecimalColumnDataMatch(result.toString(), measureValue, measureOperation, columnName, columnDataMatch, columnDataNotMatch, intColumnDataMatch, intColumnDataNotMatch, measureResult);
                                }
                                if(BasicType.TIMESTAMP.equals(measureProperty.getBasicType()) ||
                                        BasicType.DATETIME.equals(measureProperty.getBasicType())) {
                                    measureResult = this.measureDateTimeColumnDataMatch(resultSet.getTimestamp(i), measureValue, measureOperation, columnName, columnDataMatch, columnDataNotMatch, intColumnDataMatch, intColumnDataNotMatch, measureResult);
                                }
                                int columnDataNotMatchTotal = measureResult.getInteger(columnTypeError).intValue()
                                        + measureResult.getInteger(columnDataIsNull).intValue()
                                        + measureResult.getInteger(columnDataIsEmpty).intValue()
                                        + measureResult.getInteger(columnDataNotMatch).intValue();

                                int columnDataMatchTotal = measureResult.getInteger(columnDataMatch).intValue();
                                int columnDataTotal = columnDataNotMatchTotal + columnDataMatchTotal;
                                BigDecimal a = new BigDecimal(columnDataMatchTotal).divide(new BigDecimal(columnDataTotal),2,BigDecimal.ROUND_HALF_UP);
                                int b = a.compareTo(measureMatchRate.divide(new BigDecimal(100),2,BigDecimal.ROUND_HALF_UP));
                                if(b < 0){
                                    measureFailStateJson.put(columnName,MeasureFailState.FALSE);
                                }else{
                                    measureFailStateJson.put(columnName,MeasureFailState.TRUE);
                                }
                                //measureResult.put(columnName+"-DataNotMatchTotal",columnDataNotMatchTotal);
                                //measureResult.put(columnName+"-DataMatchTotal",columnDataMatchTotal);
                                measureResult.put(columnName+"【DataTotal】",columnDataTotal);
                                measureResult.put(columnName+"【MatchRate】",a.multiply(new BigDecimal(100))+"%");
                            }
                        }
                    }
                }
                resultSet.close();

                logger.info("measureResult : {}", JSONObject.toJSONString(measureResult, SerializerFeature.MapSortField));
                LinkedHashMap<String, String> jsonMap = JSON.parseObject(JSONObject.toJSONString(measureResult, SerializerFeature.MapSortField), new TypeReference<LinkedHashMap<String, String>>() {
                });
                JSONObject mapOfColValues = new JSONObject(true);
                for (Map.Entry<String, String> entry : jsonMap.entrySet()) {
                    mapOfColValues.put(entry.getKey(),entry.getValue());
                }
                resultJSONArray.add(mapOfColValues);
                // if there is a result set
                if (resultJSONArray.size() > 0) {
                    if (StringUtils.isNotEmpty(measureParameters.getTitle())) {
                        sendAttachment(measureParameters.getTitle(),
                                JSONObject.toJSONString(resultJSONArray, SerializerFeature.WriteMapNullValue));
                    }else{
                        sendAttachment(taskProps.getNodeName() + " query resultsets ",
                                JSONObject.toJSONString(resultJSONArray, SerializerFeature.WriteMapNullValue));
                    }
                }
                for (int i = 0; i < measureParams.size(); i++) {
                    MeasureProperty measureProperty = measureParams.get(i);
                    String measureProp = measureProperty.getProp();
                    MeasureFailState measureFailState = measureProperty.getFailState();
                    if(MeasureFailState.TRUE.equals(measureFailState)){
                        for (Map.Entry entry : measureFailStateJson.entrySet()) {
                            if(entry.getKey().equals(measureProp)){
                                if(!entry.getValue().equals(measureFailState)){
                                    logger.info("jobFailReason : {}", measureProp +" target is 【"+ measureFailState +"】 , result is 【"+ entry.getValue() +"】");
                                    exitStatusCode = -1;
                                    return connection;
                                }else{
                                    exitStatusCode = 0;
                                }
                            }
                        }
                    }
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
        if(params != null) {
            for (Map.Entry<Integer, Property> entry : params.entrySet()) {
                Property prop = entry.getValue();
                ParameterUtils.setInParameter(entry.getKey(), stmt, prop.getType(), prop.getValue());
            }
        }
        logger.info("prepare statement replace sql : {} ",stmt.toString());
        return stmt;
    }

    /**
     * send mail as an attachment
     * @param title     title
     * @param content   content
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
        String receivers = measureParameters.getReceivers();
        if (StringUtils.isNotEmpty(receivers)){
            String[] splits = receivers.split(COMMA);
            for (String receiver : splits){
                receviersList.add(receiver.trim());
            }
        }

        // copy list
        List<String> receviersCcList = new ArrayList<String>();
        // Custom Copier
        String receiversCc = measureParameters.getReceiversCc();
        if (StringUtils.isNotEmpty(receiversCc)){
            String[] splits = receiversCc.split(COMMA);
            for (String receiverCc : splits){
                receviersCcList.add(receiverCc.trim());
            }
        }

        String showTypeName = measureParameters.getShowType().replace(COMMA,"").trim();
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
        logger.info(logPrint.toString());
    }

    private JSONObject measureStringColumnDataMatch(String result, String measureValue,
                                                    MeasureOperation measureOperation, String columnName, String columnDataMatch,
                                                    String columnDataNotMatch, int intColumnDataMatch, int intColumnDataNotMatch, JSONObject measureResult){
        if (MeasureOperation.CONTAIN.equals(measureOperation)) {
            if (result.contains(measureValue)) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.NOTCONTAIN.equals(measureOperation)) {
            if (!result.contains(measureValue)) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        }
        return measureResult;
    }

    private JSONObject measureIntColumnDataMatch(String result, String measureValue,
                                              MeasureOperation measureOperation, String columnName, String columnDataMatch,
                                              String columnDataNotMatch, int intColumnDataMatch, int intColumnDataNotMatch, JSONObject measureResult){
        int basicTypeResult = Integer.parseInt(result);
        int basicTypeMeasureValue = Integer.parseInt(measureValue);
        if (MeasureOperation.EQUAL.equals(measureOperation)) {
            if (basicTypeResult == basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.NOTEQUAL.equals(measureOperation)) {
            if (basicTypeResult != basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.MORE.equals(measureOperation)) {
            if (basicTypeResult > basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.MOREEQUAL.equals(measureOperation)) {
            if (basicTypeResult >= basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.LESS.equals(measureOperation)) {
            if (basicTypeResult < basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.LESSEQUAL.equals(measureOperation)) {
            if (basicTypeResult <= basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        }
        return measureResult;
    }

    private JSONObject measureLongColumnDataMatch(String result, String measureValue,
                                                 MeasureOperation measureOperation, String columnName, String columnDataMatch,
                                                 String columnDataNotMatch, int intColumnDataMatch, int intColumnDataNotMatch, JSONObject measureResult){
        long basicTypeResult = Long.parseLong(result);
        long basicTypeMeasureValue = Long.parseLong(measureValue);
        if (MeasureOperation.EQUAL.equals(measureOperation)) {
            if (basicTypeResult == basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.NOTEQUAL.equals(measureOperation)) {
            if (basicTypeResult != basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.MORE.equals(measureOperation)) {
            if (basicTypeResult > basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.MOREEQUAL.equals(measureOperation)) {
            if (basicTypeResult >= basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.LESS.equals(measureOperation)) {
            if (basicTypeResult < basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.LESSEQUAL.equals(measureOperation)) {
            if (basicTypeResult <= basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        }
        return measureResult;
    }

    private JSONObject measureDoubleColumnDataMatch(String result, String measureValue,
                                                 MeasureOperation measureOperation, String columnName, String columnDataMatch,
                                                 String columnDataNotMatch, int intColumnDataMatch, int intColumnDataNotMatch, JSONObject measureResult){
        double basicTypeResult = Double.parseDouble(result);
        double basicTypeMeasureValue = Double.parseDouble(measureValue);
        if (MeasureOperation.EQUAL.equals(measureOperation)) {
            if (basicTypeResult == basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.NOTEQUAL.equals(measureOperation)) {
            if (basicTypeResult != basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.MORE.equals(measureOperation)) {
            if (basicTypeResult > basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.MOREEQUAL.equals(measureOperation)) {
            if (basicTypeResult >= basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.LESS.equals(measureOperation)) {
            if (basicTypeResult < basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.LESSEQUAL.equals(measureOperation)) {
            if (basicTypeResult <= basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        }
        return measureResult;
    }

    private JSONObject measureFloatColumnDataMatch(String result, String measureValue,
                                                    MeasureOperation measureOperation, String columnName, String columnDataMatch,
                                                    String columnDataNotMatch, int intColumnDataMatch, int intColumnDataNotMatch, JSONObject measureResult){
        float basicTypeResult = Float.parseFloat(result);
        float basicTypeMeasureValue = Float.parseFloat(measureValue);
        if (MeasureOperation.EQUAL.equals(measureOperation)) {
            if (basicTypeResult == basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.NOTEQUAL.equals(measureOperation)) {
            if (basicTypeResult != basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.MORE.equals(measureOperation)) {
            if (basicTypeResult > basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.MOREEQUAL.equals(measureOperation)) {
            if (basicTypeResult >= basicTypeMeasureValue) {
                
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.LESS.equals(measureOperation)) {
            if (basicTypeResult < basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.LESSEQUAL.equals(measureOperation)) {
            if (basicTypeResult <= basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        }
        return measureResult;
    }

    private JSONObject measureDecimalColumnDataMatch(String result, String measureValue,
                                                   MeasureOperation measureOperation, String columnName, String columnDataMatch,
                                                   String columnDataNotMatch, int intColumnDataMatch, int intColumnDataNotMatch, JSONObject measureResult){
        BigDecimal basicTypeResult = new BigDecimal(result);
        BigDecimal basicTypeMeasureValue = new BigDecimal(measureValue);
        if (MeasureOperation.EQUAL.equals(measureOperation)) {
            if (basicTypeResult.compareTo(basicTypeMeasureValue) == 0) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.NOTEQUAL.equals(measureOperation)) {
            if (basicTypeResult.compareTo(basicTypeMeasureValue) != 0) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.MORE.equals(measureOperation)) {
            if (basicTypeResult.compareTo(basicTypeMeasureValue) == 1) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.MOREEQUAL.equals(measureOperation)) {
            if (basicTypeResult.compareTo(basicTypeMeasureValue) > -1) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.LESS.equals(measureOperation)) {
            if (basicTypeResult.compareTo(basicTypeMeasureValue) == -1) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.LESSEQUAL.equals(measureOperation)) {
            if (basicTypeResult.compareTo(basicTypeMeasureValue) < 1) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        }
        return measureResult;
    }

    private JSONObject measureDateTimeColumnDataMatch(Timestamp result, String measureValue,
                                                  MeasureOperation measureOperation, String columnName, String columnDataMatch,
                                                  String columnDataNotMatch, int intColumnDataMatch, int intColumnDataNotMatch, JSONObject measureResult){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateResult =  DateUtils.parse(df.format(result),"yyyy-MM-dd HH:mm:ss");
        Date dateMeasureValue =  DateUtils.parse(measureValue,"yyyy-MM-dd HH:mm:ss");
        long basicTypeResult = dateResult.getTime();
        long basicTypeMeasureValue = dateMeasureValue.getTime();
        if (MeasureOperation.EQUAL.equals(measureOperation)) {
            if (basicTypeResult == basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.NOTEQUAL.equals(measureOperation)) {
            if (basicTypeResult != basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.MORE.equals(measureOperation)) {
            if (basicTypeResult > basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.MOREEQUAL.equals(measureOperation)) {
            if (basicTypeResult >= basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.LESS.equals(measureOperation)) {
            if (basicTypeResult < basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        } else if (MeasureOperation.LESSEQUAL.equals(measureOperation)) {
            if (basicTypeResult <= basicTypeMeasureValue) {
                measureResult.put(columnDataMatch,intColumnDataMatch + 1);
            } else {
                measureResult.put(columnDataNotMatch,intColumnDataNotMatch + 1);
            }
        }
        return measureResult;
    }
}
