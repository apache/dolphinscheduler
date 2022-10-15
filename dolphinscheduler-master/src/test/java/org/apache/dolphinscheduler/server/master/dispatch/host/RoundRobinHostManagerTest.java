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

package org.apache.dolphinscheduler.server.master.dispatch.host;

import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutionContextTestUtils;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Sets;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * round robin host manager test
 */
@ExtendWith(MockitoExtension.class)
public class RoundRobinHostManagerTest {

    @Mock
    private ServerNodeManager serverNodeManager;

    @InjectMocks
    RoundRobinHostManager roundRobinHostManager;

    @Test
    public void testSelectWithEmptyResult() {
        Mockito.when(serverNodeManager.getWorkerGroupNodes("default")).thenReturn(null);
        ExecutionContext context = ExecutionContextTestUtils.getExecutionContext(10000);
        Host emptyHost = roundRobinHostManager.select(context);
        Assertions.assertTrue(Strings.isNullOrEmpty(emptyHost.getAddress()));
    }

    @Test
    public void testSelectWithResult() {
        Mockito.when(serverNodeManager.getWorkerGroupNodes("default")).thenReturn(Sets.newHashSet("192.168.1.1:22"));
        Mockito.when(serverNodeManager.getWorkerNodeInfo("192.168.1.1:22")).thenReturn(new WorkerHeartBeat());
        ExecutionContext context = ExecutionContextTestUtils.getExecutionContext(10000);
        Host host = roundRobinHostManager.select(context);
        Assertions.assertFalse(Strings.isNullOrEmpty(host.getAddress()));
        Assertions.assertTrue(host.getAddress().equalsIgnoreCase("192.168.1.1:22"));
    }
}
