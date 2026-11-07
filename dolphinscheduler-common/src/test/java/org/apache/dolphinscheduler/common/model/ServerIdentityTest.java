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

package org.apache.dolphinscheduler.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.common.enums.ServerStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.junit.jupiter.api.Test;

class ServerIdentityTest {

    @Test
    void containsOnlyStableProcessIdentity() {
        WorkerHeartBeat metadata = WorkerHeartBeat.builder().host("localhost").port(1234).processId(10).startupTime(1)
                .reportTime(100).serverStatus(ServerStatus.NORMAL).cpuUsage(0.5).workerGroup("analytics").build();
        ServerIdentity identity = ServerIdentity.fromHeartBeat(metadata);
        String json = JSONUtils.toJsonString(identity);
        assertFalse(json.contains("cpuUsage"));
        assertFalse(json.contains("serverStatus"));
        assertFalse(json.contains("workerGroup"));
        assertFalse(json.contains("reportTime"));
        assertEquals(identity, JSONUtils.parseObject(json, ServerIdentity.class));
        assertTrue(identity.matches(metadata));
        metadata.setStartupTime(2);
        assertFalse(identity.matches(metadata));
    }
}
