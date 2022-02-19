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

package org.apache.dolphinscheduler.server;

import org.apache.dolphinscheduler.alert.AlertServer;
import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.server.master.MasterServer;
import org.apache.dolphinscheduler.server.worker.WorkerServer;

import org.apache.curator.test.TestingServer;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan
public class StandaloneServer {
    public static void main(String[] args) throws Exception {
        final TestingServer server = new TestingServer(true);
        System.setProperty("registry.servers", server.getConnectString());

        new SpringApplicationBuilder(
            ApiApplicationServer.class,
            MasterServer.class,
            WorkerServer.class,
            AlertServer.class,
            PythonGatewayServer.class
        ).profiles("master", "worker", "api", "alert", "python-gateway", "h2", "standalone").run(args);
    }
}
