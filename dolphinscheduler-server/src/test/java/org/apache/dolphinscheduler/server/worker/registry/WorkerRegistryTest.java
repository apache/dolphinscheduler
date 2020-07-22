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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import org.apache.curator.framework.imps.CuratorFrameworkImpl;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * worker registry test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class WorkerRegistryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerRegistryTest.class);

    private static final String TEST_WORKER_GROUP = "test";

    @InjectMocks
    private WorkerRegistry workerRegistry;

    @Mock
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Mock
    private ZookeeperCachedOperator zookeeperCachedOperator;

    @Mock
    private CuratorFrameworkImpl zkClient;

    @Mock
    private WorkerConfig workerConfig;

    @Before
    public void before() {
        Set<String> workerGroups = Sets.newHashSet(DEFAULT_WORKER_GROUP, TEST_WORKER_GROUP);
        Mockito.when(workerConfig.getWorkerGroups()).thenReturn(workerGroups);

        Mockito.when(zookeeperRegistryCenter.getWorkerPath()).thenReturn("/dolphinscheduler/nodes/worker");
        Mockito.when(zookeeperRegistryCenter.getZookeeperCachedOperator()).thenReturn(zookeeperCachedOperator);
        Mockito.when(zookeeperRegistryCenter.getZookeeperCachedOperator().getZkClient()).thenReturn(zkClient);
        Mockito.when(zookeeperRegistryCenter.getZookeeperCachedOperator().getZkClient().getConnectionStateListenable()).thenReturn(
                new Listenable<ConnectionStateListener>() {
                    @Override
                    public void addListener(ConnectionStateListener connectionStateListener) {
                        LOGGER.info("add listener");
                    }

                    @Override
                    public void addListener(ConnectionStateListener connectionStateListener, Executor executor) {
                        LOGGER.info("add listener executor");
                    }

                    @Override
                    public void removeListener(ConnectionStateListener connectionStateListener) {
                        LOGGER.info("remove listener");
                    }
                });

        Mockito.when(workerConfig.getWorkerHeartbeatInterval()).thenReturn(10);

        Mockito.when(workerConfig.getWorkerReservedMemory()).thenReturn(1.1);

        Mockito.when(workerConfig.getWorkerMaxCpuloadAvg()).thenReturn(1);
    }

    @Test
    public void testRegistry() {

        workerRegistry.init();

        workerRegistry.registry();

        String workerPath = zookeeperRegistryCenter.getWorkerPath();

        int i = 0;
        for (String workerGroup : workerConfig.getWorkerGroups()) {
            String workerZkPath = workerPath + "/" + workerGroup.trim() + "/" + (OSUtils.getHost() + ":" + workerConfig.getListenPort());
            String heartbeat = zookeeperRegistryCenter.getZookeeperCachedOperator().get(workerZkPath);
            if (0 == i) {
                Assert.assertTrue(workerZkPath.startsWith("/dolphinscheduler/nodes/worker/test/"));
            } else {
                Assert.assertTrue(workerZkPath.startsWith("/dolphinscheduler/nodes/worker/default/"));
            }
            i++;
        }

        workerRegistry.unRegistry();

        workerConfig.getWorkerGroups().add(StringUtils.EMPTY);
        workerRegistry.init();
        workerRegistry.registry();

        workerRegistry.unRegistry();

        // testEmptyWorkerGroupsRegistry
        workerConfig.getWorkerGroups().remove(StringUtils.EMPTY);
        workerConfig.getWorkerGroups().remove(TEST_WORKER_GROUP);
        workerConfig.getWorkerGroups().remove(DEFAULT_WORKER_GROUP);
        workerRegistry.init();
        workerRegistry.registry();

        List<String> testWorkerGroupPathZkChildren = zookeeperRegistryCenter.getChildrenKeys(workerPath + "/" + TEST_WORKER_GROUP);
        List<String> defaultWorkerGroupPathZkChildren = zookeeperRegistryCenter.getChildrenKeys(workerPath + "/" + DEFAULT_WORKER_GROUP);

        Assert.assertEquals(0, testWorkerGroupPathZkChildren.size());
        Assert.assertEquals(0, defaultWorkerGroupPathZkChildren.size());
    }

    @Test
    public void testUnRegistry() {
        workerRegistry.init();
        workerRegistry.registry();

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

        workerRegistry.unRegistry();
    }
}
