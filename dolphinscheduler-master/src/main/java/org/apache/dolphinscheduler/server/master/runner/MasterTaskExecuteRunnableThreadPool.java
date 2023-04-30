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

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.execute.MasterTaskExecuteRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

@Slf4j
@Component
public class MasterTaskExecuteRunnableThreadPool {

    @Autowired
    private MasterConfig masterConfig;

    private static final Map<Integer, MasterTaskExecuteRunnable> SUBMITTED_MASTER_TASK_MAP = new ConcurrentHashMap<>();

    private ListeningExecutorService listeningExecutorService;

    public synchronized void start() {
        log.info("MasterTaskExecuteRunnableThreadPool starting...");
        this.listeningExecutorService = MoreExecutors.listeningDecorator(ThreadUtils.newDaemonFixedThreadExecutor(
                "MasterTaskExecuteRunnableThread", masterConfig.getMasterTaskExecuteThreadPoolSize()));
        log.info("MasterTaskExecuteRunnableThreadPool started...");
    }

    public void submitMasterTaskExecuteRunnable(MasterTaskExecuteRunnable masterTaskExecuteRunnable) {
        ListenableFuture<?> future = listeningExecutorService.submit(masterTaskExecuteRunnable);
        Futures.addCallback(future, new MasterTaskExecuteCallback(masterTaskExecuteRunnable),
                this.listeningExecutorService);
        SUBMITTED_MASTER_TASK_MAP.put(masterTaskExecuteRunnable.getTaskExecutionContext().getTaskInstanceId(),
                masterTaskExecuteRunnable);
    }

    public MasterTaskExecuteRunnable getMasterTaskExecuteRunnable(Integer taskInstanceId) {
        return SUBMITTED_MASTER_TASK_MAP.get(taskInstanceId);
    }

    private static class MasterTaskExecuteCallback implements FutureCallback {

        private MasterTaskExecuteRunnable masterTaskExecuteRunnable;

        public MasterTaskExecuteCallback(MasterTaskExecuteRunnable masterTaskExecuteRunnable) {
            this.masterTaskExecuteRunnable = masterTaskExecuteRunnable;
        }

        @Override
        public void onSuccess(Object result) {
            log.info("MasterTaskExecuteRunnable execute success, will remove this task");
            SUBMITTED_MASTER_TASK_MAP.remove(masterTaskExecuteRunnable.getTaskExecutionContext().getTaskInstanceId());
        }

        @Override
        public void onFailure(Throwable t) {
            log.info("MasterTaskExecuteRunnable execute failed, will remove this task");
            SUBMITTED_MASTER_TASK_MAP.remove(masterTaskExecuteRunnable.getTaskExecutionContext().getTaskInstanceId());
        }
    }

}
