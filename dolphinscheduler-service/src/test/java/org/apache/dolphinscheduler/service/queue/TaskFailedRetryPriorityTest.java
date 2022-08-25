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

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class TaskFailedRetryPriorityTest {

    private static final int RETRY_TIMES = 10;

    private static final long DELAY_MILLIS = Constants.SLEEP_TIME_MILLIS * (RETRY_TIMES + 1);

    private static final long MAX_DELAY_MILLIS = Constants.SLEEP_TIME_MILLIS * Constants.DEFAULT_MAX_RETRY_COUNT;

    @Test
    public void testTaskFailedRetryPriority() {
        for (int i = 0; i < 10; i++) {
            TaskPriority taskPriority = new TaskPriority();
            taskPriority.setDispatchFailedRetryTimes(10);

            TaskFailedRetryPriority taskFailedRetryPriority = new TaskFailedRetryPriority(taskPriority);
            long delay = taskFailedRetryPriority.getDelay(TimeUnit.MILLISECONDS);
            Assert.assertTrue(DELAY_MILLIS >= delay);
            Assert.assertTrue(taskFailedRetryPriority.getDelayTime() <= DELAY_MILLIS + System.currentTimeMillis());
            Assert.assertEquals(RETRY_TIMES, taskFailedRetryPriority.getTaskPriority().getDispatchFailedRetryTimes());

            TaskPriority maxRetryPriority = new TaskPriority();
            maxRetryPriority.setDispatchFailedRetryTimes(Constants.DEFAULT_MAX_RETRY_COUNT);
            TaskFailedRetryPriority maxTaskFailedRetryPriority = new TaskFailedRetryPriority(maxRetryPriority);
            long maxDelay = maxTaskFailedRetryPriority.getDelay(TimeUnit.MILLISECONDS);
            Assert.assertTrue(MAX_DELAY_MILLIS >= maxDelay);

            // compare
            Assert.assertTrue(maxTaskFailedRetryPriority.compareTo(taskFailedRetryPriority) > 0);
        }
    }
}
