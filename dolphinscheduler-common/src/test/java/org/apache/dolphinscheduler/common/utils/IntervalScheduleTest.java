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

package org.apache.dolphinscheduler.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class IntervalScheduleTest {

    @Test
    void parseIntervalSchedule() {
        IntervalSchedule intervalSchedule =
                IntervalSchedule.parse("{\"hour\":1,\"minute\":2,\"second\":3,\"repeat\":4}");

        assertEquals(3_723_000L, intervalSchedule.getIntervalMilliseconds());
        assertEquals(4, intervalSchedule.getRepeatCount());
    }

    @Test
    void rejectInvalidIntervalSchedule() {
        assertThrows(IllegalArgumentException.class,
                () -> IntervalSchedule.parse("{\"second\":0}"));
        assertThrows(IllegalArgumentException.class,
                () -> IntervalSchedule.parse("{\"second\":-1,\"repeat\":-1}"));
        assertThrows(IllegalArgumentException.class,
                () -> IntervalSchedule.parse("{\"minute\":60,\"repeat\":1}"));
        assertThrows(IllegalArgumentException.class,
                () -> IntervalSchedule.parse("{\"second\":60,\"repeat\":1}"));
        assertThrows(IllegalArgumentException.class,
                () -> IntervalSchedule.parse("{\"second\":1}"));
    }
}
