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

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.server.master.runner.events.TaskDispatchPriorityEntryEvent;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class TaskDispatchPriorityEntryEventTest {

    @Test
    void getDelay() {
        TaskDispatchPriorityEntryEvent<String> delayEntry = new TaskDispatchPriorityEntryEvent<>(5_000L, "Item");
        assertThat(delayEntry.getDelay(TimeUnit.NANOSECONDS))
                .isWithin(TimeUnit.NANOSECONDS.convert(500, TimeUnit.MILLISECONDS))
                .of(TimeUnit.NANOSECONDS.convert(5_000L, TimeUnit.MILLISECONDS));
    }

    @Test
    void priorityCompare() {
        TaskDispatchPriorityEntryEvent<String> highPriorityEntry =
                new TaskDispatchPriorityEntryEvent<>(15_000L, "1_HIGH");
        TaskDispatchPriorityEntryEvent<String> lowPriorityEntry = new TaskDispatchPriorityEntryEvent<>(5_000L, "3_LOW");
        assertThat(highPriorityEntry.compareTo(lowPriorityEntry) < 0).isTrue();
    }
}
