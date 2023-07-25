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
import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.event.WorkflowEvent;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventHandleError;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventHandleException;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventHandler;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventQueue;
import org.apache.dolphinscheduler.server.master.event.WorkflowEventType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkflowEventLooper extends BaseDaemonThread implements AutoCloseable {

    @Autowired
    private WorkflowEventQueue workflowEventQueue;

    @Autowired
    private List<WorkflowEventHandler> workflowEventHandlerList;

    private final Map<WorkflowEventType, WorkflowEventHandler> workflowEventHandlerMap = new HashMap<>();

    private final AtomicBoolean RUNNING_FLAG = new AtomicBoolean(false);

    protected WorkflowEventLooper() {
        super("WorkflowEventLooper");
    }

    @PostConstruct
    public void init() {
        workflowEventHandlerList.forEach(
                workflowEventHandler -> workflowEventHandlerMap.put(workflowEventHandler.getHandleWorkflowEventType(),
                        workflowEventHandler));
    }

    @Override
    public synchronized void start() {
        if (!RUNNING_FLAG.compareAndSet(false, true)) {
            log.error("WorkflowEventLooper thread has already started, will not start again");
            return;
        }
        log.info("WorkflowEventLooper starting...");
        super.start();
        log.info("WorkflowEventLooper started...");
    }

    public void run() {
        WorkflowEvent workflowEvent;
        while (RUNNING_FLAG.get()) {
            try {
                workflowEvent = workflowEventQueue.poolEvent();
            } catch (InterruptedException e) {
                log.warn("WorkflowEventLooper thread is interrupted, will close this loop");
                Thread.currentThread().interrupt();
                break;
            }
            try (
                    LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                            LogUtils.setWorkflowInstanceIdMDC(workflowEvent.getWorkflowInstanceId())) {
                log.info("Begin to handle WorkflowEvent: {}", workflowEvent);
                WorkflowEventHandler workflowEventHandler =
                        workflowEventHandlerMap.get(workflowEvent.getWorkflowEventType());
                workflowEventHandler.handleWorkflowEvent(workflowEvent);
                log.info("Success handle WorkflowEvent: {}", workflowEvent);
            } catch (WorkflowEventHandleException workflowEventHandleException) {
                log.error("Handle workflow event failed, will retry again: {}", workflowEvent,
                        workflowEventHandleException);
                workflowEventQueue.addEvent(workflowEvent);
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            } catch (WorkflowEventHandleError workflowEventHandleError) {
                log.error("Handle workflow event error, will drop this event: {}",
                        workflowEvent,
                        workflowEventHandleError);
            } catch (Exception unknownException) {
                log.error("Handle workflow event failed, get a unknown exception, will retry again: {}", workflowEvent,
                        unknownException);
                workflowEventQueue.addEvent(workflowEvent);
                ThreadUtils.sleep(Constants.SLEEP_TIME_MILLIS);
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (!RUNNING_FLAG.compareAndSet(true, false)) {
            log.info("WorkflowEventLooper thread is not start, no need to close");
            return;
        }
        log.info("WorkflowEventLooper is closing...");
        this.interrupt();
        log.info("WorkflowEventLooper closed...");
    }
}
