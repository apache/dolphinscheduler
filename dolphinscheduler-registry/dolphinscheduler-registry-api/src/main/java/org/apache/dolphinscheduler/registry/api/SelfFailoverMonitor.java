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

package org.apache.dolphinscheduler.registry.api;

import org.apache.dolphinscheduler.common.model.ServerIdentity;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.registry.api.utils.RegistryUtils;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/** Stops this process if another server has completed its failover. */
@Slf4j
public class SelfFailoverMonitor implements AutoCloseable {

    private final RegistryClient registryClient;
    private final String failoverPath;
    private final long checkIntervalMillis;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> checkFuture;
    private boolean closed;

    public SelfFailoverMonitor(RegistryClient registryClient, ServerIdentity identity, Duration checkInterval) {
        this(registryClient, identity, checkInterval,
                ThreadUtils.newSingleDaemonScheduledExecutorService("SelfFailoverMonitor"));
    }

    SelfFailoverMonitor(RegistryClient registryClient, ServerIdentity identity, Duration checkInterval,
                        ScheduledExecutorService executor) {
        this.registryClient = registryClient;
        this.failoverPath = RegistryUtils.getFailoveredNodePath(identity.getAddress(),
                identity.getStartupTime(), identity.getProcessId());
        this.checkIntervalMillis = checkInterval.toMillis();
        if (checkIntervalMillis <= 0) {
            throw new IllegalArgumentException("Failover check interval must be positive");
        }
        this.executor = executor;
    }

    public synchronized void start() {
        checkFuture = executor.scheduleWithFixedDelay(this::checkFailoverMark, 0,
                checkIntervalMillis, TimeUnit.MILLISECONDS);
    }

    private synchronized void checkFailoverMark() {
        if (closed) {
            return;
        }
        try {
            if (registryClient.exists(failoverPath)) {
                log.warn("The current process has been failovered: {}, will stop myself", failoverPath);
                registryClient.getStoppable().stop("The current process has been failovered: " + failoverPath);
            }
        } catch (Exception ex) {
            log.error("Failed to check failover mark: {}", failoverPath, ex);
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (checkFuture != null) {
            checkFuture.cancel(false);
        }
        // stop() can close the server on this thread; do not interrupt its shutdown sequence.
        executor.shutdown();
    }
}
