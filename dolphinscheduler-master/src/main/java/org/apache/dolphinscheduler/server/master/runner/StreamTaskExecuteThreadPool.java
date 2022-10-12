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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Used to execute {@link StreamTaskExecuteRunnable}.
 */
@Component
public class StreamTaskExecuteThreadPool extends ThreadPoolTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(StreamTaskExecuteThreadPool.class);

    @Autowired
    private MasterConfig masterConfig;

    @PostConstruct
    private void init() {
        this.setDaemon(true);
        this.setThreadNamePrefix("StreamTaskExecuteThread-");
        this.setMaxPoolSize(masterConfig.getExecThreads());
        this.setCorePoolSize(masterConfig.getExecThreads());
    }

    /**
     * Handle the events belong to the given stream task
     */
    public void executeEvent(final StreamTaskExecuteRunnable streamTaskExecuteRunnable) {
        if (!streamTaskExecuteRunnable.isStart() || streamTaskExecuteRunnable.eventSize() == 0) {
            return;
        }
        int taskInstanceId = streamTaskExecuteRunnable.getTaskInstance().getId();
        ListenableFuture<?> future = this.submitListenable(streamTaskExecuteRunnable::handleEvents);
        future.addCallback(new ListenableFutureCallback() {
            @Override
            public void onFailure(Throwable ex) {
                LoggerUtils.setTaskInstanceIdMDC(taskInstanceId);
                logger.error("Stream task instance events handle failed", ex);
                LoggerUtils.removeTaskInstanceIdMDC();
            }

            @Override
            public void onSuccess(Object result) {
                LoggerUtils.setTaskInstanceIdMDC(taskInstanceId);
                logger.info("Stream task instance is finished.");
                LoggerUtils.removeTaskInstanceIdMDC();
            }
        });
    }
}
