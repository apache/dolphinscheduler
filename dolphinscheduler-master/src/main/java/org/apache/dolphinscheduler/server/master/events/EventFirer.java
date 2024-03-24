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

package org.apache.dolphinscheduler.server.master.events;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.utils.ExceptionUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventFirer implements IEventFirer {

    private final IEventOperatorManager<IEvent> eventOperatorManager;

    private final ThreadPoolExecutor eventFireThreadPool;

    public EventFirer(IEventOperatorManager<IEvent> eventOperatorManager, MasterConfig masterConfig) {
        this.eventOperatorManager = eventOperatorManager;
        this.eventFireThreadPool =
                ThreadUtils.newDaemonFixedThreadExecutor("EventFireThreadPool", masterConfig.getExecThreads());
    }

    @Override
    public CompletableFuture<Integer> fireActiveEvents(IEventRepository eventRepository) {
        if (eventRepository.getEventSize() == 0) {
            return CompletableFuture.completedFuture(0);
        }
        return CompletableFuture.supplyAsync(() -> {
            int fireCount = 0;
            for (;;) {
                IEvent event = eventRepository.poolEvent();
                if (event == null) {
                    break;
                }

                if (event instanceof IAsyncEvent) {
                    fireAsyncEvent(event);
                    fireCount++;
                    continue;
                }
                try {
                    fireSyncEvent(event);
                    fireCount++;
                } catch (Exception ex) {
                    if (ExceptionUtils.isDatabaseConnectedFailedException(ex)) {
                        // If the event is failed due to cannot connect to DB, we should retry it
                        eventRepository.storeEventToHead(event);
                    }
                    throw ex;
                }
            }
            return fireCount;
        }, eventFireThreadPool);
    }

    private void fireAsyncEvent(IEvent event) {
        CompletableFuture.runAsync(() -> {
            log.info("Begin fire IAsyncEvent: {}", event);
            eventOperatorManager.getEventOperator(event).handleEvent(event);
            log.info("Success fire IAsyncEvent: {}", event);
        }, eventFireThreadPool).exceptionally(ex -> {
            log.error("Failed to fire IAsyncEvent: {}", event, ex);
            return null;
        });
    }

    private void fireSyncEvent(IEvent event) {
        log.info("Begin fire SyncEvent: {}", event);
        eventOperatorManager.getEventOperator(event).handleEvent(event);
        log.info("Success fire SyncEvent: {}", event);
    }

}
