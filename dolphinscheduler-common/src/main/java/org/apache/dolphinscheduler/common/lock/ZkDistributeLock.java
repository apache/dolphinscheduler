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

package org.apache.dolphinscheduler.common.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundVersionable;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.recipes.locks.InterProcessMultiLock;
import org.apache.curator.retry.RetryNTimes;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZkDistributeLock implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ZkDistributeLock.class);

    private String serverAddress;
    private int retryTimes;
    private int retryBetween;
    private int acquire;
    private String rootPath;

    public void init(String rootPath, String serverAddress, int retryTimes, int retryBetween, int acquire) {
        this.rootPath = rootPath;
        this.serverAddress = serverAddress;
        this.retryTimes = retryTimes;
        this.retryBetween = retryBetween;
        this.acquire = acquire;
    }

    public void execute() {
        logger.debug("default execute");
    }

    private void curatorLock() {
        final CuratorFramework client = CuratorFrameworkFactory.newClient(serverAddress, new RetryNTimes(retryTimes, retryBetween));
        client.start();
        InterProcessMultiLock lock = new InterProcessMultiLock(client, Collections.singletonList(rootPath));
        try {
            if (lock.acquire(acquire, TimeUnit.SECONDS)) {
                execute();
            }
        } catch (Exception e) {
            logger.error("execute error", e);
        } finally {
            try {
                lock.release();
            } catch (Exception e) {
                logger.warn("lock release", e);
            }
        }
    }

    @Override
    public void run() {
        curatorLock();
    }
}
