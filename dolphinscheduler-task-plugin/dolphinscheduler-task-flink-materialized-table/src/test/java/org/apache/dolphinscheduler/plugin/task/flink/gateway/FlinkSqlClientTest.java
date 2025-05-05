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

package org.apache.dolphinscheduler.plugin.task.flink.gateway;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.FetchResultResponseBody;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.JobStatus;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.RefreshMaterializedTableRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlinkSqlClientTest {

    private MockFlinkSqlGateway mockGateway;
    private FlinkSqlClient client;

    @BeforeEach
    void setUp() throws IOException {
        // Start mock server on a random port
        mockGateway = new MockFlinkSqlGateway();
        mockGateway.start();

        // Create client with mock server endpoint
        client = new FlinkSqlClient(mockGateway.getBaseUrl());
    }

    @AfterEach
    void tearDown() {
        if (mockGateway != null) {
            mockGateway.stop();
        }
    }

    @Test
    void testOpenSession() throws IOException {
        Map<String, String> properties = new HashMap<>();
        properties.put("test.property", "test.value");
        String sessionHandle = client.openSession(properties);
        assertNotNull(sessionHandle);
    }

    @Test
    void testExecuteStatement() throws Exception {
        // First open a session
        String sessionHandle = client.openSession(new HashMap<>());

        // Execute a statement
        String operationHandle = client.executeStatement(sessionHandle, "SELECT 1");
        assertNotNull(operationHandle);

        FetchResultResponseBody fetchResultResponseBody = client.waitForOperationResult(sessionHandle, operationHandle);
        assertNotNull(fetchResultResponseBody);
        Assertions.assertEquals(fetchResultResponseBody.getResult().get(0).getValues().get(0), "1");
    }

    @Test
    void testRefreshMaterializedTable() throws Exception {
        // First open a session
        String sessionHandle = client.openSession(new HashMap<>());

        // Create refresh request
        RefreshMaterializedTableRequest request = new RefreshMaterializedTableRequest();
        request.setIsPeriodic(true);
        request.setScheduleTime("2024-01-01 00:00:00");

        // Execute refresh
        String operationHandle =
                client.refreshMaterializedTable(sessionHandle, "mt_cat.mydb.continuous_users_shops", request);
        assertNotNull(operationHandle);

        FetchResultResponseBody fetchResultResponseBody = client.waitForOperationResult(sessionHandle, operationHandle);

        String jobId = fetchResultResponseBody.getResult().get(0).getValues().get(0);
        JobStatus jobStatus = client.describeJob(sessionHandle, jobId);
        while (jobStatus != JobStatus.FINISHED) {
            Thread.sleep(1000);
            jobStatus = client.describeJob(sessionHandle, jobId);
        }
        assertNotNull(fetchResultResponseBody);
    }

    @Test
    void testGetJobStatus() throws Exception {
        // First open a session
        String sessionHandle = client.openSession(new HashMap<>());

        String operationHandle = client.executeStatement(sessionHandle, "SELECT 1");

        FetchResultResponseBody fetchResultResponseBody = client.waitForOperationResult(sessionHandle, operationHandle);
        // Get job status
        JobStatus status = client.describeJob(sessionHandle, fetchResultResponseBody.getJobId());
        assertNotNull(status);
    }
}
