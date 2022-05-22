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
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.CLUSTER;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DATA_QUALITY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_K8S;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.COMPARISON_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.COMPARISON_TABLE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.COMPARISON_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.SRC_CONNECTOR_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.SRC_DATASOURCE_ID;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.TARGET_CONNECTOR_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.TARGET_DATASOURCE_ID;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
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
import org.apache.dolphinscheduler.plugin.task.api.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ConnectorType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ExecuteSqlType;
import org.apache.dolphinscheduler.plugin.task.api.model.JdbcInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.AbstractResourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.UdfFuncParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.JdbcUrlParser;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.dq.DataQualityParameters;
import org.apache.dolphinscheduler.plugin.task.k8s.K8sTaskParameters;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.enums.ResourceType;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

public abstract class BaseTaskProcessor implements ITaskProcessor {

    protected final Logger logger = LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, getClass()));

    protected boolean killed = false;

    protected boolean paused = false;

    protected boolean timeout = false;

    protected TaskInstance taskInstance = null;

    protected ProcessInstance processInstance;

    protected int maxRetryTimes;

    protected int commitInterval;

    protected ProcessService processService = SpringApplicationContext.getBean(ProcessService.class);

    protected MasterConfig masterConfig = SpringApplicationContext.getBean(MasterConfig.class);

    protected TaskPluginManager taskPluginManager = SpringApplicationContext.getBean(TaskPluginManager.class);

    protected String threadLoggerInfoName;

    @Override
    public void init(TaskInstance taskInstance, ProcessInstance processInstance) {
        if (processService == null) {
            processService = SpringApplicationContext.getBean(ProcessService.class);
        }
        if (masterConfig == null) {
            masterConfig = SpringApplicationContext.getBean(MasterConfig.class);
        }
        this.taskInstance = taskInstance;
        this.processInstance = processInstance;
        this.maxRetryTimes = masterConfig.getTaskCommitRetryTimes();
        this.commitInterval = masterConfig.getTaskCommitInterval();
    }

    protected javax.sql.DataSource defaultDataSource =
                        SpringApplicationContext.getBean(javax.sql.DataSource.class);

    /**
     * pause task, common tasks donot need this.
     */
    protected abstract boolean pauseTask();

    /**
     * kill task, all tasks need to realize this function
     */
    protected abstract boolean killTask();

    /**
     * task timeout process
     */
    protected abstract boolean taskTimeout();

    /**
     * submit task
     */
    protected abstract boolean submitTask();

    /**
     * run task
     */
    protected abstract boolean runTask();

    /**
     * dispatch task
     */
    protected abstract boolean dispatchTask();

    @Override
    public boolean action(TaskAction taskAction) {
        String threadName = Thread.currentThread().getName();
        if (StringUtils.isNotEmpty(threadLoggerInfoName)) {
            Thread.currentThread().setName(threadLoggerInfoName);
        }
        switch (taskAction) {
            case STOP:
                return stop();
            case PAUSE:
                return pause();
            case TIMEOUT:
                return timeout();
            case SUBMIT:
                return submit();
            case RUN:
                return run();
            case DISPATCH:
                return dispatch();
            default:
                logger.error("unknown task action: {}", taskAction);
        }
        // reset thread name
        Thread.currentThread().setName(threadName);
        return false;
    }

    protected boolean submit() {
        return submitTask();
    }

    protected boolean run() {
        return runTask();
    }

    protected boolean dispatch() {
        return dispatchTask();
    }

    protected boolean timeout() {
        if (timeout) {
            return true;
        }
        timeout = taskTimeout();
        return timeout;
    }

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

    @Override
    public TaskInstance taskInstance() {
        return this.taskInstance;
    }

    /**
     * set master task running logger.
     */
    public void setTaskExecutionLogger() {
        threadLoggerInfoName = LoggerUtils.buildTaskId(taskInstance.getFirstSubmitTime(),
                processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getId());
        Thread.currentThread().setName(threadLoggerInfoName);
    }

    /**
     * get TaskExecutionContext
     *
     * @param taskInstance taskInstance
     * @return TaskExecutionContext
     */
    protected TaskExecutionContext getTaskExecutionContext(TaskInstance taskInstance) {
        int userId = taskInstance.getProcessDefine() == null ? 0 : taskInstance.getProcessDefine().getUserId();
        Tenant tenant = processService.getTenantForProcess(taskInstance.getProcessInstance().getTenantId(), userId);

        // verify tenant is null
        if (verifyTenantIsNull(tenant, taskInstance)) {
            taskInstance.setState(ExecutionStatus.FAILURE);
            processService.saveTaskInstance(taskInstance);
            return null;
        }
        // set queue for process instance, user-specified queue takes precedence over tenant queue
        String userQueue = processService.queryUserQueueByProcessInstance(taskInstance.getProcessInstance());
        taskInstance.getProcessInstance().setQueue(StringUtils.isEmpty(userQueue) ? tenant.getQueue() : userQueue);
        taskInstance.getProcessInstance().setTenantCode(tenant.getTenantCode());
        taskInstance.setResources(getResourceFullNames(taskInstance));

        TaskChannel taskChannel = taskPluginManager.getTaskChannel(taskInstance.getTaskType());
        ResourceParametersHelper resources = taskChannel.getResources(taskInstance.getTaskParams());
        this.setTaskResourceInfo(resources);

        // TODO to be optimized
        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = new DataQualityTaskExecutionContext();
        if (TASK_TYPE_DATA_QUALITY.equalsIgnoreCase(taskInstance.getTaskType())) {
            setDataQualityTaskRelation(dataQualityTaskExecutionContext,taskInstance,tenant.getTenantCode());
        }
        K8sTaskExecutionContext k8sTaskExecutionContext = new K8sTaskExecutionContext();
        if (TASK_TYPE_K8S.equalsIgnoreCase(taskInstance.getTaskType())) {
            setK8sTaskRelation(k8sTaskExecutionContext, taskInstance);
        }

        return TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildTaskDefinitionRelatedInfo(taskInstance.getTaskDefine())
                .buildProcessInstanceRelatedInfo(taskInstance.getProcessInstance())
                .buildProcessDefinitionRelatedInfo(taskInstance.getProcessDefine())
                .buildResourceParametersInfo(resources)
                .buildDataQualityTaskExecutionContext(dataQualityTaskExecutionContext)
                .buildK8sTaskRelatedInfo(k8sTaskExecutionContext)
                .create();
    }

    private void setTaskResourceInfo(ResourceParametersHelper resourceParametersHelper) {
        if (Objects.isNull(resourceParametersHelper)) {
            return;
        }
        resourceParametersHelper.getResourceMap().forEach((type, map) -> {
            switch (type) {
                case DATASOURCE:
                    this.setTaskDataSourceResourceInfo(map);
                    break;
                case UDF:
                    this.setTaskUdfFuncResourceInfo(map);
                    break;
                default:
                    break;
            }
        });
    }

    private void setTaskDataSourceResourceInfo(Map<Integer, AbstractResourceParameters> map) {
        if (MapUtils.isEmpty(map)) {
            return;
        }

        map.forEach((code, parameters) -> {
            DataSource datasource = processService.findDataSourceById(code);
            if (Objects.isNull(datasource)) {
                return;
            }
            DataSourceParameters dataSourceParameters = new DataSourceParameters();
            dataSourceParameters.setType(datasource.getType());
            dataSourceParameters.setConnectionParams(datasource.getConnectionParams());
            map.put(code, dataSourceParameters);
        });

    }

    private void setTaskUdfFuncResourceInfo(Map<Integer, AbstractResourceParameters> map) {
        if (MapUtils.isEmpty(map)) {
            return;
        }
        List<UdfFunc> udfFuncList = processService.queryUdfFunListByIds(map.keySet().toArray(new Integer[map.size()]));

        udfFuncList.forEach(udfFunc -> {
            UdfFuncParameters udfFuncParameters = JSONUtils.parseObject(JSONUtils.toJsonString(udfFunc), UdfFuncParameters.class);
            udfFuncParameters.setDefaultFS(HadoopUtils.getInstance().getDefaultFS());
            String tenantCode = processService.queryTenantCodeByResName(udfFunc.getResourceName(), ResourceType.UDF);
            udfFuncParameters.setTenantCode(tenantCode);
            map.put(udfFunc.getId(), udfFuncParameters);
        });
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

        // set the path used to store data quality task check error data
        dataQualityTaskExecutionContext.setHdfsPath(
                PropertyUtils.getString(Constants.FS_DEFAULT_FS)
                + PropertyUtils.getString(
                        Constants.DATA_QUALITY_ERROR_OUTPUT_PATH,
                        "/user/" + tenantCode + "/data_quality_error_data"));

        setSourceConfig(dataQualityTaskExecutionContext, config);
        setTargetConfig(dataQualityTaskExecutionContext, config);
        setWriterConfig(dataQualityTaskExecutionContext);
        setStatisticsValueWriterConfig(dataQualityTaskExecutionContext);
    }

    /**
     * It is used to get comparison params, the param contains
     * comparison name、comparison table and execute sql.
     * When the type is fixed_value, params will be null.
     * @param dataQualityTaskExecutionContext
     * @param config
     * @param ruleInputEntryList
     * @param executeSqlList
     */
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

    /**
     * The default datasource is used to get the dolphinscheduler datasource info,
     * and the info will be used in StatisticsValueConfig and WriterConfig
     * @return DataSource
     */
    public DataSource getDefaultDataSource() {
        DataSource dataSource = new DataSource();

        HikariDataSource hikariDataSource = (HikariDataSource)defaultDataSource;
        dataSource.setUserName(hikariDataSource.getUsername());
        JdbcInfo jdbcInfo = JdbcUrlParser.getJdbcInfo(hikariDataSource.getJdbcUrl());
        if (jdbcInfo != null) {
            Properties properties = new Properties();
            properties.setProperty(USER,hikariDataSource.getUsername());
            properties.setProperty(PASSWORD,hikariDataSource.getPassword());
            properties.setProperty(DATABASE, jdbcInfo.getDatabase());
            properties.setProperty(ADDRESS,jdbcInfo.getAddress());
            properties.setProperty(OTHER,jdbcInfo.getParams());
            properties.setProperty(JDBC_URL,jdbcInfo.getAddress() + SINGLE_SLASH + jdbcInfo.getDatabase());
            dataSource.setType(DbType.of(JdbcUrlParser.getDbType(jdbcInfo.getDriverName()).getCode()));
            dataSource.setConnectionParams(JSONUtils.toJsonString(properties));
        }

        return dataSource;
    }

    /**
     * The StatisticsValueWriterConfig will be used in DataQualityApplication that
     * writes the statistics value into dolphin scheduler datasource
     * @param dataQualityTaskExecutionContext
     */
    private void setStatisticsValueWriterConfig(DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        DataSource dataSource = getDefaultDataSource();
        ConnectorType writerConnectorType = ConnectorType.of(dataSource.getType().isHive() ? 1 : 0);
        dataQualityTaskExecutionContext.setStatisticsValueConnectorType(writerConnectorType.getDescription());
        dataQualityTaskExecutionContext.setStatisticsValueType(dataSource.getType().getCode());
        dataQualityTaskExecutionContext.setStatisticsValueWriterConnectionParams(dataSource.getConnectionParams());
        dataQualityTaskExecutionContext.setStatisticsValueTable("t_ds_dq_task_statistics_value");
    }

    /**
     * The WriterConfig will be used in DataQualityApplication that
     * writes the data quality check result into dolphin scheduler datasource
     * @param dataQualityTaskExecutionContext
     */
    private void setWriterConfig(DataQualityTaskExecutionContext dataQualityTaskExecutionContext) {
        DataSource dataSource = getDefaultDataSource();
        ConnectorType writerConnectorType = ConnectorType.of(dataSource.getType().isHive() ? 1 : 0);
        dataQualityTaskExecutionContext.setWriterConnectorType(writerConnectorType.getDescription());
        dataQualityTaskExecutionContext.setWriterType(dataSource.getType().getCode());
        dataQualityTaskExecutionContext.setWriterConnectionParams(dataSource.getConnectionParams());
        dataQualityTaskExecutionContext.setWriterTable("t_ds_dq_execute_result");
    }

    /**
     * The TargetConfig will be used in DataQualityApplication that
     * get the data which be used to compare to src value
     * @param dataQualityTaskExecutionContext
     * @param config
     */
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

    /**
     * The SourceConfig will be used in DataQualityApplication that
     * get the data which be used to get the statistics value
     * @param dataQualityTaskExecutionContext
     * @param config
     */
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
        AbstractParameters baseParam = taskPluginManager.getParameters(ParametersNode.builder().taskType(taskInstance.getTaskType()).taskParams(taskInstance.getTaskParams()).build());
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

    /**
     * set k8s task relation
     * @param k8sTaskExecutionContext k8sTaskExecutionContext
     * @param taskInstance taskInstance
     */
    private void setK8sTaskRelation(K8sTaskExecutionContext k8sTaskExecutionContext, TaskInstance taskInstance) {
        K8sTaskParameters k8sTaskParameters = JSONUtils.parseObject(taskInstance.getTaskParams(), K8sTaskParameters.class);
        Map<String,String> namespace = JSONUtils.toMap(k8sTaskParameters.getNamespace());
        String clusterName = namespace.get(CLUSTER);
        String configYaml = processService.findConfigYamlByName(clusterName);
        if (configYaml != null) {
            k8sTaskExecutionContext.setConfigYaml(configYaml);
        }
    }
}
