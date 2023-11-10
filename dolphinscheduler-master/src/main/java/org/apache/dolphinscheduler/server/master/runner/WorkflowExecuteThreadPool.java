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

import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.StateEvent;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Used to execute {@link WorkflowExecuteRunnable}.
 */
@Component
@Slf4j
public class WorkflowExecuteThreadPool extends ThreadPoolTaskExecutor {

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private StateWheelExecuteThread stateWheelExecuteThread;

    /**
     * multi-thread filter, avoid handling workflow at the same time
     */
    private ConcurrentHashMap<Integer, WorkflowExecuteRunnable> multiThreadFilterMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        this.setDaemon(true);
        this.setThreadNamePrefix("WorkflowExecuteThread-");
        this.setMaxPoolSize(masterConfig.getExecThreads());
        this.setCorePoolSize(masterConfig.getExecThreads());
    }

    /**
     * submit state event
     */
    public void submitStateEvent(StateEvent stateEvent) {
        WorkflowExecuteRunnable workflowExecuteThread =
                processInstanceExecCacheManager.getByProcessInstanceId(stateEvent.getProcessInstanceId());
        if (workflowExecuteThread == null) {
            log.warn("Submit state event error, cannot from workflowExecuteThread from cache manager, stateEvent:{}",
                    stateEvent);
            return;
        }
        workflowExecuteThread.addStateEvent(stateEvent);
        log.info("Submit state event success, stateEvent: {}", stateEvent);
    }

    /**
     * Handle the events belong to the given workflow.
     */
    public void executeEvent(final WorkflowExecuteRunnable workflowExecuteThread) {
        if (!workflowExecuteThread.isStart() || workflowExecuteThread.eventSize() == 0) {
            return;
        }
        IWorkflowExecuteContext workflowExecuteRunnableContext =
                workflowExecuteThread.getWorkflowExecuteContext();
        Integer workflowInstanceId = workflowExecuteRunnableContext.getWorkflowInstance().getId();

        if (multiThreadFilterMap.containsKey(workflowInstanceId)) {
            log.debug("The workflow has been executed by another thread");
            return;
        }
        multiThreadFilterMap.put(workflowInstanceId, workflowExecuteThread);
        ListenableFuture<?> future = this.submitListenable(workflowExecuteThread::handleEvents);
        future.addCallback(new ListenableFutureCallback() {

            @Override
            public void onFailure(Throwable ex) {
                LogUtils.setWorkflowInstanceIdMDC(workflowInstanceId);
                try {
                    log.error("Workflow instance events handle failed", ex);
                    multiThreadFilterMap.remove(workflowInstanceId);
                } finally {
                    LogUtils.removeWorkflowInstanceIdMDC();
                }
            }

            @Override
            public void onSuccess(Object result) {
                try {
                    LogUtils.setWorkflowInstanceIdMDC(
                            workflowExecuteThread.getWorkflowExecuteContext().getWorkflowInstance().getId());
                    if (workflowExecuteThread.workFlowFinish() && workflowExecuteThread.eventSize() == 0) {
                        stateWheelExecuteThread
                                .removeProcess4TimeoutCheck(workflowExecuteThread.getWorkflowExecuteContext()
                                        .getWorkflowInstance().getId());
                        processInstanceExecCacheManager.removeByProcessInstanceId(workflowInstanceId);
                        log.info("Workflow instance is finished.");
                    }
                } catch (Exception e) {
                    log.error("Workflow instance is finished, but notify changed error", e);
                } finally {
                    // make sure the process has been removed from multiThreadFilterMap
                    multiThreadFilterMap.remove(workflowInstanceId);
                    LogUtils.removeWorkflowInstanceIdMDC();
                }
            }
        });
    }

}
