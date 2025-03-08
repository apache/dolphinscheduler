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

package org.apache.dolphinscheduler.server.master.runner.queue;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import java.util.Objects;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;

@Getter
public class TimeBasedTaskExecutionRunnableComparableEntry
        implements
            Comparable<TimeBasedTaskExecutionRunnableComparableEntry> {

    private final long delayTimeMills;

    private final ITaskExecutionRunnable data;

    public TimeBasedTaskExecutionRunnableComparableEntry(long delayTimeMills, ITaskExecutionRunnable data) {
        this.delayTimeMills = delayTimeMills;
        this.data = checkNotNull(data, "data is null");
    }

    @Override
    public int compareTo(@NotNull TimeBasedTaskExecutionRunnableComparableEntry other) {
        int delayTimeCompareResult = Long.compare(delayTimeMills, other.delayTimeMills);
        if (delayTimeCompareResult != 0) {
            return delayTimeCompareResult;
        }
        return data.compareTo(other.data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TimeBasedTaskExecutionRunnableComparableEntry that = (TimeBasedTaskExecutionRunnableComparableEntry) obj;
        return delayTimeMills == that.delayTimeMills && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delayTimeMills, data);
    }
}
