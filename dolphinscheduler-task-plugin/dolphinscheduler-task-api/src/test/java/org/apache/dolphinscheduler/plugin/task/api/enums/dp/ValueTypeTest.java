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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ValueTypeTest {
    /**
     * 0-string
     * 1-list
     * 2-number
     * 3-sql
     */
    @Test
    public void testGetCode() {
        assertEquals(0, ValueType.STRING.getCode());
        assertEquals(1, ValueType.LIST.getCode());
        assertEquals(2, ValueType.NUMBER.getCode());
        assertEquals(3, ValueType.LIKE_SQL.getCode());
    }
 
    @Test
    public void testGetDescription() {
        assertEquals("string", ValueType.STRING.getDescription());
        assertEquals("list", ValueType.LIST.getDescription());
        assertEquals("number", ValueType.NUMBER.getDescription());
        assertEquals("sql", ValueType.LIKE_SQL.getDescription());
    }

    @Test
    public void testOf() {
        assertEquals(ValueType.STRING, ValueType.of(0));
        assertEquals(ValueType.LIST, ValueType.of(1));
        assertEquals(ValueType.NUMBER, ValueType.of(2));
        assertEquals(ValueType.LIKE_SQL, ValueType.of(3));
    }
}
