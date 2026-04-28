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
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.AbstractTaskLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.TaskLifecycleEventType;

import java.util.concurrent.TimeUnit;

import lombok.Getter;

@Getter
public class TaskRetryLifecycleEvent extends AbstractTaskLifecycleEvent {

    private final ITaskExecution taskExecution;

    protected TaskRetryLifecycleEvent(final ITaskExecution taskExecution,
                                      final long delayTime) {
        super(delayTime);
        this.taskExecution = taskExecution;
    }

    public static TaskRetryLifecycleEvent of(final ITaskExecution taskExecution) {
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        checkState(taskInstance != null, "The task instance must be initialized before retrying.");
        final int delayTime = taskInstance.getRetryInterval();

        final int retryTimes = taskInstance.getRetryTimes();
        final int maxRetryTimes = taskInstance.getMaxRetryTimes();
        checkState(retryTimes < maxRetryTimes,
                "The task retry times: %s must smaller then maxRetryTimes: %s.",
                retryTimes,
                maxRetryTimes);
        final long remainingTime =
                TimeUnit.MINUTES.toMillis(delayTime) + System.currentTimeMillis() - taskInstance.getEndTime().getTime();
        return new TaskRetryLifecycleEvent(taskExecution, remainingTime);
    }

    @Override
    public ILifecycleEventType getEventType() {
        return TaskLifecycleEventType.RETRY;
    }

    @Override
    public String toString() {
        return "TaskRetryLifecycleEvent{" +
                "task=" + taskExecution.getName() +
                ", delayTime=" + delayTime + "/ms" +
                '}';
    }
}
