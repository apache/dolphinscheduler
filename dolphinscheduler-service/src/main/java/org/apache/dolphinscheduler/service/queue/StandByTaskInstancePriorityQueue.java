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
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

/**
 * Task instances priority queue implementation
 * All the task instances are in the same process instance.
 */
public class StandByTaskInstancePriorityQueue implements TaskPriorityQueue<TaskInstance> {

    /**
     * queue
     */
    private final PriorityQueue<TaskInstance> queue = new PriorityQueue<>(new TaskInstancePriorityComparator());
    private final Set<String> taskInstanceIdentifySet = Collections.synchronizedSet(new HashSet<>());

    /**
     * put task instance to priority queue
     *
     * @param taskInstance taskInstance
     */
    @Override
    public void put(TaskInstance taskInstance) {
        Preconditions.checkNotNull(taskInstance);
        queue.add(taskInstance);
        taskInstanceIdentifySet.add(getTaskInstanceIdentify(taskInstance));
    }

    /**
     * take task info
     *
     * @return task instance
     * @throws TaskPriorityQueueException
     */
    @Override
    public TaskInstance take() throws TaskPriorityQueueException {
        TaskInstance taskInstance = queue.poll();
        if (taskInstance != null) {
            taskInstanceIdentifySet.remove(getTaskInstanceIdentify(taskInstance));
        }
        return taskInstance;
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
        throw new TaskPriorityQueueException(
                "This operation is not currently supported and suggest to use PriorityBlockingQueue if you want！");
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
        taskInstanceIdentifySet.clear();
    }

    /**
     * whether contains the task instance
     *
     * @param taskInstance task instance
     * @return true is contains
     */
    public boolean contains(TaskInstance taskInstance) {
        Preconditions.checkNotNull(taskInstance);
        return taskInstanceIdentifySet.contains(getTaskInstanceIdentify(taskInstance));
    }

    /**
     * remove task
     *
     * @param taskInstance task instance
     * @return true if remove success
     */
    public boolean remove(TaskInstance taskInstance) {
        Preconditions.checkNotNull(taskInstance);
        taskInstanceIdentifySet.remove(getTaskInstanceIdentify(taskInstance));
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

    // since the task instance will not contain taskInstanceId until insert into database
    // So we use processInstanceId + taskCode + version to identify a taskInstance.
    private String getTaskInstanceIdentify(TaskInstance taskInstance) {
        return String.join(
                String.valueOf(taskInstance.getProcessInstanceId()),
                String.valueOf(taskInstance.getTaskCode()),
                String.valueOf(taskInstance.getTaskDefinitionVersion()), "-");
    }

    /**
     * This comparator is used to sort task instances in the standby queue.
     * If the TaskInstance is in the same taskGroup, then we will sort the TaskInstance by {@link TaskInstance#getTaskGroupPriority()} in the taskGroup.
     * Otherwise, we will sort the TaskInstance by {@link TaskInstance#getTaskInstancePriority()} in the workflow.
     */
    private static class TaskInstancePriorityComparator implements Comparator<TaskInstance> {

        @Override
        public int compare(TaskInstance o1, TaskInstance o2) {
            int taskPriorityInTaskGroup = -1 * Integer.compare(o1.getTaskGroupPriority(), o2.getTaskGroupPriority());
            int taskInstancePriorityInWorkflow =
                    Long.compare(o1.getTaskInstancePriority().getCode(), o2.getTaskInstancePriority().getCode());

            if (o1.getTaskGroupId() == o2.getTaskGroupId()) {
                // If at the same taskGroup
                if (taskPriorityInTaskGroup != 0) {
                    return taskPriorityInTaskGroup;
                }
            }
            return taskInstancePriorityInWorkflow;
        }
    }
}
