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

import static org.apache.dolphinscheduler.common.thread.ThreadUtils.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TimeBasedTaskExecutionRunnableComparableEntryTest {

    private static final long TEST_DELAY_MILLS = 1000L;
    private String testData = "testData";
    private TimeBasedTaskExecutionRunnableComparableEntry<String> entry;

    @BeforeEach
    public void setUp() {
        entry = new TimeBasedTaskExecutionRunnableComparableEntry<>(TEST_DELAY_MILLS, testData);
    }

    @Test
    public void constructor_NullData_ThrowsNullPointerException() {
        try {
            new TimeBasedTaskExecutionRunnableComparableEntry<>(TEST_DELAY_MILLS, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException e) {
            assertEquals("data is null", e.getMessage());
        }
    }

    @Test
    public void getDelay_BeforeTriggerTime_ReturnsPositive() {
        entry = new TimeBasedTaskExecutionRunnableComparableEntry<>(TEST_DELAY_MILLS, testData);
        sleep(500L);
        long remainTime = entry.getDelay(TimeUnit.MILLISECONDS);
        assertTrue(remainTime > 0);
    }

    @Test
    public void getDelay_AtTriggerTime_ReturnsZero() {
        entry = new TimeBasedTaskExecutionRunnableComparableEntry<>(TEST_DELAY_MILLS, testData);
        sleep(1000L);
        long remainTime = entry.getDelay(TimeUnit.MILLISECONDS);
        long tolerance = 100;
        assertTrue(remainTime <= tolerance);
    }

    @Test
    public void getDelay_AfterTriggerTime_ReturnsNegative() {
        entry = new TimeBasedTaskExecutionRunnableComparableEntry<>(TEST_DELAY_MILLS, testData);
        sleep(1500L);
        long remainTime = entry.getDelay(TimeUnit.MILLISECONDS);
        assertTrue(remainTime < 0);
    }

    @Test
    public void getDelay_DifferentTimeUnits_ReturnsCorrectValues() {
        long remainTimeMillis = entry.getDelay(TimeUnit.MILLISECONDS);
        long remainTimeSeconds = entry.getDelay(TimeUnit.SECONDS);

        assertTrue(remainTimeSeconds <= remainTimeMillis / 1000);
    }

    @Test
    public void compareTo_SameObject_ReturnsZero() {
        assertEquals(0, entry.compareTo(entry));
    }
}
