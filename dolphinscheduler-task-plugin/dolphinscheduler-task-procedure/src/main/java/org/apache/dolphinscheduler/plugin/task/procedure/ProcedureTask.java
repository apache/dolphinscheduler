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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourcePluginManager;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcedureTask extends AbstractTask {

    private final ProcedureParameters procedureParameters;

    private final TaskExecutionContext taskExecutionContext;

    private final ProcedureTaskExecutionContext procedureTaskExecutionContext;

    private volatile Statement sessionStatement;

    public ProcedureTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);

        this.taskExecutionContext = taskExecutionContext;

        this.procedureParameters =
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ProcedureParameters.class);
        log.info("Initialize procedure task params {}", JSONUtils.toPrettyJsonString(procedureParameters));

        // check parameters
        if (procedureParameters == null || !procedureParameters.checkParameters()) {
            throw new TaskException("procedure task params is not valid");
        }

        procedureTaskExecutionContext =
                procedureParameters.generateExtendedContext(taskExecutionContext.getResourceParametersHelper());
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        log.info("procedure type : {}, datasource : {}, method : {} , localParams : {}",
                procedureParameters.getType(),
                procedureParameters.getDatasource(),
                procedureParameters.getMethod(),
                procedureParameters.getLocalParams());

        DbType dbType = DbType.valueOf(procedureParameters.getType());
        DataSourceProcessor dataSourceProcessor = DataSourcePluginManager.getDataSourceProcessor(dbType);
        ConnectionParam connectionParams =
                dataSourceProcessor.createConnectionParams(procedureTaskExecutionContext.getConnectionParams());
        try (Connection connection = DataSourceClientProvider.getAdHocConnection(dbType, connectionParams)) {
            // Record the placeholder index and parameter mapping relationship
            Map<Integer, Property> sqlPlaceHolders = new HashMap<>();

            Map<String, Property> prepareParams = taskExecutionContext.getPrepareParamsMap();

            // todo: rename to resolveSqlPlaceHolder and make it return placeHolderIndex map
            setSqlParamsMap(procedureParameters.getMethod(), sqlPlaceHolders, prepareParams,
                    taskExecutionContext.getTaskInstanceId());

            // Replace the SQL statement's parameter placeholders with "?" for CallableStatement
            // Then will set the parameters through CallableStatement's setObject method
            // todo: maybe we can directly replace the parameter placeholders with the actual parameter values, don't
            // use ? here
            String proceduerSql = procedureParameters.getMethod().replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?");
            // call method
            try (CallableStatement stat = connection.prepareCall(proceduerSql)) {
                sessionStatement = stat;
                // set timeout
                setTimeout(stat);

                // outParameterMap
                Map<Integer, Property> sqlOutPlaceHolders =
                        assemblySqlPlaceHolder(stat, sqlPlaceHolders, prepareParams);

                // todo: deal with the result
                stat.execute();

                Map<String, String> sqlOutParameters = parseOutParameters(stat, sqlOutPlaceHolders);

                // todo: If the task is failed, do we need to deal with the output parameters? otherwise the localparam
                // cannot pass to post.
                // If so, we can set the output parameters in the finally block.
                procedureParameters.dealOutParam(sqlOutParameters);
                taskExecutionContext.setVarPool(procedureParameters.getVarPool());
                setExitStatusCode(EXIT_CODE_SUCCESS);
            }
        } catch (Exception e) {
            if (exitStatusCode == TaskConstants.EXIT_CODE_KILL) {
                log.info("This procedure task has been killed");
                return;
            }
            setExitStatusCode(EXIT_CODE_FAILURE);
            log.error("Failed to execute this procedure task", e);
            throw new TaskException("Execute procedure task failed", e);
        }
    }

    @Override
    public void cancel() throws TaskException {
        if (sessionStatement != null) {
            try {
                log.info("Try to cancel this procedure task");
                sessionStatement.cancel();
                setExitStatusCode(TaskConstants.EXIT_CODE_KILL);
                log.info("This procedure task was canceled");
            } catch (Exception ex) {
                log.warn("Failed to cancel this procedure task", ex);
                throw new TaskException("Cancel this procedure task failed", ex);
            }
        } else {
            log.info(
                    "Attempted to cancel this procedure task, but no active statement exists. Possible reasons: task not started, already completed, or canceled.");
        }
    }

    // parse the out parameter from stmt and put them into varPool
    private Map<String, String> parseOutParameters(CallableStatement stmt,
                                                   Map<Integer, Property> sqlOutPlaceHolders) throws SQLException {
        Map<String, String> sqlOutParameters = new HashMap<>();
        for (Map.Entry<Integer, Property> out : sqlOutPlaceHolders.entrySet()) {
            int index = out.getKey();
            Property property = out.getValue();
            String prop = property.getProp();
            DataType dataType = property.getType();
            // get output parameter
            Object outputParameterValue = getOutputParameter(stmt, index, prop, dataType);
            sqlOutParameters.put(prop, String.valueOf(outputParameterValue));
        }
        return sqlOutParameters;
    }

    /**
     * get output parameter
     *
     * @param stmt      CallableStatement
     * @param sqlParams paramsMap
     * @return outParameterMap
     * @throws Exception Exception
     */
    private Map<Integer, Property> assemblySqlPlaceHolder(CallableStatement stmt,
                                                          Map<Integer, Property> sqlParams,
                                                          Map<String, Property> prepareParams) throws Exception {
        Map<Integer, Property> outParameterMap = new HashMap<>();
        if (MapUtils.isEmpty(sqlParams)) {
            return outParameterMap;
        }

        int index = 1;
        for (Map.Entry<Integer, Property> entry : sqlParams.entrySet()) {
            Property sqlProperty = entry.getValue();
            Property prepareParam = prepareParams.get(sqlProperty.getProp());
            if (sqlProperty.getDirect().equals(Direct.IN)) {
                ParameterUtils.setInParameter(index, stmt, sqlProperty.getType(), prepareParam.getValue());
            } else if (sqlProperty.getDirect().equals(Direct.OUT)) {
                // todo: It's reasonable to set the value here?
                setOutParameter(index, stmt, sqlProperty.getType(), prepareParam.getValue());
                outParameterMap.put(index, sqlProperty);
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
     * get output parameter
     *
     * @param stmt     stmt
     * @param index    index
     * @param prop     prop
     * @param dataType dataType
     * @throws SQLException SQLException
     */
    private Object getOutputParameter(CallableStatement stmt, int index, String prop,
                                      DataType dataType) throws SQLException {
        Object value = null;
        switch (dataType) {
            case VARCHAR:
                log.info("out parameter varchar key : {} , value : {}", prop, stmt.getString(index));
                value = stmt.getString(index);
                break;
            case INTEGER:
                log.info("out parameter integer key : {} , value : {}", prop, stmt.getInt(index));
                value = stmt.getInt(index);
                break;
            case LONG:
                log.info("out parameter long key : {} , value : {}", prop, stmt.getLong(index));
                value = stmt.getLong(index);
                break;
            case FLOAT:
                log.info("out parameter float key : {} , value : {}", prop, stmt.getFloat(index));
                value = stmt.getFloat(index);
                break;
            case DOUBLE:
                log.info("out parameter double key : {} , value : {}", prop, stmt.getDouble(index));
                value = stmt.getDouble(index);
                break;
            case DATE:
                log.info("out parameter date key : {} , value : {}", prop, stmt.getDate(index));
                value = stmt.getDate(index);
                break;
            case TIME:
                log.info("out parameter time key : {} , value : {}", prop, stmt.getTime(index));
                value = stmt.getTime(index);
                break;
            case TIMESTAMP:
                log.info("out parameter timestamp key : {} , value : {}", prop, stmt.getTimestamp(index));
                value = stmt.getTimestamp(index);
                break;
            case BOOLEAN:
                log.info("out parameter boolean key : {} , value : {}", prop, stmt.getBoolean(index));
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
