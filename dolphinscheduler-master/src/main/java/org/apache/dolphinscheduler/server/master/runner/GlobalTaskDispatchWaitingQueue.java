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

import org.apache.dolphinscheduler.server.master.runner.queue.DelayEntry;
import org.apache.dolphinscheduler.server.master.runner.queue.PriorityDelayQueue;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * The class is used to store {@link TaskExecuteRunnable} which needs to be dispatched. The {@link TaskExecuteRunnable}
 * will be stored in {@link PriorityDelayQueue}, if the {@link TaskExecuteRunnable}'s delay time is 0, then it will be
 * consumed by {@link GlobalTaskDispatchWaitingQueueLooper}.
 * <p>
 * The order of {@link TaskExecuteRunnable} in the {@link PriorityDelayQueue} is determined by {@link TaskExecuteRunnable#compareTo}.
 */
@Slf4j
@Component
public class GlobalTaskDispatchWaitingQueue {

    private final PriorityDelayQueue<DelayEntry<TaskExecuteRunnable>> priorityDelayQueue = new PriorityDelayQueue<>();

    /**
     * Submit a {@link TaskExecuteRunnable} with delay time 0, it will be consumed immediately.
     */
    public void dispatchTaskExecuteRunnable(TaskExecuteRunnable taskExecuteRunnable) {
        dispatchTaskExecuteRunnableWithDelay(taskExecuteRunnable, 0);
    }

    /**
     * Submit a {@link TaskExecuteRunnable} with delay time, if the delay time <= 0 then it can be consumed.
     */
    public void dispatchTaskExecuteRunnableWithDelay(TaskExecuteRunnable taskExecuteRunnable, long delayTimeMills) {
        priorityDelayQueue.add(new DelayEntry<>(delayTimeMills, taskExecuteRunnable));
    }

    /**
     * Consume {@link TaskExecuteRunnable} from the {@link PriorityDelayQueue}, only the delay time <= 0 can be consumed.
     */
    @SneakyThrows
    public TaskExecuteRunnable takeTaskExecuteRunnable() {
        return priorityDelayQueue.take().getData();
    }

    public int getWaitingDispatchTaskNumber() {
        return priorityDelayQueue.size();
    }

}
