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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcedureTaskTest {

    @Mock
    private TaskExecutionContext context;

    @Mock
    private ResourceParametersHelper resourceHelper;

    @Mock
    private ProcedureParameters params;

    private ProcedureTask task;

    @BeforeEach
    void setUp() {
        String validJson = "{"
                + "\"type\":\"MYSQL\","
                + "\"datasource\":\"test_db\","
                + "\"method\":\"{call my_proc(?, ?)}\","
                + "\"localParams\":{},"
                + "\"outProperty\":{}"
                + "}";

        when(context.getTaskParams()).thenReturn(validJson);
        when(context.getResourceParametersHelper()).thenReturn(resourceHelper);

        try (MockedStatic<JSONUtils> jsonUtilsMock = Mockito.mockStatic(JSONUtils.class)) {
            jsonUtilsMock.when(() -> JSONUtils.parseObject(anyString(), eq(ProcedureParameters.class)))
                    .thenReturn(params);
            when(params.checkParameters()).thenReturn(true);
            when(params.generateExtendedContext(any())).thenReturn(mock(ProcedureTaskExecutionContext.class));

            task = new ProcedureTask(context);
        }
    }

    @Test
    void constructorThrowsTaskExceptionWhenParametersInvalid() {
        when(context.getTaskParams()).thenReturn("{}");

        try (MockedStatic<JSONUtils> jsonUtilsMock = Mockito.mockStatic(JSONUtils.class)) {
            ProcedureParameters badParams = mock(ProcedureParameters.class);
            jsonUtilsMock.when(() -> JSONUtils.parseObject(anyString(), eq(ProcedureParameters.class)))
                    .thenReturn(badParams);
            when(badParams.checkParameters()).thenReturn(false);

            TaskException ex = assertThrows(TaskException.class, () -> new ProcedureTask(context));
            assertTrue(ex.getMessage().contains("not valid"));
        }
    }

    @Test
    void cancelWithActiveStatementCancelThenSetKillCode() throws Exception {
        CallableStatement stmt = mock(CallableStatement.class);
        setPrivateField(this.task, "sessionStatement", stmt);

        task.cancel();

        verify(stmt).cancel();
        assertEquals(TaskConstants.EXIT_CODE_KILL, getExitStatusCode());
    }

    @Test
    void cancelWhenStatementCancelFailThenSetFailureCode() throws Exception {
        CallableStatement stmt = mock(CallableStatement.class);
        setPrivateField(this.task, "sessionStatement", stmt);

        SQLException sqlEx = new SQLException("Driver does not support cancel");
        doThrow(sqlEx).when(stmt).cancel();

        TaskException ex = assertThrows(TaskException.class, () -> task.cancel());

        assertEquals(ex.getCause(), sqlEx);
        assertEquals(TaskConstants.EXIT_CODE_FAILURE, getExitStatusCode());
    }

    private int getExitStatusCode() {
        return readPrivateField(this.task, "exitStatusCode");
    }

    // Helper: get private field via reflection
    @SuppressWarnings("unchecked")
    private <T> T readPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read field: " + fieldName, e);
        }
    }

    // Helper: set private field via reflection
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
