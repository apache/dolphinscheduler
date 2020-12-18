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

package queue;

import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.service.queue.PeerTaskInstancePriorityQueue;

import org.junit.Assert;
import org.junit.Test;

/**
 * Task instances priority queue implementation
 * All the task instances are in the same process instance.
 */
public class PeerTaskInstancePriorityQueueTest {

    @Test
    public void testPut() throws Exception {
        PeerTaskInstancePriorityQueue queue = new PeerTaskInstancePriorityQueue();
        TaskInstance taskInstanceHigPriority = createTaskInstance("high", Priority.HIGH);
        TaskInstance taskInstanceMediumPriority = createTaskInstance("high", Priority.MEDIUM);
        queue.put(taskInstanceHigPriority);
        queue.put(taskInstanceMediumPriority);
        Assert.assertEquals(2,queue.size());
    }

    @Test
    public void testPeek() throws Exception {
        PeerTaskInstancePriorityQueue queue = getPeerTaskInstancePriorityQueue();
        int peekBeforeLength = queue.size();
        queue.peek();
        Assert.assertEquals(peekBeforeLength,queue.size());

    }

    @Test
    public void testTake() throws Exception {
        PeerTaskInstancePriorityQueue queue = getPeerTaskInstancePriorityQueue();
        int peekBeforeLength = queue.size();
        queue.take();
        Assert.assertTrue(queue.size() < peekBeforeLength);
    }

    /**
     * get queue
     *
     * @return queue
     * @throws Exception
     */
    private PeerTaskInstancePriorityQueue getPeerTaskInstancePriorityQueue() throws Exception {
        PeerTaskInstancePriorityQueue queue = new PeerTaskInstancePriorityQueue();
        TaskInstance taskInstanceHigPriority = createTaskInstance("high", Priority.HIGH);
        TaskInstance taskInstanceMediumPriority = createTaskInstance("high", Priority.MEDIUM);
        queue.put(taskInstanceHigPriority);
        queue.put(taskInstanceMediumPriority);
        return queue;
    }

    /**
     * create task instance
     *
     * @param name      name
     * @param priority  priority
     * @return
     */
    private TaskInstance createTaskInstance(String name, Priority priority) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setName(name);
        taskInstance.setTaskInstancePriority(priority);
        return taskInstance;
    }

}