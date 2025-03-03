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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PriorityDelayEntryTest {

    private final long baseDelayTimeMills = 1000L;

    @Test
    public void testCompareToDataNullEqual() {
        assertThrows(NullPointerException.class, () -> new PriorityDelayEntry<>(baseDelayTimeMills, null));
    }

    @Test
    public void testCompareToPriorityDifferent() {
        PriorityDelayEntry<String> entry1 = new PriorityDelayEntry<>(baseDelayTimeMills + 100, "A");
        PriorityDelayEntry<String> entry2 = new PriorityDelayEntry<>(baseDelayTimeMills, "B");
        assertEquals(-1, entry1.compareTo(entry2)); // "A" has higher priority than "B"
        assertEquals(1, entry2.compareTo(entry1)); // "B" has lower priority than "A"
    }

    @Test
    public void testCompareToPriorityEqualDifferentDelay() {
        PriorityDelayEntry<String> entry1 = new PriorityDelayEntry<>(baseDelayTimeMills, "A");
        PriorityDelayEntry<String> entry2 = new PriorityDelayEntry<>(baseDelayTimeMills + 100, "A");
        assertEquals(-1, entry1.compareTo(entry2)); // entry1 has earlier trigger time
        assertEquals(1, entry2.compareTo(entry1)); // entry2 has later trigger time
    }

    @Test
    public void testCompareToEqualPriorityAndDelay() {
        PriorityDelayEntry<String> entry1 = new PriorityDelayEntry<>(baseDelayTimeMills, "A");
        PriorityDelayEntry<String> entry2 = new PriorityDelayEntry<>(baseDelayTimeMills, "A");
        assertEquals(0, entry1.compareTo(entry2));
    }
}
