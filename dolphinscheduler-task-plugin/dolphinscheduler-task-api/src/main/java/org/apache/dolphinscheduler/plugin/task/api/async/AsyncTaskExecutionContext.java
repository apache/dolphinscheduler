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

package org.apache.dolphinscheduler.plugin.task.api.async;

import lombok.Data;
import lombok.NonNull;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Data
public class AsyncTaskExecutionContext implements Delayed {

    private final TaskExecutionContext taskExecutionContext;

    private final AsyncTaskExecuteFunction asyncTaskExecuteFunction;

    private final AsyncTaskCallbackFunction asyncTaskCallbackFunction;

    private long currentStartTime;
    private final long executeInterval;

    public AsyncTaskExecutionContext(@NonNull TaskExecutionContext taskExecutionContext,
                                     @NonNull AsyncTaskExecuteFunction asyncTaskExecuteFunction,
                                     @NonNull AsyncTaskCallbackFunction asyncTaskCallbackFunction) {
        this.taskExecutionContext = taskExecutionContext;
        this.asyncTaskExecuteFunction = asyncTaskExecuteFunction;
        this.asyncTaskCallbackFunction = asyncTaskCallbackFunction;
        this.currentStartTime = System.currentTimeMillis();
        this.executeInterval = Math.max(asyncTaskExecuteFunction.getTaskExecuteInterval().toMillis(), 1000L);
    }

    public void refreshStartTime() {
        currentStartTime = System.currentTimeMillis();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(currentStartTime + executeInterval - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }


    @Override
    public int compareTo(Delayed o) {
        if (o == null) {
            return 1;
        }
        return Long.compare(this.getDelay(TimeUnit.SECONDS), o.getDelay(TimeUnit.SECONDS));
    }
}
