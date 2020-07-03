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

package org.apache.dolphinscheduler.server.worker.registry;

import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.common.Constants.HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.zk.SpringZKServer;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * worker registry test
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes={SpringZKServer.class, WorkerRegistry.class,ZookeeperRegistryCenter.class, WorkerConfig.class, ZookeeperCachedOperator.class, ZookeeperConfig.class})

public class WorkerRegistryTest {

    private static final String TEST_WORKER_GROUP = "test";

    @Autowired
    private WorkerRegistry workerRegistry;

    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Autowired
    private WorkerConfig workerConfig;

    @Test
    public void testRegistry() throws InterruptedException {
        workerConfig.getWorkerGroups().add(TEST_WORKER_GROUP);
        workerRegistry.registry();
        String workerPath = zookeeperRegistryCenter.getWorkerPath();

        int i = 0;
        for (String workerGroup : workerConfig.getWorkerGroups()) {
            if (0 == i) {
                Assert.assertEquals(DEFAULT_WORKER_GROUP, workerGroup.trim());
            } else {
                Assert.assertEquals(TEST_WORKER_GROUP, workerGroup.trim());
            }
            String instancePath = workerPath + "/" + workerGroup.trim() + "/" + (NetUtils.getHost() + ":" + workerConfig.getListenPort());
            TimeUnit.SECONDS.sleep(workerConfig.getWorkerHeartbeatInterval() + 2); // wait heartbeat info write into zk node
            String heartbeat = zookeeperRegistryCenter.getZookeeperCachedOperator().get(instancePath);
            Assert.assertEquals(HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH, heartbeat.split(",").length);
            i++;
        }

        workerRegistry.unRegistry();

        workerConfig.getWorkerGroups().add(StringUtils.EMPTY);
        workerRegistry.init();
        workerRegistry.registry();
        TimeUnit.SECONDS.sleep(workerConfig.getWorkerHeartbeatInterval() + 2); // wait heartbeat info write into zk node

        workerRegistry.unRegistry();

        // testEmptyWorkerGroupsRegistry
        workerConfig.getWorkerGroups().remove(StringUtils.EMPTY);
        workerConfig.getWorkerGroups().remove(TEST_WORKER_GROUP);
        workerConfig.getWorkerGroups().remove(DEFAULT_WORKER_GROUP);
        workerRegistry.init();
        workerRegistry.registry();

        List<String> testWorkerGroupPathZkChildren = zookeeperRegistryCenter.getChildrenKeys(workerPath + "/" + TEST_WORKER_GROUP);
        List<String> defaultWorkerGroupPathZkChildren = zookeeperRegistryCenter.getChildrenKeys(workerPath + "/" + DEFAULT_WORKER_GROUP);

        TimeUnit.SECONDS.sleep(workerConfig.getWorkerHeartbeatInterval() + 2); // wait heartbeat info write into zk node
        Assert.assertEquals(0, testWorkerGroupPathZkChildren.size());
        Assert.assertEquals(0, defaultWorkerGroupPathZkChildren.size());
    }

    @Test
    public void testUnRegistry() throws InterruptedException {
        workerConfig.getWorkerGroups().add(TEST_WORKER_GROUP);
        workerRegistry.registry();
        TimeUnit.SECONDS.sleep(workerConfig.getWorkerHeartbeatInterval() + 2); // wait heartbeat info write into zk node
        workerRegistry.unRegistry();
        String workerPath = zookeeperRegistryCenter.getWorkerPath();

        for (String workerGroup : workerConfig.getWorkerGroups()) {
            String workerGroupPath = workerPath + "/" + workerGroup.trim();
            List<String> childrenKeys = zookeeperRegistryCenter.getZookeeperCachedOperator().getChildrenKeys(workerGroupPath);
            Assert.assertTrue(childrenKeys.isEmpty());
        }

        // testEmptyWorkerGroupsUnRegistry
        workerConfig.getWorkerGroups().remove(TEST_WORKER_GROUP);
        workerConfig.getWorkerGroups().remove(DEFAULT_WORKER_GROUP);
        workerRegistry.init();
        workerRegistry.registry();

        List<String> testWorkerGroupPathZkChildren = zookeeperRegistryCenter.getChildrenKeys(workerPath + "/" + TEST_WORKER_GROUP);
        List<String> defaultWorkerGroupPathZkChildren = zookeeperRegistryCenter.getChildrenKeys(workerPath + "/" + DEFAULT_WORKER_GROUP);

        TimeUnit.SECONDS.sleep(workerConfig.getWorkerHeartbeatInterval() + 2); // wait heartbeat info write into zk node
        workerRegistry.unRegistry();

        Assert.assertEquals(0, testWorkerGroupPathZkChildren.size());
        Assert.assertEquals(0, defaultWorkerGroupPathZkChildren.size());
    }
}
