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

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertData;
import org.apache.dolphinscheduler.alert.api.AlertInfo;
import org.apache.dolphinscheduler.alert.api.AlertResult;
import org.apache.dolphinscheduler.alert.config.AlertConfig;
import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.AlertSendStatus;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.AbstractListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ProcessDefinitionCreatedListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ProcessDefinitionDeletedListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ProcessDefinitionUpdatedListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ProcessEndListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ProcessFailListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ProcessStartListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ServerDownListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.TaskEndListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.TaskFailListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.TaskStartListenerEvent;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ListenerEventMapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public final class ListenerEventPostService extends BaseDaemonThread implements AutoCloseable {

    @Value("${alert.query_alert_threshold:100}")
    private Integer QUERY_ALERT_THRESHOLD;
    @Autowired
    private ListenerEventMapper listenerEventMapper;
    @Autowired
    private AlertPluginInstanceMapper alertPluginInstanceMapper;
    @Autowired
    private AlertPluginManager alertPluginManager;
    @Autowired
    private AlertConfig alertConfig;

    public ListenerEventPostService() {
        super("ListenerEventPostService");
    }

    @Override
    public void run() {
        log.info("listener event post thread started");
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                List<ListenerEvent> listenerEvents = listenerEventMapper
                        .listingListenerEventByStatus(AlertStatus.WAIT_EXECUTION, QUERY_ALERT_THRESHOLD);
                if (CollectionUtils.isEmpty(listenerEvents)) {
                    log.debug("There is no waiting listener events");
                    continue;
                }
                this.send(listenerEvents);
            } catch (Exception e) {
                log.error("listener event post thread meet an exception", e);
            } finally {
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS * 5L);
            }
        }
        log.info("listener event post thread stopped");
    }

    public void send(List<ListenerEvent> listenerEvents) {
        for (ListenerEvent listenerEvent : listenerEvents) {
            int eventId = listenerEvent.getId();
            List<AlertPluginInstance> globalAlertInstanceList =
                    alertPluginInstanceMapper.queryAllGlobalAlertPluginInstanceList();
            if (CollectionUtils.isEmpty(globalAlertInstanceList)) {
                log.error("post listener event fail,no bind global plugin instance.");
                listenerEventMapper.updateListenerEvent(eventId, AlertStatus.EXECUTION_FAILURE,
                        "no bind plugin instance", new Date());
                continue;
            }
            AbstractListenerEvent event = generateEventFromContent(listenerEvent);
            if (event == null) {
                log.error("parse listener event to abstract listener event fail.ed {}", listenerEvent.getContent());
                listenerEventMapper.updateListenerEvent(eventId, AlertStatus.EXECUTION_FAILURE,
                        "parse listener event to abstract listener event failed", new Date());
                continue;
            }
            List<AbstractListenerEvent> events = Lists.newArrayList(event);
            AlertData alertData = AlertData.builder()
                    .id(eventId)
                    .content(JSONUtils.toJsonString(events))
                    .log(listenerEvent.getLog())
                    .title(event.getTitle())
                    .warnType(WarningType.GLOBAL.getCode())
                    .alertType(event.getEventType().getCode())
                    .build();

            int sendSuccessCount = 0;
            List<AlertSendStatus> failedPostResults = new ArrayList<>();
            for (AlertPluginInstance instance : globalAlertInstanceList) {
                AlertResult alertResult = this.alertResultHandler(instance, alertData);
                if (alertResult != null) {
                    AlertStatus sendStatus = Boolean.parseBoolean(alertResult.getStatus())
                            ? AlertStatus.EXECUTION_SUCCESS
                            : AlertStatus.EXECUTION_FAILURE;
                    if (AlertStatus.EXECUTION_SUCCESS.equals(sendStatus)) {
                        sendSuccessCount++;
                    } else {
                        AlertSendStatus alertSendStatus = AlertSendStatus.builder()
                                .alertId(eventId)
                                .alertPluginInstanceId(instance.getId())
                                .sendStatus(sendStatus)
                                .log(JSONUtils.toJsonString(alertResult))
                                .createTime(new Date())
                                .build();
                        failedPostResults.add(alertSendStatus);
                    }
                }
            }
            if (sendSuccessCount == globalAlertInstanceList.size()) {
                listenerEventMapper.deleteById(eventId);
            } else {
                AlertStatus alertStatus =
                        sendSuccessCount == 0 ? AlertStatus.EXECUTION_FAILURE : AlertStatus.EXECUTION_PARTIAL_SUCCESS;
                listenerEventMapper.updateListenerEvent(eventId, alertStatus, JSONUtils.toJsonString(failedPostResults),
                        new Date());
            }
        }
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
            String message =
                    String.format("Global Alert Plugin %s send error: the channel doesn't exist, pluginDefineId: %s",
                            pluginInstanceName,
                            pluginDefineId);
            log.error("Global Alert Plugin {} send error : not found plugin {}", pluginInstanceName, pluginDefineId);
            return new AlertResult("false", message);
        }
        AlertChannel alertChannel = alertChannelOptional.get();

        Map<String, String> paramsMap = JSONUtils.toMap(instance.getPluginInstanceParams());

        AlertInfo alertInfo = AlertInfo.builder()
                .alertData(alertData)
                .alertParams(paramsMap)
                .alertPluginInstanceId(instance.getId())
                .build();
        int waitTimeout = alertConfig.getWaitTimeout();
        try {
            AlertResult alertResult;
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
            if (alertResult == null) {
                throw new RuntimeException("Alert result cannot be null");
            }
            return alertResult;
        } catch (InterruptedException e) {
            log.error("post listener event error alert data id :{},", alertData.getId(), e);
            Thread.currentThread().interrupt();
            return new AlertResult("false", e.getMessage());
        } catch (Exception e) {
            log.error("post listener event error alert data id :{},", alertData.getId(), e);
            return new AlertResult("false", e.getMessage());
        }
    }

    private AbstractListenerEvent generateEventFromContent(ListenerEvent listenerEvent) {
        String content = listenerEvent.getContent();
        switch (listenerEvent.getEventType()) {
            case SERVER_DOWN:
                return JSONUtils.parseObject(content, ServerDownListenerEvent.class);
            case PROCESS_DEFINITION_CREATED:
                return JSONUtils.parseObject(content, ProcessDefinitionCreatedListenerEvent.class);
            case PROCESS_DEFINITION_UPDATED:
                return JSONUtils.parseObject(content, ProcessDefinitionUpdatedListenerEvent.class);
            case PROCESS_DEFINITION_DELETED:
                return JSONUtils.parseObject(content, ProcessDefinitionDeletedListenerEvent.class);
            case PROCESS_START:
                return JSONUtils.parseObject(content, ProcessStartListenerEvent.class);
            case PROCESS_END:
                return JSONUtils.parseObject(content, ProcessEndListenerEvent.class);
            case PROCESS_FAIL:
                return JSONUtils.parseObject(content, ProcessFailListenerEvent.class);
            case TASK_START:
                return JSONUtils.parseObject(content, TaskStartListenerEvent.class);
            case TASK_END:
                return JSONUtils.parseObject(content, TaskEndListenerEvent.class);
            case TASK_FAIL:
                return JSONUtils.parseObject(content, TaskFailListenerEvent.class);
            default:
                return null;
        }
    }
    @Override
    public void close() {
        log.info("Closed ListenerEventPostService...");
    }
}
