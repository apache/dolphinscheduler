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

package org.apache.dolphinscheduler.service.registry;

import static org.apache.dolphinscheduler.common.Constants.ADD_OP;
import static org.apache.dolphinscheduler.common.Constants.DELETE_OP;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.spi.register.Registry;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RegistryClientTest {

    @InjectMocks
    private RegistryClient registryClient;

    @Mock
    private Registry registry;

    @Before
    public void before() {
        //  registry=mock(Registry.class);
    }

    @Test
    public void te() throws Exception {
        doNothing().when(registry).persist(Mockito.anyString(), Mockito.anyString());
        doNothing().when(registry).update(Mockito.anyString(), Mockito.anyString());
        given(registry.releaseLock(Mockito.anyString())).willReturn(true);
        given(registry.getChildren("/dead-servers")).willReturn(Arrays.asList("worker_127.0.0.1:8089"));
        registryClient.persist("/key", "");
        registryClient.update("/key", "");
        registryClient.releaseLock("/key");
        registryClient.getChildrenKeys("/key");
        registryClient.handleDeadServer(Sets.newHashSet("ma/127.0.0.1:8089"), NodeType.WORKER, DELETE_OP);
        registryClient.handleDeadServer(Sets.newHashSet("ma/127.0.0.1:8089"), NodeType.WORKER, ADD_OP);
        //registryClient.removeDeadServerByHost("127.0.0.1:8089","master");
        registryClient.handleDeadServer("ma/127.0.0.1:8089", NodeType.WORKER, DELETE_OP);
        registryClient.handleDeadServer("ma/127.0.0.1:8089", NodeType.WORKER, ADD_OP);
        registryClient.checkIsDeadServer("master/127.0.0.1","master");
        given(registry.getChildren("/nodes/worker")).willReturn(Arrays.asList("worker_127.0.0.1:8089"));
        given(registry.getChildren("/nodes/worker/worker_127.0.0.1:8089")).willReturn(Arrays.asList("default"));

        registryClient.checkNodeExists("127.0.0.1",NodeType.WORKER);

        registryClient.getServerList(NodeType.MASTER);

    }

}
