package org.apache.dolphinscheduler.server.master.registry;

import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.registry.api.Event;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class ServerNodeManagerTest {


    @Mock
    RegistryClient registryClient;

    @Mock
    AlertDao alertDao;

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
        Assert.assertTrue(workerNodeInfo.containsKey("worker-node-1"));
        Assert.assertTrue(workerNodeInfo.containsKey("worker-node-2"));

        // receive remove event when worker-node-1 server stop
        ServerNodeManager.WorkerDataListener workerDataListener = serverNodeManager.new WorkerDataListener();
        Event event = new Event("", "/nodes/worker/worker-node-1", "", Event.Type.REMOVE);
        workerDataListener.notify(event);

        // check worker-node-1 not exist in cache
        workerNodeInfo = serverNodeManager.getWorkerNodeInfo();
        Assert.assertFalse(workerNodeInfo.containsKey("worker-node-1"));
        Assert.assertTrue(workerNodeInfo.containsKey("worker-node-2"));

        // worker-node-1 restart, getServerMaps(RegistryNodeType.WORKER) method return two worker
        updateWorkerNodes.invoke(serverNodeManager);

        //check cache
        workerNodeInfo = serverNodeManager.getWorkerNodeInfo();
        Assert.assertTrue(workerNodeInfo.containsKey("worker-node-1"));
        Assert.assertTrue(workerNodeInfo.containsKey("worker-node-2"));

    }




}