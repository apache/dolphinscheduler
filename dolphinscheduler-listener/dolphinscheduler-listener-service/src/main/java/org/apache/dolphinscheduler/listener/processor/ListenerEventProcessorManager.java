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

package org.apache.dolphinscheduler.listener.processor;

import static java.lang.String.format;

import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPIFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ListenerEventProcessorManager {

    private final Map<Integer, ListenerEventProcessor> listenerEventProcessorMap = new ConcurrentHashMap<>();

    private ListenerEventProcessorManager() {
        installListenerEventProcessor();
    }

    public static ListenerEventProcessorManager getInstance() {
        return ListenerEventProcessorManagerHolder.INSTANCE;
    }

    public ListenerEventProcessor getListenerEventProcessor(@NonNull ListenerEventType eventType) {
        return listenerEventProcessorMap.get(eventType.getCode());
    }

    public Map<Integer, ListenerEventProcessor> getListenerEventProcessorMap() {
        return listenerEventProcessorMap;
    }

    private void installListenerEventProcessor() {

        PrioritySPIFactory<ListenerEventProcessor> prioritySPIFactory =
                new PrioritySPIFactory<>(ListenerEventProcessor.class);
        for (Map.Entry<String, ListenerEventProcessor> entry : prioritySPIFactory.getSPIMap().entrySet()) {
            final ListenerEventProcessor processor = entry.getValue();
            final int code = processor.getListenerEventType().getCode();
            if (listenerEventProcessorMap.containsKey(code)) {
                throw new IllegalStateException(format("Duplicate listener event processor '%d'", code));
            }
            listenerEventProcessorMap.put(code, processor);
        }
    }

    private static class ListenerEventProcessorManagerHolder {

        private static final ListenerEventProcessorManager INSTANCE = new ListenerEventProcessorManager();
    }

}
