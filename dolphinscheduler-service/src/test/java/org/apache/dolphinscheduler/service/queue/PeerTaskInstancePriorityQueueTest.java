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

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class PeerTaskInstancePriorityQueueTest {

    @Test
    public void put() throws TaskPriorityQueueException {
        PeerTaskInstancePriorityQueue queue = new PeerTaskInstancePriorityQueue();
        TaskInstance taskInstanceHigPriority = createTaskInstance("high", Priority.HIGH, 1);
        TaskInstance taskInstanceMediumPriority = createTaskInstance("medium", Priority.MEDIUM, 1);
        queue.put(taskInstanceHigPriority);
        queue.put(taskInstanceMediumPriority);
        Assert.assertEquals(2, queue.size());
    }

    @Test
    public void take() throws Exception {
        PeerTaskInstancePriorityQueue queue = getPeerTaskInstancePriorityQueue();
        int peekBeforeLength = queue.size();
        queue.take();
        Assert.assertTrue(queue.size() < peekBeforeLength);
    }


    @Test(expected = TaskPriorityQueueException.class)
    public void poll() throws Exception {
        PeerTaskInstancePriorityQueue queue = getPeerTaskInstancePriorityQueue();
        queue.poll(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void peek() throws Exception {
        PeerTaskInstancePriorityQueue queue = getPeerTaskInstancePriorityQueue();
        int peekBeforeLength = queue.size();
        Assert.assertEquals(peekBeforeLength, queue.size());
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
        Assert.assertEquals(taskInstance.getName(), "high");

        taskInstanceHigPriority = createTaskInstance("high", Priority.HIGH, 1);
        taskInstanceMediumPriority = createTaskInstance("medium", Priority.HIGH, 2);
        queue.put(taskInstanceMediumPriority);
        queue.put(taskInstanceHigPriority);
        taskInstance = queue.peek();
        queue.clear();
        Assert.assertEquals(taskInstance.getName(), "medium");

        taskInstanceHigPriority = createTaskInstance("high", Priority.HIGH, 1);
        taskInstanceMediumPriority = createTaskInstance("medium", Priority.MEDIUM, 2);
        queue.put(taskInstanceMediumPriority);
        queue.put(taskInstanceHigPriority);
        taskInstance = queue.peek();
        queue.clear();
        Assert.assertEquals(taskInstance.getName(), "high");

        taskInstanceHigPriority = createTaskInstance("high", Priority.HIGH, 1);
        taskInstanceMediumPriority = createTaskInstance("medium", Priority.MEDIUM, 1);
        queue.put(taskInstanceMediumPriority);
        queue.put(taskInstanceHigPriority);
        taskInstance = queue.peek();
        queue.clear();
        Assert.assertEquals(taskInstance.getName(), "high");

    }

    @Test
    public void size() throws Exception {
        Assert.assertEquals(2, getPeerTaskInstancePriorityQueue().size());
    }

    @Test
    public void contains() throws Exception {
        PeerTaskInstancePriorityQueue queue = new PeerTaskInstancePriorityQueue();
        TaskInstance taskInstanceMediumPriority = createTaskInstance("medium", Priority.MEDIUM, 1);
        queue.put(taskInstanceMediumPriority);
        Assert.assertTrue(queue.contains(taskInstanceMediumPriority));
    }

    @Test
    public void remove() throws Exception {
        PeerTaskInstancePriorityQueue queue = new PeerTaskInstancePriorityQueue();
        TaskInstance taskInstanceMediumPriority = createTaskInstance("medium", Priority.MEDIUM, 1);
        queue.put(taskInstanceMediumPriority);
        int peekBeforeLength = queue.size();
        queue.remove(taskInstanceMediumPriority);
        Assert.assertNotEquals(peekBeforeLength, queue.size());
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
