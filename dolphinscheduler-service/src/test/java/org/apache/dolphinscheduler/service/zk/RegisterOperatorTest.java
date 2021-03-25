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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * register operator test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class RegisterOperatorTest {

    private static ZKServer zkServer;

    @InjectMocks
    private RegisterOperator registerOperator;

    @Mock
    private ZookeeperConfig zookeeperConfig;

    private static final String DS_ROOT = "/dolphinscheduler";
    private static final String MASTER_NODE = "127.0.0.1:5678";

    @Before
    public void before() {
        new Thread(() -> {
            if (zkServer == null) {
                zkServer = new ZKServer();
            }
            zkServer.startLocalZkServer(2185);
        }).start();
    }

    @Test
    public void testAfterPropertiesSet() throws Exception {
        TimeUnit.SECONDS.sleep(10);
        Mockito.when(zookeeperConfig.getServerList()).thenReturn("127.0.0.1:2185");
        Mockito.when(zookeeperConfig.getBaseSleepTimeMs()).thenReturn(100);
        Mockito.when(zookeeperConfig.getMaxRetries()).thenReturn(10);
        Mockito.when(zookeeperConfig.getMaxSleepMs()).thenReturn(30000);
        Mockito.when(zookeeperConfig.getSessionTimeoutMs()).thenReturn(60000);
        Mockito.when(zookeeperConfig.getConnectionTimeoutMs()).thenReturn(30000);
        Mockito.when(zookeeperConfig.getDigest()).thenReturn("");
        Mockito.when(zookeeperConfig.getDsRoot()).thenReturn(DS_ROOT);
        Mockito.when(zookeeperConfig.getMaxWaitTime()).thenReturn(30000);

        registerOperator.afterPropertiesSet();
        Assert.assertNotNull(registerOperator.getZkClient());
    }

    @After
    public void after() {
        if (zkServer != null) {
            zkServer.stop();
        }
    }

    @Test
    public void testGetDeadZNodeParentPath() throws Exception {

        testAfterPropertiesSet();
        String path = registerOperator.getDeadZNodeParentPath();

        Assert.assertEquals(DS_ROOT + Constants.ZOOKEEPER_DOLPHINSCHEDULER_DEAD_SERVERS, path);
    }

    @Test
    public void testHandleDeadServer() throws Exception {
        testAfterPropertiesSet();
        registerOperator.handleDeadServer(MASTER_NODE, ZKNodeType.MASTER,Constants.ADD_ZK_OP);
        String path = registerOperator.getDeadZNodeParentPath();
        Assert.assertTrue(registerOperator.getChildrenKeys(path).contains(String.format("%s_%s",Constants.MASTER_TYPE,MASTER_NODE)));

    }

    @Test
    public void testRemoveDeadServerByHost() throws Exception {
        testAfterPropertiesSet();
        String path = registerOperator.getDeadZNodeParentPath();

        registerOperator.handleDeadServer(MASTER_NODE, ZKNodeType.MASTER,Constants.ADD_ZK_OP);
        Assert.assertTrue(registerOperator.getChildrenKeys(path).contains(String.format("%s_%s",Constants.MASTER_TYPE,MASTER_NODE)));

        registerOperator.removeDeadServerByHost(MASTER_NODE,Constants.MASTER_TYPE);
        Assert.assertFalse(registerOperator.getChildrenKeys(path).contains(String.format("%s_%s",Constants.MASTER_TYPE,MASTER_NODE)));
    }

    @Test
    public void testGetChildrenKeysWithNoNodeException() throws Exception {
        testAfterPropertiesSet();
        String path = registerOperator.getDeadZNodeParentPath();
        Assert.assertEquals(0, registerOperator.getChildrenKeys(path).size());
    }

    @Test
    public void testNoNodeException() throws Exception {
        testAfterPropertiesSet();
        String path = registerOperator.getDeadZNodeParentPath();
        registerOperator.persistEphemeral(path, "test");
        registerOperator.remove(path);
    }

}