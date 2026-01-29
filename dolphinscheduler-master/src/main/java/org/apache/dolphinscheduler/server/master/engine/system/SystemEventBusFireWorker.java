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

package org.apache.dolphinscheduler.server.master.engine.system;

import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.master.engine.system.event.AbstractSystemEvent;
import org.apache.dolphinscheduler.server.master.engine.system.event.ISystemEventHandler;
import org.apache.dolphinscheduler.server.master.failover.FailoverCoordinator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class SystemEventBusFireWorker extends BaseDaemonThread implements AutoCloseable {

    @Autowired
    private SystemEventBus systemEventBus;

    @Autowired
    private FailoverCoordinator failoverCoordinator;

    @Autowired
    private List<ISystemEventHandler> systemEventHandlers;

    private static boolean flag = false;

    public SystemEventBusFireWorker() {
        super("SystemEventBusFireWorker");
    }

    @Override
    public void start() {
        flag = true;
        super.start();
        log.info("SystemEventBusFireWorker started");
    }

    @Override
    public void run() {
        while (flag) {
            final AbstractSystemEvent systemEvent;
            try {
                systemEvent = systemEventBus.take();
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                log.warn("SystemEventBusFireWorker has been interrupted", interruptedException);
                break;
            }
            if (ServerLifeCycleManager.isStopped()) {
                log.info("SystemEventBusFireWorker has been stopped");
                break;
            }
            try {
                fireSystemEvent(systemEvent);
            } catch (Exception ex) {
                // Put the event back to eventbus.
                systemEventBus.publish(systemEvent);
                log.error("Fire SystemEvent: {} failed", systemEvent, ex);
                ThreadUtils.sleep(10_000);
            }
        }
    }

    private void fireSystemEvent(final AbstractSystemEvent systemEvent) {
        final StopWatch stopWatch = StopWatch.createStarted();
        final List<ISystemEventHandler> matchedSystemEventHandlers = systemEventHandlers
                .stream()
                .filter(systemEventHandler -> systemEventHandler.matchState() == systemEvent.getEventType())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(matchedSystemEventHandlers)) {
            log.error("No matched SystemEventHandler for SystemEvent: {}", systemEvent);
            return;
        }
        matchedSystemEventHandlers.forEach(systemEventHandler -> systemEventHandler.handle(systemEvent));
        stopWatch.stop();
        log.info("Fire SystemEvent: {} cost: {} ms", systemEvent, stopWatch.getTime());
    }

    @Override
    public void close() {
        flag = false;
        log.info("SystemEventBusFireWorker closed");
    }
}
