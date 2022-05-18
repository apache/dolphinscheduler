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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Task instances priority queue implementation
 * All the task instances are in the same process instance.
 */
public class PeerTaskInstancePriorityQueue implements TaskPriorityQueue<TaskInstance> {
    /**
     * queue size
     */
    private static final Integer QUEUE_MAX_SIZE = 3000;

    /**
     * queue
     */
    private PriorityQueue<TaskInstance> queue = new PriorityQueue<>(QUEUE_MAX_SIZE, new TaskInfoComparator());

    /**
     * Lock used for all public operations
     */
    private final ReentrantLock lock = new ReentrantLock(true);

    /**
     * put task instance to priority queue
     *
     * @param taskInstance taskInstance
     * @throws TaskPriorityQueueException
     */
    @Override
    public void put(TaskInstance taskInstance) throws TaskPriorityQueueException {
        queue.add(taskInstance);
    }

    /**
     * take task info
     *
     * @return task instance
     * @throws TaskPriorityQueueException
     */
    @Override
    public TaskInstance take() throws TaskPriorityQueueException {
        return queue.poll();
    }

    /**
     * poll task info with timeout
     * <p>
     * WARN: Please use PriorityBlockingQueue if you want to use poll(timeout, unit)
     * because this method of override interface used without considering accuracy of timeout
     *
     * @param timeout
     * @param unit
     * @return
     * @throws TaskPriorityQueueException
     * @throws InterruptedException
     */
    @Override
    public TaskInstance poll(long timeout, TimeUnit unit) throws TaskPriorityQueueException {
        throw new TaskPriorityQueueException("This operation is not currently supported and suggest to use PriorityBlockingQueue if you want！");
    }

    /**
     * peek taskInfo
     *
     * @return task instance
     */
    public TaskInstance peek() {
        return queue.peek();
    }

    /**
     * queue size
     *
     * @return size
     */
    @Override
    public int size() {
        return queue.size();
    }

    /**
     * clear task
     *
     */
    public void clear() {
        queue.clear();
    }

    /**
     * whether contains the task instance
     *
     * @param taskInstance task instance
     * @return true is contains
     */
    public boolean contains(TaskInstance taskInstance) {
        return this.contains(taskInstance.getTaskCode(), taskInstance.getTaskDefinitionVersion());
    }

    public boolean contains(long taskCode, int taskVersion) {
        Iterator<TaskInstance> iterator = this.queue.iterator();
        while (iterator.hasNext()) {
            TaskInstance taskInstance = iterator.next();
            if (taskCode == taskInstance.getTaskCode()
                    && taskVersion == taskInstance.getTaskDefinitionVersion()) {
                return true;
            }
        }
        return false;

    }

    /**
     * remove task
     *
     * @param taskInstance task instance
     * @return true if remove success
     */
    public boolean remove(TaskInstance taskInstance) {
        return queue.remove(taskInstance);
    }

    /**
     * get iterator
     *
     * @return Iterator
     */
    public Iterator<TaskInstance> iterator() {
        return queue.iterator();
    }

    /**
     * TaskInfoComparator
     */
    private class TaskInfoComparator implements Comparator<TaskInstance> {

        /**
         * compare o1 o2
         *
         * @param o1 o1
         * @param o2 o2
         * @return compare result
         */
        @Override
        public int compare(TaskInstance o1, TaskInstance o2) {
            if(o1.getTaskInstancePriority().equals(o2.getTaskInstancePriority())){
                // larger number, higher priority
                return Constants.OPPOSITE_VALUE * Integer.compare(o1.getTaskGroupPriority(),o2.getTaskGroupPriority());
            }
            return o1.getTaskInstancePriority().compareTo(o2.getTaskInstancePriority());
        }
    }
}
