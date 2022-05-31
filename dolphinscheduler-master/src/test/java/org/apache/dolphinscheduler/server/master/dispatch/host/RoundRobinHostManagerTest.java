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

import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.ExecutionContextTestUtils;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;
import org.assertj.core.util.Strings;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Sets;

/**
 * round robin host manager test
 */
@RunWith(MockitoJUnitRunner.class)
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
        Assert.assertTrue(Strings.isNullOrEmpty(emptyHost.getAddress()));
    }

    @Test
    public void testSelectWithResult() {
        Mockito.when(serverNodeManager.getWorkerGroupNodes("default")).thenReturn(Sets.newHashSet("192.168.1.1:22"));
        ExecutionContext context = ExecutionContextTestUtils.getExecutionContext(10000);
        Host host = roundRobinHostManager.select(context);
        Assert.assertTrue(!Strings.isNullOrEmpty(host.getAddress()));
        Assert.assertTrue(host.getAddress().equalsIgnoreCase("192.168.1.1:22"));
    }
}
