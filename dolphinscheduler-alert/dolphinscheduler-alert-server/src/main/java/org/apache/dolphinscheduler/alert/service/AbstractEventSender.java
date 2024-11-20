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

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertInfo;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.AlertSendStatus;
import org.apache.dolphinscheduler.extract.alert.request.AlertSendResponse;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

@Slf4j
public abstract class AbstractEventSender<T> implements EventSender<T> {

    protected final AlertPluginManager alertPluginManager;

    private final long sendEventTimeout;

    protected AbstractEventSender(AlertPluginManager alertPluginManager, long sendEventTimeout) {
        this.alertPluginManager = alertPluginManager;
        this.sendEventTimeout = sendEventTimeout;
    }

    @Override
    public void sendEvent(T event) {
        List<AlertPluginInstance> alertPluginInstanceList = getAlertPluginInstanceList(event);
        if (CollectionUtils.isEmpty(alertPluginInstanceList)) {
            onError(event, "No bind plugin instance found");
            return;
        }
        AlertData alertData = getAlertData(event);
        List<AlertSendStatus> alertSendStatuses = new ArrayList<>();
        for (AlertPluginInstance instance : alertPluginInstanceList) {
            AlertResult alertResult = doSendEvent(instance, alertData);
            AlertStatus alertStatus =
                    alertResult.isSuccess() ? AlertStatus.EXECUTION_SUCCESS : AlertStatus.EXECUTION_FAILURE;
            AlertSendStatus alertSendStatus = AlertSendStatus.builder()
                    .alertId(getEventId(event))
                    .alertPluginInstanceId(instance.getId())
                    .sendStatus(alertStatus)
                    .log(JSONUtils.toJsonString(alertResult))
                    .createTime(new Date())
                    .build();
            alertSendStatuses.add(alertSendStatus);
        }
        long failureCount = alertSendStatuses.stream()
                .filter(alertSendStatus -> alertSendStatus.getSendStatus() == AlertStatus.EXECUTION_FAILURE)
                .count();
        long successCount = alertSendStatuses.stream()
                .filter(alertSendStatus -> alertSendStatus.getSendStatus() == AlertStatus.EXECUTION_SUCCESS)
                .count();
        if (successCount == 0) {
            onError(event, JSONUtils.toJsonString(alertSendStatuses));
        } else {
            if (failureCount > 0) {
                onPartialSuccess(event, JSONUtils.toJsonString(alertSendStatuses));
            } else {
                onSuccess(event, JSONUtils.toJsonString(alertSendStatuses));
            }
        }
    }

    public abstract List<AlertPluginInstance> getAlertPluginInstanceList(T event);

    public abstract AlertData getAlertData(T event);

    public abstract Integer getEventId(T event);

    public abstract void onError(T event, String log);

    public abstract void onPartialSuccess(T event, String log);

    public abstract void onSuccess(T event, String log);

    @Override
    public AlertResult doSendEvent(AlertPluginInstance instance, AlertData alertData) {
        int pluginDefineId = instance.getPluginDefineId();
        Optional<AlertChannel> alertChannelOptional = alertPluginManager.getAlertChannel(pluginDefineId);
        if (!alertChannelOptional.isPresent()) {
            return AlertResult.fail("Cannot find the alertPlugin: " + pluginDefineId);
        }
        AlertChannel alertChannel = alertChannelOptional.get();

        AlertInfo alertInfo = AlertInfo.builder()
                .alertData(alertData)
                .alertParams(JSONUtils.toMap(instance.getPluginInstanceParams()))
                .alertPluginInstanceId(instance.getId())
                .build();
        try {
            AlertResult alertResult;
            if (sendEventTimeout <= 0) {
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
                alertResult = future.get(sendEventTimeout, TimeUnit.MILLISECONDS);
            }
            checkNotNull(alertResult, "AlertResult cannot be null");
            return alertResult;
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            return AlertResult.fail(ExceptionUtils.getMessage(interruptedException));
        } catch (Exception e) {
            log.error("Send alert data {} failed", alertData, e);
            return AlertResult.fail(ExceptionUtils.getMessage(e));
        }
    }

    @Override
    public AlertSendResponse syncTestSend(int pluginDefineId, String pluginInstanceParams) {

        Optional<AlertChannel> alertChannelOptional = alertPluginManager.getAlertChannel(pluginDefineId);
        if (!alertChannelOptional.isPresent()) {
            AlertSendResponse.AlertSendResponseResult alertSendResponseResult =
                    AlertSendResponse.AlertSendResponseResult.fail("Cannot find the alertPlugin: " + pluginDefineId);
            return AlertSendResponse.fail(Lists.newArrayList(alertSendResponseResult));
        }
        AlertData alertData = AlertData.builder()
                .title(AlertConstants.TEST_TITLE)
                .content(AlertConstants.TEST_CONTENT)
                .build();

        AlertInfo alertInfo = AlertInfo.builder()
                .alertData(alertData)
                .alertParams(PluginParamsTransfer.getPluginParamsMap(pluginInstanceParams))
                .build();

        try {
            AlertResult alertResult = alertChannelOptional.get().process(alertInfo);
            Preconditions.checkNotNull(alertResult, "AlertResult cannot be null");
            if (alertResult.isSuccess()) {
                return AlertSendResponse
                        .success(Lists.newArrayList(AlertSendResponse.AlertSendResponseResult.success()));
            }
            return AlertSendResponse.fail(
                    Lists.newArrayList(AlertSendResponse.AlertSendResponseResult.fail(alertResult.getMessage())));
        } catch (Exception e) {
            log.error("Test send alert error", e);
            return new AlertSendResponse(false,
                    Lists.newArrayList(AlertSendResponse.AlertSendResponseResult.fail(ExceptionUtils.getMessage(e))));
        }

    }
}
