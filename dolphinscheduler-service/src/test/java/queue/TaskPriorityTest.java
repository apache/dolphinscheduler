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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TaskPriorityTest {

    @Test
    public void testSort() {
        TaskPriority priorityOne = new TaskPriority(1, 0, 0, 0, "default");
        TaskPriority priorityTwo = new TaskPriority(2, 0, 0, 0, "default");
        TaskPriority priorityThree = new TaskPriority(3, 0, 0, 0, "default");
        List<TaskPriority> taskPrioritys = Arrays.asList(priorityOne, priorityThree, priorityTwo);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
                Arrays.asList(priorityOne, priorityTwo, priorityThree),
                taskPrioritys
        );

        priorityOne = new TaskPriority(0, 1, 0, 0, "default");
        priorityTwo = new TaskPriority(0, 2, 0, 0, "default");
        priorityThree = new TaskPriority(0, 3, 0, 0, "default");
        taskPrioritys = Arrays.asList(priorityOne, priorityThree, priorityTwo);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
                Arrays.asList(priorityOne, priorityTwo, priorityThree),
                taskPrioritys
        );

        priorityOne = new TaskPriority(0, 0, 1, 0, "default");
        priorityTwo = new TaskPriority(0, 0, 2, 0, "default");
        priorityThree = new TaskPriority(0, 0, 3, 0, "default");
        taskPrioritys = Arrays.asList(priorityOne, priorityThree, priorityTwo);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
                Arrays.asList(priorityOne, priorityTwo, priorityThree),
                taskPrioritys
        );

        priorityOne = new TaskPriority(0, 0, 0, 1, "default");
        priorityTwo = new TaskPriority(0, 0, 0, 2, "default");
        priorityThree = new TaskPriority(0, 0, 0, 3, "default");
        taskPrioritys = Arrays.asList(priorityOne, priorityThree, priorityTwo);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
                Arrays.asList(priorityOne, priorityTwo, priorityThree),
                taskPrioritys
        );

        priorityOne = new TaskPriority(0, 0, 0, 0, "default_1");
        priorityTwo = new TaskPriority(0, 0, 0, 0, "default_2");
        priorityThree = new TaskPriority(0, 0, 0, 0, "default_3");
        taskPrioritys = Arrays.asList(priorityOne, priorityThree, priorityTwo);
        Collections.sort(taskPrioritys);
        Assert.assertEquals(
                Arrays.asList(priorityOne, priorityTwo, priorityThree),
                taskPrioritys
        );
    }
}
