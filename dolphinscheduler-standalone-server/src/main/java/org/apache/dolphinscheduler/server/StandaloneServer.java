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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class StandaloneServer {

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName("Standalone-Server");

        System.setProperty("spring.profiles.active", "api,h2");
        System.setProperty("spring.datasource.sql.schema", "file:./sql/dolphinscheduler_h2.sql");

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

    private static void startAlertServer() {
        final Path alertPluginPath = Paths.get(
                StandaloneServer.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
                "../../../dolphinscheduler-alert-plugin/dolphinscheduler-alert-email/pom.xml"
        ).toAbsolutePath();
        if (Files.exists(alertPluginPath)) {
            System.setProperty("alert.plugin.binding", alertPluginPath.toString());
            System.setProperty("alert.plugin.dir", "");
        }
        AlertServer.getInstance().start();
    }

    private static void startRegistry() throws Exception {
        final TestingServer server = new TestingServer(true);
        System.setProperty("registry.servers", server.getConnectString());
    }

    private static void setTaskPlugin() {
        final Path taskPluginPath = Paths.get(
                StandaloneServer.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
                "../../../dolphinscheduler-task-plugin/dolphinscheduler-task-shell/pom.xml"
        ).toAbsolutePath();
        if (Files.exists(taskPluginPath)) {
            System.setProperty("task.plugin.binding", taskPluginPath.toString());
            System.setProperty("task.plugin.dir", "");
        } else {
            System.setProperty("task.plugin.binding", "lib/plugin/task/shell");
        }
    }
}
