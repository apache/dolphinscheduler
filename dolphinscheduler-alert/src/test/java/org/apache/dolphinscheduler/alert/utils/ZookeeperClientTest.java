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

package org.apache.dolphinscheduler.alert.utils;

import org.apache.dolphinscheduler.service.zk.ZKServer;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ZookeeperClientTest {

    private static ZKServer zkServer;

    private static boolean flag;

    @Before
    public void before() throws IOException {
        new Thread(() -> {
            if (zkServer == null) {
                zkServer = new ZKServer();
            }
            zkServer.startLocalZkServer(2181);
        }).start();
    }

    @Test
    public void testConcurrentOperation() {
        try {
            flag = false;
            ZookeeperClient.concurrentOperation(new ZookeeperClient.LockCallBall() {
                public void handle() {
                    flag = true;
                }
            });
            Assert.assertTrue(flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void after() {
        if (zkServer != null) {
            zkServer.stop();
        }
    }
}
