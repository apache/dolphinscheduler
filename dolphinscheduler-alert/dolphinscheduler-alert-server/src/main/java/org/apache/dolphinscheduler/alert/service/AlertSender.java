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

import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.config.AlertConfig;
import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.extract.alert.request.AlertSendResponse;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlertSender extends AbstractEventSender<Alert> {

    private final AlertDao alertDao;

    public AlertSender(AlertDao alertDao,
                       AlertPluginManager alertPluginManager,
                       AlertConfig alertConfig) {
        super(alertPluginManager, alertConfig.getWaitTimeout());
        this.alertDao = alertDao;
    }

    /**
     * sync send alert handler
     *
     * @param alertGroupId alertGroupId
     * @param title        title
     * @param content      content
     * @return AlertSendResponseCommand
     */
    public AlertSendResponse syncHandler(int alertGroupId, String title, String content) {
        List<AlertPluginInstance> alertInstanceList = alertDao.listInstanceByAlertGroupId(alertGroupId);
        AlertData alertData = AlertData.builder()
                .content(content)
                .title(title)
                .build();

        boolean sendResponseStatus = true;
        List<AlertSendResponse.AlertSendResponseResult> sendResponseResults = new ArrayList<>();

        if (CollectionUtils.isEmpty(alertInstanceList)) {
            AlertSendResponse.AlertSendResponseResult alertSendResponseResult =
                    new AlertSendResponse.AlertSendResponseResult();
            String message = String.format("Alert GroupId %s send error : not found alert instance", alertGroupId);
            alertSendResponseResult.setSuccess(false);
            alertSendResponseResult.setMessage(message);
            sendResponseResults.add(alertSendResponseResult);
            log.error("Alert GroupId {} send error : not found alert instance", alertGroupId);
            return new AlertSendResponse(false, sendResponseResults);
        }

        for (AlertPluginInstance instance : alertInstanceList) {
            AlertResult alertResult = doSendEvent(instance, alertData);
            if (alertResult != null) {
                AlertSendResponse.AlertSendResponseResult alertSendResponseResult =
                        new AlertSendResponse.AlertSendResponseResult(
                                alertResult.isSuccess(),
                                alertResult.getMessage());
                sendResponseStatus = sendResponseStatus && alertSendResponseResult.isSuccess();
                sendResponseResults.add(alertSendResponseResult);
            }
        }

        return new AlertSendResponse(sendResponseStatus, sendResponseResults);
    }

    @Override
    public List<AlertPluginInstance> getAlertPluginInstanceList(Alert event) {
        return alertDao.listInstanceByAlertGroupId(event.getAlertGroupId());
    }

    @Override
    public AlertData getAlertData(Alert event) {
        return AlertData.builder()
                .id(event.getId())
                .content(event.getContent())
                .log(event.getLog())
                .title(event.getTitle())
                .alertType(event.getAlertType().getCode())
                .build();
    }

    @Override
    public Integer getEventId(Alert event) {
        return event.getId();
    }

    @Override
    public void onError(Alert event, String log) {
        alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE, log, event.getId());
    }

    @Override
    public void onPartialSuccess(Alert event, String log) {
        alertDao.updateAlert(AlertStatus.EXECUTION_PARTIAL_SUCCESS, log, event.getId());
    }

    @Override
    public void onSuccess(Alert event, String log) {
        alertDao.updateAlert(AlertStatus.EXECUTION_SUCCESS, log, event.getId());
    }
}
