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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CuratorZookeeperClientTest {
    private static ZKServer zkServer;

    @Before
    public void before() throws IOException {
        new Thread(() -> {
            if (zkServer == null) {
                zkServer = new ZKServer();
            }
            zkServer.startLocalZkServer(2185);
        }).start();
    }

    @After
    public void after() {
        if (zkServer != null) {
            zkServer.stop();
        }
    }

    @Test
    public void testAfterPropertiesSet() throws Exception {
        TimeUnit.SECONDS.sleep(10);
        CuratorZookeeperClient zookeeperClient = new CuratorZookeeperClient();
        ZookeeperConfig zookeeperConfig = new ZookeeperConfig();
        zookeeperConfig.setServerList("127.0.0.1:2185");
        zookeeperConfig.setBaseSleepTimeMs(100);
        zookeeperConfig.setMaxSleepMs(30000);
        zookeeperConfig.setMaxRetries(10);
        zookeeperConfig.setSessionTimeoutMs(60000);
        zookeeperConfig.setConnectionTimeoutMs(30000);
        zookeeperConfig.setDigest(" ");
        zookeeperConfig.setDsRoot("/dolphinscheduler");
        zookeeperConfig.setMaxWaitTime(30000);
        zookeeperClient.setZookeeperConfig(zookeeperConfig);
        zookeeperClient.afterPropertiesSet();

        Assert.assertNotNull(zookeeperClient.getZkClient());
    }
}