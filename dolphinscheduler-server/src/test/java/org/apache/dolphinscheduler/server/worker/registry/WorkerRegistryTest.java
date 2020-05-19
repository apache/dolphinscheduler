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

import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.remote.utils.Constants;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;


import static org.apache.dolphinscheduler.common.Constants.HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH;
/**
 * worker registry test
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes={SpringZKServer.class, WorkerRegistry.class,ZookeeperRegistryCenter.class, WorkerConfig.class, ZookeeperCachedOperator.class, ZookeeperConfig.class})

public class WorkerRegistryTest {

    @Autowired
    private WorkerRegistry workerRegistry;

    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Autowired
    private WorkerConfig workerConfig;

    @Test
    public void testRegistry() throws InterruptedException {
        workerRegistry.registry();
        String workerPath = zookeeperRegistryCenter.getWorkerPath();
        Assert.assertEquals(DEFAULT_WORKER_GROUP, workerConfig.getWorkerGroup().trim());
        String instancePath = workerPath + "/" + workerConfig.getWorkerGroup().trim() + "/" + (OSUtils.getHost() + ":" + workerConfig.getListenPort());
        TimeUnit.SECONDS.sleep(workerConfig.getWorkerHeartbeatInterval() + 2); //wait heartbeat info write into zk node
        String heartbeat = zookeeperRegistryCenter.getZookeeperCachedOperator().get(instancePath);
        Assert.assertEquals(HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH, heartbeat.split(",").length);
    }

    @Test
    public void testUnRegistry() throws InterruptedException {
        workerRegistry.registry();
        TimeUnit.SECONDS.sleep(workerConfig.getWorkerHeartbeatInterval() + 2); //wait heartbeat info write into zk node
        workerRegistry.unRegistry();
        String workerPath = zookeeperRegistryCenter.getWorkerPath();
        String workerGroupPath = workerPath + "/" + workerConfig.getWorkerGroup().trim();
        List<String> childrenKeys = zookeeperRegistryCenter.getZookeeperCachedOperator().getChildrenKeys(workerGroupPath);
        Assert.assertTrue(childrenKeys.isEmpty());
    }
}
