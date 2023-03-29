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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.factory.NettyRemotingServerFactory;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;

import java.io.Closeable;
import java.util.List;

import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@ComponentScan("org.apache.dolphinscheduler")
@Slf4j
public class AlertServer implements Closeable {

    @Autowired
    private PluginDao pluginDao;

    @Autowired
    private AlertSenderService alertSenderService;
    @Autowired
    private List<NettyRequestProcessor> nettyRequestProcessors;
    @Autowired
    private AlertConfig alertConfig;
    private NettyRemotingServer nettyRemotingServer;

    public static void main(String[] args) {
        Thread.currentThread().setName(Constants.THREAD_NAME_ALERT_SERVER);
        new SpringApplicationBuilder(AlertServer.class).run(args);
    }

    @EventListener
    public void run(ApplicationReadyEvent readyEvent) {
        log.info("Alert server is staring ...");

        checkTable();
        startServer();
        alertSenderService.start();
        log.info("Alert server is started ...");
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
            // set stop signal is true
            // execute only once
            if (!ServerLifeCycleManager.toStopped()) {
                log.warn("AlterServer is already stopped");
                return;
            }

            log.info("Alert server is stopping, cause: {}", cause);

            // thread sleep 3 seconds for thread quietly stop
            ThreadUtils.sleep(Constants.SERVER_CLOSE_WAIT_TIME.toMillis());

            // close
            this.nettyRemotingServer.close();
            log.info("Alter server stopped, cause: {}", cause);
        } catch (Exception e) {
            log.error("Alert server stop failed, cause: {}", cause, e);
        }
    }

    protected void checkTable() {
        if (!pluginDao.checkPluginDefineTableExist()) {
            log.error("Plugin Define Table t_ds_plugin_define Not Exist . Please Create it First !");
            System.exit(1);
        }
    }

    protected void startServer() {
        nettyRemotingServer = NettyRemotingServerFactory.buildNettyRemotingServer(alertConfig.getPort());
        for (NettyRequestProcessor nettyRequestProcessor : nettyRequestProcessors) {
            nettyRemotingServer.registerProcessor(nettyRequestProcessor);
            log.info("Success register netty processor: {}", nettyRequestProcessor.getClass().getName());
        }
        nettyRemotingServer.start();
    }
}
