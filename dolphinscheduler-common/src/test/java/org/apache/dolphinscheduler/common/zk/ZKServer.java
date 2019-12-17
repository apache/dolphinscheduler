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
package org.apache.dolphinscheduler.common.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * just for test
 */
public class ZKServer {

    private static final Logger logger = LoggerFactory.getLogger(ZKServer.class);

    private static volatile PublicZooKeeperServerMain zkServer = null;

    public static final int DEFAULT_ZK_TEST_PORT = 22181;

    public static final String DEFAULT_ZK_STR = "localhost:" + DEFAULT_ZK_TEST_PORT;

    private static String dataDir = null;

    private static final AtomicBoolean isStarted = new AtomicBoolean(false);

    public static void start() {
        try {
            startLocalZkServer(DEFAULT_ZK_TEST_PORT);
        } catch (Exception e) {
            logger.error("Failed to start ZK: " + e);
        }
    }

    public static boolean isStarted(){
        return isStarted.get();
    }

    static class PublicZooKeeperServerMain extends ZooKeeperServerMain {

        @Override
        public void initializeAndRun(String[] args)
                throws QuorumPeerConfig.ConfigException, IOException {
            super.initializeAndRun(args);
        }

        @Override
        public void shutdown() {
            super.shutdown();
        }
    }

    /**
     * Starts a local Zk instance with a generated empty data directory
     *
     * @param port The port to listen on
     */
    public static void startLocalZkServer(final int port) {
        startLocalZkServer(port, org.apache.commons.io.FileUtils.getTempDirectoryPath() + File.separator + "test-" + System.currentTimeMillis());
    }

    /**
     * Starts a local Zk instance
     *
     * @param port        The port to listen on
     * @param dataDirPath The path for the Zk data directory
     */
    private static synchronized void startLocalZkServer(final int port, final String dataDirPath) {
        if (zkServer != null) {
            throw new RuntimeException("Zookeeper server is already started!");
        }
        try {
            zkServer = new PublicZooKeeperServerMain();
            logger.info("Zookeeper data path : {} ", dataDirPath);
            dataDir = dataDirPath;
            final String[] args = new String[]{Integer.toString(port), dataDirPath};
            Thread init = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        zkServer.initializeAndRun(args);
                    } catch (QuorumPeerConfig.ConfigException e) {
                        logger.warn("Caught exception while starting ZK", e);
                    } catch (IOException e) {
                        logger.warn("Caught exception while starting ZK", e);
                    }
                }
            }, "init-zk-thread");
            init.start();
        } catch (Exception e) {
            logger.warn("Caught exception while starting ZK", e);
            throw new RuntimeException(e);
        }

        CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                .connectString(DEFAULT_ZK_STR)
                .retryPolicy(new ExponentialBackoffRetry(10,100))
                .sessionTimeoutMs(1000 * 30)
                .connectionTimeoutMs(1000 * 30)
                .build();

        try {
            zkClient.blockUntilConnected(10, TimeUnit.SECONDS);
            zkClient.close();
        } catch (InterruptedException ignore) {
        }
        isStarted.compareAndSet(false, true);
        logger.info("zk server started");
    }

    /**
     * Stops a local Zk instance, deleting its data directory
     */
    public static void stop() {
        try {
            stopLocalZkServer(true);
        } catch (Exception e) {
            logger.error("Failed to stop ZK ",e);
        }
    }

    /**
     * Stops a local Zk instance.
     *
     * @param deleteDataDir Whether or not to delete the data directory
     */
    private static synchronized void stopLocalZkServer(final boolean deleteDataDir) {
        if (zkServer != null) {
            try {
                zkServer.shutdown();
                zkServer = null;
                if (deleteDataDir) {
                    org.apache.commons.io.FileUtils.deleteDirectory(new File(dataDir));
                }
                isStarted.compareAndSet(true, false);
            } catch (Exception e) {
                logger.warn("Caught exception while stopping ZK server", e);
                throw new RuntimeException(e);
            }
        }
    }
}