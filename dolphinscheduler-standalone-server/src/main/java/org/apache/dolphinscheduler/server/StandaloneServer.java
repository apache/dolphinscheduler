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

import org.apache.dolphinscheduler.alert.AlertServer;
import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.common.utils.ScriptRunner;
import org.apache.dolphinscheduler.dao.datasource.ConnectionFactory;
import org.apache.dolphinscheduler.server.master.MasterServer;
import org.apache.dolphinscheduler.server.worker.WorkerServer;

import org.apache.curator.test.TestingServer;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

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
        Thread.currentThread().setName("Standalone-Server");

        System.setProperty("spring.profiles.active", "api");

        startDatabase();

        startRegistry();

        startAlertServer();

        setTaskPlugin();

        new SpringApplicationBuilder(
                ApiApplicationServer.class,
                MasterServer.class,
                WorkerServer.class,
                PythonGatewayServer.class
        ).run(args);
    }

    private static Path getAbsolutePath(String relativePath) throws URISyntaxException {
        String primaryPath = Paths.get(
                StandaloneServer.class.getProtectionDomain().getCodeSource().getLocation().toURI()
        ).toAbsolutePath().toString();
        return Paths.get(primaryPath, relativePath).normalize().toAbsolutePath();
    }

    private static void startAlertServer() throws URISyntaxException {
        final Path alertPluginPath = getAbsolutePath(
                "../../../dolphinscheduler-alert-plugin/dolphinscheduler-alert-email/pom.xml"
        );
        if (Files.exists(alertPluginPath)) {
            System.setProperty("alert.plugin.binding", alertPluginPath.toString());
            System.setProperty("alert.plugin.dir", "");
        }
        AlertServer.getInstance().start();
    }

    private static void startRegistry() throws Exception {
        final TestingServer server = new TestingServer(true);
        System.setProperty("registry.servers", server.getConnectString());

        final Path registryPath = getAbsolutePath(
                "../../../dolphinscheduler-registry-plugin/dolphinscheduler-registry-zookeeper/pom.xml"
        );

        if (Files.exists(registryPath)) {
            System.setProperty("registry.plugin.binding", registryPath.toString());
            System.setProperty("registry.plugin.dir", "");
        }
    }

    private static void startDatabase() throws IOException, SQLException {
        final String databaseDir = "dolphinscheduler";
        final String storageFilePrefix = "standalone.server";
        final String storageFileName = "standalone.server.mv.db";

        Path databaseDirPath = Paths.get(System.getProperty("java.io.tmpdir"), databaseDir);
        LOGGER.info("H2 database directory: {}", databaseDirPath);
        System.setProperty(
                SPRING_DATASOURCE_DRIVER_CLASS_NAME,
                org.h2.Driver.class.getName()
        );
        System.setProperty(
                SPRING_DATASOURCE_URL,
                String.format("jdbc:h2:tcp://localhost/%s/%s;MODE=MySQL;DATABASE_TO_LOWER=true",
                        databaseDirPath.toAbsolutePath(), storageFilePrefix)
        );
        System.setProperty(SPRING_DATASOURCE_USERNAME, "sa");
        System.setProperty(SPRING_DATASOURCE_PASSWORD, "");

        Server.createTcpServer("-ifNotExists", "-tcpDaemon").start();

        if (Files.notExists(Paths.get(databaseDirPath.toString(), storageFileName))) {
            final DataSource ds = ConnectionFactory.getInstance().getDataSource();
            final ScriptRunner runner = new ScriptRunner(ds.getConnection(), true, true);
            runner.runScript(new FileReader("sql/dolphinscheduler_h2.sql"));
        }
    }

    private static void setTaskPlugin() throws URISyntaxException {
        final Path taskPluginPath = getAbsolutePath(
                "../../../dolphinscheduler-task-plugin/dolphinscheduler-task-shell/pom.xml"
        );

        if (Files.exists(taskPluginPath)) {
            System.setProperty("task.plugin.binding", taskPluginPath.toString());
            System.setProperty("task.plugin.dir", "");
        } else {
            System.setProperty("task.plugin.binding", "lib/plugin/task/shell");
        }
    }
}
