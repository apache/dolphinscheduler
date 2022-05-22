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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class TaskPriorityQueueImplTest {

    @Test
    public void testSort() {
        TaskPriority priorityOne = new TaskPriority(1, 0, 0, 0, 1, "default");
        TaskPriority priorityTwo = new TaskPriority(2, 0, 0, 0, 1, "default");
        TaskPriority priorityThree = new TaskPriority(3, 0, 0, 0, 1, "default");
        List<TaskPriority> taskPrioritys = Arrays.asList(priorityOne, priorityThree, priorityTwo);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
            Arrays.asList(priorityOne, priorityTwo, priorityThree),
            taskPrioritys
        );

        priorityOne = new TaskPriority(0, 1, 0, 0, 1, "default");
        priorityTwo = new TaskPriority(0, 2, 0, 0, 1, "default");
        priorityThree = new TaskPriority(0, 3, 0, 0, 1, "default");
        taskPrioritys = Arrays.asList(priorityOne, priorityThree, priorityTwo);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
            Arrays.asList(priorityOne, priorityTwo, priorityThree),
            taskPrioritys
        );

        priorityOne = new TaskPriority(0, 0, 1, 0, 1, "default");
        priorityTwo = new TaskPriority(0, 0, 2, 0, 1, "default");
        priorityThree = new TaskPriority(0, 0, 3, 0, 1, "default");
        taskPrioritys = Arrays.asList(priorityOne, priorityThree, priorityTwo);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
            Arrays.asList(priorityOne, priorityTwo, priorityThree),
            taskPrioritys
        );

        priorityOne = new TaskPriority(0, 0, 0, 1, 1, "default");
        priorityTwo = new TaskPriority(0, 0, 0, 2, 1, "default");
        priorityThree = new TaskPriority(0, 0, 0, 3, 1, "default");
        taskPrioritys = Arrays.asList(priorityOne, priorityThree, priorityTwo);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
            Arrays.asList(priorityOne, priorityTwo, priorityThree),
            taskPrioritys
        );

        priorityOne = new TaskPriority(0, 0, 0, 0, 1, "default_1");
        priorityTwo = new TaskPriority(0, 0, 0, 0, 1,"default_2");
        priorityThree = new TaskPriority(0, 0, 0, 0, 1, "default_3");
        taskPrioritys = Arrays.asList(priorityOne, priorityThree, priorityTwo);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
            Arrays.asList(priorityOne, priorityTwo, priorityThree),
            taskPrioritys
        );

        priorityOne = new TaskPriority(0, 0, 0, 0, 2, "default_1");
        priorityTwo = new TaskPriority(0, 0, 0, 0, 1,"default_2");
        priorityThree = new TaskPriority(0, 0, 0, 0, 3, "default_3");
        taskPrioritys = Arrays.asList(priorityOne, priorityThree, priorityTwo);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
                Arrays.asList(priorityThree, priorityOne, priorityTwo),
                taskPrioritys
        );

        priorityOne = new TaskPriority(0, 0, 0, 0, 1, "default_1");
        priorityTwo = new TaskPriority(0, 0, 0, 0, 1,"default_2");
        priorityThree = new TaskPriority(0, 0, 0, 0, 3, "default_3");
        taskPrioritys = Arrays.asList(priorityOne, priorityThree, priorityTwo);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
                Arrays.asList(priorityThree, priorityOne, priorityTwo),
                taskPrioritys
        );

        priorityTwo = new TaskPriority(0, 0, 0, 0, 1,"default_1");
        priorityOne = new TaskPriority(0, 0, 0, 0, 1, "default_1");
        priorityThree = new TaskPriority(0, 0, 0, 0, 3, "default_1");
        taskPrioritys = Arrays.asList(priorityTwo, priorityOne, priorityThree);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
                Arrays.asList(priorityThree, priorityTwo, priorityOne),
                taskPrioritys
        );
    }

    @Test
    public void put() throws Exception {
        TaskPriorityQueue queue = getPriorityQueue();
        Assert.assertEquals(2, queue.size());
    }

    @Test
    public void take() throws Exception {
        TaskPriorityQueue queue = getPriorityQueue();
        int peekBeforeLength = queue.size();
        queue.take();
        Assert.assertTrue(queue.size() < peekBeforeLength);
    }

    @Test
    public void poll() throws Exception {
        TaskPriorityQueue queue = getPriorityQueue();
        int peekBeforeLength = queue.size();
        queue.poll(1000, TimeUnit.MILLISECONDS);
        queue.poll(1000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(queue.size() == 0);
        queue.poll(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void size() throws Exception {
        Assert.assertTrue(getPriorityQueue().size() == 2);
    }

    /**
     * get queue
     *
     * @return queue
     * @throws Exception
     */
    private TaskPriorityQueue getPriorityQueue() throws Exception {
        TaskPriorityQueue queue = new TaskPriorityQueueImpl();
        TaskPriority taskInstanceHigPriority = createTaskPriority(Priority.HIGH.getCode(), 1);
        TaskPriority taskInstanceMediumPriority = createTaskPriority(Priority.MEDIUM.getCode(), 2);
        queue.put(taskInstanceHigPriority);
        queue.put(taskInstanceMediumPriority);
        return queue;
    }

    /**
     * create task priority
     *
     * @param priority
     * @param processInstanceId
     * @return
     */
    private TaskPriority createTaskPriority(Integer priority, Integer processInstanceId) {
        TaskPriority priorityOne = new TaskPriority(priority, processInstanceId, 0, 0, 1, "default");
        return priorityOne;
    }
}