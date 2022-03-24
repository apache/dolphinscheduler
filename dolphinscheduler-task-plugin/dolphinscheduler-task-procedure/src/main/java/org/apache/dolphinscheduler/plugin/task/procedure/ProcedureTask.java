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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;

import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
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
    private TaskExecutionContext taskExecutionContext;

    private ProcedureTaskExecutionContext procedureTaskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    public ProcedureTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;

        logger.info("procedure task params {}", taskExecutionContext.getTaskParams());

        this.procedureParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ProcedureParameters.class);

        // check parameters
        if (!procedureParameters.checkParameters()) {
            throw new RuntimeException("procedure task params is not valid");
        }

        procedureTaskExecutionContext = procedureParameters.generateExtendedContext(taskExecutionContext.getResourceParametersHelper());
    }

    @Override
    public void handle() throws Exception {
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
            ConnectionParam connectionParam = DataSourceUtils.buildConnectionParams(DbType.valueOf(procedureParameters.getType()),
                    procedureTaskExecutionContext.getConnectionParams());

            // get jdbc connection
            connection = DataSourceClientProvider.getInstance().getConnection(dbType, connectionParam);
            Map<Integer, Property> sqlParamsMap = new HashMap<>();
            Map<String, Property> paramsMap = ParamUtils.convert(taskExecutionContext, getParameters());
            String proceduerSql = formatSql(sqlParamsMap, paramsMap);
            // call method
            stmt = connection.prepareCall(proceduerSql);

            // set timeout
            setTimeout(stmt);

            // outParameterMap
            Map<Integer, Property> outParameterMap = getOutParameterMap(stmt, sqlParamsMap, paramsMap);

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

    private String formatSql(Map<Integer, Property> sqlParamsMap, Map<String, Property> paramsMap) {
        // combining local and global parameters
        setSqlParamsMap(procedureParameters.getMethod(), rgex, sqlParamsMap, paramsMap, taskExecutionContext.getTaskInstanceId());
        return procedureParameters.getMethod().replaceAll(rgex, "?");
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
            procedureParameters.dealOutParam4Procedure(getOutputParameter(stmt, index, prop, dataType), prop);
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
    private Map<Integer, Property> getOutParameterMap(CallableStatement stmt, Map<Integer, Property> paramsMap
            , Map<String, Property> totalParamsMap) throws Exception {
        Map<Integer, Property> outParameterMap = new HashMap<>();
        if (procedureParameters.getLocalParametersMap() == null) {
            return outParameterMap;
        }

        int index = 1;
        if (paramsMap != null) {
            for (Map.Entry<Integer, Property> entry : paramsMap.entrySet()) {
                Property property = entry.getValue();
                if (property.getDirect().equals(Direct.IN)) {
                    ParameterUtils.setInParameter(index, stmt, property.getType(), totalParamsMap.get(property.getProp()).getValue());
                } else if (property.getDirect().equals(Direct.OUT)) {
                    setOutParameter(index, stmt, property.getType(), totalParamsMap.get(property.getProp()).getValue());
                    outParameterMap.put(index, property);
                }
                index++;
            }
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
    private Object getOutputParameter(CallableStatement stmt, int index, String prop, DataType dataType) throws SQLException {
        Object value = null;
        switch (dataType) {
            case VARCHAR:
                logger.info("out prameter varchar key : {} , value : {}", prop, stmt.getString(index));
                value = stmt.getString(index);
                break;
            case INTEGER:
                logger.info("out prameter integer key : {} , value : {}", prop, stmt.getInt(index));
                value = stmt.getInt(index);
                break;
            case LONG:
                logger.info("out prameter long key : {} , value : {}", prop, stmt.getLong(index));
                value = stmt.getLong(index);
                break;
            case FLOAT:
                logger.info("out prameter float key : {} , value : {}", prop, stmt.getFloat(index));
                value = stmt.getFloat(index);
                break;
            case DOUBLE:
                logger.info("out prameter double key : {} , value : {}", prop, stmt.getDouble(index));
                value = stmt.getDouble(index);
                break;
            case DATE:
                logger.info("out prameter date key : {} , value : {}", prop, stmt.getDate(index));
                value = stmt.getDate(index);
                break;
            case TIME:
                logger.info("out prameter time key : {} , value : {}", prop, stmt.getTime(index));
                value = stmt.getTime(index);
                break;
            case TIMESTAMP:
                logger.info("out prameter timestamp key : {} , value : {}", prop, stmt.getTimestamp(index));
                value = stmt.getTimestamp(index);
                break;
            case BOOLEAN:
                logger.info("out prameter boolean key : {} , value : {}", prop, stmt.getBoolean(index));
                value = stmt.getBoolean(index);
                break;
            default:
                break;
        }
        return value;
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
