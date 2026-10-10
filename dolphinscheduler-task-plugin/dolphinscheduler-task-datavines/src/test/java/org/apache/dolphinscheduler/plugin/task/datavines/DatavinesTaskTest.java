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

package org.apache.dolphinscheduler.plugin.task.datavines;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.datavines.utils.RequestUtils;

import org.apache.http.HttpStatus;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.fasterxml.jackson.databind.JsonNode;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DatavinesTaskTest {

    @Mock
    private TaskExecutionContext taskExecutionContext;

    private DatavinesTask datavinesTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        datavinesTask = new DatavinesTask(taskExecutionContext);
    }

    @Test
    void initValidParametersInitializesSuccessfully() {
        when(taskExecutionContext.getTaskParams())
                .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
        datavinesTask.init();
        Assertions.assertNotNull(datavinesTask.getParameters());
    }

    @Test
    void initInvalidParametersThrowsException() {
        when(taskExecutionContext.getTaskParams()).thenReturn("{}");
        assertThrows(DatavinesTaskException.class, () -> datavinesTask.init());
    }

    @Test
    void submitApplicationExecutesJobSuccessfully() {
        JsonNode executeJobResult = RequestUtils.parse("{\"code\":200,\"data\":\"1\"}");
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
            datavinesTask.init();
            requestUtilsStatic.when(() -> RequestUtils.executeJob(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeJobResult);
            assertDoesNotThrow(() -> datavinesTask.submitApplication());
        }
    }

    @Test
    void trackApplicationStatusJobExecutionSuccessSetsExitCodeSuccess() throws TaskException {
        JsonNode executeJobResult = RequestUtils.parse("{\"code\":\"200\",\"data\":\"1\"}");
        JsonNode executeStatus = RequestUtils.parse("{\"code\":\"200\",\"data\":\"SUCCESS\"}");
        JsonNode executeResult = RequestUtils.parse("{\"code\":\"200\",\"data\":\"1\"}");
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
            datavinesTask.init();

            requestUtilsStatic.when(() -> RequestUtils.executeJob(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeJobResult);
            datavinesTask.submitApplication();

            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeStatus);

            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionResult(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeResult);

            datavinesTask.trackApplicationStatus();
            Assertions.assertEquals(EXIT_CODE_SUCCESS, datavinesTask.getExitStatusCode());
        }
    }

    @Test
    void trackApplicationStatusJobExecutionFailureSetsExitCodeFailure() throws TaskException {
        JsonNode executeJobResult = RequestUtils.parse("{\"code\":500,\"msg\":\"error\",\"data\":\"error\"}");
        JsonNode failureStatus = RequestUtils.parse("{\"code\":200,\"data\":\"FAILURE\"}");
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
            datavinesTask.init();
            requestUtilsStatic.when(() -> RequestUtils.executeJob(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeJobResult);
            datavinesTask.submitApplication();

            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(failureStatus);
            datavinesTask.trackApplicationStatus();
            Assertions.assertEquals(EXIT_CODE_FAILURE, datavinesTask.getExitStatusCode());
            requestUtilsStatic.verify(
                    () -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()),
                    Mockito.never());
        }
    }

    @Test
    void trackApplicationStatusMissingExecutionIdSetsExitCodeFailure() throws TaskException {
        JsonNode executeJobResult = RequestUtils.parse("{\"code\":200}");
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
            datavinesTask.init();
            requestUtilsStatic.when(() -> RequestUtils.executeJob(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeJobResult);

            datavinesTask.submitApplication();
            datavinesTask.trackApplicationStatus();

            Assertions.assertEquals(EXIT_CODE_FAILURE, datavinesTask.getExitStatusCode());
            requestUtilsStatic.verify(
                    () -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()),
                    Mockito.never());
        }
    }

    @Test
    void trackApplicationStatusRestoresExecutionIdFromAppIds() throws TaskException {
        JsonNode successStatus = RequestUtils.parse("{\"code\":200,\"data\":\"SUCCESS\"}");
        JsonNode successResult = RequestUtils.parse("{\"code\":200,\"data\":\"SUCCESS\"}");
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
            when(taskExecutionContext.getAppIds()).thenReturn("http://localhost-1");
            datavinesTask.init();

            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(successStatus);
            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionResult(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(successResult);

            datavinesTask.trackApplicationStatus();
            Assertions.assertEquals(EXIT_CODE_SUCCESS, datavinesTask.getExitStatusCode());
            requestUtilsStatic.verify(
                    () -> RequestUtils.getJobExecutionStatus("http://localhost", "1", "token"));
        }
    }

    @Test
    void cancelApplicationTerminatesJobSuccessfully() throws NoSuchFieldException, IllegalAccessException {
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
            datavinesTask.init();
            Field jobExecutionIdField = DatavinesTask.class.getDeclaredField("jobExecutionId");
            jobExecutionIdField.setAccessible(true);
            jobExecutionIdField.set(datavinesTask, "1");
            assertDoesNotThrow(() -> datavinesTask.cancelApplication());
            requestUtilsStatic.verify(() -> RequestUtils.killJobExecution("http://localhost", "1", "token"));
        }
    }

    @Test
    void cancelApplicationWithoutJobExecutionIdSkipsKill() {
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
            datavinesTask.init();
            assertDoesNotThrow(() -> datavinesTask.cancelApplication());
            requestUtilsStatic.verify(
                    () -> RequestUtils.killJobExecution(Mockito.any(), Mockito.any(), Mockito.any()),
                    Mockito.never());
        }
    }

    @Test
    void checkParametersValidParametersReturnsTrue() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress("http://localhost");
        parameters.setJobId("1");
        parameters.setToken("token");
        Assertions.assertTrue(parameters.checkParameters());
    }

    static Stream<Arguments> invalidCheckParametersInputs() {
        return Stream.of(
                Arguments.of("", "1", "token"),
                Arguments.of("http://localhost", "", "token"),
                Arguments.of(null, "1", "token"),
                Arguments.of("http://localhost", null, "token"),
                Arguments.of("http://localhost", "1", ""),
                Arguments.of("http://localhost", "1", null));
    }

    @ParameterizedTest
    @MethodSource("invalidCheckParametersInputs")
    void checkParametersInvalidInputsReturnsFalse(String address, String jobId, String token) {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress(address);
        parameters.setJobId(jobId);
        parameters.setToken(token);
        Assertions.assertFalse(parameters.checkParameters());
    }

    @Test
    void getResourceFilesListReturnsEmptyList() {
        DatavinesParameters parameters = new DatavinesParameters();
        Assertions.assertTrue(parameters.getResourceFilesList().isEmpty());
    }

    @Test
    void killJobExecutionValidParametersExecutesSuccessfully() {
        assertDoesNotThrow(() -> RequestUtils.validateKillResponse(HttpStatus.SC_OK, "{\"code\":200}"));
    }

    @Test
    void killJobExecutionInvalidHttpStatusThrowsException() {
        assertThrows(IllegalStateException.class, () -> RequestUtils.validateKillResponse(500, "{\"code\":500}"));
    }

    @Test
    void killJobExecutionInvalidResponseThrowsException() {
        assertThrows(IllegalStateException.class, () -> RequestUtils.validateKillResponse(HttpStatus.SC_OK, "invalid"));
        assertThrows(IllegalStateException.class,
                () -> RequestUtils.validateKillResponse(HttpStatus.SC_OK, "{\"code\":500}"));
    }

    @Test
    void parseValidJsonStringReturnsJsonNode() {
        String jsonString = "{\"code\":\"200\",\"data\":\"SUCCESS\"}";
        JsonNode result = RequestUtils.parse(jsonString);
        Assertions.assertEquals("200", result.get("code").asText());
        Assertions.assertEquals("SUCCESS", result.get("data").asText());
    }

    @Test
    void parseInvalidJsonStringReturnsNull() {
        String jsonString = "invalid json";
        JsonNode result = RequestUtils.parse(jsonString);
        Assertions.assertNull(result);
    }

    @Test
    void getApplicationIdsReturnsEmptyList() throws TaskException {
        when(taskExecutionContext.getTaskParams())
                .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
        datavinesTask.init();
        Assertions.assertTrue(datavinesTask.getApplicationIds().isEmpty());
    }

    @Test
    void trackApplicationStatusExecutionStatusTrueWithNullJobExecutionIdSetsExitCodeFailure() throws TaskException, NoSuchFieldException, IllegalAccessException {
        when(taskExecutionContext.getTaskParams())
                .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
        datavinesTask.init();

        Field executionStatusField = DatavinesTask.class.getDeclaredField("executionStatus");
        executionStatusField.setAccessible(true);
        executionStatusField.set(datavinesTask, true);
        // jobExecutionId remains null (default)

        datavinesTask.trackApplicationStatus();
        Assertions.assertEquals(EXIT_CODE_FAILURE, datavinesTask.getExitStatusCode());
    }

    @Test
    void trackApplicationStatusJobStatusFailureSetsExitCodeFailure() throws TaskException {
        JsonNode executeJobResult = RequestUtils.parse("{\"code\":200,\"data\":\"1\"}");
        JsonNode failureStatus = RequestUtils.parse("{\"code\":200,\"data\":\"FAILURE\"}");
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
            datavinesTask.init();

            requestUtilsStatic.when(() -> RequestUtils.executeJob(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeJobResult);
            datavinesTask.submitApplication();

            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(failureStatus);

            datavinesTask.trackApplicationStatus();
            Assertions.assertEquals(EXIT_CODE_FAILURE, datavinesTask.getExitStatusCode());
        }
    }

    @Test
    void trackApplicationStatusJobStatusKillSetsExitCodeFailure() throws TaskException {
        JsonNode executeJobResult = RequestUtils.parse("{\"code\":200,\"data\":\"1\"}");
        JsonNode killStatus = RequestUtils.parse("{\"code\":200,\"data\":\"KILL\"}");
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
            datavinesTask.init();

            requestUtilsStatic.when(() -> RequestUtils.executeJob(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeJobResult);
            datavinesTask.submitApplication();

            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(killStatus);

            datavinesTask.trackApplicationStatus();
            Assertions.assertEquals(EXIT_CODE_FAILURE, datavinesTask.getExitStatusCode());
        }
    }

    static Stream<String> terminalStatuses() {
        return Stream.of("PAUSE", "STOP", "NEED_FAULT_TOLERANCE");
    }

    @ParameterizedTest
    @MethodSource("terminalStatuses")
    void trackApplicationStatusOtherTerminalStatusSetsExitCodeFailure(String terminalStatus) throws TaskException {
        JsonNode executeJobResult = RequestUtils.parse("{\"code\":200,\"data\":\"1\"}");
        JsonNode terminalStatusResult = RequestUtils.parse("{\"code\":200,\"data\":\"" + terminalStatus + "\"}");
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
            datavinesTask.init();

            requestUtilsStatic.when(() -> RequestUtils.executeJob(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeJobResult);
            datavinesTask.submitApplication();

            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(terminalStatusResult);

            datavinesTask.trackApplicationStatus();
            Assertions.assertEquals(EXIT_CODE_FAILURE, datavinesTask.getExitStatusCode());
        }
    }

    @Test
    void trackApplicationStatusGetJobStatusApiErrorSetsExitCodeFailure() throws TaskException {
        JsonNode executeJobResult = RequestUtils.parse("{\"code\":200,\"data\":\"1\"}");
        JsonNode errorStatus = RequestUtils.parse("{\"code\":500,\"msg\":\"error\",\"data\":\"error\"}");
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
            datavinesTask.init();

            requestUtilsStatic.when(() -> RequestUtils.executeJob(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeJobResult);
            datavinesTask.submitApplication();

            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(errorStatus);

            datavinesTask.trackApplicationStatus();
            Assertions.assertEquals(EXIT_CODE_FAILURE, datavinesTask.getExitStatusCode());
        }
    }

    @Test
    void trackApplicationStatusGetJobResultApiErrorSetsExitCodeFailure() throws TaskException {
        JsonNode executeJobResult = RequestUtils.parse("{\"code\":200,\"data\":\"1\"}");
        JsonNode successStatus = RequestUtils.parse("{\"code\":200,\"data\":\"SUCCESS\"}");
        JsonNode errorResult = RequestUtils.parse("{\"code\":500,\"msg\":\"error\",\"data\":\"error\"}");
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
            datavinesTask.init();

            requestUtilsStatic.when(() -> RequestUtils.executeJob(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeJobResult);
            datavinesTask.submitApplication();

            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(successStatus);
            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionResult(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(errorResult);

            datavinesTask.trackApplicationStatus();
            Assertions.assertEquals(EXIT_CODE_FAILURE, datavinesTask.getExitStatusCode());
            requestUtilsStatic.verify(
                    () -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()),
                    Mockito.times(1));
        }
    }

    @Test
    void trackApplicationStatusFailureBlockTrueWithSuccessResultSetsExitCodeSuccess() throws TaskException {
        JsonNode executeJobResult = RequestUtils.parse("{\"code\":200,\"data\":\"1\"}");
        JsonNode successStatus = RequestUtils.parse("{\"code\":200,\"data\":\"SUCCESS\"}");
        JsonNode successResult = RequestUtils.parse("{\"code\":200,\"data\":\"SUCCESS\"}");
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn(
                            "{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\",\"failureBlock\":true}");
            datavinesTask.init();

            requestUtilsStatic.when(() -> RequestUtils.executeJob(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeJobResult);
            datavinesTask.submitApplication();

            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(successStatus);
            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionResult(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(successResult);

            datavinesTask.trackApplicationStatus();
            Assertions.assertEquals(EXIT_CODE_SUCCESS, datavinesTask.getExitStatusCode());
        }
    }

    @Test
    void trackApplicationStatusFailureBlockTrueWithNonSuccessResultSetsExitCodeFailure() throws TaskException {
        JsonNode executeJobResult = RequestUtils.parse("{\"code\":200,\"data\":\"1\"}");
        JsonNode successStatus = RequestUtils.parse("{\"code\":200,\"data\":\"SUCCESS\"}");
        JsonNode failureResult = RequestUtils.parse("{\"code\":200,\"data\":\"FAILURE\"}");
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            when(taskExecutionContext.getTaskParams())
                    .thenReturn(
                            "{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\",\"failureBlock\":true}");
            datavinesTask.init();

            requestUtilsStatic.when(() -> RequestUtils.executeJob(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeJobResult);
            datavinesTask.submitApplication();

            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(successStatus);
            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionResult(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(failureResult);

            datavinesTask.trackApplicationStatus();
            Assertions.assertEquals(EXIT_CODE_FAILURE, datavinesTask.getExitStatusCode());
        }
    }

}
