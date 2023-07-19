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

package org.apache.dolphinscheduler.server.master.processor.queue;

import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.remote.command.StateEventResponse;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.event.StateEvent;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;

@Component
@Slf4j
public class StateEventResponseService {

    /**
     * attemptQueue
     */
    private final BlockingQueue<StateEvent> eventQueue = new LinkedBlockingQueue<>(5000);

    /**
     * task response worker
     */
    private Thread responseWorker;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @PostConstruct
    public void start() {
        this.responseWorker = new StateEventResponseWorker();
        this.responseWorker.start();
    }

    @PreDestroy
    public void stop() {
        this.responseWorker.interrupt();
        if (!eventQueue.isEmpty()) {
            List<StateEvent> remainEvents = new ArrayList<>(eventQueue.size());
            eventQueue.drainTo(remainEvents);
            for (StateEvent event : remainEvents) {
                try (
                        final LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                                LogUtils.setWorkflowAndTaskInstanceIDMDC(event.getProcessInstanceId(),
                                        event.getTaskInstanceId())) {
                    this.persist(event);
                }
            }
        }
    }

    /**
     * put task to attemptQueue
     */
    public void addStateChangeEvent(StateEvent stateEvent) {
        try {
            // check the event is validated
            eventQueue.put(stateEvent);
        } catch (InterruptedException e) {
            log.error("Put state event : {} error", stateEvent, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * task worker thread
     */
    class StateEventResponseWorker extends BaseDaemonThread {

        protected StateEventResponseWorker() {
            super("StateEventResponseWorker");
        }

        @Override
        public void run() {
            log.info("State event loop service started");
            while (!ServerLifeCycleManager.isStopped()) {
                StateEvent stateEvent;
                try {
                    stateEvent = eventQueue.take();
                } catch (InterruptedException e) {
                    log.warn("State event loop service interrupted, will stop loop");
                    Thread.currentThread().interrupt();
                    break;
                }
                try (
                        final LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                                LogUtils.setWorkflowAndTaskInstanceIDMDC(stateEvent.getProcessInstanceId(),
                                        stateEvent.getTaskInstanceId())) {
                    // if not task , blocking here
                    persist(stateEvent);
                }
            }
            log.info("State event loop service stopped");
        }
    }

    private void writeResponse(StateEvent stateEvent) {
        Channel channel = stateEvent.getChannel();
        if (channel != null) {
            StateEventResponse command = new StateEventResponse(stateEvent.getKey());
            channel.writeAndFlush(command.convert2Command());
        }
    }

    private void persist(StateEvent stateEvent) {
        try {
            if (!this.processInstanceExecCacheManager.contains(stateEvent.getProcessInstanceId())) {
                log.warn("Persist event into workflow execute thread error, "
                        + "cannot find the workflow instance from cache manager, event: {}", stateEvent);
                writeResponse(stateEvent);
                return;
            }

            WorkflowExecuteRunnable workflowExecuteThread =
                    this.processInstanceExecCacheManager.getByProcessInstanceId(stateEvent.getProcessInstanceId());
            // We will refresh the task instance status first, if the refresh failed the event will not be removed
            switch (stateEvent.getType()) {
                case TASK_STATE_CHANGE:
                    workflowExecuteThread.refreshTaskInstance(stateEvent.getTaskInstanceId());
                    break;
                case PROCESS_STATE_CHANGE:
                    workflowExecuteThread.refreshProcessInstance(stateEvent.getProcessInstanceId());
                    break;
                default:
            }
            workflowExecuteThreadPool.submitStateEvent(stateEvent);
            // this response is not needed.
            writeResponse(stateEvent);
        } catch (Exception e) {
            log.error("Persist event queue error, event: {}", stateEvent, e);
        }
    }

    public void addEvent2WorkflowExecute(StateEvent stateEvent) {
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }
}
