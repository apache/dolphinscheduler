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

import io.netty.channel.Channel;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.remote.command.StateEventResponseCommand;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.event.StateEvent;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class StateEventResponseService {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(StateEventResponseService.class);

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
                try {
                    LoggerUtils.setWorkflowAndTaskInstanceIDMDC(event.getProcessInstanceId(),
                            event.getTaskInstanceId());
                    this.persist(event);

                } finally {
                    LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
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
            logger.error("Put state event : {} error", stateEvent, e);
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
            logger.info("State event loop service started");
            while (!ServerLifeCycleManager.isStopped()) {
                try {
                    // if not task , blocking here
                    StateEvent stateEvent = eventQueue.take();
                    LoggerUtils.setWorkflowAndTaskInstanceIDMDC(stateEvent.getProcessInstanceId(),
                            stateEvent.getTaskInstanceId());
                    persist(stateEvent);
                } catch (InterruptedException e) {
                    logger.warn("State event loop service interrupted, will stop this loop", e);
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
                }
            }
            logger.info("State event loop service stopped");
        }
    }

    private void writeResponse(StateEvent stateEvent) {
        Channel channel = stateEvent.getChannel();
        if (channel != null) {
            StateEventResponseCommand command = new StateEventResponseCommand(stateEvent.getKey());
            channel.writeAndFlush(command.convert2Command());
        }
    }

    private void persist(StateEvent stateEvent) {
        try {
            if (!this.processInstanceExecCacheManager.contains(stateEvent.getProcessInstanceId())) {
                logger.warn("Persist event into workflow execute thread error, "
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
            logger.error("Persist event queue error, event: {}", stateEvent, e);
        }
    }

    public void addEvent2WorkflowExecute(StateEvent stateEvent) {
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }
}
