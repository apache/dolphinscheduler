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

package org.apache.dolphinscheduler.server.master.zk;

import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.datasource.SpringConnectionFactory;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistry;
import org.apache.dolphinscheduler.server.master.registry.ServerNodeManager;
import org.apache.dolphinscheduler.server.registry.DependencyConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.zk.RegisterOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * zookeeper master client test
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DependencyConfig.class, SpringZKServer.class, MasterRegistry.class,
        ZookeeperRegistryCenter.class, MasterConfig.class, WorkerConfig.class, SpringConnectionFactory.class,
        ZookeeperCachedOperator.class, ZookeeperConfig.class, ServerNodeManager.class,
        ZKMasterClient.class, RegisterOperator.class})
public class ZKMasterClientTest {

    @Autowired
    private ZKMasterClient zkMasterClient;

    @Autowired
    private ServerNodeManager serverNodeManager;

    @Autowired
    private MasterConfig masterConfig;

    @Test
    public void testZKMasterClient() {
        zkMasterClient.start();
        try {
            //let the serverNodeManager catch the registry event
            Thread.sleep(2000);
        } catch (InterruptedException ignore) {
            //ignore
        }
        Set<String> masterNodes = serverNodeManager.getMasterNodes();
        Assert.assertTrue(CollectionUtils.isNotEmpty(masterNodes));
        Assert.assertEquals(1, masterNodes.size());
        Assert.assertEquals(NetUtils.getAddr(masterConfig.getListenPort()), masterNodes.iterator().next());
        zkMasterClient.close();
    }

}
