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

import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.alert.registry.AlertRegistryClient;
import org.apache.dolphinscheduler.alert.rpc.AlertRpcServer;
import org.apache.dolphinscheduler.alert.service.AlertBootstrapService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;

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
public class AlertServer {

    @Autowired
    private AlertBootstrapService alertBootstrapService;
    @Autowired
    private AlertRpcServer alertRpcServer;
    @Autowired
    private AlertPluginManager alertPluginManager;
    @Autowired
    private AlertRegistryClient alertRegistryClient;

    public static void main(String[] args) {
        Thread.currentThread().setName(Constants.THREAD_NAME_ALERT_SERVER);
        new SpringApplicationBuilder(AlertServer.class).run(args);
    }

    @EventListener
    public void run(ApplicationReadyEvent readyEvent) {
        log.info("Alert server is staring ...");
        alertPluginManager.start();
        alertRegistryClient.start();
        alertBootstrapService.start();
        alertRpcServer.start();
        log.info("Alert server is started ...");
    }

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
            try (
                    AlertRpcServer closedAlertRpcServer = alertRpcServer;
                    AlertBootstrapService closedAlertBootstrapService = alertBootstrapService;
                    AlertRegistryClient closedAlertRegistryClient = alertRegistryClient) {
                // close resource
            }
            // thread sleep 3 seconds for thread quietly stop
            ThreadUtils.sleep(Constants.SERVER_CLOSE_WAIT_TIME.toMillis());
            log.info("Alter server stopped, cause: {}", cause);
        } catch (Exception e) {
            log.error("Alert server stop failed, cause: {}", cause, e);
        }
    }
}
