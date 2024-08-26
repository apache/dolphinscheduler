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

import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.queue.DelayEntry;
import org.apache.dolphinscheduler.server.master.runner.queue.PriorityDelayQueue;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * The class is used to store {@link ITaskExecutionRunnable} which needs to be dispatched. The {@link ITaskExecutionRunnable}
 * will be stored in {@link PriorityDelayQueue}, if the {@link ITaskExecutionRunnable}'s delay time is 0, then it will be
 * consumed by {@link GlobalTaskDispatchWaitingQueueLooper}.
 * <p>
 * The order of {@link ITaskExecutionRunnable} in the {@link PriorityDelayQueue} is determined by {@link ITaskExecutionRunnable#compareTo}.
 */
@Slf4j
@Component
public class GlobalTaskDispatchWaitingQueue {

    private final Set<Integer> waitingTaskInstanceIds = ConcurrentHashMap.newKeySet();
    private final PriorityDelayQueue<DelayEntry<ITaskExecutionRunnable>> priorityDelayQueue =
            new PriorityDelayQueue<>();

    /**
     * Submit a {@link ITaskExecutionRunnable} with delay time 0, it will be consumed immediately.
     */
    public synchronized void dispatchTaskExecuteRunnable(ITaskExecutionRunnable ITaskExecutionRunnable) {
        dispatchTaskExecuteRunnableWithDelay(ITaskExecutionRunnable, 0);
    }

    /**
     * Submit a {@link ITaskExecutionRunnable} with delay time, if the delay time <= 0 then it can be consumed.
     */
    public synchronized void dispatchTaskExecuteRunnableWithDelay(ITaskExecutionRunnable taskExecutionRunnable,
                                                                  long delayTimeMills) {
        waitingTaskInstanceIds.add(taskExecutionRunnable.getTaskInstance().getId());
        priorityDelayQueue.add(new DelayEntry<>(delayTimeMills, taskExecutionRunnable));
    }

    /**
     * Consume {@link ITaskExecutionRunnable} from the {@link PriorityDelayQueue}, only the delay time <= 0 can be consumed.
     */
    @SneakyThrows
    public ITaskExecutionRunnable takeTaskExecuteRunnable() {
        ITaskExecutionRunnable taskExecutionRunnable = priorityDelayQueue.take().getData();
        while (!markTaskExecutionRunnableRemoved(taskExecutionRunnable)) {
            taskExecutionRunnable = priorityDelayQueue.take().getData();
        }
        return taskExecutionRunnable;
    }

    public int getWaitingDispatchTaskNumber() {
        return waitingTaskInstanceIds.size();
    }

    public synchronized boolean markTaskExecutionRunnableRemoved(ITaskExecutionRunnable taskExecutionRunnable) {
        return waitingTaskInstanceIds.remove(taskExecutionRunnable.getTaskInstance().getId());
    }
}
