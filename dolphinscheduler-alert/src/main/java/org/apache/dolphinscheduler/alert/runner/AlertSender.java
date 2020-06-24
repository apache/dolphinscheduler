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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.dolphinscheduler.alert.plugin.AlertChannelManager;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertData;
import org.apache.dolphinscheduler.spi.alert.AlertInfo;
import org.apache.dolphinscheduler.spi.alert.AlertResult;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * alert sender
 */
public class AlertSender {

    private static final Logger logger = LoggerFactory.getLogger(AlertSender.class);

    private List<Alert> alertList;
    private AlertDao alertDao;
    private AlertChannelManager alertChannelManager;

    public AlertSender(AlertChannelManager alertChannelManager) {
        this.alertChannelManager = alertChannelManager;
    }

    public AlertSender(List<Alert> alertList, AlertDao alertDao, AlertChannelManager alertChannelManager) {
        super();
        this.alertList = alertList;
        this.alertDao = alertDao;
        this.alertChannelManager = alertChannelManager;
    }

    public void run() {
        for (Alert alert : alertList) {
            //get alert group from alert
            int alertGroupId = alert.getAlertGroupId();
            //TODO: get alert plugin instance from the alert group
            List<Object> alertInstanceList = new ArrayList<>();

            AlertData alertData = new AlertData();
            alertData.setId(alert.getId())
                    .setContent(alert.getContent())
                    .setLog(alert.getLog())
                    .setTitle(alert.getTitle());

            //TODO: iterator all alert plugin instance and call the 'process' method

            for(Object instance : alertInstanceList) {
                if(alertChannelManager == null || alertChannelManager.getConfiguredAlertChannelMap().size() == 0) {
                    logger.warn("No Alert Plugin configured. Can not send alert info. ");
                    return;
                }

                String pluginName = "";
                String pluginInstanceName = "";
                AlertInfo alertInfo = new AlertInfo();
                alertInfo.setAlertData(alertData);
                alertInfo.setAlertParams("alert plugin instance params json data");
                AlertChannel alertChannel = alertChannelManager.getConfiguredAlertChannelMap().get(pluginName);
                AlertResult alertResult =alertChannel.process(alertInfo);

                if (alertResult == null) {
                    alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE, "alert send error", alert.getId());
                    logger.info("Alert Plugin {} send error : return value is null", pluginInstanceName);
                } else if (!Boolean.parseBoolean(String.valueOf(alertResult.getStatus()))) {
                    alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE, String.valueOf(alertResult.getMessage()), alert.getId());
                    logger.info("Alert Plugin {} send error : {}", pluginInstanceName, alertResult.getMessage());
                } else {
                    alertDao.updateAlert(AlertStatus.EXECUTION_SUCCESS, alertResult.getMessage(), alert.getId());
                    logger.info("Alert Plugin {} send success", pluginInstanceName);
                }
            }
        }

    }

}
