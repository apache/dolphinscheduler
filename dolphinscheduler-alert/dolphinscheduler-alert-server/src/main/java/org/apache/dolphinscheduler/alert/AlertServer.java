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

package org.apache.dolphinscheduler.alert;

import static org.apache.dolphinscheduler.common.Constants.ALERT_RPC_PORT;

import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan(value = {
    "org.apache.dolphinscheduler.alert",
    "org.apache.dolphinscheduler.dao"
})
public class AlertServer implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(AlertServer.class);

    private final PluginDao pluginDao;
    private final AlertDao alertDao;
    private final AlertPluginManager alertPluginManager;
    private final AlertSender alertSender;
    private final AlertRequestProcessor alertRequestProcessor;

    private NettyRemotingServer server;

    public AlertServer(PluginDao pluginDao, AlertDao alertDao, AlertPluginManager alertPluginManager, AlertSender alertSender, AlertRequestProcessor alertRequestProcessor) {
        this.pluginDao = pluginDao;
        this.alertDao = alertDao;
        this.alertPluginManager = alertPluginManager;
        this.alertSender = alertSender;
        this.alertRequestProcessor = alertRequestProcessor;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(AlertServer.class)
            .web(WebApplicationType.NONE)
            .run(args);
    }

    @PostConstruct
    public void start() {
        log.info("Starting Alert server");

        checkTable();
        startServer();

        if (alertPluginManager.size() == 0) {
            log.warn("No alert plugin, alert sender will exit.");
            return;
        }

        Executors.newScheduledThreadPool(1)
                 .scheduleAtFixedRate(new Sender(), 5, 5, TimeUnit.SECONDS);
    }

    @Override
    @PreDestroy
    public void close() {
        server.close();
    }

    private void checkTable() {
        if (!pluginDao.checkPluginDefineTableExist()) {
            log.error("Plugin Define Table t_ds_plugin_define Not Exist . Please Create it First !");
            System.exit(1);
        }
    }

    private void startServer() {
        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(ALERT_RPC_PORT);

        server = new NettyRemotingServer(serverConfig);
        server.registerProcessor(CommandType.ALERT_SEND_REQUEST, alertRequestProcessor);
        server.start();
    }

    final class Sender implements Runnable {
        @Override
        public void run() {
            if (!Stopper.isRunning()) {
                return;
            }

            try {
                final List<Alert> alerts = alertDao.listPendingAlerts();
                alertSender.send(alerts);
            } catch (Exception e) {
                log.error("Failed to send alert", e);
            }
        }
    }
}
