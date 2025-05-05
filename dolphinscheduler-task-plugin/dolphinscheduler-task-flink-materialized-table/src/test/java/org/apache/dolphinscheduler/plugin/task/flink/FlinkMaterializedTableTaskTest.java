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

package org.apache.dolphinscheduler.plugin.task.flink;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.MockFlinkSqlGateway;

import java.io.IOException;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlinkMaterializedTableTaskTest {

    @Mock
    private TaskExecutionContext taskExecutionContext;

    private MockFlinkSqlGateway mockGateway;
    private FlinkMaterializedTableTask task;
    private FlinkMaterializedTableParameters parameters;

    @BeforeEach
    void setUp() throws IOException {
        // Create and start mock gateway
        mockGateway = new MockFlinkSqlGateway();
        mockGateway.start();

        // Set up task parameters
        parameters = new FlinkMaterializedTableParameters();
        parameters.setIdentifier("catalog.database.table");
        parameters.setGatewayEndpoint(mockGateway.getBaseUrl());
        parameters.setInitConfig(new HashMap<>());
        parameters.setDynamicOptions(new HashMap<>());
        parameters.setExecutionConfig(new HashMap<>());

        // Set up task context
        when(taskExecutionContext.getTaskParams()).thenReturn(JSONUtils.toJsonString(parameters));

        // Create task instance
        task = new FlinkMaterializedTableTask(taskExecutionContext);
        task.init();
    }

    @AfterEach
    void tearDown() {
        if (mockGateway != null) {
            mockGateway.stop();
        }
    }

    @Test
    void testInit() {
        assertDoesNotThrow(() -> task.init());
    }

    @Test
    void testHandle() {
        assertDoesNotThrow(() -> task.handle(mock(TaskCallBack.class)));
    }

    @Test
    void testHandleWithInvalidEndpoint() throws IOException {
        // Set invalid gateway endpoint
        parameters.setGatewayEndpoint("http://invalid:8080");
        when(taskExecutionContext.getTaskParams()).thenReturn(JSONUtils.toJsonString(parameters));
        task.init();

        assertThrows(TaskException.class, () -> task.handle(mock(TaskCallBack.class)));
    }

    @Test
    void testCancel() {
        assertDoesNotThrow(() -> task.cancel());
    }

    @Test
    void testGetApplicationIds() {
        assertDoesNotThrow(() -> task.getApplicationIds());
    }
}
