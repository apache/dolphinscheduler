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

package org.apache.dolphinscheduler.plugin.task.api.parameters;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.SQLTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SqlParametersTest {

    private final String type = "MYSQL";
    private final String sql = "select * from t_ds_user";
    private final int datasource = 1;
    private final int sqlType = 0;
    private final Boolean sendEmail = true;
    private final int displayRows = 10;
    private final String showType = "TABLE";
    private final String title = "sql test";
    private final int groupId = 0;

    @Test
    public void testSqlParameters() {
        List<Property> properties = new ArrayList<>();
        Property property = new Property();
        property.setProp("test1");
        property.setDirect(Direct.OUT);
        property.setType(DataType.VARCHAR);
        property.setValue("test1");
        properties.add(property);

        SqlParameters sqlParameters = new SqlParameters();
        Assertions.assertTrue(CollectionUtils.isEmpty(sqlParameters.getResourceFilesList()));

        sqlParameters.setType(type);
        sqlParameters.setSql(sql);
        sqlParameters.setDatasource(datasource);
        sqlParameters.setSqlType(sqlType);
        sqlParameters.setSendEmail(sendEmail);
        sqlParameters.setDisplayRows(displayRows);
        sqlParameters.setShowType(showType);
        sqlParameters.setTitle(title);
        sqlParameters.setGroupId(groupId);

        Assertions.assertEquals(type, sqlParameters.getType());
        Assertions.assertEquals(sql, sqlParameters.getSql());
        Assertions.assertEquals(datasource, sqlParameters.getDatasource());
        Assertions.assertEquals(sqlType, sqlParameters.getSqlType());
        Assertions.assertEquals(sendEmail, sqlParameters.getSendEmail());
        Assertions.assertEquals(displayRows, sqlParameters.getDisplayRows());
        Assertions.assertEquals(showType, sqlParameters.getShowType());
        Assertions.assertEquals(title, sqlParameters.getTitle());
        Assertions.assertEquals(groupId, sqlParameters.getGroupId());

        String sqlResult = "[{\"id\":6,\"test1\":\"6\"},{\"id\":70002,\"test1\":\"+1\"}]";
        String sqlResult1 = "[{\"id\":6,\"test1\":\"6\"}]";
        sqlParameters.setLocalParams(properties);
        sqlParameters.varPool = new ArrayList<>();
        sqlParameters.dealOutParam(sqlResult1);
        Assertions.assertNotNull(sqlParameters.getVarPool().get(0));

        property.setType(DataType.LIST);
        properties.clear();
        properties.add(property);
        sqlParameters.setLocalParams(properties);
        sqlParameters.dealOutParam(sqlResult);
        Assertions.assertNotNull(sqlParameters.getVarPool().get(0));

        // resource files list should contain sqlResource when it is set
        sqlParameters.setSql(null);
        sqlParameters.setSqlResource("/sql/demo.sql");
        Assertions.assertFalse(CollectionUtils.isEmpty(sqlParameters.getResourceFilesList()));
    }

    @Test
    public void testBigDecimalQueryResultProducesExactLongOutParameter() {
        Property property = new Property();
        property.setProp("SRC_CNT");
        property.setDirect(Direct.OUT);
        property.setType(DataType.LONG);
        SqlParameters parameters = new SqlParameters();
        parameters.setLocalParams(Collections.singletonList(property));

        ArrayNode result = JSONUtils.createArrayNode();
        ObjectNode row = JSONUtils.createObjectNode();
        // This integer cannot be represented exactly as a double and becomes
        // scientific notation after BigDecimal node normalization.
        row.set("SRC_CNT", JSONUtils.toJsonNode(new BigDecimal("90071992547409930")));
        result.add(row);
        parameters.dealOutParam(JSONUtils.toJsonString(result));

        String value = parameters.getVarPool().get(0).getValue();
        Assertions.assertEquals("90071992547409930", value);
        Assertions.assertEquals(90071992547409930L, Long.parseLong(value));
    }

    @ParameterizedTest
    @CsvSource({
            "11423000,11423000",
            "1.1423E+7,11423000",
            "11423000.0,11423000",
            "0,0",
            "-1.1423E+7,-11423000",
            "9.223372036854775807E+18,9223372036854775807",
            "-9.223372036854775808E+18,-9223372036854775808",
            "\"1.1423E7\",11423000"
    })
    public void testLongOutParameterNormalizesExactInteger(String jsonValue, String expected) {
        Property property = new Property();
        property.setProp("SRC_CNT");
        property.setDirect(Direct.OUT);
        property.setType(DataType.LONG);
        SqlParameters parameters = new SqlParameters();
        parameters.setLocalParams(Collections.singletonList(property));

        parameters.dealOutParam("[{\"SRC_CNT\":" + jsonValue + "}]");

        Assertions.assertEquals(expected, parameters.getVarPool().get(0).getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.5", "9223372036854775808", "-9223372036854775809", "\"invalid\"", "null"})
    public void testLongOutParameterRejectsInvalidValue(String jsonValue) {
        Property property = new Property();
        property.setProp("SRC_CNT");
        property.setDirect(Direct.OUT);
        property.setType(DataType.LONG);
        SqlParameters parameters = new SqlParameters();
        parameters.setLocalParams(Collections.singletonList(property));

        TaskException exception = Assertions.assertThrows(TaskException.class,
                () -> parameters.dealOutParam("[{\"SRC_CNT\":" + jsonValue + "}]"));

        Assertions.assertTrue(exception.getMessage().contains("SRC_CNT"));
        Assertions.assertTrue(exception.getMessage().contains("LONG"));
        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(parameters.getVarPool().isEmpty());
    }

    @Test
    public void testDealOutParamValueFormatShouldNotDependOnRowCount() {
        List<Property> listProperties = new ArrayList<>();
        listProperties.add(outProperty("list_user", DataType.LIST));
        List<Property> varcharProperties = new ArrayList<>();
        varcharProperties.add(outProperty("varchar_user", DataType.VARCHAR));

        String multiLineResult = "[{\"list_user\":\"user2\",\"varchar_user\":\"user2\"},"
                + "{\"list_user\":\"user3\",\"varchar_user\":\"user3\"}]";
        String singleLineResult = "[{\"list_user\":\"user2\",\"varchar_user\":\"user2\"}]";

        // LIST out parameter keeps the json array format in both cases
        Assertions.assertEquals("[\"user2\",\"user3\"]", outParamValue(listProperties, 0, multiLineResult));
        Assertions.assertEquals("[\"user2\"]", outParamValue(listProperties, 0, singleLineResult));

        // non LIST out parameter keeps the single row value
        Assertions.assertEquals("user2", outParamValue(varcharProperties, 0, singleLineResult));

        // non LIST out parameter rejects multi-row results instead of silently picking a non-deterministic row
        TaskException exception = Assertions.assertThrows(TaskException.class,
                () -> outParamValue(varcharProperties, 0, multiLineResult));
        Assertions.assertTrue(exception.getMessage().contains("varchar_user"));
    }

    @Test
    public void testDealOutParamWithNullValue() {
        List<Property> properties = new ArrayList<>();
        properties.add(outProperty("users", DataType.LIST));

        Assertions.assertEquals("[\"user2\",\"null\"]",
                outParamValue(properties, 0, "[{\"users\":\"user2\"},{\"users\":null}]"));
    }

    @Test
    public void testNonListOutParameterRejectsMultiRowResult() {
        List<Property> properties = new ArrayList<>();
        properties.add(outProperty("SRC_CNT", DataType.LONG));

        TaskException exception = Assertions.assertThrows(TaskException.class,
                () -> outParamValue(properties, 0, "[{\"SRC_CNT\":1},{\"SRC_CNT\":2},{\"SRC_CNT\":3}]"));
        // the error tells the parameter name, type and row count
        Assertions.assertTrue(exception.getMessage().contains("SRC_CNT"));
        Assertions.assertTrue(exception.getMessage().contains("LONG"));
        Assertions.assertTrue(exception.getMessage().contains("3"));
    }

    private Property outProperty(String prop, DataType dataType) {
        Property property = new Property();
        property.setProp(prop);
        property.setDirect(Direct.OUT);
        property.setType(dataType);
        return property;
    }

    private String outParamValue(List<Property> properties, int index, String sqlResult) {
        SqlParameters sqlParameters = new SqlParameters();
        List<Property> localParams = new ArrayList<>();
        for (Property property : properties) {
            Property copy = new Property(property.getProp(), property.getDirect(), property.getType(), null);
            localParams.add(copy);
        }
        sqlParameters.setLocalParams(localParams);
        sqlParameters.varPool = new ArrayList<>();
        sqlParameters.dealOutParam(sqlResult);
        return sqlParameters.getVarPool().get(index).getValue();
    }

    @Test
    public void testCheckParameters_variants() {
        SqlParameters p = new SqlParameters();

        // datasource/type invalid
        p.setDatasource(0);
        p.setType("");
        p.setSql("select 1");
        p.setSqlResource(null);
        Assertions.assertFalse(p.checkParameters());

        // sql present -> true
        p.setDatasource(1);
        p.setType("MYSQL");
        p.setSql("select 1");
        p.setSqlResource(null);
        Assertions.assertTrue(p.checkParameters());

        // sql absent, sqlResource present -> true
        p.setSql(null);
        p.setSqlResource("/sql/demo.sql");
        Assertions.assertTrue(p.checkParameters());

        // both absent -> false
        p.setSql(null);
        p.setSqlResource(null);
        Assertions.assertFalse(p.checkParameters());
    }

    @Test
    public void testGenerateExtendedContext_setsConnectionParams() {
        SqlParameters p = new SqlParameters();
        p.setDatasource(1);

        DataSourceParameters dataSourceParameters = new DataSourceParameters();
        dataSourceParameters.setType(DbType.MYSQL);
        dataSourceParameters.setResourceType(ResourceType.DATASOURCE.name());
        dataSourceParameters.setConnectionParams("conn_params");

        ResourceParametersHelper helper = new ResourceParametersHelper();
        helper.put(ResourceType.DATASOURCE, 1, dataSourceParameters);

        SQLTaskExecutionContext ctx = p.generateExtendedContext(helper);
        Assertions.assertNotNull(ctx);
        Assertions.assertEquals("conn_params", ctx.getConnectionParams());
    }
}
