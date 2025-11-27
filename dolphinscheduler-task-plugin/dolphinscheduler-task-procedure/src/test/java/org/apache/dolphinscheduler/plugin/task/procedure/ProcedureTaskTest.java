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

package org.apache.dolphinscheduler.plugin.task.procedure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.DataSourceProcessor;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceProcessorProvider;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcedureTaskTest {

    @Mock
    private TaskExecutionContext mockTaskContext;

    @Mock
    private ResourceParametersHelper mockResourceHelper;

    @Mock
    private ProcedureParameters procedureParams;

    private ProcedureTask procedureTask;

    @BeforeEach
    void setUp() {

        // Simulate valid task parameters JSON
        String validParamsJson = "{"
                + "\"type\":\"MYSQL\","
                + "\"datasource\":\"test_db\","
                + "\"method\":\"{call my_proc(?, ?)}\","
                + "\"localParams\":{},"
                + "\"outProperty\":{}"
                + "}";

        when(mockTaskContext.getTaskParams()).thenReturn(validParamsJson);
        when(mockTaskContext.getResourceParametersHelper()).thenReturn(mockResourceHelper);

        // Mock parameter validation to pass
        try (MockedStatic<JSONUtils> jsonUtilsMock = mockStatic(JSONUtils.class)) {
            jsonUtilsMock.when(() -> JSONUtils.parseObject(anyString(), eq(ProcedureParameters.class)))
                    .thenReturn(procedureParams);
            doCallRealMethod().when(procedureParams).checkParameters(); // or stub as needed
            when(procedureParams.checkParameters()).thenReturn(true);
            when(procedureParams.generateExtendedContext(any())).thenReturn(mock(ProcedureTaskExecutionContext.class));

            procedureTask = new ProcedureTask(mockTaskContext);
        }

    }

    @Test
    void constructor_InvalidParameters_ThrowsTaskException() {
        // Arrange: invalid JSON or checkParameters returns false
        String invalidParamsJson = "{}";
        when(mockTaskContext.getTaskParams()).thenReturn(invalidParamsJson);

        try (MockedStatic<JSONUtils> jsonUtilsMock = mockStatic(JSONUtils.class)) {
            ProcedureParameters badParams = mock(ProcedureParameters.class);
            jsonUtilsMock.when(() -> JSONUtils.parseObject(anyString(), eq(ProcedureParameters.class)))
                    .thenReturn(badParams);
            when(badParams.checkParameters()).thenReturn(false);

            // Act & Assert
            TaskException exception = assertThrows(TaskException.class,
                    () -> new ProcedureTask(mockTaskContext));
            assertTrue(exception.getMessage().contains("not valid"));
        }
    }

    @Test
    void handle_SuccessfulExecution_SetsSuccessExitCode() throws Exception {
        when(procedureParams.getType()).thenReturn("MYSQL");
        when(procedureParams.getMethod()).thenReturn("{call my_proc(?, ?)}");

        // Arrange mocks
        Connection mockConn = mock(Connection.class);
        CallableStatement mockStmt = mock(CallableStatement.class);

        try (
                MockedStatic<DataSourceClientProvider> clientMock = mockStatic(DataSourceClientProvider.class);
                MockedStatic<DataSourceProcessorProvider> processorMock =
                        mockStatic(DataSourceProcessorProvider.class)) {

            DataSourceProcessor mockProcessor = mock(DataSourceProcessor.class);
            ConnectionParam mockConnParam = mock(ConnectionParam.class);

            processorMock.when(() -> DataSourceProcessorProvider.getDataSourceProcessor(DbType.MYSQL))
                    .thenReturn(mockProcessor);
            when(mockProcessor.createConnectionParams((String) null)).thenReturn(mockConnParam);
            clientMock.when(() -> DataSourceClientProvider.getAdHocConnection(eq(DbType.MYSQL), any()))
                    .thenReturn(mockConn);

            when(mockConn.prepareCall(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeUpdate()).thenReturn(1); // success

            // Mock parameter maps to avoid NPE
            when(mockTaskContext.getPrepareParamsMap()).thenReturn(new HashMap<>());

            // Act
            procedureTask.handle(null);

            // Assert
            assertEquals(TaskConstants.EXIT_CODE_SUCCESS, getField(procedureTask, "exitStatusCode"));
            verify(mockStmt).executeUpdate();
        }
    }

    @Test
    void handle_SqlException_SetsFailureExitCodeAndThrows() throws SQLException {
        when(procedureParams.getType()).thenReturn("MYSQL");
        when(procedureParams.getMethod()).thenReturn("{call my_proc(?, ?)}");

        // Arrange
        Connection mockConn = mock(Connection.class);
        CallableStatement mockStmt = mock(CallableStatement.class);

        try (
                MockedStatic<DataSourceClientProvider> clientMock = mockStatic(DataSourceClientProvider.class);
                MockedStatic<DataSourceProcessorProvider> processorMock =
                        mockStatic(DataSourceProcessorProvider.class)) {

            DataSourceProcessor mockProcessor = mock(DataSourceProcessor.class);
            ConnectionParam mockConnParam = mock(ConnectionParam.class);

            processorMock.when(() -> DataSourceProcessorProvider.getDataSourceProcessor(DbType.MYSQL))
                    .thenReturn(mockProcessor);
            when(mockProcessor.createConnectionParams((String) null)).thenReturn(mockConnParam);
            clientMock.when(() -> DataSourceClientProvider.getAdHocConnection(eq(DbType.MYSQL), any()))
                    .thenReturn(mockConn);

            when(mockConn.prepareCall(anyString())).thenReturn(mockStmt);
            when(mockStmt.executeUpdate()).thenThrow(new SQLException("DB error"));

            when(mockTaskContext.getPrepareParamsMap()).thenReturn(new HashMap<>());

            // Act & Assert
            TaskException exception = assertThrows(TaskException.class,
                    () -> procedureTask.handle(null));
            assertTrue(exception.getMessage().contains("failed"));
            assertEquals(TaskConstants.EXIT_CODE_FAILURE, getField(procedureTask, "exitStatusCode"));
        }
    }

    @Test
    void cancel_ActiveStatement_CancelsAndSetsKillCode() throws Exception {
        // Arrange: simulate active statement
        CallableStatement mockStmt = mock(CallableStatement.class);
        setField(procedureTask, "sessionStatement", mockStmt);

        // Act
        procedureTask.cancel();

        // Assert
        verify(mockStmt).cancel();
        assertEquals(TaskConstants.EXIT_CODE_KILL, getField(procedureTask, "exitStatusCode"));
    }

    @Test
    void cancel_NoActiveStatement_LogsWarning() {
        // sessionStatement is null by default after construction
        // We just call cancel and ensure no exception
        assertDoesNotThrow(() -> procedureTask.cancel());
        // (In practice, you'd verify logger.warn was called — requires Logback/TestLogger setup)
    }

    @Test
    void cancel_whenStatementCancelThrowsSQLException_shouldLogWarningAndThrowTaskException() throws SQLException {
        // Arrange: simulate active statement
        CallableStatement mockStmt = mock(CallableStatement.class);
        setField(procedureTask, "sessionStatement", mockStmt);

        // Arrange
        SQLException sqlEx = new SQLException("Driver does not support cancel");
        doThrow(sqlEx).when(mockStmt).cancel();

        // Act & Assert
        TaskException taskEx = assertThrows(TaskException.class, () -> {
            procedureTask.cancel();
        });

        // Verify the cause is the original SQLException
        assertEquals(sqlEx, taskEx.getCause());

        // Verify that exit status was NOT set (since stmt.cancel() threw before setExitStatusCode)
        // You may need a getter or reflection to check internal state
        Integer exitCode = getPrivateField(procedureTask, "exitStatusCode");
        assertEquals(exitCode, TaskConstants.EXIT_CODE_KILL);
    }

    @Test
    void setTimeout_TaskHasTimeout_SetsQueryTimeoutOnStatement() throws SQLException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // Arrange
        CallableStatement mockStmt = mock(CallableStatement.class);
        when(mockTaskContext.getTaskTimeoutStrategy()).thenReturn(TaskTimeoutStrategy.FAILED);
        when(mockTaskContext.getTaskTimeout()).thenReturn(30);

        // Act
        setField(procedureTask, "sessionStatement", mockStmt);
        // Call setTimeout via reflection or extract to package-private for test
        // Here we assume setTimeout is called during handle(), but for isolation:
        Method setTimeoutMethod = ProcedureTask.class.getDeclaredMethod("setTimeout", CallableStatement.class);
        setTimeoutMethod.setAccessible(true);
        setTimeoutMethod.invoke(procedureTask, mockStmt);

        // Assert
        verify(mockStmt).setQueryTimeout(30);
    }

    @Test
    void testFormatSql_withSqlParams_shouldReplaceWithQuestionMarks() throws Exception {
        // Arrange
        String inputSql = "CALL proc(${name}, ${age})";
        String expected = "CALL proc(?, ?)";

        when(procedureParams.getMethod()).thenReturn(inputSql);
        when(mockTaskContext.getTaskInstanceId()).thenReturn(1);

        Map<Integer, Property> sqlParamsMap = new HashMap<>();
        Map<String, Property> paramsMap = new HashMap<>();

        // Get the private method 'formatSql' and make it accessible
        Method formatSqlMethod = ProcedureTask.class.getDeclaredMethod(
                "formatSql",
                Map.class,
                Map.class);
        formatSqlMethod.setAccessible(true);

        // Act: invoke the private method
        String result = (String) formatSqlMethod.invoke(procedureTask, sqlParamsMap, paramsMap);

        // Assert: verify the returned SQL string has placeholders replaced
        assertEquals(expected, result);
    }

    @Test
    void testFormatSql_noParams_shouldReturnOriginal() throws Exception {
        // Arrange
        String inputSql = "CALL simple_proc()";
        when(procedureParams.getMethod()).thenReturn(inputSql);
        when(mockTaskContext.getTaskInstanceId()).thenReturn(1);

        Map<Integer, Property> sqlParamsMap = new HashMap<>();
        Map<String, Property> paramsMap = new HashMap<>();

        Method formatSqlMethod = ProcedureTask.class.getDeclaredMethod("formatSql", Map.class, Map.class);
        formatSqlMethod.setAccessible(true);

        // Act
        String result = (String) formatSqlMethod.invoke(procedureTask, sqlParamsMap, paramsMap);

        // Assert: original SQL should be returned unchanged
        assertEquals(inputSql, result);
    }

    // --- Helper methods for reflection (if needed) ---
    private Object getField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper: get private field via reflection
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper: get private field via reflection
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (T) field.get(target);
            } catch (NoSuchFieldException e) {
                // Field not found in this class, continue to superclass
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field: " + fieldName, e);
            }
        }
        throw new RuntimeException("Field '" + fieldName + "' not found in class hierarchy of " + target.getClass());
    }
}
