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

package org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event;

import static com.google.common.base.Preconditions.checkState;

import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.AbstractTaskLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.TaskLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import java.util.concurrent.TimeUnit;

import lombok.Getter;

@Getter
public class TaskTimeoutLifecycleEvent extends AbstractTaskLifecycleEvent {

    private final ITaskExecutionRunnable taskExecutionRunnable;

    protected TaskTimeoutLifecycleEvent(final ITaskExecutionRunnable taskExecutionRunnable,
                                        final long timeout) {
        super(timeout);
        this.taskExecutionRunnable = taskExecutionRunnable;
    }

    public static TaskTimeoutLifecycleEvent of(final ITaskExecutionRunnable taskExecutionRunnable) {
        final TaskDefinition taskDefinition = taskExecutionRunnable.getTaskDefinition();
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        checkState(taskDefinition != null, "The task instance must be initialized before retrying.");

        final int timeout = taskDefinition.getTimeout();
        checkState(timeout >= 0, "The task timeout: %s must >=0 minutes", timeout);

        long delayTime = System.currentTimeMillis() - taskInstance.getSubmitTime().getTime()
                + TimeUnit.MINUTES.toMillis(timeout);
        return new TaskTimeoutLifecycleEvent(taskExecutionRunnable, delayTime);
    }

    @Override
    public ILifecycleEventType getEventType() {
        return TaskLifecycleEventType.TIMEOUT;
    }

    @Override
    public String toString() {
        return "TaskRetryEvent{" +
                "task=" + taskExecutionRunnable.getName() +
                ", timeout=" + delayTime +
                '}';
    }
}
