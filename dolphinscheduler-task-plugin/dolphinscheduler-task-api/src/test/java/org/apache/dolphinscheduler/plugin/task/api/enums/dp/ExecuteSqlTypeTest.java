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

public class ExecuteSqlTypeTest {

    @Test
    public void testGetCode() {
        Assertions.assertEquals(0, ExecuteSqlType.MIDDLE.getCode());
        Assertions.assertEquals(1, ExecuteSqlType.STATISTICS.getCode());
        Assertions.assertEquals(2, ExecuteSqlType.COMPARISON.getCode());
    }

    @Test
    public void testGetDescription() {
        Assertions.assertEquals("middle", ExecuteSqlType.MIDDLE.getDescription());
        Assertions.assertEquals("statistics", ExecuteSqlType.STATISTICS.getDescription());
        Assertions.assertEquals("comparison", ExecuteSqlType.COMPARISON.getDescription());
    }

    @Test
    public void testOf() {
        Assertions.assertEquals(ExecuteSqlType.MIDDLE, ExecuteSqlType.of(0));
        Assertions.assertEquals(ExecuteSqlType.STATISTICS, ExecuteSqlType.of(1));
        Assertions.assertEquals(ExecuteSqlType.COMPARISON, ExecuteSqlType.of(2));
    }
}
