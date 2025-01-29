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
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
        when(taskExecutionContext.getTaskParams()).thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
        datavinesTask.init();
        assertNotNull(datavinesTask.getParameters());
    }

    @Test
    void initInvalidParametersThrowsException() {
        when(taskExecutionContext.getTaskParams()).thenReturn("{}");
        assertThrows(DatavinesTaskException.class, () -> datavinesTask.init());
    }

    @Test
    void submitApplicationExecutesJobSuccessfully() {
        when(taskExecutionContext.getTaskParams()).thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
        datavinesTask.init();
        assertDoesNotThrow(() -> datavinesTask.submitApplication());
    }

    @Test
    void trackApplicationStatusJobExecutionFailureSetsExitCodeFailure() throws TaskException {
        when(taskExecutionContext.getTaskParams()).thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
        datavinesTask.init();
        datavinesTask.submitApplication();
        datavinesTask.trackApplicationStatus();
        assertEquals(EXIT_CODE_FAILURE, datavinesTask.getExitStatusCode());
    }

    @Test
    void cancelApplicationTerminatesJobSuccessfully() {
        when(taskExecutionContext.getTaskParams()).thenReturn("{\"address\":\"http://localhost\",\"jobId\":\"1\",\"token\":\"token\"}");
        datavinesTask.init();
        assertDoesNotThrow(() -> datavinesTask.cancelApplication());
    }

    @Test
    void checkParametersValidParametersReturnsTrue() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress("http://localhost");
        parameters.setJobId("1");
        assertTrue(parameters.checkParameters());
    }

    @Test
    void checkParametersEmptyAddressReturnsFalse() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress("");
        parameters.setJobId("1");
        assertFalse(parameters.checkParameters());
    }

    @Test
    void checkParametersEmptyJobIdReturnsFalse() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress("http://localhost");
        parameters.setJobId("");
        assertFalse(parameters.checkParameters());
    }

    @Test
    void checkParametersNullAddressReturnsFalse() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress(null);
        parameters.setJobId("1");
        assertFalse(parameters.checkParameters());
    }

    @Test
    void checkParametersNullJobIdReturnsFalse() {
        DatavinesParameters parameters = new DatavinesParameters();
        parameters.setAddress("http://localhost");
        parameters.setJobId(null);
        assertFalse(parameters.checkParameters());
    }

    @Test
    void getResourceFilesListReturnsEmptyList() {
        DatavinesParameters parameters = new DatavinesParameters();
        assertTrue(parameters.getResourceFilesList().isEmpty());
    }

}