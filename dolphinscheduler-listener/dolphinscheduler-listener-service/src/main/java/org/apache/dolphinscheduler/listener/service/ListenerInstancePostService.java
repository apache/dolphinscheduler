/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.listener.service;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.listener.service.jdbc.JdbcListenerEvent;
import org.apache.dolphinscheduler.dao.entity.ListenerPluginInstance;
import org.apache.dolphinscheduler.listener.enums.ListenerEventPostServiceStatus;
import org.apache.dolphinscheduler.listener.enums.ListenerEventPostStatus;
import org.apache.dolphinscheduler.listener.plugin.ListenerPlugin;
import org.apache.dolphinscheduler.listener.processor.ListenerEventProcessorManager;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ListenerInstancePostService extends Thread {
    private final ListenerEventConsumer listenerEventConsumer;
    private volatile ListenerEventPostServiceStatus status;
    @Getter
    private ListenerPluginInstance listenerPluginInstance;
    private ListenerPlugin listenerPlugin;

    public ListenerInstancePostService(ListenerPlugin listenerPlugin, ListenerPluginInstance listenerPluginInstance,
                                       ListenerEventConsumer listenerEventConsumer) {
        log.info("listener plugin instance {} service start!", listenerPluginInstance.getInstanceName());
        this.status = ListenerEventPostServiceStatus.RUN;
        this.listenerPlugin = listenerPlugin;
        this.listenerPluginInstance = listenerPluginInstance;
        this.listenerEventConsumer = listenerEventConsumer;
    }

    @Override
    public synchronized void start() {
        super.setName("ListenerInstancePostService-" + listenerPluginInstance.getInstanceName());
        super.start();
    }

    @Override
    public void run() {
        while (!ServerLifeCycleManager.isStopped() && status != ListenerEventPostServiceStatus.STOP) {
            try {
                if (status == ListenerEventPostServiceStatus.RUN) {
                    List<JdbcListenerEvent> eventList = listenerEventConsumer.take(listenerPluginInstance.getId());
                    if (CollectionUtils.isEmpty(eventList)) {
                        log.debug("There is not waiting listener events");
                        continue;
                    }
                    this.post(eventList);
                }
            } catch (Exception e) {
                log.error("Listener post thread meet an exception", e);
            } finally {
                try {
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS * 5L);
                } catch (final InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    log.error("Current thread sleep error", interruptedException);
                }
            }
        }
    }

    public void setServiceStatus(ListenerEventPostServiceStatus status) {
        log.info("set status {}", status.getDescp());
        this.status = status;
    }

    private void post(List<JdbcListenerEvent> eventList) {
        for (JdbcListenerEvent event : eventList) {
            try {
                event.setParams(JSONUtils.toMap(listenerPluginInstance.getPluginInstanceParams()));
                ListenerEventProcessorManager.getInstance().getListenerEventProcessor(event.getEventType())
                        .process(listenerPlugin, event);

            } catch (Exception e) {
                log.error("post listener event failed, event id:{}", event.getId(), e);
                event.setPostStatus(ListenerEventPostStatus.EXECUTION_FAILURE);
                event.setLog(e.toString());
                event.setUpdateTime(new Date());
                listenerEventConsumer.update(event);
                break;
            }
            log.debug("listener event {} post successfully, delete", event.getId());
            listenerEventConsumer.delete(event);
        }
    }

    public void updateListenerPluginInstance(ListenerPluginInstance listenerPluginInstance) {
        this.listenerPluginInstance = listenerPluginInstance;
    }

    public void updateListenerPlugin(ListenerPlugin listenerPlugin) {
        this.listenerPlugin = listenerPlugin;
    }

}
