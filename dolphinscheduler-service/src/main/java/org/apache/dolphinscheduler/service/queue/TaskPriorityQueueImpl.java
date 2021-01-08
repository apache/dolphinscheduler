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

import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;

import java.util.concurrent.PriorityBlockingQueue;

import org.springframework.stereotype.Service;

/**
 * A singleton of a task queue implemented with zookeeper
 * tasks queue implementation
 */
@Service
public class TaskPriorityQueueImpl implements TaskPriorityQueue<TaskPriority> {
    /**
     * queue size
     */
    private static final Integer QUEUE_MAX_SIZE = 3000;

    /**
     * queue
     */
    private PriorityBlockingQueue<TaskPriority> queue = new PriorityBlockingQueue<>(QUEUE_MAX_SIZE);

    /**
     * put task takePriorityInfo
     *
     * @param taskPriorityInfo takePriorityInfo
     * @throws TaskPriorityQueueException
     */
    @Override
    public void put(TaskPriority taskPriorityInfo) throws TaskPriorityQueueException {
        queue.put(taskPriorityInfo);
    }

    /**
     * take taskInfo
     *
     * @return taskInfo
     * @throws TaskPriorityQueueException
     */
    @Override
    public TaskPriority take() throws TaskPriorityQueueException, InterruptedException {
        return queue.take();
    }

    /**
     * queue size
     *
     * @return size
     * @throws TaskPriorityQueueException
     */
    @Override
    public int size() throws TaskPriorityQueueException {
        return queue.size();
    }
}
