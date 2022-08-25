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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TaskFailedRetryPriority implements Delayed {

    /**
     * delay time for retries
     */
    private static final Long[] TIME_DELAY;

    /**
     * initialization failure retry delay rule
     */
    static {
        TIME_DELAY = new Long[Constants.DEFAULT_MAX_RETRY_COUNT];
        for (int i = 0; i < Constants.DEFAULT_MAX_RETRY_COUNT; i++) {
            long delayTime = (i + 1) * Constants.SLEEP_TIME_MILLIS;
            TIME_DELAY[i] = delayTime;
        }
    }

    private TaskPriority taskPriority;

    private long delayTime;

    public TaskFailedRetryPriority(TaskPriority taskPriority) {
        this.taskPriority = taskPriority;
        this.delayTime = System.currentTimeMillis() +
                (taskPriority.getDispatchFailedRetryTimes() >= Constants.DEFAULT_MAX_RETRY_COUNT
                        ? TIME_DELAY[Constants.DEFAULT_MAX_RETRY_COUNT - 1]
                        : TIME_DELAY[taskPriority.getDispatchFailedRetryTimes()]);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return delayTime - System.currentTimeMillis();
    }

    @Override
    public int compareTo(Delayed delayed) {
        TaskFailedRetryPriority delayTaskPriority = (TaskFailedRetryPriority) delayed;
        return this.delayTime <= delayTaskPriority.delayTime ? -1 : 1;
    }
}
