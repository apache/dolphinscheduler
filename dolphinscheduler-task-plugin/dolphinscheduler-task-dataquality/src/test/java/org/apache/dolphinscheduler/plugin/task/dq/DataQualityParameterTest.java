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

import org.apache.dolphinscheduler.plugin.task.api.parameters.dataquality.DataQualityParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.dataquality.spark.SparkParameters;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.TriggerType;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.input.InputParamProps;
import org.apache.dolphinscheduler.spi.params.select.SelectParam;
import org.apache.dolphinscheduler.spi.params.select.SelectParamProps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DataQualityParameterTest
 */
public class DataQualityParameterTest {

    private DataQualityParameters dataQualityParameters = null;

    @BeforeEach
    public void before() {
        dataQualityParameters = new DataQualityParameters();
        dataQualityParameters.setRuleId(1);
        dataQualityParameters.setSparkParameters(new SparkParameters());
    }

    @Test
    public void testCheckParameterNormal() {

        Map<String, String> inputParameterValue = new HashMap<>();
        inputParameterValue.put("src_connector_type", "JDBC");
        inputParameterValue.put("src_datasource_id", "1");
        inputParameterValue.put("src_table", "test1");
        inputParameterValue.put("src_filter", "date=2012-10-05");
        inputParameterValue.put("src_field", "id");

        inputParameterValue.put("rule_type", "1");
        inputParameterValue.put("process_definition_id", "1");
        inputParameterValue.put("task_instance_id", "1");
        inputParameterValue.put("check_type", "1");
        inputParameterValue.put("threshold", "1000");
        inputParameterValue.put("create_time", "2012-10-05");
        inputParameterValue.put("update_time", "2012-10-05");

        dataQualityParameters.setRuleInputParameter(inputParameterValue);

        Assertions.assertTrue(dataQualityParameters.checkParameters());
    }

    @Test
    public void testRuleInputParameter() {
        String formCreateJson = "[{\"field\":\"src_connector_type\",\"name\":\"源数据类型\","
                + "\"props\":{\"disabled\":false,\"multiple\":false,\"size\":\"small\"},"
                + "\"type\":\"select\",\"title\":\"源数据类型\",\"value\":\"JDBC\","
                + "\"options\":[{\"label\":\"HIVE\",\"value\":\"HIVE\",\"disabled\":false},"
                + "{\"label\":\"JDBC\",\"value\":\"JDBC\",\"disabled\":false}]},"
                + "{\"props\":{\"disabled\":false,\"rows\":0,\"placeholder\":\"Please enter source table name\","
                + "\"size\":\"small\"},\"field\":\"src_table\",\"name\":\"源数据表\","
                + "\"type\":\"input\",\"title\":\"源数据表\",\"validate\":[{\"required\":true,\"type\":\"string\","
                + "\"trigger\":\"blur\"}]}]";

        List<PluginParams> pluginParamsList = new ArrayList<>();
        SelectParamProps selectParamProps = new SelectParamProps();
        selectParamProps.setMultiple(false);
        selectParamProps.setDisabled(false);
        selectParamProps.setSize("small");

        SelectParam srcConnectorType = SelectParam.newBuilder("src_connector_type", "源数据类型")
                .setProps(selectParamProps)
                .addOptions(new ParamsOptions("HIVE", "HIVE", false))
                .addOptions(new ParamsOptions("JDBC", "JDBC", false))
                .setValue("JDBC")
                .build();

        InputParamProps inputParamProps = new InputParamProps();
        inputParamProps.setPlaceholder("Please enter source table name");
        inputParamProps.setDisabled(false);
        inputParamProps.setSize("small");
        inputParamProps.setRows(0);

        InputParam srcTable = InputParam.newBuilder("src_table", "源数据表")
                .setProps(inputParamProps)
                .addValidate(Validate.newBuilder().setType("string").setRequired(true)
                        .setTrigger(TriggerType.BLUR.getTriggerType()).build())
                .build();

        pluginParamsList.add(srcConnectorType);
        pluginParamsList.add(srcTable);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;

        try {
            result = mapper.writeValueAsString(pluginParamsList);
        } catch (JsonProcessingException e) {
            Assertions.fail();
        }

        Assertions.assertEquals(formCreateJson, result);
    }
}
