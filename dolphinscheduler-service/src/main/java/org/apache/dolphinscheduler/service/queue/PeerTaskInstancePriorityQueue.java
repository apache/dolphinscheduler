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


import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

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
     * put task instance to priority queue
     *
     * @param taskInstance taskInstance
     * @throws Exception
     */
    public void put(TaskInstance taskInstance) throws Exception {
        queue.add(taskInstance);
    }

    /**
     * take task info
     * @return task instance
     * @throws Exception
     */
    @Override
    public TaskInstance take() throws Exception {
        return queue.poll();
    }

    /**
     * peek taskInfo
     *
     * @return task instance
     * @throws Exception
     */
    public TaskInstance peek() throws Exception {
        return queue.peek();
    }

    /**
     * queue size
     *
     * @return size
     */
    public int size() {
        return queue.size();
    }

    /**
     * whether contains the task instance
     * @param taskInstance task instance
     * @return true is contains
     */
    public boolean contains(TaskInstance taskInstance) {
        return queue.contains(taskInstance);
    }

    /**
     * remove task
     * @param taskInstance task instance
     * @return true if remove success
     * @throws Exception
     */
    public boolean remove(TaskInstance taskInstance) throws Exception {
        return queue.remove(taskInstance);
    }

    /**
     * get iterator
     * @return Iterator
     */
    public Iterator iterator(){
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
            return o1.getTaskInstancePriority().compareTo(o2.getTaskInstancePriority());
        }
    }
}
