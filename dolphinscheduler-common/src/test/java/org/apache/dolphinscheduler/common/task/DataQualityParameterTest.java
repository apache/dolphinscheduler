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

package org.apache.dolphinscheduler.common.task;

import org.apache.dolphinscheduler.common.form.CascaderParamsOptions;
import org.apache.dolphinscheduler.common.form.ParamsOptions;
import org.apache.dolphinscheduler.common.form.PluginParams;
import org.apache.dolphinscheduler.common.form.TriggerType;
import org.apache.dolphinscheduler.common.form.Validate;
import org.apache.dolphinscheduler.common.form.props.CascaderParamsProps;
import org.apache.dolphinscheduler.common.form.props.InputParamsProps;
import org.apache.dolphinscheduler.common.form.props.SelectParamsProps;
import org.apache.dolphinscheduler.common.form.type.CascaderParam;
import org.apache.dolphinscheduler.common.form.type.InputParam;
import org.apache.dolphinscheduler.common.form.type.SelectParam;
import org.apache.dolphinscheduler.common.task.dq.DataQualityParameters;
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DataQualityParameterTest
 */
public class DataQualityParameterTest {

    private DataQualityParameters dataQualityParameters = null;

    @Before
    public void before() {
        dataQualityParameters = new DataQualityParameters();
        dataQualityParameters.setRuleId(1);
        String ruleJson = "{\n"
                + "\t\"ruleName\": \"空值检测\",\n"
                + "\t\"ruleType\": 0,\n"
                + "\t\"ruleInputEntryList\": [{\n"
                + "\t\t\"field\": \"check_type\",\n"
                + "\t\t\"type\": \"select\",\n"
                + "\t\t\"title\": \"检测方式\",\n"
                + "\t\t\"value\": \"0\",\n"
                + "\t\t\"options\": "
                + "\"[{\\\"label\\\":\\\"统计值与固定值比较\\\",\\\"value\\\":\\\"0\\\"},"
                + "{\\\"label\\\":\\\"统计值与比对值比较\\\",\\\"value\\\":\\\"1\\\"},"
                + "{\\\"label\\\":\\\"统计值占比对值百分比\\\",\\\"value\\\":\\\"2\\\"}]\",\n"
                + "\t\t\"placeholder\": \"检测类型\",\n"
                + "\t\t\"optionSourceType\": 0,\n"
                + "\t\t\"valueType\": \"string\",\n"
                + "\t\t\"inputType\": 3,\n"
                + "\t\t\"canEdit\": true,\n"
                + "\t\t\"show\": true\n"
                + "\t}, {\n"
                + "\t\t\"field\": \"operator\",\n"
                + "\t\t\"type\": \"select\",\n"
                + "\t\t\"title\": \"操作符\",\n"
                + "\t\t\"value\": \"0\",\n"
                + "\t\t\"options\": "
                + "\"[{\\\"label\\\":\\\"=\\\",\\\"value\\\":\\\"0\\\"},"
                + "{\\\"label\\\":\\\"<\\\",\\\"value\\\":\\\"1\\\"},"
                + "{\\\"label\\\":\\\"<=\\\",\\\"value\\\":\\\"2\\\"},"
                + "{\\\"label\\\":\\\">\\\",\\\"value\\\":\\\"3\\\"},"
                + "{\\\"label\\\":\\\">=\\\",\\\"value\\\":\\\"4\\\"},"
                + "{\\\"label\\\":\\\"!=\\\",\\\"value\\\":\\\"5\\\"}]\",\n"
                + "\t\t\"placeholder\": \"操作符\",\n"
                + "\t\t\"optionSourceType\": 0,\n"
                + "\t\t\"valueType\": \"string\",\n"
                + "\t\t\"inputType\": 3,\n"
                + "\t\t\"canEdit\": true,\n"
                + "\t\t\"show\": true\n"
                + "\t}, {\n"
                + "\t\t\"field\": \"threshold\",\n"
                + "\t\t\"type\": \"input\",\n"
                + "\t\t\"title\": \"阈值\",\n"
                + "\t\t\"value\": null,\n"
                + "\t\t\"options\": null,\n"
                + "\t\t\"placeholder\": \"Please enter threshold, number is needed\",\n"
                + "\t\t\"optionSourceType\": 0,\n"
                + "\t\t\"valueType\": \"number\",\n"
                + "\t\t\"inputType\": 3,\n"
                + "\t\t\"canEdit\": true,\n"
                + "\t\t\"show\": true\n"
                + "\t}, {\n"
                + "\t\t\"field\": \"failure_strategy\",\n"
                + "\t\t\"type\": \"select\",\n"
                + "\t\t\"title\": \"失败策略\",\n"
                + "\t\t\"value\": \"0\",\n"
                + "\t\t\"options\": "
                + "\"[{\\\"label\\\":\\\"结束\\\",\\\"value\\\":\\\"0\\\"},"
                + "{\\\"label\\\":\\\"继续\\\",\\\"value\\\":\\\"1\\\"},"
                + "{\\\"label\\\":\\\"结束并告警\\\",\\\"value\\\":\\\"2\\\"},"
                + "{\\\"label\\\":\\\"继续并告警\\\",\\\"value\\\":\\\"3\\\"}]\",\n"
                + "\t\t\"placeholder\": \"失败策略\",\n"
                + "\t\t\"optionSourceType\": 0,\n"
                + "\t\t\"valueType\": \"string\",\n"
                + "\t\t\"inputType\": 3,\n"
                + "\t\t\"canEdit\": true,\n"
                + "\t\t\"show\": true\n"
                + "\t}, {\n"
                + "\t\t\"field\": \"src_connector_type\",\n"
                + "\t\t\"type\": \"select\",\n"
                + "\t\t\"title\": \"源数据类型\",\n"
                + "\t\t\"value\": \"JDBC\",\n"
                + "\t\t\"options\": "
                + "\"[{\\\"label\\\":\\\"HIVE\\\",\\\"value\\\":\\\"HIVE\\\"},"
                + "{\\\"label\\\":\\\"JDBC\\\",\\\"value\\\":\\\"JDBC\\\"}]\",\n"
                + "\t\t\"placeholder\": \"${src_connector_type}\",\n"
                + "\t\t\"optionSourceType\": 0,\n"
                + "\t\t\"valueType\": \"number\",\n"
                + "\t\t\"inputType\": 0,\n"
                + "\t\t\"canEdit\": true,\n"
                + "\t\t\"show\": true\n"
                + "\t}, {\n"
                + "\t\t\"field\": \"src_datasource_id\",\n"
                + "\t\t\"type\": \"cascader\",\n"
                + "\t\t\"title\": \"源数据源\",\n"
                + "\t\t\"value\": \"1\",\n"
                + "\t\t\"options\": null,\n"
                + "\t\t\"placeholder\": \"${comparison_value}\",\n"
                + "\t\t\"optionSourceType\": 1,\n"
                + "\t\t\"valueType\": \"number\",\n"
                + "\t\t\"inputType\": 0,\n"
                + "\t\t\"canEdit\": true,\n"
                + "\t\t\"show\": true\n"
                + "\t}, {\n"
                + "\t\t\"field\": \"src_table\",\n"
                + "\t\t\"type\": \"input\",\n"
                + "\t\t\"title\": \"源数据表\",\n"
                + "\t\t\"value\": null,\n"
                + "\t\t\"options\": null,\n"
                + "\t\t\"placeholder\": \"Please enter source table name\",\n"
                + "\t\t\"optionSourceType\": 0,\n"
                + "\t\t\"valueType\": \"string\",\n"
                + "\t\t\"inputType\": 0,\n"
                + "\t\t\"canEdit\": true,\n"
                + "\t\t\"show\": true\n"
                + "\t}, {\n"
                + "\t\t\"field\": \"src_filter\",\n"
                + "\t\t\"type\": \"input\",\n"
                + "\t\t\"title\": \"源表过滤条件\",\n"
                + "\t\t\"value\": null,\n"
                + "\t\t\"options\": null,\n"
                + "\t\t\"placeholder\": \"Please enter filter expression\",\n"
                + "\t\t\"optionSourceType\": 0,\n"
                + "\t\t\"valueType\": \"sql\",\n"
                + "\t\t\"inputType\": 0,\n"
                + "\t\t\"canEdit\": true,\n"
                + "\t\t\"show\": true\n"
                + "\t}, {\n"
                + "\t\t\"field\": \"src_field\",\n"
                + "\t\t\"type\": \"input\",\n"
                + "\t\t\"title\": \"检测列\",\n"
                + "\t\t\"value\": \"\",\n"
                + "\t\t\"options\": null,\n"
                + "\t\t\"placeholder\": \"Please enter column, only single column is supported\",\n"
                + "\t\t\"optionSourceType\": 0,\n"
                + "\t\t\"valueType\": \"string\",\n"
                + "\t\t\"inputType\": 0,\n"
                + "\t\t\"canEdit\": true,\n"
                + "\t\t\"show\": true\n"
                + "\t}, {\n"
                + "\t\t\"field\": \"statistics_name\",\n"
                + "\t\t\"type\": \"input\",\n"
                + "\t\t\"title\": \"统计值\",\n"
                + "\t\t\"value\": \"miss_items.miss\",\n"
                + "\t\t\"options\": null,\n"
                + "\t\t\"placeholder\": \"${statistics_name}\",\n"
                + "\t\t\"optionSourceType\": 0,\n"
                + "\t\t\"valueType\": \"string\",\n"
                + "\t\t\"inputType\": 1,\n"
                + "\t\t\"canEdit\": false,\n"
                + "\t\t\"show\": false\n"
                + "\t}],\n"
                + "\t\"midExecuteSqlList\": null,\n"
                + "\t\"statisticsExecuteSqlList\": [{\n"
                + "\t\t\"index\": 0,\n"
                + "\t\t\"sql\": "
                + "\"SELECT count(*) AS miss FROM ${src_table} "
                + "WHERE (${src_field} is null or ${src_field} = '') AND (${src_filter}) \",\n"
                + "\t\t\"tableAlias\": \"miss_items\",\n"
                + "\t\t\"mid\": false\n"
                + "\t}],\n"
                + "\t\"comparisonParameter\": {\n"
                + "\t\t\"inputEntryList\": [{\n"
                + "\t\t\t\"field\": \"comparison_title\",\n"
                + "\t\t\t\"type\": \"input\",\n"
                + "\t\t\t\"title\": \"比对值\",\n"
                + "\t\t\t\"value\": \"表总行数\",\n"
                + "\t\t\t\"options\": null,\n"
                + "\t\t\t\"placeholder\": \"${comparison_title}\",\n"
                + "\t\t\t\"optionSourceType\": 0,\n"
                + "\t\t\t\"valueType\": \"string\",\n"
                + "\t\t\t\"inputType\": 2,\n"
                + "\t\t\t\"canEdit\": false,\n"
                + "\t\t\t\"show\": true\n"
                + "\t\t}, {\n"
                + "\t\t\t\"field\": \"comparison_value\",\n"
                + "\t\t\t\"type\": \"input\",\n"
                + "\t\t\t\"title\": \"比对值\",\n"
                + "\t\t\t\"value\": null,\n"
                + "\t\t\t\"options\": null,\n"
                + "\t\t\t\"placeholder\": \"${comparison_value}\",\n"
                + "\t\t\t\"optionSourceType\": 0,\n"
                + "\t\t\t\"valueType\": \"number\",\n"
                + "\t\t\t\"inputType\": 2,\n"
                + "\t\t\t\"canEdit\": false,\n"
                + "\t\t\t\"show\": false\n"
                + "\t\t}, {\n"
                + "\t\t\t\"field\": \"comparison_name\",\n"
                + "\t\t\t\"type\": \"input\",\n"
                + "\t\t\t\"title\": \"比对值名\",\n"
                + "\t\t\t\"value\": \"total_count.total\",\n"
                + "\t\t\t\"options\": null,\n"
                + "\t\t\t\"placeholder\": \"${comparison_name}\",\n"
                + "\t\t\t\"optionSourceType\": 0,\n"
                + "\t\t\t\"valueType\": \"string\",\n"
                + "\t\t\t\"inputType\": 2,\n"
                + "\t\t\t\"canEdit\": false,\n"
                + "\t\t\t\"show\": false\n"
                + "\t\t}],\n"
                + "\t\t\"comparisonExecuteSqlList\": [{\n"
                + "\t\t\t\"index\": 0,\n"
                + "\t\t\t\"sql\": \"SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})\",\n"
                + "\t\t\t\"tableAlias\": \"total_count\",\n"
                + "\t\t\t\"mid\": false\n"
                + "\t\t}]\n"
                + "\t}\n"
                + "}";
        dataQualityParameters.setSparkParameters(new SparkParameters());
    }

    @Test
    public void testCheckParameterNormal() {

        Map<String,String> inputParameterValue = new HashMap<>();
        inputParameterValue.put("src_connector_type","JDBC");
        inputParameterValue.put("src_datasource_id","1");
        inputParameterValue.put("src_table","test1");
        inputParameterValue.put("src_filter","date=2012-10-05");
        inputParameterValue.put("src_field","id");

        inputParameterValue.put("rule_type","1");
        inputParameterValue.put("process_definition_id","1");
        inputParameterValue.put("task_instance_id","1");
        inputParameterValue.put("check_type","1");
        inputParameterValue.put("threshold","1000");
        inputParameterValue.put("create_time","2012-10-05");
        inputParameterValue.put("update_time","2012-10-05");

        dataQualityParameters.setRuleInputParameter(inputParameterValue);

        Assert.assertTrue(dataQualityParameters.checkParameters());
    }

    @Test
    public void testRuleInputParameter() {
        String formCreateJson = "[{\"field\":\"src_connector_type\","
                + "\"props\":{\"multiple\":false,\"disabled\":false,\"size\":\"small\"},\"type\":\"select\",\"title\":\"源数据类型\",\"value\":\"JDBC\","
                + "\"options\":[{\"label\":\"HIVE\",\"value\":\"HIVE\",\"disabled\":false},{\"label\":\"JDBC\",\"value\":\"JDBC\",\"disabled\":false}]},"
                + "{\"field\":\"src_datasource_id\","
                + "\"props\":{\"options\":"
                + "[{\"label\":\"mysql\",\"value\":\"0\",\"disabled\":false,\"children\":[{\"label\":\"mysql数据源\",\"value\":1,\"disabled\":false}]}],"
                + "\"changeOnSelect\":false,\"size\":\"small\"},\"type\":\"cascader\",\"title\":\"源数据源\",\"value\":1},{\"field\":\"src_table\","
                + "\"props\":{\"placeholder\":\"Please enter source table name\",\"rows\":0,\"disabled\":false,\"size\":\"small\"},\"type\":\"input\",\"title\":\"源数据表\","
                + "\"validate\":[{\"required\":true,\"type\":\"string\",\"trigger\":\"blur\"}]}]";

        List<PluginParams> pluginParamsList = new ArrayList<>();
        SelectParam srcConnectorType = SelectParam.newBuilder("src_connector_type","源数据类型")
                .setProps(new SelectParamsProps().setMultiple(false).setDisabled(false).setSize("small"))
                .addParamsOptions(new ParamsOptions("HIVE","HIVE",false))
                .addParamsOptions(new ParamsOptions("JDBC","JDBC",false))
                .setValue("JDBC")
                .build();

        List<CascaderParamsOptions> children = new ArrayList<>();
        CascaderParamsOptions child = new CascaderParamsOptions("mysql数据源",1,false);
        children.add(child);
        CascaderParamsOptions root = new CascaderParamsOptions("mysql","0",children,false);

        CascaderParam srcDatasourceId = CascaderParam.newBuilder("src_datasource_id","源数据源")
                .setProps(new CascaderParamsProps().setOption(root).setChangeOnSelect(false).setSize("small"))
                .setValue(1)
                .build();

        InputParam srcTable = InputParam.newBuilder("src_table","源数据表")
                .setProps(new InputParamsProps()
                        .setPlaceholder("Please enter source table name")
                        .setDisabled(false)
                        .setSize("small")
                        .setRows(0))
                .addValidate(Validate.newBuilder().setType("string").setRequired(true).setTrigger(TriggerType.BLUR.getTriggerType()).build())
                .build();

        pluginParamsList.add(srcConnectorType);
        pluginParamsList.add(srcDatasourceId);
        pluginParamsList.add(srcTable);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;

        try {
            result = mapper.writeValueAsString(pluginParamsList);
        } catch (JsonProcessingException e) {
            Assert.fail();
        }

        Assert.assertEquals(formCreateJson,result);
    }
}
