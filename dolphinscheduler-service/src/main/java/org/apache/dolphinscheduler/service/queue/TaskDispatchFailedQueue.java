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

package org.apache.dolphinscheduler.service.queue;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service(value = Constants.TASK_DISPATCH_FAILED_QUEUE)
public class TaskDispatchFailedQueue implements TaskPriorityQueue<TaskPriority> {

    /**
     * task dispatch failed queue
     */
    private final PriorityBlockingQueue<TaskPriority> dispatchFailedQueue = new PriorityBlockingQueue<>(1000);

    @Override
    public void put(TaskPriority taskInfo) {
        dispatchFailedQueue.put(taskInfo);
    }

    @Override
    public TaskPriority take() throws TaskPriorityQueueException, InterruptedException {
        return dispatchFailedQueue.take();
    }

    @Override
    public TaskPriority poll(long timeout, TimeUnit unit) throws TaskPriorityQueueException, InterruptedException {
        return dispatchFailedQueue.poll(timeout, unit);
    }

    @Override
    public int size() throws TaskPriorityQueueException {
        return dispatchFailedQueue.size();
    }

    @Override
    public void clear() {
        dispatchFailedQueue.clear();
    }
}
