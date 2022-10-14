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

import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class PeerTaskInstancePriorityQueueTest {

    @Test
    public void put() throws TaskPriorityQueueException {
        PeerTaskInstancePriorityQueue queue = new PeerTaskInstancePriorityQueue();
        TaskInstance taskInstanceHigPriority = createTaskInstance("high", Priority.HIGH, 1);
        TaskInstance taskInstanceMediumPriority = createTaskInstance("medium", Priority.MEDIUM, 1);
        queue.put(taskInstanceHigPriority);
        queue.put(taskInstanceMediumPriority);
        Assertions.assertEquals(2, queue.size());
        Assertions.assertTrue(queue.contains(taskInstanceHigPriority));
        Assertions.assertTrue(queue.contains(taskInstanceMediumPriority));
    }

    @Test
    public void take() throws Exception {
        PeerTaskInstancePriorityQueue queue = getPeerTaskInstancePriorityQueue();
        int peekBeforeLength = queue.size();
        queue.take();
        Assertions.assertTrue(queue.size() < peekBeforeLength);
    }


    @Test
    public void poll() throws Exception {
        PeerTaskInstancePriorityQueue queue = getPeerTaskInstancePriorityQueue();
        Assertions.assertThrows(TaskPriorityQueueException.class, () -> {
            queue.poll(1000, TimeUnit.MILLISECONDS);
        });
    }

    @Test
    public void peek() throws Exception {
        PeerTaskInstancePriorityQueue queue = getPeerTaskInstancePriorityQueue();
        int peekBeforeLength = queue.size();
        Assertions.assertEquals(peekBeforeLength, queue.size());
    }

    @Test
    public void peekTaskGroupPriority() throws Exception{
        PeerTaskInstancePriorityQueue queue = new PeerTaskInstancePriorityQueue();
        TaskInstance taskInstanceHigPriority = createTaskInstance("high", Priority.HIGH, 2);
        TaskInstance taskInstanceMediumPriority = createTaskInstance("medium", Priority.HIGH, 1);
        queue.put(taskInstanceMediumPriority);
        queue.put(taskInstanceHigPriority);
        TaskInstance taskInstance = queue.peek();
        queue.clear();
        Assertions.assertEquals(taskInstance.getName(), "high");

        taskInstanceHigPriority = createTaskInstance("high", Priority.HIGH, 1);
        taskInstanceMediumPriority = createTaskInstance("medium", Priority.HIGH, 2);
        queue.put(taskInstanceMediumPriority);
        queue.put(taskInstanceHigPriority);
        taskInstance = queue.peek();
        queue.clear();
        Assertions.assertEquals(taskInstance.getName(), "medium");

        taskInstanceHigPriority = createTaskInstance("high", Priority.HIGH, 1);
        taskInstanceMediumPriority = createTaskInstance("medium", Priority.MEDIUM, 2);
        queue.put(taskInstanceMediumPriority);
        queue.put(taskInstanceHigPriority);
        taskInstance = queue.peek();
        queue.clear();
        Assertions.assertEquals(taskInstance.getName(), "high");

        taskInstanceHigPriority = createTaskInstance("high", Priority.HIGH, 1);
        taskInstanceMediumPriority = createTaskInstance("medium", Priority.MEDIUM, 1);
        queue.put(taskInstanceMediumPriority);
        queue.put(taskInstanceHigPriority);
        taskInstance = queue.peek();
        queue.clear();
        Assertions.assertEquals(taskInstance.getName(), "high");

    }

    @Test
    public void size() throws Exception {
        Assertions.assertEquals(2, getPeerTaskInstancePriorityQueue().size());
    }

    @Test
    public void contains() throws Exception {
        PeerTaskInstancePriorityQueue queue = new PeerTaskInstancePriorityQueue();
        TaskInstance taskInstanceMediumPriority = createTaskInstance("medium", Priority.MEDIUM, 1);
        queue.put(taskInstanceMediumPriority);
        Assertions.assertTrue(queue.contains(taskInstanceMediumPriority));
        TaskInstance taskInstance2 = createTaskInstance("medium2", Priority.MEDIUM, 1);
        taskInstance2.setProcessInstanceId(2);
        Assertions.assertFalse(queue.contains(taskInstance2));
    }

    @Test
    public void remove() throws Exception {
        PeerTaskInstancePriorityQueue queue = new PeerTaskInstancePriorityQueue();
        TaskInstance taskInstanceMediumPriority = createTaskInstance("medium", Priority.MEDIUM, 1);
        queue.put(taskInstanceMediumPriority);
        int peekBeforeLength = queue.size();
        queue.remove(taskInstanceMediumPriority);
        Assertions.assertNotEquals(peekBeforeLength, queue.size());
        Assertions.assertFalse(queue.contains(taskInstanceMediumPriority));
    }

    /**
     * get queue
     *
     * @return queue
     * @throws Exception
     */
    private PeerTaskInstancePriorityQueue getPeerTaskInstancePriorityQueue() throws Exception {
        PeerTaskInstancePriorityQueue queue = new PeerTaskInstancePriorityQueue();
        TaskInstance taskInstanceHigPriority = createTaskInstance("high", Priority.HIGH, 1);
        TaskInstance taskInstanceMediumPriority = createTaskInstance("medium", Priority.MEDIUM, 1);
        taskInstanceHigPriority.setTaskGroupPriority(3);
        taskInstanceMediumPriority.setTaskGroupPriority(2);
        queue.put(taskInstanceMediumPriority);
        queue.put(taskInstanceHigPriority);
        return queue;
    }

    /**
     * create task instance
     *
     * @param name     name
     * @param priority priority
     * @return
     */
    private TaskInstance createTaskInstance(String name, Priority priority, int taskGroupPriority) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setName(name);
        taskInstance.setTaskInstancePriority(priority);
        taskInstance.setTaskGroupPriority(taskGroupPriority);
        return taskInstance;
    }
}
