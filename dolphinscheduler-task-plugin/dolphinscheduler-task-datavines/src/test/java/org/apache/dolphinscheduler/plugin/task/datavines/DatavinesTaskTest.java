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
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.datavines.utils.RequestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        when(taskExecutionContext.getTaskParams())
                .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
        datavinesTask.init();
        assertDoesNotThrow(() -> datavinesTask.submitApplication());
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

            // 第一次调用 RequestUtils.getJobExecutionResult
            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionStatus(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeStatus);

            // 第二次调用 RequestUtils.getJobExecutionResult
            requestUtilsStatic
                    .when(() -> RequestUtils.getJobExecutionResult(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(executeResult);

            datavinesTask.trackApplicationStatus();
            Assertions.assertEquals(EXIT_CODE_SUCCESS, datavinesTask.getExitStatusCode());
        }
    }

    @Test
    void trackApplicationStatusJobExecutionFailureSetsExitCodeFailure() throws TaskException {
        when(taskExecutionContext.getTaskParams())
                .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
        datavinesTask.init();
        datavinesTask.submitApplication();
        datavinesTask.trackApplicationStatus();
        Assertions.assertEquals(EXIT_CODE_FAILURE, datavinesTask.getExitStatusCode());
    }

    @Test
    void cancelApplicationTerminatesJobSuccessfully() {
        when(taskExecutionContext.getTaskParams())
                .thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
        datavinesTask.init();
        assertDoesNotThrow(() -> datavinesTask.cancelApplication());
    }

    @Test
    void checkParametersValidParametersReturnsTrue() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress("http://localhost");
        parameters.setJobId("1");
        Assertions.assertTrue(parameters.checkParameters());
    }

    @Test
    void checkParametersEmptyAddressReturnsFalse() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress("");
        parameters.setJobId("1");
        Assertions.assertFalse(parameters.checkParameters());
    }

    @Test
    void checkParametersEmptyJobIdReturnsFalse() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress("http://localhost");
        parameters.setJobId("");
        Assertions.assertFalse(parameters.checkParameters());
    }

    @Test
    void checkParametersNullAddressReturnsFalse() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress(null);
        parameters.setJobId("1");
        Assertions.assertFalse(parameters.checkParameters());
    }

    @Test
    void checkParametersNullJobIdReturnsFalse() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress("http://localhost");
        parameters.setJobId(null);
        Assertions.assertFalse(parameters.checkParameters());
    }

    @Test
    void getResourceFilesListReturnsEmptyList() {
        DatavinesParameters parameters = new DatavinesParameters();
        Assertions.assertTrue(parameters.getResourceFilesList().isEmpty());
    }

    @Test
    void killJobExecutionValidParametersExecutesSuccessfully() {
        String address = "http://localhost";
        String jobExecutionId = "1";
        String token = "token";
        try (MockedStatic<RequestUtils> requestUtilsStatic = Mockito.mockStatic(RequestUtils.class)) {
            requestUtilsStatic
                    .when(() -> RequestUtils
                            .doPost(address + DatavinesTaskConstants.JOB_EXECUTION_KILL + jobExecutionId, token))
                    .thenReturn("");
            RequestUtils.killJobExecution(address, jobExecutionId, token);
            // No exception means success
        }
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

}
