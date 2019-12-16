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
package org.apache.dolphinscheduler.server.zk;

import org.apache.dolphinscheduler.common.thread.ThreadPoolExecutors;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;


/**
 * just for test
 */
@Ignore
public class StandaloneZKServerForTest {

    private static final Logger logger = LoggerFactory.getLogger(StandaloneZKServerForTest.class);

    private static volatile ZooKeeperServerMain zkServer = null;


    @Before
    public void before() {
        logger.info("standalone zookeeper server for test service start ");

        ThreadPoolExecutors.getInstance().execute(new Runnable() {
            @Override
            public void run() {

                //delete zk data dir ?
                File zkFile = new File(System.getProperty("java.io.tmpdir"), "zookeeper");

                startStandaloneServer("2000", zkFile.getAbsolutePath(), "2181", "10", "5");
            }
        });

    }


    /**
     * start zk server
     * @param tickTime  zookeeper ticktime
     * @param dataDir zookeeper data dir
     * @param clientPort zookeeper client port
     * @param initLimit zookeeper init limit
     * @param syncLimit zookeeper sync limit
     */
    private void startStandaloneServer(String tickTime, String dataDir, String clientPort, String initLimit, String syncLimit) {
        Properties props = new Properties();
        props.setProperty("tickTime", tickTime);
        props.setProperty("dataDir", dataDir);
        props.setProperty("clientPort", clientPort);
        props.setProperty("initLimit", initLimit);
        props.setProperty("syncLimit", syncLimit);

        QuorumPeerConfig quorumConfig = new QuorumPeerConfig();
        try {
            quorumConfig.parseProperties(props);

            if(zkServer == null ){

                synchronized (StandaloneZKServerForTest.class){
                    if(zkServer == null ){
                        zkServer = new ZooKeeperServerMain();
                        final ServerConfig config = new ServerConfig();
                        config.readFrom(quorumConfig);
                        zkServer.runFromConfig(config);
                    }
                }

            }

        } catch (Exception e) {
            logger.error("start standalone server fail！", e);
        }
    }


}