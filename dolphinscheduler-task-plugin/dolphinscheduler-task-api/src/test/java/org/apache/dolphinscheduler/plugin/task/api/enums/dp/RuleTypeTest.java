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

public class RuleTypeTest {

    @Test
    public void testGetCode() {
        Assertions.assertEquals(0, RuleType.SINGLE_TABLE.getCode());
        Assertions.assertEquals(1, RuleType.SINGLE_TABLE_CUSTOM_SQL.getCode());
        Assertions.assertEquals(2, RuleType.MULTI_TABLE_ACCURACY.getCode());
        Assertions.assertEquals(3, RuleType.MULTI_TABLE_COMPARISON.getCode());
    }

    @Test
    public void testGetDescription() {
        Assertions.assertEquals("single_table", RuleType.SINGLE_TABLE.getDescription());
        Assertions.assertEquals("single_table_custom_sql", RuleType.SINGLE_TABLE_CUSTOM_SQL.getDescription());
        Assertions.assertEquals("multi_table_accuracy", RuleType.MULTI_TABLE_ACCURACY.getDescription());
        Assertions.assertEquals("multi_table_comparison", RuleType.MULTI_TABLE_COMPARISON.getDescription());
    }

    @Test
    public void testOf() {
        Assertions.assertEquals(RuleType.SINGLE_TABLE, RuleType.of(0));
        Assertions.assertEquals(RuleType.SINGLE_TABLE_CUSTOM_SQL, RuleType.of(1));
        Assertions.assertEquals(RuleType.MULTI_TABLE_ACCURACY, RuleType.of(2));
        Assertions.assertEquals(RuleType.MULTI_TABLE_COMPARISON, RuleType.of(3));
    }
}
