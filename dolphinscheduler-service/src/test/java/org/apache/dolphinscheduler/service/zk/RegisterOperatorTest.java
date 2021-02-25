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

import org.apache.curator.framework.CuratorFramework;

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

    protected CuratorFramework zkClient;


    private static final String DS_ROOT = "/dolphinscheduler";

    public void init() {
        try {
            registerOperator.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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
        Mockito.when(zookeeperConfig.getDsRoot()).thenReturn("/dolphinscheduler");
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
    public void getDeadZNodeParentPath() throws Exception {
        testAfterPropertiesSet();
        String path = registerOperator.getDeadZNodeParentPath();
        Assert.assertEquals("/dolphinscheduler/dead-servers", path);
    }

}