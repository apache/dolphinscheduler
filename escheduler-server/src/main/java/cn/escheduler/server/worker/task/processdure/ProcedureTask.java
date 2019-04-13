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
package cn.escheduler.server.worker.task.processdure;

import cn.escheduler.common.Constants;
import cn.escheduler.common.enums.DataType;
import cn.escheduler.common.enums.DbType;
import cn.escheduler.common.enums.Direct;
import cn.escheduler.common.enums.TaskTimeoutStrategy;
import cn.escheduler.common.job.db.BaseDataSource;
import cn.escheduler.common.job.db.ClickHouseDataSource;
import cn.escheduler.common.job.db.MySQLDataSource;
import cn.escheduler.common.job.db.OracleDataSource;
import cn.escheduler.common.job.db.PostgreDataSource;
import cn.escheduler.common.job.db.SQLServerDataSource;
import cn.escheduler.common.process.Property;
import cn.escheduler.common.task.AbstractParameters;
import cn.escheduler.common.task.procedure.ProcedureParameters;
import cn.escheduler.common.utils.CollectionUtils;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.DaoFactory;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.model.DataSource;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.server.utils.ParamUtils;
import cn.escheduler.server.worker.task.AbstractTask;
import cn.escheduler.server.worker.task.TaskProps;
import com.alibaba.fastjson.JSONObject;
import com.cronutils.utils.StringUtils;
import org.slf4j.Logger;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *  procedure task
 */
public class ProcedureTask extends AbstractTask {

    /**
     * procedure parameters
     */
    private ProcedureParameters procedureParameters;

    /**
     *  process database access
     */
    private ProcessDao processDao;

    public ProcedureTask(TaskProps taskProps, Logger logger) {
        super(taskProps, logger);

        logger.info("procedure task params {}", taskProps.getTaskParams());

        this.procedureParameters = JSONObject.parseObject(taskProps.getTaskParams(), ProcedureParameters.class);

        // check parameters
        if (!procedureParameters.checkParameters()) {
            throw new RuntimeException("procedure task params is not valid");
        }

        this.processDao = DaoFactory.getDaoInstance(ProcessDao.class);
    }

    @Override
    public void handle() throws Exception {
        // set the name of the current thread
        String threadLoggerInfoName = String.format("TaskLogInfo-%s", taskProps.getTaskAppId());
        Thread.currentThread().setName(threadLoggerInfoName);

        logger.info("processdure type : {}, datasource : {}, method : {} , localParams : {}",
                procedureParameters.getType(),
                procedureParameters.getDatasource(),
                procedureParameters.getMethod(),
                procedureParameters.getLocalParams());

        // determine whether there is a data source
        if (procedureParameters.getDatasource() == 0){
            logger.error("datasource is null");
            exitStatusCode = 0;
        }else {

            DataSource dataSource = processDao.findDataSourceById(procedureParameters.getDatasource());
            logger.info("datasource name : {} , type : {} , desc : {} ,  user_id : {} , parameter : {}",
                    dataSource.getName(),dataSource.getType(),dataSource.getNote(),
                    dataSource.getUserId(),dataSource.getConnectionParams());

            if (dataSource != null){
                Connection connection = null;
                CallableStatement stmt = null;
                try {
                    BaseDataSource baseDataSource = null;

                    if (DbType.MYSQL.name().equals(dataSource.getType().name())){
                        baseDataSource = JSONObject.parseObject(dataSource.getConnectionParams(),MySQLDataSource.class);
                        Class.forName(Constants.JDBC_MYSQL_CLASS_NAME);
                    }else if (DbType.POSTGRESQL.name().equals(dataSource.getType().name())){
                        baseDataSource = JSONObject.parseObject(dataSource.getConnectionParams(),PostgreDataSource.class);
                        Class.forName(Constants.JDBC_POSTGRESQL_CLASS_NAME);
                    }else if (DbType.CLICKHOUSE.name().equals(dataSource.getType().name())){
                        // NOTE: currently, ClickHouse don't support procedure or UDF yet,
                        //  but still load JDBC driver to keep source code sync with other DB
                        baseDataSource = JSONObject.parseObject(dataSource.getConnectionParams(),ClickHouseDataSource.class);
                        Class.forName(Constants.JDBC_CLICKHOUSE_CLASS_NAME);
                    }else if (DbType.ORACLE.name().equals(dataSource.getType().name())){
                        baseDataSource = JSONObject.parseObject(dataSource.getConnectionParams(), OracleDataSource.class);
                        Class.forName(Constants.JDBC_ORACLE_CLASS_NAME);
                    }else if (DbType.SQLSERVER.name().equals(dataSource.getType().name())){
                        baseDataSource = JSONObject.parseObject(dataSource.getConnectionParams(), SQLServerDataSource.class);
                        Class.forName(Constants.JDBC_SQLSERVER_CLASS_NAME);
                    }

                    // get jdbc connection
                    connection = DriverManager.getConnection(baseDataSource.getJdbcUrl(),
                            baseDataSource.getUser(),
                            baseDataSource.getPassword());

                    // get process instance by task instance id
                    ProcessInstance processInstance = processDao.findProcessInstanceByTaskId(taskProps.getTaskInstId());

                    // combining local and global parameters
                    Map<String, Property> paramsMap = ParamUtils.convert(taskProps.getUserDefParamsMap(),
                            taskProps.getDefinedParams(),
                            procedureParameters.getLocalParametersMap(),
                            processInstance.getCmdTypeIfComplement(),
                            processInstance.getScheduleTime());


                    Collection<Property> userDefParamsList = null;

                    if (procedureParameters.getLocalParametersMap() != null){
                        userDefParamsList = procedureParameters.getLocalParametersMap().values();
                    }

                    String method = "";
                    // no parameters
                    if (CollectionUtils.isEmpty(userDefParamsList)){
                        method = "{call " + procedureParameters.getMethod() + "}";
                    }else { // exists parameters
                        int size = userDefParamsList.size();
                        StringBuilder parameter = new StringBuilder();
                        parameter.append("(");
                        for (int i = 0 ;i < size - 1; i++){
                            parameter.append("?,");
                        }
                        parameter.append("?)");
                        method = "{call " + procedureParameters.getMethod() + parameter.toString()+ "}";
                    }

                    logger.info("call method : {}",method);
                    // call method
                    stmt = connection.prepareCall(method);
                    if(taskProps.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED || taskProps.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED){
                        stmt.setQueryTimeout(taskProps.getTaskTimeout());
                    }
                    Map<Integer,Property> outParameterMap = new HashMap<>();
                    if (userDefParamsList != null && userDefParamsList.size() > 0){
                        int index = 1;
                        for (Property property : userDefParamsList){
                            logger.info("localParams : prop : {} , dirct : {} , type : {} , value : {}"
                                    ,property.getProp(),
                                    property.getDirect(),
                                    property.getType(),
                                    property.getValue());
                            // set parameters
                            if (property.getDirect().equals(Direct.IN)){
                                ParameterUtils.setInParameter(index,stmt,property.getType(),paramsMap.get(property.getProp()).getValue());
                            }else if (property.getDirect().equals(Direct.OUT)){
                                setOutParameter(index,stmt,property.getType(),paramsMap.get(property.getProp()).getValue());
                                property.setValue(paramsMap.get(property.getProp()).getValue());
                                outParameterMap.put(index,property);
                            }
                            index++;
                        }
                    }

                    stmt.executeUpdate();

                    /**
                     *  print the output parameters to the log
                     */
                    Iterator<Map.Entry<Integer, Property>> iter = outParameterMap.entrySet().iterator();
                    while (iter.hasNext()){
                        Map.Entry<Integer, Property> en = iter.next();

                        int index = en.getKey();
                        Property property = en.getValue();
                        String prop = property.getProp();
                        DataType dataType = property.getType();

                        if (dataType.equals(DataType.VARCHAR)){
                            String value = stmt.getString(index);
                            logger.info("out prameter key : {} , value : {}",prop,value);
                        }else if (dataType.equals(DataType.INTEGER)){
                            int value = stmt.getInt(index);
                            logger.info("out prameter key : {} , value : {}",prop,value);
                        }else if (dataType.equals(DataType.LONG)){
                            long value = stmt.getLong(index);
                            logger.info("out prameter key : {} , value : {}",prop,value);
                        }else if (dataType.equals(DataType.FLOAT)){
                            float value = stmt.getFloat(index);
                            logger.info("out prameter key : {} , value : {}",prop,value);
                        }else if (dataType.equals(DataType.DOUBLE)){
                            double value = stmt.getDouble(index);
                            logger.info("out prameter key : {} , value : {}",prop,value);
                        }else if (dataType.equals(DataType.DATE)){
                            Date value = stmt.getDate(index);
                            logger.info("out prameter key : {} , value : {}",prop,value);
                        }else if (dataType.equals(DataType.TIME)){
                            Time value = stmt.getTime(index);
                            logger.info("out prameter key : {} , value : {}",prop,value);
                        }else if (dataType.equals(DataType.TIMESTAMP)){
                            Timestamp value = stmt.getTimestamp(index);
                            logger.info("out prameter key : {} , value : {}",prop,value);
                        }else if (dataType.equals(DataType.BOOLEAN)){
                            boolean value = stmt.getBoolean(index);
                            logger.info("out prameter key : {} , value : {}",prop,value);
                        }
                    }

                    exitStatusCode = 0;
                }catch (Exception e){
                    logger.error(e.getMessage(),e);
                    exitStatusCode = -1;
                    throw new RuntimeException("process interrupted. exit status code is : "  + exitStatusCode);
                }
                finally {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            exitStatusCode = -1;
                            logger.error(e.getMessage(),e);
                        }
                    }
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            exitStatusCode = -1;
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return procedureParameters;
    }

    /**
     * set out parameter
     * @param index
     * @param stmt
     * @param dataType
     * @param value
     * @throws Exception
     */
    private void setOutParameter(int index,CallableStatement stmt,DataType dataType,String value)throws Exception{
        if (dataType.equals(DataType.VARCHAR)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.VARCHAR);
            }else {
                stmt.registerOutParameter(index, Types.VARCHAR, value);
            }

        }else if (dataType.equals(DataType.INTEGER)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.INTEGER);
            }else {
                stmt.registerOutParameter(index, Types.INTEGER, value);
            }

        }else if (dataType.equals(DataType.LONG)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index,Types.INTEGER);
            }else {
                stmt.registerOutParameter(index,Types.INTEGER ,value);
            }
        }else if (dataType.equals(DataType.FLOAT)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.FLOAT);
            }else {
                stmt.registerOutParameter(index, Types.FLOAT,value);
            }
        }else if (dataType.equals(DataType.DOUBLE)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.DOUBLE);
            }else {
                stmt.registerOutParameter(index, Types.DOUBLE , value);
            }

        }else if (dataType.equals(DataType.DATE)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.DATE);
            }else {
                stmt.registerOutParameter(index, Types.DATE , value);
            }

        }else if (dataType.equals(DataType.TIME)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.TIME);
            }else {
                stmt.registerOutParameter(index, Types.TIME , value);
            }

        }else if (dataType.equals(DataType.TIMESTAMP)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.TIMESTAMP);
            }else {
                stmt.registerOutParameter(index, Types.TIMESTAMP , value);
            }

        }else if (dataType.equals(DataType.BOOLEAN)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.BOOLEAN);
            }else {
                stmt.registerOutParameter(index, Types.BOOLEAN , value);
            }
        }
    }
}