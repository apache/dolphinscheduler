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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FlinkSqlGatewayRestClientTest {

    @Test
    public void toRestBaseUrlShouldTranslateJdbcUrl() {
        Assertions.assertEquals("http://cmtt-dict-57:8083",
                FlinkSqlGatewayRestClient.toRestBaseUrl("jdbc:flink://cmtt-dict-57:8083"));
        Assertions.assertEquals("http://host:8083",
                FlinkSqlGatewayRestClient.toRestBaseUrl("jdbc:flink://host:8083/"));
        Assertions.assertEquals("https://host:9443",
                FlinkSqlGatewayRestClient.toRestBaseUrl("https://host:9443"));
        Assertions.assertThrows(Exception.class, () -> FlinkSqlGatewayRestClient.toRestBaseUrl(null));
        Assertions.assertThrows(Exception.class, () -> FlinkSqlGatewayRestClient.toRestBaseUrl(" "));
    }

    @Test
    public void isRestVersionNegotiationFailureShouldMatchDriverEnumAbort() {
        IllegalArgumentException enumAbort = new IllegalArgumentException(
                "No enum constant org.apache.flink.table.gateway.rest.util.SqlGatewayRestAPIVersion.V4");
        RuntimeException wrapped = new RuntimeException("outer", enumAbort);
        Assertions.assertTrue(FlinkSqlGatewayExecutor.isRestVersionNegotiationFailure(wrapped));
        Assertions.assertTrue(FlinkSqlGatewayExecutor.isRestVersionNegotiationFailure(enumAbort));
        Assertions.assertFalse(FlinkSqlGatewayExecutor.isRestVersionNegotiationFailure(
                new IllegalArgumentException("No enum constant something.else.V4")));
        Assertions.assertFalse(FlinkSqlGatewayExecutor.isRestVersionNegotiationFailure(
                new RuntimeException("connection refused")));
        Assertions.assertFalse(FlinkSqlGatewayExecutor.isRestVersionNegotiationFailure(null));
    }

    @Test
    public void interpretRestResultShouldMirrorJdbcSemantics() {
        // status column decides via the shared sequence interpretation
        Assertions.assertEquals(FlinkSqlGatewayExecutor.SubmitOutcome.SUCCESS,
                FlinkSqlGatewayExecutor.interpretRestResult(
                        Arrays.asList("status"),
                        Arrays.asList(Arrays.asList("RUNNING"), Arrays.asList("FINISHED"))));
        Assertions.assertEquals(FlinkSqlGatewayExecutor.SubmitOutcome.FAILURE,
                FlinkSqlGatewayExecutor.interpretRestResult(
                        Arrays.asList("status"), Arrays.asList(Arrays.asList("FAILED"))));
        Assertions.assertEquals(FlinkSqlGatewayExecutor.SubmitOutcome.FAILURE,
                FlinkSqlGatewayExecutor.interpretRestResult(
                        Arrays.asList("status"), Arrays.asList(Arrays.asList("CANCELED"))));
        // lone job-id column is a finished success in dml-sync mode (syncEnabled=true)
        Assertions.assertEquals(FlinkSqlGatewayExecutor.SubmitOutcome.SUCCESS,
                FlinkSqlGatewayExecutor.interpretRestResult(
                        Arrays.asList("job id"), Arrays.asList(Arrays.asList("abc123"))));
        Assertions.assertEquals(FlinkSqlGatewayExecutor.SubmitOutcome.NOT_FINISHED,
                FlinkSqlGatewayExecutor.interpretRestResult(
                        Collections.singletonList("job id"),
                        Collections.singletonList(Collections.singletonList("abc123"))));
        // no result columns: statement reached its terminal state
        Assertions.assertEquals(FlinkSqlGatewayExecutor.SubmitOutcome.SUCCESS,
                FlinkSqlGatewayExecutor.interpretRestResult(new ArrayList<>(), new ArrayList<>()));
        Assertions.assertEquals(FlinkSqlGatewayExecutor.SubmitOutcome.SUCCESS,
                FlinkSqlGatewayExecutor.interpretRestResult(null, null));
    }
}
