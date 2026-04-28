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

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
    void testSqlLoadedFromResourceFileWhenSqlIsEmpty(@TempDir Path tempDir) throws Exception {
        Path sqlFile = tempDir.resolve("test.sql");
        String sqlContent = "SELECT 1";
        Files.write(sqlFile, sqlContent.getBytes(StandardCharsets.UTF_8));

        SqlParameters sqlParameters = new SqlParameters();
        sqlParameters.setType("MYSQL");
        sqlParameters.setDatasource(1);
        sqlParameters.setSql(null);
        sqlParameters.setSqlResource("/sql/test.sql");

        DataSourceParameters dataSourceParameters = new DataSourceParameters();
        dataSourceParameters.setType(DbType.MYSQL);
        dataSourceParameters.setResourceType(ResourceType.DATASOURCE.name());

        ResourceParametersHelper resourceParametersHelper = new ResourceParametersHelper();
        resourceParametersHelper.put(ResourceType.DATASOURCE, 1, dataSourceParameters);

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(sqlParameters));
        taskExecutionContext.setScheduleTime(System.currentTimeMillis());
        taskExecutionContext.setResourceParametersHelper(resourceParametersHelper);

        ResourceContext resourceContext = new ResourceContext();
        resourceContext.addResourceItem(ResourceContext.ResourceItem.builder()
                .resourceAbsolutePathInStorage(sqlParameters.getSqlResource())
                .resourceAbsolutePathInLocal(sqlFile.toString())
                .build());
        taskExecutionContext.setResourceContext(resourceContext);

        SqlTask task = new SqlTask(taskExecutionContext);

        Method ensureSqlContent = SqlTask.class.getDeclaredMethod("ensureSqlContent");
        ensureSqlContent.setAccessible(true);
        ensureSqlContent.invoke(task);

        SqlParameters loadedParameters = (SqlParameters) task.getParameters();
        Assertions.assertEquals(sqlContent, loadedParameters.getSql());
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
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);

        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnLabel(1)).thenReturn("id");
        when(mockMetaData.getColumnLabel(2)).thenReturn("name");

        Method method = SqlTask.class.getDeclaredMethod("generateEmptyRow", ResultSet.class);
        method.setAccessible(true);

        ArrayNode result = (ArrayNode) method.invoke(sqlTask, mockResultSet);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        ObjectNode row = (ObjectNode) result.get(0);
        Assertions.assertEquals("", row.get("id").asText());
        Assertions.assertEquals("", row.get("name").asText());
    }

    @Test
    void testGenerateEmptyRow_WithNullResultSet_ReturnsErrorObject() throws Exception {
        Method method = SqlTask.class.getDeclaredMethod("generateEmptyRow", ResultSet.class);
        method.setAccessible(true);

        ArrayNode result = (ArrayNode) method.invoke(sqlTask, (ResultSet) null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        ObjectNode row = (ObjectNode) result.get(0);
        Assertions.assertTrue(row.has("error"));
        Assertions.assertEquals("resultSet is null", row.get("error").asText());
    }

    @Test
    void testGenerateEmptyRow_WithDuplicateColumns_DeduplicatesLabels() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);

        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(3);
        when(mockMetaData.getColumnLabel(1)).thenReturn("id");
        when(mockMetaData.getColumnLabel(2)).thenReturn("id");
        when(mockMetaData.getColumnLabel(3)).thenReturn("name");

        Method method = SqlTask.class.getDeclaredMethod("generateEmptyRow", ResultSet.class);
        method.setAccessible(true);

        ArrayNode result = (ArrayNode) method.invoke(sqlTask, mockResultSet);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());

        ObjectNode row = (ObjectNode) result.get(0);
        Assertions.assertTrue(row.has("id"));
        Assertions.assertTrue(row.has("name"));
    }

    @Test
    void testResultProcess_NullResultSet_ReturnsEmptyResult() throws Exception {
        Method resultProcessMethod = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
        resultProcessMethod.setAccessible(true);

        String result = (String) resultProcessMethod.invoke(sqlTask, (ResultSet) null);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.equalsIgnoreCase("[{\"error\":\"resultSet is null\"}]"));
    }

    @Test
    void testResultProcess_EmptyResultSet_ReturnsEmptyResult() throws Exception {
        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);

        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnLabel(1)).thenReturn("id");
        when(mockMetaData.getColumnLabel(2)).thenReturn("name");
        when(mockResultSet.next()).thenReturn(false);

        Method resultProcessMethod = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
        resultProcessMethod.setAccessible(true);

        String result = (String) resultProcessMethod.invoke(sqlTask, mockResultSet);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("\"id\":\"\""));
        Assertions.assertTrue(result.contains("\"name\":\"\""));
        Assertions.assertTrue(result.startsWith("[{"));
        Assertions.assertTrue(result.endsWith("}]"));
    }

    @Test
    void testResultProcess_DuplicateColumnLabels_ThrowsTaskException() throws Exception {
        ResultSet mockRs = mock(ResultSet.class);
        ResultSetMetaData mockMd = mock(ResultSetMetaData.class);

        when(mockRs.getMetaData()).thenReturn(mockMd);
        when(mockMd.getColumnCount()).thenReturn(2);
        when(mockMd.getColumnLabel(1)).thenReturn("id");
        when(mockMd.getColumnLabel(2)).thenReturn("id");

        Method method = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
        method.setAccessible(true);

        InvocationTargetException thrown = Assertions.assertThrows(
                InvocationTargetException.class,
                () -> method.invoke(sqlTask, mockRs));

        Throwable cause = thrown.getCause();
        Assertions.assertNotNull(cause);
        Assertions.assertInstanceOf(TaskException.class, cause,
                "Cause should be TaskException, but was: " + cause.getClass());
        Assertions.assertTrue(
                cause.getMessage().contains("duplicate column name"),
                "TaskException message should mention duplicate column name");
    }

    @Test
    void testGetSqlAndSqlParamsMap_nullPrepareParamsMap_replacesScheduleTimeAndTitlePlaceholder() throws Exception {
        long scheduleTimeMillis = 1700000000000L;
        String expectedDate = DateUtils.format(new java.util.Date(scheduleTimeMillis), "yyyyMMdd");

        TaskExecutionContext ctx = new TaskExecutionContext();
        ctx.setTaskParams("{\"type\":\"HIVE\",\"datasource\":1,\"sql\":\"select 1\",\"title\":\"title-$[yyyyMMdd]\"}");
        ctx.setScheduleTime(scheduleTimeMillis);
        ctx.setResourceParametersHelper(getResourceParametersHelperWithDatasourceType(DbType.HIVE));

        // Ensure prepareParamsMap == null
        ctx.setPrepareParamsMap(null);

        SqlTask task = new SqlTask(ctx);

        Method method = SqlTask.class.getDeclaredMethod("getSqlAndSqlParamsMap", String.class);
        method.setAccessible(true);

        String inputSql = "select '" + "$[yyyyMMdd]" + "'";
        SqlBinds binds = (SqlBinds) method.invoke(task, inputSql);

        Assertions.assertEquals("select '" + expectedDate + "'", binds.getSql());
        SqlParameters loadedParameters = (SqlParameters) task.getParameters();
        Assertions.assertTrue(loadedParameters.getTitle().matches("title-\\d{8}"));
    }

    @Test
    void testGetSqlAndSqlParamsMap_withPrepareParamsMap_coversPrintReplacedSql() throws Exception {
        Map<String, Property> prepareParamsMap = new HashMap<>();
        prepareParamsMap.put("dt", new Property("dt", Direct.IN, DataType.VARCHAR, "1970"));

        TaskExecutionContext ctx = new TaskExecutionContext();
        ctx.setTaskParams("{\"type\":\"HIVE\",\"datasource\":1,\"sql\":\"select 1\"}");
        ctx.setScheduleTime(System.currentTimeMillis());
        ctx.setTaskInstanceId(1);
        ctx.setResourceParametersHelper(getResourceParametersHelperWithDatasourceType(DbType.HIVE));
        ctx.setPrepareParamsMap(prepareParamsMap);

        SqlTask task = new SqlTask(ctx);

        Method method = SqlTask.class.getDeclaredMethod("getSqlAndSqlParamsMap", String.class);
        method.setAccessible(true);

        String inputSql = "select * from student where dt=${dt}";
        SqlBinds binds = (SqlBinds) method.invoke(task, inputSql);

        Assertions.assertEquals("select * from student where dt=?", binds.getSql());
        Assertions.assertNotNull(binds.getParamsMap());
        Assertions.assertEquals("1970", binds.getParamsMap().get(1).getValue());
    }

    @Test
    void testEnsureSqlContent_whenSqlAlreadyPresent_doesNotReadResource() throws Exception {
        SqlTask task = this.sqlTask;

        Method ensureSqlContent = SqlTask.class.getDeclaredMethod("ensureSqlContent");
        ensureSqlContent.setAccessible(true);

        // Should early return because sqlParameters.getSql() is not empty.
        ensureSqlContent.invoke(task);
    }

    @Test
    void testEnsureSqlContent_whenResourceMissing_throwsTaskException(@TempDir Path tempDir) throws Exception {
        SqlParameters sqlParameters = new SqlParameters();
        sqlParameters.setType("HIVE");
        sqlParameters.setDatasource(1);
        sqlParameters.setSql(null);
        sqlParameters.setSqlResource("/sql/missing.sql");

        DataSourceParameters dataSourceParameters = new DataSourceParameters();
        dataSourceParameters.setType(DbType.HIVE);
        dataSourceParameters.setResourceType(ResourceType.DATASOURCE.name());

        ResourceParametersHelper resourceParametersHelper = new ResourceParametersHelper();
        resourceParametersHelper.put(ResourceType.DATASOURCE, 1, dataSourceParameters);

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(sqlParameters));
        taskExecutionContext.setScheduleTime(System.currentTimeMillis());
        taskExecutionContext.setResourceParametersHelper(resourceParametersHelper);

        ResourceContext resourceContext = new ResourceContext();
        // Point to a file that does not exist to trigger IOException in ensureSqlContent.
        Path missingLocalPath = tempDir.resolve("missing.sql");
        resourceContext.addResourceItem(ResourceContext.ResourceItem.builder()
                .resourceAbsolutePathInStorage(sqlParameters.getSqlResource())
                .resourceAbsolutePathInLocal(missingLocalPath.toString())
                .build());
        taskExecutionContext.setResourceContext(resourceContext);

        SqlTask task = new SqlTask(taskExecutionContext);

        Method ensureSqlContent = SqlTask.class.getDeclaredMethod("ensureSqlContent");
        ensureSqlContent.setAccessible(true);

        InvocationTargetException thrown = Assertions.assertThrows(
                InvocationTargetException.class,
                () -> ensureSqlContent.invoke(task));
        Assertions.assertInstanceOf(TaskException.class, thrown.getCause());
    }

    private ResourceParametersHelper getResourceParametersHelperWithDatasourceType(DbType dbType) {
        DataSourceParameters parameters = new DataSourceParameters();
        parameters.setType(dbType);
        parameters.setResourceType(ResourceType.DATASOURCE.name());

        ResourceParametersHelper resourceParametersHelper = new ResourceParametersHelper();
        resourceParametersHelper.put(ResourceType.DATASOURCE, 1, parameters);
        return resourceParametersHelper;
    }

}
