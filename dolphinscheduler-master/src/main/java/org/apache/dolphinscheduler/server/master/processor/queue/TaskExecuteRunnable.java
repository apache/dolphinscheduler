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

import org.apache.dolphinscheduler.common.enums.TaskEventType;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.event.TaskEventHandleError;
import org.apache.dolphinscheduler.server.master.event.TaskEventHandleException;
import org.apache.dolphinscheduler.server.master.event.TaskEventHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.extern.slf4j.Slf4j;

/**
 * task execute thread
 */
@Slf4j
public class TaskExecuteRunnable implements Runnable {

    private final int processInstanceId;

    private final ConcurrentLinkedQueue<TaskEvent> events = new ConcurrentLinkedQueue<>();

    private final Map<TaskEventType, TaskEventHandler> taskEventHandlerMap;

    public TaskExecuteRunnable(int processInstanceId, Map<TaskEventType, TaskEventHandler> taskEventHandlerMap) {
        this.processInstanceId = processInstanceId;
        this.taskEventHandlerMap = taskEventHandlerMap;
    }

    @Override
    public void run() {
        while (!this.events.isEmpty()) {
            // we handle the task event belongs to one task serial, so if the event comes in wrong order,
            TaskEvent event = this.events.peek();
            try {
                LogUtils.setWorkflowAndTaskInstanceIDMDC(event.getProcessInstanceId(), event.getTaskInstanceId());
                log.info("Handle task event begin: {}", event);
                taskEventHandlerMap.get(event.getEvent()).handleTaskEvent(event);
                events.remove(event);
                log.info("Handle task event finished: {}", event);
            } catch (TaskEventHandleException taskEventHandleException) {
                // we don't need to resubmit this event, since the worker will resubmit this event
                log.error("Handle task event failed, this event will be retry later, event: {}", event,
                        taskEventHandleException);
            } catch (TaskEventHandleError taskEventHandleError) {
                log.error("Handle task event error, this event will be removed, event: {}", event,
                        taskEventHandleError);
                events.remove(event);
            } catch (Exception unknownException) {
                log.error("Handle task event error, get a unknown exception, this event will be removed, event: {}",
                        event, unknownException);
                events.remove(event);
            } finally {
                LogUtils.removeWorkflowAndTaskInstanceIdMDC();
            }
        }
    }

    public String getKey() {
        return String.valueOf(processInstanceId);
    }

    public int eventSize() {
        return this.events.size();
    }

    public boolean isEmpty() {
        return this.events.isEmpty();
    }

    public Integer getProcessInstanceId() {
        return processInstanceId;
    }

    public boolean addEvent(TaskEvent event) {
        if (event.getProcessInstanceId() != this.processInstanceId) {
            log.warn(
                    "event would be abounded, task instance id:{}, process instance id:{}, this.processInstanceId:{}",
                    event.getTaskInstanceId(), event.getProcessInstanceId(), this.processInstanceId);
            return false;
        }
        return this.events.add(event);
    }

}
