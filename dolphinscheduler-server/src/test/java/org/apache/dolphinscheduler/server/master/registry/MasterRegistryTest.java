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

import org.apache.dolphinscheduler.remote.utils.Constants;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.dolphinscheduler.common.Constants.HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH;
/**
 * master registry test
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes={SpringZKServer.class, MasterRegistry.class,ZookeeperRegistryCenter.class, MasterConfig.class, ZookeeperCachedOperator.class, ZookeeperConfig.class})
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
        String masterNodePath = masterPath + "/" + (Constants.LOCAL_ADDRESS + ":" + masterConfig.getListenPort());
        String heartbeat = zookeeperRegistryCenter.getZookeeperCachedOperator().get(masterNodePath);
        Assert.assertEquals(HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH, heartbeat.split(",").length);
    }

    @Test
    public void testUnRegistry() throws InterruptedException {
        masterRegistry.registry();
        TimeUnit.SECONDS.sleep(masterConfig.getMasterHeartbeatInterval() + 2); //wait heartbeat info write into zk node
        masterRegistry.unRegistry();
        String masterPath = zookeeperRegistryCenter.getMasterPath();
        List<String> childrenKeys = zookeeperRegistryCenter.getZookeeperCachedOperator().getChildrenKeys(masterPath);
        Assert.assertTrue(childrenKeys.isEmpty());
    }
}
