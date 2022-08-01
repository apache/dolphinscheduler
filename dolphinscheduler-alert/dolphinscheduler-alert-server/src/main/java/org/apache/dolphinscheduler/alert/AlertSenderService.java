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
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public final class AlertSenderService extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(AlertSenderService.class);

    private final AlertDao alertDao;
    private final AlertPluginManager alertPluginManager;
    private final AlertConfig alertConfig;

    public AlertSenderService(AlertDao alertDao, AlertPluginManager alertPluginManager, AlertConfig alertConfig) {
        this.alertDao = alertDao;
        this.alertPluginManager = alertPluginManager;
        this.alertConfig = alertConfig;
    }

    @Override
    public synchronized void start() {
        super.setName("AlertSenderService");
        super.start();
    }

    @Override
    public void run() {
        logger.info("alert sender started");
        while (Stopper.isRunning()) {
            try {
                List<Alert> alerts = alertDao.listPendingAlerts();
                this.send(alerts);
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS * 5L);
            } catch (Exception e) {
                logger.error("alert sender thread error", e);
            }
        }
    }

    public void send(List<Alert> alerts) {
        for (Alert alert : alerts) {
            //get alert group from alert
            int alertId = Optional.ofNullable(alert.getId()).orElse(0);
            int alertGroupId = Optional.ofNullable(alert.getAlertGroupId()).orElse(0);
            List<AlertPluginInstance> alertInstanceList = alertDao.listInstanceByAlertGroupId(alertGroupId);
            if (CollectionUtils.isEmpty(alertInstanceList)) {
                logger.error("send alert msg fail,no bind plugin instance.");
                List<AlertResult> alertResults = Lists.newArrayList(new AlertResult("false",
                                                                                    "no bind plugin instance"));
                alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE, JSONUtils.toJsonString(alertResults), alertId);
                continue;
            }
            AlertData alertData = AlertData.builder()
                .id(alertId)
                .content(alert.getContent())
                .log(alert.getLog())
                .title(alert.getTitle())
                .warnType(alert.getWarningType().getCode())
                .alertType(alert.getAlertType().getCode())
                .build();

            int sendSuccessCount = 0;
            List<AlertResult> alertResults = new ArrayList<>();
            for (AlertPluginInstance instance : alertInstanceList) {
                AlertResult alertResult = this.alertResultHandler(instance, alertData);
                if (alertResult != null) {
                    AlertStatus sendStatus = Boolean.parseBoolean(String.valueOf(alertResult.getStatus())) ? AlertStatus.EXECUTION_SUCCESS : AlertStatus.EXECUTION_FAILURE;
                    alertDao.addAlertSendStatus(sendStatus, JSONUtils.toJsonString(alertResult), alertId, instance.getId());
                    if (sendStatus.equals(AlertStatus.EXECUTION_SUCCESS)) {
                        sendSuccessCount++;
                    }
                    alertResults.add(alertResult);
                }
            }
            AlertStatus alertStatus = AlertStatus.EXECUTION_SUCCESS;
            if (sendSuccessCount == 0) {
                alertStatus = AlertStatus.EXECUTION_FAILURE;
            } else if (sendSuccessCount < alertInstanceList.size()) {
                alertStatus = AlertStatus.EXECUTION_PARTIAL_SUCCESS;
            }
            alertDao.updateAlert(alertStatus, JSONUtils.toJsonString(alertResults), alertId);
        }
    }

    /**
     * sync send alert handler
     *
     * @param alertGroupId alertGroupId
     * @param title        title
     * @param content      content
     * @return AlertSendResponseCommand
     */
    public AlertSendResponseCommand syncHandler(int alertGroupId, String title, String content, int warnType) {
        List<AlertPluginInstance> alertInstanceList = alertDao.listInstanceByAlertGroupId(alertGroupId);
        AlertData alertData = AlertData.builder()
                .content(content)
                .title(title)
                .warnType(warnType)
                .build();

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
     * @param instance  instance
     * @param alertData alertData
     * @return AlertResult
     */
    private @Nullable AlertResult alertResultHandler(AlertPluginInstance instance, AlertData alertData) {
        String pluginInstanceName = instance.getInstanceName();
        int pluginDefineId = instance.getPluginDefineId();
        Optional<AlertChannel> alertChannelOptional = alertPluginManager.getAlertChannel(instance.getPluginDefineId());
        if (!alertChannelOptional.isPresent()) {
            String message = String.format("Alert Plugin %s send error: the channel doesn't exist, pluginDefineId: %s",
                                           pluginInstanceName,
                                           pluginDefineId);
            logger.error("Alert Plugin {} send error : not found plugin {}", pluginInstanceName, pluginDefineId);
            return new AlertResult("false", message);
        }
        AlertChannel alertChannel = alertChannelOptional.get();

        Map<String, String> paramsMap = JSONUtils.toMap(instance.getPluginInstanceParams());
        String instanceWarnType = WarningType.ALL.getDescp();

        if (paramsMap != null) {
            instanceWarnType = paramsMap.getOrDefault(AlertConstants.NAME_WARNING_TYPE, WarningType.ALL.getDescp());
        }

        WarningType warningType = WarningType.of(instanceWarnType);

        if (warningType == null) {
            String message = String.format("Alert Plugin %s send error : plugin warnType is null", pluginInstanceName);
            logger.error("Alert Plugin {} send error : plugin warnType is null", pluginInstanceName);
            return new AlertResult("false", message);
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
            logger.info("Alert Plugin {} send ignore warning type not match: plugin warning type is {}, alert data warning type is {}",
                    pluginInstanceName, warningType.getCode(), alertData.getWarnType());
            return null;
        }

        AlertInfo alertInfo = AlertInfo.builder()
                .alertData(alertData)
                .alertParams(paramsMap)
                .build();
        int waitTimeout = alertConfig.getWaitTimeout();
        AlertResult alertResult;
        try {
            if (waitTimeout <= 0) {
                if (alertData.getAlertType() == AlertType.CLOSE_ALERT.getCode()) {
                    alertResult = alertChannel.closeAlert(alertInfo);
                } else {
                    alertResult = alertChannel.process(alertInfo);
                }
            } else {
                CompletableFuture<AlertResult> future;
                if (alertData.getAlertType() == AlertType.CLOSE_ALERT.getCode()) {
                    future = CompletableFuture.supplyAsync(() -> alertChannel.closeAlert(alertInfo));
                } else {
                    future = CompletableFuture.supplyAsync(() -> alertChannel.process(alertInfo));
                }
                alertResult = future.get(waitTimeout, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            alertResult = new AlertResult("false", e.getMessage());
            logger.error("send alert error alert data id :{},", alertData.getId(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            alertResult = new AlertResult("false", e.getMessage());
            logger.error("send alert error alert data id :{},", alertData.getId(), e);
        }

        AlertResult alertResultExtend = new AlertResult();
        if (alertResult == null) {
            String message = String.format("Alert Plugin %s send error : return alertResult value is null", pluginInstanceName);
            alertResultExtend.setStatus("false");
            alertResultExtend.setMessage(message);
            logger.info("Alert Plugin {} send error : return alertResult value is null", pluginInstanceName);
        } else if (!Boolean.parseBoolean(String.valueOf(alertResult.getStatus()))) {
            alertResultExtend.setStatus("false");
            alertResultExtend.setMessage(alertResult.getMessage());
            logger.info("Alert Plugin {} send error : {}", pluginInstanceName, alertResult.getMessage());
        } else {
            String message = String.format("Alert Plugin %s send success", pluginInstanceName);
            alertResultExtend.setStatus("true");
            alertResultExtend.setMessage(message);
            logger.info("Alert Plugin {} send success", pluginInstanceName);
        }
        return alertResultExtend;
    }
}
