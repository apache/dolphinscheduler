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

package org.apache.dolphinscheduler;

import org.apache.curator.test.TestingServer;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class StandaloneServer {

    public static void main(String[] args) throws Exception {
        try {
            // We cannot use try-with-resources to close "TestingServer", since SpringApplication.run() will not block
            // the main thread.
            TestingServer zookeeperServer = new TestingServer(true);
            System.setProperty("registry.zookeeper.connect-string", zookeeperServer.getConnectString());
            SpringApplication.run(StandaloneServer.class, args);
        } catch (Exception ex) {
            log.error("StandaloneServer start failed", ex);
            System.exit(1);
        }
    }

}
