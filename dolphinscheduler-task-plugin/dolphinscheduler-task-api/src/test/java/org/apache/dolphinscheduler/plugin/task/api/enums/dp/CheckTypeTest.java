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

package org.apache.dolphinscheduler.plugin.task.api.enums.dp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckTypeTest {

    @Test
    public void testGetCode() {
        Assertions.assertEquals(0, CheckType.COMPARISON_MINUS_STATISTICS.getCode());
        Assertions.assertEquals(1, CheckType.STATISTICS_MINUS_COMPARISON.getCode());
        Assertions.assertEquals(2, CheckType.STATISTICS_COMPARISON_PERCENTAGE.getCode());
        Assertions.assertEquals(3, CheckType.STATISTICS_COMPARISON_DIFFERENCE_COMPARISON_PERCENTAGE.getCode());
    }

    @Test
    public void testGetDescription() {
        Assertions.assertEquals("comparison_minus_statistics", CheckType.COMPARISON_MINUS_STATISTICS.getDescription());
        Assertions.assertEquals("statistics_minus_comparison", CheckType.STATISTICS_MINUS_COMPARISON.getDescription());
        Assertions.assertEquals("statistics_comparison_percentage",
                CheckType.STATISTICS_COMPARISON_PERCENTAGE.getDescription());
        Assertions.assertEquals("statistics_comparison_difference_comparison_percentage",
                CheckType.STATISTICS_COMPARISON_DIFFERENCE_COMPARISON_PERCENTAGE.getDescription());
    }

    @Test
    public void testOf() {
        Assertions.assertEquals(CheckType.COMPARISON_MINUS_STATISTICS, CheckType.of(0));
    }
}
