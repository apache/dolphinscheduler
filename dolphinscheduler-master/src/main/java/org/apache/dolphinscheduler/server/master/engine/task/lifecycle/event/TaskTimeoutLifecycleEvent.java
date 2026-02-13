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

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.AbstractTaskLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.TaskLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import java.util.concurrent.TimeUnit;

import lombok.Getter;

@Getter
public class TaskTimeoutLifecycleEvent extends AbstractTaskLifecycleEvent {

    private final ITaskExecutionRunnable taskExecutionRunnable;

    private final TaskTimeoutStrategy timeoutStrategy;

    protected TaskTimeoutLifecycleEvent(final ITaskExecutionRunnable taskExecutionRunnable,
                                        final TaskTimeoutStrategy timeoutStrategy,
                                        final long timeout) {
        super(timeout);
        this.timeoutStrategy = timeoutStrategy;
        this.taskExecutionRunnable = taskExecutionRunnable;
    }

    public static TaskTimeoutLifecycleEvent of(final ITaskExecutionRunnable taskExecutionRunnable,
                                               final TaskTimeoutStrategy timeoutStrategy,
                                               final long timeoutInMinutes) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();

        checkState(timeoutStrategy != null, "The task timeoutStrategy must not be null");
        checkState(timeoutInMinutes >= 0, "The task timeout: %s must >=0 minutes", timeoutInMinutes);

        long delayTime = System.currentTimeMillis() - taskInstance.getSubmitTime().getTime()
                + TimeUnit.MINUTES.toMillis(timeoutInMinutes);
        return new TaskTimeoutLifecycleEvent(taskExecutionRunnable, timeoutStrategy, delayTime);
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
