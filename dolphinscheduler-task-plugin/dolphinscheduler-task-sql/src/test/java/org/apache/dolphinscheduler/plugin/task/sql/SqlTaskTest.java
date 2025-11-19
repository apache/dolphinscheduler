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

package org.apache.dolphinscheduler.plugin.task.sql;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

class SqlTaskTest {

    private SqlTask sqlTask;

    @BeforeEach
    void setup() {
        DataSourceParameters parameters = new DataSourceParameters();
        parameters.setType(DbType.HIVE);
        parameters.setResourceType(ResourceType.DATASOURCE.name());

        ResourceParametersHelper resourceParametersHelper = new ResourceParametersHelper();
        resourceParametersHelper.put(ResourceType.DATASOURCE, 1, parameters);

        TaskExecutionContext ctx = new TaskExecutionContext();
        ctx.setResourceParametersHelper(resourceParametersHelper);
        ctx.setTaskParams("{\"type\":\"HIVE\",\"datasource\":1,\"sql\":\"select 1\"}");

        sqlTask = new SqlTask(ctx);
    }

    @Test
    void testReplacingSqlWithoutParams() {
        String querySql = "select 1";
        String expected = "select 1";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));
    }

    @Test
    void testReplacingSqlWithDollarSymbol() {
        String querySql = "select concat(amount, '$') as price from product";
        String expected = "select concat(amount, '$') as price from product";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));
    }

    @Test
    void testReplacingHiveLoadSql() {
        String hiveLoadSql = "load inpath '/tmp/test_table/dt=${dt}' into table test_table partition(dt=${dt})";
        String expected = "load inpath '/tmp/test_table/dt=?' into table test_table partition(dt=?)";
        Assertions.assertEquals(expected, hiveLoadSql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));

        Map<Integer, Property> sqlParamsMap = new HashMap<>();
        Map<Integer, Property> expectedSQLParamsMap = new HashMap<>();
        expectedSQLParamsMap.put(1, new Property("dt", Direct.IN, DataType.VARCHAR, "1970"));
        expectedSQLParamsMap.put(2, new Property("dt", Direct.IN, DataType.VARCHAR, "1970"));
        Map<String, Property> paramsMap = new HashMap<>();
        paramsMap.put("dt", new Property("dt", Direct.IN, DataType.VARCHAR, "1970"));
        sqlTask.setSqlParamsMap(hiveLoadSql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);
    }

    @Test
    void testReplacingSelectSql() {
        String querySql = "select id from student where dt='${dt}'";
        String expected = "select id from student where dt=?";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));

        Map<Integer, Property> sqlParamsMap = new HashMap<>();
        Map<Integer, Property> expectedSQLParamsMap = new HashMap<>();
        expectedSQLParamsMap.put(1, new Property("dt", Direct.IN, DataType.VARCHAR, "1970"));
        Map<String, Property> paramsMap = new HashMap<>();
        paramsMap.put("dt", new Property("dt", Direct.IN, DataType.VARCHAR, "1970"));
        sqlTask.setSqlParamsMap(querySql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);

        querySql = "select id from student where dt=\"${dt}\"";
        expected = "select id from student where dt=?";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));

        sqlParamsMap.clear();
        sqlTask.setSqlParamsMap(querySql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);

        querySql = "select id from student where dt=${dt}";
        expected = "select id from student where dt=?";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));

        sqlParamsMap.clear();
        sqlTask.setSqlParamsMap(querySql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);

        querySql = "select id from student where dt=${dt} and gender=1";
        expected = "select id from student where dt=? and gender=1";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));

        sqlParamsMap.clear();
        sqlTask.setSqlParamsMap(querySql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);
    }

    @Test
    void testReplacingSqlNonGreedy() {
        String querySql = "select id from student where year=${year} and month=${month} and gender=1";
        String expected = "select id from student where year=? and month=? and gender=1";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));

        Map<Integer, Property> sqlParamsMap = new HashMap<>();
        Map<Integer, Property> expectedSQLParamsMap = new HashMap<>();
        expectedSQLParamsMap.put(1, new Property("year", Direct.IN, DataType.VARCHAR, "1970"));
        expectedSQLParamsMap.put(2, new Property("month", Direct.IN, DataType.VARCHAR, "12"));
        Map<String, Property> paramsMap = new HashMap<>();
        paramsMap.put("year", new Property("year", Direct.IN, DataType.VARCHAR, "1970"));
        paramsMap.put("month", new Property("month", Direct.IN, DataType.VARCHAR, "12"));
        sqlTask.setSqlParamsMap(querySql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);
    }

    @Test
    void splitSql() {
    }

    @Test
    void testReplacingSqlHasQuestionMarkAndParams() {
        String querySql =
                "select id, concat('?', year) from student where year=${year} and month=${month} and gender in ('${gender}')";
        String expected =
                "select id, concat('?', year) from student where year=? and month=? and gender in (?,?)";

        Map<Integer, Property> sqlParamsMap = new HashMap<>();
        Map<Integer, Property> expectedSQLParamsMap = new HashMap<>();
        expectedSQLParamsMap.put(1, new Property("year", Direct.IN, DataType.VARCHAR, "1970"));
        expectedSQLParamsMap.put(2, new Property("month", Direct.IN, DataType.VARCHAR, "12"));
        expectedSQLParamsMap.put(3,
                new Property("gender", Direct.IN, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList(1, 2))));
        Map<String, Property> paramsMap = new HashMap<>();
        paramsMap.put("year", new Property("year", Direct.IN, DataType.VARCHAR, "1970"));
        paramsMap.put("month", new Property("month", Direct.IN, DataType.VARCHAR, "12"));
        paramsMap.put("gender",
                new Property("gender", Direct.IN, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList(1, 2))));
        sqlTask.setSqlParamsMap(querySql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);

        String formatSql = ParameterUtils.expandListParameter(sqlParamsMap, querySql);
        Assertions.assertEquals(4, sqlParamsMap.size());
        Assertions.assertEquals(expected, formatSql);
    }

    @Test
    void testVarPoolSetting() {
        SqlParameters sqlParameters = new SqlParameters();
        sqlParameters.setType("HIVE");
        sqlParameters.setDatasource(1);
        sqlParameters.setSql("select id, name from user where id = 1");

        Property outParam = new Property("id", Direct.OUT, DataType.VARCHAR, "");
        sqlParameters.setLocalParams(Lists.newArrayList(outParam));

        String sqlResult = "[{\"id\":\"1\",\"name\":\"test_user\"}]";

        sqlParameters.dealOutParam(sqlResult);

        Assertions.assertNotNull(sqlParameters.getVarPool());
        Assertions.assertEquals(1, sqlParameters.getVarPool().size());

        Property varPoolParam = sqlParameters.getVarPool().get(0);
        Assertions.assertEquals("id", varPoolParam.getProp());
        Assertions.assertEquals("1", varPoolParam.getValue());
        Assertions.assertEquals(Direct.OUT, varPoolParam.getDirect());
    }

    @Test
    void testGenerateEmptyRow_WithNonNullResultSet_ReturnsEmptyValuesForAllColumns() throws Exception {
        // Arrange
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);

        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnLabel(1)).thenReturn("id");
        when(mockMetaData.getColumnLabel(2)).thenReturn("name");

        Method method = SqlTask.class.getDeclaredMethod("generateEmptyRow", ResultSet.class);
        method.setAccessible(true);

        // Act
        ArrayNode result = (ArrayNode) method.invoke(sqlTask, mockResultSet);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        ObjectNode row = (ObjectNode) result.get(0);
        Assertions.assertEquals("", row.get("id").asText());
        Assertions.assertEquals("", row.get("name").asText());
    }

    @Test
    void testGenerateEmptyRow_WithNullResultSet_ReturnsErrorObject() throws Exception {
        // Arrange
        Method method = SqlTask.class.getDeclaredMethod("generateEmptyRow", ResultSet.class);
        method.setAccessible(true);

        // Act
        ArrayNode result = (ArrayNode) method.invoke(sqlTask, (ResultSet) null);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        ObjectNode row = (ObjectNode) result.get(0);
        Assertions.assertTrue(row.has("error"));
        Assertions.assertEquals("resultSet is null", row.get("error").asText());
    }

    @Test
    void testResultProcess_NullResultSet_ReturnsEmptyResult() throws Exception {
        Method resultProcessMethod = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
        resultProcessMethod.setAccessible(true);

        // Mock a null ResultSet
        String result = (String) resultProcessMethod.invoke(sqlTask, (ResultSet) null);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.equalsIgnoreCase("[]"));
    }

    @Test
    void testResultProcess_EmptyResultSet_ReturnsEmptyResult() throws Exception {
        // Mock a non-null ResultSet that contains no data rows
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);

        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnLabel(1)).thenReturn("id");
        when(mockMetaData.getColumnLabel(2)).thenReturn("name");
        when(mockResultSet.next()).thenReturn(false); // no rows available

        Method resultProcessMethod = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
        resultProcessMethod.setAccessible(true);

        String result = (String) resultProcessMethod.invoke(sqlTask, mockResultSet);

        Assertions.assertNotNull(result);
        // Verify the result contains empty string values for all columns and is a valid JSON array
        Assertions.assertTrue(result.contains("\"id\":\"\""));
        Assertions.assertTrue(result.contains("\"name\":\"\""));
        Assertions.assertTrue(result.startsWith("[{"));
        Assertions.assertTrue(result.endsWith("}]"));
    }

    @Test
    void testResultProcess_ColumnLabelIsNull_UsesGenericName() throws Exception {
        // Mock ResultSet with a null column label
        ResultSet mockRs = mock(ResultSet.class);
        ResultSetMetaData mockMd = mock(ResultSetMetaData.class);

        when(mockRs.getMetaData()).thenReturn(mockMd);
        when(mockMd.getColumnCount()).thenReturn(1);
        when(mockMd.getColumnLabel(1)).thenReturn(null); // null label
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getObject(1)).thenReturn("value1");

        Method method = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
        method.setAccessible(true);

        String result = (String) method.invoke(sqlTask, mockRs);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("\"col_1\":\"value1\""));
    }

    @Test
    void testResultProcess_ColumnLabelIsEmpty_UsesGenericName() throws Exception {
        // Mock ResultSet with an empty column label
        ResultSet mockRs = mock(ResultSet.class);
        ResultSetMetaData mockMd = mock(ResultSetMetaData.class);

        when(mockRs.getMetaData()).thenReturn(mockMd);
        when(mockMd.getColumnCount()).thenReturn(1);
        when(mockMd.getColumnLabel(1)).thenReturn(""); // empty label
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getObject(1)).thenReturn("value1");

        Method method = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
        method.setAccessible(true);

        String result = (String) method.invoke(sqlTask, mockRs);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("\"col_1\":\"value1\""));
    }

    @Test
    void testResultProcess_SingleRowWithDuplicateColumns_GeneratesUniqueKeys() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);

        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(3);
        when(mockMetaData.getColumnLabel(1)).thenReturn("name");
        when(mockMetaData.getColumnLabel(2)).thenReturn("name"); // duplicate
        when(mockMetaData.getColumnLabel(3)).thenReturn("age");

        when(mockResultSet.next())
                .thenReturn(true) // first row
                .thenReturn(false); // no more rows

        when(mockResultSet.getObject(1)).thenReturn("Alice");
        when(mockResultSet.getObject(2)).thenReturn("Bob");
        when(mockResultSet.getObject(3)).thenReturn(30);

        Method resultProcessMethod = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
        resultProcessMethod.setAccessible(true);

        String result = (String) resultProcessMethod.invoke(sqlTask, mockResultSet);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("\"name\":\"Alice\""));
        Assertions.assertTrue(result.contains("\"name_2\":\"Bob\"")); // deduplicated
        Assertions.assertTrue(result.contains("\"age\":30"));
        Assertions.assertTrue(result.startsWith("[{"));
        Assertions.assertTrue(result.endsWith("}]"));
    }

    @Test
    void testResultProcess_DuplicateColumnLabels_AreDeduplicated() throws Exception {
        // Mock ResultSet with three columns all named "id"
        ResultSet mockRs = mock(ResultSet.class);
        ResultSetMetaData mockMd = mock(ResultSetMetaData.class);

        when(mockRs.getMetaData()).thenReturn(mockMd);
        when(mockMd.getColumnCount()).thenReturn(3);
        when(mockMd.getColumnLabel(1)).thenReturn("id");
        when(mockMd.getColumnLabel(2)).thenReturn("id"); // duplicate
        when(mockMd.getColumnLabel(3)).thenReturn("id"); // duplicate again

        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getObject(1)).thenReturn(101);
        when(mockRs.getObject(2)).thenReturn(102);
        when(mockRs.getObject(3)).thenReturn(103);

        Method method = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
        method.setAccessible(true);

        String result = (String) method.invoke(sqlTask, mockRs);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("\"id\":101"));
        Assertions.assertTrue(result.contains("\"id_2\":102"));
        Assertions.assertTrue(result.contains("\"id_3\":103"));
    }

    @Test
    void testResultProcess_ColumnNames_IdIdId2Id2Id3_HandlesSuffixCollisionCorrectly() throws Exception {
        // Arrange: ResultSet with column labels: id, id, id_2, id_2, id_3
        ResultSet mockRs = mock(ResultSet.class);
        ResultSetMetaData mockMd = mock(ResultSetMetaData.class);

        when(mockRs.getMetaData()).thenReturn(mockMd);
        when(mockMd.getColumnCount()).thenReturn(5);
        when(mockMd.getColumnLabel(1)).thenReturn("id");
        when(mockMd.getColumnLabel(2)).thenReturn("id"); // duplicate
        when(mockMd.getColumnLabel(3)).thenReturn("id_2"); // conflicts with auto-generated id_2
        when(mockMd.getColumnLabel(4)).thenReturn("id_2"); // another duplicate
        when(mockMd.getColumnLabel(5)).thenReturn("id_3");

        // One data row with distinct values for clarity
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getObject(1)).thenReturn(1);
        when(mockRs.getObject(2)).thenReturn(2);
        when(mockRs.getObject(3)).thenReturn(3);
        when(mockRs.getObject(4)).thenReturn(4);
        when(mockRs.getObject(5)).thenReturn(5);

        Method method = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
        method.setAccessible(true);

        // Act
        String result = (String) method.invoke(sqlTask, mockRs);

        // Assert: Verify each final key and its value
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("\"id\":1"));
        Assertions.assertTrue(result.contains("\"id_2\":2")); // auto-generated from 2nd "id"
        Assertions.assertTrue(result.contains("\"id_2_2\":3")); // 3rd col: "id_2" conflicted → _2
        Assertions.assertTrue(result.contains("\"id_2_3\":4")); // 4th col: next suffix
        Assertions.assertTrue(result.contains("\"id_3\":5")); // no conflict

        // Ensure structure is valid JSON object inside array
        Assertions.assertTrue(result.startsWith("[{"));
        Assertions.assertTrue(result.endsWith("}]"));
    }
}
