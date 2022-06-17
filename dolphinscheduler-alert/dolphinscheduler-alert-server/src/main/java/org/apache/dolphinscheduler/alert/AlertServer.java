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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;

import java.io.Closeable;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@ComponentScan("org.apache.dolphinscheduler")
public class AlertServer implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(AlertServer.class);

    private final PluginDao pluginDao;
    private final AlertSenderService alertSenderService;
    private final AlertRequestProcessor alertRequestProcessor;
    private final AlertConfig alertConfig;
    private NettyRemotingServer nettyRemotingServer;

    public AlertServer(PluginDao pluginDao, AlertSenderService alertSenderService, AlertRequestProcessor alertRequestProcessor, AlertConfig alertConfig) {
        this.pluginDao = pluginDao;
        this.alertSenderService = alertSenderService;
        this.alertRequestProcessor = alertRequestProcessor;
        this.alertConfig = alertConfig;
    }

    /**
     * alert server startup, not use web service
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        Thread.currentThread().setName(Constants.THREAD_NAME_ALERT_SERVER);
        new SpringApplicationBuilder(AlertServer.class).run(args);
    }

    @EventListener
    public void run(ApplicationReadyEvent readyEvent) {
        logger.info("alert server starting...");

        checkTable();
        startServer();
        alertSenderService.start();
    }

    @Override
    @PreDestroy
    public void close() {
        destroy("alert server destroy");
    }

    /**
     * gracefully stop
     *
     * @param cause stop cause
     */
    public void destroy(String cause) {

        try {
            // execute only once
            if (Stopper.isStopped()) {
                return;
            }

            logger.info("alert server is stopping ..., cause : {}", cause);

            // set stop signal is true
            Stopper.stop();

            // thread sleep 3 seconds for thread quietly stop
            ThreadUtils.sleep(3000L);

            // close
            this.nettyRemotingServer.close();

        } catch (Exception e) {
            logger.error("alert server stop exception ", e);
        }
    }

    private void checkTable() {
        if (!pluginDao.checkPluginDefineTableExist()) {
            logger.error("Plugin Define Table t_ds_plugin_define Not Exist . Please Create it First !");
            System.exit(1);
        }
    }

    private void startServer() {
        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(alertConfig.getPort());

        nettyRemotingServer = new NettyRemotingServer(serverConfig);
        nettyRemotingServer.registerProcessor(CommandType.ALERT_SEND_REQUEST, alertRequestProcessor);
        nettyRemotingServer.start();
    }

}
