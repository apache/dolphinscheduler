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

package org.apache.dolphinscheduler.server.master.registry;

import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.service.alert.ListenerEventAlertManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ServerNodeManagerTest {

    @Mock
    RegistryClient registryClient;

    @Mock
    AlertDao alertDao;

    @Mock
    ListenerEventAlertManager listenerEventAlertManager;

    @InjectMocks
    ServerNodeManager serverNodeManager;

    @Test
    public void updateWorkerNodesTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        MockitoAnnotations.initMocks(this);
        HashMap<String, String> workerNodeMaps = new HashMap<>();
        workerNodeMaps.put("worker-node-1", JSONUtils.toJsonString(new WorkerHeartBeat()));
        workerNodeMaps.put("worker-node-2", JSONUtils.toJsonString(new WorkerHeartBeat()));

        Mockito.when(registryClient.getServerMaps(Mockito.any())).thenReturn(workerNodeMaps);
        Mockito.when(registryClient.isWorkerPath(Mockito.anyString())).thenReturn(true);

        // two worker server running (worker-node-1, worker-node-2)
        Method updateWorkerNodes = serverNodeManager.getClass().getDeclaredMethod("updateWorkerNodes");
        updateWorkerNodes.setAccessible(true);
        updateWorkerNodes.invoke(serverNodeManager);

        Map<String, WorkerHeartBeat> workerNodeInfo = serverNodeManager.getWorkerNodeInfo();
        Assertions.assertTrue(workerNodeInfo.containsKey("worker-node-1"));
        Assertions.assertTrue(workerNodeInfo.containsKey("worker-node-2"));

        // receive remove event when worker-node-1 server stop
        ServerNodeManager.WorkerDataListener workerDataListener = serverNodeManager.new WorkerDataListener();
        Event event = new Event("", "/nodes/worker/worker-node-1", "", Event.Type.REMOVE);
        workerDataListener.notify(event);

        // check worker-node-1 not exist in cache
        workerNodeInfo = serverNodeManager.getWorkerNodeInfo();
        Assertions.assertFalse(workerNodeInfo.containsKey("worker-node-1"));
        Assertions.assertTrue(workerNodeInfo.containsKey("worker-node-2"));

        // worker-node-1 restart, getServerMaps(RegistryNodeType.WORKER) method return two worker
        updateWorkerNodes.invoke(serverNodeManager);

        // check cache
        workerNodeInfo = serverNodeManager.getWorkerNodeInfo();
        Assertions.assertTrue(workerNodeInfo.containsKey("worker-node-1"));
        Assertions.assertTrue(workerNodeInfo.containsKey("worker-node-2"));

    }

}
