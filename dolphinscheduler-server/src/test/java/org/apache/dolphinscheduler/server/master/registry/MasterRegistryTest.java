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

import static org.apache.dolphinscheduler.common.Constants.HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH;

import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.remote.utils.Constants;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.zk.CuratorZookeeperClient;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * master registry test
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SpringZKServer.class, MasterRegistry.class, ZookeeperRegistryCenter.class,
        MasterConfig.class, ZookeeperCachedOperator.class, ZookeeperConfig.class, CuratorZookeeperClient.class})
public class MasterRegistryTest {

    @Autowired
    private MasterRegistry masterRegistry;

    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Autowired
    private MasterConfig masterConfig;

    @Test
    public void testRegistry() throws InterruptedException {
        masterRegistry.registry();
        String masterPath = zookeeperRegistryCenter.getMasterPath();
        TimeUnit.SECONDS.sleep(masterConfig.getMasterHeartbeatInterval() + 2); //wait heartbeat info write into zk node
        String masterNodePath = masterPath + "/" + (NetUtils.getAddr(Constants.LOCAL_ADDRESS, masterConfig.getListenPort()));
        String heartbeat = zookeeperRegistryCenter.getRegisterOperator().get(masterNodePath);
        Assert.assertEquals(HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH, heartbeat.split(",").length);
        masterRegistry.unRegistry();
    }

    @Test
    public void testUnRegistry() throws InterruptedException {
        masterRegistry.init();
        masterRegistry.registry();
        TimeUnit.SECONDS.sleep(masterConfig.getMasterHeartbeatInterval() + 2); //wait heartbeat info write into zk node
        masterRegistry.unRegistry();
        String masterPath = zookeeperRegistryCenter.getMasterPath();
        List<String> childrenKeys = zookeeperRegistryCenter.getRegisterOperator().getChildrenKeys(masterPath);
        Assert.assertTrue(childrenKeys.isEmpty());
    }
}
