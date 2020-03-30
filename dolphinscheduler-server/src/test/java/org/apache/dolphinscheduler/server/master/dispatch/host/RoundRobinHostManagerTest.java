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
package org.apache.dolphinscheduler.server.master.dispatch.host;


import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.registry.DependencyConfig;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.utils.ExecutionContextTestUtils;
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
import org.springframework.test.context.junit4.SpringRunner;


/**
 * round robin host manager test
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes={DependencyConfig.class, SpringZKServer.class, WorkerRegistry.class, ZookeeperRegistryCenter.class, WorkerConfig.class,
        ZookeeperNodeManager.class, ZookeeperCachedOperator.class, ZookeeperConfig.class})
public class RoundRobinHostManagerTest {


    @Autowired
    private ZookeeperNodeManager zookeeperNodeManager;

    @Autowired
    private WorkerRegistry workerRegistry;

    @Autowired
    private WorkerConfig workerConfig;

    @Test
    public void testSelectWithEmptyResult(){
        RoundRobinHostManager roundRobinHostManager = new RoundRobinHostManager();
        roundRobinHostManager.setZookeeperNodeManager(zookeeperNodeManager);
        ExecutionContext context = ExecutionContextTestUtils.getExecutionContext(10000);
        Host emptyHost = roundRobinHostManager.select(context);
        Assert.assertTrue(StringUtils.isEmpty(emptyHost.getAddress()));
    }

    @Test
    public void testSelectWithResult(){
        workerRegistry.registry();
        RoundRobinHostManager roundRobinHostManager = new RoundRobinHostManager();
        roundRobinHostManager.setZookeeperNodeManager(zookeeperNodeManager);
        ExecutionContext context = ExecutionContextTestUtils.getExecutionContext(10000);
        Host host = roundRobinHostManager.select(context);
        Assert.assertTrue(StringUtils.isNotEmpty(host.getAddress()));
        Assert.assertTrue(host.getAddress().equalsIgnoreCase(OSUtils.getHost() + ":" + workerConfig.getListenPort()));
    }
}
