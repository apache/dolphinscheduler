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

import static org.apache.dolphinscheduler.common.Constants.SPRING_DATASOURCE_DRIVER_CLASS_NAME;
import static org.apache.dolphinscheduler.common.Constants.SPRING_DATASOURCE_PASSWORD;
import static org.apache.dolphinscheduler.common.Constants.SPRING_DATASOURCE_URL;
import static org.apache.dolphinscheduler.common.Constants.SPRING_DATASOURCE_USERNAME;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.common.utils.ScriptRunner;
import org.apache.dolphinscheduler.dao.datasource.ConnectionFactory;
import org.apache.dolphinscheduler.server.master.MasterServer;
import org.apache.dolphinscheduler.server.worker.WorkerServer;

import org.apache.curator.test.TestingServer;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sql.DataSource;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class StandaloneServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneServer.class);

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.profiles.active", "api");

        final Path temp = Files.createTempDirectory("dolphinscheduler_");
        LOGGER.info("H2 database directory: {}", temp);
        System.setProperty(
                SPRING_DATASOURCE_DRIVER_CLASS_NAME,
                org.h2.Driver.class.getName()
        );
        System.setProperty(
                SPRING_DATASOURCE_URL,
                String.format("jdbc:h2:tcp://localhost/%s", temp.toAbsolutePath())
        );
        System.setProperty(SPRING_DATASOURCE_USERNAME, "sa");
        System.setProperty(SPRING_DATASOURCE_PASSWORD, "");

        Server.createTcpServer("-ifNotExists").start();

        final DataSource ds = ConnectionFactory.getInstance().getDataSource();
        final ScriptRunner runner = new ScriptRunner(ds.getConnection(), true, true);
        runner.runScript(new FileReader("sql/dolphinscheduler_h2.sql"));

        final TestingServer server = new TestingServer(true);
        System.setProperty("registry.servers", server.getConnectString());

        Thread.currentThread().setName("Standalone-Server");

        new SpringApplicationBuilder(
                ApiApplicationServer.class,
                MasterServer.class,
                WorkerServer.class
        ).run(args);
    }
}
