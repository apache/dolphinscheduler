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
package org.apache.dolphinscheduler.server.registry;


import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistry;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistry;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.Set;

/**
 * zookeeper node manager test
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DependencyConfig.class, SpringZKServer.class, MasterRegistry.class,WorkerRegistry.class,
        ZookeeperRegistryCenter.class, MasterConfig.class, WorkerConfig.class,
        ZookeeperCachedOperator.class, ZookeeperConfig.class, ZookeeperNodeManager.class})
public class ZookeeperNodeManagerTest {

    @Autowired
    private ZookeeperNodeManager zookeeperNodeManager;

    @Autowired
    private MasterRegistry masterRegistry;

    @Autowired
    private WorkerRegistry workerRegistry;

    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Autowired
    private WorkerConfig workerConfig;

    @Autowired
    private MasterConfig masterConfig;

    @Test
    public void testGetMasterNodes(){
        masterRegistry.registry();
        try {
            //let the zookeeperNodeManager catch the registry event
            Thread.sleep(2000);
        } catch (InterruptedException ignore) {
        }
        Set<String> masterNodes = zookeeperNodeManager.getMasterNodes();
        Assert.assertTrue(CollectionUtils.isNotEmpty(masterNodes));
        Assert.assertEquals(1, masterNodes.size());
        Assert.assertEquals(OSUtils.getHost() + ":" + masterConfig.getListenPort(), masterNodes.iterator().next());
    }

    @Test
    public void testGetWorkerGroupNodes(){
        workerRegistry.registry();
        try {
            //let the zookeeperNodeManager catch the registry event
            Thread.sleep(2000);
        } catch (InterruptedException ignore) {
        }
        Map<String, Set<String>> workerGroupNodes = zookeeperNodeManager.getWorkerGroupNodes();
        Assert.assertEquals(1, workerGroupNodes.size());
        Assert.assertEquals("default".trim(), workerGroupNodes.keySet().iterator().next());
    }

    @Test
    public void testGetWorkerGroupNodesWithParam(){
        workerRegistry.registry();
        try {
            //let the zookeeperNodeManager catch the registry event
            Thread.sleep(3000);
        } catch (InterruptedException ignore) {
        }
        Map<String, Set<String>> workerGroupNodes = zookeeperNodeManager.getWorkerGroupNodes();
        Set<String> workerNodes = zookeeperNodeManager.getWorkerGroupNodes("default");
        Assert.assertTrue(CollectionUtils.isNotEmpty(workerNodes));
        Assert.assertEquals(1, workerNodes.size());
        Assert.assertEquals(OSUtils.getHost() + ":" + workerConfig.getListenPort(), workerNodes.iterator().next());
    }
}
