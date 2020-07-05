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

import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * just speed experience version
 * embedded zookeeper service
 */
public class ZKServer {
    private static final Logger logger = LoggerFactory.getLogger(ZKServer.class);

    public static final int DEFAULT_ZK_TEST_PORT = 2181;

    private PublicZooKeeperServerMain zkServer = null;

    private int port = DEFAULT_ZK_TEST_PORT;

    private static String dataDir = null;

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public static void main(String[] args) {
        ZKServer zkServer;
        if (args.length == 0) {
            zkServer = new ZKServer();
        } else {
            zkServer = new ZKServer(Integer.valueOf(args[0]));
        }
        zkServer.registerHook();
        zkServer.start();
    }

    public ZKServer() {}

    public ZKServer(int port) {
        this.port = port;
    }

    private void registerHook() {
        /**
         *  register hooks, which are called before the process exits
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
    }

    /**
     * start service
     */
    public void start() {
        try {
            startLocalZkServer(port);
        } catch (Exception e) {
            logger.error("Failed to start ZK: " + e);
        }
    }

    public boolean isStarted(){
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
        String zkDataDir = System.getProperty("user.dir") +"/zookeeper_data";
        logger.info("zk server starting, data dir path:{}" , zkDataDir);
        startLocalZkServer(port, zkDataDir, ZooKeeperServer.DEFAULT_TICK_TIME,"60");
    }

    /**
     * Starts a local Zk instance
     *
     * @param port        The port to listen on
     * @param dataDirPath The path for the Zk data directory
     * @param tickTime    zk tick time
     * @param maxClientCnxns    zk max client connections
     */
    private void startLocalZkServer(final int port, final String dataDirPath,final int tickTime,String maxClientCnxns) {
        if (zkServer != null) {
            throw new RuntimeException("Zookeeper server is already started!");
        }
        zkServer = new PublicZooKeeperServerMain();
        logger.info("Zookeeper data path : {} ", dataDirPath);
        dataDir = dataDirPath;
        final String[] args = new String[]{Integer.toString(port), dataDirPath, Integer.toString(tickTime), maxClientCnxns};

        try {
            logger.info("Zookeeper server started ");
            isStarted.compareAndSet(false, true);

            zkServer.initializeAndRun(args);
        } catch (QuorumPeerConfig.ConfigException e) {
            logger.warn("Caught exception while starting ZK", e);
        } catch (IOException e) {
            logger.warn("Caught exception while starting ZK", e);
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
            logger.error("Failed to stop ZK ",e);
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
                if (zkServer == null) {
                    return;
                }
                zkServer.shutdown();
                zkServer = null;
                if (deleteDataDir) {
                    org.apache.commons.io.FileUtils.deleteDirectory(new File(dataDir));
                }
            } catch (Exception e) {
                logger.warn("Caught exception while stopping ZK server", e);
                throw new RuntimeException(e);
            }
        }
    }
}