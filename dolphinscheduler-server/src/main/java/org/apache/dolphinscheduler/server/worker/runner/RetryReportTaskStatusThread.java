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

package org.apache.dolphinscheduler.server.worker.runner;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.server.worker.cache.ResponceCache;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.processor.TaskCallbackService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Retry Report Task Status Thread
 */
@Component
public class RetryReportTaskStatusThread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(RetryReportTaskStatusThread.class);

    @Autowired
    WorkerConfig workerConfig;

    /**
     * task callback service
     */
    private final TaskCallbackService taskCallbackService;

    public void start() {
        Thread thread = new Thread(this, "RetryReportTaskStatusThread");
        thread.setDaemon(true);
        thread.start();
    }

    public RetryReportTaskStatusThread() {
        this.taskCallbackService = SpringApplicationContext.getBean(TaskCallbackService.class);
    }

    /**
     * retry ack/response
     */
    @Override
    public void run() {
        ResponceCache responceCache = ResponceCache.get();
        long interval = workerConfig.getRetryReportTaskStatusInterval() * Constants.SLEEP_TIME_MILLIS * 60L;
        while (Stopper.isRunning()) {
            ThreadUtils.sleep(60 * Constants.SLEEP_TIME_MILLIS);
            long nowTimeMillis = System.currentTimeMillis();
            try {
                retrySendCommand(responceCache.getAckCache(), interval, nowTimeMillis);
                retrySendCommand(responceCache.getResponseCache(), interval, nowTimeMillis);
                retrySendCommand(responceCache.getKillResponseCache(), interval, nowTimeMillis);
                retrySendCommand(responceCache.getRecallCache(), interval, nowTimeMillis);
            } catch (Exception e) {
                logger.warn("retry report task status error", e);
            }
        }
    }

    private void retrySendCommand(Map<Integer, Command> cache, long interval, long nowTimeMillis) {
        for (Map.Entry<Integer, Command> entry : cache.entrySet()) {
            Command command = entry.getValue();
            if (nowTimeMillis - command.getGenCommandTimeMillis() > interval) {
                Integer taskInstanceId = entry.getKey();
                taskCallbackService.sendResult(taskInstanceId, command);
                logger.info("retry send command successfully, the command type {}, the task id:{}", command.getType(),taskInstanceId);
            }
        }
    }
}
