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

import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueueImpl;
import org.junit.Test;

import static org.junit.Assert.*;

public class TaskUpdateQueueTest {

    /**
     * test put
     */
    @Test
    public void testQueue() throws Exception{

        /**
         * 1_1_2_1_default
         * 1_1_2_2_default
         * 1_1_0_3_default
         * 1_1_0_4_default
         */
        TaskPriority taskInfo1 = new TaskPriority(1, 1, 2, 1, "default");
        TaskPriority taskInfo2 = new TaskPriority(1, 1, 2, 2, "default");
        TaskPriority taskInfo3 = new TaskPriority(1, 1, 0, 3, "default");
        TaskPriority taskInfo4 = new TaskPriority(1, 1, 0, 4, "default");

        TaskPriorityQueue queue = new TaskPriorityQueueImpl();
        queue.put(taskInfo1);
        queue.put(taskInfo2);
        queue.put(taskInfo3);
        queue.put(taskInfo4);

        assertEquals(taskInfo3, queue.take());
        assertEquals(taskInfo4, queue.take());
        assertEquals(taskInfo1, queue.take());
        assertEquals(taskInfo2, queue.take());
    }
}
