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

import com.google.common.collect.Sets;

import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.apache.dolphinscheduler.server.utils.ExecutionContextTestUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * round robin host manager test
 */
@RunWith(MockitoJUnitRunner.class)
public class RoundRobinHostManagerTest {


    @Mock
    private ZookeeperNodeManager zookeeperNodeManager;

    @InjectMocks
    RoundRobinHostManager roundRobinHostManager;

    @Test
    public void testSelectWithEmptyResult() {
        Mockito.when(zookeeperNodeManager.getWorkerGroupNodes("default")).thenReturn(null);
        ExecutionContext context = ExecutionContextTestUtils.getExecutionContext(10000);
        Host emptyHost = roundRobinHostManager.select(context);
        Assert.assertTrue(StringUtils.isEmpty(emptyHost.getAddress()));
    }

    @Test
    public void testSelectWithResult() {
        Mockito.when(zookeeperNodeManager.getWorkerGroupNodes("default")).thenReturn(Sets.newHashSet("192.168.1.1:22:100"));
        ExecutionContext context = ExecutionContextTestUtils.getExecutionContext(10000);
        Host host = roundRobinHostManager.select(context);
        Assert.assertTrue(StringUtils.isNotEmpty(host.getAddress()));
        Assert.assertTrue(host.getAddress().equalsIgnoreCase("192.168.1.1:22"));
    }
}
