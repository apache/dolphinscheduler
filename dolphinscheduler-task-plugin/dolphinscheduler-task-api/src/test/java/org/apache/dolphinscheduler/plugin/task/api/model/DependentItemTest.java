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

package org.apache.dolphinscheduler.plugin.task.api.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class DependentItemTest {

    @ParameterizedTest
    @CsvSource({
            "123, 2001-123-hour-currentHour, hour, currentHour",
            "0, 2001-0-week-lastWeek, week, lastWeek",
            "-1, 2001--1-day-today, day, today",
            "-9223372036854775808, 2001--9223372036854775808-month-lastMonthEnd, month, lastMonthEnd"
    })
    public void testKeyRoundTrip(long depTaskCode, String key, String cycle, String dateValue) {
        DependentItem dependentItem = new DependentItem();
        dependentItem.setDefinitionCode(2001);
        dependentItem.setDepTaskCode(depTaskCode);
        dependentItem.setCycle(cycle);
        dependentItem.setDateValue(dateValue);

        Assertions.assertEquals(key, dependentItem.getKey());

        DependentItem parsedDependentItem = new DependentItem().fromKey(key);
        Assertions.assertEquals(dependentItem.getDefinitionCode(), parsedDependentItem.getDefinitionCode());
        Assertions.assertEquals(dependentItem.getDepTaskCode(), parsedDependentItem.getDepTaskCode());
        Assertions.assertEquals(dependentItem.getCycle(), parsedDependentItem.getCycle());
        Assertions.assertEquals(dependentItem.getDateValue(), parsedDependentItem.getDateValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "invalid-key",
            "2001-1-day",
            "2001-1-day-",
            "2001-1-day-today-extra",
            "2001---1-day-today"
    })
    public void testFromKeyWithInvalidFormat(String key) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new DependentItem().fromKey(key));
    }
}
