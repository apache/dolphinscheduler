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

package org.apache.dolphinscheduler.server.worker.task.dq;

import static org.apache.dolphinscheduler.common.Constants.COMPARISON_TABLE;
import static org.apache.dolphinscheduler.common.Constants.SRC_FIELD;

import org.apache.dolphinscheduler.common.enums.dq.ExecuteSqlType;
import org.apache.dolphinscheduler.common.enums.dq.FormType;
import org.apache.dolphinscheduler.common.enums.dq.InputType;
import org.apache.dolphinscheduler.common.enums.dq.OptionSourceType;
import org.apache.dolphinscheduler.common.enums.dq.RuleType;
import org.apache.dolphinscheduler.common.enums.dq.ValueType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.server.entity.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.dq.rule.RuleManager;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * DataQualityTaskTest
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class DataQualityTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(DataQualityTaskTest.class);

    /**
     * Method: init
     */
    @Test
    public void testInit() {
        try {
            ProcessService processService = Mockito.mock(ProcessService.class);
            ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
            SpringApplicationContext springApplicationContext = new SpringApplicationContext();
            springApplicationContext.setApplicationContext(applicationContext);
            Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);
            TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
            taskExecutionContext.setTaskAppId(String.valueOf(System.currentTimeMillis()));
            taskExecutionContext.setTenantCode("1");
            taskExecutionContext.setEnvFile(".dolphinscheduler_env.sh");
            taskExecutionContext.setStartTime(new Date());
            taskExecutionContext.setTaskTimeout(0);
            taskExecutionContext.setTaskParams("{\"ruleId\":10,"
                    + "\"localParams\":[],"
                    + "\"sparkParameters\":{\"deployMode\":\"cluster\",\"driverCores\":1,"
                    + "\"driverMemory\":\"512M\",\"numExecutors\":2,"
                    + "\"executorMemory\":\"2G\",\"executorCores\":2,"
                    + "\"others\":\"--conf spark.yarn.maxAppAttempts=1 \"},"
                    + "\"ruleInputParameter\":{\"src_connector_type\":0,"
                    + "\"src_datasource_id\":2,\"src_table\":\"dqs_result\","
                    + "\"src_filter\":null,\"check_type\":\"0\","
                    + "\"operator\":\"3\",\"threshold\":\"1\","
                    + "\"failure_strategy\":\"0\",\"comparison_type\":1,"
                    + "\"comparison_name\":\"10\"}}");
            taskExecutionContext.setDataQualityTaskExecutionContext(getSingleTableContext());
            DataQualityTask dataQualityTask = new DataQualityTask(taskExecutionContext, logger);
            dataQualityTask.init();
            dataQualityTask.buildCommand();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSingleTable() throws Exception {
        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = getSingleTableContext();

        Map<String,String> inputParameterValue = new HashMap<>();
        inputParameterValue.put("src_connector_type","0");
        inputParameterValue.put("src_datasource_id","2");
        inputParameterValue.put("src_table","src_result");
        inputParameterValue.put("check_type","0");
        inputParameterValue.put("operator","3");
        inputParameterValue.put("threshold","1");
        inputParameterValue.put("failure_strategy","0");
        inputParameterValue.put("comparison_type","1");
        inputParameterValue.put("comparison_name","10");
        inputParameterValue.put("rule_id","10");
        inputParameterValue.put("rule_type","0");
        inputParameterValue.put("rule_name","'表行数校验'");
        inputParameterValue.put("create_time","'2021-08-12 10:15:48'");
        inputParameterValue.put("update_time","'2021-08-12 10:15:48'");
        inputParameterValue.put("process_definition_id","21");
        inputParameterValue.put("process_instance_id","284");
        inputParameterValue.put("task_instance_id","287");
        inputParameterValue.put("data_time","'2021-08-12 10:15:48'");
        inputParameterValue.put("error_output_path","hdfs://192.168.0.1:8022/user/ods/data_quality_error_data/21_284_287");

        RuleManager ruleManager = new RuleManager(inputParameterValue,dataQualityTaskExecutionContext);
        String expect = "{\"name\":\"表行数校验\",\"env\":{\"type\":\"batch\",\"config\":null},"
                + "\"readers\":[{\"type\":\"JDBC\",\"config\":"
                + "{\"database\":\"test\",\"password\":\"test\",\"driver\":\"com.mysql.jdbc.Driver\","
                + "\"user\":\"test\",\"output_table\":\"test_src_result\",\"table\":\"src_result\","
                + "\"url\":\"jdbc:mysql://localhost:3306/test?autoReconnect=true\"}}],"
                + "\"transformers\":[{\"type\":\"sql\",\"config\":{\"index\":1,"
                + "\"output_table\":\"table_count\",\"sql\":\"SELECT COUNT(*) AS total FROM test_src_result \"}}],"
                + "\"writers\":[{\"type\":\"JDBC\",\"config\":{\"database\":\"test\",\"password\":\"test\","
                + "\"driver\":\"com.mysql.jdbc.Driver\",\"user\":\"test\",\"table\":\"dqc_result\","
                + "\"url\":\"jdbc:mysql://localhost:3306/test?autoReconnect=true\","
                + "\"sql\":\"select 0 as rule_type,'表行数校验' as rule_name,21 as process_definition_id,284 as process_instance_id,"
                + "287 as task_instance_id,table_count.total AS statistics_value,10 AS comparison_value,1 AS comparison_type,"
                + "0 as check_type,1 as threshold,3 as operator,0 as failure_strategy,"
                + "'hdfs://192.168.0.1:8022/user/ods/data_quality_error_data/21_284_287' as error_output_path,"
                + "'2021-08-12 10:15:48' as create_time,'2021-08-12 10:15:48' as update_time from table_count \"}},"
                + "{\"type\":\"JDBC\",\"config\":{\"database\":\"test\",\"password\":\"test\",\"driver\":\"com.mysql.jdbc.Driver\","
                + "\"user\":\"test\",\"table\":\"dqc_statistics_value\",\"url\":\"jdbc:mysql://localhost:3306/test?autoReconnect=true\","
                + "\"sql\":\"select 21 as process_definition_id,287 as task_instance_id,10 as rule_id,'DN/MS5NLTSLVZ/++KEJ9BHPQSEN6/UY/EV5TWI1IRRY=' "
                + "as unique_code,'table_count.total'AS statistics_name,"
                + "table_count.total AS statistics_value,'2021-08-12 10:15:48' as data_time,'2021-08-12 10:15:48' as create_time,"
                + "'2021-08-12 10:15:48' as update_time from table_count\"}}]}";
        Assert.assertEquals(expect,JSONUtils.toJsonString(ruleManager.generateDataQualityParameter()));
    }

    private DataQualityTaskExecutionContext getSingleTableContext() {
        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = new DataQualityTaskExecutionContext();

        dataQualityTaskExecutionContext.setRuleName("表行数校验");
        dataQualityTaskExecutionContext.setRuleType(RuleType.SINGLE_TABLE);

        List<DqRuleInputEntry> defaultInputEntryList = new ArrayList<>();

        DqRuleInputEntry srcConnectorType = new DqRuleInputEntry();
        srcConnectorType.setTitle("源数据类型");
        srcConnectorType.setField("src_connector_type");
        srcConnectorType.setType(FormType.SELECT);
        srcConnectorType.setCanEdit(true);
        srcConnectorType.setShow(true);
        srcConnectorType.setValue(null);
        srcConnectorType.setPlaceholder("${src_connector_type}");
        srcConnectorType.setOptionSourceType(OptionSourceType.DATASOURCE_TYPE);
        srcConnectorType.setOptions(null);
        srcConnectorType.setInputType(InputType.DEFAULT);
        srcConnectorType.setValueType(ValueType.NUMBER);
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry srcDatasourceId = new DqRuleInputEntry();
        srcDatasourceId.setTitle("源数据源");
        srcDatasourceId.setField("src_datasource_id");
        srcDatasourceId.setType(FormType.CASCADER);
        srcDatasourceId.setCanEdit(true);
        srcDatasourceId.setShow(true);
        srcDatasourceId.setValue(null);
        srcDatasourceId.setOptionSourceType(OptionSourceType.DATASOURCE_ID);
        srcDatasourceId.setInputType(InputType.DEFAULT);
        srcDatasourceId.setValueType(ValueType.NUMBER);
        srcDatasourceId.setCreateTime(new Date());
        srcDatasourceId.setUpdateTime(new Date());

        DqRuleInputEntry srcTable = new DqRuleInputEntry();
        srcTable.setTitle("源数据表");
        srcTable.setField("src_table");
        srcTable.setType(FormType.INPUT);
        srcTable.setCanEdit(true);
        srcTable.setShow(true);
        srcTable.setPlaceholder("Please enter source table name");
        srcTable.setOptionSourceType(OptionSourceType.DEFAULT);
        srcTable.setInputType(InputType.DEFAULT);
        srcTable.setValueType(ValueType.STRING);
        srcTable.setCreateTime(new Date());
        srcTable.setUpdateTime(new Date());

        DqRuleInputEntry srcFilter = new DqRuleInputEntry();
        srcFilter.setTitle("源表过滤条件");
        srcFilter.setField("src_filter");
        srcFilter.setType(FormType.INPUT);
        srcFilter.setCanEdit(true);
        srcFilter.setShow(true);
        srcFilter.setPlaceholder("Please enter filter expression");
        srcFilter.setOptionSourceType(OptionSourceType.DEFAULT);
        srcFilter.setInputType(InputType.DEFAULT);
        srcFilter.setValueType(ValueType.LIKE_SQL);
        srcFilter.setCreateTime(new Date());
        srcFilter.setUpdateTime(new Date());

        DqRuleInputEntry srcField = new DqRuleInputEntry();
        srcField.setTitle("检测列");
        srcField.setField(SRC_FIELD);
        srcField.setType(FormType.INPUT);
        srcField.setCanEdit(true);
        srcField.setShow(true);
        srcField.setValue("");
        srcField.setPlaceholder("Please enter column, only single column is supported");
        srcField.setOptionSourceType(OptionSourceType.DEFAULT);
        srcField.setInputType(InputType.DEFAULT);
        srcField.setValueType(ValueType.STRING);
        srcField.setCreateTime(new Date());
        srcField.setUpdateTime(new Date());

        DqRuleInputEntry statisticsName = new DqRuleInputEntry();
        statisticsName.setTitle("统计值");
        statisticsName.setField("statistics_name");
        statisticsName.setType(FormType.INPUT);
        statisticsName.setCanEdit(false);
        statisticsName.setShow(false);
        statisticsName.setValue("table_count.total");
        statisticsName.setPlaceholder("${statistics_name}");
        statisticsName.setOptionSourceType(OptionSourceType.DEFAULT);
        statisticsName.setInputType(InputType.STATISTICS);
        statisticsName.setValueType(ValueType.STRING);
        statisticsName.setCreateTime(new Date());
        statisticsName.setUpdateTime(new Date());

        DqRuleInputEntry checkType = new DqRuleInputEntry();
        checkType.setTitle("检测方式");
        checkType.setField("check_type");
        checkType.setType(FormType.SELECT);
        checkType.setCanEdit(true);
        checkType.setShow(true);
        checkType.setOptionSourceType(OptionSourceType.DEFAULT);
        checkType.setOptions("[{\"label\":\"比对值 - 统计值\",\"value\":\"0\"},{\"label\":\"统计值 - 比对值\",\"value\":\"1\"},{\"label\":\"统计值 / 比对值\","
                + "\"value\":\"2\"},{\"label\":\"(比对值-统计值) / 比对值\",\"value\":\"3\"}]");
        checkType.setValue("0");
        checkType.setInputType(InputType.CHECK);
        checkType.setValueType(ValueType.STRING);
        checkType.setPlaceholder("检测类型");
        checkType.setCreateTime(new Date());
        checkType.setUpdateTime(new Date());

        DqRuleInputEntry operator = new DqRuleInputEntry();
        operator.setTitle("操作符");
        operator.setField("operator");
        operator.setType(FormType.SELECT);
        operator.setCanEdit(true);
        operator.setShow(true);
        operator.setOptionSourceType(OptionSourceType.DEFAULT);
        operator.setOptions("[{\"label\":\"=\",\"value\":\"0\"},"
                + "{\"label\":\"<\",\"value\":\"1\"},{\"label\":\"<=\",\"value\":\"2\"},"
                + "{\"label\":\">\",\"value\":\"3\"},{\"label\":\">=\",\"value\":\"4\"},"
                + "{\"label\":\"!=\",\"value\":\"5\"}]");
        operator.setValue("0");
        operator.setInputType(InputType.CHECK);
        operator.setValueType(ValueType.STRING);
        operator.setPlaceholder("操作符");
        operator.setCreateTime(new Date());
        operator.setUpdateTime(new Date());

        DqRuleInputEntry threshold = new DqRuleInputEntry();
        threshold.setTitle("阈值");
        threshold.setField("threshold");
        threshold.setType(FormType.INPUT);
        threshold.setCanEdit(true);
        threshold.setShow(true);
        threshold.setPlaceholder("Please enter threshold, number is needed");
        threshold.setInputType(InputType.CHECK);
        threshold.setValueType(ValueType.NUMBER);
        threshold.setCreateTime(new Date());
        threshold.setUpdateTime(new Date());

        DqRuleInputEntry afterFailure = new DqRuleInputEntry();
        afterFailure.setTitle("失败策略");
        afterFailure.setField("failure_strategy");
        afterFailure.setType(FormType.SELECT);
        afterFailure.setCanEdit(true);
        afterFailure.setShow(true);
        afterFailure.setOptionSourceType(OptionSourceType.DEFAULT);
        afterFailure.setOptions("[{\"label\":\"告警\",\"value\":\"0\"},{\"label\":\"阻断\",\"value\":\"1\"}]");
        afterFailure.setValue("0");
        afterFailure.setInputType(InputType.CHECK);
        afterFailure.setValueType(ValueType.STRING);
        afterFailure.setPlaceholder("失败策略");
        afterFailure.setCreateTime(new Date());
        afterFailure.setUpdateTime(new Date());

        defaultInputEntryList.add(checkType);
        defaultInputEntryList.add(operator);
        defaultInputEntryList.add(threshold);
        defaultInputEntryList.add(afterFailure);

        defaultInputEntryList.add(srcConnectorType);
        defaultInputEntryList.add(srcDatasourceId);
        defaultInputEntryList.add(srcTable);
        defaultInputEntryList.add(srcFilter);
        defaultInputEntryList.add(srcField);
        defaultInputEntryList.add(statisticsName);

        DqRuleExecuteSql executeSqlDefinition3 = new DqRuleExecuteSql();
        executeSqlDefinition3.setIndex(0);
        executeSqlDefinition3.setSql("SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})");
        executeSqlDefinition3.setTableAlias("table_count");
        executeSqlDefinition3.setType(ExecuteSqlType.STATISTICS);
        dataQualityTaskExecutionContext.addExecuteSql(executeSqlDefinition3);

        dataQualityTaskExecutionContext.setRuleInputEntryList(defaultInputEntryList);
        dataQualityTaskExecutionContext.setSourceConnectorType("JDBC");
        dataQualityTaskExecutionContext.setSourceType(0);
        dataQualityTaskExecutionContext.setSourceConnectionParams(
                "{\"address\":\"jdbc:mysql://localhost:3306\","
                        + "\"database\":\"test\","
                        + "\"jdbcUrl\":\"jdbc:mysql://localhost:3306/test\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\","
                        + "\"other\":\"autoReconnect=true\"}");

        dataQualityTaskExecutionContext.setWriterType(0);
        dataQualityTaskExecutionContext.setWriterConnectorType("JDBC");
        dataQualityTaskExecutionContext.setWriterTable("dqc_result");
        dataQualityTaskExecutionContext.setWriterConnectionParams(
                "{\"address\":\"jdbc:mysql://localhost:3306\","
                        + "\"database\":\"test\","
                        + "\"jdbcUrl\":\"jdbc:mysql://localhost:3306/test\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\","
                        + "\"other\":\"autoReconnect=true\"}");

        dataQualityTaskExecutionContext.setStatisticsValueConnectorType("JDBC");
        dataQualityTaskExecutionContext.setStatisticsValueType(0);
        dataQualityTaskExecutionContext.setStatisticsValueTable("dqc_statistics_value");
        dataQualityTaskExecutionContext.setStatisticsValueWriterConnectionParams(
                "{\"address\":\"jdbc:mysql://localhost:3306\","
                + "\"database\":\"test\","
                + "\"jdbcUrl\":\"jdbc:mysql://localhost:3306/test\","
                + "\"user\":\"test\","
                + "\"password\":\"test\","
                + "\"other\":\"autoReconnect=true\"}");

        dataQualityTaskExecutionContext.setCompareWithFixedValue(true);
        return dataQualityTaskExecutionContext;
    }

    @Test
    public void testSingleTableCustomSql() throws Exception {
        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = new DataQualityTaskExecutionContext();

        dataQualityTaskExecutionContext.setRuleName("自定义SQL");
        dataQualityTaskExecutionContext.setRuleType(RuleType.SINGLE_TABLE_CUSTOM_SQL);

        List<DqRuleInputEntry> defaultInputEntryList = new ArrayList<>();

        DqRuleInputEntry srcConnectorType = new DqRuleInputEntry();
        srcConnectorType.setTitle("源数据类型");
        srcConnectorType.setField("src_connector_type");
        srcConnectorType.setType(FormType.SELECT);
        srcConnectorType.setCanEdit(true);
        srcConnectorType.setShow(true);
        srcConnectorType.setValue(null);
        srcConnectorType.setPlaceholder("${src_connector_type}");
        srcConnectorType.setOptionSourceType(OptionSourceType.DATASOURCE_TYPE);
        srcConnectorType.setOptions(null);
        srcConnectorType.setInputType(InputType.DEFAULT);
        srcConnectorType.setValueType(ValueType.NUMBER);
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry srcDatasourceId = new DqRuleInputEntry();
        srcDatasourceId.setTitle("源数据源");
        srcDatasourceId.setField("src_datasource_id");
        srcDatasourceId.setType(FormType.CASCADER);
        srcDatasourceId.setCanEdit(true);
        srcDatasourceId.setShow(true);
        srcDatasourceId.setValue(null);
        srcDatasourceId.setPlaceholder("${comparison_value}");
        srcDatasourceId.setOptionSourceType(OptionSourceType.DATASOURCE_ID);
        srcDatasourceId.setInputType(InputType.DEFAULT);
        srcDatasourceId.setValueType(ValueType.NUMBER);
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry srcTable = new DqRuleInputEntry();
        srcTable.setTitle("源数据表");
        srcTable.setField("src_table");
        srcTable.setType(FormType.INPUT);
        srcTable.setCanEdit(true);
        srcTable.setShow(true);
        srcTable.setPlaceholder("Please enter source table name");
        srcTable.setOptionSourceType(OptionSourceType.DEFAULT);
        srcTable.setInputType(InputType.DEFAULT);
        srcTable.setValueType(ValueType.STRING);
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry srcFilter = new DqRuleInputEntry();
        srcFilter.setTitle("源表过滤条件");
        srcFilter.setField("src_filter");
        srcFilter.setType(FormType.INPUT);
        srcFilter.setCanEdit(true);
        srcFilter.setShow(true);
        srcFilter.setPlaceholder("Please enter source filter expression");
        srcFilter.setOptionSourceType(OptionSourceType.DEFAULT);
        srcFilter.setInputType(InputType.DEFAULT);
        srcFilter.setValueType(ValueType.LIKE_SQL);

        DqRuleInputEntry statisticsName = new DqRuleInputEntry();
        statisticsName.setTitle("统计值名");
        statisticsName.setField("statistics_name");
        statisticsName.setType(FormType.INPUT);
        statisticsName.setCanEdit(true);
        statisticsName.setShow(true);
        statisticsName.setPlaceholder("Please enter statistics name, the alias in statistics execute sql");
        statisticsName.setOptionSourceType(OptionSourceType.DEFAULT);
        statisticsName.setInputType(InputType.DEFAULT);
        statisticsName.setValueType(ValueType.STRING);

        DqRuleInputEntry statisticsExecuteSql = new DqRuleInputEntry();
        statisticsExecuteSql.setTitle("统计值计算SQL");
        statisticsExecuteSql.setField("statistics_execute_sql");
        statisticsExecuteSql.setType(FormType.TEXTAREA);
        statisticsExecuteSql.setCanEdit(true);
        statisticsExecuteSql.setShow(true);
        statisticsExecuteSql.setPlaceholder("Please enter the statistics execute sql");
        statisticsExecuteSql.setOptionSourceType(OptionSourceType.DEFAULT);
        statisticsExecuteSql.setValueType(ValueType.LIKE_SQL);

        DqRuleInputEntry checkType = new DqRuleInputEntry();
        checkType.setTitle("检测方式");
        checkType.setField("check_type");
        checkType.setType(FormType.SELECT);
        checkType.setCanEdit(true);
        checkType.setShow(true);
        checkType.setOptionSourceType(OptionSourceType.DEFAULT);
        checkType.setOptions("[{\"label\":\"比对值 - 统计值\",\"value\":\"0\"},{\"label\":\"统计值 - 比对值\",\"value\":\"1\"},"
                + "{\"label\":\"统计值 / 比对值\",\"value\":\"2\"},{\"label\":\"(比对值-统计值) / 比对值\",\"value\":\"3\"}]");
        checkType.setValue("0");
        checkType.setInputType(InputType.CHECK);
        checkType.setValueType(ValueType.STRING);
        checkType.setPlaceholder("检测类型");

        DqRuleInputEntry operator = new DqRuleInputEntry();
        operator.setTitle("操作符");
        operator.setField("operator");
        operator.setType(FormType.SELECT);
        operator.setCanEdit(true);
        operator.setShow(true);
        operator.setOptionSourceType(OptionSourceType.DEFAULT);
        operator.setOptions("[{\"label\":\"=\",\"value\":\"0\"},"
                + "{\"label\":\"<\",\"value\":\"1\"},{\"label\":\"<=\",\"value\":\"2\"},"
                + "{\"label\":\">\",\"value\":\"3\"},{\"label\":\">=\",\"value\":\"4\"},"
                + "{\"label\":\"!=\",\"value\":\"5\"}]");
        operator.setValue("0");
        operator.setInputType(InputType.CHECK);
        operator.setValueType(ValueType.STRING);
        operator.setPlaceholder("操作符");

        DqRuleInputEntry threshold = new DqRuleInputEntry();
        threshold.setTitle("阈值");
        threshold.setField("threshold");
        threshold.setType(FormType.INPUT);
        threshold.setCanEdit(true);
        threshold.setShow(true);
        threshold.setPlaceholder("Please enter threshold value, number is needed");
        threshold.setInputType(InputType.CHECK);
        threshold.setValueType(ValueType.NUMBER);

        DqRuleInputEntry afterFailure = new DqRuleInputEntry();
        afterFailure.setTitle("失败策略");
        afterFailure.setField("failure_strategy");
        afterFailure.setType(FormType.SELECT);
        afterFailure.setCanEdit(true);
        afterFailure.setShow(true);
        afterFailure.setOptionSourceType(OptionSourceType.DEFAULT);
        afterFailure.setOptions("[{\"label\":\"告警\",\"value\":\"0\"},{\"label\":\"阻断\",\"value\":\"1\"}]");
        afterFailure.setValue("0");
        afterFailure.setInputType(InputType.CHECK);
        afterFailure.setValueType(ValueType.STRING);
        afterFailure.setPlaceholder("失败策略");

        defaultInputEntryList.add(checkType);
        defaultInputEntryList.add(operator);
        defaultInputEntryList.add(threshold);
        defaultInputEntryList.add(afterFailure);
        defaultInputEntryList.add(srcConnectorType);
        defaultInputEntryList.add(srcDatasourceId);
        defaultInputEntryList.add(srcTable);
        defaultInputEntryList.add(statisticsName);
        defaultInputEntryList.add(statisticsExecuteSql);
        defaultInputEntryList.add(srcFilter);

        dataQualityTaskExecutionContext.setRuleInputEntryList(defaultInputEntryList);

        Map<String,String> inputParameterValue = new HashMap<>();
        inputParameterValue.put("src_connector_type","0");
        inputParameterValue.put("src_datasource_id","2");
        inputParameterValue.put("src_table","person");
        inputParameterValue.put("statistics_name","miss");
        inputParameterValue.put("statistics_execute_sql","select count(*) as miss from ${src_table} where (sex = null or sex='') and age=1");
        inputParameterValue.put("src_filter","age=1");
        inputParameterValue.put("check_type","2");
        inputParameterValue.put("operator","3");
        inputParameterValue.put("threshold","50");
        inputParameterValue.put("failure_strategy","1");
        inputParameterValue.put("comparison_type","1");
        inputParameterValue.put("comparison_name","3");
        inputParameterValue.put("rule_id","1");
        inputParameterValue.put("rule_type","1");
        inputParameterValue.put("rule_name","'自定义SQL'");
        inputParameterValue.put("create_time","'2021-08-30 00:00:00'");
        inputParameterValue.put("update_time","'2021-08-30 00:00:00'");
        inputParameterValue.put("process_definition_id","1");
        inputParameterValue.put("process_instance_id","1");
        inputParameterValue.put("task_instance_id","1");
        inputParameterValue.put("data_time","'2021-08-30 00:00:00'");
        inputParameterValue.put("error_output_path","hdfs://localhost:8022/user/ods/data_quality_error_data/1_1_test2");

        dataQualityTaskExecutionContext.setRuleInputEntryList(defaultInputEntryList);
        dataQualityTaskExecutionContext.setSourceConnectorType("JDBC");
        dataQualityTaskExecutionContext.setSourceType(0);
        dataQualityTaskExecutionContext.setSourceConnectionParams(
                "{\"address\":\"jdbc:mysql://localhost:3306\","
                        + "\"database\":\"test\","
                        + "\"jdbcUrl\":\"jdbc:mysql://localhost:3306/test\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\"}");

        dataQualityTaskExecutionContext.setWriterType(1);
        dataQualityTaskExecutionContext.setWriterConnectorType("JDBC");
        dataQualityTaskExecutionContext.setWriterTable("t_ds_dq_execute_result");
        dataQualityTaskExecutionContext.setWriterConnectionParams(
                "{\"address\":\"jdbc:postgresql://localhost:5432\","
                        + "\"database\":\"dolphinscheduler\","
                        + "\"jdbcUrl\":\"jdbc:postgresql://localhost:5432/dolphinscheduler\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\","
                        + "\"other\":\"stringtype=unspecified&characterEncoding=UTF-8&allowMultiQueries=true\"}");

        dataQualityTaskExecutionContext.setStatisticsValueConnectorType("JDBC");
        dataQualityTaskExecutionContext.setStatisticsValueType(1);
        dataQualityTaskExecutionContext.setStatisticsValueTable("t_ds_dq_task_statistics_value");
        dataQualityTaskExecutionContext.setStatisticsValueWriterConnectionParams(
                "{\"address\":\"jdbc:postgresql://localhost:5432\","
                        + "\"database\":\"dolphinscheduler\","
                        + "\"jdbcUrl\":\"jdbc:postgresql://localhost:5432/dolphinscheduler\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\","
                        + "\"other\":\"stringtype=unspecified&characterEncoding=UTF-8&allowMultiQueries=true\"}");

        dataQualityTaskExecutionContext.setCompareWithFixedValue(true);

        RuleManager ruleManager = new RuleManager(inputParameterValue,dataQualityTaskExecutionContext);
        String expect = "{\"name\":\"自定义SQL\",\"env\":{\"type\":\"batch\",\"config\":null},\"readers\":[{\"type\":\"JDBC\","
                + "\"config\":{\"database\":\"test\",\"password\":\"test\",\"driver\":\"com.mysql.jdbc.Driver\",\"user\":"
                + "\"test\",\"output_table\":\"test_person\",\"table\":\"person\",\"url\":"
                + "\"jdbc:mysql://localhost:3306/test\"}}],\"transformers\":[{\"type\":\"sql\",\"config\":"
                + "{\"index\":2,\"output_table\":\"test_person\",\"sql\":\"select count(*) as "
                + "miss from test_person where (sex = null or sex='') and age=1\"}}],\"writers\":"
                + "[{\"type\":\"JDBC\",\"config\":{\"database\":\"dolphinscheduler\",\"password\":"
                + "\"test\",\"driver\":\"org.postgresql.Driver\",\"user\":\"test\",\"table\":"
                + "\"t_ds_dq_execute_result\",\"url\":"
                + "\"jdbc:postgresql://localhost:5432/dolphinscheduler?stringtype=unspecified&characterEncoding"
                + "=UTF-8&allowMultiQueries=true\",\"sql\":\"select 1 as rule_type,'自定义SQL' as rule_name,1 "
                + "as process_definition_id,1 as process_instance_id,1 as task_instance_id,miss AS "
                + "statistics_value,3 AS comparison_value,1 AS comparison_type,2 as check_type,50 as "
                + "threshold,3 as operator,1 as failure_strategy,'hdfs://localhost:8022/user/ods/"
                + "data_quality_error_data/1_1_test2' as error_output_path,'2021-08-30 00:00:00' as "
                + "create_time,'2021-08-30 00:00:00' as update_time from ( test_person ) tmp1 \"}},"
                + "{\"type\":\"JDBC\",\"config\":{\"database\":\"dolphinscheduler\",\"password\":\"test\",\"driver\":"
                + "\"org.postgresql.Driver\",\"user\":\"test\",\"table\":\"t_ds_dq_task_statistics_value\",\"url\":"
                + "\"jdbc:postgresql://localhost:5432/dolphinscheduler?stringtype=unspecified&characterEncoding="
                + "UTF-8&allowMultiQueries=true\",\"sql\":\"select 1 as process_definition_id,1 as "
                + "task_instance_id,1 as rule_id,'FNWZLNCPWWF4ZWKO/LYENOPL6JPV1SHPPWQ9YSYLOCU=' as unique_code,'miss'AS statistics_name,miss AS statistics_value,"
                + "'2021-08-30 00:00:00' as data_time,'2021-08-30 00:00:00' as create_time,'2021-08-30 00:00:00' "
                + "as update_time from test_person\"}}]}";

        Assert.assertEquals(expect,JSONUtils.toJsonString(ruleManager.generateDataQualityParameter()));
    }

    @Test
    public void testMultiTableComparison() throws Exception {
        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = new DataQualityTaskExecutionContext();
        dataQualityTaskExecutionContext.setRuleName("跨表值比对");
        dataQualityTaskExecutionContext.setRuleType(RuleType.MULTI_TABLE_COMPARISON);

        List<DqRuleInputEntry> defaultInputEntryList = new ArrayList<>();

        DqRuleInputEntry srcConnectorType = new DqRuleInputEntry();
        srcConnectorType.setTitle("源数据类型");
        srcConnectorType.setField("src_connector_type");
        srcConnectorType.setType(FormType.SELECT);
        srcConnectorType.setCanEdit(true);
        srcConnectorType.setShow(true);
        srcConnectorType.setValue(null);
        srcConnectorType.setPlaceholder("${src_connector_type}");
        srcConnectorType.setOptionSourceType(OptionSourceType.DATASOURCE_TYPE);
        srcConnectorType.setOptions(null);
        srcConnectorType.setInputType(InputType.DEFAULT);
        srcConnectorType.setValueType(ValueType.NUMBER);
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry srcDatasourceId = new DqRuleInputEntry();
        srcDatasourceId.setTitle("源数据源");
        srcDatasourceId.setField("src_datasource_id");
        srcDatasourceId.setType(FormType.CASCADER);
        srcDatasourceId.setCanEdit(true);
        srcDatasourceId.setShow(true);
        srcDatasourceId.setValue(null);
        srcDatasourceId.setPlaceholder("${comparison_value}");
        srcDatasourceId.setOptionSourceType(OptionSourceType.DATASOURCE_ID);
        srcDatasourceId.setInputType(InputType.DEFAULT);
        srcDatasourceId.setValueType(ValueType.NUMBER);
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry srcTable = new DqRuleInputEntry();
        srcTable.setTitle("源数据表");
        srcTable.setField("src_table");
        srcTable.setType(FormType.INPUT);
        srcTable.setCanEdit(true);
        srcTable.setShow(true);
        srcTable.setPlaceholder("Please enter source table name");
        srcTable.setOptionSourceType(OptionSourceType.DEFAULT);
        srcTable.setInputType(InputType.DEFAULT);
        srcTable.setValueType(ValueType.STRING);
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry statisticsName = new DqRuleInputEntry();
        statisticsName.setTitle("统计值名");
        statisticsName.setField("statistics_name");
        statisticsName.setType(FormType.INPUT);
        statisticsName.setCanEdit(true);
        statisticsName.setShow(true);
        statisticsName.setPlaceholder("Please enter statistics name, the alias in statistics execute sql");
        statisticsName.setOptionSourceType(OptionSourceType.DEFAULT);
        statisticsName.setValueType(ValueType.STRING);
        statisticsName.setInputType(InputType.DEFAULT);

        DqRuleInputEntry statisticsExecuteSql = new DqRuleInputEntry();
        statisticsExecuteSql.setTitle("统计值计算SQL");
        statisticsExecuteSql.setField("statistics_execute_sql");
        statisticsExecuteSql.setType(FormType.TEXTAREA);
        statisticsExecuteSql.setCanEdit(true);
        statisticsExecuteSql.setShow(true);
        statisticsExecuteSql.setPlaceholder("Please enter statistics execute sql");
        statisticsExecuteSql.setOptionSourceType(OptionSourceType.DEFAULT);
        statisticsExecuteSql.setValueType(ValueType.LIKE_SQL);
        statisticsExecuteSql.setInputType(InputType.DEFAULT);

        DqRuleInputEntry targetConnectorType = new DqRuleInputEntry();
        targetConnectorType.setTitle("目标数据类型");
        targetConnectorType.setField("target_connector_type");
        targetConnectorType.setType(FormType.SELECT);
        targetConnectorType.setCanEdit(true);
        targetConnectorType.setShow(true);
        targetConnectorType.setValue("JDBC");
        targetConnectorType.setPlaceholder("Please select target connector type");
        targetConnectorType.setOptionSourceType(OptionSourceType.DATASOURCE_TYPE);
        targetConnectorType.setOptions(null);
        targetConnectorType.setInputType(InputType.DEFAULT);

        DqRuleInputEntry targetDatasourceId = new DqRuleInputEntry();
        targetDatasourceId.setTitle("目标数据源");
        targetDatasourceId.setField("target_datasource_id");
        targetDatasourceId.setType(FormType.SELECT);
        targetDatasourceId.setCanEdit(true);
        targetDatasourceId.setShow(true);
        targetDatasourceId.setValue("1");
        targetDatasourceId.setPlaceholder("Please select target datasource");
        targetDatasourceId.setOptionSourceType(OptionSourceType.DATASOURCE_ID);
        targetDatasourceId.setInputType(InputType.DEFAULT);

        DqRuleInputEntry targetTable = new DqRuleInputEntry();
        targetTable.setTitle("目标数据表");
        targetTable.setField("target_table");
        targetTable.setType(FormType.INPUT);
        targetTable.setCanEdit(true);
        targetTable.setShow(true);
        targetTable.setPlaceholder("Please enter target table");
        targetTable.setOptionSourceType(OptionSourceType.DEFAULT);
        targetTable.setValueType(ValueType.STRING);
        targetTable.setInputType(InputType.DEFAULT);

        DqRuleInputEntry comparisonName = new DqRuleInputEntry();
        comparisonName.setTitle("比对值名");
        comparisonName.setField("comparison_name");
        comparisonName.setType(FormType.INPUT);
        comparisonName.setCanEdit(true);
        comparisonName.setShow(true);
        comparisonName.setPlaceholder("Please enter comparison name, the alias in comparison execute sql");
        comparisonName.setOptionSourceType(OptionSourceType.DEFAULT);
        comparisonName.setValueType(ValueType.STRING);
        comparisonName.setInputType(InputType.DEFAULT);

        DqRuleInputEntry comparisonExecuteSql = new DqRuleInputEntry();
        comparisonExecuteSql.setTitle("比对值计算SQL");
        comparisonExecuteSql.setField("comparison_execute_sql");
        comparisonExecuteSql.setType(FormType.TEXTAREA);
        comparisonExecuteSql.setCanEdit(true);
        comparisonExecuteSql.setShow(true);
        comparisonExecuteSql.setPlaceholder("Please enter comparison execute sql");
        comparisonExecuteSql.setOptionSourceType(OptionSourceType.DEFAULT);
        comparisonExecuteSql.setValueType(ValueType.LIKE_SQL);
        comparisonExecuteSql.setInputType(InputType.DEFAULT);

        DqRuleInputEntry checkType = new DqRuleInputEntry();
        checkType.setTitle("检测方式");
        checkType.setField("check_type");
        checkType.setType(FormType.SELECT);
        checkType.setCanEdit(true);
        checkType.setShow(true);
        checkType.setOptionSourceType(OptionSourceType.DEFAULT);
        checkType.setOptions("[{\"label\":\"比对值 - 统计值\",\"value\":\"0\"},{\"label\":\"统计值 - 比对值\",\"value\":\"1\"},"
                + "{\"label\":\"统计值 / 比对值\",\"value\":\"2\"},{\"label\":\"(比对值-统计值) / 比对值\",\"value\":\"3\"}]");
        checkType.setValue("0");
        checkType.setInputType(InputType.CHECK);
        checkType.setValueType(ValueType.STRING);
        checkType.setPlaceholder("检测类型");

        DqRuleInputEntry operator = new DqRuleInputEntry();
        operator.setTitle("操作符");
        operator.setField("operator");
        operator.setType(FormType.SELECT);
        operator.setCanEdit(true);
        operator.setShow(true);
        operator.setOptionSourceType(OptionSourceType.DEFAULT);
        operator.setOptions("[{\"label\":\"=\",\"value\":\"0\"},"
                + "{\"label\":\"<\",\"value\":\"1\"},{\"label\":\"<=\",\"value\":\"2\"},"
                + "{\"label\":\">\",\"value\":\"3\"},{\"label\":\">=\",\"value\":\"4\"},"
                + "{\"label\":\"!=\",\"value\":\"5\"}]");
        operator.setValue("0");
        operator.setInputType(InputType.CHECK);
        operator.setValueType(ValueType.STRING);
        operator.setPlaceholder("操作符");

        DqRuleInputEntry threshold = new DqRuleInputEntry();
        threshold.setTitle("阈值");
        threshold.setField("threshold");
        threshold.setType(FormType.INPUT);
        threshold.setCanEdit(true);
        threshold.setShow(true);
        threshold.setInputType(InputType.CHECK);
        threshold.setValueType(ValueType.NUMBER);
        threshold.setPlaceholder("Please enter threshold, number is needed");

        DqRuleInputEntry afterFailure = new DqRuleInputEntry();
        afterFailure.setTitle("失败策略");
        afterFailure.setField("failure_strategy");
        afterFailure.setType(FormType.SELECT);
        afterFailure.setCanEdit(true);
        afterFailure.setShow(true);
        afterFailure.setOptionSourceType(OptionSourceType.DEFAULT);
        afterFailure.setOptions("[{\"label\":\"告警\",\"value\":\"0\"},{\"label\":\"阻断\",\"value\":\"1\"}]");
        afterFailure.setValue("0");
        afterFailure.setInputType(InputType.CHECK);
        afterFailure.setValueType(ValueType.STRING);
        afterFailure.setPlaceholder("失败策略");

        defaultInputEntryList.add(checkType);
        defaultInputEntryList.add(operator);
        defaultInputEntryList.add(threshold);
        defaultInputEntryList.add(afterFailure);

        defaultInputEntryList.add(srcConnectorType);
        defaultInputEntryList.add(srcDatasourceId);
        defaultInputEntryList.add(srcTable);
        defaultInputEntryList.add(statisticsName);
        defaultInputEntryList.add(statisticsExecuteSql);

        defaultInputEntryList.add(targetConnectorType);
        defaultInputEntryList.add(targetDatasourceId);
        defaultInputEntryList.add(targetTable);
        defaultInputEntryList.add(comparisonName);
        defaultInputEntryList.add(comparisonExecuteSql);

        dataQualityTaskExecutionContext.setRuleInputEntryList(defaultInputEntryList);

        Map<String,String> inputParameterValue = new HashMap<>();
        inputParameterValue.put("src_connector_type","0");
        inputParameterValue.put("src_datasource_id","2");
        inputParameterValue.put("src_table","test1");
        inputParameterValue.put("statistics_name","src");
        inputParameterValue.put("statistics_execute_sql","select count(*) as src from ${src_table} where c1>20");
        inputParameterValue.put("target_connector_type","2");
        inputParameterValue.put("target_datasource_id","3");
        inputParameterValue.put("target_table","test1_1");
        inputParameterValue.put("comparison_name","target");
        inputParameterValue.put("comparison_execute_sql","select count(*) as target from ${target_table} where c1>20");
        inputParameterValue.put("check_type","1");
        inputParameterValue.put("operator","3");
        inputParameterValue.put("threshold","2");
        inputParameterValue.put("failure_strategy","0");
        inputParameterValue.put("rule_id","4");
        inputParameterValue.put("rule_type","3");
        inputParameterValue.put("rule_name","'跨表值比对'");
        inputParameterValue.put("create_time","'2021-08-25 00:00:00'");
        inputParameterValue.put("update_time","'2021-08-25 00:00:00'");
        inputParameterValue.put("process_definition_id","1");
        inputParameterValue.put("process_instance_id","1");
        inputParameterValue.put("task_instance_id","1");
        inputParameterValue.put("data_time","'2021-08-25 00:00:00'");
        inputParameterValue.put("error_output_path","hdfs://localhost:8022/user/ods/data_quality_error_data/1_1_1");

        dataQualityTaskExecutionContext.setSourceConnectorType("JDBC");
        dataQualityTaskExecutionContext.setSourceType(0);
        dataQualityTaskExecutionContext.setSourceConnectionParams(
                "{\"address\":\"jdbc:mysql://localhost:3306\","
                        + "\"database\":\"test\","
                        + "\"jdbcUrl\":\"jdbc:mysql://localhost:3306/test\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\"}");

        dataQualityTaskExecutionContext.setTargetConnectorType("HIVE");
        dataQualityTaskExecutionContext.setTargetType(2);
        dataQualityTaskExecutionContext.setTargetConnectionParams(
                "{\"address\":\"jdbc:hive2://localhost:10000\","
                        + "\"database\":\"default\","
                        + "\"jdbcUrl\":\"jdbc:hive2://localhost:10000/default\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\"}");

        dataQualityTaskExecutionContext.setWriterType(1);
        dataQualityTaskExecutionContext.setWriterConnectorType("JDBC");
        dataQualityTaskExecutionContext.setWriterTable("t_ds_dq_execute_result");
        dataQualityTaskExecutionContext.setWriterConnectionParams(
                "{\"address\":\"jdbc:postgresql://localhost:5432\","
                        + "\"database\":\"dolphinscheduler\","
                        + "\"jdbcUrl\":\"jdbc:postgresql://localhost:5432/dolphinscheduler\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\","
                        + "\"other\":\"stringtype=unspecified&characterEncoding=UTF-8&allowMultiQueries=true\"}");

        String expect = "{\"name\":\"跨表值比对\",\"env\":{\"type\":\"batch\",\"config\":null},\"readers\""
                + ":[{\"type\":\"JDBC\",\"config\":{\"database\":\"test\",\"password\":\"test\",\"driver\":"
                + "\"com.mysql.jdbc.Driver\",\"user\":\"test\",\"output_table\":\"test_test1\",\"table\":"
                + "\"test1\",\"url\":\"jdbc:mysql://localhost:3306/test\"}},{\"type\":\"HIVE\",\"config\":"
                + "{\"database\":\"default\",\"password\":\"test\",\"driver\":\"org.apache.hive.jdbc.HiveDriver\",\"user\":"
                + "\"test\",\"output_table\":\"default_test1_1\",\"table\":\"test1_1\",\"url\":"
                + "\"jdbc:hive2://localhost:10000/default\"}}],\"transformers\":[],\"writers\":"
                + "[{\"type\":\"JDBC\",\"config\":{\"database\":\"dolphinscheduler\",\"password\":"
                + "\"test\",\"driver\":\"org.postgresql.Driver\",\"user\":\"test\",\"table\":"
                + "\"t_ds_dq_execute_result\",\"url\":"
                + "\"jdbc:postgresql://localhost:5432/dolphinscheduler?stringtype=unspecified&characterEncoding=UTF-8&allowMultiQueries=true\","
                + "\"sql\":\"select 3 as rule_type,'跨表值比对' as rule_name,"
                + "1 as process_definition_id,1 as process_instance_id,1 as task_instance_id,src AS statistics_value,"
                + "target AS comparison_value,0 AS comparison_type,1 as check_type,2 as threshold,3 as operator,"
                + "0 as failure_strategy,'hdfs://localhost:8022/user/ods/data_quality_error_data/1_1_1' "
                + "as error_output_path,'2021-08-25 00:00:00' as create_time,'2021-08-25 00:00:00' as update_time "
                + "from ( select count(*) as src from test_test1 where c1>20 ) tmp1 join ( select count(*) as target from default_test1_1 "
                + "where c1>20 ) tmp2\"}}]}";

        RuleManager ruleManager = new RuleManager(inputParameterValue,dataQualityTaskExecutionContext);
        Assert.assertEquals(expect,JSONUtils.toJsonString(ruleManager.generateDataQualityParameter()));
    }

    @Test
    public void testMultiTableAccuracy() throws Exception {

        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = new DataQualityTaskExecutionContext();

        List<DqRuleInputEntry> defaultInputEntryList = new ArrayList<>();

        DqRuleInputEntry srcConnectorType = new DqRuleInputEntry();
        srcConnectorType.setTitle("源数据类型");
        srcConnectorType.setField("src_connector_type");
        srcConnectorType.setType(FormType.SELECT);
        srcConnectorType.setCanEdit(true);
        srcConnectorType.setShow(true);
        srcConnectorType.setValue("JDBC");
        srcConnectorType.setPlaceholder("Please select source connector type");
        srcConnectorType.setOptionSourceType(OptionSourceType.DATASOURCE_TYPE);
        srcConnectorType.setOptions(null);
        srcConnectorType.setInputType(InputType.DEFAULT);
        srcConnectorType.setValueType(ValueType.NUMBER);

        DqRuleInputEntry srcDatasourceId = new DqRuleInputEntry();
        srcDatasourceId.setTitle("源数据源");
        srcDatasourceId.setField("src_datasource_id");
        srcDatasourceId.setType(FormType.SELECT);
        srcDatasourceId.setCanEdit(true);
        srcDatasourceId.setShow(true);
        srcDatasourceId.setValue("1");
        srcDatasourceId.setPlaceholder("Please select source datasource");
        srcDatasourceId.setOptionSourceType(OptionSourceType.DATASOURCE_ID);
        srcDatasourceId.setInputType(InputType.DEFAULT);
        srcDatasourceId.setValueType(ValueType.NUMBER);

        DqRuleInputEntry srcTable = new DqRuleInputEntry();
        srcTable.setTitle("源数据表");
        srcTable.setField("src_table");
        srcTable.setType(FormType.INPUT);
        srcTable.setCanEdit(true);
        srcTable.setShow(true);
        srcTable.setPlaceholder("Please enter source table");
        srcTable.setOptionSourceType(OptionSourceType.DEFAULT);
        srcTable.setInputType(InputType.DEFAULT);
        srcTable.setValueType(ValueType.STRING);

        DqRuleInputEntry srcFilter = new DqRuleInputEntry();
        srcFilter.setTitle("源表过滤条件");
        srcFilter.setField("src_filter");
        srcFilter.setType(FormType.INPUT);
        srcFilter.setCanEdit(true);
        srcFilter.setShow(true);
        srcFilter.setPlaceholder("Please enter source filter expression");
        srcFilter.setOptionSourceType(OptionSourceType.DEFAULT);
        srcFilter.setInputType(InputType.DEFAULT);
        srcFilter.setValueType(ValueType.LIKE_SQL);

        DqRuleInputEntry targetConnectorType = new DqRuleInputEntry();
        targetConnectorType.setTitle("目标数据类型");
        targetConnectorType.setField("target_connector_type");
        targetConnectorType.setType(FormType.SELECT);
        targetConnectorType.setCanEdit(true);
        targetConnectorType.setShow(true);
        targetConnectorType.setValue("JDBC");
        targetConnectorType.setPlaceholder("Please select target connector type");
        targetConnectorType.setOptionSourceType(OptionSourceType.DATASOURCE_TYPE);
        targetConnectorType.setOptions(null);
        targetConnectorType.setInputType(InputType.DEFAULT);
        targetConnectorType.setValueType(ValueType.STRING);

        DqRuleInputEntry targetDatasourceId = new DqRuleInputEntry();
        targetDatasourceId.setTitle("目标数据源");
        targetDatasourceId.setField("target_datasource_id");
        targetDatasourceId.setType(FormType.CASCADER);
        targetDatasourceId.setCanEdit(true);
        targetDatasourceId.setShow(true);
        targetDatasourceId.setValue("1");
        targetDatasourceId.setPlaceholder("Please select target datasource");
        targetDatasourceId.setOptionSourceType(OptionSourceType.DATASOURCE_ID);
        targetDatasourceId.setInputType(InputType.DEFAULT);
        targetDatasourceId.setValueType(ValueType.NUMBER);

        DqRuleInputEntry targetTable = new DqRuleInputEntry();
        targetTable.setTitle("目标数据表");
        targetTable.setField("target_table");
        targetTable.setType(FormType.INPUT);
        targetTable.setCanEdit(true);
        targetTable.setShow(true);
        targetTable.setPlaceholder("Please enter target table");
        targetTable.setOptionSourceType(OptionSourceType.DEFAULT);
        targetTable.setInputType(InputType.DEFAULT);
        targetTable.setValueType(ValueType.STRING);

        DqRuleInputEntry targetFilter = new DqRuleInputEntry();
        targetFilter.setTitle("目标表过滤条件");
        targetFilter.setField("target_filter");
        targetFilter.setType(FormType.INPUT);
        targetFilter.setCanEdit(true);
        targetFilter.setShow(true);
        targetFilter.setPlaceholder("Please enter target filter expression");
        targetFilter.setOptionSourceType(OptionSourceType.DEFAULT);
        targetFilter.setInputType(InputType.DEFAULT);
        targetFilter.setValueType(ValueType.LIKE_SQL);

        DqRuleInputEntry mappingColumns = new DqRuleInputEntry();
        mappingColumns.setTitle("检查列");
        mappingColumns.setField("mapping_columns");
        mappingColumns.setType(FormType.INPUT);
        mappingColumns.setCanEdit(true);
        mappingColumns.setShow(true);
        mappingColumns.setPlaceholder("${mapping_columns}");
        mappingColumns.setOptionSourceType(OptionSourceType.DEFAULT);
        mappingColumns.setInputType(InputType.DEFAULT);
        mappingColumns.setValueType(ValueType.LIST);

        DqRuleInputEntry statisticsName = new DqRuleInputEntry();
        statisticsName.setTitle("统计值");
        statisticsName.setField("statistics_name");
        statisticsName.setType(FormType.INPUT);
        statisticsName.setCanEdit(false);
        statisticsName.setShow(false);
        statisticsName.setValue("miss_count.miss");
        statisticsName.setPlaceholder("${statistics_name}");
        statisticsName.setOptionSourceType(OptionSourceType.DEFAULT);
        statisticsName.setInputType(InputType.DEFAULT);
        statisticsName.setValueType(ValueType.STRING);

        defaultInputEntryList.add(srcConnectorType);
        defaultInputEntryList.add(srcDatasourceId);
        defaultInputEntryList.add(srcTable);
        defaultInputEntryList.add(srcFilter);
        defaultInputEntryList.add(targetConnectorType);
        defaultInputEntryList.add(targetDatasourceId);
        defaultInputEntryList.add(targetTable);
        defaultInputEntryList.add(targetFilter);
        defaultInputEntryList.add(mappingColumns);
        defaultInputEntryList.add(statisticsName);

        DqRuleExecuteSql executeSqlDefinition3 = new DqRuleExecuteSql();
        executeSqlDefinition3.setIndex(0);
        executeSqlDefinition3.setSql("SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})");
        executeSqlDefinition3.setTableAlias("total_count");
        executeSqlDefinition3.setType(ExecuteSqlType.MIDDLE);
        dataQualityTaskExecutionContext.addExecuteSql(executeSqlDefinition3);

        DqRuleExecuteSql executeSqlDefinition1 = new DqRuleExecuteSql();
        executeSqlDefinition1.setIndex(0);
        executeSqlDefinition1.setSql("SELECT ${src_table}.* FROM (SELECT * FROM ${src_table} WHERE (${src_filter})) "
                + "${src_table} LEFT JOIN (SELECT * FROM ${target_table} WHERE (${target_filter})) "
                + "${target_table} ON ${on_clause} WHERE ${where_clause}");
        executeSqlDefinition1.setTableAlias("miss_items");
        executeSqlDefinition1.setType(ExecuteSqlType.MIDDLE);
        executeSqlDefinition1.setErrorOutputSql(true);
        dataQualityTaskExecutionContext.addExecuteSql(executeSqlDefinition1);

        DqRuleExecuteSql executeSqlDefinition2 = new DqRuleExecuteSql();
        executeSqlDefinition2.setIndex(0);
        executeSqlDefinition2.setSql("SELECT COUNT(*) AS miss FROM miss_items");
        executeSqlDefinition2.setTableAlias("miss_count");
        executeSqlDefinition2.setType(ExecuteSqlType.STATISTICS);
        dataQualityTaskExecutionContext.addExecuteSql(executeSqlDefinition2);

        DqRuleInputEntry comparisonTitle = new DqRuleInputEntry();
        comparisonTitle.setTitle("比对值");
        comparisonTitle.setField("comparison_title");
        comparisonTitle.setType(FormType.INPUT);
        comparisonTitle.setCanEdit(false);
        comparisonTitle.setShow(true);
        comparisonTitle.setPlaceholder("${comparison_title}");
        comparisonTitle.setValue("目标表总行数");

        DqRuleInputEntry comparisonName = new DqRuleInputEntry();
        comparisonName.setTitle("比对值名");
        comparisonName.setField("comparison_name");
        comparisonName.setType(FormType.INPUT);
        comparisonName.setCanEdit(false);
        comparisonName.setShow(false);
        comparisonName.setValue("total_count.total");
        comparisonName.setPlaceholder("${comparison_name}");

        DqRuleInputEntry comparisonTable = new DqRuleInputEntry();
        comparisonTable.setField(COMPARISON_TABLE);
        comparisonTable.setValue("total_count");

        DqRuleInputEntry checkType = new DqRuleInputEntry();
        checkType.setTitle("检测方式");
        checkType.setField("check_type");
        checkType.setType(FormType.SELECT);
        checkType.setCanEdit(true);
        checkType.setShow(true);
        checkType.setOptionSourceType(OptionSourceType.DEFAULT);
        checkType.setOptions("[{\"label\":\"比对值 - 统计值\",\"value\":\"0\"},{\"label\":\"统计值 - 比对值\",\"value\":\"1\"},{\"label\":\"统计值 / 比对值\","
                + "\"value\":\"2\"},{\"label\":\"(比对值-统计值) / 比对值\",\"value\":\"3\"}]");
        checkType.setValue("0");
        checkType.setInputType(InputType.CHECK);
        checkType.setValueType(ValueType.STRING);
        checkType.setPlaceholder("检测类型");

        DqRuleInputEntry operator = new DqRuleInputEntry();
        operator.setTitle("操作符");
        operator.setField("operator");
        operator.setType(FormType.SELECT);
        operator.setCanEdit(true);
        operator.setShow(true);
        operator.setOptionSourceType(OptionSourceType.DEFAULT);
        operator.setOptions("[{\"label\":\"=\",\"value\":\"0\"},"
                + "{\"label\":\"<\",\"value\":\"1\"},{\"label\":\"<=\",\"value\":\"2\"},"
                + "{\"label\":\">\",\"value\":\"3\"},{\"label\":\">=\",\"value\":\"4\"},{\"label\":\"!=\",\"value\":\"5\"}]");
        operator.setValue("0");
        operator.setInputType(InputType.CHECK);
        operator.setValueType(ValueType.STRING);
        operator.setPlaceholder("操作符");

        DqRuleInputEntry threshold = new DqRuleInputEntry();
        threshold.setTitle("阈值");
        threshold.setField("threshold");
        threshold.setType(FormType.INPUT);
        threshold.setCanEdit(true);
        threshold.setShow(true);
        threshold.setInputType(InputType.CHECK);
        threshold.setValueType(ValueType.NUMBER);
        threshold.setPlaceholder("Please enter threshold, number is needed");

        DqRuleInputEntry afterFailure = new DqRuleInputEntry();
        afterFailure.setTitle("失败策略");
        afterFailure.setField("failure_strategy");
        afterFailure.setType(FormType.SELECT);
        afterFailure.setCanEdit(true);
        afterFailure.setShow(true);
        afterFailure.setOptionSourceType(OptionSourceType.DEFAULT);
        afterFailure.setOptions("[{\"label\":\"告警\",\"value\":\"0\"},{\"label\":\"阻断\",\"value\":\"1\"}]");
        afterFailure.setValue("0");
        afterFailure.setInputType(InputType.CHECK);
        afterFailure.setValueType(ValueType.STRING);
        afterFailure.setPlaceholder("失败策略");

        defaultInputEntryList.add(checkType);
        defaultInputEntryList.add(operator);
        defaultInputEntryList.add(threshold);
        defaultInputEntryList.add(afterFailure);
        defaultInputEntryList.add(comparisonTitle);
        defaultInputEntryList.add(comparisonName);
        defaultInputEntryList.add(comparisonTable);

        dataQualityTaskExecutionContext.setRuleInputEntryList(defaultInputEntryList);

        Map<String,String> inputParameterValue = new HashMap<>();
        inputParameterValue.put("src_connector_type","0");
        inputParameterValue.put("src_datasource_id","2");
        inputParameterValue.put("src_table","demo_src");
        inputParameterValue.put("src_filter","age<100");
        inputParameterValue.put("target_connector_type","2");
        inputParameterValue.put("target_datasource_id","3");
        inputParameterValue.put("target_table","demo_src");
        inputParameterValue.put("target_filter","age<100");
        inputParameterValue.put("mapping_columns","[{\"src_field\":\"hour\",\"operator\":\"=\",\"target_field\":\"hour\"}]");
        inputParameterValue.put("check_type","2");
        inputParameterValue.put("operator","3");
        inputParameterValue.put("threshold","3");
        inputParameterValue.put("failure_strategy","0");
        inputParameterValue.put("comparison_type","7");
        inputParameterValue.put("rule_id","3");
        inputParameterValue.put("rule_type","2");
        inputParameterValue.put("rule_name","'跨表准确性'");
        inputParameterValue.put("create_time","'2021-08-30 00:00:00'");
        inputParameterValue.put("update_time","'2021-08-30 00:00:00'");
        inputParameterValue.put("process_definition_id","1");
        inputParameterValue.put("process_instance_id","1");
        inputParameterValue.put("task_instance_id","1");
        inputParameterValue.put("data_time","'2021-08-30 00:00:00'");
        inputParameterValue.put("error_output_path","hdfs://localhost:8022/user/ods/data_quality_error_data/1_1_test");

        dataQualityTaskExecutionContext.setSourceConnectorType("JDBC");
        dataQualityTaskExecutionContext.setSourceType(0);
        dataQualityTaskExecutionContext.setSourceConnectionParams(
                "{\"address\":\"jdbc:mysql://localhost:3306\","
                        + "\"database\":\"test\","
                        + "\"jdbcUrl\":\"jdbc:mysql://localhost:3306/test\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\"}");

        dataQualityTaskExecutionContext.setTargetConnectorType("HIVE");
        dataQualityTaskExecutionContext.setTargetType(2);
        dataQualityTaskExecutionContext.setTargetConnectionParams(
                "{\"address\":\"jdbc:hive2://localhost:10000\","
                        + "\"database\":\"default\","
                        + "\"jdbcUrl\":\"jdbc:hive2://localhost:10000/default\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\"}");

        dataQualityTaskExecutionContext.setWriterType(1);
        dataQualityTaskExecutionContext.setWriterConnectorType("JDBC");
        dataQualityTaskExecutionContext.setWriterTable("t_ds_dq_execute_result");
        dataQualityTaskExecutionContext.setWriterConnectionParams(
                "{\"address\":\"jdbc:postgresql://localhost:5432\","
                        + "\"database\":\"dolphinscheduler\","
                        + "\"jdbcUrl\":\"jdbc:postgresql://localhost:5432/dolphinscheduler\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\","
                        + "\"other\":\"stringtype=unspecified&characterEncoding=UTF-8&allowMultiQueries=true\"}");

        dataQualityTaskExecutionContext.setStatisticsValueConnectorType("JDBC");
        dataQualityTaskExecutionContext.setStatisticsValueType(1);
        dataQualityTaskExecutionContext.setStatisticsValueTable("t_ds_dq_task_statistics_value");
        dataQualityTaskExecutionContext.setStatisticsValueWriterConnectionParams(
                "{\"address\":\"jdbc:postgresql://localhost:5432\","
                        + "\"database\":\"dolphinscheduler\","
                        + "\"jdbcUrl\":\"jdbc:postgresql://localhost:5432/dolphinscheduler\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\","
                        + "\"other\":\"stringtype=unspecified&characterEncoding=UTF-8&allowMultiQueries=true\"}");

        dataQualityTaskExecutionContext.setRuleName("跨表准确性");
        dataQualityTaskExecutionContext.setRuleType(RuleType.MULTI_TABLE_ACCURACY);

        String expect = "{\"name\":\"跨表准确性\",\"env\":{\"type\":\"batch\",\"config\":null},\"readers\":"
                + "[{\"type\":\"JDBC\",\"config\":{\"database\":\"test\",\"password\":\"test\",\"driver\":"
                + "\"com.mysql.jdbc.Driver\",\"user\":\"test\",\"output_table\":\"test_demo_src\",\"table\":"
                + "\"demo_src\",\"url\":\"jdbc:mysql://localhost:3306/test\"}},{\"type\":\"HIVE\",\"config\":"
                + "{\"database\":\"default\",\"password\":\"test\",\"driver\":"
                + "\"org.apache.hive.jdbc.HiveDriver\",\"user\":\"test\",\"output_table\":\"default_demo_src\",\"table\":"
                + "\"demo_src\",\"url\":\"jdbc:hive2://localhost:10000/default\"}}],\"transformers\":"
                + "[{\"type\":\"sql\",\"config\":{\"index\":1,\"output_table\":\"total_count\","
                + "\"sql\":\"SELECT COUNT(*) AS total FROM test_demo_src WHERE (age<100)\"}},"
                + "{\"type\":\"sql\",\"config\":{\"index\":2,\"output_table\":\"miss_items\",\"sql\":"
                + "\"SELECT test_demo_src.* FROM (SELECT * FROM test_demo_src WHERE (age<100)) "
                + "test_demo_src LEFT JOIN (SELECT * FROM default_demo_src WHERE (age<100)) default_demo_src ON coalesce(test_demo_src.hour, '') ="
                + " coalesce(default_demo_src.hour, '') WHERE ( NOT (test_demo_src.hour IS NULL )) AND "
                + "( default_demo_src.hour IS NULL )\"}},{\"type\":\"sql\",\"config\":{\"index\":3,\"output_table\":\"miss_count\","
                + "\"sql\":\"SELECT COUNT(*) AS miss FROM miss_items\"}}],\"writers\":[{\"type\":\"JDBC\",\"config\":"
                + "{\"database\":\"dolphinscheduler\",\"password\":\"test\",\"driver\":\"org.postgresql.Driver\",\"user\":\"test\",\"table\":"
                + "\"t_ds_dq_execute_result\",\"url\":\"jdbc:postgresql://localhost:5432/dolphinscheduler?stringtype=unspecified"
                + "&characterEncoding=UTF-8&allowMultiQueries=true\",\"sql\":\"select 2 as rule_type,'跨表准确性' as rule_name,1 as process_definition_id,"
                + "1 as process_instance_id,1 as task_instance_id,miss_count.miss AS statistics_value,total_count.total AS comparison_value,"
                + "7 AS comparison_type,2 as check_type,3 as threshold,3 as operator,0 as failure_strategy,"
                + "'hdfs://localhost:8022/user/ods/data_quality_error_data/1_1_test' as error_output_path,"
                + "'2021-08-30 00:00:00' as create_time,'2021-08-30 00:00:00' as update_time from miss_count"
                + " full join total_count\"}},{\"type\":\"JDBC\",\"config\":{\"database\":\"dolphinscheduler\","
                + "\"password\":\"test\",\"driver\":\"org.postgresql.Driver\",\"user\":\"test\",\"table\":"
                + "\"t_ds_dq_task_statistics_value\",\"url\":\"jdbc:postgresql://localhost:5432/dolphinscheduler?stringtype=unspecified"
                + "&characterEncoding=UTF-8&allowMultiQueries=true\",\"sql\":\"select 1 as process_definition_id,1 as task_instance_id,"
                + "3 as rule_id,'T4MB2XTVSL+VA/L6XCU1M/ELHKYOMGVNBBE5KHBXHHI=' as unique_code,'miss_count.miss'AS statistics_name,miss_count.miss "
                + "AS statistics_value,'2021-08-30 00:00:00' as data_time,"
                + "'2021-08-30 00:00:00' as create_time,'2021-08-30 00:00:00' as update_time from miss_count\"}},{\"type\":\"hdfs_file\","
                + "\"config\":{\"path\":\"hdfs://localhost:8022/user/ods/data_quality_error_data/1_1_test\",\"input_table\":\"miss_items\"}}]}";

        RuleManager ruleManager = new RuleManager(inputParameterValue,dataQualityTaskExecutionContext);
        Assert.assertEquals(expect,JSONUtils.toJsonString(ruleManager.generateDataQualityParameter()));
    }
}
