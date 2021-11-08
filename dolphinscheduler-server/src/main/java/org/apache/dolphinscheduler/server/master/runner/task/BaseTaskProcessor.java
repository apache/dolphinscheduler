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

package org.apache.dolphinscheduler.server.master.runner.task;

import static org.apache.dolphinscheduler.common.Constants.ADDRESS;
import static org.apache.dolphinscheduler.common.Constants.DATABASE;
import static org.apache.dolphinscheduler.common.Constants.JDBC_URL;
import static org.apache.dolphinscheduler.common.Constants.OTHER;
import static org.apache.dolphinscheduler.common.Constants.PASSWORD;
import static org.apache.dolphinscheduler.common.Constants.SINGLE_SLASH;
import static org.apache.dolphinscheduler.common.Constants.USER;
import static org.apache.dolphinscheduler.spi.task.dq.utils.DataQualityConstants.COMPARISON_NAME;
import static org.apache.dolphinscheduler.spi.task.dq.utils.DataQualityConstants.COMPARISON_TABLE;
import static org.apache.dolphinscheduler.spi.task.dq.utils.DataQualityConstants.COMPARISON_TYPE;
import static org.apache.dolphinscheduler.spi.task.dq.utils.DataQualityConstants.SRC_CONNECTOR_TYPE;
import static org.apache.dolphinscheduler.spi.task.dq.utils.DataQualityConstants.SRC_DATASOURCE_ID;
import static org.apache.dolphinscheduler.spi.task.dq.utils.DataQualityConstants.TARGET_CONNECTOR_TYPE;
import static org.apache.dolphinscheduler.spi.task.dq.utils.DataQualityConstants.TARGET_DATASOURCE_ID;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.SqoopJobType;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.datax.DataxParameters;
import org.apache.dolphinscheduler.common.task.dq.DataQualityParameters;
import org.apache.dolphinscheduler.common.task.procedure.ProcedureParameters;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.sqoop.sources.SourceMysqlParameter;
import org.apache.dolphinscheduler.common.task.sqoop.targets.TargetMysqlParameter;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.EnumUtils;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.common.utils.TaskParametersUtils;
import org.apache.dolphinscheduler.dao.datasource.SpringConnectionFactory;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.DqComparisonType;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.enums.ResourceType;
import org.apache.dolphinscheduler.spi.task.dq.enums.ConnectorType;
import org.apache.dolphinscheduler.spi.task.dq.enums.ExecuteSqlType;
import org.apache.dolphinscheduler.spi.task.dq.model.JdbcInfo;
import org.apache.dolphinscheduler.spi.task.dq.utils.JdbcUrlParser;
import org.apache.dolphinscheduler.spi.task.request.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.spi.task.request.DataxTaskExecutionContext;
import org.apache.dolphinscheduler.spi.task.request.ProcedureTaskExecutionContext;
import org.apache.dolphinscheduler.spi.task.request.SQLTaskExecutionContext;
import org.apache.dolphinscheduler.spi.task.request.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.spi.task.request.UdfFuncRequest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;

public abstract class BaseTaskProcessor implements ITaskProcessor {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected boolean killed = false;

    protected boolean paused = false;

    protected boolean timeout = false;

    protected TaskInstance taskInstance = null;

    protected ProcessInstance processInstance;

    protected ProcessService processService = SpringApplicationContext.getBean(ProcessService.class);

    protected SpringConnectionFactory springConnectionFactory =
                        SpringApplicationContext.getBean(SpringConnectionFactory.class);

    /**
     * pause task, common tasks donot need this.
     *
     * @return
     */
    protected abstract boolean pauseTask();

    /**
     * kill task, all tasks need to realize this function
     *
     * @return
     */
    protected abstract boolean killTask();

    /**
     * task timeout process
     * @return
     */
    protected abstract boolean taskTimeout();

    @Override
    public void run() {
    }

    @Override
    public boolean action(TaskAction taskAction) {

        switch (taskAction) {
            case STOP:
                return stop();
            case PAUSE:
                return pause();
            case TIMEOUT:
                return timeout();
            default:
                logger.error("unknown task action: {}", taskAction.toString());

        }
        return false;
    }

    protected boolean timeout() {
        if (timeout) {
            return true;
        }
        timeout = taskTimeout();
        return timeout;
    }

    /**
     * @return
     */
    protected boolean pause() {
        if (paused) {
            return true;
        }
        paused = pauseTask();
        return paused;
    }

    protected boolean stop() {
        if (killed) {
            return true;
        }
        killed = killTask();
        return killed;
    }

    @Override
    public String getType() {
        return null;
    }

    /**
     * get TaskExecutionContext
     *
     * @param taskInstance taskInstance
     * @return TaskExecutionContext
     */
    protected TaskExecutionContext getTaskExecutionContext(TaskInstance taskInstance) {
        processService.setTaskInstanceDetail(taskInstance);

        int userId = taskInstance.getProcessDefine() == null ? 0 : taskInstance.getProcessDefine().getUserId();
        Tenant tenant = processService.getTenantForProcess(taskInstance.getProcessInstance().getTenantId(), userId);

        // verify tenant is null
        if (verifyTenantIsNull(tenant, taskInstance)) {
            processService.changeTaskState(taskInstance, ExecutionStatus.FAILURE,
                    taskInstance.getStartTime(),
                    taskInstance.getHost(),
                    null,
                    null);
            return null;
        }
        // set queue for process instance, user-specified queue takes precedence over tenant queue
        String userQueue = processService.queryUserQueueByProcessInstanceId(taskInstance.getProcessInstanceId());
        taskInstance.getProcessInstance().setQueue(StringUtils.isEmpty(userQueue) ? tenant.getQueue() : userQueue);
        taskInstance.getProcessInstance().setTenantCode(tenant.getTenantCode());
        taskInstance.setResources(getResourceFullNames(taskInstance));

        SQLTaskExecutionContext sqlTaskExecutionContext = new SQLTaskExecutionContext();
        DataxTaskExecutionContext dataxTaskExecutionContext = new DataxTaskExecutionContext();
        ProcedureTaskExecutionContext procedureTaskExecutionContext = new ProcedureTaskExecutionContext();
        SqoopTaskExecutionContext sqoopTaskExecutionContext = new SqoopTaskExecutionContext();
        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = new DataQualityTaskExecutionContext();

        // SQL task
        if (TaskType.SQL.getDesc().equalsIgnoreCase(taskInstance.getTaskType())) {
            setSQLTaskRelation(sqlTaskExecutionContext, taskInstance);
        }

        // DATAX task
        if (TaskType.DATAX.getDesc().equalsIgnoreCase(taskInstance.getTaskType())) {
            setDataxTaskRelation(dataxTaskExecutionContext, taskInstance);
        }

        // procedure task
        if (TaskType.PROCEDURE.getDesc().equalsIgnoreCase(taskInstance.getTaskType())) {
            setProcedureTaskRelation(procedureTaskExecutionContext, taskInstance);
        }

        if (TaskType.SQOOP.getDesc().equalsIgnoreCase(taskInstance.getTaskType())) {
            setSqoopTaskRelation(sqoopTaskExecutionContext, taskInstance);
        }

        if (TaskType.DATA_QUALITY.getDesc().equalsIgnoreCase(taskInstance.getTaskType())) {
            setDataQualityTaskRelation(dataQualityTaskExecutionContext,taskInstance,tenant.getTenantCode());
        }

        return TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildTaskDefinitionRelatedInfo(taskInstance.getTaskDefine())
                .buildProcessInstanceRelatedInfo(taskInstance.getProcessInstance())
                .buildProcessDefinitionRelatedInfo(taskInstance.getProcessDefine())
                .buildSQLTaskRelatedInfo(sqlTaskExecutionContext)
                .buildDataxTaskRelatedInfo(dataxTaskExecutionContext)
                .buildProcedureTaskRelatedInfo(procedureTaskExecutionContext)
                .buildSqoopTaskRelatedInfo(sqoopTaskExecutionContext)
                .buildDataQualityTaskRelatedInfo(dataQualityTaskExecutionContext)
                .create();
    }

    /**
     * set procedure task relation
     *
     * @param procedureTaskExecutionContext procedureTaskExecutionContext
     * @param taskInstance taskInstance
     */
    private void setProcedureTaskRelation(ProcedureTaskExecutionContext procedureTaskExecutionContext, TaskInstance taskInstance) {
        ProcedureParameters procedureParameters = JSONUtils.parseObject(taskInstance.getTaskParams(), ProcedureParameters.class);
        int datasourceId = procedureParameters.getDatasource();
        DataSource datasource = processService.findDataSourceById(datasourceId);
        procedureTaskExecutionContext.setConnectionParams(datasource.getConnectionParams());
    }

    /**
     * set datax task relation
     *
     * @param dataxTaskExecutionContext dataxTaskExecutionContext
     * @param taskInstance taskInstance
     */
    protected void setDataxTaskRelation(DataxTaskExecutionContext dataxTaskExecutionContext, TaskInstance taskInstance) {
        DataxParameters dataxParameters = JSONUtils.parseObject(taskInstance.getTaskParams(), DataxParameters.class);

        DataSource dbSource = processService.findDataSourceById(dataxParameters.getDataSource());
        DataSource dbTarget = processService.findDataSourceById(dataxParameters.getDataTarget());

        if (dbSource != null) {
            dataxTaskExecutionContext.setDataSourceId(dataxParameters.getDataSource());
            dataxTaskExecutionContext.setSourcetype(dbSource.getType().getCode());
            dataxTaskExecutionContext.setSourceConnectionParams(dbSource.getConnectionParams());
        }

        if (dbTarget != null) {
            dataxTaskExecutionContext.setDataTargetId(dataxParameters.getDataTarget());
            dataxTaskExecutionContext.setTargetType(dbTarget.getType().getCode());
            dataxTaskExecutionContext.setTargetConnectionParams(dbTarget.getConnectionParams());
        }
    }

    /**
     * set sqoop task relation
     *
     * @param sqoopTaskExecutionContext sqoopTaskExecutionContext
     * @param taskInstance taskInstance
     */
    private void setSqoopTaskRelation(SqoopTaskExecutionContext sqoopTaskExecutionContext, TaskInstance taskInstance) {
        SqoopParameters sqoopParameters = JSONUtils.parseObject(taskInstance.getTaskParams(), SqoopParameters.class);

        // sqoop job type is template set task relation
        if (sqoopParameters.getJobType().equals(SqoopJobType.TEMPLATE.getDescp())) {
            SourceMysqlParameter sourceMysqlParameter = JSONUtils.parseObject(sqoopParameters.getSourceParams(), SourceMysqlParameter.class);
            TargetMysqlParameter targetMysqlParameter = JSONUtils.parseObject(sqoopParameters.getTargetParams(), TargetMysqlParameter.class);

            DataSource dataSource = processService.findDataSourceById(sourceMysqlParameter.getSrcDatasource());
            DataSource dataTarget = processService.findDataSourceById(targetMysqlParameter.getTargetDatasource());

            if (dataSource != null) {
                sqoopTaskExecutionContext.setDataSourceId(dataSource.getId());
                sqoopTaskExecutionContext.setSourcetype(dataSource.getType().getCode());
                sqoopTaskExecutionContext.setSourceConnectionParams(dataSource.getConnectionParams());
            }

            if (dataTarget != null) {
                sqoopTaskExecutionContext.setDataTargetId(dataTarget.getId());
                sqoopTaskExecutionContext.setTargetType(dataTarget.getType().getCode());
                sqoopTaskExecutionContext.setTargetConnectionParams(dataTarget.getConnectionParams());
            }
        }
    }

    /**
     * set data quality task relation
     *
     * @param dataQualityTaskExecutionContext dataQualityTaskExecutionContext
     * @param taskInstance taskInstance
     */
    private void setDataQualityTaskRelation(DataQualityTaskExecutionContext dataQualityTaskExecutionContext, TaskInstance taskInstance, String tenantCode) {
        DataQualityParameters dataQualityParameters =
                JSONUtils.parseObject(taskInstance.getTaskParams(), DataQualityParameters.class);
        if (dataQualityParameters == null) {
            return;
        }

        Map<String,String> config = dataQualityParameters.getRuleInputParameter();

        int ruleId = dataQualityParameters.getRuleId();
        DqRule dqRule = processService.getDqRule(ruleId);
        if (dqRule == null) {
            logger.error("can not get DqRule by id {}",ruleId);
            return;
        }

        dataQualityTaskExecutionContext.setRuleId(ruleId);
        dataQualityTaskExecutionContext.setRuleType(dqRule.getType());
        dataQualityTaskExecutionContext.setRuleName(dqRule.getName());

        List<DqRuleInputEntry> ruleInputEntryList = processService.getRuleInputEntry(ruleId);
        if (CollectionUtils.isEmpty(ruleInputEntryList)) {
            logger.error("{} rule input entry list is empty ",ruleId);
            return;
        }
        List<DqRuleExecuteSql> executeSqlList = processService.getDqExecuteSql(ruleId);
        setComparisonParams(dataQualityTaskExecutionContext, config, ruleInputEntryList, executeSqlList);
        dataQualityTaskExecutionContext.setRuleInputEntryList(JSONUtils.toJsonString(ruleInputEntryList));
        dataQualityTaskExecutionContext.setExecuteSqlList(JSONUtils.toJsonString(executeSqlList));

        dataQualityTaskExecutionContext.setHdfsPath(
                PropertyUtils.getString(Constants.FS_DEFAULTFS)
                + PropertyUtils.getString(
                        Constants.DATA_QUALITY_ERROR_OUTPUT_PATH,
                        "/user/" + tenantCode + "/data_quality_error_data"));

        setSourceConfig(dataQualityTaskExecutionContext, config);
        setTargetConfig(dataQualityTaskExecutionContext, config);
        setWriterConfig(dataQualityTaskExecutionContext);
        setStatisticsValueWriterConfig(dataQualityTaskExecutionContext);
    }

    private void setComparisonParams(DataQualityTaskExecutionContext dataQualityTaskExecutionContext,
                                     Map<String, String> config,
                                     List<DqRuleInputEntry> ruleInputEntryList,
                                     List<DqRuleExecuteSql> executeSqlList) {
        if (config.get(COMPARISON_TYPE) != null) {
            int comparisonTypeId = Integer.parseInt(config.get(COMPARISON_TYPE));
            // comparison type id 1 is fixed value ,do not need set param
            if (comparisonTypeId > 1) {
                DqComparisonType type = processService.getComparisonTypeById(comparisonTypeId);
                if (type != null) {
                    DqRuleInputEntry comparisonName = new DqRuleInputEntry();
                    comparisonName.setField(COMPARISON_NAME);
                    comparisonName.setValue(type.getName());
                    ruleInputEntryList.add(comparisonName);

                    DqRuleInputEntry comparisonTable = new DqRuleInputEntry();
                    comparisonTable.setField(COMPARISON_TABLE);
                    comparisonTable.setValue(type.getOutputTable());
                    ruleInputEntryList.add(comparisonTable);

                    if (executeSqlList == null) {
                        executeSqlList = new ArrayList<>();
                    }

                    DqRuleExecuteSql dqRuleExecuteSql = new DqRuleExecuteSql();
                    dqRuleExecuteSql.setType(ExecuteSqlType.MIDDLE.getCode());
                    dqRuleExecuteSql.setIndex(1);
                    dqRuleExecuteSql.setSql(type.getExecuteSql());
                    dqRuleExecuteSql.setTableAlias(type.getOutputTable());
                    executeSqlList.add(0,dqRuleExecuteSql);

                    if (Boolean.TRUE.equals(type.getInnerSource())) {
                        dataQualityTaskExecutionContext.setComparisonNeedStatisticsValueTable(true);
                    }
                }
            } else if (comparisonTypeId == 1) {
                dataQualityTaskExecutionContext.setCompareWithFixedValue(true);
            }
        }
    }

    public DataSource getDefaultDataSource() {
        DataSource dataSource = new DataSource();

        try {
            DruidDataSource druidDataSource = springConnectionFactory.dataSource();
            dataSource.setUserName(druidDataSource.getUsername());
            JdbcInfo jdbcInfo = JdbcUrlParser.getJdbcInfo(druidDataSource.getUrl());
            if (jdbcInfo != null) {
                Properties properties = new Properties();
                properties.setProperty(USER,druidDataSource.getUsername());
                properties.setProperty(PASSWORD,druidDataSource.getPassword());
                properties.setProperty(DATABASE, jdbcInfo.getDatabase());
                properties.setProperty(ADDRESS,jdbcInfo.getAddress());
                properties.setProperty(OTHER,jdbcInfo.getParams());
                properties.setProperty(JDBC_URL,jdbcInfo.getAddress() + SINGLE_SLASH + jdbcInfo.getDatabase());
                dataSource.setType(DbType.of(JdbcUrlParser.getDbType(jdbcInfo.getDriverName()).getCode()));
                dataSource.setConnectionParams(JSONUtils.toJsonString(properties));
            }
        } catch (SQLException e) {
            logger.error("can not get data source error , {}", e.getMessage());
        }

        return dataSource;
    }

    private void setStatisticsValueWriterConfig(DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        DataSource dataSource = getDefaultDataSource();
        ConnectorType writerConnectorType = ConnectorType.of(dataSource.getType().isHive() ? 1 : 0);
        dataQualityTaskExecutionContext.setStatisticsValueConnectorType(writerConnectorType.getDescription());
        dataQualityTaskExecutionContext.setStatisticsValueType(dataSource.getType().getCode());
        dataQualityTaskExecutionContext.setStatisticsValueWriterConnectionParams(dataSource.getConnectionParams());
        dataQualityTaskExecutionContext.setStatisticsValueTable("t_ds_dq_task_statistics_value");
    }

    private void setWriterConfig(DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        DataSource dataSource = getDefaultDataSource();
        ConnectorType writerConnectorType = ConnectorType.of(dataSource.getType().isHive() ? 1 : 0);
        dataQualityTaskExecutionContext.setWriterConnectorType(writerConnectorType.getDescription());
        dataQualityTaskExecutionContext.setWriterType(dataSource.getType().getCode());
        dataQualityTaskExecutionContext.setWriterConnectionParams(dataSource.getConnectionParams());
        dataQualityTaskExecutionContext.setWriterTable("t_ds_dq_execute_result");
    }

    private void setTargetConfig(DataQualityTaskExecutionContext dataQualityTaskExecutionContext, Map<String, String> config) {
        if (StringUtils.isNotEmpty(config.get(TARGET_DATASOURCE_ID))) {
            DataSource dataSource = processService.findDataSourceById(Integer.parseInt(config.get(TARGET_DATASOURCE_ID)));
            if (dataSource != null) {
                ConnectorType targetConnectorType = ConnectorType.of(
                        DbType.of(Integer.parseInt(config.get(TARGET_CONNECTOR_TYPE))).isHive() ? 1 : 0);
                dataQualityTaskExecutionContext.setTargetConnectorType(targetConnectorType.getDescription());
                dataQualityTaskExecutionContext.setTargetType(dataSource.getType().getCode());
                dataQualityTaskExecutionContext.setTargetConnectionParams(dataSource.getConnectionParams());
            }
        }
    }

    private void setSourceConfig(DataQualityTaskExecutionContext dataQualityTaskExecutionContext, Map<String, String> config) {
        if (StringUtils.isNotEmpty(config.get(SRC_DATASOURCE_ID))) {
            DataSource dataSource = processService.findDataSourceById(Integer.parseInt(config.get(SRC_DATASOURCE_ID)));
            if (dataSource != null) {
                ConnectorType srcConnectorType = ConnectorType.of(
                        DbType.of(Integer.parseInt(config.get(SRC_CONNECTOR_TYPE))).isHive() ? 1 : 0);
                dataQualityTaskExecutionContext.setSourceConnectorType(srcConnectorType.getDescription());
                dataQualityTaskExecutionContext.setSourceType(dataSource.getType().getCode());
                dataQualityTaskExecutionContext.setSourceConnectionParams(dataSource.getConnectionParams());
            }
        }
    }

    /**
     * set SQL task relation
     *
     * @param sqlTaskExecutionContext sqlTaskExecutionContext
     * @param taskInstance taskInstance
     */
    private void setSQLTaskRelation(SQLTaskExecutionContext sqlTaskExecutionContext, TaskInstance taskInstance) {
        SqlParameters sqlParameters = JSONUtils.parseObject(taskInstance.getTaskParams(), SqlParameters.class);
        int datasourceId = sqlParameters.getDatasource();
        DataSource datasource = processService.findDataSourceById(datasourceId);
        sqlTaskExecutionContext.setConnectionParams(datasource.getConnectionParams());

        sqlTaskExecutionContext.setDefaultFS(HadoopUtils.getInstance().getDefaultFS());

        // whether udf type
        boolean udfTypeFlag = EnumUtils.isValidEnum(UdfType.class, sqlParameters.getType())
                && !StringUtils.isEmpty(sqlParameters.getUdfs());

        if (udfTypeFlag) {
            String[] udfFunIds = sqlParameters.getUdfs().split(",");
            int[] udfFunIdsArray = new int[udfFunIds.length];
            for (int i = 0; i < udfFunIds.length; i++) {
                udfFunIdsArray[i] = Integer.parseInt(udfFunIds[i]);
            }

            List<UdfFunc> udfFuncList = processService.queryUdfFunListByIds(udfFunIdsArray);
            UdfFuncRequest udfFuncRequest;
            Map<UdfFuncRequest, String> udfFuncRequestMap = new HashMap<>();
            for (UdfFunc udfFunc : udfFuncList) {
                udfFuncRequest = JSONUtils.parseObject(JSONUtils.toJsonString(udfFunc), UdfFuncRequest.class);
                String tenantCode = processService.queryTenantCodeByResName(udfFunc.getResourceName(), ResourceType.UDF);
                udfFuncRequestMap.put(udfFuncRequest, tenantCode);
            }
            sqlTaskExecutionContext.setUdfFuncTenantCodeMap(udfFuncRequestMap);
        }
    }

    /**
     * whehter tenant is null
     *
     * @param tenant tenant
     * @param taskInstance taskInstance
     * @return result
     */
    protected boolean verifyTenantIsNull(Tenant tenant, TaskInstance taskInstance) {
        if (tenant == null) {
            logger.error("tenant not exists,process instance id : {},task instance id : {}",
                    taskInstance.getProcessInstance().getId(),
                    taskInstance.getId());
            return true;
        }
        return false;
    }

    /**
     * get resource map key is full name and value is tenantCode
     */
    protected Map<String, String> getResourceFullNames(TaskInstance taskInstance) {
        Map<String, String> resourcesMap = new HashMap<>();
        AbstractParameters baseParam = TaskParametersUtils.getParameters(taskInstance.getTaskType(), taskInstance.getTaskParams());

        if (baseParam != null) {
            List<ResourceInfo> projectResourceFiles = baseParam.getResourceFilesList();
            if (CollectionUtils.isNotEmpty(projectResourceFiles)) {

                // filter the resources that the resource id equals 0
                Set<ResourceInfo> oldVersionResources = projectResourceFiles.stream().filter(t -> t.getId() == 0).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(oldVersionResources)) {
                    oldVersionResources.forEach(t -> resourcesMap.put(t.getRes(), processService.queryTenantCodeByResName(t.getRes(), ResourceType.FILE)));
                }

                // get the resource id in order to get the resource names in batch
                Stream<Integer> resourceIdStream = projectResourceFiles.stream().map(ResourceInfo::getId);
                Set<Integer> resourceIdsSet = resourceIdStream.collect(Collectors.toSet());

                if (CollectionUtils.isNotEmpty(resourceIdsSet)) {
                    Integer[] resourceIds = resourceIdsSet.toArray(new Integer[resourceIdsSet.size()]);

                    List<Resource> resources = processService.listResourceByIds(resourceIds);
                    resources.forEach(t -> resourcesMap.put(t.getFullName(), processService.queryTenantCodeByResName(t.getFullName(), ResourceType.FILE)));
                }
            }
        }

        return resourcesMap;
    }
}