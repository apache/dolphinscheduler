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

package org.apache.dolphinscheduler.server.master.runner;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.CLUSTER;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.NAMESPACE_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_DATA_QUALITY;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.COMPARISON_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.COMPARISON_TABLE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.COMPARISON_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.SRC_CONNECTOR_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.SRC_DATASOURCE_ID;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.TARGET_CONNECTOR_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.TARGET_DATASOURCE_ID;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.DqComparisonType;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.plugin.task.api.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ConnectorType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ExecuteSqlType;
import org.apache.dolphinscheduler.plugin.task.api.model.JdbcInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.K8sTaskParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.parameters.dataquality.DataQualityParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.AbstractResourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.UdfFuncParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.JdbcUrlParser;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.spark.SparkParameters;
import org.apache.dolphinscheduler.server.master.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.datasource.DefaultConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

@Slf4j
@Component
public class TaskExecutionContextFactory {

    @Autowired
    private ProcessService processService;

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Autowired
    private CuringParamsService curingParamsService;

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private HikariDataSource hikariDataSource;

    public TaskExecutionContext createTaskExecutionContext(TaskInstance taskInstance) {
        ProcessInstance workflowInstance = taskInstance.getProcessInstance();

        ResourceParametersHelper resources =
                Optional.ofNullable(taskPluginManager.getTaskChannel(taskInstance.getTaskType()))
                        .map(taskChannel -> taskChannel.getResources(taskInstance.getTaskParams()))
                        .orElse(null);
        setTaskResourceInfo(resources);

        Map<String, Property> businessParamsMap = curingParamsService.preBuildBusinessParams(workflowInstance);

        AbstractParameters baseParam = taskPluginManager.getParameters(ParametersNode.builder()
                .taskType(taskInstance.getTaskType()).taskParams(taskInstance.getTaskParams()).build());
        Map<String, Property> propertyMap =
                curingParamsService.paramParsingPreparation(taskInstance, baseParam, workflowInstance);
        TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                .buildWorkflowInstanceHost(masterConfig.getMasterAddress())
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildTaskDefinitionRelatedInfo(taskInstance.getTaskDefine())
                .buildProcessInstanceRelatedInfo(taskInstance.getProcessInstance())
                .buildProcessDefinitionRelatedInfo(taskInstance.getProcessDefine())
                .buildResourceParametersInfo(resources)
                .buildBusinessParamsMap(businessParamsMap)
                .buildParamInfo(propertyMap)
                .create();

        setDataQualityTaskExecutionContext(taskExecutionContext, taskInstance, workflowInstance.getTenantCode());
        setK8sTaskRelatedInfo(taskExecutionContext, taskInstance);
        return taskExecutionContext;
    }

    public void setDataQualityTaskExecutionContext(TaskExecutionContext taskExecutionContext, TaskInstance taskInstance,
                                                   String tenantCode) {
        // TODO to be optimized
        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = null;
        if (TASK_TYPE_DATA_QUALITY.equalsIgnoreCase(taskInstance.getTaskType())) {
            dataQualityTaskExecutionContext = new DataQualityTaskExecutionContext();
            setDataQualityTaskRelation(dataQualityTaskExecutionContext, taskInstance, tenantCode);
        }
        taskExecutionContext.setDataQualityTaskExecutionContext(dataQualityTaskExecutionContext);
    }

    public void setK8sTaskRelatedInfo(TaskExecutionContext taskExecutionContext, TaskInstance taskInstance) {
        K8sTaskExecutionContext k8sTaskExecutionContext = setK8sTaskRelation(taskInstance);
        taskExecutionContext.setK8sTaskExecutionContext(k8sTaskExecutionContext);
    }

    private void setTaskResourceInfo(ResourceParametersHelper resourceParametersHelper) {
        if (Objects.isNull(resourceParametersHelper)) {
            return;
        }
        resourceParametersHelper.getResourceMap().forEach((type, map) -> {
            switch (type) {
                case DATASOURCE:
                    setTaskDataSourceResourceInfo(map);
                    break;
                case UDF:
                    setTaskUdfFuncResourceInfo(map);
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
            UdfFuncParameters udfFuncParameters =
                    JSONUtils.parseObject(JSONUtils.toJsonString(udfFunc), UdfFuncParameters.class);
            map.put(udfFunc.getId(), udfFuncParameters);
        });
    }

    private void setDataQualityTaskRelation(DataQualityTaskExecutionContext dataQualityTaskExecutionContext,
                                            TaskInstance taskInstance, String tenantCode) {
        DataQualityParameters dataQualityParameters =
                JSONUtils.parseObject(taskInstance.getTaskParams(), DataQualityParameters.class);
        if (dataQualityParameters == null) {
            return;
        }

        Map<String, String> config = dataQualityParameters.getRuleInputParameter();

        int ruleId = dataQualityParameters.getRuleId();
        DqRule dqRule = processService.getDqRule(ruleId);
        if (dqRule == null) {
            log.error("Can not get dataQuality rule by id {}", ruleId);
            return;
        }

        dataQualityTaskExecutionContext.setRuleId(ruleId);
        dataQualityTaskExecutionContext.setRuleType(dqRule.getType());
        dataQualityTaskExecutionContext.setRuleName(dqRule.getName());

        List<DqRuleInputEntry> ruleInputEntryList = processService.getRuleInputEntry(ruleId);
        if (CollectionUtils.isEmpty(ruleInputEntryList)) {
            log.error("Rule input entry list is empty, ruleId: {}", ruleId);
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

    private K8sTaskExecutionContext setK8sTaskRelation(TaskInstance taskInstance) {
        K8sTaskExecutionContext k8sTaskExecutionContext = null;
        String namespace = "";
        switch (taskInstance.getTaskType()) {
            case "K8S":
            case "KUBEFLOW":
                K8sTaskParameters k8sTaskParameters =
                        JSONUtils.parseObject(taskInstance.getTaskParams(), K8sTaskParameters.class);
                namespace = k8sTaskParameters.getNamespace();
                break;
            case "SPARK":
                SparkParameters sparkParameters =
                        JSONUtils.parseObject(taskInstance.getTaskParams(), SparkParameters.class);
                if (StringUtils.isNotEmpty(sparkParameters.getNamespace())) {
                    namespace = sparkParameters.getNamespace();
                }
                break;
            default:
                break;
        }

        if (StringUtils.isNotEmpty(namespace)) {
            String clusterName = JSONUtils.toMap(namespace).get(CLUSTER);
            String configYaml = processService.findConfigYamlByName(clusterName);
            if (configYaml != null) {
                k8sTaskExecutionContext =
                        new K8sTaskExecutionContext(configYaml, JSONUtils.toMap(namespace).get(NAMESPACE_NAME));
            }
        }
        return k8sTaskExecutionContext;
    }

    /**
     * The SourceConfig will be used in DataQualityApplication that
     * get the data which be used to get the statistics value
     *
     * @param dataQualityTaskExecutionContext
     * @param config
     */
    private void setSourceConfig(DataQualityTaskExecutionContext dataQualityTaskExecutionContext,
                                 Map<String, String> config) {
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
     * It is used to get comparison params, the param contains
     * comparison name、comparison table and execute sql.
     * When the type is fixed_value, params will be null.
     *
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
                    comparisonName.setData(type.getName());
                    ruleInputEntryList.add(comparisonName);

                    DqRuleInputEntry comparisonTable = new DqRuleInputEntry();
                    comparisonTable.setField(COMPARISON_TABLE);
                    comparisonTable.setData(type.getOutputTable());
                    ruleInputEntryList.add(comparisonTable);

                    if (executeSqlList == null) {
                        executeSqlList = new ArrayList<>();
                    }

                    DqRuleExecuteSql dqRuleExecuteSql = new DqRuleExecuteSql();
                    dqRuleExecuteSql.setType(ExecuteSqlType.MIDDLE.getCode());
                    dqRuleExecuteSql.setIndex(1);
                    dqRuleExecuteSql.setSql(type.getExecuteSql());
                    dqRuleExecuteSql.setTableAlias(type.getOutputTable());
                    executeSqlList.add(0, dqRuleExecuteSql);

                    if (Boolean.TRUE.equals(type.getIsInnerSource())) {
                        dataQualityTaskExecutionContext.setComparisonNeedStatisticsValueTable(true);
                    }
                }
            } else if (comparisonTypeId == 1) {
                dataQualityTaskExecutionContext.setCompareWithFixedValue(true);
            }
        }
    }

    /**
     * The TargetConfig will be used in DataQualityApplication that
     * get the data which be used to compare to src value
     *
     * @param dataQualityTaskExecutionContext
     * @param config
     */
    private void setTargetConfig(DataQualityTaskExecutionContext dataQualityTaskExecutionContext,
                                 Map<String, String> config) {
        if (StringUtils.isNotEmpty(config.get(TARGET_DATASOURCE_ID))) {
            DataSource dataSource =
                    processService.findDataSourceById(Integer.parseInt(config.get(TARGET_DATASOURCE_ID)));
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
     * The WriterConfig will be used in DataQualityApplication that
     * writes the data quality check result into dolphin scheduler datasource
     *
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
     * The default datasource is used to get the dolphinscheduler datasource info,
     * and the info will be used in StatisticsValueConfig and WriterConfig
     *
     * @return DataSource
     */
    public DataSource getDefaultDataSource() {
        DataSource dataSource = new DataSource();

        dataSource.setUserName(hikariDataSource.getUsername());
        JdbcInfo jdbcInfo = JdbcUrlParser.getJdbcInfo(hikariDataSource.getJdbcUrl());
        if (jdbcInfo != null) {
            //
            BaseConnectionParam baseConnectionParam = new DefaultConnectionParam();
            baseConnectionParam.setUser(hikariDataSource.getUsername());
            baseConnectionParam.setPassword(hikariDataSource.getPassword());
            baseConnectionParam.setDatabase(jdbcInfo.getDatabase());
            baseConnectionParam.setAddress(jdbcInfo.getAddress());
            baseConnectionParam.setJdbcUrl(jdbcInfo.getJdbcUrl());
            baseConnectionParam.setOther(jdbcInfo.getParams());
            dataSource.setType(DbType.of(JdbcUrlParser.getDbType(jdbcInfo.getDriverName()).getCode()));
            dataSource.setConnectionParams(JSONUtils.toJsonString(baseConnectionParam));
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

}
