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
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.ScriptRunner;
import org.apache.dolphinscheduler.dao.datasource.ConnectionFactory;
import org.apache.dolphinscheduler.server.master.MasterServer;
import org.apache.dolphinscheduler.server.worker.WorkerServer;

import org.apache.curator.test.TestingServer;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
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
                WorkerServer.class
        ).run(args);
    }

    private static void startAlertServer() {

        final String alertPluginRelativePomPath =
                "../../../dolphinscheduler-alert-plugin/dolphinscheduler-alert-email/pom.xml";
        final Path alertPluginPath = getPluginPomAbsolutePath(alertPluginRelativePomPath);

        if (Files.exists(alertPluginPath)) {
            System.setProperty("alert.plugin.binding", alertPluginPath.toString());
            System.setProperty("alert.plugin.dir", "");
        }
        AlertServer.getInstance().start();
    }

    private static void startRegistry() throws Exception {
        final TestingServer server = new TestingServer(true);
        System.setProperty("registry.servers", server.getConnectString());

        final String registryPluginPomRelativePath =
                "../../../dolphinscheduler-registry-plugin/dolphinscheduler-registry-zookeeper/pom.xml";
        final Path registryPath = getPluginPomAbsolutePath(registryPluginPomRelativePath);

        if (Files.exists(registryPath)) {
            System.setProperty("registry.plugin.binding", registryPath.toString());
            System.setProperty("registry.plugin.dir", "");
        }
    }

    private static void startDatabase() throws IOException, SQLException {
        final Path temp = Files.createTempDirectory("dolphinscheduler_");
        LOGGER.info("H2 database directory: {}", temp);
        System.setProperty(
                SPRING_DATASOURCE_DRIVER_CLASS_NAME,
                org.h2.Driver.class.getName()
        );
        System.setProperty(
                SPRING_DATASOURCE_URL,
                String.format("jdbc:h2:tcp://localhost/%s;MODE=MySQL;DATABASE_TO_LOWER=true", temp.toAbsolutePath())
        );
        System.setProperty(SPRING_DATASOURCE_USERNAME, "sa");
        System.setProperty(SPRING_DATASOURCE_PASSWORD, "");

        Server.createTcpServer("-ifNotExists").start();

        final DataSource ds = ConnectionFactory.getInstance().getDataSource();
        final ScriptRunner runner = new ScriptRunner(ds.getConnection(), true, true);
        runner.runScript(new FileReader("sql/dolphinscheduler_h2.sql"));
    }

    private static void setTaskPlugin() {

        final String taskPluginPomRelativePath =
                "../../../dolphinscheduler-task-plugin/dolphinscheduler-task-shell/pom.xml";
        final Path taskPluginPath = getPluginPomAbsolutePath(taskPluginPomRelativePath);

        if (Files.exists(taskPluginPath)) {
            System.setProperty("task.plugin.binding", taskPluginPath.toString());
            System.setProperty("task.plugin.dir", "");
        }
    }

    /**
     * Get the `pom.xml` file's absolute path of a plugin from root CodeSource.
     *
     * <p>
     *     for example, the compiled class stores in path 'dolphinscheduler/target/classes' <br>
     *     but the plugin path is 'dolphinscheduler/dolphinscheduler-task-plugin/dolphinscheduler-task-shell/pom.xml' <br>
     *     call this method like this: <br>
     *     <pre>
     *      {@code Path pluginPomPath = getPluginRelativePath("../../../dolphinscheduler-task-plugin/dolphinscheduler-task-shell/pom.xml");}
     *     </pre>
     * </p>
     *
     * @param pluginPomPath the relative path of pom.xml
     * @return absolute path of the pom.xml
     */
    private static Path getPluginPomAbsolutePath(String pluginPomPath) {
        final CodeSource codeSource = StandaloneServer.class.getProtectionDomain().getCodeSource();
        URL location = codeSource.getLocation();
        Path codeSourcePath;
        try {
            codeSourcePath = Paths.get(location.toURI());
        } catch (URISyntaxException e) {
            // if the URL can't be parsed to URI, use URL#getPath() instead
            String path = location.getPath();
            LOGGER.error("Location {} can't be parsed to a URI, use {}", location, path);
            // if the system is Windows
            if (OSUtils.isWindows()) {
                int index = 0;
                if (path.startsWith("!/") || path.startsWith("/")) {
                    index = path.indexOf('/');
                } else if (path.startsWith("!\\") || path.startsWith("\\")) {
                    index = path.indexOf('\\');
                }
                path = path.substring(index, path.length());
            }
            codeSourcePath = Paths.get(path);
        }
        return codeSourcePath.resolve(pluginPomPath).toAbsolutePath();
    }
}
