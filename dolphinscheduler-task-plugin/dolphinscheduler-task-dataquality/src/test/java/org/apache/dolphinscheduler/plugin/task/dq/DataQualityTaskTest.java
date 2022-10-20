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

package org.apache.dolphinscheduler.plugin.task.dq;

import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.COMPARISON_TABLE;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.SRC_FIELD;

import org.apache.dolphinscheduler.plugin.task.api.DataQualityTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ExecuteSqlType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.InputType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.OptionSourceType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.RuleType;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.ValueType;
import org.apache.dolphinscheduler.plugin.task.dq.rule.RuleManager;
import org.apache.dolphinscheduler.plugin.task.dq.rule.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.plugin.task.dq.rule.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.spi.params.base.FormType;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * DataQualityTaskTest
 */

public class DataQualityTaskTest {

    @Test
    public void testSingleTable() throws Exception {
        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = getSingleTableContext();

        Map<String, String> inputParameterValue = new HashMap<>();
        inputParameterValue.put("src_connector_type", "0");
        inputParameterValue.put("src_datasource_id", "2");
        inputParameterValue.put("src_table", "src_result");
        inputParameterValue.put("check_type", "0");
        inputParameterValue.put("operator", "3");
        inputParameterValue.put("threshold", "1");
        inputParameterValue.put("failure_strategy", "0");
        inputParameterValue.put("comparison_type", "1");
        inputParameterValue.put("comparison_name", "10");
        inputParameterValue.put("rule_id", "10");
        inputParameterValue.put("rule_type", "0");
        inputParameterValue.put("rule_name", "'表行数校验'");
        inputParameterValue.put("create_time", "'2021-08-12 10:15:48'");
        inputParameterValue.put("update_time", "'2021-08-12 10:15:48'");
        inputParameterValue.put("process_definition_id", "21");
        inputParameterValue.put("process_instance_id", "284");
        inputParameterValue.put("task_instance_id", "287");
        inputParameterValue.put("data_time", "'2021-08-12 10:15:48'");
        inputParameterValue.put("error_output_path",
                "hdfs://192.168.0.1:8022/user/ods/data_quality_error_data/21_284_287");

        RuleManager ruleManager = new RuleManager(inputParameterValue, dataQualityTaskExecutionContext);
        String expect = "{\"name\":\"表行数校验\",\"env\":{\"type\":\"batch\",\"config\":null},"
                + "\"readers\":[{\"type\":\"JDBC\",\"config\":"
                + "{\"database\":\"test\",\"password\":\"test\",\"driver\":\"com.mysql.cj.jdbc.Driver\","
                + "\"user\":\"test\",\"output_table\":\"test_src_result\",\"table\":\"src_result\","
                + "\"url\":\"jdbc:mysql://localhost:3306/test?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false\"}}],"
                + "\"transformers\":[{\"type\":\"sql\",\"config\":{\"index\":1,"
                + "\"output_table\":\"table_count\",\"sql\":\"SELECT COUNT(*) AS total FROM test_src_result \"}}],"
                + "\"writers\":[{\"type\":\"JDBC\",\"config\":{\"database\":\"test\",\"password\":\"test\","
                + "\"driver\":\"com.mysql.cj.jdbc.Driver\",\"user\":\"test\",\"table\":\"dqc_result\","
                + "\"url\":\"jdbc:mysql://localhost:3306/test?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false\","
                + "\"sql\":\"select 0 as rule_type,'表行数校验' as rule_name,21 as process_definition_id,284 as process_instance_id,"
                + "287 as task_instance_id,table_count.total AS statistics_value,10 AS comparison_value,1 AS comparison_type,"
                + "0 as check_type,1 as threshold,3 as operator,0 as failure_strategy,"
                + "'hdfs://192.168.0.1:8022/user/ods/data_quality_error_data/21_284_287' as error_output_path,"
                + "'2021-08-12 10:15:48' as create_time,'2021-08-12 10:15:48' as update_time from table_count \"}},"
                + "{\"type\":\"JDBC\",\"config\":{\"database\":\"test\",\"password\":\"test\",\"driver\":\"com.mysql.cj.jdbc.Driver\","
                + "\"user\":\"test\",\"table\":\"dqc_statistics_value\",\"url\":"
                + "\"jdbc:mysql://localhost:3306/test?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false\","
                + "\"sql\":\"select 21 as process_definition_id,287 as task_instance_id,10 as rule_id,'DN/MS5NLTSLVZ/++KEJ9BHPQSEN6/UY/EV5TWI1IRRY=' "
                + "as unique_code,'table_count.total'AS statistics_name,"
                + "table_count.total AS statistics_value,'2021-08-12 10:15:48' as data_time,'2021-08-12 10:15:48' as create_time,"
                + "'2021-08-12 10:15:48' as update_time from table_count\"}}]}";
        Assertions.assertEquals(expect, JSONUtils.toJsonString(ruleManager.generateDataQualityParameter()));
    }

    private DataQualityTaskExecutionContext getSingleTableContext() {
        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = new DataQualityTaskExecutionContext();

        dataQualityTaskExecutionContext.setRuleName("表行数校验");
        dataQualityTaskExecutionContext.setRuleType(RuleType.SINGLE_TABLE.getCode());

        List<DqRuleInputEntry> defaultInputEntryList = new ArrayList<>();

        DqRuleInputEntry srcConnectorType = new DqRuleInputEntry();
        srcConnectorType.setTitle("源数据类型");
        srcConnectorType.setField("src_connector_type");
        srcConnectorType.setType(FormType.SELECT.getFormType());
        srcConnectorType.setCanEdit(true);
        srcConnectorType.setIsShow(true);
        srcConnectorType.setValue(null);
        srcConnectorType.setPlaceholder("${src_connector_type}");
        srcConnectorType.setOptionSourceType(OptionSourceType.DATASOURCE_TYPE.getCode());
        srcConnectorType.setOptions(null);
        srcConnectorType.setInputType(InputType.DEFAULT.getCode());
        srcConnectorType.setValueType(ValueType.NUMBER.getCode());
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry srcDatasourceId = new DqRuleInputEntry();
        srcDatasourceId.setTitle("源数据源");
        srcDatasourceId.setField("src_datasource_id");
        srcDatasourceId.setType(FormType.CASCADER.getFormType());
        srcDatasourceId.setCanEdit(true);
        srcDatasourceId.setIsShow(true);
        srcDatasourceId.setValue(null);
        srcDatasourceId.setOptionSourceType(OptionSourceType.DATASOURCE_ID.getCode());
        srcDatasourceId.setInputType(InputType.DEFAULT.getCode());
        srcDatasourceId.setValueType(ValueType.NUMBER.getCode());
        srcDatasourceId.setCreateTime(new Date());
        srcDatasourceId.setUpdateTime(new Date());

        DqRuleInputEntry srcTable = new DqRuleInputEntry();
        srcTable.setTitle("源数据表");
        srcTable.setField("src_table");
        srcTable.setType(FormType.INPUT.getFormType());
        srcTable.setCanEdit(true);
        srcTable.setIsShow(true);
        srcTable.setPlaceholder("Please enter source table name");
        srcTable.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        srcTable.setInputType(InputType.DEFAULT.getCode());
        srcTable.setValueType(ValueType.STRING.getCode());
        srcTable.setCreateTime(new Date());
        srcTable.setUpdateTime(new Date());

        DqRuleInputEntry srcFilter = new DqRuleInputEntry();
        srcFilter.setTitle("源表过滤条件");
        srcFilter.setField("src_filter");
        srcFilter.setType(FormType.INPUT.getFormType());
        srcFilter.setCanEdit(true);
        srcFilter.setIsShow(true);
        srcFilter.setPlaceholder("Please enter filter expression");
        srcFilter.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        srcFilter.setInputType(InputType.DEFAULT.getCode());
        srcFilter.setValueType(ValueType.LIKE_SQL.getCode());
        srcFilter.setCreateTime(new Date());
        srcFilter.setUpdateTime(new Date());

        DqRuleInputEntry srcField = new DqRuleInputEntry();
        srcField.setTitle("检测列");
        srcField.setField(SRC_FIELD);
        srcField.setType(FormType.INPUT.getFormType());
        srcField.setCanEdit(true);
        srcField.setIsShow(true);
        srcField.setValue("");
        srcField.setPlaceholder("Please enter column, only single column is supported");
        srcField.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        srcField.setInputType(InputType.DEFAULT.getCode());
        srcField.setValueType(ValueType.STRING.getCode());
        srcField.setCreateTime(new Date());
        srcField.setUpdateTime(new Date());

        DqRuleInputEntry statisticsName = new DqRuleInputEntry();
        statisticsName.setTitle("统计值");
        statisticsName.setField("statistics_name");
        statisticsName.setType(FormType.INPUT.getFormType());
        statisticsName.setCanEdit(false);
        statisticsName.setIsShow(false);
        statisticsName.setValue("table_count.total");
        statisticsName.setPlaceholder("${statistics_name}");
        statisticsName.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        statisticsName.setInputType(InputType.STATISTICS.getCode());
        statisticsName.setValueType(ValueType.STRING.getCode());
        statisticsName.setCreateTime(new Date());
        statisticsName.setUpdateTime(new Date());

        DqRuleInputEntry checkType = new DqRuleInputEntry();
        checkType.setTitle("检测方式");
        checkType.setField("check_type");
        checkType.setType(FormType.SELECT.getFormType());
        checkType.setCanEdit(true);
        checkType.setIsShow(true);
        checkType.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        checkType.setOptions(
                "[{\"label\":\"比对值 - 统计值\",\"value\":\"0\"},{\"label\":\"统计值 - 比对值\",\"value\":\"1\"},{\"label\":\"统计值 / 比对值\","
                        + "\"value\":\"2\"},{\"label\":\"(比对值-统计值) / 比对值\",\"value\":\"3\"}]");
        checkType.setValue("0");
        checkType.setInputType(InputType.CHECK.getCode());
        checkType.setValueType(ValueType.STRING.getCode());
        checkType.setPlaceholder("检测类型");
        checkType.setCreateTime(new Date());
        checkType.setUpdateTime(new Date());

        DqRuleInputEntry operator = new DqRuleInputEntry();
        operator.setTitle("操作符");
        operator.setField("operator");
        operator.setType(FormType.SELECT.getFormType());
        operator.setCanEdit(true);
        operator.setIsShow(true);
        operator.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        operator.setOptions("[{\"label\":\"=\",\"value\":\"0\"},"
                + "{\"label\":\"<\",\"value\":\"1\"},{\"label\":\"<=\",\"value\":\"2\"},"
                + "{\"label\":\">\",\"value\":\"3\"},{\"label\":\">=\",\"value\":\"4\"},"
                + "{\"label\":\"!=\",\"value\":\"5\"}]");
        operator.setValue("0");
        operator.setInputType(InputType.CHECK.getCode());
        operator.setValueType(ValueType.STRING.getCode());
        operator.setPlaceholder("操作符");
        operator.setCreateTime(new Date());
        operator.setUpdateTime(new Date());

        DqRuleInputEntry threshold = new DqRuleInputEntry();
        threshold.setTitle("阈值");
        threshold.setField("threshold");
        threshold.setType(FormType.INPUT.getFormType());
        threshold.setCanEdit(true);
        threshold.setIsShow(true);
        threshold.setPlaceholder("Please enter threshold, number is needed");
        threshold.setInputType(InputType.CHECK.getCode());
        threshold.setValueType(ValueType.NUMBER.getCode());
        threshold.setCreateTime(new Date());
        threshold.setUpdateTime(new Date());

        DqRuleInputEntry afterFailure = new DqRuleInputEntry();
        afterFailure.setTitle("失败策略");
        afterFailure.setField("failure_strategy");
        afterFailure.setType(FormType.SELECT.getFormType());
        afterFailure.setCanEdit(true);
        afterFailure.setIsShow(true);
        afterFailure.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        afterFailure.setOptions("[{\"label\":\"告警\",\"value\":\"0\"},{\"label\":\"阻断\",\"value\":\"1\"}]");
        afterFailure.setValue("0");
        afterFailure.setInputType(InputType.CHECK.getCode());
        afterFailure.setValueType(ValueType.STRING.getCode());
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
        executeSqlDefinition3.setType(ExecuteSqlType.STATISTICS.getCode());

        List<DqRuleExecuteSql> executeSqlList = new ArrayList<>();
        executeSqlList.add(executeSqlDefinition3);
        dataQualityTaskExecutionContext.setExecuteSqlList(JSONUtils.toJsonString(executeSqlList));
        dataQualityTaskExecutionContext.setRuleInputEntryList(JSONUtils.toJsonString(defaultInputEntryList));
        dataQualityTaskExecutionContext.setSourceConnectorType("JDBC");
        dataQualityTaskExecutionContext.setSourceType(0);
        dataQualityTaskExecutionContext.setSourceConnectionParams(
                "{\"address\":\"jdbc:mysql://localhost:3306\","
                        + "\"database\":\"test\","
                        + "\"jdbcUrl\":\"jdbc:mysql://localhost:3306/test\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\"}");

        dataQualityTaskExecutionContext.setWriterType(0);
        dataQualityTaskExecutionContext.setWriterConnectorType("JDBC");
        dataQualityTaskExecutionContext.setWriterTable("dqc_result");
        dataQualityTaskExecutionContext.setWriterConnectionParams(
                "{\"address\":\"jdbc:mysql://localhost:3306\","
                        + "\"database\":\"test\","
                        + "\"jdbcUrl\":\"jdbc:mysql://localhost:3306/test\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\"}");

        dataQualityTaskExecutionContext.setStatisticsValueConnectorType("JDBC");
        dataQualityTaskExecutionContext.setStatisticsValueType(0);
        dataQualityTaskExecutionContext.setStatisticsValueTable("dqc_statistics_value");
        dataQualityTaskExecutionContext.setStatisticsValueWriterConnectionParams(
                "{\"address\":\"jdbc:mysql://localhost:3306\","
                        + "\"database\":\"test\","
                        + "\"jdbcUrl\":\"jdbc:mysql://localhost:3306/test\","
                        + "\"user\":\"test\","
                        + "\"password\":\"test\"}");

        dataQualityTaskExecutionContext.setCompareWithFixedValue(true);
        return dataQualityTaskExecutionContext;
    }

    @Test
    public void testSingleTableCustomSql() throws Exception {
        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = new DataQualityTaskExecutionContext();

        dataQualityTaskExecutionContext.setRuleName("自定义SQL");
        dataQualityTaskExecutionContext.setRuleType(RuleType.SINGLE_TABLE_CUSTOM_SQL.getCode());

        List<DqRuleInputEntry> defaultInputEntryList = new ArrayList<>();

        DqRuleInputEntry srcConnectorType = new DqRuleInputEntry();
        srcConnectorType.setTitle("源数据类型");
        srcConnectorType.setField("src_connector_type");
        srcConnectorType.setType(FormType.SELECT.getFormType());
        srcConnectorType.setCanEdit(true);
        srcConnectorType.setIsShow(true);
        srcConnectorType.setValue(null);
        srcConnectorType.setPlaceholder("${src_connector_type}");
        srcConnectorType.setOptionSourceType(OptionSourceType.DATASOURCE_TYPE.getCode());
        srcConnectorType.setOptions(null);
        srcConnectorType.setInputType(InputType.DEFAULT.getCode());
        srcConnectorType.setValueType(ValueType.NUMBER.getCode());
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry srcDatasourceId = new DqRuleInputEntry();
        srcDatasourceId.setTitle("源数据源");
        srcDatasourceId.setField("src_datasource_id");
        srcDatasourceId.setType(FormType.CASCADER.getFormType());
        srcDatasourceId.setCanEdit(true);
        srcDatasourceId.setIsShow(true);
        srcDatasourceId.setValue(null);
        srcDatasourceId.setPlaceholder("${comparison_value}");
        srcDatasourceId.setOptionSourceType(OptionSourceType.DATASOURCE_ID.getCode());
        srcDatasourceId.setInputType(InputType.DEFAULT.getCode());
        srcDatasourceId.setValueType(ValueType.NUMBER.getCode());
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry srcTable = new DqRuleInputEntry();
        srcTable.setTitle("源数据表");
        srcTable.setField("src_table");
        srcTable.setType(FormType.INPUT.getFormType());
        srcTable.setCanEdit(true);
        srcTable.setIsShow(true);
        srcTable.setPlaceholder("Please enter source table name");
        srcTable.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        srcTable.setInputType(InputType.DEFAULT.getCode());
        srcTable.setValueType(ValueType.STRING.getCode());
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry srcFilter = new DqRuleInputEntry();
        srcFilter.setTitle("源表过滤条件");
        srcFilter.setField("src_filter");
        srcFilter.setType(FormType.INPUT.getFormType());
        srcFilter.setCanEdit(true);
        srcFilter.setIsShow(true);
        srcFilter.setPlaceholder("Please enter source filter expression");
        srcFilter.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        srcFilter.setInputType(InputType.DEFAULT.getCode());
        srcFilter.setValueType(ValueType.LIKE_SQL.getCode());

        DqRuleInputEntry statisticsName = new DqRuleInputEntry();
        statisticsName.setTitle("统计值名");
        statisticsName.setField("statistics_name");
        statisticsName.setType(FormType.INPUT.getFormType());
        statisticsName.setCanEdit(true);
        statisticsName.setIsShow(true);
        statisticsName.setPlaceholder("Please enter statistics name, the alias in statistics execute sql");
        statisticsName.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        statisticsName.setInputType(InputType.DEFAULT.getCode());
        statisticsName.setValueType(ValueType.STRING.getCode());

        DqRuleInputEntry statisticsExecuteSql = new DqRuleInputEntry();
        statisticsExecuteSql.setTitle("统计值计算SQL");
        statisticsExecuteSql.setField("statistics_execute_sql");
        statisticsExecuteSql.setType(FormType.TEXTAREA.getFormType());
        statisticsExecuteSql.setCanEdit(true);
        statisticsExecuteSql.setIsShow(true);
        statisticsExecuteSql.setPlaceholder("Please enter the statistics execute sql");
        statisticsExecuteSql.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        statisticsExecuteSql.setValueType(ValueType.LIKE_SQL.getCode());

        DqRuleInputEntry checkType = new DqRuleInputEntry();
        checkType.setTitle("检测方式");
        checkType.setField("check_type");
        checkType.setType(FormType.SELECT.getFormType());
        checkType.setCanEdit(true);
        checkType.setIsShow(true);
        checkType.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        checkType.setOptions("[{\"label\":\"比对值 - 统计值\",\"value\":\"0\"},{\"label\":\"统计值 - 比对值\",\"value\":\"1\"},"
                + "{\"label\":\"统计值 / 比对值\",\"value\":\"2\"},{\"label\":\"(比对值-统计值) / 比对值\",\"value\":\"3\"}]");
        checkType.setValue("0");
        checkType.setInputType(InputType.CHECK.getCode());
        checkType.setValueType(ValueType.STRING.getCode());
        checkType.setPlaceholder("检测类型");

        DqRuleInputEntry operator = new DqRuleInputEntry();
        operator.setTitle("操作符");
        operator.setField("operator");
        operator.setType(FormType.SELECT.getFormType());
        operator.setCanEdit(true);
        operator.setIsShow(true);
        operator.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        operator.setOptions("[{\"label\":\"=\",\"value\":\"0\"},"
                + "{\"label\":\"<\",\"value\":\"1\"},{\"label\":\"<=\",\"value\":\"2\"},"
                + "{\"label\":\">\",\"value\":\"3\"},{\"label\":\">=\",\"value\":\"4\"},"
                + "{\"label\":\"!=\",\"value\":\"5\"}]");
        operator.setValue("0");
        operator.setInputType(InputType.CHECK.getCode());
        operator.setValueType(ValueType.STRING.getCode());
        operator.setPlaceholder("操作符");

        DqRuleInputEntry threshold = new DqRuleInputEntry();
        threshold.setTitle("阈值");
        threshold.setField("threshold");
        threshold.setType(FormType.INPUT.getFormType());
        threshold.setCanEdit(true);
        threshold.setIsShow(true);
        threshold.setPlaceholder("Please enter threshold value, number is needed");
        threshold.setInputType(InputType.CHECK.getCode());
        threshold.setValueType(ValueType.NUMBER.getCode());

        DqRuleInputEntry afterFailure = new DqRuleInputEntry();
        afterFailure.setTitle("失败策略");
        afterFailure.setField("failure_strategy");
        afterFailure.setType(FormType.SELECT.getFormType());
        afterFailure.setCanEdit(true);
        afterFailure.setIsShow(true);
        afterFailure.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        afterFailure.setOptions("[{\"label\":\"告警\",\"value\":\"0\"},{\"label\":\"阻断\",\"value\":\"1\"}]");
        afterFailure.setValue("0");
        afterFailure.setInputType(InputType.CHECK.getCode());
        afterFailure.setValueType(ValueType.STRING.getCode());
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

        Map<String, String> inputParameterValue = new HashMap<>();
        inputParameterValue.put("src_connector_type", "0");
        inputParameterValue.put("src_datasource_id", "2");
        inputParameterValue.put("src_table", "person");
        inputParameterValue.put("statistics_name", "miss");
        inputParameterValue.put("statistics_execute_sql",
                "select count(*) as miss from ${src_table} where (sex = null or sex='') and age=1");
        inputParameterValue.put("src_filter", "age=1");
        inputParameterValue.put("check_type", "2");
        inputParameterValue.put("operator", "3");
        inputParameterValue.put("threshold", "50");
        inputParameterValue.put("failure_strategy", "1");
        inputParameterValue.put("comparison_type", "1");
        inputParameterValue.put("comparison_name", "3");
        inputParameterValue.put("rule_id", "1");
        inputParameterValue.put("rule_type", "1");
        inputParameterValue.put("rule_name", "'自定义SQL'");
        inputParameterValue.put("create_time", "'2021-08-30 00:00:00'");
        inputParameterValue.put("update_time", "'2021-08-30 00:00:00'");
        inputParameterValue.put("process_definition_id", "1");
        inputParameterValue.put("process_instance_id", "1");
        inputParameterValue.put("task_instance_id", "1");
        inputParameterValue.put("data_time", "'2021-08-30 00:00:00'");
        inputParameterValue.put("error_output_path",
                "hdfs://localhost:8022/user/ods/data_quality_error_data/1_1_test2");

        dataQualityTaskExecutionContext.setRuleInputEntryList(JSONUtils.toJsonString(defaultInputEntryList));
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

        RuleManager ruleManager = new RuleManager(inputParameterValue, dataQualityTaskExecutionContext);
        String expect =
                "{\"name\":\"自定义SQL\",\"env\":{\"type\":\"batch\",\"config\":null},\"readers\":[{\"type\":\"JDBC\","
                        + "\"config\":{\"database\":\"test\",\"password\":\"test\",\"driver\":\"com.mysql.cj.jdbc.Driver\",\"user\":"
                        + "\"test\",\"output_table\":\"test_person\",\"table\":\"person\",\"url\":"
                        + "\"jdbc:mysql://localhost:3306/test?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false\"}}],"
                        + "\"transformers\":[{\"type\":\"sql\",\"config\":"
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

        Assertions.assertEquals(expect, JSONUtils.toJsonString(ruleManager.generateDataQualityParameter()));
    }

    @Test
    public void testMultiTableComparison() throws Exception {
        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = new DataQualityTaskExecutionContext();
        dataQualityTaskExecutionContext.setRuleName("跨表值比对");
        dataQualityTaskExecutionContext.setRuleType(RuleType.MULTI_TABLE_COMPARISON.getCode());

        List<DqRuleInputEntry> defaultInputEntryList = new ArrayList<>();

        DqRuleInputEntry srcConnectorType = new DqRuleInputEntry();
        srcConnectorType.setTitle("源数据类型");
        srcConnectorType.setField("src_connector_type");
        srcConnectorType.setType(FormType.SELECT.getFormType());
        srcConnectorType.setCanEdit(true);
        srcConnectorType.setIsShow(true);
        srcConnectorType.setValue(null);
        srcConnectorType.setPlaceholder("${src_connector_type}");
        srcConnectorType.setOptionSourceType(OptionSourceType.DATASOURCE_TYPE.getCode());
        srcConnectorType.setOptions(null);
        srcConnectorType.setInputType(InputType.DEFAULT.getCode());
        srcConnectorType.setValueType(ValueType.NUMBER.getCode());
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry srcDatasourceId = new DqRuleInputEntry();
        srcDatasourceId.setTitle("源数据源");
        srcDatasourceId.setField("src_datasource_id");
        srcDatasourceId.setType(FormType.CASCADER.getFormType());
        srcDatasourceId.setCanEdit(true);
        srcDatasourceId.setIsShow(true);
        srcDatasourceId.setValue(null);
        srcDatasourceId.setPlaceholder("${comparison_value}");
        srcDatasourceId.setOptionSourceType(OptionSourceType.DATASOURCE_ID.getCode());
        srcDatasourceId.setInputType(InputType.DEFAULT.getCode());
        srcDatasourceId.setValueType(ValueType.NUMBER.getCode());
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry srcTable = new DqRuleInputEntry();
        srcTable.setTitle("源数据表");
        srcTable.setField("src_table");
        srcTable.setType(FormType.INPUT.getFormType());
        srcTable.setCanEdit(true);
        srcTable.setIsShow(true);
        srcTable.setPlaceholder("Please enter source table name");
        srcTable.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        srcTable.setInputType(InputType.DEFAULT.getCode());
        srcTable.setValueType(ValueType.STRING.getCode());
        srcConnectorType.setCreateTime(new Date());
        srcConnectorType.setUpdateTime(new Date());

        DqRuleInputEntry statisticsName = new DqRuleInputEntry();
        statisticsName.setTitle("统计值名");
        statisticsName.setField("statistics_name");
        statisticsName.setType(FormType.INPUT.getFormType());
        statisticsName.setCanEdit(true);
        statisticsName.setIsShow(true);
        statisticsName.setPlaceholder("Please enter statistics name, the alias in statistics execute sql");
        statisticsName.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        statisticsName.setValueType(ValueType.STRING.getCode());
        statisticsName.setInputType(InputType.DEFAULT.getCode());

        DqRuleInputEntry statisticsExecuteSql = new DqRuleInputEntry();
        statisticsExecuteSql.setTitle("统计值计算SQL");
        statisticsExecuteSql.setField("statistics_execute_sql");
        statisticsExecuteSql.setType(FormType.TEXTAREA.getFormType());
        statisticsExecuteSql.setCanEdit(true);
        statisticsExecuteSql.setIsShow(true);
        statisticsExecuteSql.setPlaceholder("Please enter statistics execute sql");
        statisticsExecuteSql.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        statisticsExecuteSql.setValueType(ValueType.LIKE_SQL.getCode());
        statisticsExecuteSql.setInputType(InputType.DEFAULT.getCode());

        DqRuleInputEntry targetConnectorType = new DqRuleInputEntry();
        targetConnectorType.setTitle("目标数据类型");
        targetConnectorType.setField("target_connector_type");
        targetConnectorType.setType(FormType.SELECT.getFormType());
        targetConnectorType.setCanEdit(true);
        targetConnectorType.setIsShow(true);
        targetConnectorType.setValue("JDBC");
        targetConnectorType.setPlaceholder("Please select target connector type");
        targetConnectorType.setOptionSourceType(OptionSourceType.DATASOURCE_TYPE.getCode());
        targetConnectorType.setOptions(null);
        targetConnectorType.setInputType(InputType.DEFAULT.getCode());

        DqRuleInputEntry targetDatasourceId = new DqRuleInputEntry();
        targetDatasourceId.setTitle("目标数据源");
        targetDatasourceId.setField("target_datasource_id");
        targetDatasourceId.setType(FormType.SELECT.getFormType());
        targetDatasourceId.setCanEdit(true);
        targetDatasourceId.setIsShow(true);
        targetDatasourceId.setValue("1");
        targetDatasourceId.setPlaceholder("Please select target datasource");
        targetDatasourceId.setOptionSourceType(OptionSourceType.DATASOURCE_ID.getCode());
        targetDatasourceId.setInputType(InputType.DEFAULT.getCode());

        DqRuleInputEntry targetTable = new DqRuleInputEntry();
        targetTable.setTitle("目标数据表");
        targetTable.setField("target_table");
        targetTable.setType(FormType.INPUT.getFormType());
        targetTable.setCanEdit(true);
        targetTable.setIsShow(true);
        targetTable.setPlaceholder("Please enter target table");
        targetTable.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        targetTable.setValueType(ValueType.STRING.getCode());
        targetTable.setInputType(InputType.DEFAULT.getCode());

        DqRuleInputEntry comparisonName = new DqRuleInputEntry();
        comparisonName.setTitle("比对值名");
        comparisonName.setField("comparison_name");
        comparisonName.setType(FormType.INPUT.getFormType());
        comparisonName.setCanEdit(true);
        comparisonName.setIsShow(true);
        comparisonName.setPlaceholder("Please enter comparison name, the alias in comparison execute sql");
        comparisonName.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        comparisonName.setValueType(ValueType.STRING.getCode());
        comparisonName.setInputType(InputType.DEFAULT.getCode());

        DqRuleInputEntry comparisonExecuteSql = new DqRuleInputEntry();
        comparisonExecuteSql.setTitle("比对值计算SQL");
        comparisonExecuteSql.setField("comparison_execute_sql");
        comparisonExecuteSql.setType(FormType.TEXTAREA.getFormType());
        comparisonExecuteSql.setCanEdit(true);
        comparisonExecuteSql.setIsShow(true);
        comparisonExecuteSql.setPlaceholder("Please enter comparison execute sql");
        comparisonExecuteSql.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        comparisonExecuteSql.setValueType(ValueType.LIKE_SQL.getCode());
        comparisonExecuteSql.setInputType(InputType.DEFAULT.getCode());

        DqRuleInputEntry checkType = new DqRuleInputEntry();
        checkType.setTitle("检测方式");
        checkType.setField("check_type");
        checkType.setType(FormType.SELECT.getFormType());
        checkType.setCanEdit(true);
        checkType.setIsShow(true);
        checkType.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        checkType.setOptions("[{\"label\":\"比对值 - 统计值\",\"value\":\"0\"},{\"label\":\"统计值 - 比对值\",\"value\":\"1\"},"
                + "{\"label\":\"统计值 / 比对值\",\"value\":\"2\"},{\"label\":\"(比对值-统计值) / 比对值\",\"value\":\"3\"}]");
        checkType.setValue("0");
        checkType.setInputType(InputType.CHECK.getCode());
        checkType.setValueType(ValueType.STRING.getCode());
        checkType.setPlaceholder("检测类型");

        DqRuleInputEntry operator = new DqRuleInputEntry();
        operator.setTitle("操作符");
        operator.setField("operator");
        operator.setType(FormType.SELECT.getFormType());
        operator.setCanEdit(true);
        operator.setIsShow(true);
        operator.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        operator.setOptions("[{\"label\":\"=\",\"value\":\"0\"},"
                + "{\"label\":\"<\",\"value\":\"1\"},{\"label\":\"<=\",\"value\":\"2\"},"
                + "{\"label\":\">\",\"value\":\"3\"},{\"label\":\">=\",\"value\":\"4\"},"
                + "{\"label\":\"!=\",\"value\":\"5\"}]");
        operator.setValue("0");
        operator.setInputType(InputType.CHECK.getCode());
        operator.setValueType(ValueType.STRING.getCode());
        operator.setPlaceholder("操作符");

        DqRuleInputEntry threshold = new DqRuleInputEntry();
        threshold.setTitle("阈值");
        threshold.setField("threshold");
        threshold.setType(FormType.INPUT.getFormType());
        threshold.setCanEdit(true);
        threshold.setIsShow(true);
        threshold.setInputType(InputType.CHECK.getCode());
        threshold.setValueType(ValueType.NUMBER.getCode());
        threshold.setPlaceholder("Please enter threshold, number is needed");

        DqRuleInputEntry afterFailure = new DqRuleInputEntry();
        afterFailure.setTitle("失败策略");
        afterFailure.setField("failure_strategy");
        afterFailure.setType(FormType.SELECT.getFormType());
        afterFailure.setCanEdit(true);
        afterFailure.setIsShow(true);
        afterFailure.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        afterFailure.setOptions("[{\"label\":\"告警\",\"value\":\"0\"},{\"label\":\"阻断\",\"value\":\"1\"}]");
        afterFailure.setValue("0");
        afterFailure.setInputType(InputType.CHECK.getCode());
        afterFailure.setValueType(ValueType.STRING.getCode());
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

        dataQualityTaskExecutionContext.setRuleInputEntryList(JSONUtils.toJsonString(defaultInputEntryList));

        Map<String, String> inputParameterValue = new HashMap<>();
        inputParameterValue.put("src_connector_type", "0");
        inputParameterValue.put("src_datasource_id", "2");
        inputParameterValue.put("src_table", "test1");
        inputParameterValue.put("statistics_name", "src");
        inputParameterValue.put("statistics_execute_sql", "select count(*) as src from ${src_table} where c1>20");
        inputParameterValue.put("target_connector_type", "2");
        inputParameterValue.put("target_datasource_id", "3");
        inputParameterValue.put("target_table", "test1_1");
        inputParameterValue.put("comparison_name", "target");
        inputParameterValue.put("comparison_execute_sql", "select count(*) as target from ${target_table} where c1>20");
        inputParameterValue.put("check_type", "1");
        inputParameterValue.put("operator", "3");
        inputParameterValue.put("threshold", "2");
        inputParameterValue.put("failure_strategy", "0");
        inputParameterValue.put("rule_id", "4");
        inputParameterValue.put("rule_type", "3");
        inputParameterValue.put("rule_name", "'跨表值比对'");
        inputParameterValue.put("create_time", "'2021-08-25 00:00:00'");
        inputParameterValue.put("update_time", "'2021-08-25 00:00:00'");
        inputParameterValue.put("process_definition_id", "1");
        inputParameterValue.put("process_instance_id", "1");
        inputParameterValue.put("task_instance_id", "1");
        inputParameterValue.put("data_time", "'2021-08-25 00:00:00'");
        inputParameterValue.put("error_output_path", "hdfs://localhost:8022/user/ods/data_quality_error_data/1_1_1");

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
                + "\"com.mysql.cj.jdbc.Driver\",\"user\":\"test\",\"output_table\":\"test_test1\",\"table\":"
                + "\"test1\",\"url\":\"jdbc:mysql://localhost:3306/test?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false\"}},"
                + "{\"type\":\"HIVE\",\"config\":"
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

        RuleManager ruleManager = new RuleManager(inputParameterValue, dataQualityTaskExecutionContext);
        Assertions.assertEquals(expect, JSONUtils.toJsonString(ruleManager.generateDataQualityParameter()));
    }

    @Test
    public void testMultiTableAccuracy() throws Exception {

        DataQualityTaskExecutionContext dataQualityTaskExecutionContext = new DataQualityTaskExecutionContext();

        List<DqRuleInputEntry> defaultInputEntryList = new ArrayList<>();

        DqRuleInputEntry srcConnectorType = new DqRuleInputEntry();
        srcConnectorType.setTitle("源数据类型");
        srcConnectorType.setField("src_connector_type");
        srcConnectorType.setType(FormType.SELECT.getFormType());
        srcConnectorType.setCanEdit(true);
        srcConnectorType.setIsShow(true);
        srcConnectorType.setValue("JDBC");
        srcConnectorType.setPlaceholder("Please select source connector type");
        srcConnectorType.setOptionSourceType(OptionSourceType.DATASOURCE_TYPE.getCode());
        srcConnectorType.setOptions(null);
        srcConnectorType.setInputType(InputType.DEFAULT.getCode());
        srcConnectorType.setValueType(ValueType.NUMBER.getCode());

        DqRuleInputEntry srcDatasourceId = new DqRuleInputEntry();
        srcDatasourceId.setTitle("源数据源");
        srcDatasourceId.setField("src_datasource_id");
        srcDatasourceId.setType(FormType.SELECT.getFormType());
        srcDatasourceId.setCanEdit(true);
        srcDatasourceId.setIsShow(true);
        srcDatasourceId.setValue("1");
        srcDatasourceId.setPlaceholder("Please select source datasource");
        srcDatasourceId.setOptionSourceType(OptionSourceType.DATASOURCE_ID.getCode());
        srcDatasourceId.setInputType(InputType.DEFAULT.getCode());
        srcDatasourceId.setValueType(ValueType.NUMBER.getCode());

        DqRuleInputEntry srcTable = new DqRuleInputEntry();
        srcTable.setTitle("源数据表");
        srcTable.setField("src_table");
        srcTable.setType(FormType.INPUT.getFormType());
        srcTable.setCanEdit(true);
        srcTable.setIsShow(true);
        srcTable.setPlaceholder("Please enter source table");
        srcTable.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        srcTable.setInputType(InputType.DEFAULT.getCode());
        srcTable.setValueType(ValueType.STRING.getCode());

        DqRuleInputEntry srcFilter = new DqRuleInputEntry();
        srcFilter.setTitle("源表过滤条件");
        srcFilter.setField("src_filter");
        srcFilter.setType(FormType.INPUT.getFormType());
        srcFilter.setCanEdit(true);
        srcFilter.setIsShow(true);
        srcFilter.setPlaceholder("Please enter source filter expression");
        srcFilter.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        srcFilter.setInputType(InputType.DEFAULT.getCode());
        srcFilter.setValueType(ValueType.LIKE_SQL.getCode());

        DqRuleInputEntry targetConnectorType = new DqRuleInputEntry();
        targetConnectorType.setTitle("目标数据类型");
        targetConnectorType.setField("target_connector_type");
        targetConnectorType.setType(FormType.SELECT.getFormType());
        targetConnectorType.setCanEdit(true);
        targetConnectorType.setIsShow(true);
        targetConnectorType.setValue("JDBC");
        targetConnectorType.setPlaceholder("Please select target connector type");
        targetConnectorType.setOptionSourceType(OptionSourceType.DATASOURCE_TYPE.getCode());
        targetConnectorType.setOptions(null);
        targetConnectorType.setInputType(InputType.DEFAULT.getCode());
        targetConnectorType.setValueType(ValueType.STRING.getCode());

        DqRuleInputEntry targetDatasourceId = new DqRuleInputEntry();
        targetDatasourceId.setTitle("目标数据源");
        targetDatasourceId.setField("target_datasource_id");
        targetDatasourceId.setType(FormType.CASCADER.getFormType());
        targetDatasourceId.setCanEdit(true);
        targetDatasourceId.setIsShow(true);
        targetDatasourceId.setValue("1");
        targetDatasourceId.setPlaceholder("Please select target datasource");
        targetDatasourceId.setOptionSourceType(OptionSourceType.DATASOURCE_ID.getCode());
        targetDatasourceId.setInputType(InputType.DEFAULT.getCode());
        targetDatasourceId.setValueType(ValueType.NUMBER.getCode());

        DqRuleInputEntry targetTable = new DqRuleInputEntry();
        targetTable.setTitle("目标数据表");
        targetTable.setField("target_table");
        targetTable.setType(FormType.INPUT.getFormType());
        targetTable.setCanEdit(true);
        targetTable.setIsShow(true);
        targetTable.setPlaceholder("Please enter target table");
        targetTable.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        targetTable.setInputType(InputType.DEFAULT.getCode());
        targetTable.setValueType(ValueType.STRING.getCode());

        DqRuleInputEntry targetFilter = new DqRuleInputEntry();
        targetFilter.setTitle("目标表过滤条件");
        targetFilter.setField("target_filter");
        targetFilter.setType(FormType.INPUT.getFormType());
        targetFilter.setCanEdit(true);
        targetFilter.setIsShow(true);
        targetFilter.setPlaceholder("Please enter target filter expression");
        targetFilter.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        targetFilter.setInputType(InputType.DEFAULT.getCode());
        targetFilter.setValueType(ValueType.LIKE_SQL.getCode());

        DqRuleInputEntry mappingColumns = new DqRuleInputEntry();
        mappingColumns.setTitle("检查列");
        mappingColumns.setField("mapping_columns");
        mappingColumns.setType(FormType.INPUT.getFormType());
        mappingColumns.setCanEdit(true);
        mappingColumns.setIsShow(true);
        mappingColumns.setPlaceholder("${mapping_columns}");
        mappingColumns.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        mappingColumns.setInputType(InputType.DEFAULT.getCode());
        mappingColumns.setValueType(ValueType.LIST.getCode());

        DqRuleInputEntry statisticsName = new DqRuleInputEntry();
        statisticsName.setTitle("统计值");
        statisticsName.setField("statistics_name");
        statisticsName.setType(FormType.INPUT.getFormType());
        statisticsName.setCanEdit(false);
        statisticsName.setIsShow(false);
        statisticsName.setValue("miss_count.miss");
        statisticsName.setPlaceholder("${statistics_name}");
        statisticsName.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        statisticsName.setInputType(InputType.DEFAULT.getCode());
        statisticsName.setValueType(ValueType.STRING.getCode());

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
        executeSqlDefinition3.setType(ExecuteSqlType.MIDDLE.getCode());

        DqRuleExecuteSql executeSqlDefinition1 = new DqRuleExecuteSql();
        executeSqlDefinition1.setIndex(0);
        executeSqlDefinition1.setSql("SELECT ${src_table}.* FROM (SELECT * FROM ${src_table} WHERE (${src_filter})) "
                + "${src_table} LEFT JOIN (SELECT * FROM ${target_table} WHERE (${target_filter})) "
                + "${target_table} ON ${on_clause} WHERE ${where_clause}");
        executeSqlDefinition1.setTableAlias("miss_items");
        executeSqlDefinition1.setType(ExecuteSqlType.MIDDLE.getCode());
        executeSqlDefinition1.setErrorOutputSql(true);

        DqRuleExecuteSql executeSqlDefinition2 = new DqRuleExecuteSql();
        executeSqlDefinition2.setIndex(0);
        executeSqlDefinition2.setSql("SELECT COUNT(*) AS miss FROM miss_items");
        executeSqlDefinition2.setTableAlias("miss_count");
        executeSqlDefinition2.setType(ExecuteSqlType.STATISTICS.getCode());

        DqRuleInputEntry comparisonTitle = new DqRuleInputEntry();
        comparisonTitle.setTitle("比对值");
        comparisonTitle.setField("comparison_title");
        comparisonTitle.setType(FormType.INPUT.getFormType());
        comparisonTitle.setCanEdit(false);
        comparisonTitle.setIsShow(true);
        comparisonTitle.setPlaceholder("${comparison_title}");
        comparisonTitle.setValue("目标表总行数");

        DqRuleInputEntry comparisonName = new DqRuleInputEntry();
        comparisonName.setTitle("比对值名");
        comparisonName.setField("comparison_name");
        comparisonName.setType(FormType.INPUT.getFormType());
        comparisonName.setCanEdit(false);
        comparisonName.setIsShow(false);
        comparisonName.setValue("total_count.total");
        comparisonName.setPlaceholder("${comparison_name}");

        DqRuleInputEntry comparisonTable = new DqRuleInputEntry();
        comparisonTable.setField(COMPARISON_TABLE);
        comparisonTable.setValue("total_count");

        DqRuleInputEntry checkType = new DqRuleInputEntry();
        checkType.setTitle("检测方式");
        checkType.setField("check_type");
        checkType.setType(FormType.SELECT.getFormType());
        checkType.setCanEdit(true);
        checkType.setIsShow(true);
        checkType.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        checkType.setOptions(
                "[{\"label\":\"比对值 - 统计值\",\"value\":\"0\"},{\"label\":\"统计值 - 比对值\",\"value\":\"1\"},{\"label\":\"统计值 / 比对值\","
                        + "\"value\":\"2\"},{\"label\":\"(比对值-统计值) / 比对值\",\"value\":\"3\"}]");
        checkType.setValue("0");
        checkType.setInputType(InputType.CHECK.getCode());
        checkType.setValueType(ValueType.STRING.getCode());
        checkType.setPlaceholder("检测类型");

        DqRuleInputEntry operator = new DqRuleInputEntry();
        operator.setTitle("操作符");
        operator.setField("operator");
        operator.setType(FormType.SELECT.getFormType());
        operator.setCanEdit(true);
        operator.setIsShow(true);
        operator.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        operator.setOptions("[{\"label\":\"=\",\"value\":\"0\"},"
                + "{\"label\":\"<\",\"value\":\"1\"},{\"label\":\"<=\",\"value\":\"2\"},"
                + "{\"label\":\">\",\"value\":\"3\"},{\"label\":\">=\",\"value\":\"4\"},{\"label\":\"!=\",\"value\":\"5\"}]");
        operator.setValue("0");
        operator.setInputType(InputType.CHECK.getCode());
        operator.setValueType(ValueType.STRING.getCode());
        operator.setPlaceholder("操作符");

        DqRuleInputEntry threshold = new DqRuleInputEntry();
        threshold.setTitle("阈值");
        threshold.setField("threshold");
        threshold.setType(FormType.INPUT.getFormType());
        threshold.setCanEdit(true);
        threshold.setIsShow(true);
        threshold.setInputType(InputType.CHECK.getCode());
        threshold.setValueType(ValueType.NUMBER.getCode());
        threshold.setPlaceholder("Please enter threshold, number is needed");

        DqRuleInputEntry afterFailure = new DqRuleInputEntry();
        afterFailure.setTitle("失败策略");
        afterFailure.setField("failure_strategy");
        afterFailure.setType(FormType.SELECT.getFormType());
        afterFailure.setCanEdit(true);
        afterFailure.setIsShow(true);
        afterFailure.setOptionSourceType(OptionSourceType.DEFAULT.getCode());
        afterFailure.setOptions("[{\"label\":\"告警\",\"value\":\"0\"},{\"label\":\"阻断\",\"value\":\"1\"}]");
        afterFailure.setValue("0");
        afterFailure.setInputType(InputType.CHECK.getCode());
        afterFailure.setValueType(ValueType.STRING.getCode());
        afterFailure.setPlaceholder("失败策略");

        defaultInputEntryList.add(checkType);
        defaultInputEntryList.add(operator);
        defaultInputEntryList.add(threshold);
        defaultInputEntryList.add(afterFailure);
        defaultInputEntryList.add(comparisonTitle);
        defaultInputEntryList.add(comparisonName);
        defaultInputEntryList.add(comparisonTable);

        List<DqRuleExecuteSql> executeSqlList = new ArrayList<>();
        executeSqlList.add(executeSqlDefinition3);
        executeSqlList.add(executeSqlDefinition1);
        executeSqlList.add(executeSqlDefinition2);
        dataQualityTaskExecutionContext.setExecuteSqlList(JSONUtils.toJsonString(executeSqlList));
        dataQualityTaskExecutionContext.setRuleInputEntryList(JSONUtils.toJsonString(defaultInputEntryList));

        Map<String, String> inputParameterValue = new HashMap<>();
        inputParameterValue.put("src_connector_type", "0");
        inputParameterValue.put("src_datasource_id", "2");
        inputParameterValue.put("src_table", "demo_src");
        inputParameterValue.put("src_filter", "age<100");
        inputParameterValue.put("target_connector_type", "2");
        inputParameterValue.put("target_datasource_id", "3");
        inputParameterValue.put("target_table", "demo_src");
        inputParameterValue.put("target_filter", "age<100");
        inputParameterValue.put("mapping_columns",
                "[{\"src_field\":\"hour\",\"operator\":\"=\",\"target_field\":\"hour\"}]");
        inputParameterValue.put("check_type", "2");
        inputParameterValue.put("operator", "3");
        inputParameterValue.put("threshold", "3");
        inputParameterValue.put("failure_strategy", "0");
        inputParameterValue.put("comparison_type", "7");
        inputParameterValue.put("rule_id", "3");
        inputParameterValue.put("rule_type", "2");
        inputParameterValue.put("rule_name", "'跨表准确性'");
        inputParameterValue.put("create_time", "'2021-08-30 00:00:00'");
        inputParameterValue.put("update_time", "'2021-08-30 00:00:00'");
        inputParameterValue.put("process_definition_id", "1");
        inputParameterValue.put("process_instance_id", "1");
        inputParameterValue.put("task_instance_id", "1");
        inputParameterValue.put("data_time", "'2021-08-30 00:00:00'");
        inputParameterValue.put("error_output_path", "hdfs://localhost:8022/user/ods/data_quality_error_data/1_1_test");

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
        dataQualityTaskExecutionContext.setRuleType(RuleType.MULTI_TABLE_ACCURACY.getCode());

        String expect = "{\"name\":\"跨表准确性\",\"env\":{\"type\":\"batch\",\"config\":null},\"readers\":"
                + "[{\"type\":\"JDBC\",\"config\":{\"database\":\"test\",\"password\":\"test\",\"driver\":"
                + "\"com.mysql.cj.jdbc.Driver\",\"user\":\"test\",\"output_table\":\"test_demo_src\",\"table\":"
                + "\"demo_src\",\"url\":\"jdbc:mysql://localhost:3306/test?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false\"}},"
                + "{\"type\":\"HIVE\",\"config\":"
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

        RuleManager ruleManager = new RuleManager(inputParameterValue, dataQualityTaskExecutionContext);
        Assertions.assertEquals(expect, JSONUtils.toJsonString(ruleManager.generateDataQualityParameter()));
    }
}
