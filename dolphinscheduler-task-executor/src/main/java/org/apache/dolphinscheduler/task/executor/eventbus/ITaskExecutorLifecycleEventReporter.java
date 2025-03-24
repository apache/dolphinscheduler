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

package org.apache.dolphinscheduler.task.executor.eventbus;

import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The interface of task executor event reporter, used to report task execution events to the master.
 * <p> Once the task executor lifecycle changed, need to report the event to the master.
 */
public interface ITaskExecutorLifecycleEventReporter extends AutoCloseable {

    /**
     * Start the reporter.
     */
    void start();

    /**
     * Receive the task executor event, which need to be reported to the master.
     * <p> This method is not sync, will only put the event into a pending queue and send the event in another thread.
     */
    void reportTaskExecutorLifecycleEvent(final IReportableTaskExecutorLifecycleEvent reportableTaskExecutorLifecycleEvent);

    /**
     * Receive the task execution event ACK, which means the master has received the event.
     * <p> Once the reporter received the ACK, will remove the event from the pending queue, otherwise will retry to send the event to master at interval.
     */
    void receiveTaskExecutorLifecycleEventACK(final TaskExecutorLifecycleEventAck taskExecutorLifecycleEventAck);

    /**
     * Reassign the workflow instance host of the IReportableTaskExecutorLifecycleEvent.
     * <p> This method is used to reassign the workflow instance host of the IReportableTaskExecutorLifecycleEvent, once the workflow's host changed.
     */
    boolean reassignWorkflowInstanceHost(int taskInstanceId, String workflowHost);

    /**
     * Shutdown the reporter.
     */
    @Override
    void close();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class TaskExecutorLifecycleEventAck {

        private int taskExecutorId;

        private TaskExecutorLifecycleEventType taskExecutorLifecycleEventType;
    }

}
