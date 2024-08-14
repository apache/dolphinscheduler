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

package org.apache.dolphinscheduler.common.model;

import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseHeartBeatTask<T extends HeartBeat> extends BaseDaemonThread {

    private static final long DEFAULT_HEARTBEAT_SCAN_INTERVAL = 1_000L;

    private final String threadName;
    private final long heartBeatInterval;

    protected boolean runningFlag;

    protected long lastWriteTime = 0L;

    protected T lastHeartBeat = null;

    public BaseHeartBeatTask(String threadName, long heartBeatInterval) {
        super(threadName);
        this.threadName = threadName;
        this.heartBeatInterval = heartBeatInterval;
        this.runningFlag = true;
    }

    @Override
    public synchronized void start() {
        log.info("Starting {}...", threadName);
        super.start();
        log.info("Started {}, heartBeatInterval: {}...", threadName, heartBeatInterval);
    }

    @Override
    public void run() {
        while (runningFlag) {
            try {
                if (!ServerLifeCycleManager.isRunning()) {
                    log.info("The current server status is {}, will not write heartBeatInfo into registry",
                            ServerLifeCycleManager.getServerStatus());
                    continue;
                }
                T heartBeat = getHeartBeat();
                // if first time or heartBeat status changed, write heartBeatInfo into registry
                if (System.currentTimeMillis() - lastWriteTime >= heartBeatInterval
                        || !lastHeartBeat.getServerStatus().equals(heartBeat.getServerStatus())) {
                    lastHeartBeat = heartBeat;
                    writeHeartBeat(heartBeat);
                    lastWriteTime = System.currentTimeMillis();
                }
            } catch (Exception ex) {
                log.error("{} task execute failed", threadName, ex);
            } finally {
                try {
                    Thread.sleep(DEFAULT_HEARTBEAT_SCAN_INTERVAL);
                } catch (InterruptedException e) {
                    handleInterruptException(e);
                }
            }
        }
    }

    public void shutdown() {
        runningFlag = false;
        log.warn("{} finished...", threadName);
    }

    private void handleInterruptException(InterruptedException ex) {
        log.warn("{} has been interrupted", threadName, ex);
        Thread.currentThread().interrupt();
    }

    public abstract T getHeartBeat();

    public abstract void writeHeartBeat(T heartBeat);
}
