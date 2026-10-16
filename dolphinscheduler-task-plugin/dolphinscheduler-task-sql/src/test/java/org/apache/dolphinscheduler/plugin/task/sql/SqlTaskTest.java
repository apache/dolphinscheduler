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

import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskAlertInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.fasterxml.jackson.databind.JsonNode;
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
    void testResultProcessLogsRowsWhileFetchingAndOnlyUpToDisplayRows() throws Exception {
        String taskParams = "{\"type\":\"HIVE\",\"datasource\":1,\"sql\":\"select 1\",\"displayRows\":2}";
        TaskExecutionContext ctx = new TaskExecutionContext();
        ctx.setTaskParams(taskParams);
        ctx.setTaskName("display_rows_task");
        ctx.setResourceParametersHelper(getResourceParametersHelperWithDatasourceType(DbType.HIVE));

        SqlTask task = new SqlTask(ctx);

        ListAppender<ILoggingEvent> appender = attachLogAppender();
        try {
            ResultSet mockResultSet = mock(ResultSet.class);
            ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);
            when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
            when(mockMetaData.getColumnCount()).thenReturn(1);
            when(mockMetaData.getColumnLabel(1)).thenReturn("id");
            AtomicInteger fetchedRows = new AtomicInteger();
            when(mockResultSet.next()).thenAnswer(inv -> fetchedRows.incrementAndGet() <= 5);
            when(mockResultSet.getObject(1)).thenAnswer(inv -> {
                int currentRow = fetchedRows.get();
                if (currentRow > 1) {
                    long loggedRows = appender.list.stream()
                            .map(ILoggingEvent::getFormattedMessage)
                            .filter(message -> message.startsWith("row "))
                            .count();
                    Assertions.assertEquals(Math.min(currentRow - 1, 2), loggedRows, "before fetching row "
                            + currentRow + ", the first " + Math.min(currentRow - 1, 2)
                            + " rows should already be logged");
                }
                return "v" + currentRow;
            });

            Method resultProcessMethod = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
            resultProcessMethod.setAccessible(true);
            String result = (String) resultProcessMethod.invoke(task, mockResultSet);

            List<String> messages = appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .collect(Collectors.toList());
            Assertions.assertTrue(messages.contains("display the first 2 rows of sql result as follows:"));
            Assertions.assertTrue(messages.contains("row 1 : {\"id\":\"v1\"}"));
            Assertions.assertTrue(messages.contains("row 2 : {\"id\":\"v2\"}"));
            Assertions.assertEquals(2, messages.stream().filter(message -> message.startsWith("row ")).count(),
                    "only the first displayRows=2 rows should be logged");
            Assertions.assertTrue(messages.contains("displayed 2 of 5 rows"));
            // the full result set is kept regardless of the display limit
            Assertions.assertTrue(result.contains("v3"));
            Assertions.assertTrue(result.contains("v5"));
        } finally {
            detachLogAppender(appender);
        }
    }

    @Test
    void testResultProcessLogsAllRowsWhenResultSmallerThanDefaultDisplayRows() throws Exception {
        String taskParams = "{\"type\":\"HIVE\",\"datasource\":1,\"sql\":\"select 1\"}";
        TaskExecutionContext ctx = new TaskExecutionContext();
        ctx.setTaskParams(taskParams);
        ctx.setTaskName("default_display_rows_task");
        ctx.setResourceParametersHelper(getResourceParametersHelperWithDatasourceType(DbType.HIVE));

        SqlTask task = new SqlTask(ctx);

        ListAppender<ILoggingEvent> appender = attachLogAppender();
        try {
            ResultSet mockResultSet = mock(ResultSet.class);
            ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);
            when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
            when(mockMetaData.getColumnCount()).thenReturn(1);
            when(mockMetaData.getColumnLabel(1)).thenReturn("id");
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getObject(1)).thenReturn("v");

            Method resultProcessMethod = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
            resultProcessMethod.setAccessible(true);
            resultProcessMethod.invoke(task, mockResultSet);

            List<String> messages = appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .collect(Collectors.toList());
            Assertions.assertTrue(messages.contains("display the first 10 rows of sql result as follows:"));
            Assertions.assertTrue(messages.contains("row 1 : {\"id\":\"v\"}"));
            Assertions.assertTrue(messages.contains("row 2 : {\"id\":\"v\"}"));
            Assertions.assertEquals(2, messages.stream().filter(message -> message.startsWith("row ")).count());
            Assertions.assertTrue(messages.contains("displayed 2 of 2 rows"));
        } finally {
            detachLogAppender(appender);
        }
    }

    @Test
    void testResultProcessLogsNoRowForEmptyResultSet() throws Exception {
        String taskParams = "{\"type\":\"HIVE\",\"datasource\":1,\"sql\":\"select 1\",\"displayRows\":3}";
        TaskExecutionContext ctx = new TaskExecutionContext();
        ctx.setTaskParams(taskParams);
        ctx.setTaskName("empty_result_display_rows_task");
        ctx.setResourceParametersHelper(getResourceParametersHelperWithDatasourceType(DbType.HIVE));

        SqlTask task = new SqlTask(ctx);

        ListAppender<ILoggingEvent> appender = attachLogAppender();
        try {
            ResultSet mockResultSet = mock(ResultSet.class);
            ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);
            when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
            when(mockMetaData.getColumnCount()).thenReturn(1);
            when(mockMetaData.getColumnLabel(1)).thenReturn("id");
            when(mockResultSet.next()).thenReturn(false);

            Method resultProcessMethod = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
            resultProcessMethod.setAccessible(true);
            resultProcessMethod.invoke(task, mockResultSet);

            List<String> messages = appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .collect(Collectors.toList());
            Assertions.assertTrue(messages.contains("display the first 3 rows of sql result as follows:"));
            Assertions.assertEquals(0, messages.stream().filter(message -> message.startsWith("row ")).count());
            Assertions.assertTrue(messages.contains("displayed 0 of 0 rows"));
        } finally {
            detachLogAppender(appender);
        }
    }

    private ListAppender<ILoggingEvent> attachLogAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(SqlTask.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private void detachLogAppender(ListAppender<ILoggingEvent> appender) {
        Logger logger = (Logger) LoggerFactory.getLogger(SqlTask.class);
        logger.detachAppender(appender);
        appender.stop();
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

    private TaskExecutionContext createSqlTaskWithAlert(ArrayNode resultArray, int displayRows, String title,
                                                        int groupId) {
        SqlParameters sqlParameters = new SqlParameters();
        sqlParameters.setType("HIVE");
        sqlParameters.setDatasource(1);
        sqlParameters.setSql("select 1");
        sqlParameters.setSendEmail(true);
        sqlParameters.setDisplayRows(displayRows);
        sqlParameters.setGroupId(groupId);
        if (title != null) {
            sqlParameters.setTitle(title);
        }

        TaskExecutionContext ctx = new TaskExecutionContext();
        ctx.setTaskParams(JSONUtils.toJsonString(sqlParameters));
        ctx.setTaskName("test_sql_task");
        ctx.setResourceParametersHelper(getResourceParametersHelperWithDatasourceType(DbType.HIVE));

        SqlTask task = new SqlTask(ctx);
        try {
            Method method = SqlTask.class.getDeclaredMethod("prepareTaskResultAlert", ArrayNode.class);
            method.setAccessible(true);
            method.invoke(task, resultArray);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ctx;
    }

    @Test
    void testPrepareTaskResultAlertSetsNeedAlertAndTaskAlertInfo() {
        ArrayNode resultArray = JSONUtils.createArrayNode();
        resultArray.add(JSONUtils.parseObject("{\"id\":\"1\",\"name\":\"alice\"}"));
        resultArray.add(JSONUtils.parseObject("{\"id\":\"2\",\"name\":\"bob\"}"));

        TaskExecutionContext ctx = createSqlTaskWithAlert(resultArray, 10, "My Alert Title", 5);

        Assertions.assertTrue(ctx.isNeedAlert());

        TaskAlertInfo alertInfo = ctx.getTaskAlertInfo();
        Assertions.assertNotNull(alertInfo);
        Assertions.assertEquals(5, alertInfo.getAlertGroupId());
        Assertions.assertEquals("My Alert Title", alertInfo.getTitle());
        Assertions.assertEquals(AlertType.TASK_RESULT, alertInfo.getAlertType());
        Assertions.assertNotNull(alertInfo.getContent());

        ArrayNode contentArray = JSONUtils.parseArray(alertInfo.getContent());
        Assertions.assertEquals(2, contentArray.size());
    }

    @Test
    void testPrepareTaskResultAlertKeepsFullContentRegardlessOfDisplayRows() throws Exception {
        int totalRows = readStaticIntConstant("DEFAULT_ALERT_MAX_ROWS") - 1;
        ArrayNode resultArray = JSONUtils.createArrayNode();
        for (int i = 0; i < totalRows; i++) {
            resultArray.add(JSONUtils.parseObject("{\"id\":\"" + i + "\"}"));
        }

        // displayRows is only a log-display setting, the alert content must keep all rows
        TaskExecutionContext ctx = createSqlTaskWithAlert(resultArray, 3, null, 1);

        TaskAlertInfo alertInfo = ctx.getTaskAlertInfo();
        Assertions.assertNotNull(alertInfo);

        ArrayNode contentArray = JSONUtils.parseArray(alertInfo.getContent());
        Assertions.assertEquals(totalRows, contentArray.size());
        Assertions.assertEquals("0", contentArray.get(0).get("id").asText());
        Assertions.assertEquals(String.valueOf(totalRows - 1), contentArray.get(totalRows - 1).get("id").asText());
    }

    @Test
    void testPrepareTaskResultAlertKeepsFullContentWhenDisplayRowsUnset() throws Exception {
        int totalRows = readStaticIntConstant("DEFAULT_ALERT_MAX_ROWS") - 1;
        ArrayNode resultArray = JSONUtils.createArrayNode();
        for (int i = 0; i < totalRows; i++) {
            resultArray.add(JSONUtils.parseObject("{\"id\":\"" + i + "\"}"));
        }

        // displayRows = 0 means unset, the alert content still keeps all rows
        TaskExecutionContext ctx = createSqlTaskWithAlert(resultArray, 0, "default rows test", 2);

        TaskAlertInfo alertInfo = ctx.getTaskAlertInfo();
        Assertions.assertNotNull(alertInfo);

        ArrayNode contentArray = JSONUtils.parseArray(alertInfo.getContent());
        Assertions.assertEquals(totalRows, contentArray.size());
    }

    @Test
    void testPrepareTaskResultAlertUsesCustomTitle() {
        ArrayNode resultArray = JSONUtils.createArrayNode();
        resultArray.add(JSONUtils.parseObject("{\"id\":\"1\"}"));

        TaskExecutionContext ctx = createSqlTaskWithAlert(resultArray, 10, "Custom Title", 1);

        TaskAlertInfo alertInfo = ctx.getTaskAlertInfo();
        Assertions.assertNotNull(alertInfo);
        Assertions.assertEquals("Custom Title", alertInfo.getTitle());
    }

    @Test
    void testPrepareTaskResultAlertUsesDefaultTitleWhenEmpty() {
        ArrayNode resultArray = JSONUtils.createArrayNode();
        resultArray.add(JSONUtils.parseObject("{\"id\":\"1\"}"));

        TaskExecutionContext ctx = createSqlTaskWithAlert(resultArray, 10, null, 1);

        TaskAlertInfo alertInfo = ctx.getTaskAlertInfo();
        Assertions.assertNotNull(alertInfo);
        Assertions.assertEquals("test_sql_task query result sets", alertInfo.getTitle());
    }

    @Test
    void testPrepareTaskResultAlertTruncatesRowsBeyondMax() throws Exception {
        int maxRows = readStaticIntConstant("DEFAULT_ALERT_MAX_ROWS");
        int totalRows = maxRows + 10;
        ArrayNode resultArray = JSONUtils.createArrayNode();
        for (int i = 0; i < totalRows; i++) {
            resultArray.add(JSONUtils.parseObject("{\"id\":\"" + i + "\"}"));
        }

        TaskExecutionContext ctx = createSqlTaskWithAlert(resultArray, 10, "truncate rows test", 1);

        TaskAlertInfo alertInfo = ctx.getTaskAlertInfo();
        ArrayNode contentArray = JSONUtils.parseArray(alertInfo.getContent());
        Assertions.assertEquals(maxRows + 1, contentArray.size());
        Assertions.assertEquals("0", contentArray.get(0).get("id").asText());
        JsonNode last = contentArray.get(contentArray.size() - 1);
        Assertions.assertTrue(last.has("..."));
        Assertions.assertEquals((totalRows - maxRows) + " more rows omitted", last.get("...").asText());
    }

    @Test
    void testPrepareTaskResultAlertTruncatesLongFieldValue() throws Exception {
        int maxLen = readStaticIntConstant("DEFAULT_ALERT_MAX_ROW_VALUE_LENGTH");
        StringBuilder longValue = new StringBuilder();
        for (int i = 0; i < maxLen + 50; i++) {
            longValue.append("x");
        }
        ArrayNode resultArray = JSONUtils.createArrayNode();
        resultArray.add(JSONUtils.parseObject("{\"id\":\"1\",\"payload\":\"" + longValue + "\"}"));

        TaskExecutionContext ctx = createSqlTaskWithAlert(resultArray, 10, "truncate field test", 1);

        TaskAlertInfo alertInfo = ctx.getTaskAlertInfo();
        ArrayNode contentArray = JSONUtils.parseArray(alertInfo.getContent());
        Assertions.assertEquals(1, contentArray.size());
        String payload = contentArray.get(0).get("payload").asText();
        Assertions.assertEquals(maxLen + 3, payload.length());
        Assertions.assertTrue(payload.endsWith("..."));
    }

    @Test
    void testPrepareTaskResultAlertBoundsTotalBytesWithWideRows() throws Exception {
        int maxRows = readStaticIntConstant("DEFAULT_ALERT_MAX_ROWS");
        int maxLen = readStaticIntConstant("DEFAULT_ALERT_MAX_ROW_VALUE_LENGTH");
        int budget = readStaticIntConstant("DEFAULT_ALERT_MAX_CONTENT_BYTES");

        // Wide rows: enough columns that keeping all rows overflows the byte budget
        // even though every single value stays under the per-value character limit.
        int columns = budget / (maxRows * (maxLen - 1)) + 5;
        int totalRows = maxRows;
        ArrayNode resultArray = JSONUtils.createArrayNode();
        for (int i = 0; i < totalRows; i++) {
            ObjectNode row = JSONUtils.createObjectNode();
            for (int c = 0; c < columns; c++) {
                row.put("col_" + c, repeatChar('v', maxLen - 1));
            }
            resultArray.add(row);
        }

        TaskExecutionContext ctx = createSqlTaskWithAlert(resultArray, 10, "wide rows test", 1);

        TaskAlertInfo alertInfo = ctx.getTaskAlertInfo();
        int contentBytes = alertInfo.getContent().getBytes(StandardCharsets.UTF_8).length;
        Assertions.assertTrue(contentBytes <= budget,
                "serialized alert content must stay within the byte budget: " + contentBytes);

        ArrayNode contentArray = JSONUtils.parseArray(alertInfo.getContent());
        int keptRows = contentArray.size() - 1;
        Assertions.assertTrue(keptRows < totalRows, "rows must be dropped from the tail to fit the budget");
        Assertions.assertTrue(contentArray.get(0).has("col_0"));
        JsonNode last = contentArray.get(contentArray.size() - 1);
        Assertions.assertTrue(last.has("..."));
        Assertions.assertEquals((totalRows - keptRows) + " more rows omitted", last.get("...").asText());
    }

    @Test
    void testPrepareTaskResultAlertBoundsTotalBytesWithMultibyteText() throws Exception {
        int maxRows = readStaticIntConstant("DEFAULT_ALERT_MAX_ROWS");
        int maxLen = readStaticIntConstant("DEFAULT_ALERT_MAX_ROW_VALUE_LENGTH");
        int budget = readStaticIntConstant("DEFAULT_ALERT_MAX_CONTENT_BYTES");

        // Each character is 3 UTF-8 bytes, so rows within the character limits still
        // overflow the byte budget and rows must be dropped from the tail.
        char cjk = '\u4e2d';
        int columns = budget / (maxRows * 3 * maxLen) + 5;
        int totalRows = maxRows;
        ArrayNode resultArray = JSONUtils.createArrayNode();
        for (int i = 0; i < totalRows; i++) {
            ObjectNode row = JSONUtils.createObjectNode();
            for (int c = 0; c < columns; c++) {
                row.put("col_" + c, repeatChar(cjk, maxLen));
            }
            resultArray.add(row);
        }

        TaskExecutionContext ctx = createSqlTaskWithAlert(resultArray, 10, "multibyte test", 1);

        TaskAlertInfo alertInfo = ctx.getTaskAlertInfo();
        int contentBytes = alertInfo.getContent().getBytes(StandardCharsets.UTF_8).length;
        Assertions.assertTrue(contentBytes <= budget,
                "multibyte alert content must stay within the byte budget: " + contentBytes);

        ArrayNode contentArray = JSONUtils.parseArray(alertInfo.getContent());
        int keptRows = contentArray.size() - 1;
        Assertions.assertTrue(keptRows < totalRows, "rows must be dropped from the tail to fit the budget");
        // Kept values are exactly at the character limit, so they must stay complete.
        String firstValue = contentArray.get(0).get("col_0").asText();
        Assertions.assertEquals(repeatChar(cjk, maxLen), firstValue);
        JsonNode last = contentArray.get(contentArray.size() - 1);
        Assertions.assertTrue(last.has("..."));
        Assertions.assertEquals((totalRows - keptRows) + " more rows omitted", last.get("...").asText());
    }

    @Test
    void testPrepareTaskResultAlertReplacesRowExceedingByteBudgetAlone() throws Exception {
        int budget = readStaticIntConstant("DEFAULT_ALERT_MAX_CONTENT_BYTES");

        // Nested values are not truncated per-field, so a single row can exceed the
        // budget alone; it must be replaced by the omission marker.
        ObjectNode row = JSONUtils.createObjectNode();
        row.put("id", "1");
        ObjectNode payload = JSONUtils.createObjectNode();
        payload.put("blob", repeatChar('n', budget * 2));
        row.set("payload", payload);

        ArrayNode resultArray = JSONUtils.createArrayNode();
        resultArray.add(row);

        TaskExecutionContext ctx = createSqlTaskWithAlert(resultArray, 10, "huge nested value test", 1);

        TaskAlertInfo alertInfo = ctx.getTaskAlertInfo();
        int contentBytes = alertInfo.getContent().getBytes(StandardCharsets.UTF_8).length;
        Assertions.assertTrue(contentBytes <= budget);

        ArrayNode contentArray = JSONUtils.parseArray(alertInfo.getContent());
        Assertions.assertEquals(1, contentArray.size());
        JsonNode only = contentArray.get(0);
        Assertions.assertTrue(only.has("..."));
        Assertions.assertEquals("1 more rows omitted", only.get("...").asText());
    }

    private String repeatChar(char c, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    @Test
    void testPrepareTaskResultAlertRespectsConfiguredLimits() throws Exception {
        int configuredMaxRows = 3;
        int configuredMaxValueLength = 10;
        int configuredMaxBytes = 60000;
        int totalRows = 10;

        ArrayNode resultArray = JSONUtils.createArrayNode();
        for (int i = 0; i < totalRows; i++) {
            resultArray.add(JSONUtils.parseObject("{\"id\":\"" + i + "\"}"));
        }

        try (MockedStatic<PropertyUtils> propertyUtils = Mockito.mockStatic(PropertyUtils.class)) {
            propertyUtils.when(() -> PropertyUtils.getInt("sql.task.alert.max.rows", 30)).thenReturn(configuredMaxRows);
            propertyUtils.when(() -> PropertyUtils.getInt("sql.task.alert.max.row.value.length", 100))
                    .thenReturn(configuredMaxValueLength);
            propertyUtils.when(() -> PropertyUtils.getInt("sql.task.alert.max.content.bytes", 60000))
                    .thenReturn(configuredMaxBytes);

            TaskExecutionContext ctx = createSqlTaskWithAlert(resultArray, 10, "configured limits test", 1);

            TaskAlertInfo alertInfo = ctx.getTaskAlertInfo();
            ArrayNode contentArray = JSONUtils.parseArray(alertInfo.getContent());
            Assertions.assertEquals(configuredMaxRows + 1, contentArray.size());
            JsonNode last = contentArray.get(contentArray.size() - 1);
            Assertions.assertTrue(last.has("..."));
            Assertions.assertEquals((totalRows - configuredMaxRows) + " more rows omitted",
                    last.get("...").asText());
        }
    }

    private int readStaticIntConstant(String name) throws Exception {
        Field field = SqlTask.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(null);
    }

    @Test
    void testResultProcessSendEmailDisabledDoesNotSetNeedAlert() throws Exception {
        // Build a SqlTask with sendEmail = false
        String taskParams = "{\"type\":\"HIVE\",\"datasource\":1,\"sql\":\"select 1\",\"sendEmail\":false}";
        TaskExecutionContext ctx = new TaskExecutionContext();
        ctx.setTaskParams(taskParams);
        ctx.setTaskName("no_alert_task");
        ctx.setResourceParametersHelper(getResourceParametersHelperWithDatasourceType(DbType.HIVE));

        SqlTask task = new SqlTask(ctx);

        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnLabel(1)).thenReturn("id");
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getObject(1)).thenReturn("1");

        Method resultProcessMethod = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
        resultProcessMethod.setAccessible(true);
        resultProcessMethod.invoke(task, mockResultSet);

        Assertions.assertFalse(ctx.isNeedAlert());
        Assertions.assertNull(ctx.getTaskAlertInfo());
    }

    @Test
    void testResultProcessEmptyResultSetPreparesAlertWithEmptyRow() throws Exception {
        String taskParams = "{\"type\":\"HIVE\",\"datasource\":1,\"sql\":\"select 1\""
                + ",\"sendEmail\":true,\"displayRows\":10,\"groupId\":3,\"title\":\"empty result\"}";

        TaskExecutionContext ctx = new TaskExecutionContext();
        ctx.setTaskParams(taskParams);
        ctx.setTaskName("empty_result_task");
        ctx.setResourceParametersHelper(getResourceParametersHelperWithDatasourceType(DbType.HIVE));

        SqlTask task = new SqlTask(ctx);

        ResultSet mockResultSet = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnLabel(1)).thenReturn("id");
        when(mockMetaData.getColumnLabel(2)).thenReturn("name");
        when(mockResultSet.next()).thenReturn(false); // empty result set

        Method resultProcessMethod = SqlTask.class.getDeclaredMethod("resultProcess", ResultSet.class);
        resultProcessMethod.setAccessible(true);
        resultProcessMethod.invoke(task, mockResultSet);

        Assertions.assertTrue(ctx.isNeedAlert());

        TaskAlertInfo alertInfo = ctx.getTaskAlertInfo();
        Assertions.assertNotNull(alertInfo);
        Assertions.assertEquals(3, alertInfo.getAlertGroupId());
        Assertions.assertEquals("empty result", alertInfo.getTitle());
        Assertions.assertEquals(AlertType.TASK_RESULT, alertInfo.getAlertType());

        ArrayNode contentArray = JSONUtils.parseArray(alertInfo.getContent());
        Assertions.assertEquals(1, contentArray.size());
        Assertions.assertEquals("", contentArray.get(0).get("id").asText());
        Assertions.assertEquals("", contentArray.get(0).get("name").asText());
    }

    @Test
    void testTaskExecutionContextCarriesAlertInfoForEventPropagation() {
        TaskExecutionContext ctx = new TaskExecutionContext();
        ctx.setNeedAlert(true);

        TaskAlertInfo alertInfo = new TaskAlertInfo();
        alertInfo.setTitle("test title");
        alertInfo.setContent("[{\"id\":\"1\"}]");
        alertInfo.setAlertGroupId(7);
        alertInfo.setAlertType(AlertType.TASK_RESULT);
        ctx.setTaskAlertInfo(alertInfo);

        Assertions.assertTrue(ctx.isNeedAlert());
        Assertions.assertNotNull(ctx.getTaskAlertInfo());
        Assertions.assertEquals("test title", ctx.getTaskAlertInfo().getTitle());
        Assertions.assertEquals(7, ctx.getTaskAlertInfo().getAlertGroupId());
        Assertions.assertEquals(AlertType.TASK_RESULT, ctx.getTaskAlertInfo().getAlertType());

        TaskExecutionContext ctx2 = new TaskExecutionContext();
        Assertions.assertFalse(ctx2.isNeedAlert());
        Assertions.assertNull(ctx2.getTaskAlertInfo());
    }

}
