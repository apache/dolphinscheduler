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

package org.apache.dolphinscheduler.server.master.runner.execute;

import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.runner.operator.TaskExecuteRunnableOperatorManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PriorityTaskExecuteRunnableTest {

    @Test
    public void testCompareTo() {
        TaskExecuteRunnableOperatorManager taskOperatorManager = new TaskExecuteRunnableOperatorManager();

        ProcessInstance workflowInstance = new ProcessInstance();
        workflowInstance.setId(1);
        workflowInstance.setProcessInstancePriority(Priority.HIGH);

        TaskInstance t1 = new TaskInstance();
        t1.setId(1);
        t1.setTaskInstancePriority(Priority.HIGH);

        TaskInstance t2 = new TaskInstance();
        t2.setId(1);
        t2.setTaskInstancePriority(Priority.HIGH);

        TaskExecutionContext context1 = new TaskExecutionContext();
        TaskExecutionContext context2 = new TaskExecutionContext();
        PriorityTaskExecuteRunnable p1 =
                new DefaultTaskExecuteRunnable(workflowInstance, t1, context1, taskOperatorManager);
        PriorityTaskExecuteRunnable p2 =
                new DefaultTaskExecuteRunnable(workflowInstance, t2, context2, taskOperatorManager);

        Assertions.assertEquals(0, p1.compareTo(p2));

        // the higher priority, the higher priority
        t2.setTaskInstancePriority(Priority.MEDIUM);
        Assertions.assertTrue(p1.compareTo(p2) < 0);

        // the smaller dispatch fail times, the higher priority
        context1.setDispatchFailTimes(1);
        Assertions.assertTrue(p1.compareTo(p2) > 0);
    }

}
