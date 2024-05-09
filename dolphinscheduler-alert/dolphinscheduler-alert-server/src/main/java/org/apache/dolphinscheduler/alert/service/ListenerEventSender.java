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
import org.apache.dolphinscheduler.alert.config.AlertConfig;
import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
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
import org.apache.dolphinscheduler.dao.repository.ListenerEventDao;

import org.apache.curator.shaded.com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ListenerEventSender extends AbstractEventSender<ListenerEvent> {

    private final ListenerEventDao listenerEventDao;

    private final AlertPluginInstanceMapper alertPluginInstanceMapper;

    public ListenerEventSender(ListenerEventDao listenerEventDao,
                               AlertPluginInstanceMapper alertPluginInstanceMapper,
                               AlertPluginManager alertPluginManager,
                               AlertConfig alertConfig) {
        super(alertPluginManager, alertConfig.getWaitTimeout());
        this.listenerEventDao = listenerEventDao;
        this.alertPluginInstanceMapper = alertPluginInstanceMapper;
    }

    private AbstractListenerEvent generateEventFromContent(ListenerEvent listenerEvent) {
        String content = listenerEvent.getContent();
        AbstractListenerEvent event = null;
        switch (listenerEvent.getEventType()) {
            case SERVER_DOWN:
                event = JSONUtils.parseObject(content, ServerDownListenerEvent.class);
                break;
            case PROCESS_DEFINITION_CREATED:
                event = JSONUtils.parseObject(content, ProcessDefinitionCreatedListenerEvent.class);
                break;
            case PROCESS_DEFINITION_UPDATED:
                event = JSONUtils.parseObject(content, ProcessDefinitionUpdatedListenerEvent.class);
                break;
            case PROCESS_DEFINITION_DELETED:
                event = JSONUtils.parseObject(content, ProcessDefinitionDeletedListenerEvent.class);
                break;
            case PROCESS_START:
                event = JSONUtils.parseObject(content, ProcessStartListenerEvent.class);
                break;
            case PROCESS_END:
                event = JSONUtils.parseObject(content, ProcessEndListenerEvent.class);
                break;
            case PROCESS_FAIL:
                event = JSONUtils.parseObject(content, ProcessFailListenerEvent.class);
                break;
            case TASK_START:
                event = JSONUtils.parseObject(content, TaskStartListenerEvent.class);
                break;
            case TASK_END:
                event = JSONUtils.parseObject(content, TaskEndListenerEvent.class);
                break;
            case TASK_FAIL:
                event = JSONUtils.parseObject(content, TaskFailListenerEvent.class);
                break;
            default:
                throw new IllegalArgumentException("Unsupported event type: " + listenerEvent.getEventType());
        }
        if (event == null) {
            throw new IllegalArgumentException("Failed to parse event from content: " + content);
        }
        return event;
    }

    @Override
    public List<AlertPluginInstance> getAlertPluginInstanceList(ListenerEvent event) {
        return alertPluginInstanceMapper.queryAllGlobalAlertPluginInstanceList();
    }

    @Override
    public AlertData getAlertData(ListenerEvent listenerEvent) {
        AbstractListenerEvent event = generateEventFromContent(listenerEvent);
        return AlertData.builder()
                .id(listenerEvent.getId())
                .content(JSONUtils.toJsonString(Lists.newArrayList(event)))
                .log(listenerEvent.getLog())
                .title(event.getTitle())
                .alertType(event.getEventType().getCode())
                .build();
    }

    @Override
    public Integer getEventId(ListenerEvent event) {
        return event.getId();
    }

    @Override
    public void onError(ListenerEvent event, String log) {
        listenerEventDao.updateListenerEvent(event.getId(), AlertStatus.EXECUTION_FAILURE, log, new Date());
    }

    @Override
    public void onPartialSuccess(ListenerEvent event, String log) {
        listenerEventDao.updateListenerEvent(event.getId(), AlertStatus.EXECUTION_PARTIAL_SUCCESS, log, new Date());
    }

    @Override
    public void onSuccess(ListenerEvent event, String log) {
        listenerEventDao.updateListenerEvent(event.getId(), AlertStatus.EXECUTION_FAILURE, log, new Date());
    }
}
