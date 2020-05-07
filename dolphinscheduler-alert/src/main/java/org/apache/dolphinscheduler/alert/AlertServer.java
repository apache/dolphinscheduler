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

import org.apache.dolphinscheduler.alert.plugin.EmailAlertPlugin;
import org.apache.dolphinscheduler.alert.runner.AlertSender;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.plugin.FilePluginManager;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * alert of start
 */
public class AlertServer {
    private static final Logger logger = LoggerFactory.getLogger(AlertServer.class);
    /**
     * Alert Dao
     */
    private AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);

    private AlertSender alertSender;

    private static AlertServer instance;

    private FilePluginManager alertPluginManager;

    private static final String[] whitePrefixes = new String[]{"org.apache.dolphinscheduler.plugin.utils."};

    private static final String[] excludePrefixes = new String[]{
            "org.apache.dolphinscheduler.plugin.",
            "ch.qos.logback.",
            "org.slf4j."
    };

    public AlertServer() {
        alertPluginManager =
                new FilePluginManager(PropertyUtils.getString(Constants.PLUGIN_DIR), whitePrefixes, excludePrefixes);
        // add default alert plugins
        alertPluginManager.addPlugin(new EmailAlertPlugin());
    }

    public synchronized static AlertServer getInstance() {
        if (null == instance) {
            instance = new AlertServer();
        }
        return instance;
    }

    public void start() {
        logger.info("alert server ready start ");
        while (Stopper.isRunning()) {
            try {
                Thread.sleep(Constants.ALERT_SCAN_INTERVAL);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
            List<Alert> alerts = alertDao.listWaitExecutionAlert();
            alertSender = new AlertSender(alerts, alertDao, alertPluginManager);
            alertSender.run();
        }
    }


    public static void main(String[] args) {
        AlertServer alertServer = AlertServer.getInstance();
        alertServer.start();
    }

}
