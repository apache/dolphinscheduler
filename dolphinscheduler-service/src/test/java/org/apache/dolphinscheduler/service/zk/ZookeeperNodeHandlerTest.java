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

package org.apache.dolphinscheduler.service.zk;

import org.apache.dolphinscheduler.common.model.WorkerZkNode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *  zookeeper node handler test
 */
public class ZookeeperNodeHandlerTest {

    private String address;
    private String weight;
    private long workerStartTime;
    private String workerZkNodeStr;

    @Before
    public void before() {
        address = "127.0.0.1:1234";
        weight = "100";
        workerStartTime = System.currentTimeMillis();
        workerZkNodeStr = address + ":" + weight + ":" + workerStartTime;
    }

    @Test
    public void testGenerateWorkerZkNodeName() {
        String workerZkNodeName = ZookeeperNodeHandler.generateWorkerZkNodeName(address, weight, workerStartTime);
        Assert.assertEquals(workerZkNodeStr, workerZkNodeName);
    }

    @Test
    public void testGetWorkerZkNodeName() {
        WorkerZkNode workerZkNodeName = ZookeeperNodeHandler.getWorkerZkNodeName(workerZkNodeStr);
        Assert.assertNotNull(workerZkNodeName);
        workerZkNodeName = ZookeeperNodeHandler.getWorkerZkNodeName("");
        Assert.assertNull(workerZkNodeName);
    }

    @Test
    public void testGetWorkerAddress() {
        String workerAddress = ZookeeperNodeHandler.getWorkerAddress(workerZkNodeStr);
        Assert.assertEquals(address, workerAddress);
        workerAddress = ZookeeperNodeHandler.getWorkerAddress("");
        Assert.assertNull(workerAddress);
    }

    @Test
    public void testGetWorkerAddressAndWeight() {
        String workerAddressAndWeight = ZookeeperNodeHandler.getWorkerAddressAndWeight(workerZkNodeStr);
        Assert.assertEquals(address + ":" + weight, workerAddressAndWeight);
    }

    @Test
    public void testGetWorkerStartTime() {
        String workerStartTime = ZookeeperNodeHandler.getWorkerStartTime(ZookeeperNodeHandler.getWorkerZkNodeName(workerZkNodeStr));
        Assert.assertEquals(workerStartTime + "", workerStartTime);
    }

}
