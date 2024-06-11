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

package org.apache.dolphinscheduler.extract.base.metrics;

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.common.utils.NetUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class RpcMetricsTest {

    @BeforeEach
    public void setup() {
        Metrics.globalRegistry.clear();
        Metrics.addRegistry(new SimpleMeterRegistry());
    }

    @Test
    void testRecordClientSyncRequestException() {
        assertThat(Metrics.globalRegistry.find("ds.rpc.client.sync.request.exception.count").counter()).isNull();

        String clientHost = NetUtils.getHost();
        String serverHost = NetUtils.getHost();

        RpcMetrics.recordClientSyncRequestException(
                new IllegalArgumentException("id is null"), "getById", clientHost, serverHost);
        RpcMetrics.recordClientSyncRequestException(
                new IllegalArgumentException("name is null"), "getByName", clientHost, serverHost);
        RpcMetrics.recordClientSyncRequestException(
                new IllegalArgumentException("age is null"), "getByAge", clientHost, serverHost);
        RpcMetrics.recordClientSyncRequestException(new UnsupportedOperationException("update id is not supported"),
                "updateById", clientHost, serverHost);
        assertThat(Metrics.globalRegistry.find("ds.rpc.client.sync.request.exception.count").counter()).isNotNull();
    }

    @Test
    void testRecordRpcRequestDuration() {
        assertThat(Metrics.globalRegistry.find("ds.rpc.client.sync.request.duration.time").timer()).isNull();

        String clientHost = NetUtils.getHost();
        String serverHost = NetUtils.getHost();

        RpcMetrics.recordClientSyncRequestDuration("getById", 100, clientHost, serverHost);
        RpcMetrics.recordClientSyncRequestDuration("getByName", 200, clientHost, serverHost);
        RpcMetrics.recordClientSyncRequestDuration("getByAge", 300, clientHost, serverHost);
        RpcMetrics.recordClientSyncRequestDuration("updateById", 400, clientHost, serverHost);
        assertThat(Metrics.globalRegistry.find("ds.rpc.client.sync.request.duration.time").timer()).isNotNull();
    }

}
