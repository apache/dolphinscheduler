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

package org.apache.dolphinscheduler.alert.runner;

import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseCommand;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseResult;
import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertData;
import org.apache.dolphinscheduler.spi.alert.AlertInfo;
import org.apache.dolphinscheduler.spi.alert.AlertResult;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * alert sender
 */
public class AlertSender {

    private static final Logger logger = LoggerFactory.getLogger(AlertSender.class);

    private List<Alert> alertList;
    private AlertDao alertDao;
    private PluginDao pluginDao;
    private AlertPluginManager alertPluginManager;

    public AlertSender(AlertPluginManager alertPluginManager) {
        this.alertPluginManager = alertPluginManager;
    }

    public AlertSender(AlertDao alertDao, AlertPluginManager alertPluginManager, PluginDao pluginDao) {
        super();
        this.alertDao = alertDao;
        this.pluginDao = pluginDao;
        this.alertPluginManager = alertPluginManager;
    }

    public AlertSender(List<Alert> alertList, AlertDao alertDao, AlertPluginManager alertPluginManager, PluginDao pluginDao) {
        super();
        this.alertList = alertList;
        this.alertDao = alertDao;
        this.pluginDao = pluginDao;
        this.alertPluginManager = alertPluginManager;
    }

    public void run() {
        for (Alert alert : alertList) {
            //get alert group from alert
            int alertGroupId = alert.getAlertGroupId();
            List<AlertPluginInstance> alertInstanceList = alertDao.listInstanceByAlertGroupId(alertGroupId);
            if (CollectionUtils.isEmpty(alertInstanceList)) {
                logger.error("send alert msg fail,no bind plugin instance.");
                return;
            }
            AlertData alertData = new AlertData();
            alertData.setId(alert.getId())
                .setContent(alert.getContent())
                .setLog(alert.getLog())
                .setTitle(alert.getTitle());

            for (AlertPluginInstance instance : alertInstanceList) {

                AlertResult alertResult = this.alertResultHandler(instance, alertData);
                AlertStatus alertStatus = Boolean.parseBoolean(String.valueOf(alertResult.getStatus())) ? AlertStatus.EXECUTION_SUCCESS : AlertStatus.EXECUTION_FAILURE;
                alertDao.updateAlert(alertStatus, alertResult.getMessage(), alert.getId());

            }
        }

    }

    /**
     * sync send alert handler
     *
     * @param alertGroupId alertGroupId
     * @param title title
     * @param content content
     * @return AlertSendResponseCommand
     */
    public AlertSendResponseCommand syncHandler(int alertGroupId, String title, String content) {

        List<AlertPluginInstance> alertInstanceList = alertDao.listInstanceByAlertGroupId(alertGroupId);
        AlertData alertData = new AlertData();
        alertData.setContent(title)
            .setTitle(content);

        boolean sendResponseStatus = true;
        List<AlertSendResponseResult> sendResponseResults = new ArrayList<>();

        if (CollectionUtils.isEmpty(alertInstanceList)) {
            sendResponseStatus = false;
            AlertSendResponseResult alertSendResponseResult = new AlertSendResponseResult();
            String message = String.format("Alert GroupId %s send error : not found alert instance", alertGroupId);
            alertSendResponseResult.setStatus(sendResponseStatus);
            alertSendResponseResult.setMessage(message);
            sendResponseResults.add(alertSendResponseResult);
            logger.error("Alert GroupId {} send error : not found alert instance", alertGroupId);
            return new AlertSendResponseCommand(sendResponseStatus, sendResponseResults);
        }

        for (AlertPluginInstance instance : alertInstanceList) {
            AlertResult alertResult = this.alertResultHandler(instance, alertData);
            AlertSendResponseResult alertSendResponseResult = new AlertSendResponseResult(
                Boolean.parseBoolean(String.valueOf(alertResult.getStatus())), alertResult.getMessage());
            sendResponseStatus = sendResponseStatus && alertSendResponseResult.getStatus();
            sendResponseResults.add(alertSendResponseResult);
        }

        return new AlertSendResponseCommand(sendResponseStatus, sendResponseResults);
    }

    /**
     * alert result handler
     *
     * @param instance instance
     * @param alertData alertData
     * @return AlertResult
     */
    private AlertResult alertResultHandler(AlertPluginInstance instance, AlertData alertData) {
        String pluginName = pluginDao.getPluginDefineById(instance.getPluginDefineId()).getPluginName();
        AlertChannel alertChannel = alertPluginManager.getAlertChannelMap().get(pluginName);
        AlertResult alertResultExtend = new AlertResult();
        String pluginInstanceName = instance.getInstanceName();
        if (alertChannel == null) {
            String message = String.format("Alert Plugin %s send error : return value is null", pluginInstanceName);
            alertResultExtend.setStatus(String.valueOf(false));
            alertResultExtend.setMessage(message);
            logger.error("Alert Plugin {} send error : not found plugin {}", pluginInstanceName, pluginName);
            return alertResultExtend;
        }

        AlertInfo alertInfo = new AlertInfo();
        alertInfo.setAlertData(alertData);
        alertInfo.setAlertParams(instance.getPluginInstanceParams());
        AlertResult alertResult = alertChannel.process(alertInfo);

        if (alertResult == null) {
            String message = String.format("Alert Plugin %s send error : return alertResult value is null", pluginInstanceName);
            alertResultExtend.setStatus(String.valueOf(false));
            alertResultExtend.setMessage(message);
            logger.info("Alert Plugin {} send error : return alertResult value is null", pluginInstanceName);
        } else if (!Boolean.parseBoolean(String.valueOf(alertResult.getStatus()))) {
            alertResultExtend.setStatus(String.valueOf(false));
            alertResultExtend.setMessage(alertResult.getMessage());
            logger.info("Alert Plugin {} send error : {}", pluginInstanceName, alertResult.getMessage());
        } else {
            String message = String.format("Alert Plugin %s send success", pluginInstanceName);
            alertResultExtend.setStatus(String.valueOf(true));
            alertResultExtend.setMessage(message);
            logger.info("Alert Plugin {} send success", pluginInstanceName);
        }
        return alertResultExtend;
    }

}
