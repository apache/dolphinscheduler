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

import org.apache.dolphinscheduler.common.form.ParamsOptions;
import org.apache.dolphinscheduler.common.form.PluginParams;
import org.apache.dolphinscheduler.common.form.TriggerType;
import org.apache.dolphinscheduler.common.form.Validate;
import org.apache.dolphinscheduler.common.form.props.InputParamsProps;
import org.apache.dolphinscheduler.common.form.props.SelectParamsProps;
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
                + "{\"field\":\"src_table\","
                + "\"props\":{\"placeholder\":\"Please enter source table name\",\"rows\":0,\"disabled\":false,\"size\":\"small\"},\"type\":\"input\",\"title\":\"源数据表\","
                + "\"validate\":[{\"required\":true,\"type\":\"string\",\"trigger\":\"blur\"}]}]";

        List<PluginParams> pluginParamsList = new ArrayList<>();
        SelectParam srcConnectorType = SelectParam.newBuilder("src_connector_type","源数据类型")
                .setProps(new SelectParamsProps().setMultiple(false).setDisabled(false).setSize("small"))
                .addParamsOptions(new ParamsOptions("HIVE","HIVE",false))
                .addParamsOptions(new ParamsOptions("JDBC","JDBC",false))
                .setValue("JDBC")
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
