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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.cache.StreamTaskInstanceExecCacheManager;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventExecuteService extends BaseDaemonThread {

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private StreamTaskInstanceExecCacheManager streamTaskInstanceExecCacheManager;

    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Autowired
    private StreamTaskExecuteThreadPool streamTaskExecuteThreadPool;

    protected EventExecuteService() {
        super("EventServiceStarted");
    }

    @Override
    public synchronized void start() {
        log.info("Master Event execute service starting");
        super.start();
        log.info("Master Event execute service started");
    }

    @Override
    public void run() {
        while (!ServerLifeCycleManager.isStopped()) {
            try {
                workflowEventHandler();
                streamTaskEventHandler();
                TimeUnit.MILLISECONDS.sleep(Constants.SLEEP_TIME_MILLIS_SHORT);
            } catch (InterruptedException interruptedException) {
                log.warn("Master event service interrupted, will exit this loop", interruptedException);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Master event execute service error", e);
            }
        }
    }

    private void workflowEventHandler() {
        for (WorkflowExecuteRunnable workflowExecuteThread : this.processInstanceExecCacheManager.getAll()) {
            try {
                LogUtils.setWorkflowInstanceIdMDC(
                        workflowExecuteThread.getWorkflowExecuteContext().getWorkflowInstance().getId());
                workflowExecuteThreadPool.executeEvent(workflowExecuteThread);

            } finally {
                LogUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

    private void streamTaskEventHandler() {
        for (StreamTaskExecuteRunnable streamTaskExecuteRunnable : streamTaskInstanceExecCacheManager.getAll()) {
            try {
                LogUtils.setTaskInstanceIdMDC(streamTaskExecuteRunnable.getTaskInstance().getId());
                streamTaskExecuteThreadPool.executeEvent(streamTaskExecuteRunnable);

            } finally {
                LogUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }
}
