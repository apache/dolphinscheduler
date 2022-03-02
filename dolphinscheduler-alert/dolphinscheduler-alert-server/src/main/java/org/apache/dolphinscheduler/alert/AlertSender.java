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

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertInfo;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseCommand;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseResult;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class AlertSender {
    private static final Logger logger = LoggerFactory.getLogger(AlertSender.class);

    private final AlertDao alertDao;
    private final AlertPluginManager alertPluginManager;

    public AlertSender(AlertDao alertDao, AlertPluginManager alertPluginManager) {
        this.alertDao = alertDao;
        this.alertPluginManager = alertPluginManager;
    }

    public void send(List<Alert> alerts) {
        for (Alert alert : alerts) {
            //get alert group from alert
            int alertGroupId = alert.getAlertGroupId();
            List<AlertPluginInstance> alertInstanceList = alertDao.listInstanceByAlertGroupId(alertGroupId);
            if (CollectionUtils.isEmpty(alertInstanceList)) {
                logger.error("send alert msg fail,no bind plugin instance.");
                alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE, "no bind plugin instance", alert.getId());
                continue;
            }
            AlertData alertData = new AlertData();
            alertData.setId(alert.getId())
                     .setContent(alert.getContent())
                     .setLog(alert.getLog())
                     .setTitle(alert.getTitle())
                     .setTitle(alert.getTitle())
                     .setWarnType(alert.getWarningType().getCode());

            for (AlertPluginInstance instance : alertInstanceList) {

                AlertResult alertResult = this.alertResultHandler(instance, alertData);
                if (alertResult != null) {
                    AlertStatus alertStatus = Boolean.parseBoolean(String.valueOf(alertResult.getStatus())) ? AlertStatus.EXECUTION_SUCCESS : AlertStatus.EXECUTION_FAILURE;
                    alertDao.updateAlert(alertStatus, alertResult.getMessage(), alert.getId());
                }
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
    public AlertSendResponseCommand syncHandler(int alertGroupId, String title, String content , int warnType) {
        List<AlertPluginInstance> alertInstanceList = alertDao.listInstanceByAlertGroupId(alertGroupId);
        AlertData alertData = new AlertData();
        alertData.setContent(content)
                 .setTitle(title)
                 .setWarnType(warnType);

        boolean sendResponseStatus = true;
        List<AlertSendResponseResult> sendResponseResults = new ArrayList<>();

        if (CollectionUtils.isEmpty(alertInstanceList)) {
            AlertSendResponseResult alertSendResponseResult = new AlertSendResponseResult();
            String message = String.format("Alert GroupId %s send error : not found alert instance", alertGroupId);
            alertSendResponseResult.setStatus(false);
            alertSendResponseResult.setMessage(message);
            sendResponseResults.add(alertSendResponseResult);
            logger.error("Alert GroupId {} send error : not found alert instance", alertGroupId);
            return new AlertSendResponseCommand(false, sendResponseResults);
        }

        for (AlertPluginInstance instance : alertInstanceList) {
            AlertResult alertResult = this.alertResultHandler(instance, alertData);
            if (alertResult != null) {
                AlertSendResponseResult alertSendResponseResult = new AlertSendResponseResult(
                    Boolean.parseBoolean(String.valueOf(alertResult.getStatus())), alertResult.getMessage());
                sendResponseStatus = sendResponseStatus && alertSendResponseResult.getStatus();
                sendResponseResults.add(alertSendResponseResult);
            }
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
        Optional<AlertChannel> alertChannel = alertPluginManager.getAlertChannel(instance.getPluginDefineId());
        AlertResult alertResultExtend = new AlertResult();
        String pluginInstanceName = instance.getInstanceName();
        if (!alertChannel.isPresent()) {
            String message = String.format("Alert Plugin %s send error : return value is null", pluginInstanceName);
            alertResultExtend.setStatus(String.valueOf(false));
            alertResultExtend.setMessage(message);
            logger.error("Alert Plugin {} send error : not found plugin {}", pluginInstanceName, instance.getPluginDefineId());
            return alertResultExtend;
        }

        Map<String, String> paramsMap = JSONUtils.toMap(instance.getPluginInstanceParams());
        String instanceWarnType = WarningType.ALL.getDescp();

        if(paramsMap != null){
            instanceWarnType = paramsMap.getOrDefault(AlertConstants.NAME_WARNING_TYPE, WarningType.ALL.getDescp());
        }

        WarningType warningType = WarningType.of(instanceWarnType);

        if (warningType == null) {
            String message = String.format("Alert Plugin %s send error : plugin warnType is null", pluginInstanceName);
            alertResultExtend.setStatus(String.valueOf(false));
            alertResultExtend.setMessage(message);
            logger.error("Alert Plugin {} send error : plugin warnType is null", pluginInstanceName);
            return alertResultExtend;
        }

        boolean sendWarning = false;
        switch (warningType) {
            case ALL:
                sendWarning = true;
                break;
            case SUCCESS:
                if (alertData.getWarnType() == WarningType.SUCCESS.getCode()) {
                    sendWarning = true;
                }
                break;
            case FAILURE:
                if (alertData.getWarnType() == WarningType.FAILURE.getCode()) {
                    sendWarning = true;
                }
                break;
            default:
        }

        if (!sendWarning) {
            logger.info("Alert Plugin {} send ignore warning type not match: plugin warning type is {}, alert data warning type is {}", pluginInstanceName, warningType.getCode(), alertData.getWarnType());
            return null;
        }

        AlertInfo alertInfo = new AlertInfo();
        alertInfo.setAlertData(alertData);
        alertInfo.setAlertParams(paramsMap);
        AlertResult alertResult;
        try {
            alertResult = alertChannel.get().process(alertInfo);
        } catch (Exception e) {
            alertResult = new AlertResult("false", e.getMessage());
            logger.error("send alert error alert data id :{},", alertData.getId(), e);
        }

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
