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

package org.apache.dolphinscheduler.alert.service;

import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.alert.registry.AlertRegistryClient;
import org.apache.dolphinscheduler.alert.rpc.AlertRpcServer;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * The bootstrap service for alert server. it will start all the necessary component for alert server.
 */
@Slf4j
@Service
public final class AlertBootstrapService implements AutoCloseable {

    private final AlertRpcServer alertRpcServer;

    private final AlertRegistryClient alertRegistryClient;

    private final AlertPluginManager alertPluginManager;

    private final AlertHAServer alertHAServer;

    private final AlertEventFetcher alertEventFetcher;

    private final AlertEventLoop alertEventLoop;

    private final ListenerEventLoop listenerEventLoop;

    private final ListenerEventFetcher listenerEventFetcher;

    public AlertBootstrapService(AlertRpcServer alertRpcServer,
                                 AlertRegistryClient alertRegistryClient,
                                 AlertPluginManager alertPluginManager,
                                 AlertHAServer alertHAServer,
                                 AlertEventFetcher alertEventFetcher,
                                 AlertEventLoop alertEventLoop,
                                 ListenerEventLoop listenerEventLoop,
                                 ListenerEventFetcher listenerEventFetcher) {
        this.alertRpcServer = alertRpcServer;
        this.alertRegistryClient = alertRegistryClient;
        this.alertPluginManager = alertPluginManager;
        this.alertHAServer = alertHAServer;
        this.alertEventFetcher = alertEventFetcher;
        this.alertEventLoop = alertEventLoop;
        this.listenerEventLoop = listenerEventLoop;
        this.listenerEventFetcher = listenerEventFetcher;
    }

    public void start() {
        log.info("AlertBootstrapService starting...");
        alertPluginManager.start();
        alertRpcServer.start();
        alertRegistryClient.start();
        alertHAServer.start();

        listenerEventFetcher.start();
        alertEventFetcher.start();

        listenerEventLoop.start();
        alertEventLoop.start();
        log.info("AlertBootstrapService started...");
    }

    @Override
    public void close() {
        log.info("AlertBootstrapService stopping...");
        try (
                AlertRpcServer closedAlertRpcServer = alertRpcServer;
                AlertRegistryClient closedAlertRegistryClient = alertRegistryClient) {
            // close resource
            listenerEventFetcher.shutdown();
            alertEventFetcher.shutdown();

            listenerEventLoop.shutdown();
            alertEventLoop.shutdown();
            alertHAServer.shutdown();
        }
        log.info("AlertBootstrapService stopped...");
    }
}
