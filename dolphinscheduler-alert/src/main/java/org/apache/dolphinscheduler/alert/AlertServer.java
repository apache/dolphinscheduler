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

import static org.apache.dolphinscheduler.alert.utils.Constants.ALERT_PROPERTIES_PATH;
import static org.apache.dolphinscheduler.common.Constants.ALERT_PLUGIN_BINDING;
import static org.apache.dolphinscheduler.common.Constants.ALERT_PLUGIN_DIR;
import static org.apache.dolphinscheduler.common.Constants.ALERT_RPC_PORT;

import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.alert.processor.AlertRequestProcessor;
import org.apache.dolphinscheduler.alert.runner.AlertSender;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.spi.plugin.DolphinPluginLoader;
import org.apache.dolphinscheduler.spi.plugin.DolphinPluginManagerConfig;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class AlertServer {

    private static final Logger logger = LoggerFactory.getLogger(AlertServer.class);

    private final PluginDao pluginDao = DaoFactory.getDaoInstance(PluginDao.class);

    private final AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);

    private AlertPluginManager alertPluginManager;

    public static final String MAVEN_LOCAL_REPOSITORY = "maven.local.repository";

    private NettyRemotingServer server;

    private static class AlertServerHolder {
        private static final AlertServer INSTANCE = new AlertServer();
    }

    public static AlertServer getInstance() {
        return AlertServerHolder.INSTANCE;
    }

    private AlertServer() {

    }

    private void checkTable() {
        if (!pluginDao.checkPluginDefineTableExist()) {
            logger.error("Plugin Define Table t_ds_plugin_define Not Exist . Please Create it First !");
            System.exit(1);
        }
    }

    private void initPlugin() {
        DolphinPluginManagerConfig alertPluginManagerConfig = new DolphinPluginManagerConfig();
        alertPluginManagerConfig.setPlugins(PropertyUtils.getString(ALERT_PLUGIN_BINDING));
        if (StringUtils.isNotBlank(PropertyUtils.getString(ALERT_PLUGIN_DIR))) {
            alertPluginManagerConfig.setInstalledPluginsDir(PropertyUtils.getString(ALERT_PLUGIN_DIR, Constants.ALERT_PLUGIN_PATH).trim());
        }

        if (StringUtils.isNotBlank(PropertyUtils.getString(MAVEN_LOCAL_REPOSITORY))) {
            alertPluginManagerConfig.setMavenLocalRepository(PropertyUtils.getString(MAVEN_LOCAL_REPOSITORY).trim());
        }

        alertPluginManager = new AlertPluginManager();
        DolphinPluginLoader alertPluginLoader = new DolphinPluginLoader(alertPluginManagerConfig, ImmutableList.of(alertPluginManager));
        try {
            alertPluginLoader.loadPlugins();
        } catch (Exception e) {
            throw new RuntimeException("Load Alert Plugin Failed !", e);
        }
    }

    private void initRemoteServer() {
        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(ALERT_RPC_PORT);
        this.server = new NettyRemotingServer(serverConfig);
        this.server.registerProcessor(CommandType.ALERT_SEND_REQUEST, new AlertRequestProcessor(alertDao, alertPluginManager));
        this.server.start();
    }

    private void runSender() {
        new Thread(new Sender()).start();
    }

    public void start() {
        PropertyUtils.loadPropertyFile(ALERT_PROPERTIES_PATH);
        checkTable();
        initPlugin();
        initRemoteServer();
        logger.info("alert server ready start ");
        runSender();
    }

    public void stop() {
        this.server.close();
        logger.info("alert server shut down");
    }

    final class Sender implements Runnable {
        @Override
        public void run() {
            while (Stopper.isRunning()) {
                try {
                    Thread.sleep(Constants.ALERT_SCAN_INTERVAL);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
                if (alertPluginManager == null || alertPluginManager.getAlertChannelMap().size() == 0) {
                    logger.warn("No Alert Plugin . Cannot send alert info. ");
                } else {
                    List<Alert> alerts = alertDao.listWaitExecutionAlert();
                    new AlertSender(alerts, alertDao, alertPluginManager).run();
                }
            }
        }
    }

    public static void main(String[] args) {
        AlertServer alertServer = AlertServer.getInstance();
        alertServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(alertServer::stop));
    }

}
