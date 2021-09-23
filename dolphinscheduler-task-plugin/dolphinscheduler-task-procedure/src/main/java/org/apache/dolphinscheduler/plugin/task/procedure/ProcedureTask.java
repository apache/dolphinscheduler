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

package org.apache.dolphinscheduler.plugin.task.procedure;

import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.EXIT_CODE_SUCCESS;
import static org.apache.dolphinscheduler.spi.task.TaskConstants.TASK_LOG_INFO_FORMAT;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.datasource.ConnectionParam;
import org.apache.dolphinscheduler.plugin.task.datasource.DatasourceUtil;
import org.apache.dolphinscheduler.spi.enums.DataType;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.Direct;
import org.apache.dolphinscheduler.spi.task.Property;
import org.apache.dolphinscheduler.spi.task.paramparser.ParamUtils;
import org.apache.dolphinscheduler.spi.task.paramparser.ParameterUtils;
import org.apache.dolphinscheduler.spi.task.request.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.CollectionUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * procedure task
 */
public class ProcedureTask extends AbstractTaskExecutor {

    /**
     * procedure parameters
     */
    private ProcedureParameters procedureParameters;

    /**
     * taskExecutionContext
     */
    private TaskRequest taskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public ProcedureTask(TaskRequest taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;

        logger.info("procedure task params {}", taskExecutionContext.getTaskParams());

        this.procedureParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ProcedureParameters.class);

        // check parameters
        if (!procedureParameters.checkParameters()) {
            throw new RuntimeException("procedure task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {
        // set the name of the current thread
        String threadLoggerInfoName = String.format(TASK_LOG_INFO_FORMAT, taskExecutionContext.getTaskAppId());
        Thread.currentThread().setName(threadLoggerInfoName);

        logger.info("procedure type : {}, datasource : {}, method : {} , localParams : {}",
                procedureParameters.getType(),
                procedureParameters.getDatasource(),
                procedureParameters.getMethod(),
                procedureParameters.getLocalParams());

        Connection connection = null;
        CallableStatement stmt = null;
        try {
            // load class
            DbType dbType = DbType.valueOf(procedureParameters.getType());
            // get datasource
            ConnectionParam connectionParam = DatasourceUtil.buildConnectionParams(DbType.valueOf(procedureParameters.getType()),
                    taskExecutionContext.getProcedureTaskExecutionContext().getConnectionParams());

            // get jdbc connection
            connection = DatasourceUtil.getConnection(dbType, connectionParam);

            // combining local and global parameters
            Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext,getParameters());

            // call method
            stmt = connection.prepareCall(procedureParameters.getMethod());

            // set timeout
            setTimeout(stmt);

            // outParameterMap
            Map<Integer, Property> outParameterMap = getOutParameterMap(stmt, paramsMap);

            stmt.executeUpdate();

            // print the output parameters to the log
            printOutParameter(stmt, outParameterMap);

            setExitStatusCode(EXIT_CODE_SUCCESS);
        } catch (Exception e) {
            setExitStatusCode(EXIT_CODE_FAILURE);
            logger.error("procedure task error", e);
            throw e;
        } finally {
            close(stmt, connection);
        }
    }

    /**
     * print outParameter
     *
     * @param stmt            CallableStatement
     * @param outParameterMap outParameterMap
     * @throws SQLException SQLException
     */
    private void printOutParameter(CallableStatement stmt,
                                   Map<Integer, Property> outParameterMap) throws SQLException {
        for (Map.Entry<Integer, Property> en : outParameterMap.entrySet()) {
            int index = en.getKey();
            Property property = en.getValue();
            String prop = property.getProp();
            DataType dataType = property.getType();
            // get output parameter
            getOutputParameter(stmt, index, prop, dataType);
        }
    }

    /**
     * get output parameter
     *
     * @param stmt      CallableStatement
     * @param paramsMap paramsMap
     * @return outParameterMap
     * @throws Exception Exception
     */
    private Map<Integer, Property> getOutParameterMap(CallableStatement stmt, Map<String, Property> paramsMap) throws Exception {
        Map<Integer, Property> outParameterMap = new HashMap<>();
        if (procedureParameters.getLocalParametersMap() == null) {
            return outParameterMap;
        }

        Collection<Property> userDefParamsList = procedureParameters.getLocalParametersMap().values();

        if (CollectionUtils.isEmpty(userDefParamsList)) {
            return outParameterMap;
        }

        int index = 1;
        for (Property property : userDefParamsList) {
            logger.info("localParams : prop : {} , dirct : {} , type : {} , value : {}"
                    , property.getProp(),
                    property.getDirect(),
                    property.getType(),
                    property.getValue());
            // set parameters
            if (property.getDirect().equals(Direct.IN)) {
                ParameterUtils.setInParameter(index, stmt, property.getType(), paramsMap.get(property.getProp()).getValue());
            } else if (property.getDirect().equals(Direct.OUT)) {
                setOutParameter(index, stmt, property.getType(), paramsMap.get(property.getProp()).getValue());
                property.setValue(paramsMap.get(property.getProp()).getValue());
                outParameterMap.put(index, property);
            }
            index++;
        }

        return outParameterMap;
    }

    /**
     * set timeout
     *
     * @param stmt CallableStatement
     */
    private void setTimeout(CallableStatement stmt) throws SQLException {
        Boolean failed = taskExecutionContext.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED;
        Boolean warnFailed = taskExecutionContext.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED;
        if (failed || warnFailed) {
            stmt.setQueryTimeout(taskExecutionContext.getTaskTimeout());
        }
    }

    /**
     * close jdbc resource
     *
     * @param stmt       stmt
     * @param connection connection
     */
    private void close(PreparedStatement stmt, Connection connection) {
        if (stmt != null) {
            try {
                stmt.close();
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
     * get output parameter
     *
     * @param stmt     stmt
     * @param index    index
     * @param prop     prop
     * @param dataType dataType
     * @throws SQLException SQLException
     */
    private void getOutputParameter(CallableStatement stmt, int index, String prop, DataType dataType) throws SQLException {
        switch (dataType) {
            case VARCHAR:
                logger.info("out prameter varchar key : {} , value : {}", prop, stmt.getString(index));
                break;
            case INTEGER:
                logger.info("out prameter integer key : {} , value : {}", prop, stmt.getInt(index));
                break;
            case LONG:
                logger.info("out prameter long key : {} , value : {}", prop, stmt.getLong(index));
                break;
            case FLOAT:
                logger.info("out prameter float key : {} , value : {}", prop, stmt.getFloat(index));
                break;
            case DOUBLE:
                logger.info("out prameter double key : {} , value : {}", prop, stmt.getDouble(index));
                break;
            case DATE:
                logger.info("out prameter date key : {} , value : {}", prop, stmt.getDate(index));
                break;
            case TIME:
                logger.info("out prameter time key : {} , value : {}", prop, stmt.getTime(index));
                break;
            case TIMESTAMP:
                logger.info("out prameter timestamp key : {} , value : {}", prop, stmt.getTimestamp(index));
                break;
            case BOOLEAN:
                logger.info("out prameter boolean key : {} , value : {}", prop, stmt.getBoolean(index));
                break;
            default:
                break;
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return procedureParameters;
    }

    /**
     * set out parameter
     *
     * @param index    index
     * @param stmt     stmt
     * @param dataType dataType
     * @param value    value
     * @throws Exception exception
     */
    private void setOutParameter(int index, CallableStatement stmt, DataType dataType, String value) throws Exception {
        int sqlType;
        switch (dataType) {
            case VARCHAR:
                sqlType = Types.VARCHAR;
                break;
            case INTEGER:
            case LONG:
                sqlType = Types.INTEGER;
                break;
            case FLOAT:
                sqlType = Types.FLOAT;
                break;
            case DOUBLE:
                sqlType = Types.DOUBLE;
                break;
            case DATE:
                sqlType = Types.DATE;
                break;
            case TIME:
                sqlType = Types.TIME;
                break;
            case TIMESTAMP:
                sqlType = Types.TIMESTAMP;
                break;
            case BOOLEAN:
                sqlType = Types.BOOLEAN;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + dataType);
        }

        if (StringUtils.isEmpty(value)) {
            stmt.registerOutParameter(index, sqlType);
        } else {
            stmt.registerOutParameter(index, sqlType, value);
        }
    }
}