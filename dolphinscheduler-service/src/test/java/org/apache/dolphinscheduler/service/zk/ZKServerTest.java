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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ZKServerTest {
    private static final Logger log = LoggerFactory.getLogger(ZKServerTest.class);

    @Test
    public void testRunWithDefaultPort() {
        AtomicReference<ZKServer> zkServer = new AtomicReference<>();
        new Thread(() -> {
            zkServer.set(new ZKServer());
            zkServer.get().start();
        }).start();
        try {
            TimeUnit.SECONDS.sleep(5);
            Assert.assertEquals(true, zkServer.get().isStarted());
        } catch (InterruptedException e) {
            log.error("Thread interrupted", e);
        }
        zkServer.get().stop();
    }

    @Test
    public void testRunWithCustomPort() {
        AtomicReference<ZKServer> zkServer = new AtomicReference<>();
        new Thread(() -> {
            zkServer.set(new ZKServer(2183, null));
            zkServer.get().start();
        }).start();
        try {
            TimeUnit.SECONDS.sleep(5);
            Assert.assertEquals(true, zkServer.get().isStarted());
        } catch (InterruptedException e) {
            log.error("Thread interrupted", e);
        }
        zkServer.get().stop();
    }
}