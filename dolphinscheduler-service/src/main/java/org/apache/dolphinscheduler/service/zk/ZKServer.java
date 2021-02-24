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

import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;

import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * just speed experience version
 * embedded zookeeper service
 */
public class ZKServer {
    private static final Logger logger = LoggerFactory.getLogger(ZKServer.class);

    public static final int DEFAULT_ZK_TEST_PORT = 2181;

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    private PublicZooKeeperServerMain zooKeeperServerMain = null;

    private int port;

    private String dataDir = null;

    private String prefix;

    public static void main(String[] args) {
        ZKServer zkServer;
        if (args.length == 0) {
            zkServer = new ZKServer();
        } else if (args.length == 1) {
            zkServer = new ZKServer(Integer.parseInt(args[0]), "");
        } else {
            zkServer = new ZKServer(Integer.parseInt(args[0]), args[1]);
        }
        zkServer.registerHook();
        zkServer.start();
    }

    public ZKServer() {
        this(DEFAULT_ZK_TEST_PORT, "");
    }

    public ZKServer(int port, String prefix) {
        this.port = port;
        if (prefix != null && prefix.contains("/")) {
            throw new IllegalArgumentException("The prefix of path may not have '/'");
        }
        this.prefix = (prefix == null ? null : prefix.trim());
    }

    private void registerHook() {
        /*
         *  register hooks, which are called before the process exits
         */
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    /**
     * start service
     */
    public void start() {
        try {
            startLocalZkServer(port);
        } catch (Exception e) {
            logger.error("Failed to start ZK ", e);
        }
    }

    public boolean isStarted() {
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
    public void startLocalZkServer(final int port) {
        String zkDataDir = System.getProperty("user.dir") + (StringUtils.isEmpty(prefix) ? StringUtils.EMPTY : ("/" + prefix)) + "/zookeeper_data";
        File file = new File(zkDataDir);
        if (file.exists()) {
            logger.warn("The path of zk server exists");
        }
        logger.info("zk server starting, data dir path:{}", zkDataDir);
        startLocalZkServer(port, zkDataDir, ZooKeeperServer.DEFAULT_TICK_TIME, "60");
    }

    /**
     * Starts a local Zk instance
     *
     * @param port The port to listen on
     * @param dataDirPath The path for the Zk data directory
     * @param tickTime zk tick time
     * @param maxClientCnxns zk max client connections
     */
    private void startLocalZkServer(final int port, final String dataDirPath, final int tickTime, String maxClientCnxns) {
        if (isStarted.compareAndSet(false, true)) {
            zooKeeperServerMain = new PublicZooKeeperServerMain();
            logger.info("Zookeeper data path : {} ", dataDirPath);
            dataDir = dataDirPath;
            final String[] args = new String[]{Integer.toString(port), dataDirPath, Integer.toString(tickTime), maxClientCnxns};

            try {
                logger.info("Zookeeper server started ");
                isStarted.compareAndSet(false, true);

                zooKeeperServerMain.initializeAndRun(args);
            } catch (QuorumPeerConfig.ConfigException | IOException e) {
                throw new ServiceException("Caught exception while starting ZK", e);
            }
        }
    }

    /**
     * Stops a local Zk instance, deleting its data directory
     */
    public void stop() {
        try {
            stopLocalZkServer(true);
            logger.info("zk server stopped");

        } catch (Exception e) {
            logger.error("Failed to stop ZK ", e);
        }
    }

    /**
     * Stops a local Zk instance.
     *
     * @param deleteDataDir Whether or not to delete the data directory
     */
    private void stopLocalZkServer(final boolean deleteDataDir) {
        if (isStarted.compareAndSet(true, false)) {
            try {
                if (zooKeeperServerMain == null) {
                    return;
                }
                zooKeeperServerMain.shutdown();
                zooKeeperServerMain = null;
                if (deleteDataDir) {
                    org.apache.commons.io.FileUtils.deleteDirectory(new File(dataDir));
                }
            } catch (Exception e) {
                throw new ServiceException("Caught exception while starting ZK", e);
            }
        }
    }
}
